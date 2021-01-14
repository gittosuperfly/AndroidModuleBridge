package com.cai.open.bridge

class PluginExtension {

    boolean abortOnError = true
    String bridgeUtilsClass = "com.cai.open.bridge.ModuleBridge"
    String bridgeBaseClass = "com.cai.open.bridge.BaseBridge"
    String bridgeAnnotationClass = "com.cai.open.bridge.Bridge"
    String onlyScanLibRegex = ""
    String jumpScanLibRegex = ""

    @Override
    String toString() {
        return "PluginExtension { " +
                "abortOnError: " + abortOnError +
                ", bridgeUtilsClass: " + bridgeUtilsClass +
                ", bridgeBaseClass: " + bridgeBaseClass +
                ", bridgeAnnotationClass: " + bridgeAnnotationClass +
                (onlyScanLibRegex == "" ? "" : ", onlyScanLibRegex: " + onlyScanLibRegex) +
                (jumpScanLibRegex == "" ? "" : ", jumpScanLibRegex: " + jumpScanLibRegex) +
                " }";
    }
}
