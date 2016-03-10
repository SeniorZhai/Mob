package android.support.v7.widget;

import android.support.v4.util.Pools.Pool;
import android.support.v4.util.Pools.SimplePool;
import android.support.v7.widget.RecyclerView.ViewHolder;
import com.google.android.gms.analytics.ecommerce.ProductAction;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;
import io.fabric.sdk.android.services.common.CommonUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

class AdapterHelper implements Callback {
    private static final boolean DEBUG = false;
    static final int POSITION_TYPE_INVISIBLE = 0;
    static final int POSITION_TYPE_NEW_OR_LAID_OUT = 1;
    private static final String TAG = "AHT";
    final Callback mCallback;
    final boolean mDisableRecycler;
    Runnable mOnItemProcessedCallback;
    final OpReorderer mOpReorderer;
    final ArrayList<UpdateOp> mPendingUpdates;
    final ArrayList<UpdateOp> mPostponedList;
    private Pool<UpdateOp> mUpdateOpPool;

    interface Callback {
        ViewHolder findViewHolder(int i);

        void markViewHoldersUpdated(int i, int i2, Object obj);

        void offsetPositionsForAdd(int i, int i2);

        void offsetPositionsForMove(int i, int i2);

        void offsetPositionsForRemovingInvisible(int i, int i2);

        void offsetPositionsForRemovingLaidOutOrNewView(int i, int i2);

        void onDispatchFirstPass(UpdateOp updateOp);

        void onDispatchSecondPass(UpdateOp updateOp);
    }

    static class UpdateOp {
        static final int ADD = 0;
        static final int MOVE = 3;
        static final int POOL_SIZE = 30;
        static final int REMOVE = 1;
        static final int UPDATE = 2;
        int cmd;
        int itemCount;
        Object payload;
        int positionStart;

        UpdateOp(int cmd, int positionStart, int itemCount, Object payload) {
            this.cmd = cmd;
            this.positionStart = positionStart;
            this.itemCount = itemCount;
            this.payload = payload;
        }

        String cmdToString() {
            switch (this.cmd) {
                case ADD /*0*/:
                    return ProductAction.ACTION_ADD;
                case REMOVE /*1*/:
                    return "rm";
                case UPDATE /*2*/:
                    return "up";
                case MOVE /*3*/:
                    return "mv";
                default:
                    return "??";
            }
        }

        public String toString() {
            return Integer.toHexString(System.identityHashCode(this)) + "[" + cmdToString() + ",s:" + this.positionStart + "c:" + this.itemCount + ",p:" + this.payload + "]";
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return AdapterHelper.DEBUG;
            }
            UpdateOp op = (UpdateOp) o;
            if (this.cmd != op.cmd) {
                return AdapterHelper.DEBUG;
            }
            if (this.cmd == MOVE && Math.abs(this.itemCount - this.positionStart) == REMOVE && this.itemCount == op.positionStart && this.positionStart == op.itemCount) {
                return true;
            }
            return this.itemCount != op.itemCount ? AdapterHelper.DEBUG : this.positionStart != op.positionStart ? AdapterHelper.DEBUG : this.payload != null ? !this.payload.equals(op.payload) ? AdapterHelper.DEBUG : true : op.payload != null ? AdapterHelper.DEBUG : true;
        }

        public int hashCode() {
            return (((this.cmd * 31) + this.positionStart) * 31) + this.itemCount;
        }
    }

    AdapterHelper(Callback callback) {
        this(callback, DEBUG);
    }

    AdapterHelper(Callback callback, boolean disableRecycler) {
        this.mUpdateOpPool = new SimplePool(30);
        this.mPendingUpdates = new ArrayList();
        this.mPostponedList = new ArrayList();
        this.mCallback = callback;
        this.mDisableRecycler = disableRecycler;
        this.mOpReorderer = new OpReorderer(this);
    }

    AdapterHelper addUpdateOp(UpdateOp... ops) {
        Collections.addAll(this.mPendingUpdates, ops);
        return this;
    }

    void reset() {
        recycleUpdateOpsAndClearList(this.mPendingUpdates);
        recycleUpdateOpsAndClearList(this.mPostponedList);
    }

    void preProcess() {
        this.mOpReorderer.reorderOps(this.mPendingUpdates);
        int count = this.mPendingUpdates.size();
        for (int i = POSITION_TYPE_INVISIBLE; i < count; i += POSITION_TYPE_NEW_OR_LAID_OUT) {
            UpdateOp op = (UpdateOp) this.mPendingUpdates.get(i);
            switch (op.cmd) {
                case POSITION_TYPE_INVISIBLE /*0*/:
                    applyAdd(op);
                    break;
                case POSITION_TYPE_NEW_OR_LAID_OUT /*1*/:
                    applyRemove(op);
                    break;
                case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
                    applyUpdate(op);
                    break;
                case TimePickerDialog.ENABLE_PICKER_INDEX /*3*/:
                    applyMove(op);
                    break;
            }
            if (this.mOnItemProcessedCallback != null) {
                this.mOnItemProcessedCallback.run();
            }
        }
        this.mPendingUpdates.clear();
    }

    void consumePostponedUpdates() {
        int count = this.mPostponedList.size();
        for (int i = POSITION_TYPE_INVISIBLE; i < count; i += POSITION_TYPE_NEW_OR_LAID_OUT) {
            this.mCallback.onDispatchSecondPass((UpdateOp) this.mPostponedList.get(i));
        }
        recycleUpdateOpsAndClearList(this.mPostponedList);
    }

    private void applyMove(UpdateOp op) {
        postponeAndUpdateViewHolders(op);
    }

    private void applyRemove(UpdateOp op) {
        int tmpStart = op.positionStart;
        int tmpCount = POSITION_TYPE_INVISIBLE;
        int tmpEnd = op.positionStart + op.itemCount;
        int type = -1;
        int position = op.positionStart;
        while (position < tmpEnd) {
            boolean typeChanged = DEBUG;
            if (this.mCallback.findViewHolder(position) != null || canFindInPreLayout(position)) {
                if (type == 0) {
                    dispatchAndUpdateViewHolders(obtainUpdateOp(POSITION_TYPE_NEW_OR_LAID_OUT, tmpStart, tmpCount, null));
                    typeChanged = true;
                }
                type = POSITION_TYPE_NEW_OR_LAID_OUT;
            } else {
                if (type == POSITION_TYPE_NEW_OR_LAID_OUT) {
                    postponeAndUpdateViewHolders(obtainUpdateOp(POSITION_TYPE_NEW_OR_LAID_OUT, tmpStart, tmpCount, null));
                    typeChanged = true;
                }
                type = POSITION_TYPE_INVISIBLE;
            }
            if (typeChanged) {
                position -= tmpCount;
                tmpEnd -= tmpCount;
                tmpCount = POSITION_TYPE_NEW_OR_LAID_OUT;
            } else {
                tmpCount += POSITION_TYPE_NEW_OR_LAID_OUT;
            }
            position += POSITION_TYPE_NEW_OR_LAID_OUT;
        }
        if (tmpCount != op.itemCount) {
            recycleUpdateOp(op);
            op = obtainUpdateOp(POSITION_TYPE_NEW_OR_LAID_OUT, tmpStart, tmpCount, null);
        }
        if (type == 0) {
            dispatchAndUpdateViewHolders(op);
        } else {
            postponeAndUpdateViewHolders(op);
        }
    }

    private void applyUpdate(UpdateOp op) {
        int tmpStart = op.positionStart;
        int tmpCount = POSITION_TYPE_INVISIBLE;
        int tmpEnd = op.positionStart + op.itemCount;
        int type = -1;
        int position = op.positionStart;
        while (position < tmpEnd) {
            if (this.mCallback.findViewHolder(position) != null || canFindInPreLayout(position)) {
                if (type == 0) {
                    dispatchAndUpdateViewHolders(obtainUpdateOp(2, tmpStart, tmpCount, op.payload));
                    tmpCount = POSITION_TYPE_INVISIBLE;
                    tmpStart = position;
                }
                type = POSITION_TYPE_NEW_OR_LAID_OUT;
            } else {
                if (type == POSITION_TYPE_NEW_OR_LAID_OUT) {
                    postponeAndUpdateViewHolders(obtainUpdateOp(2, tmpStart, tmpCount, op.payload));
                    tmpCount = POSITION_TYPE_INVISIBLE;
                    tmpStart = position;
                }
                type = POSITION_TYPE_INVISIBLE;
            }
            tmpCount += POSITION_TYPE_NEW_OR_LAID_OUT;
            position += POSITION_TYPE_NEW_OR_LAID_OUT;
        }
        if (tmpCount != op.itemCount) {
            Object payload = op.payload;
            recycleUpdateOp(op);
            op = obtainUpdateOp(2, tmpStart, tmpCount, payload);
        }
        if (type == 0) {
            dispatchAndUpdateViewHolders(op);
        } else {
            postponeAndUpdateViewHolders(op);
        }
    }

    private void dispatchAndUpdateViewHolders(UpdateOp op) {
        if (op.cmd == 0 || op.cmd == 3) {
            throw new IllegalArgumentException("should not dispatch add or move for pre layout");
        }
        int positionMultiplier;
        int tmpStart = updatePositionWithPostponed(op.positionStart, op.cmd);
        int tmpCnt = POSITION_TYPE_NEW_OR_LAID_OUT;
        int offsetPositionForPartial = op.positionStart;
        switch (op.cmd) {
            case POSITION_TYPE_NEW_OR_LAID_OUT /*1*/:
                positionMultiplier = POSITION_TYPE_INVISIBLE;
                break;
            case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
                positionMultiplier = POSITION_TYPE_NEW_OR_LAID_OUT;
                break;
            default:
                throw new IllegalArgumentException("op should be remove or update." + op);
        }
        for (int p = POSITION_TYPE_NEW_OR_LAID_OUT; p < op.itemCount; p += POSITION_TYPE_NEW_OR_LAID_OUT) {
            int updatedPos = updatePositionWithPostponed(op.positionStart + (positionMultiplier * p), op.cmd);
            boolean continuous = DEBUG;
            switch (op.cmd) {
                case POSITION_TYPE_NEW_OR_LAID_OUT /*1*/:
                    continuous = updatedPos == tmpStart ? true : DEBUG;
                    break;
                case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
                    continuous = updatedPos == tmpStart + POSITION_TYPE_NEW_OR_LAID_OUT ? true : DEBUG;
                    break;
            }
            if (continuous) {
                tmpCnt += POSITION_TYPE_NEW_OR_LAID_OUT;
            } else {
                UpdateOp tmp = obtainUpdateOp(op.cmd, tmpStart, tmpCnt, op.payload);
                dispatchFirstPassAndUpdateViewHolders(tmp, offsetPositionForPartial);
                recycleUpdateOp(tmp);
                if (op.cmd == 2) {
                    offsetPositionForPartial += tmpCnt;
                }
                tmpStart = updatedPos;
                tmpCnt = POSITION_TYPE_NEW_OR_LAID_OUT;
            }
        }
        Object payload = op.payload;
        recycleUpdateOp(op);
        if (tmpCnt > 0) {
            tmp = obtainUpdateOp(op.cmd, tmpStart, tmpCnt, payload);
            dispatchFirstPassAndUpdateViewHolders(tmp, offsetPositionForPartial);
            recycleUpdateOp(tmp);
        }
    }

    void dispatchFirstPassAndUpdateViewHolders(UpdateOp op, int offsetStart) {
        this.mCallback.onDispatchFirstPass(op);
        switch (op.cmd) {
            case POSITION_TYPE_NEW_OR_LAID_OUT /*1*/:
                this.mCallback.offsetPositionsForRemovingInvisible(offsetStart, op.itemCount);
                return;
            case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
                this.mCallback.markViewHoldersUpdated(offsetStart, op.itemCount, op.payload);
                return;
            default:
                throw new IllegalArgumentException("only remove and update ops can be dispatched in first pass");
        }
    }

    private int updatePositionWithPostponed(int pos, int cmd) {
        int i;
        for (i = this.mPostponedList.size() - 1; i >= 0; i--) {
            UpdateOp postponed = (UpdateOp) this.mPostponedList.get(i);
            if (postponed.cmd == 3) {
                int start;
                int end;
                if (postponed.positionStart < postponed.itemCount) {
                    start = postponed.positionStart;
                    end = postponed.itemCount;
                } else {
                    start = postponed.itemCount;
                    end = postponed.positionStart;
                }
                if (pos < start || pos > end) {
                    if (pos < postponed.positionStart) {
                        if (cmd == 0) {
                            postponed.positionStart += POSITION_TYPE_NEW_OR_LAID_OUT;
                            postponed.itemCount += POSITION_TYPE_NEW_OR_LAID_OUT;
                        } else if (cmd == POSITION_TYPE_NEW_OR_LAID_OUT) {
                            postponed.positionStart--;
                            postponed.itemCount--;
                        }
                    }
                } else if (start == postponed.positionStart) {
                    if (cmd == 0) {
                        postponed.itemCount += POSITION_TYPE_NEW_OR_LAID_OUT;
                    } else if (cmd == POSITION_TYPE_NEW_OR_LAID_OUT) {
                        postponed.itemCount--;
                    }
                    pos += POSITION_TYPE_NEW_OR_LAID_OUT;
                } else {
                    if (cmd == 0) {
                        postponed.positionStart += POSITION_TYPE_NEW_OR_LAID_OUT;
                    } else if (cmd == POSITION_TYPE_NEW_OR_LAID_OUT) {
                        postponed.positionStart--;
                    }
                    pos--;
                }
            } else if (postponed.positionStart <= pos) {
                if (postponed.cmd == 0) {
                    pos -= postponed.itemCount;
                } else if (postponed.cmd == POSITION_TYPE_NEW_OR_LAID_OUT) {
                    pos += postponed.itemCount;
                }
            } else if (cmd == 0) {
                postponed.positionStart += POSITION_TYPE_NEW_OR_LAID_OUT;
            } else if (cmd == POSITION_TYPE_NEW_OR_LAID_OUT) {
                postponed.positionStart--;
            }
        }
        for (i = this.mPostponedList.size() - 1; i >= 0; i--) {
            UpdateOp op = (UpdateOp) this.mPostponedList.get(i);
            if (op.cmd == 3) {
                if (op.itemCount == op.positionStart || op.itemCount < 0) {
                    this.mPostponedList.remove(i);
                    recycleUpdateOp(op);
                }
            } else if (op.itemCount <= 0) {
                this.mPostponedList.remove(i);
                recycleUpdateOp(op);
            }
        }
        return pos;
    }

    private boolean canFindInPreLayout(int position) {
        int count = this.mPostponedList.size();
        for (int i = POSITION_TYPE_INVISIBLE; i < count; i += POSITION_TYPE_NEW_OR_LAID_OUT) {
            UpdateOp op = (UpdateOp) this.mPostponedList.get(i);
            if (op.cmd == 3) {
                if (findPositionOffset(op.itemCount, i + POSITION_TYPE_NEW_OR_LAID_OUT) == position) {
                    return true;
                }
            } else if (op.cmd == 0) {
                int end = op.positionStart + op.itemCount;
                for (int pos = op.positionStart; pos < end; pos += POSITION_TYPE_NEW_OR_LAID_OUT) {
                    if (findPositionOffset(pos, i + POSITION_TYPE_NEW_OR_LAID_OUT) == position) {
                        return true;
                    }
                }
                continue;
            } else {
                continue;
            }
        }
        return DEBUG;
    }

    private void applyAdd(UpdateOp op) {
        postponeAndUpdateViewHolders(op);
    }

    private void postponeAndUpdateViewHolders(UpdateOp op) {
        this.mPostponedList.add(op);
        switch (op.cmd) {
            case POSITION_TYPE_INVISIBLE /*0*/:
                this.mCallback.offsetPositionsForAdd(op.positionStart, op.itemCount);
                return;
            case POSITION_TYPE_NEW_OR_LAID_OUT /*1*/:
                this.mCallback.offsetPositionsForRemovingLaidOutOrNewView(op.positionStart, op.itemCount);
                return;
            case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
                this.mCallback.markViewHoldersUpdated(op.positionStart, op.itemCount, op.payload);
                return;
            case TimePickerDialog.ENABLE_PICKER_INDEX /*3*/:
                this.mCallback.offsetPositionsForMove(op.positionStart, op.itemCount);
                return;
            default:
                throw new IllegalArgumentException("Unknown update op type for " + op);
        }
    }

    boolean hasPendingUpdates() {
        return this.mPendingUpdates.size() > 0 ? true : DEBUG;
    }

    int findPositionOffset(int position) {
        return findPositionOffset(position, POSITION_TYPE_INVISIBLE);
    }

    int findPositionOffset(int position, int firstPostponedItem) {
        int count = this.mPostponedList.size();
        for (int i = firstPostponedItem; i < count; i += POSITION_TYPE_NEW_OR_LAID_OUT) {
            UpdateOp op = (UpdateOp) this.mPostponedList.get(i);
            if (op.cmd == 3) {
                if (op.positionStart == position) {
                    position = op.itemCount;
                } else {
                    if (op.positionStart < position) {
                        position--;
                    }
                    if (op.itemCount <= position) {
                        position += POSITION_TYPE_NEW_OR_LAID_OUT;
                    }
                }
            } else if (op.positionStart > position) {
                continue;
            } else if (op.cmd == POSITION_TYPE_NEW_OR_LAID_OUT) {
                if (position < op.positionStart + op.itemCount) {
                    return -1;
                }
                position -= op.itemCount;
            } else if (op.cmd == 0) {
                position += op.itemCount;
            }
        }
        return position;
    }

    boolean onItemRangeChanged(int positionStart, int itemCount, Object payload) {
        this.mPendingUpdates.add(obtainUpdateOp(2, positionStart, itemCount, payload));
        if (this.mPendingUpdates.size() == POSITION_TYPE_NEW_OR_LAID_OUT) {
            return true;
        }
        return DEBUG;
    }

    boolean onItemRangeInserted(int positionStart, int itemCount) {
        this.mPendingUpdates.add(obtainUpdateOp(POSITION_TYPE_INVISIBLE, positionStart, itemCount, null));
        return this.mPendingUpdates.size() == POSITION_TYPE_NEW_OR_LAID_OUT ? true : DEBUG;
    }

    boolean onItemRangeRemoved(int positionStart, int itemCount) {
        this.mPendingUpdates.add(obtainUpdateOp(POSITION_TYPE_NEW_OR_LAID_OUT, positionStart, itemCount, null));
        if (this.mPendingUpdates.size() == POSITION_TYPE_NEW_OR_LAID_OUT) {
            return true;
        }
        return DEBUG;
    }

    boolean onItemRangeMoved(int from, int to, int itemCount) {
        boolean z = true;
        if (from == to) {
            return DEBUG;
        }
        if (itemCount != POSITION_TYPE_NEW_OR_LAID_OUT) {
            throw new IllegalArgumentException("Moving more than 1 item is not supported yet");
        }
        this.mPendingUpdates.add(obtainUpdateOp(3, from, to, null));
        if (this.mPendingUpdates.size() != POSITION_TYPE_NEW_OR_LAID_OUT) {
            z = DEBUG;
        }
        return z;
    }

    void consumeUpdatesInOnePass() {
        consumePostponedUpdates();
        int count = this.mPendingUpdates.size();
        for (int i = POSITION_TYPE_INVISIBLE; i < count; i += POSITION_TYPE_NEW_OR_LAID_OUT) {
            UpdateOp op = (UpdateOp) this.mPendingUpdates.get(i);
            switch (op.cmd) {
                case POSITION_TYPE_INVISIBLE /*0*/:
                    this.mCallback.onDispatchSecondPass(op);
                    this.mCallback.offsetPositionsForAdd(op.positionStart, op.itemCount);
                    break;
                case POSITION_TYPE_NEW_OR_LAID_OUT /*1*/:
                    this.mCallback.onDispatchSecondPass(op);
                    this.mCallback.offsetPositionsForRemovingInvisible(op.positionStart, op.itemCount);
                    break;
                case CommonUtils.DEVICE_STATE_JAILBROKEN /*2*/:
                    this.mCallback.onDispatchSecondPass(op);
                    this.mCallback.markViewHoldersUpdated(op.positionStart, op.itemCount, op.payload);
                    break;
                case TimePickerDialog.ENABLE_PICKER_INDEX /*3*/:
                    this.mCallback.onDispatchSecondPass(op);
                    this.mCallback.offsetPositionsForMove(op.positionStart, op.itemCount);
                    break;
            }
            if (this.mOnItemProcessedCallback != null) {
                this.mOnItemProcessedCallback.run();
            }
        }
        recycleUpdateOpsAndClearList(this.mPendingUpdates);
    }

    public int applyPendingUpdatesToPosition(int position) {
        int size = this.mPendingUpdates.size();
        for (int i = POSITION_TYPE_INVISIBLE; i < size; i += POSITION_TYPE_NEW_OR_LAID_OUT) {
            UpdateOp op = (UpdateOp) this.mPendingUpdates.get(i);
            switch (op.cmd) {
                case POSITION_TYPE_INVISIBLE /*0*/:
                    if (op.positionStart > position) {
                        break;
                    }
                    position += op.itemCount;
                    break;
                case POSITION_TYPE_NEW_OR_LAID_OUT /*1*/:
                    if (op.positionStart <= position) {
                        if (op.positionStart + op.itemCount <= position) {
                            position -= op.itemCount;
                            break;
                        }
                        return -1;
                    }
                    continue;
                case TimePickerDialog.ENABLE_PICKER_INDEX /*3*/:
                    if (op.positionStart != position) {
                        if (op.positionStart < position) {
                            position--;
                        }
                        if (op.itemCount > position) {
                            break;
                        }
                        position += POSITION_TYPE_NEW_OR_LAID_OUT;
                        break;
                    }
                    position = op.itemCount;
                    break;
                default:
                    break;
            }
        }
        return position;
    }

    public UpdateOp obtainUpdateOp(int cmd, int positionStart, int itemCount, Object payload) {
        UpdateOp op = (UpdateOp) this.mUpdateOpPool.acquire();
        if (op == null) {
            return new UpdateOp(cmd, positionStart, itemCount, payload);
        }
        op.cmd = cmd;
        op.positionStart = positionStart;
        op.itemCount = itemCount;
        op.payload = payload;
        return op;
    }

    public void recycleUpdateOp(UpdateOp op) {
        if (!this.mDisableRecycler) {
            op.payload = null;
            this.mUpdateOpPool.release(op);
        }
    }

    void recycleUpdateOpsAndClearList(List<UpdateOp> ops) {
        int count = ops.size();
        for (int i = POSITION_TYPE_INVISIBLE; i < count; i += POSITION_TYPE_NEW_OR_LAID_OUT) {
            recycleUpdateOp((UpdateOp) ops.get(i));
        }
        ops.clear();
    }
}
