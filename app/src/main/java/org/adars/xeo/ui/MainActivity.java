package org.adars.xeo.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.adars.xeo.R;
import org.adars.xeo.emulator.EmulatorActivity;
import org.adars.xeo.emulator.GpuTraceViewerActivity;
import org.adars.xeo.emulator.WindowedAppActivity;
import org.adars.xeo.utils.GameEntry;
import org.adars.xeo.utils.GameLibraryManager;
import org.adars.xeo.utils.PathUtils;

import java.util.List;

/**
 * Main launcher screen: shows the scanned game library and lets the user
 * pick a title to launch, browse for a file directly, or open the GPU trace
 * viewer.
 */
public class MainActivity extends AppCompatActivity
        implements GameLibraryAdapter.OnGameClickListener {

    private GameLibraryManager mLibraryManager;
    private GameLibraryAdapter mAdapter;
    private TextView mEmptyView;

    // -------------------------------------------------------------------------
    // Activity-result launchers (replaces deprecated startActivityForResult)
    // -------------------------------------------------------------------------

    /** Browse for any game file (.iso, .xex, .zar, etc.) */
    private final ActivityResultLauncher<String[]> mPickGameFile =
            registerForActivityResult(
                    new ActivityResultContracts.OpenDocument(),
                    uri -> {
                        if (uri != null) launchGame(uri);
                    });

    /** Browse for a GPU trace file */
    private final ActivityResultLauncher<String[]> mPickTraceFile =
            registerForActivityResult(
                    new ActivityResultContracts.OpenDocument(),
                    uri -> {
                        if (uri != null) launchGpuTraceViewer(uri);
                    });

    /** Pick a folder to scan as the game library root */
    private final ActivityResultLauncher<Uri> mPickLibraryFolder =
            registerForActivityResult(
                    new ActivityResultContracts.OpenDocumentTree(),
                    uri -> {
                        if (uri != null) {
                            // Persist read permission across reboots.
                            getContentResolver().takePersistableUriPermission(
                                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            mLibraryManager.setLibraryRoot(uri.toString());
                            refreshLibrary();
                        }
                    });

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        mLibraryManager = new GameLibraryManager(this);

        mEmptyView = findViewById(R.id.tv_empty);

        final RecyclerView recyclerView = findViewById(R.id.rv_games);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new GameLibraryAdapter(this);
        recyclerView.setAdapter(mAdapter);

        // If launched via file open intent, hand off immediately.
        handleIncomingIntent(getIntent());

        refreshLibrary();
    }

    @Override
    protected void onNewIntent(final Intent intent) {
        super.onNewIntent(intent);
        handleIncomingIntent(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull final MenuItem item) {
        final int id = item.getItemId();
        if (id == R.id.action_open_file) {
            mPickGameFile.launch(new String[]{"application/octet-stream", "*/*"});
            return true;
        } else if (id == R.id.action_set_library_folder) {
            mPickLibraryFolder.launch(null);
            return true;
        } else if (id == R.id.action_gpu_trace_viewer) {
            mPickTraceFile.launch(new String[]{"application/octet-stream"});
            return true;
        } else if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // -------------------------------------------------------------------------
    // GameLibraryAdapter.OnGameClickListener
    // -------------------------------------------------------------------------

    @Override
    public void onGameClick(final GameEntry game) {
        launchGame(Uri.parse(game.getUri()));
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private void handleIncomingIntent(final Intent intent) {
        if (intent == null) return;
        final Uri data = intent.getData();
        if (data != null && Intent.ACTION_VIEW.equals(intent.getAction())) {
            launchGame(data);
        }
    }

    private void launchGame(final Uri gameUri) {
        final String path = PathUtils.uriToNativePath(this, gameUri);
        if (path == null) {
            Toast.makeText(this, R.string.error_cannot_open_file,
                    Toast.LENGTH_SHORT).show();
            return;
        }
        final Bundle cvars = new Bundle();
        // "target" matches DEFINE_transient_path(target, ...) in xenia_main.cc.
        cvars.putString("target", path);

        final Intent intent = new Intent(this, EmulatorActivity.class);
        intent.setData(gameUri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.putExtra(WindowedAppActivity.EXTRA_CVARS, cvars);
        intent.putExtra("game_uri", gameUri.toString());
        startActivity(intent);
    }

    private void launchGpuTraceViewer(final Uri traceUri) {
        final Bundle cvars = new Bundle();
        cvars.putString("target_trace_file", traceUri.toString());

        final Intent intent = new Intent(this, GpuTraceViewerActivity.class);
        intent.setData(traceUri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.putExtra(WindowedAppActivity.EXTRA_CVARS, cvars);
        startActivity(intent);
    }

    private void refreshLibrary() {
        final List<GameEntry> games = mLibraryManager.scanLibrary();
        mAdapter.submitList(games);
        mEmptyView.setVisibility(games.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
