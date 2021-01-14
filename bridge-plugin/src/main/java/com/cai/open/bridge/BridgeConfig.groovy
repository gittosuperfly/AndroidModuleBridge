package com.cai.open.bridge

import com.cai.open.bridge.transform.BaseTransformConfig

class BridgeConfig {

    public static final String EXT_NAME = 'bridge'

    public static final List<String> EXCLUDE_LIBS_START_WITH = [
            'com.android.support',
            'androidx',
            'com.google',
            'android.arch',
            'org.jetbrains',
            'com.squareup',
            'org.greenrobot',
            'com.github.bumptech.glide'
    ]

    public static final String FILE_SEP = BaseTransformConfig.FILE_SEP
}