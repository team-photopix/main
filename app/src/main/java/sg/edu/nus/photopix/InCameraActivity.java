package sg.edu.nus.photopix;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.utils.StorageUtils;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;


public class InCameraActivity extends ActionBarActivity {

    private Uri uri;
    private ImageView image;
    private ImageLoader imageLoader;
    private String dir;

    private static final int CAPTURE_IMAGE_ACTIVITY_REQ = 1377;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_show);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        image = (ImageView) findViewById(R.id.image);
        imageLoader = ImageLoader.getInstance();
        if (!imageLoader.isInited())
            initImageLoader();

        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        uri = Uri.fromFile(getOutputPhotoFile());
        i.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(i, CAPTURE_IMAGE_ACTIVITY_REQ );
    }

    public void onBackToActivityClicked(View v) {
        Intent i = new Intent(this, InCameraActivity.class);
        startActivity(i);
        finish();
    }

    public void onHomeClicked(View v) {
        finish();
    }

    public void onEditClicked(View v) {
        Intent i = new Intent(this, EditActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("LOCATION", dir);
        i.putExtras(bundle);
        startActivity(i);
        finish();
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case CAPTURE_IMAGE_ACTIVITY_REQ:
                if (resultCode == RESULT_OK) {
                    Uri photoUri = null;
                    if (data == null) {
                        Toast.makeText(this, "Photo saved successfully",
                                Toast.LENGTH_LONG).show();
                        photoUri = uri;
                    } else {
                        photoUri = data.getData();
                        Toast.makeText(this, "Photo saved successfully in: " + data.getData(),
                                Toast.LENGTH_LONG).show();
                    }
                    showPhoto(photoUri);
                } else if (resultCode == RESULT_CANCELED) {
                    Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Callout for image capture failed!",
                            Toast.LENGTH_LONG).show();
                    finish();
                }
                break;

            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    private void showPhoto(Uri photoUri) {
        //image.setImageBitmap(ExifUtil.rotateBitmap(
                //dir, BitmapUtil.decodeSampledBitmapFromResource(dir, 256, 256)));
        imageLoader.displayImage(photoUri.toString(), image);
    }

    @Override
    public void onResume() {
        super.onResume();
        initImageLoader();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private File getOutputPhotoFile() {
        File directory = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "Photopix");
        if (!directory.exists()) {
            if (!directory.mkdirs()) {
                return null;
            }
        }
        String timeStamp = new SimpleDateFormat("yyyMMdd_HHmmss").format(new Date());
        dir = directory.getPath() + File.separator + "IMG_" + timeStamp + ".jpg";
        return new File(dir);
    }

    private void initImageLoader() {
        try {
            String CACHE_DIR = Environment.getExternalStorageDirectory()
                    .getAbsolutePath() + "/.temp_tmp";
            new File(CACHE_DIR).mkdirs();

            File cacheDir = StorageUtils.getOwnCacheDirectory(getBaseContext(),
                    CACHE_DIR);

            DisplayImageOptions defaultOptions = new DisplayImageOptions.Builder()
                    .cacheOnDisc(true).cacheInMemory(true)
                    .imageScaleType(ImageScaleType.EXACTLY)
                    .considerExifParams(true)
                    .bitmapConfig(Bitmap.Config.RGB_565).build();

            ImageLoaderConfiguration.Builder builder = new ImageLoaderConfiguration.Builder(
                    getBaseContext())
                    .defaultDisplayImageOptions(defaultOptions)
                    .discCache(new UnlimitedDiscCache(cacheDir))
                    .memoryCache(new WeakMemoryCache());

            ImageLoaderConfiguration config = builder.build();
            imageLoader = ImageLoader.getInstance();
            imageLoader.init(config);

        } catch (Exception e) {

        }
    }
}
