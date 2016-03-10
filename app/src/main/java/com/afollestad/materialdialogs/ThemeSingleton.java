package com.afollestad.materialdialogs;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;

public class ThemeSingleton {
    private static ThemeSingleton singleton;
    @ColorInt
    public int backgroundColor = 0;
    @DrawableRes
    public int btnSelectorNegative = 0;
    @DrawableRes
    public int btnSelectorNeutral = 0;
    @DrawableRes
    public int btnSelectorPositive = 0;
    @DrawableRes
    public int btnSelectorStacked = 0;
    public GravityEnum btnStackedGravity = GravityEnum.END;
    public GravityEnum buttonsGravity = GravityEnum.START;
    @ColorInt
    public int contentColor = 0;
    public GravityEnum contentGravity = GravityEnum.START;
    public boolean darkTheme = false;
    @ColorInt
    public int dividerColor = 0;
    public Drawable icon = null;
    @ColorInt
    public int itemColor = 0;
    public GravityEnum itemsGravity = GravityEnum.START;
    @DrawableRes
    public int listSelector = 0;
    @ColorInt
    public ColorStateList negativeColor = null;
    @ColorInt
    public ColorStateList neutralColor = null;
    @ColorInt
    public ColorStateList positiveColor = null;
    @ColorInt
    public int titleColor = 0;
    public GravityEnum titleGravity = GravityEnum.START;
    @ColorInt
    public int widgetColor = 0;

    public static ThemeSingleton get(boolean createIfNull) {
        if (singleton == null && createIfNull) {
            singleton = new ThemeSingleton();
        }
        return singleton;
    }

    public static ThemeSingleton get() {
        return get(true);
    }
}
