package sg.edu.nus.photopix;

import android.graphics.Bitmap;

/**
 * Created by linxiuqing on 17/4/15.
 */
public class SavedImage {
    private static Bitmap bitmap;
    public static void saveImage (Bitmap image){
        bitmap = image;
    }

    public static Bitmap getImage (){
        return bitmap;
    }
}