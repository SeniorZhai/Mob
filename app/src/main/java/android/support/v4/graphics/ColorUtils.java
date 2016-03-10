package android.support.v4.graphics;

import android.graphics.Color;
import android.support.v4.view.ViewCompat;
import com.android.volley.DefaultRetryPolicy;
import com.mobcrush.mobcrush.R;
import com.mobcrush.mobcrush.player.Player;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import io.fabric.sdk.android.services.common.CommonUtils;
import io.fabric.sdk.android.services.common.ResponseParser;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;

public class ColorUtils {
    private static final int MIN_ALPHA_SEARCH_MAX_ITERATIONS = 10;
    private static final int MIN_ALPHA_SEARCH_PRECISION = 10;

    private ColorUtils() {
    }

    public static int compositeColors(int foreground, int background) {
        int bgAlpha = Color.alpha(background);
        int fgAlpha = Color.alpha(foreground);
        int a = compositeAlpha(fgAlpha, bgAlpha);
        return Color.argb(a, compositeComponent(Color.red(foreground), fgAlpha, Color.red(background), bgAlpha, a), compositeComponent(Color.green(foreground), fgAlpha, Color.green(background), bgAlpha, a), compositeComponent(Color.blue(foreground), fgAlpha, Color.blue(background), bgAlpha, a));
    }

    private static int compositeAlpha(int foregroundAlpha, int backgroundAlpha) {
        return 255 - (((255 - backgroundAlpha) * (255 - foregroundAlpha)) / SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT);
    }

    private static int compositeComponent(int fgC, int fgA, int bgC, int bgA, int a) {
        if (a == 0) {
            return 0;
        }
        return (((fgC * SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT) * fgA) + ((bgC * bgA) * (255 - fgA))) / (a * SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT);
    }

    public static double calculateLuminance(int color) {
        double red = ((double) Color.red(color)) / 255.0d;
        double green = ((double) Color.green(color)) / 255.0d;
        double blue = ((double) Color.blue(color)) / 255.0d;
        return ((0.2126d * (red < 0.03928d ? red / 12.92d : Math.pow((0.055d + red) / 1.055d, 2.4d))) + (0.7152d * (green < 0.03928d ? green / 12.92d : Math.pow((0.055d + green) / 1.055d, 2.4d)))) + (0.0722d * (blue < 0.03928d ? blue / 12.92d : Math.pow((0.055d + blue) / 1.055d, 2.4d)));
    }

    public static double calculateContrast(int foreground, int background) {
        if (Color.alpha(background) != SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT) {
            throw new IllegalArgumentException("background can not be translucent");
        }
        if (Color.alpha(foreground) < SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT) {
            foreground = compositeColors(foreground, background);
        }
        double luminance1 = calculateLuminance(foreground) + 0.05d;
        double luminance2 = calculateLuminance(background) + 0.05d;
        return Math.max(luminance1, luminance2) / Math.min(luminance1, luminance2);
    }

    public static int calculateMinimumAlpha(int foreground, int background, float minContrastRatio) {
        if (Color.alpha(background) != SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT) {
            throw new IllegalArgumentException("background can not be translucent");
        } else if (calculateContrast(setAlphaComponent(foreground, SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT), background) < ((double) minContrastRatio)) {
            return -1;
        } else {
            int minAlpha = 0;
            int maxAlpha = SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT;
            for (int numIterations = 0; numIterations <= MIN_ALPHA_SEARCH_PRECISION && maxAlpha - minAlpha > MIN_ALPHA_SEARCH_PRECISION; numIterations++) {
                int testAlpha = (minAlpha + maxAlpha) / 2;
                if (calculateContrast(setAlphaComponent(foreground, testAlpha), background) < ((double) minContrastRatio)) {
                    minAlpha = testAlpha;
                } else {
                    maxAlpha = testAlpha;
                }
            }
            return maxAlpha;
        }
    }

    public static void RGBToHSL(int r, int g, int b, float[] hsl) {
        float s;
        float h;
        float rf = ((float) r) / 255.0f;
        float gf = ((float) g) / 255.0f;
        float bf = ((float) b) / 255.0f;
        float max = Math.max(rf, Math.max(gf, bf));
        float min = Math.min(rf, Math.min(gf, bf));
        float deltaMaxMin = max - min;
        float l = (max + min) / 2.0f;
        if (max == min) {
            s = 0.0f;
            h = 0.0f;
        } else {
            if (max == rf) {
                h = ((gf - bf) / deltaMaxMin) % 6.0f;
            } else if (max == gf) {
                h = ((bf - rf) / deltaMaxMin) + 2.0f;
            } else {
                h = ((rf - gf) / deltaMaxMin) + 4.0f;
            }
            s = deltaMaxMin / (DefaultRetryPolicy.DEFAULT_BACKOFF_MULT - Math.abs((2.0f * l) - DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        }
        h = (60.0f * h) % 360.0f;
        if (h < 0.0f) {
            h += 360.0f;
        }
        hsl[0] = constrain(h, 0.0f, 360.0f);
        hsl[1] = constrain(s, 0.0f, (float) DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
        hsl[2] = constrain(l, 0.0f, (float) DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
    }

    public static void colorToHSL(int color, float[] hsl) {
        RGBToHSL(Color.red(color), Color.green(color), Color.blue(color), hsl);
    }

    public static int HSLToColor(float[] hsl) {
        float h = hsl[0];
        float s = hsl[1];
        float l = hsl[2];
        float c = (DefaultRetryPolicy.DEFAULT_BACKOFF_MULT - Math.abs((2.0f * l) - DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)) * s;
        float m = l - (0.5f * c);
        float x = c * (DefaultRetryPolicy.DEFAULT_BACKOFF_MULT - Math.abs(((h / 60.0f) % 2.0f) - DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        int r = 0;
        int g = 0;
        int b = 0;
        switch (((int) h) / 60) {
            case ResponseParser.ResponseActionDiscard /*0*/:
                r = Math.round(255.0f * (c + m));
                g = Math.round(255.0f * (x + m));
                b = Math.round(255.0f * m);
                break;
            case SettingsJsonConstants.ANALYTICS_SAMPLING_RATE_DEFAULT /*1*/:
                r = Math.round(255.0f * (x + m));
                g = Math.round(255.0f * (c + m));
                b = Math.round(255.0f * m);
                break;
            case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
                r = Math.round(255.0f * m);
                g = Math.round(255.0f * (c + m));
                b = Math.round(255.0f * (x + m));
                break;
            case TimePickerDialog.ENABLE_PICKER_INDEX /*3*/:
                r = Math.round(255.0f * m);
                g = Math.round(255.0f * (x + m));
                b = Math.round(255.0f * (c + m));
                break;
            case CommonUtils.DEVICE_STATE_DEBUGGERATTACHED /*4*/:
                r = Math.round(255.0f * (x + m));
                g = Math.round(255.0f * m);
                b = Math.round(255.0f * (c + m));
                break;
            case Player.STATE_ENDED /*5*/:
            case R.styleable.Toolbar_contentInsetEnd /*6*/:
                r = Math.round(255.0f * (c + m));
                g = Math.round(255.0f * m);
                b = Math.round(255.0f * (x + m));
                break;
        }
        return Color.rgb(constrain(r, 0, (int) SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT), constrain(g, 0, (int) SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT), constrain(b, 0, (int) SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT));
    }

    public static int setAlphaComponent(int color, int alpha) {
        if (alpha >= 0 && alpha <= SettingsJsonConstants.SETTINGS_IDENTIFIER_MASK_DEFAULT) {
            return (ViewCompat.MEASURED_SIZE_MASK & color) | (alpha << 24);
        }
        throw new IllegalArgumentException("alpha must be between 0 and 255.");
    }

    private static float constrain(float amount, float low, float high) {
        if (amount < low) {
            return low;
        }
        return amount > high ? high : amount;
    }

    private static int constrain(int amount, int low, int high) {
        if (amount < low) {
            return low;
        }
        return amount > high ? high : amount;
    }
}
