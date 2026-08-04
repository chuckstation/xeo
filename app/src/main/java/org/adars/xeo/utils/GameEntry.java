package org.adars.xeo.utils;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

/**
 * Lightweight data class representing a discovered game in the library.
 */
public final class GameEntry {

    private final String mUri;          // content:// or file:// URI string
    private final String mTitle;        // Display title (extracted or filename)
    private final String mDisplayPath;  // Human-readable path
    @Nullable
    private final Bitmap mIconBitmap;   // Extracted game icon, may be null

    public GameEntry(@NonNull final String uri,
                     @NonNull final String title,
                     @NonNull final String displayPath,
                     @Nullable final Bitmap iconBitmap) {
        mUri         = uri;
        mTitle       = title;
        mDisplayPath = displayPath;
        mIconBitmap  = iconBitmap;
    }

    @NonNull public String getUri()         { return mUri; }
    @NonNull public String getTitle()       { return mTitle; }
    @NonNull public String getDisplayPath() { return mDisplayPath; }
    @Nullable public Bitmap getIconBitmap() { return mIconBitmap; }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof GameEntry)) return false;
        final GameEntry other = (GameEntry) o;
        return mUri.equals(other.mUri) && mTitle.equals(other.mTitle);
    }

    @Override
    public int hashCode() {
        return Objects.hash(mUri, mTitle);
    }
}
