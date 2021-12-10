package com.cai.open.bridge.transform

import com.android.build.api.transform.*
import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.internal.pipeline.TransformManager
import com.cai.open.bridge.utils.LogUtils
import com.cai.open.bridge.utils.ZipUtils
import groovy.io.FileType
import org.apache.commons.io.FileUtils
import org.gradle.api.Plugin
import org.gradle.api.Project

import java.lang.reflect.ParameterizedType

abstract class ScanPlugin<T> implements Plugin<Project>, ScanCallback<T> {

    Project mProject

    T getExt() {
        return mProject.getExtensions().getByName(getPluginName())
    }

    @Override
    void apply(Project project) {
        if (project.plugins.hasPlugin(AppPlugin)) {
            mProject = project
            LogUtils.init(project)
            project.extensions.create(getPluginName(), getGenericClass())
            def android = project.extensions.getByType(AppExtension)
            android.registerTransform(new ScanTransform())
        }
    }

    Class<T> getGenericClass() {
        return ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0]
    }

    class ScanTransform extends Transform {

        @Override
        String getName() {
            return getPluginName()
        }

        @Override
        Set<QualifiedContent.ContentType> getInputTypes() {
            return TransformManager.CONTENT_CLASS
        }

        @Override
        Set<? super QualifiedContent.Scope> getScopes() {
            return TransformManager.SCOPE_FULL_PROJECT
        }

        @Override
        boolean isIncremental() {
            return false
        }

        @Override
        void transform(TransformInvocation transformInvocation)
                throws TransformException, InterruptedException, IOException {
            super.transform(transformInvocation)
            log('====== GradlePlugin: [ ' + getPluginName() + ' ] Activated ======')
            log("## Plugin ext :" + getExt())
            log("## Project file scan start >>>")
            long stTime = System.currentTimeMillis()

            def inputs = transformInvocation.getInputs()
            def outputProvider = transformInvocation.getOutputProvider()

            outputProvider.deleteAll()
            onScanStarted()

            inputs.each { TransformInput input ->
                input.directoryInputs.each { DirectoryInput dirInput ->// 遍历文件夹
                    File dir = dirInput.file
                    File dest = outputProvider.getContentLocation(
                            dirInput.name,
                            dirInput.contentTypes,
                            dirInput.scopes,
                            Format.DIRECTORY
                    )
                    FileUtils.copyDirectory(dir, dest)

                    log("SCAN DIR: ${dirInput.file}")
                    scanDir(dest)
                }
                input.jarInputs.each { JarInput jarInput ->
                    File jar = jarInput.file

                    def jarName = jarInput.name
                    def dest = outputProvider.getContentLocation(
                            jarName,
                            jarInput.contentTypes,
                            jarInput.scopes,
                            Format.JAR
                    )
                    FileUtils.copyFile(jar, dest)

                    if (isIgnoreScan(jarInput)) {
                        log("SKIP JAR: $jarName")
                        return
                    }

                    if (jarName.indexOf('.') != -1) {
                        log("SCAN JAR: $jarName")
                    }
                    scanJar(dest)
                }
            }

            onScanFinished()
            log(getName() + "Scan finished, it took: " + (System.currentTimeMillis() - stTime) + "ms")

            log("====== GradlePlugin: [ " + getPluginName() + " ] finished ======")
        }


        void scanJar(File jar) {
            File tmp = new File(jar.getParent(), "temp_" + jar.getName())
            List<File> unzipFile = ZipUtils.unzipFile(jar, tmp)
            if (unzipFile != null && unzipFile.size() > 0) {
                scanDir(tmp, jar)
                FileUtils.forceDelete(tmp)
            }
        }

        void scanDir(File root) {
            scanDir(root, root)
        }

        void scanDir(File dir, File originScannedJarOrDir) {
            if (!dir.isDirectory()) return
            String rootPath = dir.getAbsolutePath()
            if (!rootPath.endsWith(ScanConfig.SEPARATOR)) {
                rootPath += ScanConfig.SEPARATOR
            }

            dir.eachFileRecurse(FileType.FILES) { File file ->
                def fileName = file.name
                if (!fileName.endsWith('.class')
                        || fileName.startsWith('R$')
                        || fileName == 'R.class'
                        || fileName == 'BuildConfig.class') {
                    return
                }

                if (fileName.endsWith('.jpg')
                        || fileName.endsWith('.jpeg')
                        || fileName.endsWith('.webp')
                ) {
                    log("发现图片：" + fileName)
                }

                def filePath = file.absolutePath
                def packagePath = filePath.replace(rootPath, '')
                def className = packagePath.replace(ScanConfig.SEPARATOR, ".")
                className = className.substring(0, className.length() - 6)
                scanClassFile(file, className, originScannedJarOrDir)
            }
        }
    }

    void log(Object obj) {
        LogUtils.l(getPluginName(), obj)
    }
}
