package org.adars.xeo.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Utilities for resolving Android content:// URIs to filesystem paths that
 * can be passed to the native Xenia layer via JNI.
 *
 * <p>On Android 10+ (API 29+) direct filesystem paths are increasingly
 * restricted; for those devices Xenia's native layer must open files via the
 * AssetFileDescriptor / ParcelFileDescriptor path (fd-based I/O).  This class
 * returns a /proc/self/fd/<N> path in that case so the native code can still
 * use open().</p>
 */
public final class PathUtils {

    private PathUtils() {}

    /**
     * Resolves a URI to a path the native layer can open.
     * Returns null if the URI cannot be resolved.
     */
    @Nullable
    public static String uriToNativePath(@NonNull final Context context,
                                          @NonNull final Uri uri) {
        final String scheme = uri.getScheme();

        // Plain file:// URI — already a path.
        if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            return uri.getPath();
        }

        // content:// URI — try various resolution strategies.
        if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            // Strategy 1: DocumentProvider with primary external storage.
            if (DocumentsContract.isDocumentUri(context, uri)) {
                final String authority = uri.getAuthority();
                if ("com.android.externalstorage.documents".equals(authority)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final String type = split[0];
                    if ("primary".equalsIgnoreCase(type)) {
                        return Environment.getExternalStorageDirectory()
                                + "/" + (split.length > 1 ? split[1] : "");
                    }
                }
                // Strategy 2: MediaStore document.
                if ("com.android.providers.media.documents".equals(authority)) {
                    final String docId = DocumentsContract.getDocumentId(uri);
                    final String[] split = docId.split(":");
                    final Uri mediaUri;
                    if ("image".equals(split[0])) {
                        mediaUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                    } else if ("video".equals(split[0])) {
                        mediaUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                    } else {
                        mediaUri = MediaStore.Files.getContentUri("external");
                    }
                    final Uri contentUri = MediaStore.Files.getContentUri("external");
                    return queryDataColumn(context, contentUri,
                            "_id=?", new String[]{split[1]});
                }
            }

            // Strategy 3: Generic MediaStore column query.
            final String path = queryDataColumn(context, uri, null, null);
            if (path != null) return path;

            // Strategy 4: Expose via /proc/self/fd — works on all API levels
            // because ParcelFileDescriptor is always available.
            return openAsFdPath(context, uri);
        }

        return null;
    }

    @Nullable
    private static String queryDataColumn(@NonNull final Context context,
                                           @NonNull final Uri uri,
                                           @Nullable final String selection,
                                           @Nullable final String[] selectionArgs) {
        try (Cursor cursor = context.getContentResolver().query(
                uri, new String[]{MediaStore.MediaColumns.DATA},
                selection, selectionArgs, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                final String path = cursor.getString(0);
                if (path != null && !path.isEmpty()) return path;
            }
        } catch (final Exception ignored) {}
        return null;
    }

    @Nullable
    private static String openAsFdPath(@NonNull final Context context,
                                        @NonNull final Uri uri) {
        try {
            android.os.ParcelFileDescriptor pfd =
                    context.getContentResolver().openFileDescriptor(uri, "r");
            if (pfd == null) return null;
            // Detach the file descriptor so that garbage collection won't close it
            // before the native thread can access the symlink at /proc/self/fd/<N>.
            final int fd = pfd.detachFd();
            return "/proc/self/fd/" + fd;
        } catch (final Exception ignored) {}
        return null;
    }
}
