package sg.edu.nus.photopix;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FreeStyleActivity extends Activity {

    private Bitmap bMap;
    private int[] bg = {R.drawable.bg1, R.drawable.bg2, R.drawable.bg3, R.drawable.bg4, R.drawable.bg5,
            R.drawable.bg6, R.drawable.bg7, R.drawable.bg8, R.drawable.bg9};
    private HorizontalScrollView hsv;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.free_style);

        imageView = (ImageView) findViewById(R.id.collageBgView);

        findViewById(R.id.collageBgView).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                return true;
            }
        });

        hsv = (HorizontalScrollView)findViewById(R.id.hsv);

        for (int i = 0; i < 9; i++) {
            ((ImageView)((LinearLayout)((LinearLayout)hsv.getChildAt(0)).getChildAt(i))
                    .getChildAt(0)).setImageBitmap(Bitmap.createScaledBitmap(
                    BitmapFactory.decodeResource(getResources(), bg[i]), 50, 50, false
            ));
        }
        final RelativeLayout layout = (RelativeLayout) findViewById(R.id.freeStyleLayout);

        for(int i=0; i<MultiPickActivity.totalImage; i++){

            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.topMargin=20+(30*i);
            params.leftMargin=20+(20*i);

            BorderImage borderImage = new BorderImage(FreeStyleActivity.this);
            borderImage.setLayoutParams(params);
            borderImage.setAdjustViewBounds(true);
            borderImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
            borderImage.setOnTouchListener(new TouchListener());

            bMap = BitmapUtil.decodeSampledBitmapFromResource(ImageCollector.getPath(i), 256,256);
            bMap = ExifUtil.rotateBitmap(ImageCollector.getPath(i), bMap);
            borderImage.setImageBitmap(Bitmap.createScaledBitmap(bMap, bMap.getWidth(), bMap.getHeight(), false));

            layout.addView(borderImage);
            if(bMap!=null){  bMap=null; }
        }

        final Button saveBtn = (Button) findViewById(R.id.saveFreeBtn);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layout.setDrawingCacheEnabled(true);
                Bitmap bitmap = layout.getDrawingCache();

                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes);

                SavedImage.saveImage(bitmap);

                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

                String dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath();

                File f1 = new File(dir+ "/PhotoPix");
                f1.mkdir();
                String fileName = timeStamp + ".jpg";
                File f2 = new File (f1, fileName);
                try {
                    FileOutputStream fo1 = new FileOutputStream(f2);
                    fo1.write(bytes.toByteArray());
                    fo1.close();
                    Uri contentUri = Uri.fromFile(f2);

                    Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                    scanIntent.setData(contentUri);
                    getApplicationContext().sendBroadcast(scanIntent);
                    Toast.makeText(getApplicationContext(), "Saved", Toast.LENGTH_LONG).show();

                    Intent intent;
                    intent = new Intent (getApplicationContext(),DisplayActivity.class);
                    startActivity(intent);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if(bitmap!=null){  bitmap=null; }
            }
        });
    }
    public void onClick(View v) {

            switch (v.getId()){
                case R.id.white:
                    imageView.setImageResource(0);
                    imageView.setBackgroundColor(getResources().getColor(R.color.white));
                    break;
                case R.id.moccasin:
                    imageView.setImageResource(0);
                    imageView.setBackgroundColor(getResources().getColor(R.color.moccasin));
                    break;
                case R.id.light_pink:
                    imageView.setImageResource(0);
                    imageView.setBackgroundColor(getResources().getColor(R.color.light_pink));
                    break;
                case R.id.light_yellow:
                    imageView.setImageResource(0);
                    imageView.setBackgroundColor(getResources().getColor(R.color.light_yellow));
                    break;
                case R.id.yellow:
                    imageView.setImageResource(0);
                    imageView.setBackgroundColor(getResources().getColor(R.color.yellow));
                    break;
                case R.id.orange:
                    imageView.setImageResource(0);
                    imageView.setBackgroundColor(getResources().getColor(R.color.orange));
                    break;
                case R.id.light_green:
                    imageView.setImageResource(0);
                    imageView.setBackgroundColor(getResources().getColor(R.color.light_green));
                    break;
                case R.id.green:
                    imageView.setImageResource(0);
                    imageView.setBackgroundColor(getResources().getColor(R.color.green));
                    break;
                case R.id.light_blue:
                    imageView.setImageResource(0);
                    imageView.setBackgroundColor(getResources().getColor(R.color.light_blue));
                    break;
                case R.id.aqua:
                    imageView.setImageResource(0);
                    imageView.setBackgroundColor(getResources().getColor(R.color.aqua));
                    break;
                case R.id.medium_blue:
                    imageView.setImageResource(0);
                    imageView.setBackgroundColor(getResources().getColor(R.color.medium_blue));
                    break;
                case R.id.blue:
                    imageView.setImageResource(0);
                    imageView.setBackgroundColor(getResources().getColor(R.color.blue));
                    break;
                case R.id.purple:
                    imageView.setImageResource(0);
                    imageView.setBackgroundColor(getResources().getColor(R.color.purple));
                    break;
                case R.id.dark_pink:
                    imageView.setImageResource(0);
                    imageView.setBackgroundColor(getResources().getColor(R.color.dark_pink));
                    break;
                case R.id.light_red:
                    imageView.setImageResource(0);
                    imageView.setBackgroundColor(getResources().getColor(R.color.light_red));
                    break;
                case R.id.red:
                    imageView.setImageResource(0);
                    imageView.setBackgroundColor(getResources().getColor(R.color.red));
                    break;
                case R.id.dark_gray:
                    imageView.setImageResource(0);
                    imageView.setBackgroundColor(getResources().getColor(R.color.dark_gray));
                    break;
                case R.id.light_black:
                    imageView.setImageResource(0);
                    imageView.setBackgroundColor(getResources().getColor(R.color.light_black));
                    break;
                case R.id.black:
                    imageView.setImageResource(0);
                    imageView.setBackgroundColor(getResources().getColor(R.color.black));
                    break;

                case R.id.layout1:
                    imageView.setImageResource(R.drawable.bg1);
                    break;
                case R.id.layout2:
                    imageView.setImageResource(R.drawable.bg2);
                    break;
                case R.id.layout3:
                    imageView.setImageResource(R.drawable.bg3);
                    break;
                case R.id.layout4:
                    imageView.setImageResource(R.drawable.bg4);
                    break;
                case R.id.layout5:
                    imageView.setImageResource(R.drawable.bg5);
                    break;
                case R.id.layout6:
                    imageView.setImageResource(R.drawable.bg6);
                    break;
                case R.id.layout7:
                    imageView.setImageResource(R.drawable.bg7);
                    break;
                case R.id.layout8:
                    imageView.setImageResource(R.drawable.bg8);
                    break;
                case R.id.layout9:
                    imageView.setImageResource(R.drawable.bg9);
                    break;

            }




    }

    public void goBackToPhotoSelection(View view) {  finish();  }
}