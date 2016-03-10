package android.support.v4.text;

import com.mobcrush.mobcrush.R;
import io.fabric.sdk.android.services.common.CommonUtils;
import java.nio.CharBuffer;
import java.util.Locale;
import org.apache.http.util.LangUtils;

public class TextDirectionHeuristicsCompat {
    public static final TextDirectionHeuristicCompat ANYRTL_LTR = new TextDirectionHeuristicInternal(AnyStrong.INSTANCE_RTL, false);
    public static final TextDirectionHeuristicCompat FIRSTSTRONG_LTR = new TextDirectionHeuristicInternal(FirstStrong.INSTANCE, false);
    public static final TextDirectionHeuristicCompat FIRSTSTRONG_RTL = new TextDirectionHeuristicInternal(FirstStrong.INSTANCE, true);
    public static final TextDirectionHeuristicCompat LOCALE = TextDirectionHeuristicLocale.INSTANCE;
    public static final TextDirectionHeuristicCompat LTR = new TextDirectionHeuristicInternal(null, false);
    public static final TextDirectionHeuristicCompat RTL = new TextDirectionHeuristicInternal(null, true);
    private static final int STATE_FALSE = 1;
    private static final int STATE_TRUE = 0;
    private static final int STATE_UNKNOWN = 2;

    private interface TextDirectionAlgorithm {
        int checkRtl(CharSequence charSequence, int i, int i2);
    }

    private static class AnyStrong implements TextDirectionAlgorithm {
        public static final AnyStrong INSTANCE_LTR = new AnyStrong(false);
        public static final AnyStrong INSTANCE_RTL = new AnyStrong(true);
        private final boolean mLookForRtl;

        public int checkRtl(CharSequence cs, int start, int count) {
            boolean haveUnlookedFor = false;
            int e = start + count;
            for (int i = start; i < e; i += TextDirectionHeuristicsCompat.STATE_FALSE) {
                switch (TextDirectionHeuristicsCompat.isRtlText(Character.getDirectionality(cs.charAt(i)))) {
                    case TextDirectionHeuristicsCompat.STATE_TRUE /*0*/:
                        if (!this.mLookForRtl) {
                            haveUnlookedFor = true;
                            break;
                        }
                        return TextDirectionHeuristicsCompat.STATE_TRUE;
                    case TextDirectionHeuristicsCompat.STATE_FALSE /*1*/:
                        if (this.mLookForRtl) {
                            haveUnlookedFor = true;
                            break;
                        }
                        return TextDirectionHeuristicsCompat.STATE_FALSE;
                    default:
                        break;
                }
            }
            if (haveUnlookedFor) {
                return !this.mLookForRtl ? TextDirectionHeuristicsCompat.STATE_TRUE : TextDirectionHeuristicsCompat.STATE_FALSE;
            } else {
                return TextDirectionHeuristicsCompat.STATE_UNKNOWN;
            }
        }

        private AnyStrong(boolean lookForRtl) {
            this.mLookForRtl = lookForRtl;
        }
    }

    private static class FirstStrong implements TextDirectionAlgorithm {
        public static final FirstStrong INSTANCE = new FirstStrong();

        public int checkRtl(CharSequence cs, int start, int count) {
            int result = TextDirectionHeuristicsCompat.STATE_UNKNOWN;
            int e = start + count;
            for (int i = start; i < e && result == TextDirectionHeuristicsCompat.STATE_UNKNOWN; i += TextDirectionHeuristicsCompat.STATE_FALSE) {
                result = TextDirectionHeuristicsCompat.isRtlTextOrFormat(Character.getDirectionality(cs.charAt(i)));
            }
            return result;
        }

        private FirstStrong() {
        }
    }

    private static abstract class TextDirectionHeuristicImpl implements TextDirectionHeuristicCompat {
        private final TextDirectionAlgorithm mAlgorithm;

        protected abstract boolean defaultIsRtl();

        public TextDirectionHeuristicImpl(TextDirectionAlgorithm algorithm) {
            this.mAlgorithm = algorithm;
        }

        public boolean isRtl(char[] array, int start, int count) {
            return isRtl(CharBuffer.wrap(array), start, count);
        }

        public boolean isRtl(CharSequence cs, int start, int count) {
            if (cs == null || start < 0 || count < 0 || cs.length() - count < start) {
                throw new IllegalArgumentException();
            } else if (this.mAlgorithm == null) {
                return defaultIsRtl();
            } else {
                return doCheck(cs, start, count);
            }
        }

        private boolean doCheck(CharSequence cs, int start, int count) {
            switch (this.mAlgorithm.checkRtl(cs, start, count)) {
                case TextDirectionHeuristicsCompat.STATE_TRUE /*0*/:
                    return true;
                case TextDirectionHeuristicsCompat.STATE_FALSE /*1*/:
                    return false;
                default:
                    return defaultIsRtl();
            }
        }
    }

    private static class TextDirectionHeuristicInternal extends TextDirectionHeuristicImpl {
        private final boolean mDefaultIsRtl;

        private TextDirectionHeuristicInternal(TextDirectionAlgorithm algorithm, boolean defaultIsRtl) {
            super(algorithm);
            this.mDefaultIsRtl = defaultIsRtl;
        }

        protected boolean defaultIsRtl() {
            return this.mDefaultIsRtl;
        }
    }

    private static class TextDirectionHeuristicLocale extends TextDirectionHeuristicImpl {
        public static final TextDirectionHeuristicLocale INSTANCE = new TextDirectionHeuristicLocale();

        public TextDirectionHeuristicLocale() {
            super(null);
        }

        protected boolean defaultIsRtl() {
            if (TextUtilsCompat.getLayoutDirectionFromLocale(Locale.getDefault()) == TextDirectionHeuristicsCompat.STATE_FALSE) {
                return true;
            }
            return false;
        }
    }

    private static int isRtlText(int directionality) {
        switch (directionality) {
            case STATE_TRUE /*0*/:
                return STATE_FALSE;
            case STATE_FALSE /*1*/:
            case STATE_UNKNOWN /*2*/:
                return STATE_TRUE;
            default:
                return STATE_UNKNOWN;
        }
    }

    private static int isRtlTextOrFormat(int directionality) {
        switch (directionality) {
            case STATE_TRUE /*0*/:
            case R.styleable.Toolbar_titleMarginEnd /*14*/:
            case R.styleable.Toolbar_titleMarginTop /*15*/:
                return STATE_FALSE;
            case STATE_FALSE /*1*/:
            case STATE_UNKNOWN /*2*/:
            case CommonUtils.DEVICE_STATE_VENDORINTERNAL /*16*/:
            case LangUtils.HASH_SEED /*17*/:
                return STATE_TRUE;
            default:
                return STATE_UNKNOWN;
        }
    }
}
