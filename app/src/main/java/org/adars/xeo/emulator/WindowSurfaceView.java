package org.adars.xeo.emulator;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.SurfaceView;

/**
 * SurfaceView subclass that forwards onDraw callbacks to the hosting
 * WindowedAppActivity so the native renderer can be driven from the
 * Android view system.
 */
public class WindowSurfaceView extends SurfaceView {

    public WindowSurfaceView(final Context context) {
        super(context);
        init();
    }

    public WindowSurfaceView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WindowSurfaceView(final Context context, final AttributeSet attrs,
                             final int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public WindowSurfaceView(final Context context, final AttributeSet attrs,
                             final int defStyleAttr, final int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        // Required so that onDraw is called for native paint callbacks.
        setWillNotDraw(false);
    }

    @Override
    protected void onDraw(final Canvas canvas) {
        final Context context = getContext();
        if (!(context instanceof WindowedAppActivity)) {
            return;
        }
        ((WindowedAppActivity) context).onWindowSurfaceDraw(false);
    }
}
