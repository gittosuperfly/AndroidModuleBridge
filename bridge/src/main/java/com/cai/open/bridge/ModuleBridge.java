package com.cai.open.bridge;


import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ModuleBridge {

    private static final String TAG = "Bridge";

    private final Map<Class, BaseBridge> mBridgeMap = new ConcurrentHashMap<>();
    private final Map<Class, Class> mInjectBridgeImplMap = new HashMap<>();

    private ModuleBridge() {
        init();
    }

    /**
     * Get api.
     *
     * @param bridgeClass The class of api.
     * @param <T>         The type.
     * @return the bridge
     */
    public static <T extends BaseBridge> T get(final Class<T> bridgeClass) {
        return getInstance().getApiInner(bridgeClass);
    }

    public static void register(Class<? extends BaseBridge> implClass) {
        getInstance().registerImpl(implClass);
    }

    private static ModuleBridge getInstance() {
        return LazyHolder.INSTANCE;
    }

    /**
     * It will automatically inject bridge implementation classes with  {@link Bridge} annotation
     * <p>
     * by function of {@link ModuleBridge#registerImpl} when execute transform task.
     */
    private void init() {/*inject*/}

    private void registerImpl(Class implClass) {
        mInjectBridgeImplMap.put(implClass.getSuperclass(), implClass);
    }

    @Override
    public String toString() {
        return "BridgeCore: " + mInjectBridgeImplMap;
    }

    private <Result> Result getApiInner(Class apiClass) {
        BaseBridge api = mBridgeMap.get(apiClass);
        if (api != null) {
            return (Result) api;
        }
        synchronized (apiClass) {
            api = mBridgeMap.get(apiClass);
            if (api != null) {
                return (Result) api;
            }
            Class implClass = mInjectBridgeImplMap.get(apiClass);
            if (implClass != null) {
                try {
                    api = (BaseBridge) implClass.newInstance();
                    mBridgeMap.put(apiClass, api);
                    return (Result) api;
                } catch (Exception ignore) {
                    Log.e(TAG, "The <" + implClass + "> has no parameterless constructor.");
                    return null;
                }
            } else {
                Log.e(TAG, "The <" + apiClass + "> doesn't implement.");
                return null;
            }
        }
    }

    private static class LazyHolder {
        private static final ModuleBridge INSTANCE = new ModuleBridge();
    }
}
