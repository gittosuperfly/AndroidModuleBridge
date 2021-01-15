package com.cai.open.bridge

import com.cai.open.bridge.transform.ScanConfig

class BridgeConfig {

    public static final String EXT_NAME = 'Bridge'

    public static final List<String> EXCLUDE_LIBS_START_WITH = [
            'com.android.support',
            'androidx',
            'com.google',
            'android.arch',
            'org.jetbrains',
            'com.squareup',
            'org.greenrobot',
            'com.github.bumptech.glide',
            'io.reactivex.rxjava3',
            'com.github.tbruyelle:rxpermissions',
            'com.tencent.bugly',
            'com.alibaba:arouter'
    ]

    public static final String FILE_SEP = ScanConfig.SEPARATOR
}