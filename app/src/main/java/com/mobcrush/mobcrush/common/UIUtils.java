package com.mobcrush.mobcrush.common;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.LightingColorFilter;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.app.FragmentActivity;
import android.support.v4.util.Pair;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.ListPopupWindow;
import android.support.v7.widget.PopupMenu;
import android.text.SpannableString;
import android.text.method.PasswordTransformationMethod;
import android.text.style.ForegroundColorSpan;
import android.view.Display;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SimpleAdapter;
import com.android.volley.DefaultRetryPolicy;
import com.crashlytics.android.Crashlytics;
import com.mobcrush.mobcrush.Constants;
import com.mobcrush.mobcrush.MainApplication;
import com.mobcrush.mobcrush.R;
import com.mobcrush.mobcrush.player.Player;
import com.wdullaer.materialdatetimepicker.date.DayPickerView;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import io.fabric.sdk.android.services.common.AbstractSpiCall;
import io.fabric.sdk.android.services.common.CommonUtils;
import io.fabric.sdk.android.services.events.EventsFilesManager;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class UIUtils {
    private static final HashMap<String, Typeface> mFonts = new HashMap();

    public static Typeface getTypeface(Context context, String name) {
        try {
            Typeface tf = (Typeface) mFonts.get(name);
            if (tf != null) {
                return tf;
            }
            tf = Typeface.createFromAsset(context.getAssets(), name);
            mFonts.put(name, tf);
            return tf;
        } catch (Exception e) {
            return Typeface.DEFAULT;
        }
    }

    public static void fixPasswordHintIssue(EditText editText) {
        if (editText != null) {
            editText.setTypeface(Typeface.DEFAULT);
            editText.setTransformationMethod(new PasswordTransformationMethod());
        }
    }

    public static int getStatusBarHeight(Context context) {
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", AbstractSpiCall.ANDROID_CLIENT_TYPE);
        if (resourceId > 0) {
            return context.getResources().getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    public static void fadeOut(View view) {
        if (view != null) {
            fadeOut(view, view.getResources().getInteger(17694720));
        }
    }

    public static void fadeOut(final View view, int animationTime) {
        if (view != null) {
            view.clearAnimation();
            if (view.getVisibility() == 8) {
                view.setAnimation(null);
                view.setVisibility(8);
                return;
            }
            view.setVisibility(8);
            AlphaAnimation animate = new AlphaAnimation(DefaultRetryPolicy.DEFAULT_BACKOFF_MULT, 0.0f);
            animate.setDuration((long) animationTime);
            animate.setInterpolator(new DecelerateInterpolator());
            animate.setAnimationListener(new AnimationListener() {
                public void onAnimationStart(Animation animation) {
                }

                public void onAnimationEnd(Animation animation) {
                    view.setAnimation(null);
                }

                public void onAnimationRepeat(Animation animation) {
                }
            });
            view.startAnimation(animate);
        }
    }

    public static void fadeIn(final View view) {
        if (view != null) {
            view.clearAnimation();
            if (view.getVisibility() == 0) {
                view.setAnimation(null);
                view.setVisibility(0);
                return;
            }
            view.setVisibility(0);
            AlphaAnimation animate = new AlphaAnimation(0.0f, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
            animate.setDuration((long) view.getResources().getInteger(17694720));
            animate.setInterpolator(new AccelerateInterpolator());
            animate.setAnimationListener(new AnimationListener() {
                public void onAnimationStart(Animation animation) {
                }

                public void onAnimationEnd(Animation animation) {
                    view.setAnimation(null);
                }

                public void onAnimationRepeat(Animation animation) {
                }
            });
            view.startAnimation(animate);
        }
    }

    public static void slideOutToTop(View view) {
        if (view != null) {
            if (view.getVisibility() == 8) {
                view.setAnimation(null);
                view.setVisibility(8);
                return;
            }
            TranslateAnimation animate = new TranslateAnimation(0.0f, 0.0f, 0.0f, (float) (-view.getHeight()));
            animate.setDuration((long) view.getResources().getInteger(17694720));
            animate.setFillAfter(true);
            view.setAnimation(animate);
            view.startAnimation(animate);
            view.setVisibility(8);
        }
    }

    public static void slideInFromTop(View view) {
        if (view != null) {
            if (view.getVisibility() == 0) {
                view.setAnimation(null);
                view.setVisibility(0);
                return;
            }
            TranslateAnimation animate = new TranslateAnimation(0.0f, 0.0f, (float) (-view.getHeight()), 0.0f);
            animate.setDuration((long) view.getResources().getInteger(17694720));
            animate.setFillAfter(true);
            view.setAnimation(animate);
            view.startAnimation(animate);
            view.setVisibility(0);
        }
    }

    public static float getProgress(int value, int min, int max) {
        if (min != max) {
            return ((float) (value - min)) / ((float) (max - min));
        }
        throw new IllegalArgumentException("Max (" + max + ") cannot equal min (" + min + ")");
    }

    public static boolean isLandscape(Context context) {
        if (context == null || context.getResources() == null || 2 != context.getResources().getConfiguration().orientation) {
            return false;
        }
        return true;
    }

    public static Point getScreenSize(WindowManager windowManager) {
        int width;
        int height;
        Point size = new Point();
        if (VERSION.SDK_INT >= 11) {
            windowManager.getDefaultDisplay().getSize(size);
            width = size.x;
            height = size.y;
        } else {
            Display d = windowManager.getDefaultDisplay();
            width = d.getWidth();
            height = d.getHeight();
        }
        return new Point(width, height);
    }

    public static void hideVirtualKeyboard(FragmentActivity a) {
        hideVirtualKeyboard(a, null);
    }

    public static void hideVirtualKeyboard(FragmentActivity a, IBinder windowToken) {
        if (a != null) {
            InputMethodManager imm = (InputMethodManager) a.getSystemService("input_method");
            if (imm != null) {
                if (windowToken == null) {
                    windowToken = a.getWindow().getDecorView().getWindowToken();
                }
                try {
                    imm.hideSoftInputFromWindow(windowToken, 0);
                } catch (Exception e) {
                }
            }
        }
    }

    public static void hideVirtualKeyboard(Service service, View view) {
        if (service != null) {
            InputMethodManager imm = (InputMethodManager) service.getSystemService("input_method");
            if (imm != null) {
                try {
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                } catch (Exception e) {
                }
            }
        }
    }

    public static void hideVirtualKeyboard(Activity a, IBinder windowToken) {
        if (a != null) {
            InputMethodManager imm = (InputMethodManager) a.getSystemService("input_method");
            if (imm != null) {
                if (windowToken == null) {
                    windowToken = a.getWindow().getDecorView().getWindowToken();
                }
                try {
                    imm.hideSoftInputFromWindow(windowToken, 0);
                } catch (Exception e) {
                }
            }
        }
    }

    public static void showVirtualKeyboard(Activity a) {
        if (a != null) {
            InputMethodManager imm = (InputMethodManager) a.getSystemService("input_method");
            if (imm != null) {
                imm.toggleSoftInput(2, 2);
            }
        }
    }

    public static File createImageFile() throws IOException {
        String imageFileName = "JPEG_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + EventsFilesManager.ROLL_OVER_FILE_NAME_SEPARATOR;
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        storageDir.mkdirs();
        return File.createTempFile(imageFileName, ".jpg", storageDir);
    }

    public static Bitmap rotateBitmap(Bitmap bitmap, int orientation) {
        Matrix matrix = new Matrix();
        switch (orientation) {
            case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
                matrix.setScale(-1.0f, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                break;
            case TimePickerDialog.ENABLE_PICKER_INDEX /*3*/:
                matrix.setRotate(180.0f);
                break;
            case CommonUtils.DEVICE_STATE_DEBUGGERATTACHED /*4*/:
                matrix.setRotate(180.0f);
                matrix.postScale(-1.0f, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                break;
            case Player.STATE_ENDED /*5*/:
                matrix.setRotate(90.0f);
                matrix.postScale(-1.0f, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                break;
            case R.styleable.Toolbar_contentInsetEnd /*6*/:
                matrix.setRotate(90.0f);
                break;
            case DayPickerView.DAYS_PER_WEEK /*7*/:
                matrix.setRotate(-90.0f);
                matrix.postScale(-1.0f, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);
                break;
            case SettingsJsonConstants.SETTINGS_MAX_CHAINED_EXCEPTION_DEPTH_DEFAULT /*8*/:
                matrix.setRotate(-90.0f);
                break;
            default:
                return bitmap;
        }
        try {
            Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return bmRotated;
        } catch (OutOfMemoryError e) {
            Crashlytics.logException(e);
            e.printStackTrace();
            return bitmap;
        }
    }

    public static Pair<Integer, Integer> getImageSize(String fileName) {
        Options options = new Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(fileName, options);
        return new Pair(Integer.valueOf(options.outWidth), Integer.valueOf(options.outHeight));
    }

    public static void colorize(Drawable d, int color) {
        d.setColorFilter(new LightingColorFilter(ViewCompat.MEASURED_STATE_MASK, color));
    }

    public static void colorizeProgress(ProgressBar pb, int color) {
        if (pb != null) {
            Drawable d = pb.getIndeterminateDrawable();
            d.setColorFilter(new LightingColorFilter(ViewCompat.MEASURED_STATE_MASK, color));
            pb.setIndeterminateDrawable(d);
        }
    }

    public static void enableIconsForPopupMenu(PopupMenu popup) {
        try {
            for (Field field : popup.getClass().getDeclaredFields()) {
                if ("mPopup".equals(field.getName())) {
                    field.setAccessible(true);
                    Object menuPopupHelper = field.get(popup);
                    Class.forName(menuPopupHelper.getClass().getName()).getMethod("setForceShowIcon", new Class[]{Boolean.TYPE}).invoke(menuPopupHelper, new Object[]{Boolean.valueOf(true)});
                    return;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showListMenu(ListPopupWindow listPopupWindow, final View anchor, List<HashMap<String, Object>> data, String[] keys, OnItemClickListener listener) {
        if (anchor == null) {
            throw new IllegalArgumentException("anchor should be not null");
        }
        final ListPopupWindow popupWindow = listPopupWindow != null ? listPopupWindow : new ListPopupWindow(anchor.getContext());
        final List<HashMap<String, Object>> list = data;
        ListAdapter adapter = new SimpleAdapter(anchor.getContext(), data, R.layout.item_menu_list, keys, new int[]{16908308, 16908294}) {
            public long getItemId(int position) {
                return (long) ((HashMap) list.get(position)).get(Constants.MENU_ADAPTER_KEYS[0]).hashCode();
            }
        };
        anchor.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            public void onGlobalLayout() {
                try {
                    if (!anchor.isShown() && popupWindow.isShowing()) {
                        popupWindow.dismiss();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Crashlytics.logException(e);
                }
            }
        });
        popupWindow.setAnchorView(anchor);
        popupWindow.setAdapter(adapter);
        popupWindow.setWidth(anchor.getResources().getDimensionPixelSize(R.dimen.popup_menu_width));
        popupWindow.setOnItemClickListener(listener);
        popupWindow.setModal(true);
        popupWindow.show();
        ListView listView = popupWindow.getListView();
        if (listView != null) {
            listView.setDivider(anchor.getResources().getDrawable(R.drawable.popup_list_divider));
            listView.setDividerHeight(anchor.getResources().getDimensionPixelSize(R.dimen.list_divider_height));
        }
    }

    public static void addListMenuItem(List<HashMap<String, Object>> data, String title, int iconResourceId) {
        HashMap<String, Object> map = new HashMap();
        map.put(Constants.MENU_ADAPTER_KEYS[0], title);
        map.put(Constants.MENU_ADAPTER_KEYS[1], Integer.valueOf(iconResourceId));
        data.add(map);
    }

    public static SpannableString colorizeText(CharSequence text, int colorResId) {
        if (text == null) {
            return null;
        }
        SpannableString spannable = new SpannableString(text);
        spannable.setSpan(new ForegroundColorSpan(MainApplication.getContext().getResources().getColor(colorResId)), 0, spannable.length(), 0);
        return spannable;
    }
}
