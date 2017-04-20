package ml.matteolobello.quickphoto.utils;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.AsyncTask;
import android.widget.Toast;

import java.util.Collections;
import java.util.List;

import ml.matteolobello.quickphoto.R;
import ml.matteolobello.quickphoto.pojo.Photo;

public class ShortcutHelper {

    /**
     * Google says that the maximum shortcuts number is 5, but actually maximum 4 icons are shown.
     */
    private static final int SHORTCUTS_LIMIT = 4;

    /**
     * This size could be fine for both HomeScreen shortcut and Nougat icon shortcut.
     */
    private static final int SHORTCUT_ICON_SIZE = 128;

    /**
     * The Intent INSTALL_SHORTCUT action.
     */
    private static final String INSTALL_SHORTCUT_ACTION = "com.android.launcher.action.INSTALL_SHORTCUT";

    /**
     * The Activity we're using.
     */
    private final Activity mActivity;

    /**
     * A private Constructor, so that we are forced to use the {@link #get(Activity)}.
     *
     * @param activity The Activity.
     */
    private ShortcutHelper(Activity activity) {
        mActivity = activity;
    }

    /**
     * A method to get a new Instance of this class.
     * Avoid using a singleton as we could get a memory leak.
     *
     * @param activity The Activity we're using.
     * @return A new Instance of this Class.
     */
    public static ShortcutHelper get(Activity activity) {
        return new ShortcutHelper(activity);
    }

    /**
     * A method to manage HomeScreen shortcuts and Nougat icon shortcuts.
     *
     * @param photo The Photo object.
     */
    public void addHomeScreenShortcut(final Photo photo) {
        final Intent shortcutIntent = new Intent();
        shortcutIntent.setAction(Intent.ACTION_VIEW);
        shortcutIntent.setDataAndType(Uri.parse("file://" + UriUtils.getPathFromUri(mActivity, photo.getUri())), "image/*");
        shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        shortcutIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        final Intent addIntent = new Intent();
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        addIntent.putExtra(Intent.EXTRA_SHORTCUT_NAME, photo.getName());

        new AsyncTask<Void, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Void... params) {
                final Bitmap originalPhotoBitmap = BitmapUtils.getBitmapFromUri(mActivity, photo.getUri());

                return BitmapUtils.getRoundedBitmap(
                        BitmapUtils.scaleCenterCrop(originalPhotoBitmap, SHORTCUT_ICON_SIZE, SHORTCUT_ICON_SIZE));
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                super.onPostExecute(bitmap);

                addIntent.putExtra(Intent.EXTRA_SHORTCUT_ICON, bitmap);
                addIntent.setAction(INSTALL_SHORTCUT_ACTION);

                mActivity.sendBroadcast(addIntent);

                createNougatShortcut(photo, bitmap, shortcutIntent);
            }
        }.execute();

        Toast.makeText(mActivity, mActivity.getResources().getString(R.string.shortcut_created, photo.getName()), Toast.LENGTH_SHORT).show();
    }

    /**
     * Handle Nougat icon shortcuts.
     *
     * @param photo  The Photo object.
     * @param bitmap The rounded and scaled Bitmap.
     * @param intent The Intent to launch when tapping on the shortcut.
     */
    private void createNougatShortcut(Photo photo, Bitmap bitmap, Intent intent) {
        if (SDKUtils.AT_LEAST_NOUGAT) {
            ShortcutManager shortcutManager = mActivity.getSystemService(ShortcutManager.class);

            List<ShortcutInfo> shortcutInfoList = shortcutManager.getDynamicShortcuts();

            // Remove oldest Shortcut if we passed the shortcut limit number, using the
            // milliseconds as the IDs
            int numberOfShortcuts = shortcutInfoList.size();
            if (numberOfShortcuts == SHORTCUTS_LIMIT) {
                long oldestShortcutId = 0;
                long currentShortcutMs;
                long currentMs = System.currentTimeMillis();
                for (ShortcutInfo shortcutInfo : shortcutInfoList) {
                    currentShortcutMs = Long.valueOf(shortcutInfo.getId());

                    if (currentMs - currentShortcutMs > oldestShortcutId) {
                        oldestShortcutId = currentShortcutMs;
                    }
                }

                shortcutManager.removeDynamicShortcuts(Collections.singletonList(String.valueOf(oldestShortcutId)));
            }

            long newShortcutId = System.currentTimeMillis();
            shortcutManager.addDynamicShortcuts(Collections.singletonList(
                    new ShortcutInfo.Builder(mActivity, String.valueOf(newShortcutId))
                            .setShortLabel(photo.getName())
                            .setIcon(Icon.createWithBitmap(bitmap))
                            .setIntent(intent)
                            .build()));
        }
    }

    /**
     * Remove all Nougat icon shortcuts.
     */
    public void removeShortcuts() {
        if (SDKUtils.AT_LEAST_NOUGAT) {
            mActivity.getSystemService(ShortcutManager.class).removeAllDynamicShortcuts();
        }
    }

    /**
     * A Method to check if we have already added at least one Nougat icon shortcut.
     *
     * @return true if at least 1 Nougat shortcut icon is added.
     */
    public boolean hasShortcuts() {
        return SDKUtils.AT_LEAST_NOUGAT
                && mActivity.getSystemService(ShortcutManager.class).getDynamicShortcuts().size() > 0;
    }
}
