package sg.edu.nus.photopix;

import android.graphics.PointF;

import java.util.LinkedList;
import java.util.List;

import jp.co.cyberagent.android.gpuimage.*;

public class FilterUtil {
    final FilterList filters;
    final FilterList myFilters;
    final FilterList adjustFilters;
    public FilterUtil() {
        filters = new FilterList();
        myFilters = new FilterList();
        adjustFilters = new FilterList();

        filters.addFilter("Contrast", FilterType.CONTRAST);
        filters.addFilter("Invert", FilterType.INVERT);
        filters.addFilter("Pixelation", FilterType.PIXELATION);
        filters.addFilter("Hue", FilterType.HUE);
        filters.addFilter("Gamma", FilterType.GAMMA);
        filters.addFilter("Brightness", FilterType.BRIGHTNESS);
        filters.addFilter("Sepia", FilterType.SEPIA);
        filters.addFilter("Grayscale", FilterType.GRAYSCALE);
        filters.addFilter("Sharpness", FilterType.SHARPEN);
        filters.addFilter("Sobel Edge Detection", FilterType.SOBEL_EDGE_DETECTION);
        filters.addFilter("3x3 Convolution", FilterType.THREE_X_THREE_CONVOLUTION);
        filters.addFilter("Emboss", FilterType.EMBOSS);
        filters.addFilter("Posterize", FilterType.POSTERIZE);
        filters.addFilter("Grouped filters", FilterType.FILTER_GROUP);
        filters.addFilter("Saturation", FilterType.SATURATION);
        filters.addFilter("Exposure", FilterType.EXPOSURE);
        filters.addFilter("Highlight Shadow", FilterType.HIGHLIGHT_SHADOW);
        filters.addFilter("Monochrome", FilterType.MONOCHROME);
        filters.addFilter("Opacity", FilterType.OPACITY);
        filters.addFilter("RGB", FilterType.RGB);
        filters.addFilter("White Balance", FilterType.WHITE_BALANCE);
        filters.addFilter("Vignette", FilterType.VIGNETTE);
        filters.addFilter("ToneCurve", FilterType.TONE_CURVE);

        filters.addFilter("Gaussian Blur", FilterType.GAUSSIAN_BLUR);
        filters.addFilter("Crosshatch", FilterType.CROSSHATCH);

        filters.addFilter("Box Blur", FilterType.BOX_BLUR);
        filters.addFilter("CGA Color Space", FilterType.CGA_COLORSPACE);
        filters.addFilter("Dilation", FilterType.DILATION);
        filters.addFilter("Kuwahara", FilterType.KUWAHARA);
        filters.addFilter("RGB Dilation", FilterType.RGB_DILATION);
        filters.addFilter("Sketch", FilterType.SKETCH);
        filters.addFilter("Toon", FilterType.TOON);
        filters.addFilter("Smooth Toon", FilterType.SMOOTH_TOON);

        filters.addFilter("Bulge Distortion", FilterType.BULGE_DISTORTION);
        filters.addFilter("Glass Sphere", FilterType.GLASS_SPHERE);
        filters.addFilter("Haze", FilterType.HAZE);
        filters.addFilter("Laplacian", FilterType.LAPLACIAN);
        filters.addFilter("Non Maximum Suppression", FilterType.NON_MAXIMUM_SUPPRESSION);
        filters.addFilter("Sphere Refraction", FilterType.SPHERE_REFRACTION);
        filters.addFilter("Swirl", FilterType.SWIRL);
        filters.addFilter("Weak Pixel Inclusion", FilterType.WEAK_PIXEL_INCLUSION);
        filters.addFilter("False Color", FilterType.FALSE_COLOR);

        filters.addFilter("Color Balance", FilterType.COLOR_BALANCE);

        filters.addFilter("Levels Min (Mid Adjust)", FilterType.LEVELS_FILTER_MIN);

        createMyFilterList();
        createAdjustFilterList();

    }

    private void createMyFilterList() {
        myFilters.addFilter("Sepia", FilterType.SEPIA);
        myFilters.addFilter("Grayscale", FilterType.GRAYSCALE);
        myFilters.addFilter("Monochrome", FilterType.MONOCHROME);
        myFilters.addFilter("Sketch", FilterType.SKETCH);
        myFilters.addFilter("Sharpness", FilterType.SHARPEN);
        myFilters.addFilter("Gaussian Blur", FilterType.GAUSSIAN_BLUR);
        myFilters.addFilter("Grouped filters", FilterType.FILTER_GROUP);
        myFilters.addFilter("Sobel Edge Detection", FilterType.SOBEL_EDGE_DETECTION);
        myFilters.addFilter("Posterize", FilterType.POSTERIZE);
    }

    private void createAdjustFilterList() {
        adjustFilters.addFilter("Brightness", FilterType.BRIGHTNESS);
        adjustFilters.addFilter("Contrast", FilterType.CONTRAST);
        adjustFilters.addFilter("Saturation", FilterType.SATURATION);
        adjustFilters.addFilter("Exposure", FilterType.EXPOSURE);
        adjustFilters.addFilter("White Balance", FilterType.WHITE_BALANCE);
    }

    public GPUImageFilter getMyFilter(int num) {
        return createFilterForType(myFilters.getFilter(num));
    }

    public GPUImageFilter getAdjustFilter(int num) {
        return createFilterForType(adjustFilters.getFilter(num));
    }

    public int getMyFilterSize() {
        return myFilters.getSize();
    }
    public int getAdjustFilterSize() { return adjustFilters.getSize(); }

    public GPUImageFilter getMyFilter(final FilterType type) {
        return createFilterForType(type);
    }
    public GPUImageFilter getAdjustFilter(final FilterType type) {
        return createFilterForType(type);
    }

    private static GPUImageFilter createFilterForType(final FilterType type) {
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
                return new GPUImageBrightnessFilter(1.5f);
            case GRAYSCALE:
                return new GPUImageGrayscaleFilter();
            case SEPIA:
                return new GPUImageSepiaFilter();
            case SHARPEN:
                GPUImageSharpenFilter sharpness = new GPUImageSharpenFilter();
                sharpness.setSharpness(2.5f);
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

    public enum FilterType {
        CONTRAST, GRAYSCALE, SHARPEN, SEPIA, SOBEL_EDGE_DETECTION, THREE_X_THREE_CONVOLUTION, FILTER_GROUP, EMBOSS, POSTERIZE, GAMMA, BRIGHTNESS, INVERT, HUE, PIXELATION,
        SATURATION, EXPOSURE, HIGHLIGHT_SHADOW, MONOCHROME, OPACITY, RGB, WHITE_BALANCE, VIGNETTE, TONE_CURVE, BLEND_COLOR_BURN, BLEND_COLOR_DODGE, BLEND_DARKEN, BLEND_DIFFERENCE,
        BLEND_DISSOLVE, BLEND_EXCLUSION, BLEND_SOURCE_OVER, BLEND_HARD_LIGHT, BLEND_LIGHTEN, BLEND_ADD, BLEND_DIVIDE, BLEND_MULTIPLY, BLEND_OVERLAY, BLEND_SCREEN, BLEND_ALPHA,
        BLEND_COLOR, BLEND_HUE, BLEND_SATURATION, BLEND_LUMINOSITY, BLEND_LINEAR_BURN, BLEND_SOFT_LIGHT, BLEND_SUBTRACT, BLEND_CHROMA_KEY, BLEND_NORMAL, LOOKUP_AMATORKA,
        GAUSSIAN_BLUR, CROSSHATCH, BOX_BLUR, CGA_COLORSPACE, DILATION, KUWAHARA, RGB_DILATION, SKETCH, TOON, SMOOTH_TOON, BULGE_DISTORTION, GLASS_SPHERE, HAZE, LAPLACIAN, NON_MAXIMUM_SUPPRESSION,
        SPHERE_REFRACTION, SWIRL, WEAK_PIXEL_INCLUSION, FALSE_COLOR, COLOR_BALANCE, LEVELS_FILTER_MIN
    }

    private static class FilterList {
        public List<String> names = new LinkedList<String>();
        public List<FilterType> filters = new LinkedList<FilterType>();

        public void addFilter(final String name, final FilterType filter) {
            names.add(name);
            filters.add(filter);
        }

        public FilterType getFilter(int num) {
            return filters.get(num);
        }
        public int getSize() {
            return filters.size();
        }
    }

    public static class FilterAdjuster {
        private final Adjuster<? extends GPUImageFilter> adjuster;

        public FilterAdjuster(final GPUImageFilter filter) {
            if (filter instanceof GPUImageSharpenFilter) {
                adjuster = new SharpnessAdjuster().filter(filter);
            } else if (filter instanceof GPUImageSepiaFilter) {
                adjuster = new SepiaAdjuster().filter(filter);
            } else if (filter instanceof GPUImageContrastFilter) {
                adjuster = new ContrastAdjuster().filter(filter);
            } else if (filter instanceof GPUImageGammaFilter) {
                adjuster = new GammaAdjuster().filter(filter);
            } else if (filter instanceof GPUImageBrightnessFilter) {
                adjuster = new BrightnessAdjuster().filter(filter);
            } else if (filter instanceof GPUImageSobelEdgeDetection) {
                adjuster = new SobelAdjuster().filter(filter);
            } else if (filter instanceof GPUImageEmbossFilter) {
                adjuster = new EmbossAdjuster().filter(filter);
            } else if (filter instanceof GPUImage3x3TextureSamplingFilter) {
                adjuster = new GPU3x3TextureAdjuster().filter(filter);
            } else if (filter instanceof GPUImageHueFilter) {
                adjuster = new HueAdjuster().filter(filter);
            } else if (filter instanceof GPUImagePosterizeFilter) {
                adjuster = new PosterizeAdjuster().filter(filter);
            } else if (filter instanceof GPUImagePixelationFilter) {
                adjuster = new PixelationAdjuster().filter(filter);
            } else if (filter instanceof GPUImageSaturationFilter) {
                adjuster = new SaturationAdjuster().filter(filter);
            } else if (filter instanceof GPUImageExposureFilter) {
                adjuster = new ExposureAdjuster().filter(filter);
            } else if (filter instanceof GPUImageHighlightShadowFilter) {
                adjuster = new HighlightShadowAdjuster().filter(filter);
            } else if (filter instanceof GPUImageMonochromeFilter) {
                adjuster = new MonochromeAdjuster().filter(filter);
            } else if (filter instanceof GPUImageOpacityFilter) {
                adjuster = new OpacityAdjuster().filter(filter);
            } else if (filter instanceof GPUImageRGBFilter) {
                adjuster = new RGBAdjuster().filter(filter);
            } else if (filter instanceof GPUImageWhiteBalanceFilter) {
                adjuster = new WhiteBalanceAdjuster().filter(filter);
            } else if (filter instanceof GPUImageVignetteFilter) {
                adjuster = new VignetteAdjuster().filter(filter);
            } else if (filter instanceof GPUImageDissolveBlendFilter) {
                adjuster = new DissolveBlendAdjuster().filter(filter);
            } else if (filter instanceof GPUImageGaussianBlurFilter) {
                adjuster = new GaussianBlurAdjuster().filter(filter);
            } else if (filter instanceof GPUImageCrosshatchFilter) {
                adjuster = new CrosshatchBlurAdjuster().filter(filter);
            } else if (filter instanceof GPUImageBulgeDistortionFilter) {
                adjuster = new BulgeDistortionAdjuster().filter(filter);
            } else if (filter instanceof GPUImageGlassSphereFilter) {
                adjuster = new GlassSphereAdjuster().filter(filter);
            } else if (filter instanceof GPUImageHazeFilter) {
                adjuster = new HazeAdjuster().filter(filter);
            } else if (filter instanceof GPUImageSphereRefractionFilter) {
                adjuster = new SphereRefractionAdjuster().filter(filter);
            } else if (filter instanceof GPUImageSwirlFilter) {
                adjuster = new SwirlAdjuster().filter(filter);
            } else if (filter instanceof GPUImageColorBalanceFilter) {
                adjuster = new ColorBalanceAdjuster().filter(filter);
            } else if (filter instanceof GPUImageLevelsFilter) {
                adjuster = new LevelsMinMidAdjuster().filter(filter);
            } else {
                adjuster = null;
            }
        }

        public boolean canAdjust() {
            return adjuster != null;
        }

        public GPUImageFilter adjust(final int percentage) {
            GPUImageFilter filter = new GPUImageFilter();
            if (adjuster != null) {
                filter = adjuster.adjust(percentage);
            }
            return filter;
        }

        private abstract class Adjuster<T extends GPUImageFilter> {
            private T filter;

            @SuppressWarnings("unchecked")
            public Adjuster<T> filter(final GPUImageFilter filter) {
                this.filter = (T) filter;
                return this;
            }

            public T getFilter() {
                return filter;
            }

            public abstract GPUImageFilter adjust(int percentage);

            protected float range(final int percentage, final float start, final float end) {
                return (end - start) * percentage / 100.0f + start;
            }

            protected int range(final int percentage, final int start, final int end) {
                return (end - start) * percentage / 100 + start;
            }
        }

        private class SharpnessAdjuster extends Adjuster<GPUImageSharpenFilter> {
            @Override
            public GPUImageFilter adjust(final int percentage) {
                GPUImageFilter filter = getFilter();
                ((GPUImageSharpenFilter)filter).setSharpness(range(percentage, -4.0f, 4.0f));
                return filter;
            }
        }

        private class PixelationAdjuster extends Adjuster<GPUImagePixelationFilter> {
            @Override
            public GPUImageFilter adjust(final int percentage) {
                GPUImageFilter filter = getFilter();
                ((GPUImagePixelationFilter)filter).setPixel(range(percentage, 1.0f, 100.0f));
                return filter;
            }
        }

        private class HueAdjuster extends Adjuster<GPUImageHueFilter> {
            @Override
            public GPUImageFilter adjust(final int percentage) {
                GPUImageFilter filter = getFilter();
                ((GPUImageHueFilter)filter).setHue(range(percentage, 0.0f, 360.0f));
                return filter;
            }
        }

        private class ContrastAdjuster extends Adjuster<GPUImageContrastFilter> {
            @Override
            public GPUImageFilter adjust(final int percentage) {
                GPUImageFilter filter = getFilter();
                ((GPUImageContrastFilter)filter).setContrast(range(percentage, 0.0f, 2.0f));
                return filter;
            }
        }

        private class GammaAdjuster extends Adjuster<GPUImageGammaFilter> {
            @Override
            public GPUImageFilter adjust(final int percentage) {
                GPUImageFilter filter = getFilter();
                ((GPUImageGammaFilter)filter).setGamma(range(percentage, 0.0f, 3.0f));
                return filter;
            }
        }

        private class BrightnessAdjuster extends Adjuster<GPUImageBrightnessFilter> {
            @Override
            public GPUImageFilter adjust(final int percentage) {
                GPUImageFilter filter = getFilter();
                ((GPUImageBrightnessFilter)filter).setBrightness(range(percentage, -1.0f, 1.0f));
                return filter;
            }
        }

        private class SepiaAdjuster extends Adjuster<GPUImageSepiaFilter> {
            @Override
            public GPUImageFilter adjust(final int percentage) {
                GPUImageFilter filter = getFilter();
                ((GPUImageSepiaFilter)filter).setIntensity(range(percentage, 0.0f, 2.0f));
                return filter;
            }
        }

        private class SobelAdjuster extends Adjuster<GPUImageSobelEdgeDetection> {
            @Override
            public GPUImageFilter adjust(final int percentage) {
                GPUImageFilter filter = getFilter();
                ((GPUImageSobelEdgeDetection)filter).setLineSize(range(percentage, 0.0f, 5.0f));
                return filter;
            }
        }

        private class EmbossAdjuster extends Adjuster<GPUImageEmbossFilter> {
            @Override
            public GPUImageFilter adjust(final int percentage) {
                GPUImageFilter filter = getFilter();
                ((GPUImageEmbossFilter)filter).setIntensity(range(percentage, 0.0f, 4.0f));
                return filter;
            }
        }

        private class PosterizeAdjuster extends Adjuster<GPUImagePosterizeFilter> {
            @Override
            public GPUImageFilter adjust(final int percentage) {
                GPUImageFilter filter = getFilter();
                // In theorie to 256, but only first 50 are interesting
                ((GPUImagePosterizeFilter)filter).setColorLevels(range(percentage, 1, 50));
                return filter;
            }
        }

        private class GPU3x3TextureAdjuster extends Adjuster<GPUImage3x3TextureSamplingFilter> {
            @Override
            public GPUImageFilter adjust(final int percentage) {
                GPUImageFilter filter = getFilter();
                ((GPUImage3x3TextureSamplingFilter)filter).setLineSize(range(percentage, 0.0f, 5.0f));
                return filter;
            }
        }

        private class SaturationAdjuster extends Adjuster<GPUImageSaturationFilter> {
            @Override
            public GPUImageFilter adjust(final int percentage) {
                GPUImageFilter filter = getFilter();
                ((GPUImageSaturationFilter)filter).setSaturation(range(percentage, 0.0f, 2.0f));
                return filter;
            }
        }

        private class ExposureAdjuster extends Adjuster<GPUImageExposureFilter> {
            @Override
            public GPUImageFilter adjust(final int percentage) {
                GPUImageFilter filter = getFilter();
                ((GPUImageExposureFilter)filter).setExposure(range(percentage, -2.50f, 2.50f));
                return filter;
            }
        }

        private class HighlightShadowAdjuster extends Adjuster<GPUImageHighlightShadowFilter> {
            @Override
            public GPUImageFilter adjust(final int percentage) {
                GPUImageFilter filter = getFilter();
                ((GPUImageHighlightShadowFilter)filter).setShadows(range(percentage, 0.0f, 1.0f));
                ((GPUImageHighlightShadowFilter)filter).setHighlights(range(percentage, 0.0f, 1.0f));
                return filter;
            }
        }

        private class MonochromeAdjuster extends Adjuster<GPUImageMonochromeFilter> {
            @Override
            public GPUImageFilter adjust(final int percentage) {
                GPUImageFilter filter = getFilter();
                ((GPUImageMonochromeFilter)filter).setIntensity(range(percentage, 0.0f, 1.0f));
                //getFilter().setColor(new float[]{0.6f, 0.45f, 0.3f, 1.0f});
                return filter;
            }
        }

        private class OpacityAdjuster extends Adjuster<GPUImageOpacityFilter> {
            @Override
            public GPUImageFilter adjust(final int percentage) {
                GPUImageFilter filter = getFilter();
                ((GPUImageOpacityFilter)filter).setOpacity(range(percentage, 0.0f, 1.0f));
                return filter;
            }
        }

        private class RGBAdjuster extends Adjuster<GPUImageRGBFilter> {
            @Override
            public GPUImageFilter adjust(final int percentage) {
                GPUImageFilter filter = getFilter();
                ((GPUImageRGBFilter)filter).setRed(range(percentage, 0.0f, 1.0f));
                //getFilter().setGreen(range(percentage, 0.0f, 1.0f));
                //getFilter().setBlue(range(percentage, 0.0f, 1.0f));
                return filter;
            }
        }

        private class WhiteBalanceAdjuster extends Adjuster<GPUImageWhiteBalanceFilter> {
            @Override
            public GPUImageFilter adjust(final int percentage) {
                GPUImageFilter filter = getFilter();
                ((GPUImageWhiteBalanceFilter)filter).setTemperature(range(percentage, 2000.0f, 8000.0f));
                //getFilter().setTint(range(percentage, -100.0f, 100.0f));
                return filter;
            }
        }

        private class VignetteAdjuster extends Adjuster<GPUImageVignetteFilter> {
            @Override
            public GPUImageFilter adjust(final int percentage) {
                GPUImageFilter filter = getFilter();
                ((GPUImageVignetteFilter)filter).setVignetteStart(range(percentage, 0.0f, 1.0f));
                return filter;
            }
        }

        private class DissolveBlendAdjuster extends Adjuster<GPUImageDissolveBlendFilter> {
            @Override
            public GPUImageFilter adjust(final int percentage) {
                GPUImageFilter filter = getFilter();
                ((GPUImageDissolveBlendFilter)filter).setMix(range(percentage, 0.0f, 1.0f));
                return filter;
            }
        }

        private class GaussianBlurAdjuster extends Adjuster<GPUImageGaussianBlurFilter> {
            @Override
            public GPUImageFilter adjust(final int percentage) {
                GPUImageFilter filter = getFilter();
                ((GPUImageGaussianBlurFilter)filter).setBlurSize(range(percentage, 0.0f, 1.0f));
                return filter;
            }
        }

        private class CrosshatchBlurAdjuster extends Adjuster<GPUImageCrosshatchFilter> {
            @Override
            public GPUImageFilter adjust(final int percentage) {
                GPUImageFilter filter = getFilter();
                ((GPUImageCrosshatchFilter)filter).setCrossHatchSpacing(range(percentage, 0.0f, 0.06f));
                ((GPUImageCrosshatchFilter)filter).setLineWidth(range(percentage, 0.0f, 0.006f));
                return filter;
            }
        }

        private class BulgeDistortionAdjuster extends Adjuster<GPUImageBulgeDistortionFilter> {
            @Override
            public GPUImageFilter adjust(final int percentage) {
                GPUImageFilter filter = getFilter();
                ((GPUImageBulgeDistortionFilter)filter).setRadius(range(percentage, 0.0f, 1.0f));
                ((GPUImageBulgeDistortionFilter)filter).setScale(range(percentage, -1.0f, 1.0f));
                return filter;
            }
        }

        private class GlassSphereAdjuster extends Adjuster<GPUImageGlassSphereFilter> {
            @Override
            public GPUImageFilter adjust(final int percentage) {
                GPUImageFilter filter = getFilter();
                ((GPUImageGlassSphereFilter)filter).setRadius(range(percentage, 0.0f, 1.0f));
                return filter;
            }
        }

        private class HazeAdjuster extends Adjuster<GPUImageHazeFilter> {
            @Override
            public GPUImageFilter adjust(final int percentage) {
                GPUImageFilter filter = getFilter();
                ((GPUImageHazeFilter)filter).setDistance(range(percentage, -0.3f, 0.3f));
                ((GPUImageHazeFilter)filter).setSlope(range(percentage, -0.3f, 0.3f));
                return filter;
            }
        }

        private class SphereRefractionAdjuster extends Adjuster<GPUImageSphereRefractionFilter> {
            @Override
            public GPUImageFilter adjust(final int percentage) {
                GPUImageFilter filter = getFilter();
                ((GPUImageSphereRefractionFilter)filter).setRadius(range(percentage, 0.0f, 1.0f));
                return filter;
            }
        }

        private class SwirlAdjuster extends Adjuster<GPUImageSwirlFilter> {
            @Override
            public GPUImageFilter adjust(final int percentage) {
                GPUImageFilter filter = getFilter();
                ((GPUImageSwirlFilter)filter).setAngle(range(percentage, 0.0f, 2.0f));
                return filter;
            }
        }

        private class ColorBalanceAdjuster extends Adjuster<GPUImageColorBalanceFilter> {

            @Override
            public GPUImageFilter adjust(int percentage) {
                GPUImageFilter filter = getFilter();
                ((GPUImageColorBalanceFilter)filter).setMidtones(new float[]{
                        range(percentage, 0.0f, 1.0f),
                        range(percentage / 2, 0.0f, 1.0f),
                        range(percentage / 3, 0.0f, 1.0f)});
                return filter;
            }
        }

        private class LevelsMinMidAdjuster extends Adjuster<GPUImageLevelsFilter> {
            @Override
            public GPUImageFilter adjust(int percentage) {
                GPUImageFilter filter = getFilter();
                ((GPUImageLevelsFilter)filter).setMin(0.0f, range(percentage, 0.0f, 1.0f) , 1.0f);
                return filter;
            }
        }
    }
}


