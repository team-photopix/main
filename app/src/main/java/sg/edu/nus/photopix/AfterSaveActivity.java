package sg.edu.nus.photopix;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.os.ResultReceiver;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.utils.StorageUtils;

import java.io.File;

public class AfterSaveActivity extends ActionBarActivity {

    private ImageLoader imageLoader;
    private Uri incomingUri;
    private ImageView image;
    private Button pickBtn, homeBtn;
    private ImageButton imageBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_show);

        Bundle bundle = getIntent().getExtras();
        incomingUri = null;

        if (bundle != null) {
            incomingUri = bundle.getParcelable("URI");

        }

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        image = (ImageView)findViewById(R.id.image);
        pickBtn = (Button)findViewById(R.id.button2);
        homeBtn = (Button)findViewById(R.id.button3);
        imageBtn = (ImageButton)findViewById(R.id.imageButton2);
        imageBtn.setVisibility(View.GONE);

        pickBtn.setText("Pick Another Photo");
        homeBtn.setText("Home");

        imageLoader = ImageLoader.getInstance();
        if (!imageLoader.isInited())
            initImageLoader();

        if (bundle != null) {
            imageLoader.displayImage(incomingUri.toString(), image);
        }
        else finish();
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

    public void onBackToActivityClicked(View v) {
        Intent i = new Intent(this, EditActivity.class);
        startActivity(i);
        ((ResultReceiver)getIntent().getParcelableExtra("finisher")).send(10, new Bundle());
        finish();
    }

    public void onEditClicked(View v) {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        ((ResultReceiver)getIntent().getParcelableExtra("finisher")).send(5, new Bundle());
        finish();
    }

    @Override
    public void onResume() {
        super.onResume();
        initImageLoader();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        clear();
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

    private void clear() {
        image = null;
        pickBtn = null;
        homeBtn = null;
        imageLoader.destroy();
    }
}

