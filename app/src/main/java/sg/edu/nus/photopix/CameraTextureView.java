package sg.edu.nus.photopix;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.TextureView;
import android.widget.FrameLayout;

import java.io.IOException;


public class CameraTextureView extends TextureView
        implements TextureView.SurfaceTextureListener, SurfaceTexture.OnFrameAvailableListener {

    private static final String TAG = "MySurfaceView";

    private Camera camera;

    private boolean isPreviewRunning = false;

    public CameraTextureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setSurfaceTextureListener(this);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        startPreview(surface);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        stopPreview();
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        System.out.println("changed");
    }

    private void startPreview(SurfaceTexture surface) {
        camera = Camera.open();
        isPreviewRunning = true;

        try {
            camera.setPreviewTexture(surface);
            //surface.setOnFrameAvailableListener(this);
        } catch (IOException e) {
            e.printStackTrace();
        }

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
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        //System.out.println("changed");
    }
}
