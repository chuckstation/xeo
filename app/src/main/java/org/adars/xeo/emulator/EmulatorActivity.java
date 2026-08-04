package org.adars.xeo.emulator;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.Toast;

import org.adars.xeo.R;
import org.adars.xeo.utils.PathUtils;

/**
 * Full-screen emulator activity that runs an Xbox 360 game.
 */
public class EmulatorActivity extends WindowedAppActivity {

    // JNI input state bridge
    private native void nativeSetGamepadState(int buttons, int leftTrigger, int rightTrigger,
                                              int lx, int ly, int rx, int ry);

    // JNI Savestate bridges
    private native boolean nativeSaveState(String filePath);
    private native boolean nativeRestoreState(String filePath);

    @Override
    protected String getWindowedAppIdentifier() {
        // Must match XE_DEFINE_WINDOWED_APP(xenia, ...) in xenia_main.cc.
        return "xenia";
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        String uriStr = getIntent().getStringExtra("game_uri");
        if (uriStr != null) {
            Uri gameUri = Uri.parse(uriStr);
            String nativePath = PathUtils.uriToNativePath(this, gameUri);
            if (nativePath != null) {
                Bundle cvars = getIntent().getBundleExtra(WindowedAppActivity.EXTRA_CVARS);
                if (cvars == null) {
                    cvars = new Bundle();
                }
                cvars.putString("target", nativePath);
                getIntent().putExtra(WindowedAppActivity.EXTRA_CVARS, cvars);
            }
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emulator);
        setWindowSurfaceView(findViewById(R.id.emulator_surface_view));

        final TouchControllerOverlay overlay = findViewById(R.id.touch_controller_overlay);
        if (overlay != null) {
            overlay.setOnControllerInputListener((buttons, leftTrigger, rightTrigger, lx, ly, rx, ry) -> {
                nativeSetGamepadState(buttons, leftTrigger, rightTrigger, lx, ly, rx, ry);
            });
        }

        enterFullscreen();
    }

    @Override
    public void onWindowFocusChanged(final boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            enterFullscreen();
        }
    }

    @Override
    public boolean onKeyDown(final int keyCode, final KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            showEmulatorMenu();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    // -------------------------------------------------------------------------

    private void enterFullscreen() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            final WindowInsetsController c = getWindow().getInsetsController();
            if (c != null) {
                c.hide(WindowInsets.Type.statusBars()
                        | WindowInsets.Type.navigationBars());
                c.setSystemBarsBehavior(
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            //noinspection deprecation
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
    }

    private void showEmulatorMenu() {
        final String[] options = {
            getString(R.string.emulator_menu_save_state_slot, 1),
            getString(R.string.emulator_menu_load_state_slot, 1),
            getString(R.string.emulator_menu_save_state_slot, 2),
            getString(R.string.emulator_menu_load_state_slot, 2),
            getString(R.string.emulator_menu_save_state_slot, 3),
            getString(R.string.emulator_menu_load_state_slot, 3),
            getString(R.string.emulator_menu_exit_game)
        };

        new android.app.AlertDialog.Builder(this)
                .setTitle(R.string.emulator_menu_title)
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            handleSaveState(1);
                            break;
                        case 1:
                            handleLoadState(1);
                            break;
                        case 2:
                            handleSaveState(2);
                            break;
                        case 3:
                            handleLoadState(2);
                            break;
                        case 4:
                            handleSaveState(3);
                            break;
                        case 5:
                            handleLoadState(3);
                            break;
                        case 6:
                            confirmExit();
                            break;
                    }
                })
                .show();
    }

    private String getSaveStatePath(int slot) {
        String baseName = "default_game";
        String uriStr = getIntent().getStringExtra("game_uri");
        Uri gameUri = null;
        if (uriStr != null) {
            gameUri = Uri.parse(uriStr);
        } else {
            gameUri = getIntent().getData();
        }

        if (gameUri != null) {
            String filename = null;
            if ("content".equals(gameUri.getScheme())) {
                try (android.database.Cursor cursor = getContentResolver().query(gameUri, null, null, null, null)) {
                    if (cursor != null && cursor.moveToFirst()) {
                        int index = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                        if (index != -1) {
                            filename = cursor.getString(index);
                        }
                    }
                } catch (Exception ignored) {}
            }
            if (filename == null) {
                filename = gameUri.getPath();
                if (filename != null) {
                    int cut = filename.lastIndexOf('/');
                    if (cut != -1) {
                        filename = filename.substring(cut + 1);
                    }
                }
            }
            if (filename != null) {
                int dotIndex = filename.lastIndexOf('.');
                if (dotIndex > 0) {
                    baseName = filename.substring(0, dotIndex);
                } else {
                    baseName = filename;
                }
            }
        } else {
            Bundle cvarBundle = getIntent().getBundleExtra(WindowedAppActivity.EXTRA_CVARS);
            if (cvarBundle != null) {
                String target = cvarBundle.getString("target");
                if (target != null) {
                    int cut = target.lastIndexOf('/');
                    if (cut != -1) {
                        baseName = target.substring(cut + 1);
                        int dotIndex = baseName.lastIndexOf('.');
                        if (dotIndex > 0) {
                            baseName = baseName.substring(0, dotIndex);
                        }
                    }
                }
            }
        }

        baseName = baseName.replaceAll("[^a-zA-Z0-9_\\- ]", "_");

        java.io.File dir = getExternalFilesDir("savestates");
        if (dir != null && !dir.exists()) {
            dir.mkdirs();
        }
        return new java.io.File(dir, baseName + "_slot" + slot + ".sav").getAbsolutePath();
    }

    private void handleSaveState(int slot) {
        String path = getSaveStatePath(slot);
        boolean success = nativeSaveState(path);
        Toast.makeText(this,
                success ? getString(R.string.savestate_saved, slot)
                        : getString(R.string.savestate_save_failed, slot),
                Toast.LENGTH_SHORT).show();
    }

    private void handleLoadState(int slot) {
        String path = getSaveStatePath(slot);
        java.io.File file = new java.io.File(path);
        if (!file.exists()) {
            Toast.makeText(this, getString(R.string.savestate_not_found, slot),
                    Toast.LENGTH_SHORT).show();
            return;
        }
        boolean success = nativeRestoreState(path);
        Toast.makeText(this,
                success ? getString(R.string.savestate_loaded, slot)
                        : getString(R.string.savestate_load_failed, slot),
                Toast.LENGTH_SHORT).show();
    }

    private void confirmExit() {
        new android.app.AlertDialog.Builder(this)
                .setTitle(R.string.dialog_exit_title)
                .setMessage(R.string.dialog_exit_message)
                .setPositiveButton(R.string.dialog_exit_confirm,
                        (d, w) -> finish())
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }
}
