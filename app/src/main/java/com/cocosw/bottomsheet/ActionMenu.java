package com.cocosw.bottomsheet;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.support.v4.internal.view.SupportMenu;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.SubMenu;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class ActionMenu implements SupportMenu {
    private static final int[] sCategoryToOrder = new int[]{1, 4, 5, 3, 2, 0};
    private Context mContext;
    private boolean mIsQwerty;
    private ArrayList<ActionMenuItem> mItems = new ArrayList();

    public ActionMenu(Context context) {
        this.mContext = context;
    }

    public Context getContext() {
        return this.mContext;
    }

    public MenuItem add(CharSequence title) {
        return add(0, 0, 0, title);
    }

    public MenuItem add(int titleRes) {
        return add(0, 0, 0, titleRes);
    }

    public MenuItem add(int groupId, int itemId, int order, int titleRes) {
        return add(groupId, itemId, order, this.mContext.getResources().getString(titleRes));
    }

    public MenuItem add(int groupId, int itemId, int order, CharSequence title) {
        ActionMenuItem item = new ActionMenuItem(getContext(), groupId, itemId, 0, order, title);
        this.mItems.add(findInsertIndex(this.mItems, getOrdering(order)), item);
        return item;
    }

    private static int findInsertIndex(ArrayList<ActionMenuItem> items, int ordering) {
        for (int i = items.size() - 1; i >= 0; i--) {
            if (((ActionMenuItem) items.get(i)).getOrder() <= ordering) {
                return i + 1;
            }
        }
        return 0;
    }

    MenuItem add(ActionMenuItem item) {
        this.mItems.add(findInsertIndex(this.mItems, getOrdering(item.getOrder())), item);
        return item;
    }

    private static int getOrdering(int categoryOrder) {
        int index = (SupportMenu.CATEGORY_MASK & categoryOrder) >> 16;
        if (index >= 0 && index < sCategoryToOrder.length) {
            return (sCategoryToOrder[index] << 16) | (SupportMenu.USER_MASK & categoryOrder);
        }
        throw new IllegalArgumentException("order does not contain a valid category.");
    }

    public int addIntentOptions(int groupId, int itemId, int order, ComponentName caller, Intent[] specifics, Intent intent, int flags, MenuItem[] outSpecificItems) {
        PackageManager pm = this.mContext.getPackageManager();
        List<ResolveInfo> lri = pm.queryIntentActivityOptions(caller, specifics, intent, 0);
        int N = lri != null ? lri.size() : 0;
        if ((flags & 1) == 0) {
            removeGroup(groupId);
        }
        for (int i = 0; i < N; i++) {
            Intent intent2;
            ResolveInfo ri = (ResolveInfo) lri.get(i);
            if (ri.specificIndex < 0) {
                intent2 = intent;
            } else {
                intent2 = specifics[ri.specificIndex];
            }
            Intent rintent = new Intent(intent2);
            rintent.setComponent(new ComponentName(ri.activityInfo.applicationInfo.packageName, ri.activityInfo.name));
            MenuItem item = add(groupId, itemId, order, ri.loadLabel(pm)).setIcon(ri.loadIcon(pm)).setIntent(rintent);
            if (outSpecificItems != null && ri.specificIndex >= 0) {
                outSpecificItems[ri.specificIndex] = item;
            }
        }
        return N;
    }

    public SubMenu addSubMenu(CharSequence title) {
        return null;
    }

    public SubMenu addSubMenu(int titleRes) {
        return null;
    }

    public SubMenu addSubMenu(int groupId, int itemId, int order, CharSequence title) {
        return null;
    }

    public SubMenu addSubMenu(int groupId, int itemId, int order, int titleRes) {
        return null;
    }

    public void clear() {
        this.mItems.clear();
    }

    public void close() {
    }

    private int findItemIndex(int id) {
        ArrayList<ActionMenuItem> items = this.mItems;
        int itemCount = items.size();
        for (int i = 0; i < itemCount; i++) {
            if (((ActionMenuItem) items.get(i)).getItemId() == id) {
                return i;
            }
        }
        return -1;
    }

    public MenuItem findItem(int id) {
        return (MenuItem) this.mItems.get(findItemIndex(id));
    }

    public MenuItem getItem(int index) {
        return (MenuItem) this.mItems.get(index);
    }

    public boolean hasVisibleItems() {
        ArrayList<ActionMenuItem> items = this.mItems;
        int itemCount = items.size();
        for (int i = 0; i < itemCount; i++) {
            if (((ActionMenuItem) items.get(i)).isVisible()) {
                return true;
            }
        }
        return false;
    }

    private ActionMenuItem findItemWithShortcut(int keyCode, KeyEvent event) {
        boolean qwerty = this.mIsQwerty;
        ArrayList<ActionMenuItem> items = this.mItems;
        int itemCount = items.size();
        for (int i = 0; i < itemCount; i++) {
            ActionMenuItem item = (ActionMenuItem) items.get(i);
            if (keyCode == (qwerty ? item.getAlphabeticShortcut() : item.getNumericShortcut())) {
                return item;
            }
        }
        return null;
    }

    public boolean isShortcutKey(int keyCode, KeyEvent event) {
        return findItemWithShortcut(keyCode, event) != null;
    }

    public boolean performIdentifierAction(int id, int flags) {
        int index = findItemIndex(id);
        if (index < 0) {
            return false;
        }
        return ((ActionMenuItem) this.mItems.get(index)).invoke();
    }

    public boolean performShortcut(int keyCode, KeyEvent event, int flags) {
        ActionMenuItem item = findItemWithShortcut(keyCode, event);
        if (item == null) {
            return false;
        }
        return item.invoke();
    }

    public void removeGroup(int groupId) {
        ArrayList<ActionMenuItem> items = this.mItems;
        int itemCount = items.size();
        int i = 0;
        while (i < itemCount) {
            if (((ActionMenuItem) items.get(i)).getGroupId() == groupId) {
                items.remove(i);
                itemCount--;
            } else {
                i++;
            }
        }
    }

    public void removeItem(int id) {
        this.mItems.remove(findItemIndex(id));
    }

    public void setGroupCheckable(int group, boolean checkable, boolean exclusive) {
        ArrayList<ActionMenuItem> items = this.mItems;
        int itemCount = items.size();
        for (int i = 0; i < itemCount; i++) {
            ActionMenuItem item = (ActionMenuItem) items.get(i);
            if (item.getGroupId() == group) {
                item.setCheckable(checkable);
                item.setExclusiveCheckable(exclusive);
            }
        }
    }

    public void setGroupEnabled(int group, boolean enabled) {
        ArrayList<ActionMenuItem> items = this.mItems;
        int itemCount = items.size();
        for (int i = 0; i < itemCount; i++) {
            ActionMenuItem item = (ActionMenuItem) items.get(i);
            if (item.getGroupId() == group) {
                item.setEnabled(enabled);
            }
        }
    }

    public void setGroupVisible(int group, boolean visible) {
        ArrayList<ActionMenuItem> items = this.mItems;
        int itemCount = items.size();
        for (int i = 0; i < itemCount; i++) {
            ActionMenuItem item = (ActionMenuItem) items.get(i);
            if (item.getGroupId() == group) {
                item.setVisible(visible);
            }
        }
    }

    public void setQwertyMode(boolean isQwerty) {
        this.mIsQwerty = isQwerty;
    }

    public int size() {
        return this.mItems.size();
    }

    ActionMenu clone(int size) {
        ActionMenu out = new ActionMenu(getContext());
        out.mItems = new ArrayList(this.mItems.subList(0, size));
        return out;
    }

    void removeInvisible() {
        Iterator<ActionMenuItem> iter = this.mItems.iterator();
        while (iter.hasNext()) {
            if (!((ActionMenuItem) iter.next()).isVisible()) {
                iter.remove();
            }
        }
    }
}
