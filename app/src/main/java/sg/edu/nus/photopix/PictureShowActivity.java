package sg.edu.nus.photopix;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.ResultReceiver;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import java.io.File;


public class PictureShowActivity extends ActionBarActivity {

    static File pictureFile;
    static Uri uri;
    private ImageView image;
    private Bitmap bitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_show);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        image = (ImageView) findViewById(R.id.image);
        System.out.println(pictureFile.getAbsolutePath());
        bitmap = ExifUtil.rotateBitmap(uri.toString(),
                BitmapFactory.decodeFile(pictureFile.getAbsolutePath()));
        image.setImageBitmap(bitmap);
    }

    public void onBackToActivityClicked(View v) {
        finish();
    }

    public void onHomeClicked(View v) {
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
        ((ResultReceiver)getIntent().getParcelableExtra("finisher")).send(5, new Bundle());
        finish();
    }

    public void onEditClicked(View v) {
        Intent i = new Intent(this, EditActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable("URI", uri);
        i.putExtras(bundle);
        startActivity(i);
        ((ResultReceiver)getIntent().getParcelableExtra("finisher")).send(5, new Bundle());
        finish();
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
        bitmap = null;
        pictureFile = null;
        uri = null;
    }
}
