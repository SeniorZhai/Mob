package com.google.android.exoplayer.text.ttml;

import android.text.SpannableStringBuilder;
import com.fasterxml.jackson.core.util.MinimalPrettyPrinter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;
import org.apache.http.message.TokenParser;

final class TtmlNode {
    public static final String TAG_BODY = "body";
    public static final String TAG_BR = "br";
    public static final String TAG_DIV = "div";
    public static final String TAG_HEAD = "head";
    public static final String TAG_LAYOUT = "layout";
    public static final String TAG_METADATA = "metadata";
    public static final String TAG_P = "p";
    public static final String TAG_REGION = "region";
    public static final String TAG_SMPTE_DATA = "smpte:data";
    public static final String TAG_SMPTE_IMAGE = "smpte:image";
    public static final String TAG_SMPTE_INFORMATION = "smpte:information";
    public static final String TAG_SPAN = "span";
    public static final String TAG_STYLE = "style";
    public static final String TAG_STYLING = "styling";
    public static final String TAG_TT = "tt";
    public static final long UNDEFINED_TIME = -1;
    private List<TtmlNode> children;
    public final long endTimeUs;
    public final boolean isTextNode;
    public final long startTimeUs;
    public final String tag;
    public final String text;

    public static TtmlNode buildTextNode(String text) {
        return new TtmlNode(null, applyTextElementSpacePolicy(text), UNDEFINED_TIME, UNDEFINED_TIME);
    }

    public static TtmlNode buildNode(String tag, long startTimeUs, long endTimeUs) {
        return new TtmlNode(tag, null, startTimeUs, endTimeUs);
    }

    private TtmlNode(String tag, String text, long startTimeUs, long endTimeUs) {
        this.tag = tag;
        this.text = text;
        this.isTextNode = text != null;
        this.startTimeUs = startTimeUs;
        this.endTimeUs = endTimeUs;
    }

    public boolean isActive(long timeUs) {
        return (this.startTimeUs == UNDEFINED_TIME && this.endTimeUs == UNDEFINED_TIME) || ((this.startTimeUs <= timeUs && this.endTimeUs == UNDEFINED_TIME) || ((this.startTimeUs == UNDEFINED_TIME && timeUs < this.endTimeUs) || (this.startTimeUs <= timeUs && timeUs < this.endTimeUs)));
    }

    public void addChild(TtmlNode child) {
        if (this.children == null) {
            this.children = new ArrayList();
        }
        this.children.add(child);
    }

    public TtmlNode getChild(int index) {
        if (this.children != null) {
            return (TtmlNode) this.children.get(index);
        }
        throw new IndexOutOfBoundsException();
    }

    public int getChildCount() {
        return this.children == null ? 0 : this.children.size();
    }

    public long[] getEventTimesUs() {
        TreeSet<Long> eventTimeSet = new TreeSet();
        getEventTimes(eventTimeSet, false);
        long[] eventTimes = new long[eventTimeSet.size()];
        Iterator<Long> eventTimeIterator = eventTimeSet.iterator();
        int i = 0;
        while (eventTimeIterator.hasNext()) {
            int i2 = i + 1;
            eventTimes[i] = ((Long) eventTimeIterator.next()).longValue();
            i = i2;
        }
        return eventTimes;
    }

    private void getEventTimes(TreeSet<Long> out, boolean descendsPNode) {
        boolean isPNode = TAG_P.equals(this.tag);
        if (descendsPNode || isPNode) {
            if (this.startTimeUs != UNDEFINED_TIME) {
                out.add(Long.valueOf(this.startTimeUs));
            }
            if (this.endTimeUs != UNDEFINED_TIME) {
                out.add(Long.valueOf(this.endTimeUs));
            }
        }
        if (this.children != null) {
            for (int i = 0; i < this.children.size(); i++) {
                TtmlNode ttmlNode = (TtmlNode) this.children.get(i);
                boolean z = descendsPNode || isPNode;
                ttmlNode.getEventTimes(out, z);
            }
        }
    }

    public CharSequence getText(long timeUs) {
        int i;
        SpannableStringBuilder builder = getText(timeUs, new SpannableStringBuilder(), false);
        int builderLength = builder.length();
        for (i = 0; i < builderLength; i++) {
            if (builder.charAt(i) == TokenParser.SP) {
                int j = i + 1;
                while (j < builder.length() && builder.charAt(j) == TokenParser.SP) {
                    j++;
                }
                int spacesToDelete = j - (i + 1);
                if (spacesToDelete > 0) {
                    builder.delete(i, i + spacesToDelete);
                    builderLength -= spacesToDelete;
                }
            }
        }
        if (builderLength > 0 && builder.charAt(0) == TokenParser.SP) {
            builder.delete(0, 1);
            builderLength--;
        }
        i = 0;
        while (i < builderLength - 1) {
            if (builder.charAt(i) == '\n' && builder.charAt(i + 1) == TokenParser.SP) {
                builder.delete(i + 1, i + 2);
                builderLength--;
            }
            i++;
        }
        if (builderLength > 0 && builder.charAt(builderLength - 1) == TokenParser.SP) {
            builder.delete(builderLength - 1, builderLength);
            builderLength--;
        }
        i = 0;
        while (i < builderLength - 1) {
            if (builder.charAt(i) == TokenParser.SP && builder.charAt(i + 1) == '\n') {
                builder.delete(i, i + 1);
                builderLength--;
            }
            i++;
        }
        if (builderLength > 0 && builder.charAt(builderLength - 1) == '\n') {
            builder.delete(builderLength - 1, builderLength);
            builderLength--;
        }
        return builder.subSequence(0, builderLength);
    }

    private SpannableStringBuilder getText(long timeUs, SpannableStringBuilder builder, boolean descendsPNode) {
        if (this.isTextNode && descendsPNode) {
            builder.append(this.text);
        } else if (TAG_BR.equals(this.tag) && descendsPNode) {
            builder.append('\n');
        } else if (!TAG_METADATA.equals(this.tag) && isActive(timeUs)) {
            boolean isPNode = TAG_P.equals(this.tag);
            for (int i = 0; i < getChildCount(); i++) {
                TtmlNode child = getChild(i);
                boolean z = descendsPNode || isPNode;
                child.getText(timeUs, builder, z);
            }
            if (isPNode) {
                endParagraph(builder);
            }
        }
        return builder;
    }

    private static void endParagraph(SpannableStringBuilder builder) {
        int position = builder.length() - 1;
        while (position >= 0 && builder.charAt(position) == TokenParser.SP) {
            position--;
        }
        if (position >= 0 && builder.charAt(position) != '\n') {
            builder.append('\n');
        }
    }

    private static String applyTextElementSpacePolicy(String in) {
        return in.replaceAll("\r\n", "\n").replaceAll(" *\n *", "\n").replaceAll("\n", MinimalPrettyPrinter.DEFAULT_ROOT_VALUE_SEPARATOR).replaceAll("[ \t\\x0B\f\r]+", MinimalPrettyPrinter.DEFAULT_ROOT_VALUE_SEPARATOR);
    }
}
