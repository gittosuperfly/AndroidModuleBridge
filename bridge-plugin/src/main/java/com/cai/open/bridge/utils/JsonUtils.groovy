package com.cai.open.bridge.utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder

final class JsonUtils {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create()

    static String getFormatJson(Object object) {
        return gson.toJson(object)
    }
}