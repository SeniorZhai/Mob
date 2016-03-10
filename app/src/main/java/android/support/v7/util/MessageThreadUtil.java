package android.support.v7.util;

import android.os.Handler;
import android.os.Looper;
import android.support.v7.util.ThreadUtil.BackgroundCallback;
import android.support.v7.util.ThreadUtil.MainThreadCallback;
import android.support.v7.util.TileList.Tile;
import android.util.Log;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

class MessageThreadUtil<T> implements ThreadUtil<T> {

    static class MessageQueue {
        private SyncQueueItem mRoot;

        MessageQueue() {
        }

        synchronized SyncQueueItem next() {
            SyncQueueItem syncQueueItem;
            if (this.mRoot == null) {
                syncQueueItem = null;
            } else {
                syncQueueItem = this.mRoot;
                this.mRoot = this.mRoot.next;
            }
            return syncQueueItem;
        }

        synchronized void sendMessageAtFrontOfQueue(SyncQueueItem item) {
            item.next = this.mRoot;
            this.mRoot = item;
        }

        synchronized void sendMessage(SyncQueueItem item) {
            if (this.mRoot == null) {
                this.mRoot = item;
            } else {
                SyncQueueItem last = this.mRoot;
                while (last.next != null) {
                    last = last.next;
                }
                last.next = item;
            }
        }

        synchronized void removeMessages(int what) {
            while (this.mRoot != null && this.mRoot.what == what) {
                SyncQueueItem item = this.mRoot;
                this.mRoot = this.mRoot.next;
                item.recycle();
            }
            if (this.mRoot != null) {
                SyncQueueItem prev = this.mRoot;
                item = prev.next;
                while (item != null) {
                    SyncQueueItem next = item.next;
                    if (item.what == what) {
                        prev.next = next;
                        item.recycle();
                    } else {
                        prev = item;
                    }
                    item = next;
                }
            }
        }
    }

    static class SyncQueueItem {
        private static SyncQueueItem sPool;
        private static final Object sPoolLock = new Object();
        public int arg1;
        public int arg2;
        public int arg3;
        public int arg4;
        public int arg5;
        public Object data;
        private SyncQueueItem next;
        public int what;

        SyncQueueItem() {
        }

        void recycle() {
            this.next = null;
            this.arg5 = 0;
            this.arg4 = 0;
            this.arg3 = 0;
            this.arg2 = 0;
            this.arg1 = 0;
            this.what = 0;
            this.data = null;
            synchronized (sPoolLock) {
                if (sPool != null) {
                    this.next = sPool;
                }
                sPool = this;
            }
        }

        static SyncQueueItem obtainMessage(int what, int arg1, int arg2, int arg3, int arg4, int arg5, Object data) {
            SyncQueueItem item;
            synchronized (sPoolLock) {
                if (sPool == null) {
                    item = new SyncQueueItem();
                } else {
                    item = sPool;
                    sPool = sPool.next;
                    item.next = null;
                }
                item.what = what;
                item.arg1 = arg1;
                item.arg2 = arg2;
                item.arg3 = arg3;
                item.arg4 = arg4;
                item.arg5 = arg5;
                item.data = data;
            }
            return item;
        }

        static SyncQueueItem obtainMessage(int what, int arg1, int arg2) {
            return obtainMessage(what, arg1, arg2, 0, 0, 0, null);
        }

        static SyncQueueItem obtainMessage(int what, int arg1, Object data) {
            return obtainMessage(what, arg1, 0, 0, 0, 0, data);
        }
    }

    MessageThreadUtil() {
    }

    public MainThreadCallback<T> getMainThreadProxy(final MainThreadCallback<T> callback) {
        return new MainThreadCallback<T>() {
            private static final int ADD_TILE = 2;
            private static final int REMOVE_TILE = 3;
            private static final int UPDATE_ITEM_COUNT = 1;
            private final Handler mMainThreadHandler = new Handler(Looper.getMainLooper());
            private Runnable mMainThreadRunnable = new Runnable() {
                public void run() {
                    SyncQueueItem msg = AnonymousClass1.this.mQueue.next();
                    while (msg != null) {
                        switch (msg.what) {
                            case AnonymousClass1.UPDATE_ITEM_COUNT /*1*/:
                                callback.updateItemCount(msg.arg1, msg.arg2);
                                break;
                            case AnonymousClass1.ADD_TILE /*2*/:
                                callback.addTile(msg.arg1, (Tile) msg.data);
                                break;
                            case AnonymousClass1.REMOVE_TILE /*3*/:
                                callback.removeTile(msg.arg1, msg.arg2);
                                break;
                            default:
                                Log.e("ThreadUtil", "Unsupported message, what=" + msg.what);
                                break;
                        }
                        msg = AnonymousClass1.this.mQueue.next();
                    }
                }
            };
            private final MessageQueue mQueue = new MessageQueue();

            public void updateItemCount(int generation, int itemCount) {
                sendMessage(SyncQueueItem.obtainMessage((int) UPDATE_ITEM_COUNT, generation, itemCount));
            }

            public void addTile(int generation, Tile<T> tile) {
                sendMessage(SyncQueueItem.obtainMessage((int) ADD_TILE, generation, (Object) tile));
            }

            public void removeTile(int generation, int position) {
                sendMessage(SyncQueueItem.obtainMessage((int) REMOVE_TILE, generation, position));
            }

            private void sendMessage(SyncQueueItem msg) {
                this.mQueue.sendMessage(msg);
                this.mMainThreadHandler.post(this.mMainThreadRunnable);
            }
        };
    }

    public BackgroundCallback<T> getBackgroundProxy(final BackgroundCallback<T> callback) {
        return new BackgroundCallback<T>() {
            private static final int LOAD_TILE = 3;
            private static final int RECYCLE_TILE = 4;
            private static final int REFRESH = 1;
            private static final int UPDATE_RANGE = 2;
            private Runnable mBackgroundRunnable = new Runnable() {
                public void run() {
                    SyncQueueItem msg = AnonymousClass2.this.mQueue.next();
                    if (msg != null) {
                        switch (msg.what) {
                            case AnonymousClass2.REFRESH /*1*/:
                                AnonymousClass2.this.mQueue.removeMessages(AnonymousClass2.REFRESH);
                                callback.refresh(msg.arg1);
                                return;
                            case AnonymousClass2.UPDATE_RANGE /*2*/:
                                AnonymousClass2.this.mQueue.removeMessages(AnonymousClass2.UPDATE_RANGE);
                                AnonymousClass2.this.mQueue.removeMessages(AnonymousClass2.LOAD_TILE);
                                callback.updateRange(msg.arg1, msg.arg2, msg.arg3, msg.arg4, msg.arg5);
                                return;
                            case AnonymousClass2.LOAD_TILE /*3*/:
                                callback.loadTile(msg.arg1, msg.arg2);
                                return;
                            case AnonymousClass2.RECYCLE_TILE /*4*/:
                                callback.recycleTile((Tile) msg.data);
                                return;
                            default:
                                Log.e("ThreadUtil", "Unsupported message, what=" + msg.what);
                                return;
                        }
                    }
                }
            };
            private final Executor mExecutor = Executors.newSingleThreadExecutor();
            private final MessageQueue mQueue = new MessageQueue();

            public void refresh(int generation) {
                sendMessageAtFrontOfQueue(SyncQueueItem.obtainMessage((int) REFRESH, generation, null));
            }

            public void updateRange(int rangeStart, int rangeEnd, int extRangeStart, int extRangeEnd, int scrollHint) {
                sendMessageAtFrontOfQueue(SyncQueueItem.obtainMessage(UPDATE_RANGE, rangeStart, rangeEnd, extRangeStart, extRangeEnd, scrollHint, null));
            }

            public void loadTile(int position, int scrollHint) {
                sendMessage(SyncQueueItem.obtainMessage((int) LOAD_TILE, position, scrollHint));
            }

            public void recycleTile(Tile<T> tile) {
                sendMessage(SyncQueueItem.obtainMessage((int) RECYCLE_TILE, 0, (Object) tile));
            }

            private void sendMessage(SyncQueueItem msg) {
                this.mQueue.sendMessage(msg);
                this.mExecutor.execute(this.mBackgroundRunnable);
            }

            private void sendMessageAtFrontOfQueue(SyncQueueItem msg) {
                this.mQueue.sendMessageAtFrontOfQueue(msg);
                this.mExecutor.execute(this.mBackgroundRunnable);
            }
        };
    }
}
