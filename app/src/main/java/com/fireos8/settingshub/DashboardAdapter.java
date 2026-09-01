package com.fireos8.settingshub;

import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class DashboardAdapter extends RecyclerView.Adapter<DashboardAdapter.TileViewHolder> {

    public interface Listener {
        void onTileClicked(DashboardTile tile);
        boolean onMoveRequested(int fromPosition, int keyCode);
        void onMoveToggleRequested(int position);
        void onTileSizeChangeRequested(int position);
    }

    private final List<DashboardTile> tiles;
    private final Listener listener;
    private boolean editing;
    private boolean moving;
    private int movingPosition = RecyclerView.NO_POSITION;
    private DashboardTheme theme = DashboardTheme.GRAPHITE;

    public DashboardAdapter(List<DashboardTile> tiles, Listener listener) {
        this.tiles = new ArrayList<>(tiles);
        this.listener = listener;
    }

    @NonNull
    @Override
    public TileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_dashboard_tile, parent, false);
        return new TileViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TileViewHolder holder, int position) {
        DashboardTile tile = tiles.get(position);
        holder.title.setText(tile.titleRes);

        ViewGroup.LayoutParams params = holder.itemView.getLayoutParams();
        int rowHeight = dp(holder.itemView, 40);
        int tileSpacing = dp(holder.itemView, 8);
        params.height = rowHeight * tile.spanY + tileSpacing * (tile.spanY - 1);
        holder.itemView.setLayoutParams(params);
        holder.itemView.setSelected(moving && position == movingPosition);
        holder.itemView.setBackground(tileBackground(holder.itemView, moving && position == movingPosition));
        holder.title.setTextColor(holder.itemView.isFocused()
                ? theme.focusText(holder.itemView.getContext())
                : theme.tileText(holder.itemView.getContext()));
        holder.itemView.setOnFocusChangeListener((view, hasFocus) -> holder.title.setTextColor(
                hasFocus ? theme.focusText(view.getContext()) : theme.tileText(view.getContext())
        ));

        holder.itemView.setOnClickListener(view -> {
            if (!editing) {
                listener.onTileClicked(tile);
            }
        });
        holder.itemView.setOnKeyListener((view, keyCode, event) -> {
            if (!editing || event.getAction() != KeyEvent.ACTION_DOWN) {
                return false;
            }
            int adapterPosition = holder.getBindingAdapterPosition();
            if (adapterPosition == RecyclerView.NO_POSITION) {
                return true;
            }
            if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
                listener.onMoveToggleRequested(adapterPosition);
                return true;
            }
            if (keyCode == KeyEvent.KEYCODE_MEDIA_FAST_FORWARD) {
                listener.onTileSizeChangeRequested(adapterPosition);
                return true;
            }
            if (moving && adapterPosition == movingPosition
                    && (keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT
                    || keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN)) {
                return listener.onMoveRequested(adapterPosition, keyCode);
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return tiles.size();
    }

    public DashboardTile getItem(int position) {
        return tiles.get(position);
    }

    public List<DashboardTile> getTiles() {
        return new ArrayList<>(tiles);
    }

    public boolean isEditing() {
        return editing;
    }

    public void beginEditing() {
        editing = true;
        moving = false;
        movingPosition = RecyclerView.NO_POSITION;
        notifyDataSetChanged();
    }

    public boolean isMoving() {
        return moving;
    }

    public void beginMoving(int position) {
        moving = true;
        movingPosition = position;
        notifyDataSetChanged();
    }

    public void setMovingPosition(int position) {
        movingPosition = position;
        notifyDataSetChanged();
    }

    public void stopMoving() {
        moving = false;
        movingPosition = RecyclerView.NO_POSITION;
        notifyDataSetChanged();
    }

    public void endEditing() {
        editing = false;
        moving = false;
        movingPosition = RecyclerView.NO_POSITION;
        notifyDataSetChanged();
    }

    public void swapItems(int firstPosition, int secondPosition) {
        Collections.swap(tiles, firstPosition, secondPosition);
        // A swap changes span positions throughout the grid.
        notifyDataSetChanged();
    }

    public void cycleSize(int position) {
        tiles.set(position, tiles.get(position).nextSize());
        // A span change affects every following grid position, not just this tile.
        notifyDataSetChanged();
    }

    public void setTheme(DashboardTheme theme) {
        this.theme = theme;
        notifyDataSetChanged();
    }

    private StateListDrawable tileBackground(View view, boolean selected) {
        StateListDrawable drawable = new StateListDrawable();
        drawable.addState(new int[]{android.R.attr.state_focused}, shape(view, theme.focus(view.getContext()), 0));
        if (selected) {
            drawable.addState(new int[]{android.R.attr.state_selected},
                    shape(view, theme.surface(view.getContext()), theme.accent(view.getContext())));
        }
        drawable.addState(new int[]{}, shape(view, theme.surface(view.getContext()), 0));
        return drawable;
    }

    private GradientDrawable shape(View view, int color, int strokeColor) {
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(color);
        shape.setCornerRadius(dp(view, 14));
        if (strokeColor != 0) {
            shape.setStroke(dp(view, 2), strokeColor);
        }
        return shape;
    }

    private int dp(View view, int value) {
        return (int) (value * view.getResources().getDisplayMetrics().density + 0.5f);
    }

    static final class TileViewHolder extends RecyclerView.ViewHolder {
        final TextView title;

        TileViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.tile_title);
        }
    }
}
