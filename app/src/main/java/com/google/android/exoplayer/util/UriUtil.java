package com.google.android.exoplayer.util;

import android.net.Uri;
import android.text.TextUtils;
import io.fabric.sdk.android.BuildConfig;

public final class UriUtil {
    private static final int FRAGMENT = 3;
    private static final int INDEX_COUNT = 4;
    private static final int PATH = 1;
    private static final int QUERY = 2;
    private static final int SCHEME_COLON = 0;

    private UriUtil() {
    }

    public static Uri resolveToUri(String baseUri, String referenceUri) {
        return Uri.parse(resolve(baseUri, referenceUri));
    }

    public static String resolve(String baseUri, String referenceUri) {
        StringBuilder uri = new StringBuilder();
        if (baseUri == null) {
            baseUri = BuildConfig.FLAVOR;
        }
        if (referenceUri == null) {
            referenceUri = BuildConfig.FLAVOR;
        }
        int[] refIndices = getUriIndices(referenceUri);
        if (refIndices[0] != -1) {
            uri.append(referenceUri);
            removeDotSegments(uri, refIndices[PATH], refIndices[QUERY]);
            return uri.toString();
        }
        int[] baseIndices = getUriIndices(baseUri);
        if (refIndices[FRAGMENT] == 0) {
            return uri.append(baseUri, 0, baseIndices[FRAGMENT]).append(referenceUri).toString();
        }
        if (refIndices[QUERY] == 0) {
            return uri.append(baseUri, 0, baseIndices[QUERY]).append(referenceUri).toString();
        }
        int baseLimit;
        if (refIndices[PATH] != 0) {
            baseLimit = baseIndices[0] + PATH;
            uri.append(baseUri, 0, baseLimit).append(referenceUri);
            return removeDotSegments(uri, refIndices[PATH] + baseLimit, refIndices[QUERY] + baseLimit);
        } else if (refIndices[PATH] != refIndices[QUERY] && referenceUri.charAt(refIndices[PATH]) == '/') {
            uri.append(baseUri, 0, baseIndices[PATH]).append(referenceUri);
            return removeDotSegments(uri, baseIndices[PATH], baseIndices[PATH] + refIndices[QUERY]);
        } else if (baseIndices[0] + QUERY >= baseIndices[PATH] || baseIndices[PATH] != baseIndices[QUERY]) {
            int lastSlashIndex = baseUri.lastIndexOf(47, baseIndices[QUERY] - 1);
            baseLimit = lastSlashIndex == -1 ? baseIndices[PATH] : lastSlashIndex + PATH;
            uri.append(baseUri, 0, baseLimit).append(referenceUri);
            return removeDotSegments(uri, baseIndices[PATH], refIndices[QUERY] + baseLimit);
        } else {
            uri.append(baseUri, 0, baseIndices[PATH]).append('/').append(referenceUri);
            return removeDotSegments(uri, baseIndices[PATH], (baseIndices[PATH] + refIndices[QUERY]) + PATH);
        }
    }

    private static String removeDotSegments(StringBuilder uri, int offset, int limit) {
        if (offset >= limit) {
            return uri.toString();
        }
        if (uri.charAt(offset) == '/') {
            offset += PATH;
        }
        int segmentStart = offset;
        int i = offset;
        while (i <= limit) {
            int nextSegmentStart;
            if (i == limit) {
                nextSegmentStart = i;
            } else if (uri.charAt(i) == '/') {
                nextSegmentStart = i + PATH;
            } else {
                i += PATH;
            }
            if (i == segmentStart + PATH && uri.charAt(segmentStart) == '.') {
                uri.delete(segmentStart, nextSegmentStart);
                limit -= nextSegmentStart - segmentStart;
                i = segmentStart;
            } else if (i == segmentStart + QUERY && uri.charAt(segmentStart) == '.' && uri.charAt(segmentStart + PATH) == '.') {
                int removeFrom;
                int prevSegmentStart = uri.lastIndexOf("/", segmentStart - 2) + PATH;
                if (prevSegmentStart > offset) {
                    removeFrom = prevSegmentStart;
                } else {
                    removeFrom = offset;
                }
                uri.delete(removeFrom, nextSegmentStart);
                limit -= nextSegmentStart - removeFrom;
                segmentStart = prevSegmentStart;
                i = prevSegmentStart;
            } else {
                i += PATH;
                segmentStart = i;
            }
        }
        return uri.toString();
    }

    private static int[] getUriIndices(String uriString) {
        int[] indices = new int[INDEX_COUNT];
        if (TextUtils.isEmpty(uriString)) {
            indices[0] = -1;
        } else {
            boolean hasAuthority;
            int pathIndex;
            int length = uriString.length();
            int fragmentIndex = uriString.indexOf(35);
            if (fragmentIndex == -1) {
                fragmentIndex = length;
            }
            int queryIndex = uriString.indexOf(63);
            if (queryIndex == -1 || queryIndex > fragmentIndex) {
                queryIndex = fragmentIndex;
            }
            int schemeIndexLimit = uriString.indexOf(47);
            if (schemeIndexLimit == -1 || schemeIndexLimit > queryIndex) {
                schemeIndexLimit = queryIndex;
            }
            int schemeIndex = uriString.indexOf(58);
            if (schemeIndex > schemeIndexLimit) {
                schemeIndex = -1;
            }
            if (schemeIndex + QUERY < queryIndex && uriString.charAt(schemeIndex + PATH) == '/' && uriString.charAt(schemeIndex + QUERY) == '/') {
                hasAuthority = true;
            } else {
                hasAuthority = false;
            }
            if (hasAuthority) {
                pathIndex = uriString.indexOf(47, schemeIndex + FRAGMENT);
                if (pathIndex == -1 || pathIndex > queryIndex) {
                    pathIndex = queryIndex;
                }
            } else {
                pathIndex = schemeIndex + PATH;
            }
            indices[0] = schemeIndex;
            indices[PATH] = pathIndex;
            indices[QUERY] = queryIndex;
            indices[FRAGMENT] = fragmentIndex;
        }
        return indices;
    }
}
