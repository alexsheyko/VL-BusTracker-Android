package ru.vlbustracker.helpers;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;

import ru.vlbustracker.R;
import ru.vlbustracker.activities.MainActivity;
import ru.vlbustracker.models.Bus;


public class BusClusterRenderer extends DefaultClusterRenderer<BusItem> {
    private final IconGenerator mIconGenerator ;//= new IconGenerator(getApplicationContext());
    //private final IconGenerator mClusterIconGenerator;// = new IconGenerator(getApplicationContext());
    private final ImageView mImageView;
    //private final ImageView mClusterImageView;
    //private final int mDimension;
    private final Context mContext;
    private final Resources mRes;

    public BusClusterRenderer(Context context, GoogleMap map,
                              ClusterManager<BusItem> clusterManager) {
        super(context, map, clusterManager);
        mIconGenerator = new IconGenerator(context);
        //mClusterIconGenerator = new IconGenerator(context);

        //View multiProfile = getLayoutInflater().inflate(R.layout.multi_profile, null);
        //mClusterIconGenerator.setContentView(multiProfile);
        //mClusterImageView = (ImageView) multiProfile.findViewById(R.id.image);

        mImageView = new ImageView(context);
        //mDimension = (int) getResources().getDimension(R.dimen.custom_profile_image);
        //mImageView.setLayoutParams(new ViewGroup.LayoutParams(mDimension, mDimension));
        //int padding = (int) getResources().getDimension(R.dimen.custom_profile_padding);
        //mImageView.setPadding(padding, padding, padding, padding);
        mIconGenerator.setContentView(mImageView);
        mContext = context;
        mRes = context.getResources();

    }

    @Override
    protected void onBeforeClusterItemRendered(BusItem item, MarkerOptions markerOptions) {
        super.onBeforeClusterItemRendered(item, markerOptions);

        //markerOptions.title(item.getTitle());

        //mImageView.setImageResource(person.profilePhoto);
        //Bitmap icon = mIconGenerator.makeIcon();

        //ImageView mImageView = new ImageView();
        mImageView.setImageResource(R.drawable.ic_map_bus);
        Bitmap icon = mIconGenerator.makeIcon();
        //markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon)).title("2");
        //.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        Bus b = item.mBus;
        markerOptions.icon(
                BitmapDescriptorFactory.fromBitmap(
                        getIcoBus(b.getTitle(), b.getHeading(), b.isHidden()
                    )
                )
        )
        .title(b.getTitle())
        .snippet("№№: " + b.getBody());


    }

    @Override
    protected void onClusterItemRendered(BusItem clusterItem, Marker marker) {
        super.onClusterItemRendered(clusterItem, marker);

        //here you have access to the marker itself
    }

    private static Bitmap rotateBitmap(Bitmap source, float angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    private Bitmap getIcoBus(String text,Float angle, Boolean hide) {
        //BitmapFactory.decodeResource(this.getResources(), R.drawable.ic_bus_arrow)

        Integer idres = R.drawable.ic_map_bus;
        if (hide){
            idres = R.drawable.ic_map_bus_hide;
        }
        //BitmapDescriptorFactory.fromResource(R.drawable.ic_map_bus).

        //Bitmap bm = BitmapFactory.decodeResource(mContext.getResources(), idres) //ic_bus_arrow
        Bitmap bm = BitmapFactory.decodeResource(mRes, idres) //ic_bus_arrow
                .copy(Bitmap.Config.ARGB_8888, true);
        bm = rotateBitmap(bm,angle);

        Typeface tf = Typeface.create("Helvetica", Typeface.BOLD);

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        //paint.setColor(Color.WHITE);
        paint.setTypeface(tf);
        //paint.setTextAlign(Align.CENTER);
        //paint.setTextSize(convertToPixels(context, 11));
        paint.setTextSize(11);

        Rect textRect = new Rect();
        paint.getTextBounds(text, 0, text.length(), textRect);

        Canvas canvas = new Canvas(bm);

        //If the text is bigger than the canvas , reduce the font size
        //if(textRect.width() >= (canvas.getWidth() - 4))     //the padding on either sides is considered as 4, so as to appropriately fit in the text
        //    paint.setTextSize(convertToPixels(context, 7));        //Scaling needs to be used for different dpi's

        //Calculate the positions
        //int xPos = (canvas.getWidth() / 2) - 2;     //-2 is for regulating the x position offset
        int xPos = (canvas.getWidth() / 2) - 3*text.length();     //-2 is for regulating the x position offset

        //"- ((paint.descent() + paint.ascent()) / 2)" is the distance from the baseline to the center.
        //int yPos = (int) ((canvas.getHeight() / 2)); //- ((paint.descent() + paint.ascent()) / 2)) ;
        int yPos = (int) ((canvas.getHeight() /2 ) - ((paint.descent() + paint.ascent()) / 2)) +2;

        canvas.drawText(text, xPos, yPos, paint);

        return  bm;
    }
}