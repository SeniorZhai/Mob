package com.firebase.client.core;

import com.firebase.client.FirebaseException;
import com.firebase.client.RunLoop;
import com.mobcrush.mobcrush.Constants;
import java.util.HashMap;
import java.util.Map;

public class RepoManager {
    private static final RepoManager instance = new RepoManager();
    private final Map<Context, Map<String, Repo>> repos = new HashMap();

    public static Repo getRepo(Context ctx, RepoInfo info) throws FirebaseException {
        return instance.getLocalRepo(ctx, info);
    }

    public static void interrupt(Context ctx) {
        instance.interruptInternal(ctx);
    }

    public static void interrupt(final Repo repo) {
        repo.scheduleNow(new Runnable() {
            public void run() {
                repo.interrupt();
            }
        });
    }

    public static void resume(final Repo repo) {
        repo.scheduleNow(new Runnable() {
            public void run() {
                repo.resume();
            }
        });
    }

    public static void resume(Context ctx) {
        instance.resumeInternal(ctx);
    }

    private Repo getLocalRepo(Context ctx, RepoInfo info) throws FirebaseException {
        ctx.freeze();
        String repoHash = Constants.BASE_SCHEME + info.host + "/" + info.namespace;
        synchronized (this.repos) {
            if (!this.repos.containsKey(ctx)) {
                this.repos.put(ctx, new HashMap());
            }
            Map<String, Repo> innerMap = (Map) this.repos.get(ctx);
            if (innerMap.containsKey(repoHash)) {
                Repo repo = (Repo) innerMap.get(repoHash);
                return repo;
            }
            Repo repo2 = new Repo(info, ctx);
            innerMap.put(repoHash, repo2);
            return repo2;
        }
    }

    private void interruptInternal(final Context ctx) {
        RunLoop runLoop = ctx.getRunLoop();
        if (runLoop != null) {
            runLoop.scheduleNow(new Runnable() {
                public void run() {
                    synchronized (RepoManager.this.repos) {
                        boolean allEmpty = true;
                        if (RepoManager.this.repos.containsKey(ctx)) {
                            for (Repo repo : ((Map) RepoManager.this.repos.get(ctx)).values()) {
                                repo.interrupt();
                                allEmpty = allEmpty && !repo.hasListeners();
                            }
                            if (allEmpty) {
                                ctx.stop();
                            }
                        }
                    }
                }
            });
        }
    }

    private void resumeInternal(final Context ctx) {
        RunLoop runLoop = ctx.getRunLoop();
        if (runLoop != null) {
            runLoop.scheduleNow(new Runnable() {
                public void run() {
                    synchronized (RepoManager.this.repos) {
                        if (RepoManager.this.repos.containsKey(ctx)) {
                            for (Repo repo : ((Map) RepoManager.this.repos.get(ctx)).values()) {
                                repo.resume();
                            }
                        }
                    }
                }
            });
        }
    }
}
