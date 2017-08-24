package com.constellio.app.utils;

import java.text.DecimalFormat;

import static com.constellio.app.ui.i18n.i18n.$;

/**
 * Credit @Mr Ed on stackOverFlow
 * @link https://stackoverflow.com/users/699240/mr-ed
 * @Link https://stackoverflow.com/a/5599842/5784924
 */
public class FileLengthUtils {

    public static String readableFileSize(long size) {
        if(size <= 0) return "0";
        final String[] units = new String[] {
                $("FileLengthUtils.bytes"),
                $("FileLengthUtils.kiloBytes"),
                $("FileLengthUtils.MegaBytes"),
                $("FileLengthUtils.GigaBytes"),
                $("FileLengthUtils.TerraBytes")
        };
        int digitGroups = (int) (Math.log10(size)/Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size/Math.pow(1024, digitGroups)) + " " + units[digitGroups] + " [ " + size + " ] ";
    }
}
