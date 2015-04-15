package sg.edu.nus.photopix;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;


public class CameraSurfaceView extends SurfaceView implements Camera.PreviewCallback, SurfaceHolder.Callback, Camera.PictureCallback {

    private SurfaceTexture dummyTexture = new SurfaceTexture(1);
    private Camera camera;
    //private int[] rgb;
    private Bitmap bitmap;
    private int previewWidth;
    private int previewHeight;
    private SurfaceHolder holder;

    private boolean isPreviewRunning = false;
    private boolean afterImageCapture = false;

    public CameraSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);

        holder = this.getHolder();
        holder.addCallback(this);
        this.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (afterImageCapture) {
                    afterImageCapture = false;
                    startPreview();
                } else if (!isPreviewRunning) {
                    startPreview();
                } else {
                    takePicture(CameraSurfaceView.this);
                }
            }
        });
    }

    public void takePicture(Camera.PictureCallback callback) {
        camera.takePicture(null, callback, callback);
        afterImageCapture = true;
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        if (data == null) {
            return;
        }
        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length, null);
        stopPreview();
        drawBitmap(bitmap);

        String file_path = Environment.getExternalStorageDirectory().getAbsolutePath() +
                "/Photopix";
        File dir = new File(file_path);
        if(!dir.exists())
            dir.mkdirs();
        File file = new File(dir, "photopix " + DateFormat.getDateTimeInstance().format(new Date()) + ".jpg");
        try {
            FileOutputStream fOut = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fOut);
            fOut.flush();
            fOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void startPreview() {
        synchronized (this) {
            if (isPreviewRunning) {
                return;
            }
            isPreviewRunning = true;
            if (camera == null) {
                camera = Camera.open();
            }
            Camera.Size size = camera.getParameters().getPreviewSize();
            ;
            previewWidth = size.width;
            previewHeight = size.height;
            try {
                camera.setPreviewTexture(dummyTexture);
                //camera.setPreviewDisplay(holder);
            } catch (IOException e) {
                e.printStackTrace();
            }

            camera.setPreviewCallback(this);
            camera.startPreview();
        }
    }

    private  void stopPreview() {
        synchronized (this) {
            try {
                if (camera != null) {
                    camera.stopPreview();
                    isPreviewRunning  = false;
                    //camera.release();
                    //camera = null;
                }
            } catch (Exception e) {
                Log.e("Camera", e.getMessage());
            }
        }
    }

    private void destroyCamera() {
        stopPreview();
        camera.release();
        camera = null;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
//        decodeYUV420SP(rgb, data, previewWidth, previewHeight);
//        bitmap = Bitmap.createBitmap(rgb, previewWidth, previewHeight, Bitmap.Config.ARGB_8888);
        bitmap = bytesToBitmap(data);
        drawBitmap(bitmap);
    }

    private void drawBitmap(Bitmap bitmap) {
        double ratio = bitmap.getWidth()/getWidth();
        if (bitmap.getWidth()/getWidth() < bitmap.getHeight()/getHeight()) {
            ratio = bitmap.getHeight()/getHeight();
        }
        int newWidth = (int)(bitmap.getWidth()/ratio);
        int newHeight = (int)(bitmap.getHeight()/ratio);
        int x = (getWidth() - newWidth) / 2;
        int y = (getHeight() - newHeight) / 2;
        Rect dest = new Rect(x, y, x + newWidth, y + newHeight);

        Canvas canvas = holder.lockCanvas();

        Paint paint = new Paint();
        paint.setFilterBitmap(true);
        canvas.drawColor(Color.BLACK);
        canvas.drawBitmap(bitmap, null, dest, paint);

        //this.draw(canvas);
        holder.unlockCanvasAndPost(canvas);
    }

    private Bitmap bytesToBitmap(byte[] data) {
        ByteArrayOutputStream os = new ByteArrayOutputStream(data.length);
        YuvImage image = new YuvImage(data, ImageFormat.NV21, previewWidth, previewHeight, null);
        Rect dest = new Rect(0, 0, previewWidth, previewHeight);
        image.compressToJpeg(dest, 20, os);
        byte bytes[] = os.toByteArray();

        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        return bitmap;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        //rgb = new int[width * height];
        startPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        destroyCamera();
    }

    /*
    // 200ms
    static public void decodeYUV420SP(int[] rgba, byte[] yuv420sp, int width,
                                      int height) {


        final int frameSize = width * height;

        int r, g, b, y1192, y, i, uvp, u, v;
        for (int j = 0, yp = 0; j < height; j++) {
            uvp = frameSize + (j >> 1) * width;
            u = 0;
            v = 0;
            for (i = 0; i < width; i++, yp++) {
                y = (0xff & ((int) yuv420sp[yp])) - 16;
                if (y < 0)
                    y = 0;
                if ((i & 1) == 0) {
                    v = (0xff & yuv420sp[uvp++]) - 128;
                    u = (0xff & yuv420sp[uvp++]) - 128;
                }

                y1192 = 1192 * y;
                r = (y1192 + 1634 * v);
                g = (y1192 - 833 * v - 400 * u);
                b = (y1192 + 2066 * u);

                r = Math.max(0, Math.min(r, 262143));
                g = Math.max(0, Math.min(g, 262143));
                b = Math.max(0, Math.min(b, 262143));

                // rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) &
                // 0xff00) | ((b >> 10) & 0xff);
                // rgba, divide 2^10 ( >> 10)
                rgba[yp] = ((r << 14) & 0xff000000) | ((g << 6) & 0xff0000)
                        | ((b >> 2) | 0xff00);
            }
        }
    }*/
}
