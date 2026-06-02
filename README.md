# AndroidLogger

一个帮助开发者快速实现日志管理的Android开源库。

## 功能特性

- **多级别日志**：支持 DEBUG、INFO、WARN、ERROR、VERBOSE 日志级别
- **文件持久化**：日志自动保存到文件系统，按日期和标签分组
- **日志分享**：支持将日志文件打包并分享给其他应用
- **自动清理**：支持自动清理指定天数前的旧日志
- **线程安全**：使用线程池异步写入日志，不阻塞主线程

## 快速开始

### 依赖配置

在项目根目录的 `build.gradle` 或 `settings.gradle` 中添加仓库：

```gradle
repositories {
    mavenCentral()
    maven { url 'https://jitpack.io' }
}
```

在 `app/build.gradle` 中添加依赖：

```gradle
dependencies {
    implementation 'com.gitee.dlkw:android-logger:1.1'
}
```

### 初始化

在 `Application` 或 `Activity` 的 `onCreate` 方法中初始化：

```java
Logger.init(new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "logs"), 
            getApplication().getPackageName() + ".fileprovider");
```

### 基本使用

```java
// 获取默认Logger实例
Logger logger = Logger.get();

// 获取指定标签的Logger实例
Logger logger = Logger.get("MyTag");

// 记录日志
logger.d("Debug message");
logger.i("Info message");
logger.w("Warning message");
logger.e("Error message");
logger.v("Verbose message");

// 记录带异常的日志
logger.e("Error with exception", new Throwable("Something went wrong"));
```

### 发送日志

```java
// 发送日志（带额外信息）
Map<String, Object[]> extras = new WeakHashMap<>();
extras.put("DeviceInfo", new Object[]{"Android 13", "Pixel 7"});
Logger.sendLogs(MainActivity.this, extras);
```

### 清理旧日志

```java
// 清理7天前的旧日志
Logger.clearOlderLogs(7);
```

## API 文档

### 初始化方法

```java
Logger.init(File logDir, String providerAuthority)
```

| 参数 | 说明 |
| --- | --- |
| `logDir` | 日志文件存储目录 |
| `providerAuthority` | FileProvider 的 authority，用于文件分享 |

### 获取Logger实例

```java
Logger.get()              // 获取默认标签的Logger
Logger.get(String tag)    // 获取指定标签的Logger
```

### 日志方法

| 方法 | 说明 |
| --- | --- |
| `d(String msg)` | 记录DEBUG级别日志 |
| `d(String msg, Throwable e)` | 记录DEBUG级别日志（带异常） |
| `i(String msg)` | 记录INFO级别日志 |
| `i(String msg, Throwable e)` | 记录INFO级别日志（带异常） |
| `w(String msg)` | 记录WARN级别日志 |
| `w(String msg, Throwable e)` | 记录WARN级别日志（带异常） |
| `e(String msg)` | 记录ERROR级别日志 |
| `e(String msg, Throwable e)` | 记录ERROR级别日志（带异常） |
| `v(String msg)` | 记录VERBOSE级别日志 |
| `v(String msg, Throwable e)` | 记录VERBOSE级别日志（带异常） |
| `save()` | 强制将缓存的日志写入文件 |

### 工具方法

| 方法 | 说明 |
| --- | --- |
| `getLogFiles()` | 获取所有日志文件列表 |
| `getLogFileDir()` | 获取日志存储目录 |
| `sendLogs(Activity activity)` | 弹出对话框选择并发送日志 |
| `sendLogs(Activity activity, Map<String, Object[]> extras)` | 发送日志（带额外信息） |
| `clearOlderLogs(int daysAgo)` | 清理指定天数前的旧日志 |

## FileProvider 配置

在 `AndroidManifest.xml` 中添加 FileProvider：

```xml
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.fileprovider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>
```

创建 `res/xml/file_paths.xml`：

```xml
<?xml version="1.0" encoding="utf-8"?>
<paths xmlns:android="http://schemas.android.com/apk/res/android">
    <external-files-path name="logs" path="documents/logs" />
</paths>
```

## 项目结构

```
AndroidLogger/
├── app/                 # 示例应用
│   └── src/main/java/com/dlkw/android/logger/sample/
│       └── MainActivity.java
├── logger/              # 核心库模块
│   └── src/main/java/com/dlkw/android/logger/
│       ├── Logger.java      # 抽象类，定义API接口
│       └── LoggerImpl.java  # 实现类
└── README.md
```

## 日志文件格式

日志文件按日期和标签命名：`yyyy-MM-dd_Tag.log`

日志内容格式：
```
yyyy-MM-dd HH:mm:ss.SSS LEVEL TAG Message
```

## 版本历史

- **1.0.0** - 初始版本，支持基本日志功能

## 许可证

Apache 2.0 License