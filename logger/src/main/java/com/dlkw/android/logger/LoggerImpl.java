package com.dlkw.android.logger;

import android.util.Log;
import android.util.SparseArray;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

class LoggerImpl extends Logger {
    private final ByteArrayOutputStream bos = new ByteArrayOutputStream();
    private final PrintStream printStream = new PrintStream(bos);
    private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private final DateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    private static final SparseArray<String> logLevels = new SparseArray<>();

    static {
        logLevels.put(Log.DEBUG, "DEBUG");
        logLevels.put(Log.INFO, "INFO");
        logLevels.put(Log.WARN, "WARN");
        logLevels.put(Log.ERROR, "ERROR");
        logLevels.put(Log.VERBOSE, "VERBOSE");
        logLevels.put(Log.ASSERT, "ASSERT");
    }

    public LoggerImpl(String tag) {
        super(tag);
    }

    @Override
    public void save() {
        writeMessageToFile(null, null, true);
    }

    @Override
    public void d(String msg) {
        String message = makeLogMsg(Log.DEBUG, msg);
        if (isDebug()) {
            Log.d(tag, message);
        }
        writeMessageToFile(message, null);
    }

    @Override
    public void d(String msg, Throwable e) {
        String message = makeLogMsg(Log.DEBUG, msg);
        if (isDebug()) {
            Log.d(tag, message, e);
        }
        writeMessageToFile(message, e);
    }

    @Override
    public void i(String msg) {
        String message = makeLogMsg(Log.INFO, msg);
        if (isDebug()) {
            Log.i(tag, message);
        }
        writeMessageToFile(message, null);
    }

    @Override
    public void i(String msg, Throwable e) {
        String message = makeLogMsg(Log.INFO, msg);
        if (isDebug()) {
            Log.i(tag, message, e);
        }
        writeMessageToFile(message, e);
    }

    @Override
    public void w(String msg) {
        String message = makeLogMsg(Log.WARN, msg);
        if (isDebug()) {
            Log.w(tag, message);
        }
        writeMessageToFile(message, null);
    }

    @Override
    public void w(String msg, Throwable e) {
        String message = makeLogMsg(Log.WARN, msg);
        if (isDebug()) {
            Log.w(tag, message, e);
        }
        writeMessageToFile(message, e);
    }

    @Override
    public void e(String msg) {
        String message = makeLogMsg(Log.ERROR, msg);
        if (isDebug()) {
            Log.e(tag, message);
        }
        writeMessageToFile(message, null);
    }

    @Override
    public void e(String msg, Throwable e) {
        String message = makeLogMsg(Log.ERROR, msg);
        if (isDebug()) {
            Log.e(tag, message, e);
        }
        writeMessageToFile(message, e);
    }

    @Override
    public void v(String msg) {
        String message = makeLogMsg(Log.VERBOSE, msg);
        if (isDebug()) {
            Log.v(tag, message);
        }
        writeMessageToFile(message, null);
    }

    @Override
    public void v(String msg, Throwable e) {
        String message = makeLogMsg(Log.VERBOSE, msg);
        if (isDebug()) {
            Log.v(tag, message, e);
        }
        writeMessageToFile(message, e);
    }

    private String makeLogMsg(int level, String msg) {
        String s = dateTimeFormat.format(new Date());
        s += " " + logLevels.get(level) + " " + tag + " " + sMessageInterceptor.intercept(msg);
        return s;
    }

    private void writeMessageToFile(String message, Throwable e) {
        writeMessageToFile(message, e, false);
    }

    private void writeMessageToFile(String message, Throwable e, boolean forceSave) {
        try {
            if (null != message) {
                printStream.print(message);
            }
            if (null != e) {
                printStream.println();
                e.printStackTrace(printStream);
            }
            if (bos.size() > 0 && (forceSave || bos.size() > 1024)) {
                String fileName = dateFormat.format(new Date()) + "_" + tag + ".log";
                File logFile = new File(getLogFileDir(), fileName);
                if (!logFile.getParentFile().exists()) {
                    logFile.getParentFile().mkdirs();
                }
                if (!logFile.exists()) {
                    logFile.createNewFile();
                }
                FileOutputStream fos = new FileOutputStream(logFile, true);
                fos.write(bos.toByteArray());
                fos.close();
                bos.reset();
            }
        } catch (IOException e1) {
            Log.e("Logger", "writeMessageToFile error", e1);
        }
    }
}
