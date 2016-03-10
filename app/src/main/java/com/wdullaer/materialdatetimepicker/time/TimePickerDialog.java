package com.wdullaer.materialdatetimepicker.time;

import android.animation.ObjectAnimator;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.wdullaer.materialdatetimepicker.HapticFeedbackController;
import com.wdullaer.materialdatetimepicker.R;
import com.wdullaer.materialdatetimepicker.TypefaceHelper;
import com.wdullaer.materialdatetimepicker.Utils;
import com.wdullaer.materialdatetimepicker.date.DayPickerView;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout.OnValueSelectedListener;
import io.fabric.sdk.android.BuildConfig;
import io.fabric.sdk.android.services.common.CommonUtils;
import io.fabric.sdk.android.services.settings.SettingsJsonConstants;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Locale;
import org.apache.http.message.TokenParser;
import org.apache.http.protocol.HTTP;

public class TimePickerDialog extends DialogFragment implements OnValueSelectedListener {
    public static final int AM = 0;
    public static final int AMPM_INDEX = 2;
    public static final int ENABLE_PICKER_INDEX = 3;
    public static final int HOUR_INDEX = 0;
    private static final String KEY_CURRENT_ITEM_SHOWING = "current_item_showing";
    private static final String KEY_DARK_THEME = "dark_theme";
    private static final String KEY_HOUR_OF_DAY = "hour_of_day";
    private static final String KEY_IN_KB_MODE = "in_kb_mode";
    private static final String KEY_IS_24_HOUR_VIEW = "is_24_hour_view";
    private static final String KEY_MINUTE = "minute";
    private static final String KEY_TITLE = "dialog_title";
    private static final String KEY_TYPED_TIMES = "typed_times";
    private static final String KEY_VIBRATE = "vibrate";
    public static final int MINUTE_INDEX = 1;
    public static final int PM = 1;
    private static final int PULSE_ANIMATOR_DELAY = 300;
    private static final String TAG = "TimePickerDialog";
    private boolean mAllowAutoAdvance;
    private int mAmKeyCode;
    private View mAmPmHitspace;
    private TextView mAmPmTextView;
    private String mAmText;
    private OnTimeSetListener mCallback;
    private String mDeletedKeyFormat;
    private String mDoublePlaceholderText;
    private HapticFeedbackController mHapticFeedbackController;
    private String mHourPickerDescription;
    private TextView mHourSpaceView;
    private TextView mHourView;
    private boolean mInKbMode;
    private int mInitialHourOfDay;
    private int mInitialMinute;
    private boolean mIs24HourMode;
    private Node mLegalTimesTree;
    private String mMinutePickerDescription;
    private TextView mMinuteSpaceView;
    private TextView mMinuteView;
    private Button mOkButton;
    private OnCancelListener mOnCancelListener;
    private OnDismissListener mOnDismissListener;
    private char mPlaceholderText;
    private int mPmKeyCode;
    private String mPmText;
    private String mSelectHours;
    private String mSelectMinutes;
    private int mSelectedColor;
    private boolean mThemeDark;
    private RadialPickerLayout mTimePicker;
    private String mTitle;
    private ArrayList<Integer> mTypedTimes;
    private int mUnselectedColor;
    private boolean mVibrate;

    private class KeyboardListener implements OnKeyListener {
        private KeyboardListener() {
        }

        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (event.getAction() == TimePickerDialog.PM) {
                return TimePickerDialog.this.processKeyUp(keyCode);
            }
            return false;
        }
    }

    private static class Node {
        private ArrayList<Node> mChildren = new ArrayList();
        private int[] mLegalKeys;

        public Node(int... legalKeys) {
            this.mLegalKeys = legalKeys;
        }

        public void addChild(Node child) {
            this.mChildren.add(child);
        }

        public boolean containsKey(int key) {
            for (int i = TimePickerDialog.HOUR_INDEX; i < this.mLegalKeys.length; i += TimePickerDialog.PM) {
                if (this.mLegalKeys[i] == key) {
                    return true;
                }
            }
            return false;
        }

        public Node canReach(int key) {
            if (this.mChildren == null) {
                return null;
            }
            Iterator i$ = this.mChildren.iterator();
            while (i$.hasNext()) {
                Node child = (Node) i$.next();
                if (child.containsKey(key)) {
                    return child;
                }
            }
            return null;
        }
    }

    public interface OnTimeSetListener {
        void onTimeSet(RadialPickerLayout radialPickerLayout, int i, int i2);
    }

    public static TimePickerDialog newInstance(OnTimeSetListener callback, int hourOfDay, int minute, boolean is24HourMode) {
        TimePickerDialog ret = new TimePickerDialog();
        ret.initialize(callback, hourOfDay, minute, is24HourMode);
        return ret;
    }

    public void initialize(OnTimeSetListener callback, int hourOfDay, int minute, boolean is24HourMode) {
        this.mCallback = callback;
        this.mInitialHourOfDay = hourOfDay;
        this.mInitialMinute = minute;
        this.mIs24HourMode = is24HourMode;
        this.mInKbMode = false;
        this.mTitle = BuildConfig.FLAVOR;
        this.mThemeDark = false;
        this.mVibrate = true;
    }

    public void setTitle(String title) {
        this.mTitle = title;
    }

    public String getTitle() {
        return this.mTitle;
    }

    public void setThemeDark(boolean dark) {
        this.mThemeDark = dark;
    }

    public boolean isThemeDark() {
        return this.mThemeDark;
    }

    public void vibrate(boolean vibrate) {
        this.mVibrate = vibrate;
    }

    public void setOnTimeSetListener(OnTimeSetListener callback) {
        this.mCallback = callback;
    }

    public void setOnCancelListener(OnCancelListener onCancelListener) {
        this.mOnCancelListener = onCancelListener;
    }

    public void setOnDismissListener(OnDismissListener onDismissListener) {
        this.mOnDismissListener = onDismissListener;
    }

    public void setStartTime(int hourOfDay, int minute) {
        this.mInitialHourOfDay = hourOfDay;
        this.mInitialMinute = minute;
        this.mInKbMode = false;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null && savedInstanceState.containsKey(KEY_HOUR_OF_DAY) && savedInstanceState.containsKey(KEY_MINUTE) && savedInstanceState.containsKey(KEY_IS_24_HOUR_VIEW)) {
            this.mInitialHourOfDay = savedInstanceState.getInt(KEY_HOUR_OF_DAY);
            this.mInitialMinute = savedInstanceState.getInt(KEY_MINUTE);
            this.mIs24HourMode = savedInstanceState.getBoolean(KEY_IS_24_HOUR_VIEW);
            this.mInKbMode = savedInstanceState.getBoolean(KEY_IN_KB_MODE);
            this.mTitle = savedInstanceState.getString(KEY_TITLE);
            this.mThemeDark = savedInstanceState.getBoolean(KEY_DARK_THEME);
            this.mVibrate = savedInstanceState.getBoolean(KEY_VIBRATE);
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(PM);
        View view = inflater.inflate(R.layout.mdtp_time_picker_dialog, null);
        TimePickerDialog timePickerDialog = this;
        KeyboardListener keyboardListener = new KeyboardListener();
        view.findViewById(R.id.time_picker_dialog).setOnKeyListener(keyboardListener);
        Resources res = getResources();
        this.mHourPickerDescription = res.getString(R.string.mdtp_hour_picker_description);
        this.mSelectHours = res.getString(R.string.mdtp_select_hours);
        this.mMinutePickerDescription = res.getString(R.string.mdtp_minute_picker_description);
        this.mSelectMinutes = res.getString(R.string.mdtp_select_minutes);
        this.mSelectedColor = res.getColor(R.color.mdtp_white);
        this.mUnselectedColor = res.getColor(R.color.mdtp_accent_color_focused);
        this.mHourView = (TextView) view.findViewById(R.id.hours);
        this.mHourView.setOnKeyListener(keyboardListener);
        this.mHourSpaceView = (TextView) view.findViewById(R.id.hour_space);
        this.mMinuteSpaceView = (TextView) view.findViewById(R.id.minutes_space);
        this.mMinuteView = (TextView) view.findViewById(R.id.minutes);
        this.mMinuteView.setOnKeyListener(keyboardListener);
        this.mAmPmTextView = (TextView) view.findViewById(R.id.ampm_label);
        this.mAmPmTextView.setOnKeyListener(keyboardListener);
        String[] amPmTexts = new DateFormatSymbols().getAmPmStrings();
        this.mAmText = amPmTexts[HOUR_INDEX];
        this.mPmText = amPmTexts[PM];
        this.mHapticFeedbackController = new HapticFeedbackController(getActivity());
        this.mTimePicker = (RadialPickerLayout) view.findViewById(R.id.time_picker);
        this.mTimePicker.setOnValueSelectedListener(this);
        this.mTimePicker.setOnKeyListener(keyboardListener);
        this.mTimePicker.initialize(getActivity(), this, this.mInitialHourOfDay, this.mInitialMinute, this.mIs24HourMode);
        int currentItemShowing = HOUR_INDEX;
        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(KEY_CURRENT_ITEM_SHOWING)) {
                currentItemShowing = savedInstanceState.getInt(KEY_CURRENT_ITEM_SHOWING);
            }
        }
        setCurrentItemShowing(currentItemShowing, false, true, true);
        this.mTimePicker.invalidate();
        this.mHourView.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                TimePickerDialog.this.setCurrentItemShowing(TimePickerDialog.HOUR_INDEX, true, false, true);
                TimePickerDialog.this.tryVibrate();
            }
        });
        this.mMinuteView.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                TimePickerDialog.this.setCurrentItemShowing(TimePickerDialog.PM, true, false, true);
                TimePickerDialog.this.tryVibrate();
            }
        });
        this.mOkButton = (Button) view.findViewById(R.id.ok);
        this.mOkButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (TimePickerDialog.this.mInKbMode && TimePickerDialog.this.isTypedTimeFullyLegal()) {
                    TimePickerDialog.this.finishKbMode(false);
                } else {
                    TimePickerDialog.this.tryVibrate();
                }
                if (TimePickerDialog.this.mCallback != null) {
                    TimePickerDialog.this.mCallback.onTimeSet(TimePickerDialog.this.mTimePicker, TimePickerDialog.this.mTimePicker.getHours(), TimePickerDialog.this.mTimePicker.getMinutes());
                }
                TimePickerDialog.this.dismiss();
            }
        });
        this.mOkButton.setOnKeyListener(keyboardListener);
        this.mOkButton.setTypeface(TypefaceHelper.get(getDialog().getContext(), "Roboto-Medium"));
        Button mCancelButton = (Button) view.findViewById(R.id.cancel);
        mCancelButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                TimePickerDialog.this.tryVibrate();
                TimePickerDialog.this.getDialog().cancel();
            }
        });
        mCancelButton.setTypeface(TypefaceHelper.get(getDialog().getContext(), "Roboto-Medium"));
        mCancelButton.setVisibility(isCancelable() ? HOUR_INDEX : 8);
        this.mAmPmHitspace = view.findViewById(R.id.ampm_hitspace);
        if (this.mIs24HourMode) {
            this.mAmPmTextView.setVisibility(8);
            LayoutParams layoutParams = new RelativeLayout.LayoutParams(-2, -2);
            layoutParams.addRule(13);
            ((TextView) view.findViewById(R.id.separator)).setLayoutParams(layoutParams);
        } else {
            this.mAmPmTextView.setVisibility(HOUR_INDEX);
            updateAmPmDisplay(this.mInitialHourOfDay < 12 ? HOUR_INDEX : PM);
            this.mAmPmHitspace.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    TimePickerDialog.this.tryVibrate();
                    int amOrPm = TimePickerDialog.this.mTimePicker.getIsCurrentlyAmOrPm();
                    if (amOrPm == 0) {
                        amOrPm = TimePickerDialog.PM;
                    } else if (amOrPm == TimePickerDialog.PM) {
                        amOrPm = TimePickerDialog.HOUR_INDEX;
                    }
                    TimePickerDialog.this.updateAmPmDisplay(amOrPm);
                    TimePickerDialog.this.mTimePicker.setAmOrPm(amOrPm);
                }
            });
        }
        this.mAllowAutoAdvance = true;
        setHour(this.mInitialHourOfDay, true);
        setMinute(this.mInitialMinute);
        this.mDoublePlaceholderText = res.getString(R.string.mdtp_time_placeholder);
        this.mDeletedKeyFormat = res.getString(R.string.mdtp_deleted_key);
        this.mPlaceholderText = this.mDoublePlaceholderText.charAt(HOUR_INDEX);
        this.mPmKeyCode = -1;
        this.mAmKeyCode = -1;
        generateLegalTimesTree();
        if (this.mInKbMode) {
            this.mTypedTimes = savedInstanceState.getIntegerArrayList(KEY_TYPED_TIMES);
            tryStartingKbMode(-1);
            this.mHourView.invalidate();
        } else if (this.mTypedTimes == null) {
            this.mTypedTimes = new ArrayList();
        }
        TextView timePickerHeader = (TextView) view.findViewById(R.id.time_picker_header);
        if (!this.mTitle.isEmpty()) {
            timePickerHeader.setVisibility(HOUR_INDEX);
            timePickerHeader.setText(this.mTitle);
        }
        this.mTimePicker.setTheme(getActivity().getApplicationContext(), this.mThemeDark);
        int white = res.getColor(R.color.mdtp_white);
        int accent = res.getColor(R.color.mdtp_accent_color);
        int circleBackground = res.getColor(R.color.mdtp_circle_background);
        int line = res.getColor(R.color.mdtp_line_background);
        int timeDisplay = res.getColor(R.color.mdtp_numbers_text_color);
        ColorStateList doneTextColor = res.getColorStateList(R.color.mdtp_done_text_color);
        int doneBackground = R.drawable.mdtp_done_background_color;
        int backgroundColor = res.getColor(R.color.mdtp_background_color);
        int darkBackgroundColor = res.getColor(R.color.mdtp_light_gray);
        int darkGray = res.getColor(R.color.mdtp_dark_gray);
        int lightGray = res.getColor(R.color.mdtp_light_gray);
        int darkLine = res.getColor(R.color.mdtp_line_dark);
        ColorStateList darkDoneTextColor = res.getColorStateList(R.color.mdtp_done_text_color_dark);
        int darkDoneBackground = R.drawable.mdtp_done_background_color_dark;
        RadialPickerLayout radialPickerLayout = this.mTimePicker;
        if (!this.mThemeDark) {
            lightGray = circleBackground;
        }
        radialPickerLayout.setBackgroundColor(lightGray);
        View findViewById = view.findViewById(R.id.time_picker_dialog);
        if (!this.mThemeDark) {
            darkBackgroundColor = backgroundColor;
        }
        findViewById.setBackgroundColor(darkBackgroundColor);
        return view;
    }

    public void onResume() {
        super.onResume();
        this.mHapticFeedbackController.start();
    }

    public void onPause() {
        super.onPause();
        this.mHapticFeedbackController.stop();
    }

    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        if (this.mOnCancelListener != null) {
            this.mOnCancelListener.onCancel(dialog);
        }
    }

    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (this.mOnDismissListener != null) {
            this.mOnDismissListener.onDismiss(dialog);
        }
    }

    public void tryVibrate() {
        if (this.mVibrate) {
            this.mHapticFeedbackController.tryVibrate();
        }
    }

    private void updateAmPmDisplay(int amOrPm) {
        if (amOrPm == 0) {
            this.mAmPmTextView.setText(this.mAmText);
            Utils.tryAccessibilityAnnounce(this.mTimePicker, this.mAmText);
            this.mAmPmHitspace.setContentDescription(this.mAmText);
        } else if (amOrPm == PM) {
            this.mAmPmTextView.setText(this.mPmText);
            Utils.tryAccessibilityAnnounce(this.mTimePicker, this.mPmText);
            this.mAmPmHitspace.setContentDescription(this.mPmText);
        } else {
            this.mAmPmTextView.setText(this.mDoublePlaceholderText);
        }
    }

    public void onSaveInstanceState(@NonNull Bundle outState) {
        if (this.mTimePicker != null) {
            outState.putInt(KEY_HOUR_OF_DAY, this.mTimePicker.getHours());
            outState.putInt(KEY_MINUTE, this.mTimePicker.getMinutes());
            outState.putBoolean(KEY_IS_24_HOUR_VIEW, this.mIs24HourMode);
            outState.putInt(KEY_CURRENT_ITEM_SHOWING, this.mTimePicker.getCurrentItemShowing());
            outState.putBoolean(KEY_IN_KB_MODE, this.mInKbMode);
            if (this.mInKbMode) {
                outState.putIntegerArrayList(KEY_TYPED_TIMES, this.mTypedTimes);
            }
            outState.putString(KEY_TITLE, this.mTitle);
            outState.putBoolean(KEY_DARK_THEME, this.mThemeDark);
            outState.putBoolean(KEY_VIBRATE, this.mVibrate);
        }
    }

    public void onValueSelected(int pickerIndex, int newValue, boolean autoAdvance) {
        if (pickerIndex == 0) {
            setHour(newValue, false);
            Object[] objArr = new Object[PM];
            objArr[HOUR_INDEX] = Integer.valueOf(newValue);
            String announcement = String.format("%d", objArr);
            if (this.mAllowAutoAdvance && autoAdvance) {
                setCurrentItemShowing(PM, true, true, false);
                announcement = announcement + ". " + this.mSelectMinutes;
            } else {
                this.mTimePicker.setContentDescription(this.mHourPickerDescription + ": " + newValue);
            }
            Utils.tryAccessibilityAnnounce(this.mTimePicker, announcement);
        } else if (pickerIndex == PM) {
            setMinute(newValue);
            this.mTimePicker.setContentDescription(this.mMinutePickerDescription + ": " + newValue);
        } else if (pickerIndex == AMPM_INDEX) {
            updateAmPmDisplay(newValue);
        } else if (pickerIndex == ENABLE_PICKER_INDEX) {
            if (!isTypedTimeFullyLegal()) {
                this.mTypedTimes.clear();
            }
            finishKbMode(true);
        }
    }

    private void setHour(int value, boolean announce) {
        String format;
        if (this.mIs24HourMode) {
            format = "%02d";
        } else {
            format = "%d";
            value %= 12;
            if (value == 0) {
                value = 12;
            }
        }
        Object[] objArr = new Object[PM];
        objArr[HOUR_INDEX] = Integer.valueOf(value);
        CharSequence text = String.format(format, objArr);
        this.mHourView.setText(text);
        this.mHourSpaceView.setText(text);
        if (announce) {
            Utils.tryAccessibilityAnnounce(this.mTimePicker, text);
        }
    }

    private void setMinute(int value) {
        if (value == 60) {
            value = HOUR_INDEX;
        }
        Object[] objArr = new Object[PM];
        objArr[HOUR_INDEX] = Integer.valueOf(value);
        CharSequence text = String.format(Locale.getDefault(), "%02d", objArr);
        Utils.tryAccessibilityAnnounce(this.mTimePicker, text);
        this.mMinuteView.setText(text);
        this.mMinuteSpaceView.setText(text);
    }

    private void setCurrentItemShowing(int index, boolean animateCircle, boolean delayLabelAnimate, boolean announce) {
        TextView labelToAnimate;
        this.mTimePicker.setCurrentItemShowing(index, animateCircle);
        if (index == 0) {
            int hours = this.mTimePicker.getHours();
            if (!this.mIs24HourMode) {
                hours %= 12;
            }
            this.mTimePicker.setContentDescription(this.mHourPickerDescription + ": " + hours);
            if (announce) {
                Utils.tryAccessibilityAnnounce(this.mTimePicker, this.mSelectHours);
            }
            labelToAnimate = this.mHourView;
        } else {
            this.mTimePicker.setContentDescription(this.mMinutePickerDescription + ": " + this.mTimePicker.getMinutes());
            if (announce) {
                Utils.tryAccessibilityAnnounce(this.mTimePicker, this.mSelectMinutes);
            }
            labelToAnimate = this.mMinuteView;
        }
        int hourColor = index == 0 ? this.mSelectedColor : this.mUnselectedColor;
        int minuteColor = index == PM ? this.mSelectedColor : this.mUnselectedColor;
        this.mHourView.setTextColor(hourColor);
        this.mMinuteView.setTextColor(minuteColor);
        ObjectAnimator pulseAnimator = Utils.getPulseAnimator(labelToAnimate, 0.85f, 1.1f);
        if (delayLabelAnimate) {
            pulseAnimator.setStartDelay(300);
        }
        pulseAnimator.start();
    }

    private boolean processKeyUp(int keyCode) {
        if (keyCode != 111 && keyCode != 4) {
            if (keyCode == 61) {
                if (this.mInKbMode) {
                    if (!isTypedTimeFullyLegal()) {
                        return true;
                    }
                    finishKbMode(true);
                    return true;
                }
            } else if (keyCode == 66) {
                if (this.mInKbMode) {
                    if (!isTypedTimeFullyLegal()) {
                        return true;
                    }
                    finishKbMode(false);
                }
                if (this.mCallback != null) {
                    this.mCallback.onTimeSet(this.mTimePicker, this.mTimePicker.getHours(), this.mTimePicker.getMinutes());
                }
                dismiss();
                return true;
            } else if (keyCode == 67) {
                if (this.mInKbMode && !this.mTypedTimes.isEmpty()) {
                    String deletedKeyStr;
                    int deleted = deleteLastTypedKey();
                    if (deleted == getAmOrPmKeyCode(HOUR_INDEX)) {
                        deletedKeyStr = this.mAmText;
                    } else if (deleted == getAmOrPmKeyCode(PM)) {
                        deletedKeyStr = this.mPmText;
                    } else {
                        Object[] objArr = new Object[PM];
                        objArr[HOUR_INDEX] = Integer.valueOf(getValFromKeyCode(deleted));
                        deletedKeyStr = String.format("%d", objArr);
                    }
                    View view = this.mTimePicker;
                    String str = this.mDeletedKeyFormat;
                    Object[] objArr2 = new Object[PM];
                    objArr2[HOUR_INDEX] = deletedKeyStr;
                    Utils.tryAccessibilityAnnounce(view, String.format(str, objArr2));
                    updateDisplay(true);
                }
            } else if (keyCode == 7 || keyCode == 8 || keyCode == 9 || keyCode == 10 || keyCode == 11 || keyCode == 12 || keyCode == 13 || keyCode == 14 || keyCode == 15 || keyCode == 16 || (!this.mIs24HourMode && (keyCode == getAmOrPmKeyCode(HOUR_INDEX) || keyCode == getAmOrPmKeyCode(PM)))) {
                if (this.mInKbMode) {
                    if (!addKeyIfLegal(keyCode)) {
                        return true;
                    }
                    updateDisplay(false);
                    return true;
                } else if (this.mTimePicker == null) {
                    Log.e(TAG, "Unable to initiate keyboard mode, TimePicker was null.");
                    return true;
                } else {
                    this.mTypedTimes.clear();
                    tryStartingKbMode(keyCode);
                    return true;
                }
            }
            return false;
        } else if (!isCancelable()) {
            return true;
        } else {
            dismiss();
            return true;
        }
    }

    private void tryStartingKbMode(int keyCode) {
        if (!this.mTimePicker.trySettingInputEnabled(false)) {
            return;
        }
        if (keyCode == -1 || addKeyIfLegal(keyCode)) {
            this.mInKbMode = true;
            this.mOkButton.setEnabled(false);
            updateDisplay(false);
        }
    }

    private boolean addKeyIfLegal(int keyCode) {
        if (this.mIs24HourMode && this.mTypedTimes.size() == 4) {
            return false;
        }
        if (!this.mIs24HourMode && isTypedTimeFullyLegal()) {
            return false;
        }
        this.mTypedTimes.add(Integer.valueOf(keyCode));
        if (isTypedTimeLegalSoFar()) {
            int val = getValFromKeyCode(keyCode);
            View view = this.mTimePicker;
            Object[] objArr = new Object[PM];
            objArr[HOUR_INDEX] = Integer.valueOf(val);
            Utils.tryAccessibilityAnnounce(view, String.format("%d", objArr));
            if (isTypedTimeFullyLegal()) {
                if (!this.mIs24HourMode && this.mTypedTimes.size() <= ENABLE_PICKER_INDEX) {
                    this.mTypedTimes.add(this.mTypedTimes.size() - 1, Integer.valueOf(7));
                    this.mTypedTimes.add(this.mTypedTimes.size() - 1, Integer.valueOf(7));
                }
                this.mOkButton.setEnabled(true);
            }
            return true;
        }
        deleteLastTypedKey();
        return false;
    }

    private boolean isTypedTimeLegalSoFar() {
        Node node = this.mLegalTimesTree;
        Iterator i$ = this.mTypedTimes.iterator();
        while (i$.hasNext()) {
            node = node.canReach(((Integer) i$.next()).intValue());
            if (node == null) {
                return false;
            }
        }
        return true;
    }

    private boolean isTypedTimeFullyLegal() {
        boolean z = false;
        if (this.mIs24HourMode) {
            int[] values = getEnteredTime(null);
            if (values[HOUR_INDEX] < 0 || values[PM] < 0 || values[PM] >= 60) {
                return false;
            }
            return true;
        }
        if (this.mTypedTimes.contains(Integer.valueOf(getAmOrPmKeyCode(HOUR_INDEX))) || this.mTypedTimes.contains(Integer.valueOf(getAmOrPmKeyCode(PM)))) {
            z = PM;
        }
        return z;
    }

    private int deleteLastTypedKey() {
        int deleted = ((Integer) this.mTypedTimes.remove(this.mTypedTimes.size() - 1)).intValue();
        if (!isTypedTimeFullyLegal()) {
            this.mOkButton.setEnabled(false);
        }
        return deleted;
    }

    private void finishKbMode(boolean updateDisplays) {
        this.mInKbMode = false;
        if (!this.mTypedTimes.isEmpty()) {
            int[] values = getEnteredTime(null);
            this.mTimePicker.setTime(values[HOUR_INDEX], values[PM]);
            if (!this.mIs24HourMode) {
                this.mTimePicker.setAmOrPm(values[AMPM_INDEX]);
            }
            this.mTypedTimes.clear();
        }
        if (updateDisplays) {
            updateDisplay(false);
            this.mTimePicker.trySettingInputEnabled(true);
        }
    }

    private void updateDisplay(boolean allowEmptyDisplay) {
        if (allowEmptyDisplay || !this.mTypedTimes.isEmpty()) {
            String hourStr;
            Object[] objArr;
            String minuteStr;
            Boolean[] enteredZeros = new Boolean[AMPM_INDEX];
            enteredZeros[HOUR_INDEX] = Boolean.valueOf(false);
            enteredZeros[PM] = Boolean.valueOf(false);
            int[] values = getEnteredTime(enteredZeros);
            String hourFormat = enteredZeros[HOUR_INDEX].booleanValue() ? "%02d" : "%2d";
            String minuteFormat = enteredZeros[PM].booleanValue() ? "%02d" : "%2d";
            if (values[HOUR_INDEX] == -1) {
                hourStr = this.mDoublePlaceholderText;
            } else {
                objArr = new Object[PM];
                objArr[HOUR_INDEX] = Integer.valueOf(values[HOUR_INDEX]);
                hourStr = String.format(hourFormat, objArr).replace(TokenParser.SP, this.mPlaceholderText);
            }
            if (values[PM] == -1) {
                minuteStr = this.mDoublePlaceholderText;
            } else {
                objArr = new Object[PM];
                objArr[HOUR_INDEX] = Integer.valueOf(values[PM]);
                minuteStr = String.format(minuteFormat, objArr).replace(TokenParser.SP, this.mPlaceholderText);
            }
            this.mHourView.setText(hourStr);
            this.mHourSpaceView.setText(hourStr);
            this.mHourView.setTextColor(this.mUnselectedColor);
            this.mMinuteView.setText(minuteStr);
            this.mMinuteSpaceView.setText(minuteStr);
            this.mMinuteView.setTextColor(this.mUnselectedColor);
            if (!this.mIs24HourMode) {
                updateAmPmDisplay(values[AMPM_INDEX]);
                return;
            }
            return;
        }
        int hour = this.mTimePicker.getHours();
        int minute = this.mTimePicker.getMinutes();
        setHour(hour, true);
        setMinute(minute);
        if (!this.mIs24HourMode) {
            updateAmPmDisplay(hour < 12 ? HOUR_INDEX : PM);
        }
        setCurrentItemShowing(this.mTimePicker.getCurrentItemShowing(), true, true, true);
        this.mOkButton.setEnabled(true);
    }

    private static int getValFromKeyCode(int keyCode) {
        switch (keyCode) {
            case DayPickerView.DAYS_PER_WEEK /*7*/:
                return HOUR_INDEX;
            case SettingsJsonConstants.SETTINGS_MAX_CHAINED_EXCEPTION_DEPTH_DEFAULT /*8*/:
                return PM;
            case HTTP.HT /*9*/:
                return AMPM_INDEX;
            case HTTP.LF /*10*/:
                return ENABLE_PICKER_INDEX;
            case com.mobcrush.mobcrush.R.styleable.Toolbar_subtitleTextAppearance /*11*/:
                return 4;
            case com.mobcrush.mobcrush.R.styleable.Toolbar_titleMargins /*12*/:
                return 5;
            case HTTP.CR /*13*/:
                return 6;
            case com.mobcrush.mobcrush.R.styleable.Toolbar_titleMarginEnd /*14*/:
                return 7;
            case com.mobcrush.mobcrush.R.styleable.Toolbar_titleMarginTop /*15*/:
                return 8;
            case CommonUtils.DEVICE_STATE_VENDORINTERNAL /*16*/:
                return 9;
            default:
                return -1;
        }
    }

    private int[] getEnteredTime(Boolean[] enteredZeros) {
        int amOrPm = -1;
        int startIndex = PM;
        if (!this.mIs24HourMode && isTypedTimeFullyLegal()) {
            int keyCode = ((Integer) this.mTypedTimes.get(this.mTypedTimes.size() - 1)).intValue();
            if (keyCode == getAmOrPmKeyCode(HOUR_INDEX)) {
                amOrPm = HOUR_INDEX;
            } else if (keyCode == getAmOrPmKeyCode(PM)) {
                amOrPm = PM;
            }
            startIndex = AMPM_INDEX;
        }
        int minute = -1;
        int hour = -1;
        for (int i = startIndex; i <= this.mTypedTimes.size(); i += PM) {
            int val = getValFromKeyCode(((Integer) this.mTypedTimes.get(this.mTypedTimes.size() - i)).intValue());
            if (i == startIndex) {
                minute = val;
            } else if (i == startIndex + PM) {
                minute += val * 10;
                if (enteredZeros != null && val == 0) {
                    enteredZeros[PM] = Boolean.valueOf(true);
                }
            } else if (i == startIndex + AMPM_INDEX) {
                hour = val;
            } else if (i == startIndex + ENABLE_PICKER_INDEX) {
                hour += val * 10;
                if (enteredZeros != null && val == 0) {
                    enteredZeros[HOUR_INDEX] = Boolean.valueOf(true);
                }
            }
        }
        int[] iArr = new int[ENABLE_PICKER_INDEX];
        iArr[HOUR_INDEX] = hour;
        iArr[PM] = minute;
        iArr[AMPM_INDEX] = amOrPm;
        return iArr;
    }

    private int getAmOrPmKeyCode(int amOrPm) {
        if (this.mAmKeyCode == -1 || this.mPmKeyCode == -1) {
            KeyCharacterMap kcm = KeyCharacterMap.load(-1);
            int i = HOUR_INDEX;
            while (i < Math.max(this.mAmText.length(), this.mPmText.length())) {
                char amChar = this.mAmText.toLowerCase(Locale.getDefault()).charAt(i);
                char pmChar = this.mPmText.toLowerCase(Locale.getDefault()).charAt(i);
                if (amChar != pmChar) {
                    char[] cArr = new char[AMPM_INDEX];
                    cArr[HOUR_INDEX] = amChar;
                    cArr[PM] = pmChar;
                    KeyEvent[] events = kcm.getEvents(cArr);
                    if (events == null || events.length != 4) {
                        Log.e(TAG, "Unable to find keycodes for AM and PM.");
                    } else {
                        this.mAmKeyCode = events[HOUR_INDEX].getKeyCode();
                        this.mPmKeyCode = events[AMPM_INDEX].getKeyCode();
                    }
                } else {
                    i += PM;
                }
            }
        }
        if (amOrPm == 0) {
            return this.mAmKeyCode;
        }
        if (amOrPm == PM) {
            return this.mPmKeyCode;
        }
        return -1;
    }

    private void generateLegalTimesTree() {
        this.mLegalTimesTree = new Node(new int[HOUR_INDEX]);
        if (this.mIs24HourMode) {
            Node minuteFirstDigit = new Node(7, 8, 9, 10, 11, 12);
            Node node = new Node(7, 8, 9, 10, 11, 12, 13, 14, 15, 16);
            minuteFirstDigit.addChild(node);
            int[] iArr = new int[AMPM_INDEX];
            iArr[HOUR_INDEX] = 7;
            iArr[PM] = 8;
            Node firstDigit = new Node(iArr);
            this.mLegalTimesTree.addChild(firstDigit);
            node = new Node(7, 8, 9, 10, 11, 12);
            firstDigit.addChild(node);
            node.addChild(minuteFirstDigit);
            node.addChild(new Node(13, 14, 15, 16));
            node = new Node(13, 14, 15, 16);
            firstDigit.addChild(node);
            node.addChild(minuteFirstDigit);
            iArr = new int[PM];
            iArr[HOUR_INDEX] = 9;
            firstDigit = new Node(iArr);
            this.mLegalTimesTree.addChild(firstDigit);
            node = new Node(7, 8, 9, 10);
            firstDigit.addChild(node);
            node.addChild(minuteFirstDigit);
            iArr = new int[AMPM_INDEX];
            iArr[HOUR_INDEX] = 11;
            iArr[PM] = 12;
            node = new Node(iArr);
            firstDigit.addChild(node);
            node.addChild(node);
            firstDigit = new Node(10, 11, 12, 13, 14, 15, 16);
            this.mLegalTimesTree.addChild(firstDigit);
            firstDigit.addChild(minuteFirstDigit);
            return;
        }
        iArr = new int[AMPM_INDEX];
        iArr[HOUR_INDEX] = getAmOrPmKeyCode(HOUR_INDEX);
        iArr[PM] = getAmOrPmKeyCode(PM);
        Node ampm = new Node(iArr);
        iArr = new int[PM];
        iArr[HOUR_INDEX] = 8;
        firstDigit = new Node(iArr);
        this.mLegalTimesTree.addChild(firstDigit);
        firstDigit.addChild(ampm);
        iArr = new int[ENABLE_PICKER_INDEX];
        iArr[HOUR_INDEX] = 7;
        iArr[PM] = 8;
        iArr[AMPM_INDEX] = 9;
        node = new Node(iArr);
        firstDigit.addChild(node);
        node.addChild(ampm);
        Node thirdDigit = new Node(7, 8, 9, 10, 11, 12);
        node.addChild(thirdDigit);
        thirdDigit.addChild(ampm);
        Node fourthDigit = new Node(7, 8, 9, 10, 11, 12, 13, 14, 15, 16);
        thirdDigit.addChild(fourthDigit);
        fourthDigit.addChild(ampm);
        thirdDigit = new Node(13, 14, 15, 16);
        node.addChild(thirdDigit);
        thirdDigit.addChild(ampm);
        iArr = new int[ENABLE_PICKER_INDEX];
        iArr[HOUR_INDEX] = 10;
        iArr[PM] = 11;
        iArr[AMPM_INDEX] = 12;
        node = new Node(iArr);
        firstDigit.addChild(node);
        thirdDigit = new Node(7, 8, 9, 10, 11, 12, 13, 14, 15, 16);
        node.addChild(thirdDigit);
        thirdDigit.addChild(ampm);
        firstDigit = new Node(9, 10, 11, 12, 13, 14, 15, 16);
        this.mLegalTimesTree.addChild(firstDigit);
        firstDigit.addChild(ampm);
        node = new Node(7, 8, 9, 10, 11, 12);
        firstDigit.addChild(node);
        thirdDigit = new Node(7, 8, 9, 10, 11, 12, 13, 14, 15, 16);
        node.addChild(thirdDigit);
        thirdDigit.addChild(ampm);
    }
}
