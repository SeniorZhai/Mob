package com.facebook.share.internal;

import android.os.Bundle;
import com.facebook.FacebookException;
import com.facebook.internal.Utility;
import com.facebook.internal.Validate;
import com.facebook.share.model.ShareContent;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.model.ShareOpenGraphContent;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.model.ShareVideoContent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.json.JSONException;
import org.json.JSONObject;

public class NativeDialogParameters {
    public static Bundle create(UUID callId, ShareContent shareContent, boolean shouldFailOnDataError) {
        Validate.notNull(shareContent, "shareContent");
        Validate.notNull(callId, "callId");
        if (shareContent instanceof ShareLinkContent) {
            return create((ShareLinkContent) shareContent, shouldFailOnDataError);
        }
        if (shareContent instanceof SharePhotoContent) {
            SharePhotoContent photoContent = (SharePhotoContent) shareContent;
            return create(photoContent, ShareInternalUtility.getPhotoUrls(photoContent, callId), shouldFailOnDataError);
        } else if (shareContent instanceof ShareVideoContent) {
            ShareVideoContent videoContent = (ShareVideoContent) shareContent;
            return create(videoContent, ShareInternalUtility.getVideoUrl(videoContent, callId), shouldFailOnDataError);
        } else if (!(shareContent instanceof ShareOpenGraphContent)) {
            return null;
        } else {
            ShareOpenGraphContent openGraphContent = (ShareOpenGraphContent) shareContent;
            try {
                return create(openGraphContent, ShareInternalUtility.removeNamespacesFromOGJsonObject(ShareInternalUtility.toJSONObjectForCall(callId, openGraphContent), false), shouldFailOnDataError);
            } catch (JSONException e) {
                throw new FacebookException("Unable to create a JSON Object from the provided ShareOpenGraphContent: " + e.getMessage());
            }
        }
    }

    private static Bundle create(ShareLinkContent linkContent, boolean dataErrorsFatal) {
        Bundle params = createBaseParameters(linkContent, dataErrorsFatal);
        Utility.putNonEmptyString(params, ShareConstants.TITLE, linkContent.getContentTitle());
        Utility.putNonEmptyString(params, ShareConstants.DESCRIPTION, linkContent.getContentDescription());
        Utility.putUri(params, ShareConstants.IMAGE_URL, linkContent.getImageUrl());
        return params;
    }

    private static Bundle create(SharePhotoContent photoContent, List<String> imageUrls, boolean dataErrorsFatal) {
        Bundle params = createBaseParameters(photoContent, dataErrorsFatal);
        params.putStringArrayList(ShareConstants.PHOTOS, new ArrayList(imageUrls));
        return params;
    }

    private static Bundle create(ShareVideoContent videoContent, String videoUrl, boolean dataErrorsFatal) {
        Bundle params = createBaseParameters(videoContent, dataErrorsFatal);
        Utility.putNonEmptyString(params, ShareConstants.TITLE, videoContent.getContentTitle());
        Utility.putNonEmptyString(params, ShareConstants.DESCRIPTION, videoContent.getContentDescription());
        Utility.putNonEmptyString(params, ShareConstants.VIDEO_URL, videoUrl);
        return params;
    }

    private static Bundle create(ShareOpenGraphContent openGraphContent, JSONObject openGraphActionJSON, boolean dataErrorsFatal) {
        Bundle params = createBaseParameters(openGraphContent, dataErrorsFatal);
        Utility.putNonEmptyString(params, ShareConstants.PREVIEW_PROPERTY_NAME, ShareInternalUtility.getFieldNameAndNamespaceFromFullName(openGraphContent.getPreviewPropertyName()).second);
        Utility.putNonEmptyString(params, ShareConstants.ACTION_TYPE, openGraphContent.getAction().getActionType());
        Utility.putNonEmptyString(params, ShareConstants.ACTION, openGraphActionJSON.toString());
        return params;
    }

    private static Bundle createBaseParameters(ShareContent content, boolean dataErrorsFatal) {
        Bundle params = new Bundle();
        Utility.putUri(params, ShareConstants.CONTENT_URL, content.getContentUrl());
        Utility.putNonEmptyString(params, ShareConstants.PLACE_ID, content.getPlaceId());
        Utility.putNonEmptyString(params, ShareConstants.REF, content.getRef());
        params.putBoolean(ShareConstants.DATA_FAILURES_FATAL, dataErrorsFatal);
        Collection peopleIds = content.getPeopleIds();
        if (!Utility.isNullOrEmpty(peopleIds)) {
            params.putStringArrayList(ShareConstants.PEOPLE_IDS, new ArrayList(peopleIds));
        }
        return params;
    }
}
