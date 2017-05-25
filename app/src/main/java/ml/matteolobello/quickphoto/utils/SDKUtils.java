package ml.matteolobello.quickphoto.utils;

import android.os.Build;

public class SDKUtils {

    /**
     * Check if we're running at least Android 7.0 Nougat
     */
    public static final boolean AT_LEAST_NOUGAT = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1;

    /**
     * Check if we're running at least Android 6.0 Marshmallow
     */
    public static final boolean AT_LEAST_MARSHMALLOW = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
}
