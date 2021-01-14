package com.cai.open.bridge.transform

import com.google.gson.Gson
import com.google.gson.GsonBuilder

final class JsonUtils {

    static final Gson GSON = new GsonBuilder().setPrettyPrinting().create()

    static String getFormatJson(Object object) {
        return GSON.toJson(object)
    }
}