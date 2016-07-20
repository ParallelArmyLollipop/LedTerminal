package com.eric.terminal.led;


import android.content.Context;
import android.hardware.Camera;
import android.hardware.camera2.CameraManager;
import android.media.FaceDetector;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import com.orhanobut.logger.Logger;

import java.io.IOException;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class FaceActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    @Bind(R.id.tv)
    TextView mTv;
    @Bind(R.id.camera_surface)
    SurfaceView mCameraSurface;

    Camera mCamera;
    FaceDetector mFaceDetector;
    FaceDetector.Face[] mFaces;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face);
        ButterKnife.bind(this);
        mCameraSurface.getHolder().addCallback(this);
        mCameraSurface.getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        mCamera = Camera.open(0);

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Logger.d("surfaceCreated");
        //打开摄像头
        try {
            mCamera.setPreviewDisplay(mCameraSurface.getHolder());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Logger.d("surfaceChanged");
        Camera.Parameters params = mCamera.getParameters();
        List<Camera.Size> list = params.getSupportedPreviewSizes();
        Camera.Size selected = list.get(0);
        params.setPictureSize(selected.width,selected.height);
        mCamera.setParameters(params);
        mCamera.setDisplayOrientation(90);
        mCamera.startPreview();
        mFaceDetector = new FaceDetector(selected.width,selected.height,10);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Logger.d("surfaceDestroyed");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCamera.release();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCamera.stopPreview();
    }
}
