# AutoTryCatchDemo
自定义Plugin实现异常自动捕获

#### 1. autotrycatchlibrary：支持上传maven

根项目build.gradle添加库依赖:

```
    repositories {
        maven {
            url uri("autotrycatchlibrary/repo")
        }
    }
    dependencies {
        classpath 'com.example.autotrycatchlibrary:pathplugin:1.0'
    }    
```

app模块build.gradle使用：

```
// 使用可以上传到maven的插件，当前是在本地
apply plugin: 'path-plugin'
```

#### 2. buildsrc：只能在本项目中使用的Groovy

app模块build.gradle使用：

```
// 使用 buildsrc 的插件
//apply plugin: com.example.autotrycatchlibrary.PathPlugin
```

