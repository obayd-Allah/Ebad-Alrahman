package com.ebadalrahman.app;

import android.content.Context;
import java.io.File;

public class AudioConfig {

    /*
     * عندما يصبح عندنا السيرفر الخاص بالتسجيلات
     * سنضع الرابط الأساسي هنا فقط.
     *
     * مثال:
     *
     * https://example.com/quran/
     */

    public static final String BASE_URL =
            "https://YOUR-AUDIO-SERVER.com/quran/";

    public static String getReciterFolder(String reciter) {

        switch (reciter) {

            case "إبراهيم":
                return "ibrahim";

            case "زياد":
                return "ziad";

            case "أسامة":
                return "osama";

            default:
                return "ibrahim";
        }
    }

    public static String getAyahUrl(
            String reciter,
            int surah,
            int ayah
    ) {

        String folder = getReciterFolder(reciter);

        return BASE_URL
                + folder
                + "/"
                + String.format("%03d", surah)
                + "/"
                + String.format("%03d", ayah)
                + ".mp3";
    }

    public static String getBasmalaUrl(String reciter) {

        String folder = getReciterFolder(reciter);

        return BASE_URL
                + folder
                + "/basmala.mp3";
    }

    public static File getLocalAyahFile(
            Context context,
            String reciter,
            int surah,
            int ayah
    ) {

        File folder = new File(
                context.getFilesDir(),
                "quran/"
                        + getReciterFolder(reciter)
                        + "/"
                        + String.format("%03d", surah)
        );

        if (!folder.exists()) {
            folder.mkdirs();
        }

        return new File(
                folder,
                String.format("%03d", ayah) + ".mp3"
        );
    }

    public static File getLocalBasmalaFile(
            Context context,
            String reciter
    ) {

        File folder = new File(
                context.getFilesDir(),
                "quran/"
                        + getReciterFolder(reciter)
        );

        if (!folder.exists()) {
            folder.mkdirs();
        }

        return new File(folder, "basmala.mp3");
    }
}
