package com.dlkw.android.logger;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.Handler;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@SuppressWarnings("all")
public abstract class Logger {
    private static final Map<String, Logger> loggers = new WeakHashMap<>();
    private static File sLogDir;
    private static String sProviderAuthority;
    protected final String tag;
    private boolean debug = true;

    private static final Handler sMainHandler = new Handler();
    private static final Executor sExecutor = Executors.newCachedThreadPool();

    public static void init(File logDir, String providerAuthority) {
        sLogDir = logDir;
        sProviderAuthority = providerAuthority;
    }

    public static Logger get() {
        return get("Logger");
    }

    public static Logger get(String tag) {
        Logger logger = loggers.get(tag);
        if (logger != null) {
            return logger;
        }
        loggers.put(tag, new LoggerImpl(tag));
        return loggers.get(tag);
    }

    public static File getLogFileDir() {
        if (sLogDir == null) {
            throw new RuntimeException("Logger not initialized");
        }
        if (!sLogDir.exists()) {
            sLogDir.mkdirs();
        }
        return sLogDir;
    }

    public static List<File> getLogFiles() {
        File logFileDir = getLogFileDir();
        File[] logFiles = logFileDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.toLowerCase().endsWith(".log");
            }
        });
        List<File> fileList = new ArrayList<>();
        if (null != logFiles) {
            Collections.addAll(fileList, logFiles);
        }
        return fileList;
    }

    public static void sendLogs(Activity activity) {

    }

    public static void sendLogs(Activity activity, Map<String, Object[]> extras) {
        List<File> logFiles = getLogFiles();
        if (logFiles.isEmpty()) {
            Toast.makeText(activity, "暂无日志", Toast.LENGTH_SHORT).show();
            return;
        }
        final String[] logFileNames = new String[logFiles.size()];
        for (int i = 0; i < logFiles.size(); i++) {
            logFileNames[i] = logFiles.get(i).getName();
        }
        final boolean[] checkStates = new boolean[logFiles.size()];
        new AlertDialog.Builder(activity)
                .setTitle("发送日志")
                .setCancelable(false)
                .setMultiChoiceItems(logFileNames, checkStates, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {

                    }
                })
                .setPositiveButton("发送", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        List<File> checkedFiles = new ArrayList<>();
                        for (int i = 0; i < checkStates.length; i++) {
                            if (checkStates[i]) {
                                checkedFiles.add(logFiles.get(i));
                            }
                        }
                        sendFiles(activity, checkedFiles, extras);
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }

    private static Class<?> BuildConfigClass = null;

    private static Class<?> getBuildConfigClass(Context context) {
        if (BuildConfigClass == null) {
            try {
                BuildConfigClass = Class.forName(context.getApplicationContext().getPackageName() + ".BuildConfig");
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
        return BuildConfigClass;
    }

    private static void write(ZipOutputStream zos, Object... datas) throws IOException {
        for (Object data : datas) {
            zos.write(String.valueOf(data).getBytes());
        }
        zos.write("\n".getBytes());
    }

    public static void sendFiles(final Activity activity, final List<File> logFiles, final Map<String, Object[]> extras) {
        sExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
                    File zipFile = new File(getLogFileDir(), "log" + dateFormat.format(new Date()) + ".zip");
                    if (!zipFile.exists()) {
                        zipFile.createNewFile();
                    }
                    ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile), StandardCharsets.UTF_8);

                    zos.putNextEntry(new ZipEntry(".desc"));
                    ApplicationInfo appInfo = activity.getApplication().getApplicationInfo();
                    PackageInfo pkgInfo = activity.getPackageManager().getPackageInfo(activity.getApplication().getPackageName(), 0);
                    write(zos, "VersionName:", pkgInfo.versionName);
                    write(zos, "VersionCode:", pkgInfo.versionCode);
                    write(zos, "ApplicationName:", appInfo.loadLabel(activity.getPackageManager()));
                    write(zos, "FirstInstallTime:", pkgInfo.firstInstallTime);
                    write(zos, "lastUpdateTime:", pkgInfo.lastUpdateTime);
                    write(zos, "Build.MODEL:", Build.MODEL);
                    write(zos, "Build.BRAND:", Build.BRAND);
                    write(zos, "Build.SUPPORTED_32_BIT_ABIS:", String.join(",", Build.SUPPORTED_32_BIT_ABIS));
                    write(zos, "Build.SUPPORTED_64_BIT_ABIS:", String.join(",", Build.SUPPORTED_64_BIT_ABIS));
                    write(zos, "Build.VERSION.SDK_INT:", Build.VERSION.SDK_INT);
                    write(zos, "Build.VERSION.SDK_INT_FULL:", Build.VERSION.SDK_INT_FULL);

                    for (Map.Entry<String, Object[]> entry : extras.entrySet()) {
                        Object[] arr = new Object[entry.getValue().length + 1];
                        arr[0] = entry.getKey();
                        System.arraycopy(entry.getValue(), 0, arr, 1, entry.getValue().length);
                        write(zos, arr);
                    }
                    zos.closeEntry();

                    for (File checkedFile : logFiles) {
                        zos.putNextEntry(new ZipEntry(checkedFile.getName()));
                        FileInputStream fis = new FileInputStream(checkedFile);
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = fis.read(buffer)) > 0) {
                            zos.write(buffer, 0, length);
                        }
                        fis.close();
                        zos.closeEntry();
                    }
                    zos.close();

                    sMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Intent intent = new Intent(Intent.ACTION_SEND);
                            intent.setType("application/zip");
                            intent.putExtra(Intent.EXTRA_TITLE, "日志");
                            intent.putExtra(Intent.EXTRA_SUBJECT, "日志");
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            intent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(activity, sProviderAuthority, zipFile));
                            activity.startActivity(Intent.createChooser(intent, "发送日志"));
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    sMainHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(activity, "日志发送出错", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    public static void clearOlderLogs(int daysAgo) {
        File logFileDir = getLogFileDir();
        File[] logFiles = logFileDir.listFiles();
        if (null == logFiles) {
            return;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 1970);
        final long deadLine = System.currentTimeMillis() - daysAgo * 24 * 60 * 60 * 1000;
        for (File logFile : logFiles) {
            long lastModified = logFile.lastModified();
            if (lastModified < deadLine) {
                logFile.delete();
            }
        }
    }

    public Logger(String tag) {
        this.tag = tag;
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public boolean isDebug() {
        return debug;
    }

    public abstract void save();

    public abstract void d(String msg);

    public abstract void d(String msg, Throwable e);

    public abstract void i(String msg);

    public abstract void i(String msg, Throwable e);

    public abstract void w(String msg);

    public abstract void w(String msg, Throwable e);

    public abstract void e(String msg);

    public abstract void e(String msg, Throwable e);

    public abstract void v(String msg);

    public abstract void v(String msg, Throwable e);
}
