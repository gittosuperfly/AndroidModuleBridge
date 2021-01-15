package com.cai.open.bridge.transform

import com.android.build.api.transform.JarInput

interface ScanCallback<T> {

    String getPluginName();

    void onScanStarted();

    boolean isIgnoreScan(JarInput input);

    void scanClassFile(File classFile, String className, File originScannedJarOrDir);

    void onScanFinished();
}