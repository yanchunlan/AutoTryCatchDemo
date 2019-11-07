package com.example.autotrycatchlibrary

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.gradle.api.Project

// 错误的FileUtils 可能会导致异常

class PathTransform extends Transform {

    Project project
    TransformOutputProvider outputProvider
    String packageName = "com.example.autotrycatchdemo"


    public PathTransform(Project project) {
        this.project = project
    }

    // task 名称 TransfromClassesWithPreDexForXXXX
    @Override
    String getName() {
        return PathTransform.class.getSimpleName()
    }

    // 要处理的文件类型
    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        // 指定处理所有class和jar的字节码
        return TransformManager.CONTENT_CLASS
    }

    // 指定Transform的作用范围
    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return false
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        super.transform(transformInvocation)
    }

    @Override
    void transform(Context context,
                   Collection<TransformInput> inputs,
                   Collection<TransformInput> referencedInputs,
                   TransformOutputProvider outputProvider,
                   boolean isIncremental) throws IOException,
            TransformException,
            InterruptedException {
        super.transform(context, inputs, referencedInputs, outputProvider, isIncremental)
        this.outputProvider = outputProvider
        // 转换方法
        traversalInputs(inputs)
    }


    /**
     *  TransformInput 内部有JarInput，DirectoryInput
     */
    private void traversalInputs(Collection<TransformInput> inputs) {
        inputs.each {
            TransformInput input ->
                traversalDirInputs(input)
                traversalJarInputs(input)
        }
    }


    /**
     * 对文件夹遍历
     */
    private void traversalDirInputs(TransformInput input) {
        input.directoryInputs.each {
            /**
             * 文件夹里面包含的是
             *  我们手写的类
             *  R.class、
             *  BuildConfig.class
             *  R$XXX.class
             *  等
             *  根据自己的需要对应处理
             */
            System.out.println("traversalDirInputs copy absolutePath " + it.file.absolutePath)


            //注入代码buildSrc\src\main\groovy\demo
            TryCatchInject.injectDir(it.file.absolutePath, packageName)

            // 获取output目录
            def dest = outputProvider.getContentLocation(it.name
                    , it.contentTypes, it.scopes, Format.DIRECTORY)
//            System.out.println("traversalDirInputs dest absolutePath " + dest)
            // 将input的目录复制到output指定目录
            FileUtils.copyDirectory(it.file, dest)
        }
    }

    /**
     * 对jar文件遍历
     */
    private void traversalJarInputs(TransformInput inputs) {
        inputs.jarInputs.each {
            JarInput jarInput ->
                //jar文件一般是第三方依赖库jar文件
                // 重命名输出文件（同目录copyFile会冲突）

                def jarName = jarInput.name
//                System.out.println("traversalJarInputs copy jarName " + jarName)
                def md5Name = DigestUtils.md5Hex(jarInput.file.getAbsolutePath())
                if (jarName.endsWith(".jar")) {
                    jarName = jarName.substring(0, jarName.length() - 4)
                }
                //生成输出路径
                def dest = outputProvider.getContentLocation(jarName + md5Name,
                        jarInput.contentTypes, jarInput.scopes, Format.JAR)
//                System.out.println("traversalJarInputs dest fileName " + dest)

                //将输入内容复制到输出
                FileUtils.copyFile(jarInput.file, dest)
        }
    }
}