package com.mobcrush.mobcrush.broadcast;

import android.annotation.TargetApi;
import android.app.Service;
import android.graphics.Matrix;
import android.graphics.Matrix.ScaleToFit;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraDevice.StateCallback;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureRequest.Builder;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.Image.Plane;
import android.media.ImageReader;
import android.media.ImageReader.OnImageAvailableListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView.SurfaceTextureListener;
import android.view.WindowManager;
import com.android.volley.DefaultRetryPolicy;
import com.crashlytics.android.Crashlytics;
import com.mobcrush.mobcrush.Constants;
import com.mobcrush.mobcrush.Mobcrush;
import java.util.Arrays;

@TargetApi(21)
public class MobcrushCameraManager {
    private static final int CAMERA = 0;
    private static final Size MAX_CAMERA_SIZE = new Size(Constants.MAX_PROFILE_IMAGE_SIZE, 480);
    private static final String TAG = MobcrushCameraManager.class.getName();
    private StateCallback cameraStateCallback = new StateCallback() {
        public void onOpened(CameraDevice camera) {
            MobcrushCameraManager.this.mCamera = camera;
            SurfaceTexture texture = MobcrushCameraManager.this.mPreview.getSurfaceTexture();
            texture.setDefaultBufferSize(MobcrushCameraManager.this.mPreviewSize.getWidth(), MobcrushCameraManager.this.mPreviewSize.getHeight());
            final Surface surface = new Surface(texture);
            try {
                MobcrushCameraManager.this.mCameraImageReader = ImageReader.newInstance(MobcrushCameraManager.this.mPreviewSize.getWidth(), MobcrushCameraManager.this.mPreviewSize.getHeight(), 35, 4);
                MobcrushCameraManager.this.mCameraImageReader.setOnImageAvailableListener(new CameraImageAvailableListener(), null);
                Builder builder = MobcrushCameraManager.this.mCamera.createCaptureRequest(3);
                builder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, MobcrushCameraManager.this.getOptimalRange(MobcrushCameraManager.this.mCamera.getId()));
                builder.addTarget(surface);
                builder.addTarget(MobcrushCameraManager.this.mCameraImageReader.getSurface());
                MobcrushCameraManager.this.mCamera.createCaptureSession(Arrays.asList(new Surface[]{surface, MobcrushCameraManager.this.mCameraImageReader.getSurface()}), new CameraCaptureSession.StateCallback() {
                    public void onConfigured(CameraCaptureSession session) {
                        MobcrushCameraManager.this.mCaptureSession = session;
                        try {
                            MobcrushCameraManager.this.mCaptureSession.setRepeatingRequest(MobcrushCameraManager.this.createCaptureRequest(surface), null, null);
                        } catch (Throwable e) {
                            Log.e(MobcrushCameraManager.TAG, e.getMessage());
                            Crashlytics.logException(e);
                        }
                    }

                    public void onConfigureFailed(CameraCaptureSession session) {
                        Log.e(MobcrushCameraManager.TAG, "Session configuration failed");
                    }
                }, null);
            } catch (Throwable e) {
                Log.e(MobcrushCameraManager.TAG, e.getMessage());
                Crashlytics.logException(e);
            }
        }

        public void onDisconnected(CameraDevice camera) {
            camera.close();
            MobcrushCameraManager.this.mCamera = null;
        }

        public void onError(CameraDevice camera, int error) {
            camera.close();
            MobcrushCameraManager.this.mCamera = null;
        }
    };
    private CameraDevice mCamera;
    private ImageReader mCameraImageReader;
    private CameraManager mCameraManager;
    private int mCameraRotationOffset;
    private CameraCaptureSession mCaptureSession;
    private int mOrientation;
    private AutoFitTextureView mPreview;
    private Size mPreviewSize;
    private int mRotation;
    private Service mService;
    private final SurfaceTextureListener mSurfaceTextureListener = new SurfaceTextureListener() {
        public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
            Log.d(MobcrushCameraManager.TAG, "onSurfaceTextureAvailable");
            if (MobcrushCameraManager.this.mService != null && MobcrushCameraManager.this.mPreview != null) {
                MobcrushCameraManager.this.startCameraCapture(MobcrushCameraManager.this.mService, MobcrushCameraManager.this.mPreview);
            }
        }

        public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
            Log.d(MobcrushCameraManager.TAG, "onSurfaceTextureSizeChanged");
            MobcrushCameraManager.this.configureTransform(width, height);
        }

        public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
            Log.d(MobcrushCameraManager.TAG, "onSurfaceTextureDestroyed");
            return true;
        }

        public void onSurfaceTextureUpdated(SurfaceTexture texture) {
            int rotation = MobcrushCameraManager.this.mWindowManager.getDefaultDisplay().getRotation();
            if (MobcrushCameraManager.this.mRotation != rotation) {
                int orientation = MobcrushCameraManager.this.mService.getResources().getConfiguration().orientation;
                if (MobcrushCameraManager.this.mOrientation == orientation) {
                    MobcrushCameraManager.this.configureTransform(MobcrushCameraManager.this.mPreview.getWidth(), MobcrushCameraManager.this.mPreview.getHeight());
                } else {
                    MobcrushCameraManager.this.mOrientation = orientation;
                }
                MobcrushCameraManager.this.mRotation = rotation;
            }
        }
    };
    private WindowManager mWindowManager;

    private class CameraImageAvailableListener implements OnImageAvailableListener {
        private CameraImageAvailableListener() {
        }

        public void onImageAvailable(ImageReader reader) {
            Image img = reader.acquireLatestImage();
            if (img != null) {
                Plane[] planes = img.getPlanes();
                if (planes[0].getBuffer() != null) {
                    Mobcrush.setCameraPixelData(img.getWidth(), img.getHeight(), planes[0].getBuffer(), planes[0].getPixelStride(), planes[0].getRowStride(), planes[1].getBuffer(), planes[1].getPixelStride(), planes[1].getRowStride(), planes[2].getBuffer(), (MobcrushCameraManager.this.mWindowManager.getDefaultDisplay().getRotation() * 90) + MobcrushCameraManager.this.mCameraRotationOffset, img.getFormat());
                    img.close();
                }
            }
        }
    }

    public void startCameraCapture(Service parent, AutoFitTextureView preview) {
        Log.d(TAG, "Starting camera capture");
        this.mService = parent;
        this.mPreview = preview;
        this.mPreview.setSurfaceTextureListener(this.mSurfaceTextureListener);
        if (this.mPreview.isAvailable()) {
            this.mWindowManager = (WindowManager) this.mService.getSystemService("window");
            this.mRotation = this.mWindowManager.getDefaultDisplay().getRotation();
            this.mOrientation = this.mService.getResources().getConfiguration().orientation;
            this.mCameraManager = (CameraManager) this.mService.getSystemService("camera");
            String cameraId = getCamera();
            try {
                CameraCharacteristics characteristics = this.mCameraManager.getCameraCharacteristics(cameraId);
                StreamConfigurationMap configs = (StreamConfigurationMap) characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                this.mCameraRotationOffset = ((Integer) characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)).intValue();
                this.mPreviewSize = getOptimalPreviewSize(configs.getOutputSizes(SurfaceTexture.class), MAX_CAMERA_SIZE);
                if (this.mOrientation == 2) {
                    this.mPreview.setAspectRatio(this.mPreviewSize.getWidth(), this.mPreviewSize.getHeight());
                } else {
                    this.mPreview.setAspectRatio(this.mPreviewSize.getHeight(), this.mPreviewSize.getWidth());
                }
                configureTransform(this.mPreview.getWidth(), this.mPreview.getHeight());
                this.mCameraManager.openCamera(cameraId, this.cameraStateCallback, null);
            } catch (Throwable e) {
                Log.e(TAG, e.getMessage());
                Crashlytics.logException(e);
            }
        }
    }

    public void stopCameraCapture() {
        Log.d(TAG, "Stopping camera capture");
        this.mPreviewSize = null;
        if (this.mCaptureSession != null) {
            this.mCaptureSession.close();
            this.mCaptureSession = null;
        }
        if (this.mCamera != null) {
            this.mCamera.close();
            this.mCamera = null;
        }
        if (this.mCameraImageReader != null) {
            this.mCameraImageReader.close();
            this.mCameraImageReader = null;
        }
    }

    private String getCamera() {
        try {
            for (String cameraId : this.mCameraManager.getCameraIdList()) {
                if (((Integer) this.mCameraManager.getCameraCharacteristics(cameraId).get(CameraCharacteristics.LENS_FACING)).intValue() == 0) {
                    return cameraId;
                }
            }
        } catch (Throwable e) {
            Log.e(TAG, e.getMessage());
            Crashlytics.logException(e);
        }
        return null;
    }

    private CaptureRequest createCaptureRequest(Surface previewSurface) {
        try {
            Builder builder = this.mCamera.createCaptureRequest(3);
            builder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, getOptimalRange(this.mCamera.getId()));
            builder.addTarget(previewSurface);
            builder.addTarget(this.mCameraImageReader.getSurface());
            return builder.build();
        } catch (Throwable e) {
            Log.e(TAG, e.getMessage());
            Crashlytics.logException(e);
            return null;
        }
    }

    private void configureTransform(int viewWidth, int viewHeight) {
        if (this.mPreview != null && this.mPreviewSize != null && this.mService != null) {
            int rotation = this.mWindowManager.getDefaultDisplay().getRotation();
            Matrix matrix = new Matrix();
            RectF viewRect = new RectF(0.0f, 0.0f, (float) viewWidth, (float) viewHeight);
            RectF bufferRect = new RectF(0.0f, 0.0f, (float) this.mPreviewSize.getHeight(), (float) this.mPreviewSize.getWidth());
            float centerX = viewRect.centerX();
            float centerY = viewRect.centerY();
            if (1 == rotation || 3 == rotation) {
                bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
                matrix.setRectToRect(viewRect, bufferRect, ScaleToFit.FILL);
                float scale = Math.max(((float) viewHeight) / ((float) this.mPreviewSize.getHeight()), ((float) viewWidth) / ((float) this.mPreviewSize.getWidth()));
                matrix.postRotate((float) ((rotation - 2) * 90), centerX, centerY);
                matrix.postScale(-scale, scale, centerX, centerY);
            } else if (2 == rotation) {
                matrix.postRotate(180.0f, centerX, centerY);
                matrix.postScale(-1.0f, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT, centerX, centerY);
            } else {
                matrix.postScale(-1.0f, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT, centerX, centerY);
            }
            this.mPreview.setTransform(matrix);
        }
    }

    private Size getOptimalPreviewSize(Size[] choices, Size max) {
        Size optimalSize = new Size(0, 0);
        DisplayMetrics metrics = this.mService.getResources().getDisplayMetrics();
        int w;
        int h;
        if (this.mService.getResources().getConfiguration().orientation == 2) {
            w = metrics.widthPixels;
            h = metrics.heightPixels;
        } else {
            w = metrics.heightPixels;
            h = metrics.widthPixels;
        }
        for (Size option : choices) {
            if (optimalSize.getWidth() * optimalSize.getHeight() <= option.getWidth() * option.getHeight() && option.getHeight() == (option.getWidth() * h) / w && option.getHeight() <= max.getHeight() && option.getWidth() <= max.getWidth()) {
                optimalSize = option;
            }
        }
        if (optimalSize.getWidth() == 0) {
            optimalSize = choices[choices.length / 2];
            for (Size size : choices) {
                if (size.getWidth() * size.getHeight() <= max.getWidth() * max.getHeight()) {
                    optimalSize = size;
                    break;
                }
            }
            Log.e(TAG, "Couldn't find a suitable preview size. Defaulting to: " + optimalSize.toString());
        }
        return optimalSize;
    }

    private Range<Integer> getOptimalRange(String cameraId) {
        try {
            Range[] fps = (Range[]) this.mCameraManager.getCameraCharacteristics(cameraId).get(CameraCharacteristics.CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES);
            Range<Integer> optimalRange = fps[0];
            for (Range<Integer> range : fps) {
                if (((Integer) range.getUpper()).intValue() < 30 && (((Integer) range.getUpper()).intValue() > ((Integer) optimalRange.getUpper()).intValue() || ((Integer) optimalRange.getUpper()).intValue() > 30)) {
                    optimalRange = range;
                }
            }
            return optimalRange;
        } catch (CameraAccessException e) {
            Log.e(TAG, e.getMessage());
            Crashlytics.logException(e);
            return null;
        }
    }
}
