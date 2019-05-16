package io.objectbox.sync;

import io.objectbox.BoxStore;
import io.objectbox.sync.server.SyncServerBuilder;

/**
 * Start building a sync client using Sync.{@link #client(BoxStore, String)}
 * or a server using Sync.{@link #server(BoxStore, String, SyncCredentials)}.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public abstract class Sync {

    public static SyncBuilder client(BoxStore boxStore, String url) {
        return new SyncBuilder(boxStore, url);
    }

    public static SyncServerBuilder server(BoxStore boxStore, String url, SyncCredentials authenticatorCredentials) {
        return new SyncServerBuilder(boxStore, url, authenticatorCredentials);
    }

    private Sync() {
    }
}
