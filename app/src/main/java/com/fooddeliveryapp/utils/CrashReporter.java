package com.fooddeliveryapp.utils;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public final class CrashReporter {
    private static final String TAG = "CrashReporter";
    private static final String LAST_CRASH_FILE = "last_crash.txt";
    private static volatile boolean installed = false;

    private CrashReporter() {}

    public static synchronized void install(Context context) {
        if (installed) return;
        installed = true;
        Thread.UncaughtExceptionHandler previous = Thread.getDefaultUncaughtExceptionHandler();
        Context appContext = context.getApplicationContext();

        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            try {
                writeCrash(appContext, thread, throwable);
            } catch (Exception ignored) {
            }
            if (previous != null) {
                previous.uncaughtException(thread, throwable);
            } else {
                System.exit(10);
            }
        });
    }

    public static String consumeLastCrash(Context context) {
        try {
            File f = new File(context.getFilesDir(), LAST_CRASH_FILE);
            if (!f.exists()) return null;
            byte[] bytes = java.nio.file.Files.readAllBytes(f.toPath());
            //noinspection ResultOfMethodCallIgnored
            f.delete();
            return new String(bytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            Log.e(TAG, "Failed to read last crash", e);
            return null;
        }
    }

    private static void writeCrash(Context context, Thread thread, Throwable throwable) throws Exception {
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(new Date());
        StringBuilder sb = new StringBuilder();
        sb.append("time=").append(time).append('\n');
        sb.append("thread=").append(thread != null ? thread.getName() : "unknown").append('\n');
        sb.append(Log.getStackTraceString(throwable)).append('\n');
        File out = new File(context.getFilesDir(), LAST_CRASH_FILE);
        try (FileOutputStream fos = new FileOutputStream(out, false)) {
            fos.write(sb.toString().getBytes(StandardCharsets.UTF_8));
        }
        Log.e(TAG, "Captured fatal crash. File: " + out.getAbsolutePath() + "\n" + sb);
    }
}
