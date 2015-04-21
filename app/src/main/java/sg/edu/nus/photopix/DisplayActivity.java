package sg.edu.nus.photopix;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

/**
 * Created by linxiuqing on 17/4/15.
 */
public class DisplayActivity extends MainActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.saved_display);

        final RelativeLayout layout = (RelativeLayout) findViewById(R.id.displayLayout);
        ImageView imageView = (ImageView) findViewById(R.id.savedView);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL);
        params.addRule(RelativeLayout.CENTER_VERTICAL);

        imageView.setImageBitmap(SavedImage.getImage());
    }

    public void goBack (View view) {
        finish ();
    }

    public void goToMain (View view){
        Intent intent;
        intent = new Intent (this, MainActivity.class);
        startActivity(intent);
        finish();
    }


}
