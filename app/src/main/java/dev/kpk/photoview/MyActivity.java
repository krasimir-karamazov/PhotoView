package dev.kpk.photoview;

import android.graphics.Bitmap;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import uk.co.senab.photoview.PhotoView;
import uk.co.senab.photoview.PhotoViewAttacher;


public class MyActivity extends ActionBarActivity {
    PhotoView mImageView;
    PhotoViewAttacher mAttacher;
    private static final float FRAME_WIDTH = 2;
    private static final float FRAME_HEIGHT = 1.5f;
    private PointF rectCoords;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        final Drawable bitmap = getResources().getDrawable(R.drawable.light);

        mImageView = (PhotoView) findViewById(R.id.iv_photo);
        mImageView.setImageDrawable(bitmap);

        mAttacher = new PhotoViewAttacher(mImageView);
        mAttacher.setMaximumScale(25f);
        mAttacher.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener() {
            @Override
            public void onViewTap(View view, float x, float y) {
                rectCoords = new PointF(x, y);
                PointF dimensions = getFrameDimensions(bitmap.getIntrinsicHeight());
                mImageView.addCropRectOnCoordinates(rectCoords, dimensions.x, dimensions.y);
            }
        });
        mAttacher.setOnMatrixChangeListener(new PhotoViewAttacher.OnMatrixChangedListener() {
            @Override
            public void onMatrixChanged(RectF rect) {
                if(rectCoords != null) {
                    PointF dimensions = getFrameDimensions(bitmap.getIntrinsicHeight());
                    mImageView.addCropRectOnCoordinates(rectCoords, dimensions.x, dimensions.y);
                }
            }
        });
    }

    private PointF getFrameDimensions(int bitmapHeight) {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        float mXDpi = metrics.xdpi;
        float bmpWidth = mAttacher.getDisplayRect().width();
        float bmpHeight = mAttacher.getDisplayRect().height();
        float widthInInches = bmpWidth / mXDpi;
        float heightInches = bmpHeight / mXDpi;
        float scale = bmpWidth / bitmapHeight;
        float frameHeightInches = FRAME_HEIGHT * scale;
        float frameWidthInches = FRAME_WIDTH * scale;
        final float frameWidthPixels = (frameWidthInches / widthInInches) * bmpWidth;
        final float frameHeightPixels = (frameHeightInches / heightInches) * bmpHeight;

        return new PointF(frameWidthPixels, frameHeightPixels);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            mAttacher.setRotationTo(90);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
