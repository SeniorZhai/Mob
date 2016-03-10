package com.firebase.client;

import com.firebase.client.core.Repo;
import com.firebase.client.core.RepoManager;

public class FirebaseApp {
    private final Repo repo;

    protected FirebaseApp(Repo repo) {
        this.repo = repo;
    }

    public void purgeOutstandingWrites() {
        this.repo.scheduleNow(new Runnable() {
            public void run() {
                FirebaseApp.this.repo.purgeOutstandingWrites();
            }
        });
    }

    public void goOnline() {
        RepoManager.resume(this.repo);
    }

    public void goOffline() {
        RepoManager.interrupt(this.repo);
    }
}
