package com.cai.open.bridge

import com.android.build.api.transform.JarInput
import com.cai.open.bridge.transform.ScanPlugin
import com.cai.open.bridge.utils.JsonUtils
import com.cai.open.bridge.visitor.BridgeClassVisitor
import org.apache.commons.io.FileUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter

import java.util.regex.Pattern

class BridgePlugin extends ScanPlugin<PluginExtension> {

    String bridgeUtilsClass = "com.cai.open.bridge.ModuleBridge"
    String bridgeBaseClass = "com.cai.open.bridge.BaseBridge"
    String bridgeAnnotationClass = "com.cai.open.bridge.Bridge"

    File jsonFile
    File bridgeUtilsTransformFile
    Map<String, BridgeInfo> bridgeInfoMap = [:]
    List<String> bridgeClasses = []

    @Override
    String getPluginName() {
        return BridgeConfig.EXT_NAME
    }

    @Override
    void onScanStarted() {
        String fileName = ext.outputFile
        jsonFile = new File(mProject.projectDir.getAbsolutePath(), fileName)
        FileUtils.write(jsonFile, "{}")
    }

    @Override
    boolean isIgnoreScan(JarInput input) {
        def jarName = input.name

        if (ext.onlyScanRegex != null && ext.onlyScanRegex.trim().length() > 0) {
            return !Pattern.matches(ext.onlyScanRegex, jarName)
        }

        if (ext.skipScanRegex != null && ext.skipScanRegex.trim().length() > 0) {
            if (Pattern.matches(ext.skipScanRegex, jarName)) {
                return true
            }
        }

        for (exclude in BridgeConfig.EXCLUDE_LIBS_START_WITH) {
            if (jarName.startsWith(exclude)) {
                return true
            }
        }
        return false
    }

    @Override
    void scanClassFile(File classFile, String className, File originScannedJarOrDir) {
        if (bridgeUtilsClass == className) {
            bridgeUtilsTransformFile = originScannedJarOrDir
        }

        ClassReader reader = new ClassReader(classFile.bytes)
        ClassWriter writer = new ClassWriter(reader, 0)
        ClassVisitor visitor = new BridgeClassVisitor(
                writer,
                bridgeInfoMap,
                bridgeClasses,
                bridgeBaseClass,
                bridgeAnnotationClass
        )
        try {
            reader.accept(visitor, ClassReader.SKIP_FRAMES)
        } catch (Exception ignore) {
            ignore.printStackTrace()
        }
    }

    @Override
    void onScanFinished() {
        if (bridgeUtilsTransformFile != null) {
            if (bridgeClasses.isEmpty()) {
                log("## No bridge.")
            } else {
                Map bridgeImpl = [:]
                List<String> notImpl = []
                bridgeInfoMap.each { key, value ->
                    bridgeImpl.put(key, value.toString())
                }
                bridgeClasses.each {
                    if (!bridgeInfoMap.containsKey(it)) {
                        notImpl.add(it)
                    }
                }
                Map bridgeDetails = [:]
                bridgeDetails.put("bridgePair", bridgeImpl)
                bridgeDetails.put("notImplemented", notImpl)
                String apiJson = JsonUtils.getFormatJson(bridgeDetails)
                String divLine = "───────────────────────────────────────────────" +
                        "────────────────────────────────────────────────────────"
                apiJson = apiJson.replaceAll("\\n", "\n│")
                log("## Project bridge scan results >>> \n┌$divLine\n│$apiJson\n└divLine")
                FileUtils.write(jsonFile, apiJson)
                log("Output to file : " + jsonFile.toString())

                if (notImpl.size() > 0) {
                    if (ext.stopOnError) {
                        throw new Exception("You should implement the following Bridge: \n $notImpl" +
                                "\nYou can find it in the following file: \n" + jsonFile.toString())
                    }
                }
                BridgeInject.start(bridgeInfoMap, bridgeUtilsTransformFile, bridgeUtilsClass)
            }
        } else {
            throw new Exception("No BridgeUtils of ${bridgeUtilsClass} in $mProject.")
        }
    }
}
