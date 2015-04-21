/*
 * Copyright (C) 2012 CyberAgent
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * modified from gpuimage-sample-ActivityCamera
 */

package sg.edu.nus.photopix;

import android.os.ResultReceiver;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TableRow;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import jp.co.cyberagent.android.gpuimage.GPUImage;
import jp.co.cyberagent.android.gpuimage.GPUImage.OnPictureSavedListener;
import jp.co.cyberagent.android.gpuimage.GPUImage3x3ConvolutionFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageBoxBlurFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageBrightnessFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageBulgeDistortionFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageCGAColorspaceFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageColorBalanceFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageColorInvertFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageContrastFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageCrosshatchFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageDilationFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageDirectionalSobelEdgeDetectionFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageEmbossFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageExposureFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageFalseColorFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageFilterGroup;
import jp.co.cyberagent.android.gpuimage.GPUImageGammaFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageGaussianBlurFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageGlassSphereFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageGrayscaleFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageHazeFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageHighlightShadowFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageHueFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageKuwaharaFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageLaplacianFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageLevelsFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageMonochromeFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageNonMaximumSuppressionFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageOpacityFilter;
import jp.co.cyberagent.android.gpuimage.GPUImagePixelationFilter;
import jp.co.cyberagent.android.gpuimage.GPUImagePosterizeFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageRGBDilationFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageRGBFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageSaturationFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageSepiaFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageSharpenFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageSketchFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageSmoothToonFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageSobelEdgeDetection;
import jp.co.cyberagent.android.gpuimage.GPUImageSphereRefractionFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageSwirlFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageToonFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageVignetteFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageWeakPixelInclusionFilter;
import jp.co.cyberagent.android.gpuimage.GPUImageWhiteBalanceFilter;
import utils.CameraHelper;
import utils.CameraHelper.CameraInfo2;
import utils.GPUImageFilterTools.FilterAdjuster;

@SuppressWarnings("deprecation")
public class CameraActivity extends ActionBarActivity implements OnSeekBarChangeListener, OnClickListener {

    private GPUImage gpuImage;
    private CameraHelper cameraHelper;
    private CameraLoader cameraLoader;
    private GPUImageFilter filter;
    private FilterAdjuster filterAdjuster;
    private SeekBar seekbar;
    private GLSurfaceView surfaceView;
    private TableRow filterChooser;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();
        seekbar = ((SeekBar) findViewById(R.id.seekBar));
        seekbar.setOnSeekBarChangeListener(this);
        findViewById(R.id.button_capture).setOnClickListener(this);

        surfaceView = (GLSurfaceView) findViewById(R.id.surfaceView);
        gpuImage = new GPUImage(this);

        filterChooser = (TableRow) findViewById(R.id.filter_choose);
        addFilterButtons();

        resetSelection();
        gpuImage.setGLSurfaceView(surfaceView);

        switchFilterTo(createFilterForType(CameraActivity.this, filters.filters.get(0)));
        seekbar.setVisibility(View.GONE);

        cameraHelper = new CameraHelper(this);
        cameraLoader = new CameraLoader();

        View cameraSwitchView = findViewById(R.id.img_switch_camera);
        cameraSwitchView.setOnClickListener(this);
        if (!cameraHelper.hasFrontCamera() || !cameraHelper.hasBackCamera()) {
            cameraSwitchView.setVisibility(View.GONE);
        }
    }

    private void addFilterButtons() {
        GPUImage imagefilter = new GPUImage(this);

        TableRow.LayoutParams params = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.MATCH_PARENT);
        params.setMargins(10, 10, 10, 10);

        LinearLayout firstButton = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.small_image_button, null);
        ImageButton imageb = (ImageButton) firstButton.getChildAt(0);
        imageb.setImageBitmap(
                imagefilter.getBitmapWithFilterApplied(
                        BitmapUtil.getRoundedCornerBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.color_wheel_icon), 10)));
        imageb.setTag(-1);
        final TextView text = (TextView) firstButton.getChildAt(1);
        text.setText("None");
        firstButton.setLayoutParams(params);
        filterChooser.addView(firstButton);
        imageb.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                switchFilterTo(createFilterForType(CameraActivity.this, filters.filters.get(0)));
                seekbar.setVisibility(View.GONE);
                resetSelection();
                text.setTextColor(getResources().getColor(R.color.select));
            }
        });


        for (int i = 0; i < filters.names.size(); i++) {
            final LinearLayout newButton = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.small_image_button, null);
            imagefilter.setFilter(createFilterForType(this, filters.filters.get(i)));
            ImageButton image = (ImageButton) newButton.getChildAt(0);
            image.setImageBitmap(
                    BitmapUtil.getRoundedCornerBitmap(imagefilter.getBitmapWithFilterApplied(
                            BitmapFactory.decodeResource(getResources(), R.drawable.color_wheel_icon)), 10));
            image.setTag(i);
            final TextView textv = (TextView) newButton.getChildAt(1);
            textv.setText(filters.names.get(i));
            newButton.setLayoutParams(params);
            filterChooser.addView(newButton);
            image.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    switchFilterTo(createFilterForType(CameraActivity.this, filters.filters.get((int) v.getTag())));
                    resetSelection();
                    textv.setTextColor(getResources().getColor(R.color.select));
                }
            });

        }
    }

    private void resetSelection() {
        for (int i = 0; i < filterChooser.getChildCount(); i++) {
            filterChooser.getChildAt(i).setBackgroundResource(R.color.bright);
            ((TextView)((LinearLayout)filterChooser.getChildAt(i)).getChildAt(1)).setTextColor(
                    getResources().getColor(R.color.white)
            );
        }
    }

    public void onHomeClicked(View v) {
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraLoader.onResume();
    }

    @Override
    protected void onPause() {
        cameraLoader.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        clear();
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.button_capture:
                if (cameraLoader.cameraInstance.getParameters().getFocusMode().equals(
                        Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                    takePicture();
                } else {
                    cameraLoader.cameraInstance.autoFocus(new Camera.AutoFocusCallback() {
                        @Override
                        public void onAutoFocus(final boolean success, final Camera camera) {
                            takePicture();
                        }
                    });
                }
                break;

            case R.id.img_switch_camera:
                cameraLoader.switchCamera();
                break;
        }
    }

    private static File getOutputMediaFile() {
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "Photopix");
        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + timeStamp + ".jpg");

        return mediaFile;
    }

    private void takePicture() {
        Parameters params = cameraLoader.cameraInstance.getParameters();
        params.setRotation(90);
        List<Camera.Size> sizes = params.getSupportedPictureSizes();
        Collections.sort(sizes, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size lhs, Camera.Size rhs) {
                return Integer.valueOf(lhs.width * lhs.height).compareTo(rhs.width * rhs.height);
            }
        });
        Camera.Size maxSize = sizes.get(sizes.size() - 1);
        params.setPictureSize(maxSize.width, maxSize.height);
        cameraLoader.cameraInstance.setParameters(params);
        cameraLoader.cameraInstance.takePicture(null, null,
                new Camera.PictureCallback() {

                    @Override
                    public void onPictureTaken(byte[] data, final Camera camera) {
                        final File pictureFile = getOutputMediaFile();

                        try {
                            FileOutputStream fos = new FileOutputStream(pictureFile);
                            fos.write(data);
                            fos.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        data = null;

                        Bitmap bitmap = BitmapUtil.decodeSampledBitmapFromResource(
                                pictureFile.getAbsolutePath()
                        , 512, 512);

                        bitmap = ExifUtil.rotateBitmap(pictureFile.getAbsolutePath(), bitmap);

                        //bitmap = BitmapFactory.decodeFile(pictureFile.getAbsolutePath());


                        final GLSurfaceView view = (GLSurfaceView) findViewById(R.id.surfaceView);
                        view.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

                        final String filename = System.currentTimeMillis() + ".jpg";
                        gpuImage.saveToPictures(bitmap, "photopix",
                                filename,
                                new OnPictureSavedListener() {

                                    @Override
                                    public void onPictureSaved(final Uri
                                                                       uri) {
                                        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
                                                Environment.DIRECTORY_PICTURES), "Photopix");
                                        File mediaFile = new File(mediaStorageDir.getPath() + File.separator +
                                                filename);
                                        pictureFile.delete();
                                        view.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

                                        PictureShowActivity.pictureFile = mediaFile;
                                        PictureShowActivity.uri = uri;

                                        Intent intent = new Intent(CameraActivity.this, PictureShowActivity.class);
                                        intent.putExtra("finisher", new ResultReceiver(null){
                                            @Override
                                            protected void onReceiveResult(int resultCode, Bundle resultData) {
                                                CameraActivity.this.finish();
                                            }
                                        });
                                        startActivityForResult(intent, 5);
                                    }
                                });
                    }
                });
    }

    private void switchFilterTo(final GPUImageFilter filter) {
        if (this.filter == null
                || (filter != null)) {
            this.filter = filter;
            gpuImage.setFilter(this.filter);
            filterAdjuster = new FilterAdjuster(this.filter);
            seekbar.setProgress(50);
            filterAdjuster.adjust(50);
            if (filterAdjuster.canAdjust()) {
                seekbar.setVisibility(View.VISIBLE);
            } else {
                seekbar.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void onProgressChanged(final SeekBar seekBar, final int progress,
            final boolean fromUser) {
        if (filterAdjuster != null) {
            filterAdjuster.adjust(progress);
        }
    }

    @Override
    public void onStartTrackingTouch(final SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(final SeekBar seekBar) {
    }

    private class CameraLoader {

        private int cameraId = 0;
        private Camera cameraInstance;

        public void onResume() {
            setUpCamera(cameraId);
        }

        public void onPause() {
            releaseCamera();
        }

        public void switchCamera() {
            releaseCamera();
            cameraId = (cameraId + 1) % cameraHelper.getNumberOfCameras();
            setUpCamera(cameraId);
        }

        private void setUpCamera(final int id) {
            cameraInstance = getCameraInstance(id);
            Parameters parameters = cameraInstance.getParameters();

            if (parameters.getSupportedFocusModes().contains(
                    Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
                parameters.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            }

            cameraInstance.setParameters(parameters);

            int orientation = cameraHelper.getCameraDisplayOrientation(
                    CameraActivity.this, cameraId);
            CameraInfo2 cameraInfo = new CameraInfo2();
            cameraHelper.getCameraInfo(cameraId, cameraInfo);
            boolean flipHorizontal = cameraInfo.facing == CameraInfo.CAMERA_FACING_FRONT;
            gpuImage.setScaleType(GPUImage.ScaleType.CENTER_CROP);
            gpuImage.setUpCamera(cameraInstance, orientation, flipHorizontal, false);
        }

        private Camera getCameraInstance(final int id) {
            Camera c = null;
            try {
                c = cameraHelper.openCamera(id);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return c;
        }

        private void releaseCamera() {
            cameraInstance.setPreviewCallback(null);
            cameraInstance.release();
            cameraInstance = null;
        }
    }

    public static class FilterList {
        public List<String> names = new LinkedList<String>();
        public List<FilterType> filters = new LinkedList<FilterType>();

        public void addFilter(final String name, final FilterType filter) {
            names.add(name);
            filters.add(filter);
        }
    }

    public enum FilterType {
        CONTRAST, GRAYSCALE, SHARPEN, SEPIA, SOBEL_EDGE_DETECTION, THREE_X_THREE_CONVOLUTION, FILTER_GROUP, EMBOSS, POSTERIZE, GAMMA, BRIGHTNESS, INVERT, HUE, PIXELATION,
        SATURATION, EXPOSURE, HIGHLIGHT_SHADOW, MONOCHROME, OPACITY, RGB, WHITE_BALANCE, VIGNETTE,
        GAUSSIAN_BLUR, CROSSHATCH, BOX_BLUR, CGA_COLORSPACE, DILATION, KUWAHARA, RGB_DILATION, SKETCH, TOON, SMOOTH_TOON, BULGE_DISTORTION, GLASS_SPHERE, HAZE, LAPLACIAN, NON_MAXIMUM_SUPPRESSION,
        SPHERE_REFRACTION, SWIRL, WEAK_PIXEL_INCLUSION, FALSE_COLOR, COLOR_BALANCE, LEVELS_FILTER_MIN
    }

    public static final FilterList filters = new FilterList() {{
        addFilter("Contrast", FilterType.CONTRAST);
        addFilter("Invert",FilterType.INVERT);
        addFilter("Pixelation",FilterType.PIXELATION);
        addFilter("Hue",FilterType.HUE);
        addFilter("Gamma",FilterType.GAMMA);
        addFilter("Brightness",FilterType.BRIGHTNESS);
        addFilter("Sepia",FilterType.SEPIA);
        addFilter("Grayscale",FilterType.GRAYSCALE);
        addFilter("Sharpness",FilterType.SHARPEN);
        addFilter("Edge",FilterType.SOBEL_EDGE_DETECTION);
        addFilter("Emboss",FilterType.EMBOSS);
        //addFilter("Posterize",FilterType.POSTERIZE);
        addFilter("Saturation",FilterType.SATURATION);
        addFilter("Exposure",FilterType.EXPOSURE);
        addFilter("Shadow",FilterType.HIGHLIGHT_SHADOW);
        addFilter("Mono",FilterType.MONOCHROME);
        addFilter("WB",FilterType.WHITE_BALANCE);
        //addFilter("Vignette",FilterType.VIGNETTE);
    }};

    private static GPUImageFilter createFilterForType(final Context context, final FilterType type) {
        switch (type) {
            case CONTRAST:
                return new GPUImageContrastFilter(2.0f);
            case GAMMA:
                return new GPUImageGammaFilter(2.0f);
            case INVERT:
                return new GPUImageColorInvertFilter();
            case PIXELATION:
                return new GPUImagePixelationFilter();
            case HUE:
                return new GPUImageHueFilter(90.0f);
            case BRIGHTNESS:
                return new GPUImageBrightnessFilter(0.5f);
            case GRAYSCALE:
                return new GPUImageGrayscaleFilter();
            case SEPIA:
                return new GPUImageSepiaFilter();
            case SHARPEN:
                GPUImageSharpenFilter sharpness = new GPUImageSharpenFilter();
                sharpness.setSharpness(2.0f);
                return sharpness;
            case SOBEL_EDGE_DETECTION:
                return new GPUImageSobelEdgeDetection();
            case THREE_X_THREE_CONVOLUTION:
                GPUImage3x3ConvolutionFilter convolution = new GPUImage3x3ConvolutionFilter();
                convolution.setConvolutionKernel(new float[] {
                        -1.0f, 0.0f, 1.0f,
                        -2.0f, 0.0f, 2.0f,
                        -1.0f, 0.0f, 1.0f
                });
                return convolution;
            case EMBOSS:
                return new GPUImageEmbossFilter();
            case POSTERIZE:
                return new GPUImagePosterizeFilter();
            case FILTER_GROUP:
                List<GPUImageFilter> filters = new LinkedList<GPUImageFilter>();
                filters.add(new GPUImageContrastFilter());
                filters.add(new GPUImageDirectionalSobelEdgeDetectionFilter());
                filters.add(new GPUImageGrayscaleFilter());
                return new GPUImageFilterGroup(filters);
            case SATURATION:
                return new GPUImageSaturationFilter(1.0f);
            case EXPOSURE:
                return new GPUImageExposureFilter(0.0f);
            case HIGHLIGHT_SHADOW:
                return new GPUImageHighlightShadowFilter(0.0f, 1.0f);
            case MONOCHROME:
                return new GPUImageMonochromeFilter(1.0f, new float[]{0.6f, 0.45f, 0.3f, 1.0f});
            case OPACITY:
                return new GPUImageOpacityFilter(1.0f);
            case RGB:
                return new GPUImageRGBFilter(1.0f, 1.0f, 1.0f);
            case WHITE_BALANCE:
                return new GPUImageWhiteBalanceFilter(5000.0f, 0.0f);
            case VIGNETTE:
                PointF centerPoint = new PointF();
                centerPoint.x = 0.5f;
                centerPoint.y = 0.5f;
                return new GPUImageVignetteFilter(centerPoint, new float[] {0.0f, 0.0f, 0.0f}, 0.3f, 0.75f);
            case GAUSSIAN_BLUR:
                return new GPUImageGaussianBlurFilter();
            case CROSSHATCH:
                return new GPUImageCrosshatchFilter();

            case BOX_BLUR:
                return new GPUImageBoxBlurFilter();
            case CGA_COLORSPACE:
                return new GPUImageCGAColorspaceFilter();
            case DILATION:
                return new GPUImageDilationFilter();
            case KUWAHARA:
                return new GPUImageKuwaharaFilter();
            case RGB_DILATION:
                return new GPUImageRGBDilationFilter();
            case SKETCH:
                return new GPUImageSketchFilter();
            case TOON:
                return new GPUImageToonFilter();
            case SMOOTH_TOON:
                return new GPUImageSmoothToonFilter();

            case BULGE_DISTORTION:
                return new GPUImageBulgeDistortionFilter();
            case GLASS_SPHERE:
                return new GPUImageGlassSphereFilter();
            case HAZE:
                return new GPUImageHazeFilter();
            case LAPLACIAN:
                return new GPUImageLaplacianFilter();
            case NON_MAXIMUM_SUPPRESSION:
                return new GPUImageNonMaximumSuppressionFilter();
            case SPHERE_REFRACTION:
                return new GPUImageSphereRefractionFilter();
            case SWIRL:
                return new GPUImageSwirlFilter();
            case WEAK_PIXEL_INCLUSION:
                return new GPUImageWeakPixelInclusionFilter();
            case FALSE_COLOR:
                return new GPUImageFalseColorFilter();
            case COLOR_BALANCE:
                return new GPUImageColorBalanceFilter();
            case LEVELS_FILTER_MIN:
                GPUImageLevelsFilter levelsFilter = new GPUImageLevelsFilter();
                levelsFilter.setMin(0.0f, 3.0f, 1.0f);
                return levelsFilter;

            default:
                throw new IllegalStateException("No filter of that type!");
        }

    }

    private void clear() {
        gpuImage = null;
        cameraHelper = null;
        cameraLoader = null;
        filter = null;
        filterAdjuster = null;
        surfaceView = null;
        filterChooser = null;
    }
}
