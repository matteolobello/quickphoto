package ml.matteolobello.quickphoto.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import ml.matteolobello.quickphoto.R;
import ml.matteolobello.quickphoto.pojo.Photo;
import ml.matteolobello.quickphoto.utils.BitmapUtils;
import ml.matteolobello.quickphoto.utils.SDKUtils;
import ml.matteolobello.quickphoto.utils.ShortcutHelper;

public class MainActivity extends AppCompatActivity {

    /**
     * Result code used when picking the images.
     */
    private static final int PICK_IMAGES_RESULT_CODE = 505;

    /**
     * The Views.
     */
    private Toolbar mToolbar;
    private ImageView mPreviewImageView;
    private TextInputEditText mEditText;
    private TextInputLayout mTextInputLayout;
    private View mSelectPhotoView;
    private Button mApplyButton;

    /**
     * The current selected Photo object.
     */
    private Photo mSelectedPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViews();
        initViews();
    }

    @SuppressWarnings("all")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGES_RESULT_CODE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();

            // Add permissions as the Uri we got will be temporary with API19+
            int takeFlags = data.getFlags() & Intent.FLAG_GRANT_READ_URI_PERMISSION;
            getApplicationContext().getContentResolver().takePersistableUriPermission(uri, takeFlags);

            Bitmap originalPreview = BitmapUtils.getBitmapFromUri(getApplicationContext(), uri);

            int originalPreviewWidth = originalPreview.getWidth();
            int originalPreviewHeight = originalPreview.getHeight();

            // Optimize Bitmap
            // originalPreviewWidth : originalPreviewHeight = x : mPreviewImageViewHeight
            mPreviewImageView.setImageBitmap(BitmapUtils.scaleCenterCrop(originalPreview,
                    originalPreviewWidth * originalPreview.getHeight() / originalPreviewHeight,
                    mPreviewImageView.getHeight()));

            mApplyButton.setAlpha(1.0f);
            mApplyButton.setTextColor(Color.WHITE);
            mApplyButton.setBackground(new ColorDrawable(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent)));

            mSelectedPhoto = new Photo(uri);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (SDKUtils.AT_LEAST_NOUGAT) {
            getMenuInflater().inflate(R.menu.menu_main, menu);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_reset:
                if (!ShortcutHelper.get(MainActivity.this).hasShortcuts()) {
                    Toast.makeText(this, R.string.add_at_least_one_shortcut_please, Toast.LENGTH_SHORT).show();

                    return super.onOptionsItemSelected(item);
                }

                new AlertDialog.Builder(this)
                        .setTitle(R.string.warning)
                        .setMessage(R.string.shortcuts_remove_message)
                        .setNegativeButton(R.string.dismiss, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ShortcutHelper.get(MainActivity.this).removeShortcuts();

                                Toast.makeText(MainActivity.this, R.string.done, Toast.LENGTH_SHORT).show();
                            }
                        })
                        .show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Find all the layout Views we need to use.
     */
    private void findViews() {
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mPreviewImageView = (ImageView) findViewById(R.id.selected_image_image_view);
        mEditText = (TextInputEditText) findViewById(R.id.edit_text);
        mTextInputLayout = (TextInputLayout) findViewById(R.id.text_input_layout);
        mSelectPhotoView = findViewById(R.id.select_photo);
        mApplyButton = (Button) findViewById(R.id.apply);
    }

    /**
     * After finding Views, let's initialize them.
     */
    private void initViews() {
        setSupportActionBar(mToolbar);

        mTextInputLayout.setActivated(false);

        mSelectPhotoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPickingPhotos();
            }
        });

        mApplyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSelectedPhoto == null) {
                    Toast.makeText(MainActivity.this, R.string.select_photo_first_please, Toast.LENGTH_SHORT).show();

                    return;
                }

                String inputText = mEditText.getText().toString();
                if (TextUtils.isEmpty(inputText)) {
                    Toast.makeText(MainActivity.this, R.string.insert_name_first_please, Toast.LENGTH_SHORT).show();

                    mTextInputLayout.setError(getString(R.string.insert_name_first_please));

                    return;
                }

                mSelectedPhoto = new Photo(inputText, mSelectedPhoto.getUri());

                ShortcutHelper.get(MainActivity.this).addHomeScreenShortcut(mSelectedPhoto);
            }
        });
    }

    /**
     * Launch default Document app to pick photos.
     */
    private void startPickingPhotos() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false);
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.pick_photos)), PICK_IMAGES_RESULT_CODE);
    }
}
