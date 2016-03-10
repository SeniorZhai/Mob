package android.support.v7.graphics.drawable;

import android.graphics.PorterDuff.Mode;
import android.os.Build.VERSION;
import com.mobcrush.mobcrush.R;
import com.mobcrush.mobcrush.player.Player;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import io.fabric.sdk.android.services.common.CommonUtils;
import org.apache.http.protocol.HTTP;

public class DrawableUtils {
    public static Mode parseTintMode(int value, Mode defaultMode) {
        switch (value) {
            case TimePickerDialog.ENABLE_PICKER_INDEX /*3*/:
                return Mode.SRC_OVER;
            case Player.STATE_ENDED /*5*/:
                return Mode.SRC_IN;
            case HTTP.HT /*9*/:
                return Mode.SRC_ATOP;
            case R.styleable.Toolbar_titleMarginEnd /*14*/:
                return Mode.MULTIPLY;
            case R.styleable.Toolbar_titleMarginTop /*15*/:
                return Mode.SCREEN;
            case CommonUtils.DEVICE_STATE_VENDORINTERNAL /*16*/:
                if (VERSION.SDK_INT >= 11) {
                    return Mode.valueOf("ADD");
                }
                return defaultMode;
            default:
                return defaultMode;
        }
    }
}
