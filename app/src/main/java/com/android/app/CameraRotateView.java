package com.android.app;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Camera 的三维变换有三类：旋转、平移、移动相机。
 *  Camera.rotate*() 三维旋转
 *       一共有四个方法： rotateX(deg) rotateY(deg) rotateZ(deg) rotate(x, y, z)。
 *  另外，Camera 和 Canvas 一样也需要保存和恢复状态才能正常绘制，不然在界面刷新之后绘制就会出现问题
 * */
public class CameraRotateView extends View {
    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    Bitmap bitmap;
    Point point1 = new Point(0, 0);
    Point point2 = new Point(400, 200);
    Camera camera = new Camera();

    public CameraRotateView(Context context) {
        super(context);
    }

    public CameraRotateView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public CameraRotateView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    {
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.planet_diqiu_activated);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
//        camera.save();
//        camera.rotateX(30);
//        camera.applyToCanvas(canvas);
//        camera.restore();
        canvas.drawBitmap(bitmap, point1.x, point1.y, paint);
        canvas.restore();
        setRotationX(60f);
    }
}
