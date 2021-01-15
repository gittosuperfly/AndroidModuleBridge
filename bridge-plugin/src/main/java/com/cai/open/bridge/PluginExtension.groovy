package com.cai.open.bridge

class PluginExtension {

    boolean stopOnError = true
    String outputFile = "bridge_table.json"
    String onlyScanRegex = ""
    String skipScanRegex = ""

    @Override
    String toString() {
        return "PluginExtension { " +
                "stopOnError: " + stopOnError +
                "outputFile: " + outputFile +
                (onlyScanRegex == "" ? "" : ", onlyScanRegex: " + onlyScanRegex) +
                (skipScanRegex == "" ? "" : ", skipScanRegex: " + skipScanRegex) +
                " }";
    }
}
