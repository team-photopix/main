package sg.edu.nus.photopix;

/**
 * Created by linxiuqing on 15/4/15.
 */
public class ImageCollector {
    private static String[] selectedImages = new String[9];
    public static void select(int index, String content) {
        selectedImages[index] = content;
    }
    public static String getPath(int index) {
        if (index < 9) {
            return selectedImages[index];
        }
        return "";
    }
}
