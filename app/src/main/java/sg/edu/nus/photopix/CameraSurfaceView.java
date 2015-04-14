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
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.widget.FrameLayout;

import java.io.ByteArrayOutputStream;
import java.io.IOException;


public class CameraSurfaceView extends SurfaceView implements Camera.PreviewCallback, SurfaceHolder.Callback {

    private SurfaceTexture dummyTexture = new SurfaceTexture(1);
    private Camera camera;
    //private int[] rgb;
    private Bitmap bitmap;
    private int previewWidth;
    private int previewHeight;
    private SurfaceHolder holder;

    private boolean isPreviewRunning = false;

    public CameraSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);

        holder = this.getHolder();
        holder.addCallback(this);
    }

    public void takePicture(Camera.PictureCallback callback) {
        camera.takePicture(null, callback, null);
    }

    /*@Override
    public void onPictureTaken(byte[] data, Camera camera) {

    }*/

    private void startPreview() {
        if (isPreviewRunning) {
            stopPreview();
        }

        camera = Camera.open();
        isPreviewRunning = true;
        Camera.Size size = camera.getParameters().getPreviewSize();;
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

    private  void stopPreview() {
        synchronized (this) {
            try {
                if (camera != null) {
                    camera.stopPreview();
                    isPreviewRunning  = false;
                    camera.release();
                }
            } catch (Exception e) {
                Log.e("Camera", e.getMessage());
            }
        }
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
//        decodeYUV420SP(rgb, data, previewWidth, previewHeight);
//        bitmap = Bitmap.createBitmap(rgb, previewWidth, previewHeight, Bitmap.Config.ARGB_8888);

        ByteArrayOutputStream os = new ByteArrayOutputStream(data.length);
        YuvImage image = new YuvImage(data, ImageFormat.NV21, previewWidth, previewHeight, null);
        Rect dest = new Rect(0, 0, previewWidth, previewHeight);
        image.compressToJpeg(dest, 100, os);
        byte bytes[] = os.toByteArray();

        bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

        Canvas canvas = holder.lockCanvas();

        Paint paint = new Paint();
        paint.setFilterBitmap(true);

        canvas.drawBitmap(bitmap, null, dest, paint);

        //this.draw(canvas);
        holder.unlockCanvasAndPost(canvas);
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
        stopPreview();
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
