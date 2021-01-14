package com.cai.open.bridge

import com.android.build.api.transform.JarInput
import com.cai.open.bridge.transform.BaseTransformPlugin
import com.cai.open.bridge.transform.JsonUtils
import com.cai.open.bridge.visitor.BridgeClassVisitor
import org.apache.commons.io.FileUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter

import java.util.regex.Pattern

class BridgePlugin extends BaseTransformPlugin<PluginExtension> {

    String bridgeUtilsClass
    String bridgeBaseClass
    String bridgeAnnotationClass
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
        bridgeUtilsClass = ext.bridgeUtilsClass
        if (bridgeUtilsClass.trim() == "") {
            throw new Exception("BridgeExtension's bridgeUtilsClass is empty.")
        }
        bridgeBaseClass = ext.bridgeBaseClass
        if (bridgeBaseClass.trim() == "") {
            throw new Exception("BridgeExtension's bridgeBaseClass is empty.")
        }
        bridgeAnnotationClass = ext.bridgeAnnotationClass
        if (bridgeAnnotationClass.trim() == "") {
            throw new Exception("BridgeExtension's bridgeAnnotationClass is empty.")
        }

        jsonFile = new File(mProject.projectDir.getAbsolutePath(), "__bridge__.json")
        FileUtils.write(jsonFile, "{}")
    }

    @Override
    boolean isIgnoreScan(JarInput input) {
        def jarName = input.name
        if (jarName.contains("utilcode")) {
            return false
        }

        if (ext.onlyScanLibRegex != null && ext.onlyScanLibRegex.trim().length() > 0) {
            return !Pattern.matches(ext.onlyScanLibRegex, jarName)
        }

        if (ext.jumpScanLibRegex != null && ext.jumpScanLibRegex.trim().length() > 0) {
            if (Pattern.matches(ext.jumpScanLibRegex, jarName)) {
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
            log("<BridgeUtils transform file>: $originScannedJarOrDir")
        }

        ClassReader cr = new ClassReader(classFile.bytes)
        ClassWriter cw = new ClassWriter(cr, 0)
        ClassVisitor cv = new BridgeClassVisitor(cw, bridgeInfoMap, bridgeClasses, bridgeBaseClass, bridgeAnnotationClass)
        try {
            cr.accept(cv, ClassReader.SKIP_FRAMES)
        } catch (Exception ignore) {
            ignore.printStackTrace()
        }
    }

    @Override
    void onScanFinished() {
        if (bridgeUtilsTransformFile != null) {
            if (bridgeClasses.isEmpty()) {
                log("No bridge.")
            } else {
                Map bridgeImpl = [:]
                List<String> noImplApis = []
                bridgeInfoMap.each { key, value ->
                    bridgeImpl.put(key, value.toString())
                }
                bridgeClasses.each {
                    if (!bridgeInfoMap.containsKey(it)) {
                        noImplApis.add(it)
                    }
                }
                Map bridgeDetails = [:]
                bridgeDetails.put("bridgeCoreClass", bridgeUtilsClass)
                bridgeDetails.put("bridgePair", bridgeImpl)
                bridgeDetails.put("notImplemented", noImplApis)
                String apiJson = JsonUtils.getFormatJson(bridgeDetails)
                log(jsonFile.toString() + ": " + apiJson)
                FileUtils.write(jsonFile, apiJson)

                if (noImplApis.size() > 0) {
                    if (ext.abortOnError) {
                        throw new Exception("你应该实现以下Bridge: \n" + noImplApis +
                                "\n 你可以在以下文件中找到它们: \n" + jsonFile.toString())
                    }
                }
                BridgeInject.start(bridgeInfoMap, bridgeUtilsTransformFile, bridgeUtilsClass)
            }
        } else {
            throw new Exception("No BridgeUtils of ${bridgeUtilsClass} in $mProject.")
        }
    }
}
