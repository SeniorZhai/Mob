package com.wdullaer.materialdatetimepicker.date;

import com.wdullaer.materialdatetimepicker.date.DatePickerDialog.OnDateChangedListener;
import com.wdullaer.materialdatetimepicker.date.MonthAdapter.CalendarDay;
import java.util.Calendar;

public interface DatePickerController {
    int getFirstDayOfWeek();

    Calendar[] getHighlightedDays();

    Calendar getMaxDate();

    int getMaxYear();

    Calendar getMinDate();

    int getMinYear();

    Calendar[] getSelectableDays();

    CalendarDay getSelectedDay();

    boolean isThemeDark();

    void onDayOfMonthSelected(int i, int i2, int i3);

    void onYearSelected(int i);

    void registerOnDateChangedListener(OnDateChangedListener onDateChangedListener);

    void tryVibrate();

    void unregisterOnDateChangedListener(OnDateChangedListener onDateChangedListener);
}
