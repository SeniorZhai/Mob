package com.google.android.exoplayer.dash.mpd;

import com.helpshift.HSFunnel;
import io.fabric.sdk.android.BuildConfig;
import java.util.Locale;

public final class UrlTemplate {
    private static final String BANDWIDTH = "Bandwidth";
    private static final int BANDWIDTH_ID = 3;
    private static final String DEFAULT_FORMAT_TAG = "%01d";
    private static final String ESCAPED_DOLLAR = "$$";
    private static final String NUMBER = "Number";
    private static final int NUMBER_ID = 2;
    private static final String REPRESENTATION = "RepresentationID";
    private static final int REPRESENTATION_ID = 1;
    private static final String TIME = "Time";
    private static final int TIME_ID = 4;
    private final int identifierCount;
    private final String[] identifierFormatTags;
    private final int[] identifiers;
    private final String[] urlPieces;

    public static UrlTemplate compile(String template) {
        String[] urlPieces = new String[5];
        int[] identifiers = new int[TIME_ID];
        String[] identifierFormatTags = new String[TIME_ID];
        return new UrlTemplate(urlPieces, identifiers, identifierFormatTags, parseTemplate(template, urlPieces, identifiers, identifierFormatTags));
    }

    private UrlTemplate(String[] urlPieces, int[] identifiers, String[] identifierFormatTags, int identifierCount) {
        this.urlPieces = urlPieces;
        this.identifiers = identifiers;
        this.identifierFormatTags = identifierFormatTags;
        this.identifierCount = identifierCount;
    }

    public String buildUri(String representationId, int segmentNumber, int bandwidth, long time) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < this.identifierCount; i += REPRESENTATION_ID) {
            builder.append(this.urlPieces[i]);
            if (this.identifiers[i] == REPRESENTATION_ID) {
                builder.append(representationId);
            } else if (this.identifiers[i] == NUMBER_ID) {
                r2 = Locale.US;
                r3 = this.identifierFormatTags[i];
                r4 = new Object[REPRESENTATION_ID];
                r4[0] = Integer.valueOf(segmentNumber);
                builder.append(String.format(r2, r3, r4));
            } else if (this.identifiers[i] == BANDWIDTH_ID) {
                r2 = Locale.US;
                r3 = this.identifierFormatTags[i];
                r4 = new Object[REPRESENTATION_ID];
                r4[0] = Integer.valueOf(bandwidth);
                builder.append(String.format(r2, r3, r4));
            } else if (this.identifiers[i] == TIME_ID) {
                r2 = Locale.US;
                r3 = this.identifierFormatTags[i];
                r4 = new Object[REPRESENTATION_ID];
                r4[0] = Long.valueOf(time);
                builder.append(String.format(r2, r3, r4));
            }
        }
        builder.append(this.urlPieces[this.identifierCount]);
        return builder.toString();
    }

    private static int parseTemplate(String template, String[] urlPieces, int[] identifiers, String[] identifierFormatTags) {
        urlPieces[0] = BuildConfig.FLAVOR;
        int templateIndex = 0;
        int identifierCount = 0;
        while (templateIndex < template.length()) {
            int dollarIndex = template.indexOf("$", templateIndex);
            if (dollarIndex == -1) {
                urlPieces[identifierCount] = urlPieces[identifierCount] + template.substring(templateIndex);
                templateIndex = template.length();
            } else if (dollarIndex != templateIndex) {
                urlPieces[identifierCount] = urlPieces[identifierCount] + template.substring(templateIndex, dollarIndex);
                templateIndex = dollarIndex;
            } else if (template.startsWith(ESCAPED_DOLLAR, templateIndex)) {
                urlPieces[identifierCount] = urlPieces[identifierCount] + "$";
                templateIndex += NUMBER_ID;
            } else {
                int secondIndex = template.indexOf("$", templateIndex + REPRESENTATION_ID);
                String identifier = template.substring(templateIndex + REPRESENTATION_ID, secondIndex);
                if (identifier.equals(REPRESENTATION)) {
                    identifiers[identifierCount] = REPRESENTATION_ID;
                } else {
                    int formatTagIndex = identifier.indexOf("%0");
                    String formatTag = DEFAULT_FORMAT_TAG;
                    if (formatTagIndex != -1) {
                        formatTag = identifier.substring(formatTagIndex);
                        if (!formatTag.endsWith(HSFunnel.LIBRARY_OPENED_DECOMP)) {
                            formatTag = formatTag + HSFunnel.LIBRARY_OPENED_DECOMP;
                        }
                        identifier = identifier.substring(0, formatTagIndex);
                    }
                    if (identifier.equals(NUMBER)) {
                        identifiers[identifierCount] = NUMBER_ID;
                    } else if (identifier.equals(BANDWIDTH)) {
                        identifiers[identifierCount] = BANDWIDTH_ID;
                    } else if (identifier.equals(TIME)) {
                        identifiers[identifierCount] = TIME_ID;
                    } else {
                        throw new IllegalArgumentException("Invalid template: " + template);
                    }
                    identifierFormatTags[identifierCount] = formatTag;
                }
                identifierCount += REPRESENTATION_ID;
                urlPieces[identifierCount] = BuildConfig.FLAVOR;
                templateIndex = secondIndex + REPRESENTATION_ID;
            }
        }
        return identifierCount;
    }
}
