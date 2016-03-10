package com.facebook.share.internal;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.util.Pair;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookGraphResponseException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.FacebookRequestError;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphRequest.ParcelableResourceWithMimeType;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.internal.AnalyticsEvents;
import com.facebook.internal.AppCall;
import com.facebook.internal.CallbackManagerImpl;
import com.facebook.internal.CallbackManagerImpl.Callback;
import com.facebook.internal.NativeAppCallAttachmentStore;
import com.facebook.internal.NativeAppCallAttachmentStore.Attachment;
import com.facebook.internal.NativeProtocol;
import com.facebook.internal.Utility;
import com.facebook.internal.Utility.Mapper;
import com.facebook.share.Sharer.Result;
import com.facebook.share.internal.OpenGraphJSONUtility.PhotoJSONProcessor;
import com.facebook.share.model.ShareOpenGraphAction;
import com.facebook.share.model.ShareOpenGraphContent;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.model.ShareVideoContent;
import com.facebook.share.widget.LikeView.ObjectType;
import com.helpshift.constants.FaqsColumns;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public final class ShareInternalUtility {
    public static final String MY_PHOTOS = "me/photos";
    private static final String MY_STAGING_RESOURCES = "me/staging_resources";
    private static final String STAGING_PARAM = "file";

    public static void invokeCallbackWithException(FacebookCallback<Result> callback, Exception exception) {
        if (exception instanceof FacebookException) {
            invokeOnErrorCallback((FacebookCallback) callback, (FacebookException) exception);
        } else {
            invokeCallbackWithError(callback, "Error preparing share content: " + exception.getLocalizedMessage());
        }
    }

    public static void invokeCallbackWithError(FacebookCallback<Result> callback, String error) {
        invokeOnErrorCallback((FacebookCallback) callback, error);
    }

    public static void invokeCallbackWithResults(FacebookCallback<Result> callback, String postId, GraphResponse graphResponse) {
        FacebookRequestError requestError = graphResponse.getError();
        if (requestError != null) {
            String errorMessage = requestError.getErrorMessage();
            if (Utility.isNullOrEmpty(errorMessage)) {
                errorMessage = "Unexpected error sharing.";
            }
            invokeOnErrorCallback(callback, graphResponse, errorMessage);
            return;
        }
        invokeOnSuccessCallback(callback, postId);
    }

    public static String getNativeDialogCompletionGesture(Bundle result) {
        if (result.containsKey(NativeProtocol.RESULT_ARGS_DIALOG_COMPLETION_GESTURE_KEY)) {
            return result.getString(NativeProtocol.RESULT_ARGS_DIALOG_COMPLETION_GESTURE_KEY);
        }
        return result.getString(NativeProtocol.EXTRA_DIALOG_COMPLETION_GESTURE_KEY);
    }

    public static String getShareDialogPostId(Bundle result) {
        if (result.containsKey(ShareConstants.RESULT_POST_ID)) {
            return result.getString(ShareConstants.RESULT_POST_ID);
        }
        if (result.containsKey(ShareConstants.EXTRA_RESULT_POST_ID)) {
            return result.getString(ShareConstants.EXTRA_RESULT_POST_ID);
        }
        return result.getString(ShareConstants.WEB_DIALOG_RESULT_PARAM_POST_ID);
    }

    public static boolean handleActivityResult(int requestCode, int resultCode, Intent data, ResultProcessor resultProcessor) {
        AppCall appCall = getAppCallFromActivityResult(requestCode, resultCode, data);
        if (appCall == null) {
            return false;
        }
        NativeAppCallAttachmentStore.cleanupAttachmentsForCall(appCall.getCallId());
        if (resultProcessor == null) {
            return true;
        }
        FacebookException exception = NativeProtocol.getExceptionFromErrorData(NativeProtocol.getErrorDataFromResultIntent(data));
        if (exception == null) {
            resultProcessor.onSuccess(appCall, NativeProtocol.getSuccessResultsFromIntent(data));
            return true;
        } else if (exception instanceof FacebookOperationCanceledException) {
            resultProcessor.onCancel(appCall);
            return true;
        } else {
            resultProcessor.onError(appCall, exception);
            return true;
        }
    }

    public static ResultProcessor getShareResultProcessor(final FacebookCallback<Result> callback) {
        return new ResultProcessor(callback) {
            public void onSuccess(AppCall appCall, Bundle results) {
                if (results != null) {
                    String gesture = ShareInternalUtility.getNativeDialogCompletionGesture(results);
                    if (gesture == null || "post".equalsIgnoreCase(gesture)) {
                        ShareInternalUtility.invokeOnSuccessCallback(callback, ShareInternalUtility.getShareDialogPostId(results));
                    } else if ("cancel".equalsIgnoreCase(gesture)) {
                        ShareInternalUtility.invokeOnCancelCallback(callback);
                    } else {
                        ShareInternalUtility.invokeOnErrorCallback(callback, new FacebookException(NativeProtocol.ERROR_UNKNOWN_ERROR));
                    }
                }
            }

            public void onCancel(AppCall appCall) {
                ShareInternalUtility.invokeOnCancelCallback(callback);
            }

            public void onError(AppCall appCall, FacebookException error) {
                ShareInternalUtility.invokeOnErrorCallback(callback, error);
            }
        };
    }

    private static AppCall getAppCallFromActivityResult(int requestCode, int resultCode, Intent data) {
        UUID callId = NativeProtocol.getCallIdFromIntent(data);
        if (callId == null) {
            return null;
        }
        return AppCall.finishPendingCall(callId, requestCode);
    }

    public static void registerStaticShareCallback(final int requestCode) {
        CallbackManagerImpl.registerStaticCallback(requestCode, new Callback() {
            public boolean onActivityResult(int resultCode, Intent data) {
                return ShareInternalUtility.handleActivityResult(requestCode, resultCode, data, ShareInternalUtility.getShareResultProcessor(null));
            }
        });
    }

    public static void registerSharerCallback(final int requestCode, CallbackManager callbackManager, final FacebookCallback<Result> callback) {
        if (callbackManager instanceof CallbackManagerImpl) {
            ((CallbackManagerImpl) callbackManager).registerCallback(requestCode, new Callback() {
                public boolean onActivityResult(int resultCode, Intent data) {
                    return ShareInternalUtility.handleActivityResult(requestCode, resultCode, data, ShareInternalUtility.getShareResultProcessor(callback));
                }
            });
            return;
        }
        throw new FacebookException("Unexpected CallbackManager, please use the provided Factory.");
    }

    public static List<String> getPhotoUrls(SharePhotoContent photoContent, final UUID appCallId) {
        if (photoContent != null) {
            List<SharePhoto> photos = photoContent.getPhotos();
            if (photos != null) {
                List<Attachment> attachments = Utility.map(photos, new Mapper<SharePhoto, Attachment>() {
                    public Attachment apply(SharePhoto item) {
                        return ShareInternalUtility.getAttachment(appCallId, item);
                    }
                });
                List<String> attachmentUrls = Utility.map(attachments, new Mapper<Attachment, String>() {
                    public String apply(Attachment item) {
                        return item.getAttachmentUrl();
                    }
                });
                NativeAppCallAttachmentStore.addAttachments(attachments);
                return attachmentUrls;
            }
        }
        return null;
    }

    public static String getVideoUrl(ShareVideoContent videoContent, UUID appCallId) {
        if (videoContent == null || videoContent.getVideo() == null) {
            return null;
        }
        Attachment attachment = NativeAppCallAttachmentStore.createAttachment(appCallId, videoContent.getVideo().getLocalUrl());
        ArrayList<Attachment> attachments = new ArrayList(1);
        attachments.add(attachment);
        NativeAppCallAttachmentStore.addAttachments(attachments);
        return attachment.getAttachmentUrl();
    }

    public static JSONObject toJSONObjectForCall(final UUID callId, ShareOpenGraphContent content) throws JSONException {
        ShareOpenGraphAction action = content.getAction();
        final ArrayList<Attachment> attachments = new ArrayList();
        JSONObject actionJSON = OpenGraphJSONUtility.toJSONObject(action, new PhotoJSONProcessor() {
            public JSONObject toJSONObject(SharePhoto photo) {
                Attachment attachment = ShareInternalUtility.getAttachment(callId, photo);
                if (attachment == null) {
                    return null;
                }
                attachments.add(attachment);
                JSONObject photoJSONObject = new JSONObject();
                try {
                    photoJSONObject.put(SettingsJsonConstants.APP_URL_KEY, attachment.getAttachmentUrl());
                    if (!photo.getUserGenerated()) {
                        return photoJSONObject;
                    }
                    photoJSONObject.put(NativeProtocol.IMAGE_USER_GENERATED_KEY, true);
                    return photoJSONObject;
                } catch (Throwable e) {
                    throw new FacebookException("Unable to attach images", e);
                }
            }
        });
        NativeAppCallAttachmentStore.addAttachments(attachments);
        if (content.getPlaceId() != null && Utility.isNullOrEmpty(actionJSON.optString("place"))) {
            actionJSON.put("place", content.getPlaceId());
        }
        if (content.getPeopleIds() != null) {
            JSONArray peopleTags = actionJSON.optJSONArray(FaqsColumns.TAGS);
            Set<String> peopleIdSet = peopleTags == null ? new HashSet() : Utility.jsonArrayToSet(peopleTags);
            for (String peopleId : content.getPeopleIds()) {
                peopleIdSet.add(peopleId);
            }
            actionJSON.put(FaqsColumns.TAGS, new ArrayList(peopleIdSet));
        }
        return actionJSON;
    }

    public static JSONObject toJSONObjectForWeb(ShareOpenGraphContent shareOpenGraphContent) throws JSONException {
        return OpenGraphJSONUtility.toJSONObject(shareOpenGraphContent.getAction(), new PhotoJSONProcessor() {
            public JSONObject toJSONObject(SharePhoto photo) {
                Uri photoUri = photo.getImageUrl();
                JSONObject photoJSONObject = new JSONObject();
                try {
                    photoJSONObject.put(SettingsJsonConstants.APP_URL_KEY, photoUri.toString());
                    return photoJSONObject;
                } catch (Throwable e) {
                    throw new FacebookException("Unable to attach images", e);
                }
            }
        });
    }

    public static JSONArray removeNamespacesFromOGJsonArray(JSONArray jsonArray, boolean requireNamespace) throws JSONException {
        JSONArray newArray = new JSONArray();
        for (int i = 0; i < jsonArray.length(); i++) {
            Object value = jsonArray.get(i);
            if (value instanceof JSONArray) {
                value = removeNamespacesFromOGJsonArray((JSONArray) value, requireNamespace);
            } else if (value instanceof JSONObject) {
                value = removeNamespacesFromOGJsonObject((JSONObject) value, requireNamespace);
            }
            newArray.put(value);
        }
        return newArray;
    }

    public static JSONObject removeNamespacesFromOGJsonObject(JSONObject jsonObject, boolean requireNamespace) {
        if (jsonObject == null) {
            return null;
        }
        try {
            JSONObject newJsonObject = new JSONObject();
            JSONObject data = new JSONObject();
            JSONArray names = jsonObject.names();
            for (int i = 0; i < names.length(); i++) {
                String key = names.getString(i);
                Object value = jsonObject.get(key);
                if (value instanceof JSONObject) {
                    value = removeNamespacesFromOGJsonObject((JSONObject) value, true);
                } else if (value instanceof JSONArray) {
                    value = removeNamespacesFromOGJsonArray((JSONArray) value, true);
                }
                Pair<String, String> fieldNameAndNamespace = getFieldNameAndNamespaceFromFullName(key);
                String namespace = fieldNameAndNamespace.first;
                String fieldName = fieldNameAndNamespace.second;
                if (requireNamespace) {
                    if (namespace != null && namespace.equals("fbsdk")) {
                        newJsonObject.put(key, value);
                    } else if (namespace == null || namespace.equals("og")) {
                        newJsonObject.put(fieldName, value);
                    } else {
                        data.put(fieldName, value);
                    }
                } else if (namespace == null || !namespace.equals("fb")) {
                    newJsonObject.put(fieldName, value);
                } else {
                    newJsonObject.put(key, value);
                }
            }
            if (data.length() <= 0) {
                return newJsonObject;
            }
            newJsonObject.put(MPDbAdapter.KEY_DATA, data);
            return newJsonObject;
        } catch (JSONException e) {
            throw new FacebookException("Failed to create json object from share content");
        }
    }

    public static Pair<String, String> getFieldNameAndNamespaceFromFullName(String fullName) {
        String fieldName;
        String namespace = null;
        int index = fullName.indexOf(58);
        if (index == -1 || fullName.length() <= index + 1) {
            fieldName = fullName;
        } else {
            namespace = fullName.substring(0, index);
            fieldName = fullName.substring(index + 1);
        }
        return new Pair(namespace, fieldName);
    }

    private static Attachment getAttachment(UUID callId, SharePhoto photo) {
        Bitmap bitmap = photo.getBitmap();
        Uri photoUri = photo.getImageUrl();
        if (bitmap != null) {
            return NativeAppCallAttachmentStore.createAttachment(callId, bitmap);
        }
        if (photoUri != null) {
            return NativeAppCallAttachmentStore.createAttachment(callId, photoUri);
        }
        return null;
    }

    static void invokeOnCancelCallback(FacebookCallback<Result> callback) {
        logShareResult(AnalyticsEvents.PARAMETER_SHARE_OUTCOME_CANCELLED, null);
        if (callback != null) {
            callback.onCancel();
        }
    }

    static void invokeOnSuccessCallback(FacebookCallback<Result> callback, String postId) {
        logShareResult(AnalyticsEvents.PARAMETER_SHARE_OUTCOME_SUCCEEDED, null);
        if (callback != null) {
            callback.onSuccess(new Result(postId));
        }
    }

    static void invokeOnErrorCallback(FacebookCallback<Result> callback, GraphResponse response, String message) {
        logShareResult(Extra.ERROR, message);
        if (callback != null) {
            callback.onError(new FacebookGraphResponseException(response, message));
        }
    }

    static void invokeOnErrorCallback(FacebookCallback<Result> callback, String message) {
        logShareResult(Extra.ERROR, message);
        if (callback != null) {
            callback.onError(new FacebookException(message));
        }
    }

    static void invokeOnErrorCallback(FacebookCallback<Result> callback, FacebookException ex) {
        logShareResult(Extra.ERROR, ex.getMessage());
        if (callback != null) {
            callback.onError(ex);
        }
    }

    private static void logShareResult(String shareOutcome, String errorMessage) {
        AppEventsLogger logger = AppEventsLogger.newLogger(FacebookSdk.getApplicationContext());
        Bundle parameters = new Bundle();
        parameters.putString(AnalyticsEvents.PARAMETER_SHARE_OUTCOME, shareOutcome);
        if (errorMessage != null) {
            parameters.putString(AnalyticsEvents.PARAMETER_SHARE_ERROR_MESSAGE, errorMessage);
        }
        logger.logSdkEvent(AnalyticsEvents.EVENT_SHARE_RESULT, null, parameters);
    }

    public static GraphRequest newUploadStagingResourceWithImageRequest(AccessToken accessToken, Bitmap image, GraphRequest.Callback callback) {
        Bundle parameters = new Bundle(1);
        parameters.putParcelable(STAGING_PARAM, image);
        return new GraphRequest(accessToken, MY_STAGING_RESOURCES, parameters, HttpMethod.POST, callback);
    }

    public static GraphRequest newUploadStagingResourceWithImageRequest(AccessToken accessToken, File file, GraphRequest.Callback callback) throws FileNotFoundException {
        ParcelableResourceWithMimeType<ParcelFileDescriptor> resourceWithMimeType = new ParcelableResourceWithMimeType(ParcelFileDescriptor.open(file, 268435456), "image/png");
        Bundle parameters = new Bundle(1);
        parameters.putParcelable(STAGING_PARAM, resourceWithMimeType);
        return new GraphRequest(accessToken, MY_STAGING_RESOURCES, parameters, HttpMethod.POST, callback);
    }

    public static GraphRequest newUploadStagingResourceWithImageRequest(AccessToken accessToken, Uri imageUri, GraphRequest.Callback callback) throws FileNotFoundException {
        if (Utility.isFileUri(imageUri)) {
            return newUploadStagingResourceWithImageRequest(accessToken, new File(imageUri.getPath()), callback);
        }
        if (Utility.isContentUri(imageUri)) {
            ParcelableResourceWithMimeType<Uri> resourceWithMimeType = new ParcelableResourceWithMimeType((Parcelable) imageUri, "image/png");
            Bundle parameters = new Bundle(1);
            parameters.putParcelable(STAGING_PARAM, resourceWithMimeType);
            return new GraphRequest(accessToken, MY_STAGING_RESOURCES, parameters, HttpMethod.POST, callback);
        }
        throw new FacebookException("The image Uri must be either a file:// or content:// Uri");
    }

    @Nullable
    public static ObjectType getMostSpecificObjectType(ObjectType objectType1, ObjectType objectType2) {
        if (objectType1 == objectType2) {
            return objectType1;
        }
        if (objectType1 == ObjectType.UNKNOWN) {
            return objectType2;
        }
        return objectType2 != ObjectType.UNKNOWN ? null : objectType1;
    }
}
