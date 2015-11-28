package com.example.jack.realityguide;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.text.Html;
import android.text.Spanned;

import com.google.android.gms.maps.model.LatLng;

public class MathFunctions {
    protected static Spanned expColor(String text, int offset, double exp, int color1, int color2) {
        String result = "";
        int r1 = color1 >>> 16 & 0xFF;
        int g1 = color1 >>> 8 & 0xFF;
        int b1 = color1 & 0xFF;
        int r2 = color2 >>> 16 & 0xFF;
        int g2 = color2 >>> 8 & 0xFF;
        int b2 = color2 & 0xFF;
        double lumpWidth = text.length();
        for (int i = 0; i < text.length(); i++) {
            String currentChar = text.charAt(i) + "";
            currentChar = currentChar.replaceAll("<", "&lt;").replaceAll(">", "&gt;");
            double colorRatio = 0;
            if (i > offset && i <= offset + lumpWidth / 2) {
                colorRatio = (double) (i - offset) / (double) (lumpWidth / 2);
            } else if (i > offset + lumpWidth / 2 && i < offset + lumpWidth) {
                colorRatio = (double) (offset + lumpWidth / 2 - i) / (lumpWidth / 2) + 1;
            }
            int r = MathFunctions.expFunction(r1, r2, colorRatio, exp);
            int g = MathFunctions.expFunction(g1, g2, colorRatio, exp);
            int b = MathFunctions.expFunction(b1, b2, colorRatio, exp);
            String color = toHex((r << 16 | g << 8 | b));
            result += "<font color=#" + color + ">" + currentChar + "</font>";
        }
        return Html.fromHtml(result);
    }

    protected static int expFunction(int start, int end, double progress, double exp) {
        if (progress > 1) return end;
        if (progress < 0) return start;
        return (int) (start + (end - start) * Math.pow(progress, exp));
    }

    protected static String toHex(int color) {
        String hex = Integer.toHexString(color);
        return "000000".substring(hex.length()) + hex;
    }

    protected static float[] flipVector(float[] a) {
        float[] result = new float[3];
        result[0] = -a[0];
        result[1] = -a[1];
        result[2] = -a[2];
        return result;
    }
    protected static float[] crossProduct(float[] a, float[] b) {
        float[] result = new float[3];
        result[0] = a[1] * b[2] - a[2] * b[1];
        result[1] = a[2] * b[0] - a[0] * b[2];
        result[2] = a[0] * b[1] - a[1] * b[0];
        return result;
    }
    protected static float[] normalize(float[] vector) {
        float[] result = new float[3];
        for (int i = 0; i < vector.length; i++) {
            result[i] = vector[i] / vectorLength(vector);
        }
        return result;
    }
    protected static float vectorLength(float[] vector) {
        return (float) Math.sqrt(
                vector[0] * vector[0] +
                        vector[1] * vector[1] +
                        vector[2] * vector[2]
        );
    }

    protected static CartesianLocation getLocationFromLatLng(LatLng latLng) {
        CartesianLocation result = new CartesianLocation();
        result.z = (float) (2 * Math.PI * Settings.EARTH_RADIUS *
                (latLng.latitude - Settings.currentLat) / 360);
        result.x = (float) (2 * Math.PI * Settings.EARTH_RADIUS * Math.cos(latLng.latitude) *
                (latLng.longitude - Settings.currentLon) / 360);
        return result;
    }

    protected static double[] getProjection(CartesianLocation location) {
        double[] result = new double[2];

        return result;
    }
    protected static Bitmap testLabel(String toWrite) {
        Bitmap result = Bitmap.createBitmap(40, 40, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        canvas.drawColor(0xFF4499FF);
        Paint paint = new Paint();
        paint.setTextSize(10);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(0xFF112244);
        canvas.drawText(toWrite, 17, 20, paint);
        return result;
    }
}
