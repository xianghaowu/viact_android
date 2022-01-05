package com.viact.viact_android.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.channels.FileChannel;

public class FileUtils {
    private final static long KB = 1024;
    private final static long MB = KB * 1024;
    private final static long GB = MB * 1024;

    /**
     * 将文件大小转换为KB、MB、GB 等字符串
     *
     * @param b 文件字节大小
     * @return B（整数位）、KB（整数位）、MB（四舍五入，两位精确度）、GB（四舍五入，两位精确度）
     */
    public static String computeFileSize(long b) {
        float size;
        BigDecimal decimal;

        if (b / GB >= 1) {
            size = b / (float) GB;
            decimal = new BigDecimal(size);
            return decimal.setScale(2, RoundingMode.HALF_UP).floatValue() + "G";
        } else if (b / MB >= 1) {
            size = b / (float) MB;
            decimal = new BigDecimal(size);
            return decimal.setScale(2, RoundingMode.HALF_UP).floatValue() + "M";
        } else if (b / KB >= 1) {
            size = b / (float) KB;
            decimal = new BigDecimal(size);
            return decimal.setScale(0, RoundingMode.HALF_UP).intValue() + "K";
        } else {
            return b + "B";
        }
    }

    /**
     * 将文件大小由KB、MB、GB 等字符串转换为long值
     *
     * @param fileSize 文件大小字符串
     * @return 文件大小
     */
    public static long parseFileSize(String fileSize) {
        try {
            double size = Double.parseDouble(fileSize.replaceAll("[^-*\\d+(\\.)?]", "")); // 提取数字
            if (fileSize.toUpperCase().contains("G")) {
                size *= 1024 * 1024 * 1024;
            } else if (fileSize.toUpperCase().contains("M")) {
                size *= 1024 * 1024;
            } else if (fileSize.toUpperCase().contains("K")) {
                size *= 1024;
            }
            return (long) size;
        } catch (NumberFormatException ignore) {
            return 0;
        }
    }

    public static void copyFile(File sourceFile, File destFile) throws IOException {
        if (!destFile.getParentFile().exists())
            destFile.getParentFile().mkdirs();

        if (!destFile.exists()) {
            try {
                destFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        FileChannel source = null;
        FileChannel destination = null;

        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }
}
