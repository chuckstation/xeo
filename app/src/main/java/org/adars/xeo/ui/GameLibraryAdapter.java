package org.adars.xeo.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import org.adars.xeo.R;
import org.adars.xeo.utils.GameEntry;

/**
 * RecyclerView adapter that displays the scanned game library.
 */
public class GameLibraryAdapter
        extends ListAdapter<GameEntry, GameLibraryAdapter.ViewHolder> {

    public interface OnGameClickListener {
        void onGameClick(GameEntry game);
    }

    private static final DiffUtil.ItemCallback<GameEntry> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<GameEntry>() {
                @Override
                public boolean areItemsTheSame(@NonNull GameEntry a,
                                               @NonNull GameEntry b) {
                    return a.getUri().equals(b.getUri());
                }

                @Override
                public boolean areContentsTheSame(@NonNull GameEntry a,
                                                  @NonNull GameEntry b) {
                    return a.equals(b);
                }
            };

    private final OnGameClickListener mListener;

    public GameLibraryAdapter(final OnGameClickListener listener) {
        super(DIFF_CALLBACK);
        mListener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent,
                                         final int viewType) {
        final View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_game, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder,
                                  final int position) {
        holder.bind(getItem(position), mListener);
    }

    // -------------------------------------------------------------------------

    static final class ViewHolder extends RecyclerView.ViewHolder {

        private final ImageView mIcon;
        private final TextView mTitle;
        private final TextView mPath;

        ViewHolder(@NonNull final View itemView) {
            super(itemView);
            mIcon  = itemView.findViewById(R.id.iv_game_icon);
            mTitle = itemView.findViewById(R.id.tv_game_title);
            mPath  = itemView.findViewById(R.id.tv_game_path);
        }

        void bind(final GameEntry game,
                  final OnGameClickListener listener) {
            mTitle.setText(game.getTitle());
            mPath.setText(game.getDisplayPath());

            if (game.getIconBitmap() != null) {
                mIcon.setImageBitmap(game.getIconBitmap());
            } else {
                mIcon.setImageResource(R.drawable.ic_launcher_foreground);
            }

            itemView.setOnClickListener(v -> listener.onGameClick(game));
        }
    }
}
