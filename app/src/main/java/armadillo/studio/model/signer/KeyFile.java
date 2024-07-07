/*
 * Copyright (c) 2020. Armadillo
 */

package armadillo.studio.model.signer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.net.URI;

public class KeyFile extends File {
    public KeyFile(@NonNull String pathname) {
        super(pathname);
    }

    public KeyFile(@Nullable String parent, @NonNull String child) {
        super(parent, child);
    }

    public KeyFile(@Nullable File parent, @NonNull String child) {
        super(parent, child);
    }

    public KeyFile(@NonNull URI uri) {
        super(uri);
    }

    @NonNull
    @Override
    public String toString() {
        return getName().replace(".key", "");
    }
}
