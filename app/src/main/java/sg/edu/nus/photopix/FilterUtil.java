package sg.edu.nus.photopix;

import android.content.Context;

import java.util.LinkedList;
import java.util.List;

import jp.co.cyberagent.android.gpuimage.*;
import sg.edu.nus.photopix.filter.*;

public class FilterUtil {
    final FilterList myFilters;
    final FilterList adjustFilters;
    Context context;

    public FilterUtil(Context context) {
        this.context = context;
        myFilters = new FilterList();
        adjustFilters = new FilterList();

        createMyFilterList();
        createAdjustFilterList();
    }

    private void createMyFilterList() {
        myFilters.addFilter("Amaro", FilterType.I_AMARO);
        myFilters.addFilter("Brannan", FilterType.I_BRANNAN);
        myFilters.addFilter("Earlybird", FilterType.I_EARLYBIRD);
        myFilters.addFilter("Inkwell", FilterType.I_INKWELL);
        myFilters.addFilter("LordKelvin", FilterType.I_LORDKELVIN);
        myFilters.addFilter("Sierra", FilterType.I_SIERRA);
        myFilters.addFilter("Toaster", FilterType.I_TOASTER);
        myFilters.addFilter("Walden", FilterType.I_WALDEN);
        myFilters.addFilter("Xproll", FilterType.I_XPROII);
    }

    private void createAdjustFilterList() {
        adjustFilters.addFilter("Brightness", FilterType.BRIGHTNESS);
        adjustFilters.addFilter("Contrast", FilterType.CONTRAST);
        adjustFilters.addFilter("Saturation", FilterType.SATURATION);
        adjustFilters.addFilter("Exposure", FilterType.EXPOSURE);
        adjustFilters.addFilter("White Balance", FilterType.WHITE_BALANCE);
    }

    public GPUImageFilter getMyFilter(int num) {
        return createFilterForType(myFilters.getFilter(num), context);
    }

    public GPUImageFilter getAdjustFilter(int num) {
        return createFilterForType(adjustFilters.getFilter(num), context);
    }

    public int getMyFilterSize() {
        return myFilters.getSize();
    }
    public int getAdjustFilterSize() { return adjustFilters.getSize(); }

    public GPUImageFilter getMyFilter(final FilterType type) {
        return createFilterForType(type, context);
    }
    public GPUImageFilter getAdjustFilter(final FilterType type) {
        return createFilterForType(type, context);
    }

    private static GPUImageFilter createFilterForType(final FilterType type, Context context) {
        switch (type) {
            case CONTRAST:
                return new GPUImageContrastFilter(2.0f);
            case BRIGHTNESS:
                return new GPUImageBrightnessFilter(1.5f);
            case SATURATION:
                return new GPUImageSaturationFilter(1.0f);
            case EXPOSURE:
                return new GPUImageExposureFilter(0.0f);
            case WHITE_BALANCE:
                return new GPUImageWhiteBalanceFilter(5000.0f, 0.0f);
            case I_AMARO:
                return new IFAmaroFilter(context);
            case I_BRANNAN:
                return new IFBrannanFilter(context);
            case I_EARLYBIRD:
                return new IFEarlybirdFilter(context);
            case I_INKWELL:
                return new IFInkwellFilter(context);
            case I_LORDKELVIN:
                return new IFLordKelvinFilter(context);
            case I_SIERRA:
                return new IFSierraFilter(context);
            case I_TOASTER:
                return new IFToasterFilter(context);
            case I_WALDEN:
                return new IFWaldenFilter(context);
            case I_XPROII:
                return new IFXprollFilter(context);

            default:
                throw new IllegalStateException("No filter of that type!");
        }
    }

    public enum FilterType {
        CONTRAST, BRIGHTNESS,SATURATION, EXPOSURE, WHITE_BALANCE,
        I_AMARO, I_BRANNAN, I_EARLYBIRD, I_INKWELL, I_LORDKELVIN, I_SIERRA, I_TOASTER, I_WALDEN, I_XPROII
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
            if (filter instanceof GPUImageContrastFilter) {
                adjuster = new ContrastAdjuster().filter(filter);
            } else if (filter instanceof GPUImageBrightnessFilter) {
                adjuster = new BrightnessAdjuster().filter(filter);
            } else if (filter instanceof GPUImageSaturationFilter) {
                adjuster = new SaturationAdjuster().filter(filter);
            } else if (filter instanceof GPUImageExposureFilter) {
                adjuster = new ExposureAdjuster().filter(filter);
            } else if (filter instanceof GPUImageWhiteBalanceFilter) {
                adjuster = new WhiteBalanceAdjuster().filter(filter);
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

        private class ContrastAdjuster extends Adjuster<GPUImageContrastFilter> {
            @Override
            public GPUImageFilter adjust(final int percentage) {
                GPUImageFilter filter = getFilter();
                ((GPUImageContrastFilter)filter).setContrast(range(percentage, 0.0f, 2.0f));
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

        private class WhiteBalanceAdjuster extends Adjuster<GPUImageWhiteBalanceFilter> {
            @Override
            public GPUImageFilter adjust(final int percentage) {
                GPUImageFilter filter = getFilter();
                ((GPUImageWhiteBalanceFilter)filter).setTemperature(range(percentage, 2000.0f, 8000.0f));
                //getFilter().setTint(range(percentage, -100.0f, 100.0f));
                return filter;
            }
        }
    }
}


