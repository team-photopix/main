package sg.edu.nus.photopix;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Display;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import android.widget.ViewSwitcher;

import com.edmodo.cropper.CropImageView;
import com.nostra13.universalimageloader.cache.disc.impl.UnlimitedDiscCache;
import com.nostra13.universalimageloader.cache.memory.impl.WeakMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.utils.StorageUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.funnything.colorbar.ColorBarHelper;
import jp.funnything.colorbar.ColorBarView;
import jp.funnything.colorbar.OnColorChangeListener;

public class EditActivity extends ActionBarActivity implements SensorEventListener,
        View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private SensorManager mSensorManager            = null;

    private float[] gyro                            = new float[3];
    private float[] gyroMatrix                      = new float[9];
    private float[] gyroOrientation                 = new float[3];
    private float[] magnet                          = new float[3];
    private float[] accel                           = new float[3];
    private float[] accMagOrientation               = new float[3];
    private float[] fusedOrientation                = new float[3];
    private float[] rotationMatrix                  = new float[9];

    public static final float EPSILON               = 0.000000001f;
    private static final float NS2S                 = 1.0f / 1000000000.0f;
    private float timestamp;
    private boolean initState                       = true;

    public static final int TIME_CONSTANT           = 30;
    public static final float FILTER_COEFFICIENT    = 0.98f;
    private Timer fuseTimer                         = new Timer();

    public Handler mHandler;
    private ViewFlipper flip, flip_seek;

    private ImageLoader imageLoader;

    private static final int REQUEST_PICK_IMAGE     = 1;
    private ImageView mImageView, view1, view2, view3, view4, view5;
    private CropImageView cropImageView;
    private GPUImage gpuImage;
    private HorizontalScrollView hsv, hsvEffects;
    private GestureDetector myGestureDetector       = null;
    private LinearLayout edit1, edit2, edit3, edit4, edit5, effect1, effect2, effect3,
                         effect4, effect5, effect6, effect7, effect8, effect9, effect10,
            innerlayEffects, adjust, rotate, crop, frame;
    private SeekBar seekBar1;
    private SeekBar seekBar2;
    private SeekBar seekBar3;
    private SeekBar seekBar4;
    private SeekBar seekBar5;
    private LayoutParams params;

    private ActionBar actionBar;
    private Uri uri;
    private FilterUtil filterUtil;

    public static short counter                     = 0;
    private String realPath;
    private Bitmap bitmap, originBitmap;
    private Bitmap thumbnail;

    private int filter = 0;

    private FilterUtil.FilterAdjuster mFilterAdjuster;
    private GPUImageFilter gpuImageFilter;

    private SeekBar prevSeekBar = null;

    private boolean cropInProcess = false;

    private ColorBarView hue, saturation, value;
    private ColorBarHelper helper;

    private int hsvColor = Color.WHITE;
    private Paint borderPaint;
    private Canvas canvas;

    private Button saveBtn, homeBtn;
    private Button cancelBtn;

    private ViewSwitcher viewSwitcher;
    private View prevView;
    private LinearLayout linearLayout;
    private boolean comeFromCamera = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            //change this 3 lines with below 2 lines
            realPath = bundle.getString("LOCATION");
            uri = Uri.fromFile(new File(realPath));
            comeFromCamera = true;

            //for implemented camera
            //uri = bundle.getParcelable("URI");
            //comeFromCamera = true;
        }

        actionBar = getSupportActionBar();
        actionBar.hide();

        gyroOrientation[0] = 0.0f;
        gyroOrientation[1] = 0.0f;
        gyroOrientation[2] = 0.0f;

        gyroMatrix[0] = 1.0f; gyroMatrix[1] = 0.0f; gyroMatrix[2] = 0.0f;
        gyroMatrix[3] = 0.0f; gyroMatrix[4] = 1.0f; gyroMatrix[5] = 0.0f;
        gyroMatrix[6] = 0.0f; gyroMatrix[7] = 0.0f; gyroMatrix[8] = 1.0f;

        mSensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        initListeners();

        fuseTimer.scheduleAtFixedRate(new calculateFusedOrientationTask(),
                1000, TIME_CONSTANT);

        mHandler = new Handler();

        gpuImage = new GPUImage(this);
        filterUtil = new FilterUtil(getApplicationContext());

        flip = (ViewFlipper)findViewById(R.id.flip);
        flip.setInAnimation(this, android.R.anim.slide_in_left);
        flip.setOutAnimation(this, android.R.anim.slide_out_right);

        flip_seek = (ViewFlipper)findViewById(R.id.flip_seek);
        flip_seek.setInAnimation(this, android.R.anim.fade_in);
        flip_seek.setOutAnimation(this, android.R.anim.fade_out);

        mImageView = (ImageView)findViewById(R.id.image_view);
        cropImageView = (CropImageView)findViewById(R.id.crop_image_view);
        cropImageView.setGuidelines(2);
        cropImageView.setVisibility(View.GONE);

        view1 = (ImageView)findViewById(R.id.view1);
        view2 = (ImageView)findViewById(R.id.view2);
        view3 = (ImageView)findViewById(R.id.view3);
        view4 = (ImageView)findViewById(R.id.view4);
        view5 = (ImageView)findViewById(R.id.view5);

        view1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (cropInProcess || flip.getDisplayedChild()==flip.indexOfChild(hsvEffects)) {
                    if (cropInProcess) view4.requestFocus();
                    return false;
                }
                if (prevView != null) {
                    LinearLayout linearLayout = (LinearLayout) prevView.getParent();
                    ((TextView) (linearLayout.getChildAt(1))).setTextColor(Color.WHITE);
                    prevView = null;
                }
                prevView = (ImageView)findViewById(R.id.effect1_btn);
                ((TextView)(effect1.getChildAt(1))).setTextColor(Color.rgb(0, 128, 255));
                view1.requestFocusFromTouch();
                cancelBtn.setVisibility(View.GONE);
                applyThumbnails();
                mImageView.setImageBitmap(bitmap);
                saveBtn.setText("Save");
                viewSwitcher.setDisplayedChild(1);
                flip.setDisplayedChild(flip.indexOfChild(hsvEffects));
                return true;
            }
        });
        view2.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (cropInProcess || flip.getDisplayedChild()==flip.indexOfChild(adjust)) {
                    if (cropInProcess) view4.requestFocus();
                    return false;
                }
                if (prevView != null) {
                    LinearLayout linearLayout = (LinearLayout) prevView.getParent();
                    ((TextView) (linearLayout.getChildAt(1))).setTextColor(Color.WHITE);
                    prevView = null;
                }
                prevView = (ImageView)findViewById(R.id.adjust1_btn);
                linearLayout = (LinearLayout)prevView.getParent();
                ((TextView)(linearLayout.getChildAt(1))).setTextColor(Color.rgb(0, 128, 255));
                view2.requestFocusFromTouch();
                cancelBtn.setVisibility(View.GONE);
                saveBtn.setText("Save");
                if (filter > 1) {
                    bitmap = applyFilter(bitmap, filterUtil.getMyFilter(filter - 2));
                    filter = 1;
                }
                mImageView.setImageBitmap(bitmap);
                viewSwitcher.setDisplayedChild(1);
                originBitmap = bitmap;
                prevSeekBar = seekBar1;
                flip.setDisplayedChild(flip.indexOfChild(adjust));
                flip_seek.setDisplayedChild(flip_seek.indexOfChild(seekBar1));
                return true;
            }
        });
        view3.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (cropInProcess || flip.getDisplayedChild()==flip.indexOfChild(rotate)) {
                    if (cropInProcess) view4.requestFocus();
                    return false;
                }
                if (prevView != null) {
                    LinearLayout linearLayout = (LinearLayout) prevView.getParent();
                    ((TextView) (linearLayout.getChildAt(1))).setTextColor(Color.WHITE);
                    prevView = null;
                }
                view3.requestFocusFromTouch();
                cancelBtn.setVisibility(View.GONE);
                if (filter > 1) {
                    bitmap = applyFilter(bitmap, filterUtil.getMyFilter(filter - 2));
                    filter = 1;
                }
                mImageView.setImageBitmap(bitmap);
                saveBtn.setText("Save");
                viewSwitcher.setDisplayedChild(1);
                flip.setDisplayedChild(flip.indexOfChild(rotate));
                return true;
            }
        });
        view4.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (flip.getDisplayedChild()==flip.indexOfChild(crop)) return false;
                if (prevView != null) {
                    LinearLayout linearLayout = (LinearLayout) prevView.getParent();
                    ((TextView) (linearLayout.getChildAt(1))).setTextColor(Color.WHITE);
                    prevView = null;
                }
                cropInProcess = false;
                view4.requestFocusFromTouch();
                cancelBtn.setVisibility(View.VISIBLE);
                cancelBtn.setText("Cancel");
                if (filter > 1) {
                    bitmap = applyFilter(bitmap, filterUtil.getMyFilter(filter - 2));
                    filter = 1;
                }
                mImageView.setImageBitmap(bitmap);
                saveBtn.setText("Save");
                viewSwitcher.setDisplayedChild(1);
                flip.setDisplayedChild(flip.indexOfChild(crop));
                originBitmap = bitmap;
                return true;
            }
        });
        view5.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (cropInProcess || flip.getDisplayedChild()==flip.indexOfChild(frame)) {
                    if (cropInProcess) view4.requestFocus();
                    return false;
                }
                if (prevView != null) {
                    LinearLayout linearLayout = (LinearLayout) prevView.getParent();
                    ((TextView) (linearLayout.getChildAt(1))).setTextColor(Color.WHITE);
                    prevView = null;
                }
                view5.requestFocusFromTouch();
                cancelBtn.setVisibility(View.GONE);
                if (filter > 1) {
                    bitmap = applyFilter(bitmap, filterUtil.getMyFilter(filter - 2));
                    filter = 1;
                }
                saveBtn.setText("Apply");
                viewSwitcher.setDisplayedChild(1);
                flip.setDisplayedChild(flip.indexOfChild(frame));
                borderPaint = new Paint();
                borderPaint.setAntiAlias(true);
                borderPaint.setStyle(Paint.Style.STROKE);
                borderPaint.setColor(hsvColor);
                borderPaint.setStrokeWidth(15);
                originBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                canvas = new Canvas(originBitmap);
                canvas.drawBitmap(originBitmap, new Matrix(), null);
                canvas.drawRect(8, 8, bitmap.getWidth() - 8, bitmap.getHeight() - 8, borderPaint);
                mImageView.setImageBitmap(originBitmap);
                return true;
            }
        });

        hsv = (HorizontalScrollView)findViewById(R.id.hsv);
        hsvEffects = (HorizontalScrollView)findViewById(R.id.hsv_effects);
        adjust = (LinearLayout)findViewById(R.id.adjust);
        rotate = (LinearLayout)findViewById(R.id.rotate);
        crop = (LinearLayout)findViewById(R.id.crop);
        frame = (LinearLayout)findViewById(R.id.frame);

        myGestureDetector = new GestureDetector(new MyGestureDetector());

        edit1 = (LinearLayout)findViewById(R.id.edit1);
        edit2 = (LinearLayout)findViewById(R.id.edit2);
        edit3 = (LinearLayout)findViewById(R.id.edit3);
        edit4 = (LinearLayout)findViewById(R.id.edit4);
        edit5 = (LinearLayout)findViewById(R.id.edit5);

        effect1 = (LinearLayout)findViewById(R.id.effect1);
        effect2 = (LinearLayout)findViewById(R.id.effect2);
        effect3 = (LinearLayout)findViewById(R.id.effect3);
        effect4 = (LinearLayout)findViewById(R.id.effect4);
        effect5 = (LinearLayout)findViewById(R.id.effect5);
        effect6 = (LinearLayout)findViewById(R.id.effect6);
        effect7 = (LinearLayout)findViewById(R.id.effect7);
        effect8 = (LinearLayout)findViewById(R.id.effect8);
        effect9 = (LinearLayout)findViewById(R.id.effect9);
        effect10 = (LinearLayout)findViewById(R.id.effect10);

        seekBar1 = (SeekBar)findViewById(R.id.seekbar1);
        seekBar2 = (SeekBar)findViewById(R.id.seekbar2);
        seekBar3 = (SeekBar)findViewById(R.id.seekbar3);
        seekBar4 = (SeekBar)findViewById(R.id.seekbar4);
        seekBar5 = (SeekBar)findViewById(R.id.seekbar5);

        seekBar1.setOnSeekBarChangeListener(this);
        seekBar2.setOnSeekBarChangeListener(this);
        seekBar3.setOnSeekBarChangeListener(this);
        seekBar4.setOnSeekBarChangeListener(this);
        seekBar5.setOnSeekBarChangeListener(this);

        seekBar1.setBackgroundColor(Color.argb(100, 255, 255, 0));
        seekBar2.setBackgroundColor(Color.argb(100, 128, 255, 0));
        seekBar3.setBackgroundColor(Color.argb(100, 255, 128, 0));
        seekBar4.setBackgroundColor(Color.argb(100, 0, 128, 255));
        seekBar5.setBackgroundColor(Color.argb(100, 0, 0, 255));

        innerlayEffects = (LinearLayout)findViewById(R.id.innerLay_effects);

        Display display = getWindowManager().getDefaultDisplay();
        params = new LayoutParams((display.getWidth())/5, LayoutParams.MATCH_PARENT);
        edit1.setLayoutParams(params);
        edit2.setLayoutParams(params);
        edit3.setLayoutParams(params);
        edit4.setLayoutParams(params);
        edit5.setLayoutParams(params);

        params = new LayoutParams((display.getWidth())/6, LayoutParams.MATCH_PARENT);
        effect1.setLayoutParams(params);
        effect2.setLayoutParams(params);
        effect3.setLayoutParams(params);
        effect4.setLayoutParams(params);
        effect5.setLayoutParams(params);
        effect6.setLayoutParams(params);
        effect7.setLayoutParams(params);
        effect8.setLayoutParams(params);
        effect9.setLayoutParams(params);
        effect10.setLayoutParams(params);

        hue = (ColorBarView)findViewById(R.id.hue);
        saturation = (ColorBarView)findViewById(R.id.saturation);
        value = (ColorBarView)findViewById(R.id.value);

        homeBtn = (Button)findViewById(R.id.home_btn);
        saveBtn = (Button)findViewById(R.id.save_btn);
        cancelBtn = (Button)findViewById(R.id.cancel_btn);
        viewSwitcher = (ViewSwitcher)findViewById(R.id.view_switcher);
        cancelBtn.setVisibility(View.GONE);


        helper = new ColorBarHelper(new ColorBarView[]{hue, saturation, value})
                .setOnColorChangeListener(colorChangeListener).setRGBColor(0xffabcdef);

        hsv.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                return myGestureDetector.onTouchEvent(event);
            }
        });
        hsvEffects.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return myGestureDetector.onTouchEvent(event);
            }
        });

        imageLoader = ImageLoader.getInstance();
        if (!imageLoader.isInited())
            initImageLoader();

        if (comeFromCamera) {
            //change this line with below one
            handleImage(realPath);

            //handleImage(uri);
        }
        else {
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
            photoPickerIntent.setType("image/*");
            startActivityForResult(photoPickerIntent, REQUEST_PICK_IMAGE);
        }
    }

    private final OnColorChangeListener colorChangeListener = new OnColorChangeListener() {
        @Override
        public void onColorChange( final int color ) {
            saveBtn.setText("Apply");
            hsvColor = color;
            borderPaint = new Paint();
            borderPaint.setAntiAlias(true);
            borderPaint.setStyle(Paint.Style.STROKE);
            borderPaint.setColor(hsvColor);
            borderPaint.setStrokeWidth(10);
            originBitmap = bitmap.copy(Bitmap.Config.RGB_565, true);
            canvas = new Canvas(originBitmap);
            canvas.drawBitmap(originBitmap, new Matrix(), null);
            canvas.drawRect(4, 4, bitmap.getWidth() - 4, bitmap.getHeight() - 4, borderPaint);
            mImageView.setImageBitmap(originBitmap);
        }
    };

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.home_btn:
                //bitmap = null;
                //originBitmap = null;
                filter = 1;
                if (comeFromCamera) {
                    Intent i = new Intent(this, MainActivity.class);
                    startActivity(i);
                }
                finish();
                break;
            case R.id.edit_btn:
                cancelBtn.setVisibility(View.GONE);
                saveBtn.setText("Save");
                if (filter > 1) {
                    bitmap = applyFilter(bitmap, filterUtil.getMyFilter(filter - 2));
                    filter = 1;
                }
                if (flip.getDisplayedChild()==flip.indexOfChild(crop)) {
                    cropInProcess = false;
                    cropImageView.setVisibility(View.GONE);
                    mImageView.setVisibility(View.VISIBLE);
                    mImageView.setImageBitmap(bitmap);
                }
                mImageView.setImageBitmap(bitmap);
                viewSwitcher.setDisplayedChild(0);
                flip.setDisplayedChild(flip.indexOfChild(hsv));
                break;
            case R.id.save_btn:
                if (saveBtn.getText().equals("Save")) {
                    if (filter > 1) {
                        bitmap = applyFilter(bitmap, filterUtil.getMyFilter(filter - 2));
                        filter = 1;
                    }
                    if (prevView != null) {
                        linearLayout = (LinearLayout) prevView.getParent();
                        ((TextView) (linearLayout.getChildAt(1))).setTextColor(Color.WHITE);
                        prevView = null;
                    }
                    saveImage(bitmap);
                    viewSwitcher.setDisplayedChild(0);
                    flip.setDisplayedChild(0);
                    cancelBtn.setVisibility(View.GONE);
                }
                else if (saveBtn.getText().equals(("Crop"))) {
                    if (prevView != null) {
                        linearLayout = (LinearLayout) prevView.getParent();
                        ((TextView) (linearLayout.getChildAt(1))).setTextColor(Color.WHITE);
                        prevView = null;
                    }
                    bitmap = cropImageView.getCroppedImage();
                    originBitmap = bitmap;
                    cropImageView.setImageBitmap(null);
                    cropImageView.setVisibility(View.GONE);
                    mImageView.setVisibility(View.VISIBLE);
                    mImageView.setImageBitmap(bitmap);
                    saveBtn.setText("Save");
                    cropInProcess = false;
                }
                else {
                    bitmap = originBitmap;
                    saveBtn.setText("Save");
                }
                break;
            case R.id.cancel_btn:
                if (prevView != null) {
                    linearLayout = (LinearLayout) prevView.getParent();
                    ((TextView) (linearLayout.getChildAt(1))).setTextColor(Color.WHITE);
                    prevView = null;
                }
                saveBtn.setText("Save");
                bitmap = originBitmap;
                cropImageView.setImageBitmap(null);
                cropImageView.setVisibility(View.GONE);
                mImageView.setVisibility(View.VISIBLE);
                mImageView.setImageBitmap(originBitmap);
                cropInProcess = false;

                break;
            case R.id.undo_all_btn:
                //imageLoader.displayImage(uri.toString(), mImageView);
                bitmap = ExifUtil.rotateBitmap(realPath,
                        BitmapUtil.decodeSampledBitmapFromResource(realPath, 256, 256));
                mImageView.setImageBitmap(bitmap);
                break;
            case R.id.edit1_btn:
                if (prevView != null) {
                    linearLayout = (LinearLayout) prevView.getParent();
                    ((TextView) (linearLayout.getChildAt(1))).setTextColor(Color.WHITE);
                    prevView = null;
                }
                prevView = (ImageView)findViewById(R.id.effect1_btn);
                ((TextView)(effect1.getChildAt(1))).setTextColor(Color.rgb(0, 128, 255));
                cancelBtn.setVisibility(View.GONE);
                applyThumbnails();
                mImageView.setImageBitmap(bitmap);
                saveBtn.setText("Save");
                viewSwitcher.setDisplayedChild(1);
                flip.setDisplayedChild(flip.indexOfChild(hsvEffects));
                view1.requestFocus();
                break;
            case R.id.edit2_btn:
                if (prevView != null) {
                    linearLayout = (LinearLayout) prevView.getParent();
                    ((TextView) (linearLayout.getChildAt(1))).setTextColor(Color.WHITE);
                    prevView = null;
                }
                prevView = (ImageView)findViewById(R.id.adjust1_btn);
                linearLayout = (LinearLayout)prevView.getParent();
                ((TextView)(linearLayout.getChildAt(1))).setTextColor(Color.rgb(0, 128, 255));
                cancelBtn.setVisibility(View.GONE);
                mImageView.setImageBitmap(bitmap);
                saveBtn.setText("Save");
                viewSwitcher.setDisplayedChild(1);
                originBitmap = bitmap;
                prevSeekBar = seekBar1;
                flip.setDisplayedChild(flip.indexOfChild(adjust));
                flip_seek.setDisplayedChild(flip_seek.indexOfChild(seekBar1));
                view2.requestFocus();
                break;
            case R.id.edit3_btn:
                if (prevView != null) {
                    linearLayout = (LinearLayout) prevView.getParent();
                    ((TextView) (linearLayout.getChildAt(1))).setTextColor(Color.WHITE);
                    prevView = null;
                }
                cancelBtn.setVisibility(View.GONE);
                mImageView.setImageBitmap(bitmap);
                saveBtn.setText("Save");
                viewSwitcher.setDisplayedChild(1);
                flip.setDisplayedChild(flip.indexOfChild(rotate));
                view3.requestFocus();
                break;
            case R.id.edit4_btn:
                if (prevView != null) {
                    linearLayout = (LinearLayout) prevView.getParent();
                    ((TextView) (linearLayout.getChildAt(1))).setTextColor(Color.WHITE);
                    prevView = null;
                }
                cropInProcess = false;
                cancelBtn.setVisibility(View.VISIBLE);
                cancelBtn.setText("Cancel");
                mImageView.setImageBitmap(bitmap);
                saveBtn.setText("Save");
                viewSwitcher.setDisplayedChild(1);
                flip.setDisplayedChild(flip.indexOfChild(crop));
                originBitmap = bitmap;
                view4.requestFocus();
                break;
            case R.id.edit5_btn:
                if (prevView != null) {
                    linearLayout = (LinearLayout) prevView.getParent();
                    ((TextView) (linearLayout.getChildAt(1))).setTextColor(Color.WHITE);
                    prevView = null;
                }
                cancelBtn.setVisibility(View.GONE);
                saveBtn.setText("Apply");
                viewSwitcher.setDisplayedChild(1);
                flip.setDisplayedChild(flip.indexOfChild(frame));
                borderPaint = new Paint();
                borderPaint.setAntiAlias(true);
                borderPaint.setStyle(Paint.Style.STROKE);
                borderPaint.setColor(hsvColor);
                borderPaint.setStrokeWidth(10);
                originBitmap = bitmap.copy(Bitmap.Config.RGB_565, true);
                canvas = new Canvas(originBitmap);
                canvas.drawBitmap(originBitmap, new Matrix(), null);
                canvas.drawRect(4, 4, bitmap.getWidth() - 4, bitmap.getHeight() - 4, borderPaint);
                mImageView.setImageBitmap(originBitmap);
                view5.requestFocus();
                break;
            case R.id.effect1_btn:
                if (filter != 1) {
                    if (prevView == null || prevView == v);
                    else {
                        linearLayout = (LinearLayout)prevView.getParent();
                        ((TextView)(linearLayout.getChildAt(1))).setTextColor(Color.WHITE);
                    }
                    prevView = v;
                    ((TextView)(effect1.getChildAt(1))).setTextColor(Color.rgb(0, 128, 255));
                    mImageView.setImageBitmap(bitmap);
                    filter = 1;
                }
                break;
            case R.id.effect2_btn:
                if (filter != 2) {
                    if (prevView == null || prevView == v);
                    else {
                        linearLayout = (LinearLayout)prevView.getParent();
                        ((TextView)(linearLayout.getChildAt(1))).setTextColor(Color.WHITE);
                    }
                    prevView = v;
                    ((TextView)(effect2.getChildAt(1))).setTextColor(Color.rgb(0, 128, 255));
                    mImageView.setImageBitmap(applyFilter(bitmap, filterUtil.getMyFilter(0)));
                    filter = 2;
                }
                break;
            case R.id.effect3_btn:
                if (filter != 3) {
                    if (prevView == null || prevView == v);
                    else {
                        linearLayout = (LinearLayout)prevView.getParent();
                        ((TextView)(linearLayout.getChildAt(1))).setTextColor(Color.WHITE);
                    }
                    prevView = v;
                    ((TextView)(effect3.getChildAt(1))).setTextColor(Color.rgb(0, 128, 255));
                    mImageView.setImageBitmap(applyFilter(bitmap, filterUtil.getMyFilter(1)));
                    filter = 3;
                }
                break;
            case R.id.effect4_btn:
                if (filter != 4) {
                    if (prevView == null || prevView == v);
                    else {
                        linearLayout = (LinearLayout)prevView.getParent();
                        ((TextView)(linearLayout.getChildAt(1))).setTextColor(Color.WHITE);
                    }
                    prevView = v;
                    ((TextView)(effect4.getChildAt(1))).setTextColor(Color.rgb(0, 128, 255));
                    mImageView.setImageBitmap(applyFilter(bitmap, filterUtil.getMyFilter(2)));
                    filter = 4;
                }
                break;
            case R.id.effect5_btn:
                if (filter != 5) {
                    if (prevView == null || prevView == v);
                    else {
                        linearLayout = (LinearLayout)prevView.getParent();
                        ((TextView)(linearLayout.getChildAt(1))).setTextColor(Color.WHITE);
                    }
                    prevView = v;
                    ((TextView)(effect5.getChildAt(1))).setTextColor(Color.rgb(0, 128, 255));
                    mImageView.setImageBitmap(applyFilter(bitmap, filterUtil.getMyFilter(3)));
                    filter = 5;
                }
                break;
            case R.id.effect6_btn:
                if (filter != 6) {
                    if (prevView == null || prevView == v);
                    else {
                        linearLayout = (LinearLayout)prevView.getParent();
                        ((TextView)(linearLayout.getChildAt(1))).setTextColor(Color.WHITE);
                    }
                    prevView = v;
                    ((TextView)(effect6.getChildAt(1))).setTextColor(Color.rgb(0, 128, 255));
                    mImageView.setImageBitmap(applyFilter(bitmap, filterUtil.getMyFilter(4)));
                    filter = 6;
                }
                break;
            case R.id.effect7_btn:
                if (filter != 7) {
                    if (prevView == null || prevView == v);
                    else {
                        linearLayout = (LinearLayout)prevView.getParent();
                        ((TextView)(linearLayout.getChildAt(1))).setTextColor(Color.WHITE);
                    }
                    prevView = v;
                    ((TextView)(effect7.getChildAt(1))).setTextColor(Color.rgb(0, 128, 255));
                    mImageView.setImageBitmap(applyFilter(bitmap, filterUtil.getMyFilter(5)));
                    filter = 7;
                }
                break;
            case R.id.effect8_btn:
                if (filter != 8) {
                    if (prevView == null || prevView == v);
                    else {
                        linearLayout = (LinearLayout)prevView.getParent();
                        ((TextView)(linearLayout.getChildAt(1))).setTextColor(Color.WHITE);
                    }
                    prevView = v;
                    ((TextView)(effect8.getChildAt(1))).setTextColor(Color.rgb(0, 128, 255));
                    mImageView.setImageBitmap(applyFilter(bitmap, filterUtil.getMyFilter(6)));
                    filter = 8;
                }
                break;
            case R.id.effect9_btn:
                if (filter != 9) {
                    if (prevView == null || prevView == v);
                    else {
                        linearLayout = (LinearLayout)prevView.getParent();
                        ((TextView)(linearLayout.getChildAt(1))).setTextColor(Color.WHITE);
                    }
                    prevView = v;
                    ((TextView)(effect9.getChildAt(1))).setTextColor(Color.rgb(0, 128, 255));
                    mImageView.setImageBitmap(applyFilter(bitmap, filterUtil.getMyFilter(7)));
                    filter = 9;
                }
                break;
            case R.id.effect10_btn:
                if (filter != 10) {
                    if (prevView == null || prevView == v);
                    else {
                        linearLayout = (LinearLayout)prevView.getParent();
                        ((TextView)(linearLayout.getChildAt(1))).setTextColor(Color.WHITE);
                    }
                    prevView = v;
                    ((TextView)(effect10.getChildAt(1))).setTextColor(Color.rgb(0, 128, 255));
                    mImageView.setImageBitmap(applyFilter(bitmap, filterUtil.getMyFilter(8)));
                    filter = 10;
                }
                break;
            case R.id.adjust1_btn:
                if (prevView == null || prevView == v);
                else {
                    linearLayout = (LinearLayout)prevView.getParent();
                    ((TextView)(linearLayout.getChildAt(1))).setTextColor(Color.WHITE);
                }
                prevView = v;
                linearLayout = (LinearLayout)prevView.getParent();
                ((TextView)(linearLayout.getChildAt(1))).setTextColor(Color.rgb(0, 128, 255));
                flip_seek.setDisplayedChild(0);
                break;
            case R.id.adjust2_btn:
                if (prevView == null || prevView == v);
                else {
                    linearLayout = (LinearLayout)prevView.getParent();
                    ((TextView)(linearLayout.getChildAt(1))).setTextColor(Color.WHITE);
                }
                prevView = v;
                linearLayout = (LinearLayout)prevView.getParent();
                ((TextView)(linearLayout.getChildAt(1))).setTextColor(Color.rgb(0, 128, 255));
                flip_seek.setDisplayedChild(1);
                break;
            case R.id.adjust3_btn:
                if (prevView == null || prevView == v);
                else {
                    linearLayout = (LinearLayout)prevView.getParent();
                    ((TextView)(linearLayout.getChildAt(1))).setTextColor(Color.WHITE);
                }
                prevView = v;
                linearLayout = (LinearLayout)prevView.getParent();
                ((TextView)(linearLayout.getChildAt(1))).setTextColor(Color.rgb(0, 128, 255));
                flip_seek.setDisplayedChild(2);
                break;
            case R.id.adjust4_btn:
                if (prevView == null || prevView == v);
                else {
                    linearLayout = (LinearLayout)prevView.getParent();
                    ((TextView)(linearLayout.getChildAt(1))).setTextColor(Color.WHITE);
                }
                prevView = v;
                linearLayout = (LinearLayout)prevView.getParent();
                ((TextView)(linearLayout.getChildAt(1))).setTextColor(Color.rgb(0, 128, 255));
                flip_seek.setDisplayedChild(3);
                break;
            case R.id.adjust5_btn:
                if (prevView == null || prevView == v);
                else {
                    linearLayout = (LinearLayout)prevView.getParent();
                    ((TextView)(linearLayout.getChildAt(1))).setTextColor(Color.WHITE);
                }
                prevView = v;
                linearLayout = (LinearLayout)prevView.getParent();
                ((TextView)(linearLayout.getChildAt(1))).setTextColor(Color.rgb(0, 128, 255));
                flip_seek.setDisplayedChild(4);
                break;
            case R.id.rotate_left_btn:
                bitmap = rotate(bitmap, 2);
                mImageView.setImageBitmap(bitmap);
                break;
            case R.id.rotate_right_btn:
                bitmap = rotate(bitmap, 1);
                mImageView.setImageBitmap(bitmap);
                break;
            case R.id.flip_horizontal_btn:
                bitmap = rotate(bitmap, 3);
                mImageView.setImageBitmap(bitmap);
                break;
            case R.id.flip_vertical_btn:
                bitmap = rotate(bitmap, 4);
                mImageView.setImageBitmap(bitmap);
                break;
            case R.id.crop1_btn:
                view4.requestFocus();
                if (prevView == null || prevView == v);
                else {
                    linearLayout = (LinearLayout)prevView.getParent();
                    ((TextView)(linearLayout.getChildAt(1))).setTextColor(Color.WHITE);
                }
                prevView = v;
                linearLayout = (LinearLayout)prevView.getParent();
                ((TextView)(linearLayout.getChildAt(1))).setTextColor(Color.rgb(0, 128, 255));
                saveBtn.setText("Crop");
                cropImageView.setFixedAspectRatio(false);
                mImageView.setVisibility(View.GONE);
                cropImageView.setVisibility(View.VISIBLE);
                cropImageView.setImageBitmap(bitmap);
                cropInProcess = true;
                break;
            case R.id.crop2_btn:
                view4.requestFocus();
                if (prevView == null || prevView == v);
                else {
                    linearLayout = (LinearLayout)prevView.getParent();
                    ((TextView)(linearLayout.getChildAt(1))).setTextColor(Color.WHITE);
                }
                prevView = v;
                linearLayout = (LinearLayout)prevView.getParent();
                ((TextView)(linearLayout.getChildAt(1))).setTextColor(Color.rgb(0, 128, 255));
                saveBtn.setText("Crop");
                cropImageView.setFixedAspectRatio(true);
                cropImageView.setAspectRatio(1, 1);
                mImageView.setVisibility(View.GONE);
                cropImageView.setVisibility(View.VISIBLE);
                cropImageView.setImageBitmap(bitmap);
                cropInProcess = true;
                break;
            case R.id.crop3_btn:
                view4.requestFocus();
                if (prevView == null || prevView == v);
                else {
                    linearLayout = (LinearLayout)prevView.getParent();
                    ((TextView)(linearLayout.getChildAt(1))).setTextColor(Color.WHITE);
                }
                prevView = v;
                linearLayout = (LinearLayout)prevView.getParent();
                ((TextView)(linearLayout.getChildAt(1))).setTextColor(Color.rgb(0, 128, 255));
                saveBtn.setText("Crop");
                cropImageView.setFixedAspectRatio(true);
                cropImageView.setAspectRatio(4, 3);
                mImageView.setVisibility(View.GONE);
                cropImageView.setVisibility(View.VISIBLE);
                cropImageView.setImageBitmap(bitmap);
                cropInProcess = true;
                break;
            case R.id.crop4_btn:
                view4.requestFocus();
                if (prevView == null || prevView == v);
                else {
                    linearLayout = (LinearLayout)prevView.getParent();
                    ((TextView)(linearLayout.getChildAt(1))).setTextColor(Color.WHITE);
                }
                prevView = v;
                linearLayout = (LinearLayout)prevView.getParent();
                ((TextView)(linearLayout.getChildAt(1))).setTextColor(Color.rgb(0, 128, 255));
                saveBtn.setText("Crop");
                cropImageView.setFixedAspectRatio(true);
                cropImageView.setAspectRatio(6, 4);
                mImageView.setVisibility(View.GONE);
                cropImageView.setVisibility(View.VISIBLE);
                cropImageView.setImageBitmap(bitmap);
                cropInProcess = true;
                break;
            case R.id.crop5_btn:
                view4.requestFocus();
                if (prevView == null || prevView == v);
                else {
                    linearLayout = (LinearLayout)prevView.getParent();
                    ((TextView)(linearLayout.getChildAt(1))).setTextColor(Color.WHITE);
                }
                prevView = v;
                linearLayout = (LinearLayout)prevView.getParent();
                ((TextView)(linearLayout.getChildAt(1))).setTextColor(Color.rgb(0, 128, 255));
                saveBtn.setText("Crop");
                cropImageView.setFixedAspectRatio(true);
                cropImageView.setAspectRatio(10, 8);
                mImageView.setVisibility(View.GONE);
                cropImageView.setVisibility(View.VISIBLE);
                cropImageView.setImageBitmap(bitmap);
                cropInProcess = true;
                break;
            case R.id.crop6_btn:
                view4.requestFocus();
                if (prevView == null || prevView == v);
                else {
                    linearLayout = (LinearLayout)prevView.getParent();
                    ((TextView)(linearLayout.getChildAt(1))).setTextColor(Color.WHITE);
                }
                prevView = v;
                linearLayout = (LinearLayout)prevView.getParent();
                ((TextView)(linearLayout.getChildAt(1))).setTextColor(Color.rgb(0, 128, 255));
                saveBtn.setText("Crop");
                cropImageView.setFixedAspectRatio(true);
                cropImageView.setAspectRatio(16, 9);
                mImageView.setVisibility(View.GONE);
                cropImageView.setVisibility(View.VISIBLE);
                cropImageView.setImageBitmap(bitmap);
                cropInProcess = true;
                break;
            default: break;
        }
    }

    private Bitmap rotate(Bitmap bitmap, int o) {
        if (bitmap == null) {
            bitmap = ExifUtil.rotateBitmap(realPath,
                    BitmapUtil.decodeSampledBitmapFromResource(realPath, 256, 256));
        }
        Matrix matrix = new Matrix();
        switch (o) {
            case 1:
                matrix.postRotate(90);
                break;
            case 2:
                matrix.postRotate(-90);
                break;
            case 3:
                matrix.preScale(-1, 1);
                break;
            case 4:
                matrix.preScale(1, -1);
                break;
            default:
                return bitmap;
        }

        try {
            Bitmap oriented = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(),
                    matrix, true);
            bitmap.recycle();
            return oriented;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return bitmap;
        }
    }

    @Override
    public void onProgressChanged(final SeekBar seekBar, final int progress, final boolean fromUser) {

    }

    @Override
    public void onStartTrackingTouch(final SeekBar seekBar) {
        if (seekBar == seekBar1) {
            gpuImageFilter = filterUtil.getAdjustFilter(0);
        } else if (seekBar == seekBar2) {
            gpuImageFilter = filterUtil.getAdjustFilter(1);
        } else if (seekBar == seekBar3) {
            gpuImageFilter = filterUtil.getAdjustFilter(2);
        } else if (seekBar == seekBar4) {
            gpuImageFilter = filterUtil.getAdjustFilter(3);
        } else if (seekBar == seekBar5) {
            gpuImageFilter = filterUtil.getAdjustFilter(4);
        }
    }

    @Override
    public void onStopTrackingTouch(final SeekBar seekBar) {
        mFilterAdjuster = new FilterUtil.FilterAdjuster(gpuImageFilter);
        gpuImageFilter = mFilterAdjuster.adjust(seekBar.getProgress());
        if (seekBar == prevSeekBar) {
            bitmap = applyFilter(originBitmap, gpuImageFilter);
        }
        else {
            originBitmap = bitmap;
            prevSeekBar = seekBar;
            bitmap = applyFilter(originBitmap, gpuImageFilter);
        }
        mImageView.setImageBitmap(bitmap);
    }

    private Bitmap applyFilter(String realPath, GPUImageFilter gpuImageFilter,
                               int width, int height) {
        gpuImage.setFilter(gpuImageFilter);
        return gpuImage.getBitmapWithFilterApplied(ExifUtil.rotateBitmap(realPath,
                BitmapUtil.decodeSampledBitmapFromResource(realPath, width, height)));
    }

    private Bitmap applyFilter(Bitmap bitmap, GPUImageFilter gpuImageFilter) {
        gpuImage.setFilter(gpuImageFilter);
        return gpuImage.getBitmapWithFilterApplied(bitmap);
    }

    private void applyThumbnails () {
        thumbnail = ExifUtil.rotateBitmap(realPath,
                        BitmapUtil.decodeSampledBitmapFromResource(realPath, 50, 50));
        if (bitmap == null) return;
        //thumbnail = Bitmap.createScaledBitmap(bitmap, 25, 25, false);
        ((ImageView)((LinearLayout)(innerlayEffects.getChildAt(0))).getChildAt(0))
                .setImageBitmap(BitmapUtil.getRoundedCornerBitmap(thumbnail, 10));
        for (int i = 1; i <= filterUtil.getMyFilterSize(); i++) {
           ((ImageView)((LinearLayout)(innerlayEffects.getChildAt(i))).getChildAt(0))
                    .setImageBitmap(BitmapUtil.getRoundedCornerBitmap(applyFilter(thumbnail,
                            filterUtil.getMyFilter(i - 1)), 10));
        }

        thumbnail = null;

    }

    private void saveImage(final Bitmap bitmap) {
        String storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES).getAbsolutePath();
        File fil = new File(storageDir + "/Photopix");
        fil.mkdir();
        String fileName = System.currentTimeMillis() + ".jpg";
        File newImg = new File(fil, fileName);

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bytes);

        try {
            FileOutputStream fos = new FileOutputStream(newImg);
            fos.write(bytes.toByteArray());
            fos.close();
            Uri contentUri = Uri.fromFile(newImg);

            Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            scanIntent.setData(contentUri);
            this.sendBroadcast(scanIntent);
            Toast.makeText(getApplicationContext(), "Saved",
                    Toast.LENGTH_SHORT).show();

            uri = contentUri;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case REQUEST_PICK_IMAGE:
                if (resultCode == RESULT_OK) {
                    handleImage(data.getData());
                } else {
                    finish();
                }
                break;

            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
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

    private void handleImage(final Uri selectedImage) {
        viewSwitcher.setDisplayedChild(0);
        uri = selectedImage;
        if (Build.VERSION.SDK_INT < 11)
            realPath = RealPathUtil.getRealPathFromURI_BelowAPI11(this, uri);
        else if (Build.VERSION.SDK_INT < 19)
            realPath = RealPathUtil.getRealPathFromURI_API11to18(this, uri);
        else
            realPath = RealPathUtil.getRealPathFromURI_API11to18(this, uri);
        if (!imageLoader.isInited())
            initImageLoader();
        //imageLoader.displayImage(selectedImage.toString(), mImageView);
        bitmap = ExifUtil.rotateBitmap(realPath,
                BitmapUtil.decodeSampledBitmapFromResource(realPath, 256, 256));
        mImageView.setImageBitmap(bitmap);
    }

    private void handleImage(final String realPath) {
        viewSwitcher.setDisplayedChild(0);
        //imageLoader.displayImage(uri.toString(), mImageView);
        bitmap = ExifUtil.rotateBitmap(realPath,
                BitmapUtil.decodeSampledBitmapFromResource(realPath, 256, 256));
        mImageView.setImageBitmap(bitmap);
    }

    @Override
    public void onStop() {
        super.onStop();
        mSensorManager.unregisterListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        initListeners();
        initImageLoader();
    }

    @Override
    public void onDestroy() {
        clear();
        super.onDestroy();
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

    public void initListeners(){
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_FASTEST);

        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                SensorManager.SENSOR_DELAY_FASTEST);

        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_FASTEST);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch(event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                System.arraycopy(event.values, 0, accel, 0, 3);
                calculateAccMagOrientation();
                break;

            case Sensor.TYPE_GYROSCOPE:
                gyroFunction(event);
                break;

            case Sensor.TYPE_MAGNETIC_FIELD:
                System.arraycopy(event.values, 0, magnet, 0, 3);
                break;
        }
    }

    public void calculateAccMagOrientation() {
        if(SensorManager.getRotationMatrix(rotationMatrix, null, accel, magnet)) {
            SensorManager.getOrientation(rotationMatrix, accMagOrientation);
        }
    }

    private void getRotationVectorFromGyro(float[] gyroValues,
                                           float[] deltaRotationVector,
                                           float timeFactor)
    {
        float[] normValues = new float[3];

        float omegaMagnitude =
                (float)Math.sqrt(gyroValues[0] * gyroValues[0] +
                        gyroValues[1] * gyroValues[1] +
                        gyroValues[2] * gyroValues[2]);

        if(omegaMagnitude > EPSILON) {
            normValues[0] = gyroValues[0] / omegaMagnitude;
            normValues[1] = gyroValues[1] / omegaMagnitude;
            normValues[2] = gyroValues[2] / omegaMagnitude;
        }

        float thetaOverTwo = omegaMagnitude * timeFactor;
        float sinThetaOverTwo = (float)Math.sin(thetaOverTwo);
        float cosThetaOverTwo = (float)Math.cos(thetaOverTwo);
        deltaRotationVector[0] = sinThetaOverTwo * normValues[0];
        deltaRotationVector[1] = sinThetaOverTwo * normValues[1];
        deltaRotationVector[2] = sinThetaOverTwo * normValues[2];
        deltaRotationVector[3] = cosThetaOverTwo;
    }

    public void gyroFunction(SensorEvent event) {
        if (accMagOrientation == null)
            return;

        if(initState) {
            float[] initMatrix = new float[9];
            initMatrix = getRotationMatrixFromOrientation(accMagOrientation);
            float[] test = new float[3];
            SensorManager.getOrientation(initMatrix, test);
            gyroMatrix = matrixMultiplication(gyroMatrix, initMatrix);
            initState = false;
        }

        float[] deltaVector = new float[4];
        if(timestamp != 0) {
            final float dT = (event.timestamp - timestamp) * NS2S;
            System.arraycopy(event.values, 0, gyro, 0, 3);
            getRotationVectorFromGyro(gyro, deltaVector, dT / 2.0f);
        }

        timestamp = event.timestamp;

        float[] deltaMatrix = new float[9];
        SensorManager.getRotationMatrixFromVector(deltaMatrix, deltaVector);

        gyroMatrix = matrixMultiplication(gyroMatrix, deltaMatrix);

        SensorManager.getOrientation(gyroMatrix, gyroOrientation);
    }

    private float[] getRotationMatrixFromOrientation(float[] o) {
        float[] xM = new float[9];
        float[] yM = new float[9];
        float[] zM = new float[9];

        float sinX = (float)Math.sin(o[1]);
        float cosX = (float)Math.cos(o[1]);
        float sinY = (float)Math.sin(o[2]);
        float cosY = (float)Math.cos(o[2]);
        float sinZ = (float)Math.sin(o[0]);
        float cosZ = (float)Math.cos(o[0]);

        xM[0] = 1.0f; xM[1] = 0.0f; xM[2] = 0.0f;
        xM[3] = 0.0f; xM[4] = cosX; xM[5] = sinX;
        xM[6] = 0.0f; xM[7] = -sinX; xM[8] = cosX;

        yM[0] = cosY; yM[1] = 0.0f; yM[2] = sinY;
        yM[3] = 0.0f; yM[4] = 1.0f; yM[5] = 0.0f;
        yM[6] = -sinY; yM[7] = 0.0f; yM[8] = cosY;

        zM[0] = cosZ; zM[1] = sinZ; zM[2] = 0.0f;
        zM[3] = -sinZ; zM[4] = cosZ; zM[5] = 0.0f;
        zM[6] = 0.0f; zM[7] = 0.0f; zM[8] = 1.0f;

        float[] resultMatrix = matrixMultiplication(xM, yM);
        resultMatrix = matrixMultiplication(zM, resultMatrix);
        return resultMatrix;
    }

    private float[] matrixMultiplication(float[] A, float[] B) {
        float[] result = new float[9];

        result[0] = A[0] * B[0] + A[1] * B[3] + A[2] * B[6];
        result[1] = A[0] * B[1] + A[1] * B[4] + A[2] * B[7];
        result[2] = A[0] * B[2] + A[1] * B[5] + A[2] * B[8];

        result[3] = A[3] * B[0] + A[4] * B[3] + A[5] * B[6];
        result[4] = A[3] * B[1] + A[4] * B[4] + A[5] * B[7];
        result[5] = A[3] * B[2] + A[4] * B[5] + A[5] * B[8];

        result[6] = A[6] * B[0] + A[7] * B[3] + A[8] * B[6];
        result[7] = A[6] * B[1] + A[7] * B[4] + A[8] * B[7];
        result[8] = A[6] * B[2] + A[7] * B[5] + A[8] * B[8];

        return result;
    }

    class calculateFusedOrientationTask extends TimerTask {
        public void run() {
            float oneMinusCoeff = 1.0f - FILTER_COEFFICIENT;

            // azimuth
            if (gyroOrientation[0] < -0.5 * Math.PI && accMagOrientation[0] > 0.0) {
                fusedOrientation[0] = (float) (FILTER_COEFFICIENT * (gyroOrientation[0] + 2.0 * Math.PI) + oneMinusCoeff * accMagOrientation[0]);
                fusedOrientation[0] -= (fusedOrientation[0] > Math.PI) ? 2.0 * Math.PI : 0;
            }
            else if (accMagOrientation[0] < -0.5 * Math.PI && gyroOrientation[0] > 0.0) {
                fusedOrientation[0] = (float) (FILTER_COEFFICIENT * gyroOrientation[0] + oneMinusCoeff * (accMagOrientation[0] + 2.0 * Math.PI));
                fusedOrientation[0] -= (fusedOrientation[0] > Math.PI)? 2.0 * Math.PI : 0;
            }
            else {
                fusedOrientation[0] = FILTER_COEFFICIENT * gyroOrientation[0] + oneMinusCoeff * accMagOrientation[0];
            }

            // pitch
            if (gyroOrientation[1] < -0.5 * Math.PI && accMagOrientation[1] > 0.0) {
                fusedOrientation[1] = (float) (FILTER_COEFFICIENT * (gyroOrientation[1] + 2.0 * Math.PI) + oneMinusCoeff * accMagOrientation[1]);
                fusedOrientation[1] -= (fusedOrientation[1] > Math.PI) ? 2.0 * Math.PI : 0;
            }
            else if (accMagOrientation[1] < -0.5 * Math.PI && gyroOrientation[1] > 0.0) {
                fusedOrientation[1] = (float) (FILTER_COEFFICIENT * gyroOrientation[1] + oneMinusCoeff * (accMagOrientation[1] + 2.0 * Math.PI));
                fusedOrientation[1] -= (fusedOrientation[1] > Math.PI)? 2.0 * Math.PI : 0;
            }
            else {
                fusedOrientation[1] = FILTER_COEFFICIENT * gyroOrientation[1] + oneMinusCoeff * accMagOrientation[1];
            }

            // roll
            if (gyroOrientation[2] < -0.5 * Math.PI && accMagOrientation[2] > 0.0) {
                fusedOrientation[2] = (float) (FILTER_COEFFICIENT * (gyroOrientation[2] + 2.0 * Math.PI) + oneMinusCoeff * accMagOrientation[2]);
                fusedOrientation[2] -= (fusedOrientation[2] > Math.PI) ? 2.0 * Math.PI : 0;
            }
            else if (accMagOrientation[2] < -0.5 * Math.PI && gyroOrientation[2] > 0.0) {
                fusedOrientation[2] = (float) (FILTER_COEFFICIENT * gyroOrientation[2] + oneMinusCoeff * (accMagOrientation[2] + 2.0 * Math.PI));
                fusedOrientation[2] -= (fusedOrientation[2] > Math.PI)? 2.0 * Math.PI : 0;
            }
            else {
                fusedOrientation[2] = FILTER_COEFFICIENT * gyroOrientation[2] + oneMinusCoeff * accMagOrientation[2];
            }

            // overwrite gyro matrix and orientation with fused orientation
            // to compensate gyro drift
            gyroMatrix = getRotationMatrixFromOrientation(fusedOrientation);
            System.arraycopy(fusedOrientation, 0, gyroOrientation, 0, 3);

            mHandler.post(updateOrientationDisplayTask);
        }

        public void updateOrientationDisplay() {
            double roll = fusedOrientation[2] * 180/Math.PI;
            if ((roll > 15 && roll < 90) || (roll > -170 && roll < -90)) {
                flip.setInAnimation(getApplicationContext(), android.R.anim.slide_in_left);
                flip.setOutAnimation(getApplicationContext(), android.R.anim.slide_out_right);
                counter++;
                if (counter > 15 && flip.getDisplayedChild() == flip.indexOfChild(hsv)) {
                    prevView = (ImageView)findViewById(R.id.effect1_btn);
                    ((TextView)(effect1.getChildAt(1))).setTextColor(Color.rgb(0, 128, 255));
                    cancelBtn.setVisibility(View.GONE);
                    applyThumbnails();
                    mImageView.setImageBitmap(bitmap);
                    viewSwitcher.setDisplayedChild(1);
                    flip.setDisplayedChild(flip.indexOfChild(hsvEffects));
                    view1.requestFocus();
                    counter = 0;
                }
                else if (counter > 15 && flip.getDisplayedChild() == flip.indexOfChild(hsvEffects)) {
                    if (prevView != null) {
                        LinearLayout linearLayout = (LinearLayout) prevView.getParent();
                        ((TextView) (linearLayout.getChildAt(1))).setTextColor(Color.WHITE);
                        prevView = null;
                    }
                    prevView = (ImageView)findViewById(R.id.adjust1_btn);
                    linearLayout = (LinearLayout)prevView.getParent();
                    ((TextView)(linearLayout.getChildAt(1))).setTextColor(Color.rgb(0, 128, 255));
                    cancelBtn.setVisibility(View.GONE);
                    flip.setDisplayedChild(flip.indexOfChild(adjust));
                    flip_seek.setDisplayedChild(0);
                    prevSeekBar = seekBar1;
                    if (filter > 1) {
                        bitmap = applyFilter(bitmap, filterUtil.getMyFilter(filter - 2));
                        filter = 1;
                    }
                    mImageView.setImageBitmap(bitmap);
                    originBitmap = bitmap;
                    view2.requestFocus();
                    counter = 0;
                }
                else if (counter > 15 && flip.getDisplayedChild() == flip.indexOfChild(adjust)) {
                    if (prevView != null) {
                        LinearLayout linearLayout = (LinearLayout) prevView.getParent();
                        ((TextView) (linearLayout.getChildAt(1))).setTextColor(Color.WHITE);
                        prevView = null;
                    }
                    cancelBtn.setVisibility(View.GONE);
                    mImageView.setImageBitmap(bitmap);
                    flip.setDisplayedChild(flip.indexOfChild(rotate));
                    view3.requestFocus();
                    counter = 0;
                }
                else if (counter > 15 && flip.getDisplayedChild() == flip.indexOfChild(rotate)) {
                    if (prevView != null) {
                        LinearLayout linearLayout = (LinearLayout) prevView.getParent();
                        ((TextView) (linearLayout.getChildAt(1))).setTextColor(Color.WHITE);
                        prevView = null;
                    }
                    cropInProcess = false;
                    cancelBtn.setVisibility(View.VISIBLE);
                    cancelBtn.setText("Cancel");
                    mImageView.setImageBitmap(bitmap);
                    flip.setDisplayedChild(flip.indexOfChild(crop));
                    originBitmap = bitmap;
                    view4.requestFocus();
                    counter = 0;
                }
                else if (counter > 15 && flip.getDisplayedChild() == flip.indexOfChild(crop)
                        && !cropInProcess) {
                    cancelBtn.setVisibility(View.GONE);
                    saveBtn.setText("Apply");
                    flip.setDisplayedChild(flip.indexOfChild(frame));
                    borderPaint = new Paint();
                    borderPaint.setAntiAlias(true);
                    borderPaint.setStyle(Paint.Style.STROKE);
                    borderPaint.setColor(hsvColor);
                    borderPaint.setStrokeWidth(10);
                    originBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
                    canvas = new Canvas(originBitmap);
                    canvas.drawBitmap(originBitmap, new Matrix(), null);
                    canvas.drawRect(4, 4, bitmap.getWidth() - 4, bitmap.getHeight() - 4, borderPaint);
                    mImageView.setImageBitmap(originBitmap);
                    view5.requestFocus();
                    counter = 0;
                }

            }
            if ((roll < -15 && roll > -90) || (roll < 170 && roll > 90)) {
                flip.setInAnimation(getApplicationContext(), android.R.anim.fade_in);
                flip.setOutAnimation(getApplicationContext(), android.R.anim.fade_out);
                counter++;
                if (counter > 15 && flip.getDisplayedChild() == flip.indexOfChild(frame)) {
                    if (prevView != null) {
                        LinearLayout linearLayout = (LinearLayout) prevView.getParent();
                        ((TextView) (linearLayout.getChildAt(1))).setTextColor(Color.WHITE);
                        prevView = null;
                    }
                    cropInProcess = false;
                    cancelBtn.setVisibility(View.VISIBLE);
                    cancelBtn.setText("Cancel");
                    saveBtn.setText("Save");
                    mImageView.setImageBitmap(bitmap);
                    flip.setDisplayedChild(flip.indexOfChild(crop));
                    view4.requestFocus();
                    counter = 0;
                }
                else if (counter > 15 && flip.getDisplayedChild() == flip.indexOfChild(crop)
                        && !cropInProcess) {

                    cancelBtn.setVisibility(View.GONE);
                    mImageView.setImageBitmap(bitmap);
                    flip.setDisplayedChild(flip.indexOfChild(rotate));
                    view3.requestFocus();
                    counter = 0;
                }
                else if (counter > 15 && flip.getDisplayedChild() == flip.indexOfChild(rotate)) {
                    prevView = (ImageView)findViewById(R.id.adjust1_btn);
                    linearLayout = (LinearLayout)prevView.getParent();
                    ((TextView)(linearLayout.getChildAt(1))).setTextColor(Color.rgb(0, 128, 255));
                    cancelBtn.setVisibility(View.GONE);
                    mImageView.setImageBitmap(bitmap);
                    flip.setDisplayedChild(flip.indexOfChild(adjust));
                    flip_seek.setDisplayedChild(0);
                    originBitmap = bitmap;
                    prevSeekBar = seekBar1;
                    view2.requestFocus();
                    counter = 0;
                }
                else if (counter > 15 && flip.getDisplayedChild() == flip.indexOfChild(adjust)) {
                    if (prevView != null) {
                        LinearLayout linearLayout = (LinearLayout) prevView.getParent();
                        ((TextView) (linearLayout.getChildAt(1))).setTextColor(Color.WHITE);
                        prevView = null;
                    }
                    prevView = (ImageView)findViewById(R.id.effect1_btn);
                    ((TextView)(effect1.getChildAt(1))).setTextColor(Color.rgb(0, 128, 255));
                    cancelBtn.setVisibility(View.GONE);
                    applyThumbnails();
                    cancelBtn.setVisibility(View.GONE);
                    mImageView.setImageBitmap(bitmap);
                    flip.setDisplayedChild(flip.indexOfChild(hsvEffects));
                    view1.requestFocus();
                    counter = 0;
                }
            }
        }

        private Runnable updateOrientationDisplayTask = new Runnable() {
            public void run() {
                updateOrientationDisplay();
            }
        };
    }

    class MyGestureDetector extends GestureDetector.SimpleOnGestureListener {

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                               float velocityY) {
            return super.onFling(e1, e2, velocityX, velocityY);
        }
    }

    private void clear() {
        mImageView.setImageBitmap(null);
        bitmap = null;
        originBitmap = null;
        cropImageView.setImageBitmap(null);
        thumbnail = null;
        gpuImage.setImage(bitmap);
    }
}
