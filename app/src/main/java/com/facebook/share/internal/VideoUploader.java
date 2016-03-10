package com.facebook.share.internal;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelFileDescriptor;
import android.os.ParcelFileDescriptor.AutoCloseInputStream;
import android.util.Log;
import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookGraphResponseException;
import com.facebook.FacebookRequestError;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.internal.Utility;
import com.facebook.internal.Validate;
import com.facebook.internal.WorkQueue;
import com.facebook.internal.WorkQueue.WorkItem;
import com.facebook.share.Sharer.Result;
import com.facebook.share.model.ShareVideo;
import com.facebook.share.model.ShareVideoContent;
import com.helpshift.res.values.HSConsts;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;

public class VideoUploader {
    private static final String ERROR_BAD_SERVER_RESPONSE = "Unexpected error in server response";
    private static final String ERROR_UPLOAD = "Video upload failed";
    private static final int MAX_RETRIES_PER_PHASE = 2;
    private static final String PARAM_DESCRIPTION = "description";
    private static final String PARAM_END_OFFSET = "end_offset";
    private static final String PARAM_FILE_SIZE = "file_size";
    private static final String PARAM_REF = "ref";
    private static final String PARAM_SESSION_ID = "upload_session_id";
    private static final String PARAM_START_OFFSET = "start_offset";
    private static final String PARAM_TITLE = "title";
    private static final String PARAM_UPLOAD_PHASE = "upload_phase";
    private static final String PARAM_VALUE_UPLOAD_FINISH_PHASE = "finish";
    private static final String PARAM_VALUE_UPLOAD_START_PHASE = "start";
    private static final String PARAM_VALUE_UPLOAD_TRANSFER_PHASE = "transfer";
    private static final String PARAM_VIDEO_FILE_CHUNK = "video_file_chunk";
    private static final String PARAM_VIDEO_ID = "video_id";
    private static final int RETRY_DELAY_BACK_OFF_FACTOR = 3;
    private static final int RETRY_DELAY_UNIT_MS = 5000;
    private static final String TAG = "VideoUploader";
    private static final int UPLOAD_QUEUE_MAX_CONCURRENT = 8;
    private static AccessTokenTracker accessTokenTracker;
    private static Handler handler;
    private static boolean initialized;
    private static Set<UploadContext> pendingUploads = new HashSet();
    private static WorkQueue uploadQueue = new WorkQueue(UPLOAD_QUEUE_MAX_CONCURRENT);

    private static abstract class UploadWorkItemBase implements Runnable {
        protected int completedRetries;
        protected UploadContext uploadContext;

        protected abstract void enqueueRetry(int i);

        protected abstract Bundle getParameters() throws Exception;

        protected abstract Set<Integer> getTransientErrorCodes();

        protected abstract void handleError(FacebookException facebookException);

        protected abstract void handleSuccess(JSONObject jSONObject) throws JSONException;

        protected UploadWorkItemBase(UploadContext uploadContext, int completedRetries) {
            this.uploadContext = uploadContext;
            this.completedRetries = completedRetries;
        }

        public void run() {
            if (this.uploadContext.isCanceled) {
                endUploadWithFailure(null);
                return;
            }
            try {
                executeGraphRequestSynchronously(getParameters());
            } catch (FacebookException fe) {
                endUploadWithFailure(fe);
            } catch (Throwable e) {
                endUploadWithFailure(new FacebookException(VideoUploader.ERROR_UPLOAD, e));
            }
        }

        protected void executeGraphRequestSynchronously(Bundle parameters) {
            Bundle bundle = parameters;
            GraphResponse response = new GraphRequest(this.uploadContext.accessToken, String.format(Locale.ROOT, "%s/videos", new Object[]{this.uploadContext.graphNode}), bundle, HttpMethod.POST, null).executeAndWait();
            if (response != null) {
                FacebookRequestError error = response.getError();
                JSONObject responseJSON = response.getJSONObject();
                if (error != null) {
                    if (!attemptRetry(error.getSubErrorCode())) {
                        handleError(new FacebookGraphResponseException(response, VideoUploader.ERROR_UPLOAD));
                        return;
                    }
                    return;
                } else if (responseJSON != null) {
                    try {
                        handleSuccess(responseJSON);
                        return;
                    } catch (Throwable e) {
                        endUploadWithFailure(new FacebookException(VideoUploader.ERROR_BAD_SERVER_RESPONSE, e));
                        return;
                    }
                } else {
                    handleError(new FacebookException(VideoUploader.ERROR_BAD_SERVER_RESPONSE));
                    return;
                }
            }
            handleError(new FacebookException(VideoUploader.ERROR_BAD_SERVER_RESPONSE));
        }

        private boolean attemptRetry(int errorCode) {
            if (this.completedRetries >= VideoUploader.MAX_RETRIES_PER_PHASE || !getTransientErrorCodes().contains(Integer.valueOf(errorCode))) {
                return false;
            }
            VideoUploader.getHandler().postDelayed(new Runnable() {
                public void run() {
                    UploadWorkItemBase.this.enqueueRetry(UploadWorkItemBase.this.completedRetries + 1);
                }
            }, (long) (((int) Math.pow(3.0d, (double) this.completedRetries)) * VideoUploader.RETRY_DELAY_UNIT_MS));
            return true;
        }

        protected void endUploadWithFailure(FacebookException error) {
            issueResponseOnMainThread(error, null);
        }

        protected void issueResponseOnMainThread(final FacebookException error, final String videoId) {
            VideoUploader.getHandler().post(new Runnable() {
                public void run() {
                    VideoUploader.issueResponse(UploadWorkItemBase.this.uploadContext, error, videoId);
                }
            });
        }
    }

    private static class FinishUploadWorkItem extends UploadWorkItemBase {
        static final Set<Integer> transientErrorCodes = new HashSet<Integer>() {
            {
                add(Integer.valueOf(1363011));
            }
        };

        public FinishUploadWorkItem(UploadContext uploadContext, int completedRetries) {
            super(uploadContext, completedRetries);
        }

        public Bundle getParameters() {
            Bundle parameters = new Bundle();
            if (this.uploadContext.params != null) {
                parameters.putAll(this.uploadContext.params);
            }
            parameters.putString(VideoUploader.PARAM_UPLOAD_PHASE, VideoUploader.PARAM_VALUE_UPLOAD_FINISH_PHASE);
            parameters.putString(VideoUploader.PARAM_SESSION_ID, this.uploadContext.sessionId);
            Utility.putNonEmptyString(parameters, VideoUploader.PARAM_TITLE, this.uploadContext.title);
            Utility.putNonEmptyString(parameters, VideoUploader.PARAM_DESCRIPTION, this.uploadContext.description);
            Utility.putNonEmptyString(parameters, VideoUploader.PARAM_REF, this.uploadContext.ref);
            return parameters;
        }

        protected void handleSuccess(JSONObject jsonObject) throws JSONException {
            if (jsonObject.getBoolean(GraphResponse.SUCCESS_KEY)) {
                issueResponseOnMainThread(null, this.uploadContext.videoId);
            } else {
                handleError(new FacebookException(VideoUploader.ERROR_BAD_SERVER_RESPONSE));
            }
        }

        protected void handleError(FacebookException error) {
            VideoUploader.logError(error, "Video '%s' failed to finish uploading", this.uploadContext.videoId);
            endUploadWithFailure(error);
        }

        protected Set<Integer> getTransientErrorCodes() {
            return transientErrorCodes;
        }

        protected void enqueueRetry(int retriesCompleted) {
            VideoUploader.enqueueUploadFinish(this.uploadContext, retriesCompleted);
        }
    }

    private static class StartUploadWorkItem extends UploadWorkItemBase {
        static final Set<Integer> transientErrorCodes = new HashSet<Integer>() {
            {
                add(Integer.valueOf(6000));
            }
        };

        public StartUploadWorkItem(UploadContext uploadContext, int completedRetries) {
            super(uploadContext, completedRetries);
        }

        public Bundle getParameters() {
            Bundle parameters = new Bundle();
            parameters.putString(VideoUploader.PARAM_UPLOAD_PHASE, VideoUploader.PARAM_VALUE_UPLOAD_START_PHASE);
            parameters.putLong(VideoUploader.PARAM_FILE_SIZE, this.uploadContext.videoSize);
            return parameters;
        }

        protected void handleSuccess(JSONObject jsonObject) throws JSONException {
            this.uploadContext.sessionId = jsonObject.getString(VideoUploader.PARAM_SESSION_ID);
            this.uploadContext.videoId = jsonObject.getString(VideoUploader.PARAM_VIDEO_ID);
            VideoUploader.enqueueUploadChunk(this.uploadContext, jsonObject.getString(VideoUploader.PARAM_START_OFFSET), jsonObject.getString(VideoUploader.PARAM_END_OFFSET), 0);
        }

        protected void handleError(FacebookException error) {
            VideoUploader.logError(error, "Error starting video upload", new Object[0]);
            endUploadWithFailure(error);
        }

        protected Set<Integer> getTransientErrorCodes() {
            return transientErrorCodes;
        }

        protected void enqueueRetry(int retriesCompleted) {
            VideoUploader.enqueueUploadStart(this.uploadContext, retriesCompleted);
        }
    }

    private static class TransferChunkWorkItem extends UploadWorkItemBase {
        static final Set<Integer> transientErrorCodes = new HashSet<Integer>() {
            {
                add(Integer.valueOf(1363019));
                add(Integer.valueOf(1363021));
                add(Integer.valueOf(1363030));
                add(Integer.valueOf(1363033));
                add(Integer.valueOf(1363041));
            }
        };
        private String chunkEnd;
        private String chunkStart;

        public TransferChunkWorkItem(UploadContext uploadContext, String chunkStart, String chunkEnd, int completedRetries) {
            super(uploadContext, completedRetries);
            this.chunkStart = chunkStart;
            this.chunkEnd = chunkEnd;
        }

        public Bundle getParameters() throws IOException {
            Bundle parameters = new Bundle();
            parameters.putString(VideoUploader.PARAM_UPLOAD_PHASE, VideoUploader.PARAM_VALUE_UPLOAD_TRANSFER_PHASE);
            parameters.putString(VideoUploader.PARAM_SESSION_ID, this.uploadContext.sessionId);
            parameters.putString(VideoUploader.PARAM_START_OFFSET, this.chunkStart);
            byte[] chunk = VideoUploader.getChunk(this.uploadContext, this.chunkStart, this.chunkEnd);
            if (chunk != null) {
                parameters.putByteArray(VideoUploader.PARAM_VIDEO_FILE_CHUNK, chunk);
                return parameters;
            }
            throw new FacebookException("Error reading video");
        }

        protected void handleSuccess(JSONObject jsonObject) throws JSONException {
            String startOffset = jsonObject.getString(VideoUploader.PARAM_START_OFFSET);
            String endOffset = jsonObject.getString(VideoUploader.PARAM_END_OFFSET);
            if (Utility.areObjectsEqual(startOffset, endOffset)) {
                VideoUploader.enqueueUploadFinish(this.uploadContext, 0);
            } else {
                VideoUploader.enqueueUploadChunk(this.uploadContext, startOffset, endOffset, 0);
            }
        }

        protected void handleError(FacebookException error) {
            VideoUploader.logError(error, "Error uploading video '%s'", this.uploadContext.videoId);
            endUploadWithFailure(error);
        }

        protected Set<Integer> getTransientErrorCodes() {
            return transientErrorCodes;
        }

        protected void enqueueRetry(int retriesCompleted) {
            VideoUploader.enqueueUploadChunk(this.uploadContext, this.chunkStart, this.chunkEnd, retriesCompleted);
        }
    }

    private static class UploadContext {
        public final AccessToken accessToken;
        public final FacebookCallback<Result> callback;
        public String chunkStart;
        public final String description;
        public final String graphNode;
        public boolean isCanceled;
        public Bundle params;
        public final String ref;
        public String sessionId;
        public final String title;
        public String videoId;
        public long videoSize;
        public InputStream videoStream;
        public final Uri videoUri;
        public WorkItem workItem;

        private UploadContext(ShareVideoContent videoContent, String graphNode, FacebookCallback<Result> callback) {
            this.chunkStart = HSConsts.STATUS_NEW;
            this.accessToken = AccessToken.getCurrentAccessToken();
            this.videoUri = videoContent.getVideo().getLocalUrl();
            this.title = videoContent.getContentTitle();
            this.description = videoContent.getContentDescription();
            this.ref = videoContent.getRef();
            this.graphNode = graphNode;
            this.callback = callback;
            this.params = videoContent.getVideo().getParameters();
        }

        private void initialize() throws FileNotFoundException {
            try {
                if (Utility.isFileUri(this.videoUri)) {
                    ParcelFileDescriptor fileDescriptor = ParcelFileDescriptor.open(new File(this.videoUri.getPath()), 268435456);
                    this.videoSize = fileDescriptor.getStatSize();
                    this.videoStream = new AutoCloseInputStream(fileDescriptor);
                } else if (Utility.isContentUri(this.videoUri)) {
                    this.videoSize = Utility.getContentSize(this.videoUri);
                    this.videoStream = FacebookSdk.getApplicationContext().getContentResolver().openInputStream(this.videoUri);
                } else {
                    throw new FacebookException("Uri must be a content:// or file:// uri");
                }
            } catch (FileNotFoundException e) {
                Utility.closeQuietly(this.videoStream);
                throw e;
            }
        }
    }

    public static synchronized void uploadAsync(ShareVideoContent videoContent, FacebookCallback<Result> callback) throws FileNotFoundException {
        synchronized (VideoUploader.class) {
            uploadAsync(videoContent, "me", callback);
        }
    }

    public static synchronized void uploadAsync(ShareVideoContent videoContent, String graphNode, FacebookCallback<Result> callback) throws FileNotFoundException {
        synchronized (VideoUploader.class) {
            if (!initialized) {
                registerAccessTokenTracker();
                initialized = true;
            }
            Validate.notNull(videoContent, "videoContent");
            Validate.notNull(graphNode, "graphNode");
            ShareVideo video = videoContent.getVideo();
            Validate.notNull(video, "videoContent.video");
            Validate.notNull(video.getLocalUrl(), "videoContent.video.localUrl");
            UploadContext uploadContext = new UploadContext(videoContent, graphNode, callback);
            uploadContext.initialize();
            pendingUploads.add(uploadContext);
            enqueueUploadStart(uploadContext, 0);
        }
    }

    private static synchronized void cancelAllRequests() {
        synchronized (VideoUploader.class) {
            for (UploadContext uploadContext : pendingUploads) {
                uploadContext.isCanceled = true;
            }
        }
    }

    private static synchronized void removePendingUpload(UploadContext uploadContext) {
        synchronized (VideoUploader.class) {
            pendingUploads.remove(uploadContext);
        }
    }

    private static synchronized Handler getHandler() {
        Handler handler;
        synchronized (VideoUploader.class) {
            if (handler == null) {
                handler = new Handler(Looper.getMainLooper());
            }
            handler = handler;
        }
        return handler;
    }

    private static void issueResponse(UploadContext uploadContext, FacebookException error, String videoId) {
        removePendingUpload(uploadContext);
        Utility.closeQuietly(uploadContext.videoStream);
        if (uploadContext.callback == null) {
            return;
        }
        if (error != null) {
            ShareInternalUtility.invokeOnErrorCallback(uploadContext.callback, error);
        } else if (uploadContext.isCanceled) {
            ShareInternalUtility.invokeOnCancelCallback(uploadContext.callback);
        } else {
            ShareInternalUtility.invokeOnSuccessCallback(uploadContext.callback, videoId);
        }
    }

    private static void enqueueUploadStart(UploadContext uploadContext, int completedRetries) {
        enqueueRequest(uploadContext, new StartUploadWorkItem(uploadContext, completedRetries));
    }

    private static void enqueueUploadChunk(UploadContext uploadContext, String chunkStart, String chunkEnd, int completedRetries) {
        enqueueRequest(uploadContext, new TransferChunkWorkItem(uploadContext, chunkStart, chunkEnd, completedRetries));
    }

    private static void enqueueUploadFinish(UploadContext uploadContext, int completedRetries) {
        enqueueRequest(uploadContext, new FinishUploadWorkItem(uploadContext, completedRetries));
    }

    private static synchronized void enqueueRequest(UploadContext uploadContext, Runnable workItem) {
        synchronized (VideoUploader.class) {
            uploadContext.workItem = uploadQueue.addActiveWorkItem(workItem);
        }
    }

    private static byte[] getChunk(UploadContext uploadContext, String chunkStart, String chunkEnd) throws IOException {
        if (Utility.areObjectsEqual(chunkStart, uploadContext.chunkStart)) {
            int len;
            int chunkSize = (int) (Long.parseLong(chunkEnd) - Long.parseLong(chunkStart));
            ByteArrayOutputStream byteBufferStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[Math.min(Utility.DEFAULT_STREAM_BUFFER_SIZE, chunkSize)];
            do {
                len = uploadContext.videoStream.read(buffer);
                if (len != -1) {
                    byteBufferStream.write(buffer, 0, len);
                    chunkSize -= len;
                    if (chunkSize == 0) {
                    }
                }
                uploadContext.chunkStart = chunkEnd;
                return byteBufferStream.toByteArray();
            } while (chunkSize >= 0);
            Object[] objArr = new Object[MAX_RETRIES_PER_PHASE];
            objArr[0] = Integer.valueOf(chunkSize + len);
            objArr[1] = Integer.valueOf(len);
            logError(null, "Error reading video chunk. Expected buffer length - '%d'. Actual - '%d'.", objArr);
            return null;
        }
        objArr = new Object[MAX_RETRIES_PER_PHASE];
        objArr[0] = uploadContext.chunkStart;
        objArr[1] = chunkStart;
        logError(null, "Error reading video chunk. Expected chunk '%s'. Requested chunk '%s'.", objArr);
        return null;
    }

    private static void registerAccessTokenTracker() {
        accessTokenTracker = new AccessTokenTracker() {
            protected void onCurrentAccessTokenChanged(AccessToken oldAccessToken, AccessToken currentAccessToken) {
                if (oldAccessToken != null) {
                    if (currentAccessToken == null || !Utility.areObjectsEqual(currentAccessToken.getUserId(), oldAccessToken.getUserId())) {
                        VideoUploader.cancelAllRequests();
                    }
                }
            }
        };
    }

    private static void logError(Exception e, String format, Object... args) {
        Log.e(TAG, String.format(Locale.ROOT, format, args), e);
    }
}
