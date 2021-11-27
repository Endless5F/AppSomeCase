package com.android.buildsrcplugin

import com.android.build.api.transform.Format
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.AppExtension
import com.android.build.gradle.internal.pipeline.TransformManager
import com.google.common.io.Files
import javassist.ClassPool
import javassist.CtClass
import javassist.bytecode.ClassFile
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.gradle.api.Project
import java.io.*
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream

/**
 * Activity#onCreate方法插入toast
 */
class ToastTransform(project: Project) : Transform() {

    private val classPool = ClassPool.getDefault()

    init {
        // 为了能够查找到android 相关的类，需要把android.jar包的路径添加到classPool 类搜索路径
        val android = project.extensions.getByType(AppExtension::class.java)
        if (android is AppExtension) {
            classPool.appendClassPath(android.bootClasspath[0].absolutePath)
        }
        classPool.importPackage("android.widget.Toast")
        classPool.importPackage("android.app.Activity")
    }

    override fun getName(): String {
        return "ToastPlugin"
    }

    /**
     * 处理的数据类型：
     *     CLASSES：代表处理的 java 的 class 文件
     *     RESOURCES：代表要处理 java 的资源
     */
    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> {
        return TransformManager.CONTENT_CLASS
    }

    override fun isIncremental(): Boolean {
        return false
    }

    /**
     * 操作内容的范围：
     *     EXTERNAL_LIBRARIES ： 只有外部库
     *     PROJECT ： 只有项目内容
     *     PROJECT_LOCAL_DEPS ： 只有项目的本地依赖(本地jar)
     *     PROVIDED_ONLY ： 只提供本地或远程依赖项
     *     SUB_PROJECTS ： 只有子项目
     *     SUB_PROJECTS_LOCAL_DEPS： 只有子项目的本地依赖项(本地jar)
     *     TESTED_CODE ：由当前变量(包括依赖项)测试的代码
     * 处理所有的class字节码，返回TransformManager.SCOPE_FULL_PROJECT
     */
    override fun getScopes(): MutableSet<in QualifiedContent.Scope> {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    override fun transform(transformInvocation: TransformInvocation?) {
        super.transform(transformInvocation)
        transformInvocation ?: return
        // 1. 对inputs--》directory--》class文件进行遍历
        // 2. 对inputs--》jar--》class文件进行遍历
        // 3. 符合我们项目包名，并且class文件路径包含 xxx.class结尾，还不能是buildconfig.class、R.class $.class文件

        val outputProvider = transformInvocation.outputProvider
        if (!isIncremental) {
            outputProvider?.deleteAll()
        }

        transformInvocation.inputs?.forEach { transformInput ->
            // 本地 project 编译成的多个 class ⽂件存放的目录
            transformInput.directoryInputs.forEach { directoryInput ->
                println("directoryInputs abs file path: ${directoryInput.file.absolutePath}")
                handleDirectory(directoryInput.file)
                // 把input--》dir--》class--》输出到dest目标目录
                // 目标目录
                val dest = outputProvider.getContentLocation(
                    directoryInput.name,
                    directoryInput.contentTypes,
                    directoryInput.scopes,
                    Format.DIRECTORY
                )
                FileUtils.copyDirectory(directoryInput.file, dest)
            }

            // 各个依赖所编译成的 jar(aar) 文件
            transformInput.jarInputs.forEach { jarInput ->
                println("jarInputs abs file path: ${jarInput.file.absolutePath}")
                val srcFile = handleJar(jarInput.file) ?: return@forEach
                // 防止重名
                var jarName = jarInput.name
                val md5 = DigestUtils.md5Hex(jarInput.file.absolutePath)
                if (jarName.endsWith(".jar")) {
                    jarName = jarName.substring(0, jarName.length - 4)
                }
                val dest = outputProvider.getContentLocation(
                    md5 + jarName, jarInput.contentTypes, jarInput.scopes, Format.JAR
                )
                FileUtils.copyFile(srcFile, dest)
            }
        }
        classPool.clearImportedPackages()
    }

    private fun handleDirectory(file: File?) {
        log("handleDirectory ${file?.isDirectory == true}")
        if (file?.isDirectory == true) {
            classPool.appendClassPath(file.absolutePath)
            getAllFiles(file).forEach {
                val filePath = it.absolutePath
                if (shouldModifyClass(filePath)) {
                    val inputStream = FileInputStream(it)
                    log("start modifyClass ${it.name}")
                    val ctClass = modifyClass(inputStream)
                    ctClass.writeFile(it.name)
                    // 从classPool中释放掉，防止一直处于内存中
                    ctClass.detach()
                    log("end modifyClass ${it.name}")
                } else {
                    log("handleDirectory error filePath $filePath")
                }
            }
        }
    }

    private fun handleJar(file: File?): File? {
        log("handleJar ${file?.name}")
        file?.let {
            classPool.appendClassPath(it.absolutePath)
            val inputJarFile = JarFile(file)
            val enumeration = inputJarFile.entries()
            // jar文件不可原路返回
            val outputJarFile = File(file.parentFile, "temp_${file.name}")
            if (outputJarFile.exists()) {
                outputJarFile.delete()
            }
            val jarOutputStream = JarOutputStream(BufferedOutputStream(FileOutputStream(outputJarFile)))
            while (enumeration.hasMoreElements()) {
                val inputJarEntry = enumeration.nextElement()
                val inputJarEntryName = inputJarEntry.name
                val outputJarEntry = JarEntry(inputJarEntryName)
                jarOutputStream.putNextEntry(outputJarEntry)
                val inputJarStream = inputJarFile.getInputStream(inputJarEntry)
                if (!shouldModifyClass(inputJarEntryName)) {
                    log("handleJar error inputJarEntryName $inputJarEntryName")
                    jarOutputStream.write(IOUtils.toByteArray(inputJarStream))
                    inputJarStream.close()
                    continue
                }

                log("start modifyClass $inputJarEntryName")
                val ctClass = modifyClass(inputJarStream)
                val byteCode = ctClass.toBytecode()
                ctClass.defrost()
                inputJarStream.close()
                jarOutputStream.write(byteCode)
                jarOutputStream.flush()
                log("end modifyClass $inputJarEntryName")
            }
            inputJarFile.close()
            jarOutputStream.closeEntry()
            jarOutputStream.flush()
            jarOutputStream.close()
            return outputJarFile
        }

        return null
    }

    private fun modifyClass(inputStream: InputStream): CtClass {
        val classFile = ClassFile(DataInputStream(BufferedInputStream(inputStream)))
        val ctClass = classPool.get(classFile.name)
        if (ctClass.isFrozen) {
            ctClass.defrost()
        }
        val bundle = classPool.get("android.os.Bundle")
        val params = arrayOf(bundle)
        val method = ctClass.getDeclaredMethod("onCreate", params)
        val message = classFile.name
        // javassist.CannotCompileException: [source error] no such field: com/android/app/MainActivity
//        method.insertAfter("Toast.makeText(this, $message, Toast.LENGTH_SHORT).show();")
        return ctClass
    }

    private fun shouldModifyClass(filePath: String): Boolean {
        return (filePath.contains("com/android/app") // 项目包名，选填(. 需要使用 / 来代替)
                && filePath.endsWith("Activity.class")
                && !filePath.endsWith("R.class")
                && !filePath.endsWith("BuildConfig.class")
                && !filePath.contains("\$")
                && !filePath.contains("R\$"))
    }

    private fun getAllFiles(dir: File): List<File> {
        return Files.fileTraverser().depthFirstPreOrder(dir).filter {
            it.isFile
        }
    }
}