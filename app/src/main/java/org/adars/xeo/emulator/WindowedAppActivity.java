package org.adars.xeo.emulator;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.View;

import androidx.annotation.Nullable;

import org.adars.xeo.R;
import org.adars.xeo.XeoRuntimeException;

/**
 * Abstract base activity that bridges the Android view system to the
 * Xenia native windowed-app framework via JNI.
 *
 * <p>Subclasses must implement {@link #getWindowedAppIdentifier()} and set
 * their content view, then call {@link #setWindowSurfaceView} with the
 * {@link WindowSurfaceView} found in that layout.</p>
 *
 * <p>The string constant {@link #EXTRA_CVARS} is shared between Java and the
 * native layer — do not rename it without updating the C++ side.</p>
 */
public abstract class WindowedAppActivity extends Activity {

    /**
     * Intent extra key for a {@link Bundle} containing Xeo cvar launch
     * arguments. The literal value is also referenced from native code
     * (patches/ui/windowed_app_context_android.cc) and must match exactly.
     */
    public static final String EXTRA_CVARS =
            "org.adars.xeo.WindowedAppActivity.EXTRA_CVARS";

    static {
        System.loadLibrary("xeo");
    }

    // -------------------------------------------------------------------------
    // Native method declarations
    // -------------------------------------------------------------------------

    /**
     * Called on onCreate to initialise the native windowed-app context.
     *
     * @return opaque pointer (cast to long) to the native context, or 0 on
     *         failure.
     */
    private native long nativeInitialize(String windowedAppIdentifier,
                                         AssetManager assetManager,
                                         Bundle cvarBundle);

    private native void nativeDestroy(long appContext);

    private native void nativeSurfaceLayoutChange(long appContext,
                                                   int left, int top,
                                                   int right, int bottom);

    private native boolean nativeSurfaceMotionEvent(long appContext,
                                                     MotionEvent event);

    private native void nativeSurfaceChanged(long appContext,
                                              Surface windowSurface);

    private native void nativePaint(long appContext, boolean forcePaint);

    // -------------------------------------------------------------------------
    // Fields
    // -------------------------------------------------------------------------

    private final SurfaceEventListener mSurfaceListener = new SurfaceEventListener();

    /** Opaque pointer to the native AndroidWindowedAppContext. 0 while gone. */
    private long mAppContext = 0;

    @Nullable
    private WindowSurfaceView mWindowSurfaceView = null;

    // -------------------------------------------------------------------------
    // Abstract contract
    // -------------------------------------------------------------------------

    /**
     * Returns the identifier string of the windowed app to instantiate on the
     * native side (e.g. {@code "xenia_app"} or
     * {@code "xenia_gpu_vulkan_trace_viewer"}).
     */
    protected abstract String getWindowedAppIdentifier();

    // -------------------------------------------------------------------------
    // Surface management
    // -------------------------------------------------------------------------

    protected void setWindowSurfaceView(
            @Nullable final WindowSurfaceView windowSurfaceView) {
        if (mWindowSurfaceView == windowSurfaceView) return;

        // Detach old surface.
        if (mWindowSurfaceView != null) {
            mWindowSurfaceView.getHolder().removeCallback(mSurfaceListener);
            mWindowSurfaceView.setOnTouchListener(null);
            mWindowSurfaceView.setOnGenericMotionListener(null);
            mWindowSurfaceView.removeOnLayoutChangeListener(mSurfaceListener);
            mWindowSurfaceView = null;
            if (mAppContext != 0) {
                nativeSurfaceChanged(mAppContext, null);
            }
        }

        if (windowSurfaceView == null) return;

        mWindowSurfaceView = windowSurfaceView;
        mWindowSurfaceView.addOnLayoutChangeListener(mSurfaceListener);
        mWindowSurfaceView.setOnGenericMotionListener(mSurfaceListener);
        mWindowSurfaceView.setOnTouchListener(mSurfaceListener);

        final SurfaceHolder holder = mWindowSurfaceView.getHolder();
        holder.addCallback(mSurfaceListener);
        // If the surface already exists when we attach (e.g. configuration
        // change), notify native immediately.
        if (mAppContext != 0 && holder.getSurface() != null
                && holder.getSurface().isValid()) {
            nativeSurfaceChanged(mAppContext, holder.getSurface());
        }
    }

    /**
     * Called from {@link WindowSurfaceView#onDraw} and from the surface
     * redraw-needed callback to drive native painting.
     */
    public void onWindowSurfaceDraw(final boolean forcePaint) {
        if (mAppContext != 0) {
            nativePaint(mAppContext, forcePaint);
        }
    }

    /**
     * Called from the native WindowedAppContext (possibly a non-UI thread)
     * to request a repaint of the surface view.
     */
    @SuppressWarnings("UnusedDeclaration")
    protected void postInvalidateWindowSurface() {
        if (mWindowSurfaceView != null) {
            mWindowSurfaceView.postInvalidate();
        }
    }

    // -------------------------------------------------------------------------
    // Activity lifecycle
    // -------------------------------------------------------------------------

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Keep screen on during emulation.
        getWindow().addFlags(
                android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        final Bundle cvarBundle = getIntent().getBundleExtra(EXTRA_CVARS);
        mAppContext = nativeInitialize(
                getWindowedAppIdentifier(), getAssets(), cvarBundle);

        if (mAppContext == 0) {
            finish();
            throw new XeoRuntimeException(
                    "Failed to initialise native windowed app: "
                            + getWindowedAppIdentifier());
        }
    }

    @Override
    protected void onDestroy() {
        setWindowSurfaceView(null);
        if (mAppContext != 0) {
            nativeDestroy(mAppContext);
            mAppContext = 0;
        }
        super.onDestroy();
    }

    // -------------------------------------------------------------------------
    // Inner: surface / input event listener
    // -------------------------------------------------------------------------

    private final class SurfaceEventListener
            implements View.OnGenericMotionListener,
                       View.OnLayoutChangeListener,
                       View.OnTouchListener,
                       SurfaceHolder.Callback2 {

        @Override
        public void onLayoutChange(final View v,
                                   final int left, final int top,
                                   final int right, final int bottom,
                                   final int oldLeft, final int oldTop,
                                   final int oldRight, final int oldBottom) {
            if (mAppContext != 0) {
                nativeSurfaceLayoutChange(mAppContext, left, top, right, bottom);
            }
        }

        @Override
        public boolean onGenericMotion(final View view, final MotionEvent event) {
            return mAppContext != 0
                    && nativeSurfaceMotionEvent(mAppContext, event);
        }

        @SuppressLint("ClickableViewAccessibility")
        @Override
        public boolean onTouch(final View view, final MotionEvent event) {
            return mAppContext != 0
                    && nativeSurfaceMotionEvent(mAppContext, event);
        }

        @Override
        public void surfaceCreated(final SurfaceHolder holder) {
            if (mAppContext != 0) {
                nativeSurfaceChanged(mAppContext, holder.getSurface());
            }
        }

        @Override
        public void surfaceChanged(final SurfaceHolder holder,
                                   final int format,
                                   final int width, final int height) {
            if (mAppContext != 0) {
                nativeSurfaceChanged(mAppContext, holder.getSurface());
            }
        }

        @Override
        public void surfaceDestroyed(final SurfaceHolder holder) {
            if (mAppContext != 0) {
                nativeSurfaceChanged(mAppContext, null);
            }
        }

        @Override
        public void surfaceRedrawNeeded(final SurfaceHolder holder) {
            onWindowSurfaceDraw(true);
        }
    }

    /**
     * Called from native code when a fatal error occurs.
     * Shows a Toast and finishes the activity gracefully.
     */
    @SuppressWarnings("UnusedDeclaration")
    public void handleFatalError(final String message) {
        runOnUiThread(() -> {
            android.widget.Toast.makeText(WindowedAppActivity.this,
                    getString(R.string.fatal_error_prefix, message),
                    android.widget.Toast.LENGTH_LONG).show();
            finish();
        });
    }
}
