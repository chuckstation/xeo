package org.adars.xeo.emulator;

import android.os.Bundle;

import org.adars.xeo.R;

/**
 * Vulkan GPU trace viewer — developer / debug tool.
 * Launched either from MainActivity (debug menu) or by opening a .trace file.
 * Accepts the trace file path via EXTRA_CVARS["target_trace_file"].
 */
public class GpuTraceViewerActivity extends WindowedAppActivity {

    @Override
    protected String getWindowedAppIdentifier() {
        return "xenia_gpu_vulkan_trace_viewer";
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gpu_trace_viewer);
        setWindowSurfaceView(findViewById(R.id.gpu_trace_viewer_surface_view));
    }
}
