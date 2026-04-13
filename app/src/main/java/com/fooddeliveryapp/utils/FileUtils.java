package com.fooddeliveryapp.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class FileUtils {

    public static File copyUriToCacheFile(Context context, Uri uri, String fallbackName) throws Exception {
        if (uri == null) return null;

        String name = queryDisplayName(context, uri);
        if (name == null || name.trim().isEmpty()) name = fallbackName != null ? fallbackName : "upload";

        File outFile = new File(context.getCacheDir(), System.currentTimeMillis() + "-" + name);
        try (InputStream is = context.getContentResolver().openInputStream(uri);
             FileOutputStream os = new FileOutputStream(outFile)) {
            if (is == null) throw new IllegalStateException("Cannot open input stream");
            byte[] buf = new byte[8192];
            int read;
            while ((read = is.read(buf)) != -1) {
                os.write(buf, 0, read);
            }
        }
        return outFile;
    }

    private static String queryDisplayName(Context context, Uri uri) {
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int idx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (idx >= 0) return cursor.getString(idx);
            }
        } catch (Exception ignored) {
        } finally {
            if (cursor != null) cursor.close();
        }
        return null;
    }
}

