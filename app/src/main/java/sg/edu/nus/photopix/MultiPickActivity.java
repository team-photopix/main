package sg.edu.nus.photopix;

/**
 * Created by linxiuqing on 12/4/15.
 */

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

public class MultiPickActivity extends Activity {
    private int count;
    private Bitmap[] thumbnails;
    private boolean[] thumbnailsselection;
    private String[] arrPath;
    private ImageAdapter imageAdapter;
    public static int totalImage;
    private Cursor imagecursor;
    public static String selectImages = "";
    private int clickcount=0;

    public void goBackToMain(View view)
    {
        finish();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_selection);

        final String[] columns = {MediaStore.Images.Media.DATA, MediaStore.Images.Media._ID};
        final String orderBy = MediaStore.Images.Media.DATE_ADDED;
        imagecursor =getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, columns, null,null, orderBy);
        int image_column_index = imagecursor.getColumnIndex(MediaStore.Images.Media._ID);
        this.count = imagecursor.getCount();
        this.thumbnails = new Bitmap[this.count];
        this.arrPath = new String[this.count];
        this.thumbnailsselection = new boolean[this.count];
        for (int i = 0; i < this.count; i++) {
            imagecursor.moveToPosition(i);
            int id = imagecursor.getInt(image_column_index);
            int dataColumnIndex = imagecursor.getColumnIndex(MediaStore.Images.Media.DATA);

            thumbnails[i] = MediaStore.Images.Thumbnails.getThumbnail(getApplicationContext().getContentResolver(), id, MediaStore.Images.Thumbnails.MICRO_KIND, null);
            arrPath[i] = imagecursor.getString(dataColumnIndex);
            thumbnails[i] = ExifUtil.rotateBitmap(arrPath[i], thumbnails[i]);

        }
        GridView imagegrid = (GridView) findViewById(R.id.PhoneImageGrid);
        imageAdapter = new ImageAdapter();
        imagegrid.setAdapter(imageAdapter);
        imagecursor.close();

        final Button selectBtn = (Button) findViewById(R.id.selectBtn);
        selectBtn.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                final int len = thumbnailsselection.length;
                int photoCount = 0;
                int j=0;
                for (int i = 0; i < len; i++) {
                    if (thumbnailsselection[i]) {
                        photoCount++;
                        ImageCollector.select(j, arrPath[i]);
                        j++;
                    }
                }
                if (photoCount == 0) {
                    Toast.makeText(getApplicationContext(),"Please select at least one image", Toast.LENGTH_LONG).show();
                } else if(photoCount <10) {
                    Toast.makeText(getApplicationContext(),"You've selected Total " + photoCount + " image(s).", Toast.LENGTH_LONG).show();
                    totalImage=photoCount;
                    imagecursor.close();
                    //Log.d("SelectedImages", selectImages);

                    Intent myIntent;
                    myIntent = new Intent(getApplicationContext(), FreeStyleActivity.class);
                    startActivity(myIntent);
                }
            }

        });
    }

    public class ImageAdapter extends BaseAdapter {
        private LayoutInflater mInflater;

        public ImageAdapter() {
            mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public int getCount() {
            return count;
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = mInflater.inflate(R.layout.galleryitem, null);
                holder.imageview = (ImageView) convertView.findViewById(R.id.thumbImage);
                holder.checkbox = (CheckBox) convertView.findViewById(R.id.itemCheckBox);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.checkbox.setId(position);
            holder.imageview.setId(position);
            holder.checkbox.setOnClickListener(new OnClickListener() {

                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    CheckBox cb = (CheckBox) v;
                    int id = cb.getId();
                    if (thumbnailsselection[id]) {
                        cb.setChecked(false);
                        thumbnailsselection[id] = false;
                        clickcount--;
                    } else {
                        if(clickcount>=0 && clickcount<9){
                            cb.setChecked(true);
                            thumbnailsselection[id] = true;
                            clickcount++;

                        }else if (clickcount >=9) {
                            Toast.makeText(getApplicationContext(),"You have reached maximum 9 photos.", Toast.LENGTH_SHORT).show();
                            holder.checkbox.setChecked(false);
                        }
                    }
                }
            });

            holder.imageview.setOnClickListener(new OnClickListener() {

                public void onClick(View v) {
                    // TODO Auto-generated method stub
                    ImageView view = (ImageView) v;
                    int id = view.getId();
                    if (thumbnailsselection[id]) {
                        holder.checkbox.setChecked(false);
                        thumbnailsselection[id] = false;
                        clickcount--;

                    } else {
                        if(clickcount >=0 && clickcount<9) {
                            clickcount++;
                            holder.checkbox.setChecked(true);
                            thumbnailsselection[id] = true;
                        }
                        else if (clickcount >=9) {
                            Toast.makeText(getApplicationContext(),"You have reached maximum 9 photos.", Toast.LENGTH_SHORT).show();
                            holder.checkbox.setChecked(false);

                        }
                    }
                }
            });
            holder.imageview.setImageBitmap(thumbnails[position]);
            holder.checkbox.setChecked(thumbnailsselection[position]);
            holder.id = position;
            return convertView;
        }
    }

    class ViewHolder {
        ImageView imageview;
        CheckBox checkbox;
        int id;
    }
}