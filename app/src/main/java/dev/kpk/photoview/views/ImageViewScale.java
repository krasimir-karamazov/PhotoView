package dev.kpk.photoview.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by krasimir.karamazov on 10/28/2014.
 */
public class ImageViewScale extends ImageView implements View.OnTouchListener {
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //canvas.save();
        //canvas.scale(mScale, mScale);
        mCanvasMatrix=canvas.getMatrix();
        Paint myPaint = new Paint();
        myPaint.setColor(Color.GREEN);
        myPaint.setStyle(Paint.Style.STROKE);
        myPaint.setStrokeWidth(1);
        if(mCanvasMatrix!=null){
            if(orignalRect!=null)
                mCanvasMatrix.mapRect(orignalRect);
        }
        if(orignalRect!=null){
            canvas.drawRect(orignalRect,myPaint);
        }
        //canvas.drawRect(rect_x1-30,rect_y1-30, rect_x1+30, rect_y1+30, myPaint);
        int canavs_width=canvas.getWidth();
        int canavs_height=canvas.getHeight();
        canvas.setMatrix(mCanvasMatrix);
        //canvas.setMatrix(mMatrix);
        if(mDrawable!=null){
            //mDrawable.draw(canvas);
            Log.v("Log_tag", "draw with Canvas is done  W:"+ canavs_width+"H:"+ canavs_height);
        }
        //canvas.restore();
    }

    private float MAX_SCALE = 2f;
    private int DOUBLE_TAP_SECOND = 400;

    private float CANVAS_MAX_SCALE=2f;

    float rect_x1=50;
    float rect_y1=150;

    private Matrix mMatrix;
    private Matrix mCanvasMatrix;

    private final float[] mCanvasMatrixValues=new float[9];
    private final float[] mMatrixValues = new float[9];
    RectF orignalRect;
    private Drawable mDrawable;
    private ImageView mImageView;
    // display width height.
    private int mWidth;
    private int mHeight;

    private int mIntrinsicWidth;
    private int mIntrinsicHeight;

    private int mCanvasWidth;
    private int mCanvasHeight;


    private float mScale;
    private float mMinScale;

    private float mCanvasMinScale;

    // double tap for determining
    private long mLastTime = 0;
    private boolean isDoubleTap;
    private int mDoubleTapX;
    private int mDoubleTapY;

    private float mPrevDistance;
    private boolean isScaling;

    private int mPrevMoveX;
    private int mPrevMoveY;

    String TAG = "ScaleImageView";

    public ImageViewScale(Context context, AttributeSet attr) {
        super(context, attr);
        initialize();
    }

    public ImageViewScale(Context context) {
        super(context);
        initialize();
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
        super.setImageBitmap(bm);
        this.initialize();

    }

    private void initialize() {
        this.setScaleType(ImageView.ScaleType.MATRIX);
        this.mMatrix = new Matrix();
        Drawable d = getDrawable();
        mDrawable=d;
        if (d != null) {
            mIntrinsicWidth = d.getIntrinsicWidth();
            mIntrinsicHeight = d.getIntrinsicHeight();
            setOnTouchListener(this);
        }
    }

    @Override
    protected boolean setFrame(int l, int t, int r, int b) {
        Log.v("Log_tag", "Size are here " + l + t + r + b);
        mWidth = r - l;
        mHeight = b - t;

        mMatrix.reset();
        mScale = (float) r / (float) mIntrinsicWidth;
        int paddingHeight = 0;
        int paddingWidth = 0;
        // scaling vertical
        if (mScale * mIntrinsicHeight > mHeight) {
            mScale = (float) mHeight / (float) mIntrinsicHeight;
            mMatrix.postScale(mScale, mScale);
            paddingWidth = (r - mWidth) / 2;
            paddingHeight = 0;
            // scaling horizontal
        } else {
            mMatrix.postScale(mScale, mScale);
            paddingHeight = (b - mHeight) / 2;
            paddingWidth = 0;
        }
        mMatrix.postTranslate(paddingWidth, paddingHeight);

        setImageMatrix(mMatrix);
        mMinScale = mScale;
        zoomTo(mScale, mWidth / 2, mHeight / 2);
        cutting();
        return super.setFrame(l, t, r, b);
    }

    protected float getValue(Matrix matrix, int whichValue) {
        matrix.getValues(mMatrixValues);
        return mMatrixValues[whichValue];
    }
    //New Added
    protected float getCanvasValue(Matrix matrix,int whichvalues){
        mCanvasMatrix.getValues(mCanvasMatrixValues);
        return mCanvasMatrixValues[whichvalues];
    }

    protected float getScale() {
        return getValue(mMatrix, Matrix.MSCALE_X);
    }

    //New added  Method
    protected float getCanvasScale(){
        return getCanvasValue(mCanvasMatrix, Matrix.MSCALE_X);
    }

    protected float getTranslateX() {
        return getValue(mMatrix, Matrix.MTRANS_X);
    }

    //New added Method
    protected float getCanvasTranslateX(){
        return getCanvasValue(mCanvasMatrix, Matrix.MTRANS_X);
    }


    protected float getTranslateY() {
        return getValue(mMatrix, Matrix.MTRANS_Y);
    }

    //New Added Method
    protected float getCanvasTranslateY(){
        return getCanvasValue(mCanvasMatrix, Matrix.MTRANS_Y);
    }

    protected void maxZoomTo(int x, int y) {
        if (mMinScale != getScale() && (getScale() - mMinScale) > 0.1f) {
            // threshold 0.1f
            float scale = mMinScale / getScale();
            zoomTo(scale, x, y);
        } else {
            float scale = MAX_SCALE / getScale();
            zoomTo(scale, x, y);
        }
    }




    protected void zoomTo(float scale, int x, int y) {
        if (getScale() * scale < mMinScale) {
            return;
        }
        if (scale >= 1 && getScale() * scale > MAX_SCALE) {
            return;
        }
        mMatrix.postScale(scale, scale);
        // move to center
        mMatrix.postTranslate(-(mWidth * scale - mWidth) / 2,
                -(mHeight * scale - mHeight) / 2);

        // move x and y distance
        mMatrix.postTranslate(-(x - (mWidth / 2)) * scale, 0);
        mMatrix.postTranslate(0, -(y - (mHeight / 2)) * scale);
        setImageMatrix(mMatrix);
    }


    protected void zoomToCanvas(float scale,int x,int y){
        if(getCanvasScale()* scale<mCanvasMinScale){
            return;
        }

        if(scale>=1 && getCanvasScale()*scale> CANVAS_MAX_SCALE){
            return;
        }
        mCanvasMatrix.postScale(scale, scale);



    }

    public void cutting() {
        int width = (int) (mIntrinsicWidth * getScale());
        int height = (int) (mIntrinsicHeight * getScale());
        if (getTranslateX() < -(width - mWidth)) {
            mMatrix.postTranslate(-(getTranslateX() + width - mWidth), 0);
        }
        if (getTranslateX() > 0) {
            mMatrix.postTranslate(-getTranslateX(), 0);
        }
        if (getTranslateY() < -(height - mHeight)) {
            mMatrix.postTranslate(0, -(getTranslateY() + height - mHeight));
        }
        if (getTranslateY() > 0) {
            mMatrix.postTranslate(0, -getTranslateY());
        }
        if (width < mWidth) {
            mMatrix.postTranslate((mWidth - width) / 2, 0);
        }
        if (height < mHeight) {
            mMatrix.postTranslate(0, (mHeight - height) / 2);
        }
        setImageMatrix(mMatrix);
    }

    private float distance(float x0, float x1, float y0, float y1) {
        float x = x0 - x1;
        float y = y0 - y1;
        return FloatMath.sqrt(x * x + y * y);
    }

    private float dispDistance() {
        return FloatMath.sqrt(mWidth * mWidth + mHeight * mHeight);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int touchCount = event.getPointerCount();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_1_DOWN:
            case MotionEvent.ACTION_POINTER_2_DOWN:
                if (touchCount >= 2) {
                    float distance = distance(event.getX(0), event.getX(1),
                            event.getY(0), event.getY(1));
                    mPrevDistance = distance;
                    isScaling = true;
                } else {
                    if (System.currentTimeMillis() <= mLastTime + DOUBLE_TAP_SECOND) {
                        if (30 > Math.abs(mPrevMoveX - event.getX())
                                + Math.abs(mPrevMoveY - event.getY())) {
                            isDoubleTap = true;
                            mDoubleTapX = (int) event.getX();
                            mDoubleTapY = (int) event.getY();
                        }
                    }
                    mLastTime = System.currentTimeMillis();
                    mPrevMoveX = (int) event.getX();
                    mPrevMoveY = (int) event.getY();
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (touchCount >= 2 && isScaling) {
                    float dist = distance(event.getX(0), event.getX(1),
                            event.getY(0), event.getY(1));
                    float scale = (dist - mPrevDistance) / dispDistance();
                    mPrevDistance = dist;
                    scale += 1;
                    scale = scale * scale;
                    zoomTo(scale, mWidth / 2, mHeight / 2);
                    cutting();
                } else if (!isScaling) {
                    int distanceX = mPrevMoveX - (int) event.getX();
                    int distanceY = mPrevMoveY - (int) event.getY();
                    mPrevMoveX = (int) event.getX();
                    mPrevMoveY = (int) event.getY();
                    mMatrix.postTranslate(-distanceX, -distanceY);
                    cutting();
                }
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_POINTER_2_UP:
                if (event.getPointerCount() <= 1) {
                    isScaling = false;
                    if (isDoubleTap) {
                        if (30 > Math.abs(mDoubleTapX - event.getX())
                                + Math.abs(mDoubleTapY - event.getY())) {
                            maxZoomTo(mDoubleTapX, mDoubleTapY);
                            cutting();
                        }
                    }
                }
                isDoubleTap = false;
                break;
        }
        return true;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int count_touch=event.getPointerCount();
        switch(event.getAction()){
            case MotionEvent.ACTION_UP:
                float point_x=event.getX();
                float point_y=event.getY();
                rect_x1=point_x;
                rect_y1=point_y;
                if(count_touch==1){
                    orignalRect=new RectF(rect_x1-30, rect_y1-30, rect_x1+30,  rect_y1+30);
                    invalidate();
                }
                break;
        }

        return super.onTouchEvent(event);
    }
}
