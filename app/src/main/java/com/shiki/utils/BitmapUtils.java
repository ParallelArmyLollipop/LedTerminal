package com.shiki.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory.Options;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Maik on 2016/3/1.
 */
public class BitmapUtils {
    public BitmapUtils() {
    }

    public static byte[] bitmapToByte(Bitmap b) {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        b.compress(CompressFormat.PNG, 100, o);
        return o.toByteArray();
    }

    public static Bitmap byteToBitmap(byte[] b) {
        return b != null && b.length != 0 ? BitmapFactory.decodeByteArray(b, 0, b.length) : null;
    }

    public static String bitmapToString(Bitmap bitmap) {
        return Base64.encodeToString(bitmapToByte(bitmap), 0);
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        return drawable == null ? null : ((BitmapDrawable) drawable).getBitmap();
    }

    public static Drawable bitmapToDrawable(Bitmap bitmap) {
        return bitmap == null ? null : new BitmapDrawable(bitmap);
    }

    public static Bitmap scaleImageTo(Bitmap org, int newWidth, int newHeight) {
        return scaleImage(org, (float) newWidth / (float) org.getWidth(), (float) newHeight / (float) org.getHeight());
    }

    public static Bitmap scaleImage(Bitmap src, float scaleWidth, float scaleHeight) {
        if (src == null) {
            return null;
        } else {
            Matrix matrix = new Matrix();
            matrix.postScale(scaleWidth, scaleHeight);
            return Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
        }
    }

    public static Bitmap toRoundCorner(Bitmap bitmap) {
        int height = bitmap.getHeight();
        int width = bitmap.getHeight();
        Bitmap output = Bitmap.createBitmap(width, height, Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Paint paint = new Paint();
        Rect rect = new Rect(0, 0, width, height);
        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(0);
        canvas.drawCircle((float) (width / 2), (float) (height / 2), (float) (width / 2), paint);
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }

    public static Bitmap createBitmapThumbnail(Bitmap bitmap, boolean needRecycle, int newHeight, int newWidth) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float scaleWidth = (float) newWidth / (float) width;
        float scaleHeight = (float) newHeight / (float) height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap newBitMap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        if (needRecycle) {
            bitmap.recycle();
        }

        return newBitMap;
    }

    public static void saveBitmap(Bitmap bitmap, File target) {
        if (target.exists()) {
            target.delete();
        }

        try {
            FileOutputStream e = new FileOutputStream(target);
            bitmap.compress(CompressFormat.JPEG, 100, e);
            e.flush();
            e.close();
        } catch (Exception var3) {
            var3.printStackTrace();
        }

    }

    public static void saveBitmap(Bitmap bitmap, int quality, File target) {
        if (target.exists()) {
            target.delete();
        }

        try {
            FileOutputStream e = new FileOutputStream(target);
            bitmap.compress(CompressFormat.JPEG, quality, e);
            e.flush();
            e.close();
        } catch (Exception var4) {
            var4.printStackTrace();
        }

    }

    public static Bitmap compressBitmap(Bitmap bitmap, boolean needRecycle, long maxSize) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(CompressFormat.JPEG, 100, baos);

        for (int options = 100; (long) baos.toByteArray().length > maxSize; options -= 10) {
            baos.reset();
            bitmap.compress(CompressFormat.JPEG, options, baos);
        }

        ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
        Bitmap bm = BitmapFactory.decodeStream(isBm, (Rect) null, (Options) null);
        if (needRecycle) {
            bitmap.recycle();
        }

        return bm;
    }

    public static Bitmap compressBitmap(Bitmap bitmap, boolean needRecycle, int targetWidth, int targeHeight) {
        float sourceWidth = (float) bitmap.getWidth();
        float sourceHeight = (float) bitmap.getHeight();
        float scaleWidth = (float) targetWidth / sourceWidth;
        float scaleHeight = (float) targeHeight / sourceHeight;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap bm = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        if (needRecycle) {
            bitmap.recycle();
        }

        return bm;
    }

    public static Bitmap compressBitmap(String imageFile, boolean qualityCompress, long maxSize, int targetWidth, int targeHeight) {
        return compress(imageFile, (String) null, false, qualityCompress, maxSize, targetWidth, targeHeight);
    }

    private static Bitmap compress(String imageFile, String targetFile, boolean isSave, boolean qualityCompress, long maxSize, int targetWidth, int targeHeight) {
        Options options = new Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imageFile, options);
        int sourceWidth = options.outWidth;
        int sourceHeight = options.outHeight;
        options.inJustDecodeBounds = false;

        int inSampleSize;
        for (inSampleSize = 1; sourceWidth / inSampleSize > targetWidth; ++inSampleSize) {
            ;
        }

        while (sourceHeight / inSampleSize > targeHeight) {
            ++inSampleSize;
        }

        if (inSampleSize <= 0) {
            inSampleSize = 1;
        }

        options.inSampleSize = inSampleSize;
        Bitmap bitmap = BitmapFactory.decodeFile(imageFile, options);
        bitmap = compressBitmap(bitmap, false, targetWidth, targeHeight);
        if (qualityCompress) {
            bitmap = compressBitmap(bitmap, true, maxSize);
        }

        if (isSave) {
            String savePath = imageFile;
            if (!StringUtils.isEmpty(targetFile)) {
                savePath = targetFile;
            }

            saveBitmap(bitmap, new File(savePath));
        }

        return bitmap;
    }

    public static void compressImage(String imageFile, String targetFile, boolean qualityCompress, long maxSize, int targetWidth, int targeHeight) {
        Bitmap bitmap = compress(imageFile, targetFile, true, qualityCompress, maxSize, targetWidth, targeHeight);
        bitmap.recycle();
    }

    public static void compressImage(String imageFile, boolean qualityCompress, long maxSize, int targetWidth, int targeHeight) {
        compressImage(imageFile, (String) null, qualityCompress, maxSize, targetWidth, targeHeight);
    }

    public static void compressImage(String imageFile, int targetWidth, int targeHeight) {
        compressImage(imageFile, (String) null, false, 0L, targetWidth, targeHeight);
    }

    public static Bitmap compressBitmap(String imageFile, int targetWidth, int targeHeight) {
        return compressBitmap(imageFile, false, 0L, targetWidth, targeHeight);
    }

    public static void compressImageSmall(String imageFile, int scale) {
        Options options = new Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imageFile, options);
        int targetWidth = options.outWidth / scale;
        int targeHeight = options.outHeight / scale;
        compressImage(imageFile, targetWidth, targeHeight);
    }

    public static Bitmap compressBitmapSmall(String imageFile, int scale) {
        Options options = new Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imageFile, options);
        int targetWidth = options.outWidth / scale;
        int targeHeight = options.outHeight / scale;
        return compressBitmap(imageFile, targetWidth, targeHeight);
    }

    public static void compressImageBig(String imageFile, int scale) {
        Options options = new Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imageFile, options);
        int targetWidth = options.outWidth * scale;
        int targeHeight = options.outHeight * scale;
        compressImage(imageFile, targetWidth, targeHeight);
    }

    public static Bitmap compressBitmapBig(String imageFile, int scale) {
        Options options = new Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imageFile, options);
        int targetWidth = options.outWidth * scale;
        int targeHeight = options.outHeight * scale;
        return compressBitmap(imageFile, targetWidth, targeHeight);
    }

    public static void compressImage(String imageFile, String targetFile, boolean qualityCompress, long maxSize) {
        Options options = new Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imageFile, options);
        int targetWidth = options.outWidth / 2;
        int targeHeight = options.outHeight / 2;
        compressImage(imageFile, targetFile, qualityCompress, maxSize, targetWidth, targeHeight);
    }

    public static void compressImage(String imageFile, boolean qualityCompress, long maxSize) {
        compressImage(imageFile, (String) null, qualityCompress, maxSize);
    }

    public static Bitmap compressBitmap(String imageFile, boolean qualityCompress, long maxSize) {
        Options options = new Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imageFile, options);
        int targetWidth = options.outWidth / 2;
        int targeHeight = options.outHeight / 2;
        return compressBitmap(imageFile, qualityCompress, maxSize, targetWidth, targeHeight);
    }

    public static void compressImage(String imageFile, long maxSize) {
        compressImage(imageFile, true, maxSize);
    }

    public static Bitmap compressBimap(String imageFile, long maxSize) {
        return compressBitmap(imageFile, true, maxSize);
    }

    public static void compressImage(String imageFile) {
        compressImage(imageFile, true, 1048576L);
    }

    public static Bitmap compressBitmap(String imageFile) {
        return compressBitmap(imageFile, true, 1048576L);
    }

    public static Bitmap rotateBitmap(Bitmap bitmap, int degress, boolean needRecycle) {
        Matrix m = new Matrix();
        m.postRotate((float) degress);
        Bitmap bm = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), m, true);
        if (needRecycle) {
            bitmap.recycle();
        }

        return bm;
    }

    public static final int getDegress(String path) {
        short degree = 0;

        try {
            ExifInterface e = new ExifInterface(path);
            int orientation = e.getAttributeInt("Orientation", 1);
            switch (orientation) {
                case 3:
                    degree = 180;
                    break;
                case 6:
                    degree = 90;
                    break;
                case 8:
                    degree = 270;
            }
        } catch (IOException var4) {
            var4.printStackTrace();
        }

        return degree;
    }
}