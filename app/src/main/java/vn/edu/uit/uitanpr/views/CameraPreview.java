package vn.edu.uit.uitanpr.views;

import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.Camera.Size;

import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import static android.R.attr.width;
import static android.content.Context.WINDOW_SERVICE;
import static vn.edu.uit.uitanpr.R.attr.height;

public class CameraPreview extends SurfaceView implements
		SurfaceHolder.Callback {
	SurfaceHolder mHolder;
    Camera mCamera;

	Camera.PreviewCallback previewCallback;

	public CameraPreview(Context context, Camera.PreviewCallback previewCallback) {
		super(context);
		this.previewCallback = previewCallback;

		// Install a SurfaceHolder.Callback so we get notified when the
		// underlying surface is created and destroyed.
		mHolder = getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	public void surfaceCreated(SurfaceHolder holder) {
		// The Surface has been created, acquire the camera and tell it where
		// to draw.
		mCamera = Camera.open();

		try {
			mCamera.setPreviewDisplay(holder);
		} catch (IOException exception) {
			mCamera.release();
			mCamera = null;

		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		// Surface will be destroyed when we return, so stop the preview.
		// Because the CameraDevice object is not a shared resource, it's very
		// important to release it when the activity is paused.
		mCamera.stopPreview();
		mCamera.release();
		mCamera = null;
	}

	private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
		final double ASPECT_TOLERANCE = 0.05;
		double targetRatio = (double) w/h;

		if (sizes==null) return null;

		Camera.Size optimalSize = null;

		double minDiff = Double.MAX_VALUE;

		int targetHeight = h;

		// Find size
		for (Camera.Size size : sizes) {
			double ratio = (double) size.width / size.height;
			if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
			if (Math.abs(size.height - targetHeight) < minDiff) {
				optimalSize = size;
				minDiff = Math.abs(size.height - targetHeight);
			}
		}

		if (optimalSize == null) {
			minDiff = Double.MAX_VALUE;
			for (Camera.Size size : sizes) {
				if (Math.abs(size.height - targetHeight) < minDiff) {
					optimalSize = size;
					minDiff = Math.abs(size.height - targetHeight);
				}
			}
		}
		return optimalSize;
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		// Now that the size is known, set up the camera parameters and begin
		// the preview.
		Camera.Parameters parameters = mCamera.getParameters();

		List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
		Camera.Size optimalSize = getOptimalPreviewSize(sizes, getResources().getDisplayMetrics().widthPixels, getResources().getDisplayMetrics().heightPixels);

		parameters.setPreviewSize(optimalSize.width, optimalSize.height);

        /*
		int maxZoom = parameters.getMaxZoom();
		   if (parameters.isZoomSupported()) {
			   
			   parameters.setZoom(maxZoom);
		   }
		   parameters.set("iso", "100");
		   parameters.setPreviewFormat(ImageFormat.RGB_565);
        */

		mCamera.setParameters(parameters);
		if (previewCallback != null) {
			mCamera.setPreviewCallbackWithBuffer(previewCallback);
			Camera.Size size = parameters.getPreviewSize();
			byte[] data = new byte[size.width
					* size.height
					* ImageFormat
							.getBitsPerPixel(parameters.getPreviewFormat()) / 8];
			// Log.e("DON", mGray.rows() + "");
			mCamera.addCallbackBuffer(data);
		}

		mCamera.startPreview();
	}

}
