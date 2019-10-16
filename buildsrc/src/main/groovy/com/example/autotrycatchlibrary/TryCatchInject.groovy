package com.example.autotrycatchlibrary

import javassist.ClassPool
import javassist.CtClass
import javassist.CtMethod
import javassist.bytecode.AnnotationsAttribute
import javassist.bytecode.MethodInfo
import javassist.bytecode.annotation.ArrayMemberValue
import javassist.bytecode.annotation.ClassMemberValue

import java.lang.annotation.Annotation

/**
 * 自动捕获异常
 */
class TryCatchInject {
    private static String path
    private static ClassPool pool = ClassPool.getDefault()

    private static final String CLASS_SUFFIX = ".class"
    private static final String PROCESSED_ANNOTATION_NAME =
            "com.example.autotrycatchdemo.autotrycatch.AutoTryCatch"

    static void injectDir(String path, String packageName) {
        this.path = path
        pool.appendClassPath(path)
        traverseFile(packageName)
    }

    private static traverseFile(String packageName) {
        File dir = new File(path)
        if (!dir.isDirectory()) {
            return
        }
        beginTraverseFile(dir, packageName)
    }

    private static beginTraverseFile(File dir, packageName) {
        dir.eachFileRecurse {
            File file ->
                String filePath = file.absolutePath
//                System.out.println("file:" + file)
                if (isClassFile(filePath)) {
//                    System.out.println("file:" + file)
                    int index = filePath.indexOf(packageName
                            .replace(".", File.separator)
                            .replace("/", File.separator)
                            .replace("\\", File.separator))
                    if (index != -1) {
                        transformPathAndInjectCode(filePath, index)
                    }
                }
        }
    }

    private static boolean isClassFile(String filePath) {
        return filePath.endsWith(".class") &&
                !filePath.contains('R$') &&
                !filePath.contains('R.class') &&
                !filePath.contains("BuildConfig.class")
    }

    private static void transformPathAndInjectCode(String filePath, int index) {
        String className = getClassNameFromFilePath(filePath, index)
        injectCode(className)
    }

    private static String getClassNameFromFilePath(String filePath, int index) {
        int end = filePath.length() - CLASS_SUFFIX.length()
        String className = filePath.substring(index, end).replace('\\', '.').replace('/', '.')
        className
    }

    private static void injectCode(String className) {
//        System.out.println("className:" + className)
        CtClass c = pool.getCtClass(className)
//        System.out.println("CtClass:" + c)
        defrostClassIfFrozen(c)
        traverseMethod(c)

        c.writeFile(path)
        c.detach()
    }

    private static void traverseMethod(CtClass c) {
        CtMethod[] methods = c.getDeclaredMethods()
        for (ctMethod in methods) {
//             System.out.println("ctMethod:" + ctMethod)
            traverseAnnotation(ctMethod)
        }
    }

    private static void traverseAnnotation(CtMethod ctMethod) {
        Annotation[] annotations = ctMethod.getAnnotations()

        for (annotation in annotations) {
            def canonicalName = annotation.annotationType().canonicalName
            if (isSpecifiedAnnotation(canonicalName)) {
                onIsSpecifiedAnnotation(ctMethod, canonicalName)
            }
        }
    }

    private static boolean isSpecifiedAnnotation(String canonicalName) {
        PROCESSED_ANNOTATION_NAME.equals(canonicalName)
    }

    private static void onIsSpecifiedAnnotation(CtMethod ctMethod, String canonicalName) {
        MethodInfo methodInfo = ctMethod.getMethodInfo()
        AnnotationsAttribute attribute = methodInfo.getAttribute(AnnotationsAttribute.visibleTag)
        javassist.bytecode.annotation.Annotation javassistAnnotation = attribute.getAnnotation(canonicalName)
        def names = javassistAnnotation.getMemberNames()
        // 注解的值
//        System.out.println("names:" + names)
        if (names == null || names.isEmpty()) {
            catchAllExceptions(ctMethod)
            return
        }
        catchSpecifiedExceptions(ctMethod, names, javassistAnnotation)
    }

    private static catchAllExceptions(CtMethod ctMethod) {
        CtClass etype = pool.get("java.lang.Exception")
        ctMethod.addCatch('{com.example.autotrycatchdemo.autotrycatch.Logger.print($e);return;}', etype)
    }

    private
    static void catchSpecifiedExceptions(CtMethod ctMethod, Set names, javassist.bytecode.annotation.Annotation javassistAnnotation) {
        names.each { def name ->

            ArrayMemberValue arrayMemberValues = (ArrayMemberValue) javassistAnnotation.getMemberValue(name)
            if (arrayMemberValues == null) {
                return
            }
            addMultiCatch(ctMethod, (ClassMemberValue[]) arrayMemberValues.getValue())
        }
    }

    private static void addMultiCatch(CtMethod ctMethod, ClassMemberValue[] classMemberValues) {
        classMemberValues.each { ClassMemberValue classMemberValue ->
            CtClass etype = pool.get(classMemberValue.value)
            ctMethod.addCatch('{com.example.autotrycatchdemo.autotrycatch.Logger.print($e);return;}', etype)
        }
    }


    private static void defrostClassIfFrozen(CtClass c) {
        if (c.isFrozen()) {
            c.defrost()
        }
    }

}