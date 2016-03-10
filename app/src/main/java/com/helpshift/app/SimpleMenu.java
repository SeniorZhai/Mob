package com.helpshift.app;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import java.util.ArrayList;

public class SimpleMenu implements Menu {
    private ArrayList<SimpleMenuItem> mMenuItems = new ArrayList();
    private Resources mResources;

    public SimpleMenu(Context context) {
        this.mResources = context.getResources();
    }

    public Resources getResources() {
        return this.mResources;
    }

    private MenuItem addMenuItemToMenu(int itemId, int order, CharSequence title) {
        SimpleMenuItem menuItem = new SimpleMenuItem(this, itemId, order, title);
        this.mMenuItems.add(findInsertIndex(this.mMenuItems, order), menuItem);
        return menuItem;
    }

    private static int findInsertIndex(ArrayList<SimpleMenuItem> items, int order) {
        for (int i = items.size() - 1; i >= 0; i--) {
            if (((MenuItem) items.get(i)).getOrder() <= order) {
                return i + 1;
            }
        }
        return 0;
    }

    public MenuItem add(CharSequence title) {
        return addMenuItemToMenu(0, 0, title);
    }

    public MenuItem add(int titleResId) {
        return addMenuItemToMenu(0, 0, getResources().getString(titleResId));
    }

    public MenuItem add(int groupId, int itemId, int order, CharSequence title) {
        return addMenuItemToMenu(itemId, order, title);
    }

    public MenuItem add(int groupId, int itemId, int order, int titleResId) {
        return addMenuItemToMenu(itemId, order, getResources().getString(titleResId));
    }

    public SubMenu addSubMenu(CharSequence charSequence) {
        return null;
    }

    public SubMenu addSubMenu(int i) {
        throw new UnsupportedOperationException("This operation is not supported for SimpleMenu");
    }

    public SubMenu addSubMenu(int i, int i2, int i3, CharSequence charSequence) {
        throw new UnsupportedOperationException("This operation is not supported for SimpleMenu");
    }

    public SubMenu addSubMenu(int i, int i2, int i3, int i4) {
        throw new UnsupportedOperationException("This operation is not supported for SimpleMenu");
    }

    public int addIntentOptions(int i, int i2, int i3, ComponentName componentName, Intent[] intents, Intent intent, int i4, MenuItem[] menuItems) {
        throw new UnsupportedOperationException("This operation is not supported for SimpleMenu");
    }

    public int findItemIndex(int id) {
        int size = size();
        for (int i = 0; i < size; i++) {
            if (((SimpleMenuItem) this.mMenuItems.get(i)).getItemId() == id) {
                return i;
            }
        }
        return -1;
    }

    public void removeItem(int itemId) {
        removeItemAtInt(findItemIndex(itemId));
    }

    private void removeItemAtInt(int index) {
        if (index >= 0 && index < this.mMenuItems.size()) {
            this.mMenuItems.remove(index);
        }
    }

    public void removeGroup(int i) {
        throw new UnsupportedOperationException("This operation is not supported for SimpleMenu");
    }

    public void clear() {
        this.mMenuItems.clear();
    }

    public void setGroupCheckable(int i, boolean b, boolean b2) {
        throw new UnsupportedOperationException("This operation is not supported for SimpleMenu");
    }

    public void setGroupVisible(int i, boolean b) {
        throw new UnsupportedOperationException("This operation is not supported for SimpleMenu");
    }

    public void setGroupEnabled(int i, boolean b) {
        throw new UnsupportedOperationException("This operation is not supported for SimpleMenu");
    }

    public boolean hasVisibleItems() {
        throw new UnsupportedOperationException("This operation is not supported for SimpleMenu");
    }

    public MenuItem findItem(int itemId) {
        int size = size();
        for (int i = 0; i < size; i++) {
            SimpleMenuItem item = (SimpleMenuItem) this.mMenuItems.get(i);
            if (item.getItemId() == itemId) {
                return item;
            }
        }
        return null;
    }

    public int size() {
        return this.mMenuItems.size();
    }

    public MenuItem getItem(int index) {
        return (MenuItem) this.mMenuItems.get(index);
    }

    public void close() {
        throw new UnsupportedOperationException("This operation is not supported for SimpleMenu");
    }

    public boolean performShortcut(int i, KeyEvent keyEvent, int i2) {
        throw new UnsupportedOperationException("This operation is not supported for SimpleMenu");
    }

    public boolean isShortcutKey(int i, KeyEvent keyEvent) {
        throw new UnsupportedOperationException("This operation is not supported for SimpleMenu");
    }

    public boolean performIdentifierAction(int i, int i2) {
        throw new UnsupportedOperationException("This operation is not supported for SimpleMenu");
    }

    public void setQwertyMode(boolean b) {
        throw new UnsupportedOperationException("This operation is not supported for SimpleMenu");
    }
}
