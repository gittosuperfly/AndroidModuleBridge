package com.cai.open.bridge

class PluginExtension {

    boolean abortOnError = true
    String bridgeUtilsClass = "com.cai.bridge.ModuleBridge"
    String bridgeBaseClass = "com.cai.bridge.BaseBridge"
    String bridgeAnnotationClass = "com.cai.bridge.Bridge"
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
