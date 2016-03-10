package com.google.android.gms.common.api;

import android.os.Looper;
import com.google.android.gms.common.api.AbstractPendingResult.CallbackHandler;
import com.google.android.gms.common.api.PendingResult.BatchCallback;
import java.util.ArrayList;
import java.util.List;

public final class Batch extends AbstractPendingResult<BatchResult> {
    private int zzWn;
    private boolean zzWo;
    private boolean zzWp;
    private final PendingResult<?>[] zzWq;
    private final Object zzqt;

    public static final class Builder {
        private List<PendingResult<?>> zzWs = new ArrayList();
        private Looper zzWt;

        public Builder(GoogleApiClient googleApiClient) {
            this.zzWt = googleApiClient.getLooper();
        }

        public <R extends Result> BatchResultToken<R> add(PendingResult<R> pendingResult) {
            BatchResultToken<R> batchResultToken = new BatchResultToken(this.zzWs.size());
            this.zzWs.add(pendingResult);
            return batchResultToken;
        }

        public Batch build() {
            return new Batch(this.zzWs, this.zzWt);
        }
    }

    private Batch(List<PendingResult<?>> pendingResultList, Looper looper) {
        super(new CallbackHandler(looper));
        this.zzqt = new Object();
        this.zzWn = pendingResultList.size();
        this.zzWq = new PendingResult[this.zzWn];
        for (int i = 0; i < pendingResultList.size(); i++) {
            PendingResult pendingResult = (PendingResult) pendingResultList.get(i);
            this.zzWq[i] = pendingResult;
            pendingResult.addBatchCallback(new BatchCallback(this) {
                final /* synthetic */ Batch zzWr;

                {
                    this.zzWr = r1;
                }

                /* JADX WARNING: inconsistent code. */
                /* Code decompiled incorrectly, please refer to instructions dump. */
                public void zzs(com.google.android.gms.common.api.Status r6) {
                    /*
                    r5 = this;
                    r0 = r5.zzWr;
                    r1 = r0.zzqt;
                    monitor-enter(r1);
                    r0 = r5.zzWr;	 Catch:{ all -> 0x0039 }
                    r0 = r0.isCanceled();	 Catch:{ all -> 0x0039 }
                    if (r0 == 0) goto L_0x0011;
                L_0x000f:
                    monitor-exit(r1);	 Catch:{ all -> 0x0039 }
                L_0x0010:
                    return;
                L_0x0011:
                    r0 = r6.isCanceled();	 Catch:{ all -> 0x0039 }
                    if (r0 == 0) goto L_0x003c;
                L_0x0017:
                    r0 = r5.zzWr;	 Catch:{ all -> 0x0039 }
                    r2 = 1;
                    r0.zzWp = r2;	 Catch:{ all -> 0x0039 }
                L_0x001d:
                    r0 = r5.zzWr;	 Catch:{ all -> 0x0039 }
                    r0.zzWn = r0.zzWn - 1;	 Catch:{ all -> 0x0039 }
                    r0 = r5.zzWr;	 Catch:{ all -> 0x0039 }
                    r0 = r0.zzWn;	 Catch:{ all -> 0x0039 }
                    if (r0 != 0) goto L_0x0037;
                L_0x002a:
                    r0 = r5.zzWr;	 Catch:{ all -> 0x0039 }
                    r0 = r0.zzWp;	 Catch:{ all -> 0x0039 }
                    if (r0 == 0) goto L_0x0049;
                L_0x0032:
                    r0 = r5.zzWr;	 Catch:{ all -> 0x0039 }
                    super.cancel();	 Catch:{ all -> 0x0039 }
                L_0x0037:
                    monitor-exit(r1);	 Catch:{ all -> 0x0039 }
                    goto L_0x0010;
                L_0x0039:
                    r0 = move-exception;
                    monitor-exit(r1);	 Catch:{ all -> 0x0039 }
                    throw r0;
                L_0x003c:
                    r0 = r6.isSuccess();	 Catch:{ all -> 0x0039 }
                    if (r0 != 0) goto L_0x001d;
                L_0x0042:
                    r0 = r5.zzWr;	 Catch:{ all -> 0x0039 }
                    r2 = 1;
                    r0.zzWo = r2;	 Catch:{ all -> 0x0039 }
                    goto L_0x001d;
                L_0x0049:
                    r0 = r5.zzWr;	 Catch:{ all -> 0x0039 }
                    r0 = r0.zzWo;	 Catch:{ all -> 0x0039 }
                    if (r0 == 0) goto L_0x0069;
                L_0x0051:
                    r0 = new com.google.android.gms.common.api.Status;	 Catch:{ all -> 0x0039 }
                    r2 = 13;
                    r0.<init>(r2);	 Catch:{ all -> 0x0039 }
                L_0x0058:
                    r2 = r5.zzWr;	 Catch:{ all -> 0x0039 }
                    r3 = new com.google.android.gms.common.api.BatchResult;	 Catch:{ all -> 0x0039 }
                    r4 = r5.zzWr;	 Catch:{ all -> 0x0039 }
                    r4 = r4.zzWq;	 Catch:{ all -> 0x0039 }
                    r3.<init>(r0, r4);	 Catch:{ all -> 0x0039 }
                    r2.setResult(r3);	 Catch:{ all -> 0x0039 }
                    goto L_0x0037;
                L_0x0069:
                    r0 = com.google.android.gms.common.api.Status.zzXP;	 Catch:{ all -> 0x0039 }
                    goto L_0x0058;
                    */
                    throw new UnsupportedOperationException("Method not decompiled: com.google.android.gms.common.api.Batch.1.zzs(com.google.android.gms.common.api.Status):void");
                }
            });
        }
    }

    public void cancel() {
        super.cancel();
        for (PendingResult cancel : this.zzWq) {
            cancel.cancel();
        }
    }

    public BatchResult createFailedResult(Status status) {
        return new BatchResult(status, this.zzWq);
    }
}
