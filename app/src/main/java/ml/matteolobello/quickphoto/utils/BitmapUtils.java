package ml.matteolobello.quickphoto.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.IOException;

public class BitmapUtils {

    /**
     * Create a round version of the given Bitmap.
     *
     * @param bitmap The Bitmap.
     * @return The Rounded version of the Bitmap.
     */
    public static Bitmap getRoundedBitmap(Bitmap bitmap) {
        Bitmap output;

        output = bitmap.getWidth() > bitmap.getHeight()
                ? Bitmap.createBitmap(bitmap.getHeight(), bitmap.getHeight(), Bitmap.Config.ARGB_8888)
                : Bitmap.createBitmap(bitmap.getWidth(), bitmap.getWidth(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        float radius = 0;

        if (bitmap.getWidth() > bitmap.getHeight()) {
            radius = bitmap.getHeight() / 2;
        } else {
            radius = bitmap.getWidth() / 2;
        }

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawCircle(radius, radius, radius, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    /**
     * Get a resized and "scaleCentered" version of the given Bitmap.
     *
     * @param bitmap    The Bitmap.
     * @param newHeight The height.
     * @param newWidth  The Width.
     * @return The resized and "scaleCentered" version of the Bitmap.
     */
    public static Bitmap scaleCenterCrop(Bitmap bitmap, int newHeight, int newWidth) {
        int sourceWidth = bitmap.getWidth();
        int sourceHeight = bitmap.getHeight();

        float xScale = (float) newWidth / sourceWidth;
        float yScale = (float) newHeight / sourceHeight;
        float scale = Math.max(xScale, yScale);

        float scaledWidth = scale * sourceWidth;
        float scaledHeight = scale * sourceHeight;

        float left = (newWidth - scaledWidth) / 2;
        float top = (newHeight - scaledHeight) / 2;

        RectF targetRect = new RectF(left, top, left + scaledWidth, top + scaledHeight);

        Bitmap dest = Bitmap.createBitmap(newWidth, newHeight, bitmap.getConfig());
        Canvas canvas = new Canvas(dest);
        canvas.drawBitmap(bitmap, null, targetRect, null);

        return dest;
    }

    public static Bitmap getBitmapFromUri(Context context, Uri uri) {
        try {
            return MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}
