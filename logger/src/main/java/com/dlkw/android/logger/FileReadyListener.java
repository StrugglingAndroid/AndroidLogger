package com.dlkw.android.logger;

import java.io.File;

public interface FileReadyListener {
    void onReady(File zipFile);

    void onError(Throwable e);
}
