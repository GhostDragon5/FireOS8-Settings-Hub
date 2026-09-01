package com.fireos8.settingshub;

import android.app.Activity;
import android.app.Dialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends Activity implements DashboardAdapter.Listener {

    private static final int GRID_COLUMNS = 6;

    private DashboardAdapter dashboardAdapter;
    private RecyclerView dashboardGrid;
    private TileRepository tileRepository;
    private RootShell rootShell;
    private UpdateManager updateManager;
    private boolean rootAvailable;
    private DashboardTheme currentTheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tileRepository = new TileRepository(this);
        rootShell = new RootShell();
        updateManager = new UpdateManager(this);
        dashboardGrid = findViewById(R.id.dashboard_grid);
        currentTheme = DashboardTheme.fromPreference(tileRepository.loadTheme());
        ((TextView) findViewById(R.id.app_version)).setText(
                getString(R.string.version_format, BuildConfig.VERSION_NAME)
        );

        dashboardAdapter = new DashboardAdapter(
                tileRepository.load(defaultTiles()),
                this
        );

        GridLayoutManager layoutManager = new GridLayoutManager(this, GRID_COLUMNS);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return dashboardAdapter.getItem(position).spanX;
            }
        });

        dashboardGrid.setLayoutManager(layoutManager);
        dashboardGrid.setAdapter(dashboardAdapter);
        findViewById(R.id.button_close).setOnClickListener(view -> finishAndRemoveTask());
        findViewById(R.id.button_edit).setOnClickListener(view -> toggleEditMode());
        findViewById(R.id.button_theme).setOnClickListener(view -> showThemePicker());

        applyTheme();
        findViewById(R.id.dashboard_root).startAnimation(
                AnimationUtils.loadAnimation(this, R.anim.slide_in_from_bottom)
        );
        checkRoot();
        checkForUpdate();
    }

    private List<DashboardTile> defaultTiles() {
        return Arrays.asList(
                new DashboardTile("display", R.string.display_sounds,
                        "am start -n com.amazon.tv.settings.v2/.tv.display_sounds.DisplayAndSoundsActivity",
                        2, 2, false),
                new DashboardTile("network", R.string.network,
                        "am start -n com.amazon.tv.settings.v2/.tv.network.NetworkActivity",
                        2, 2, false),
                new DashboardTile("apps", R.string.apps,
                        "am start -n com.amazon.tv.settings.v2/.tv.applications.ApplicationsActivity",
                        1, 2, false),
                new DashboardTile("controllers", R.string.controllers,
                        "am start -n com.amazon.tv.settings.v2/.tv.controllers_bluetooth_devices.ControllersAndBluetoothActivity",
                        1, 2, false),
                new DashboardTile("device", R.string.device,
                        "am start -n com.amazon.tv.settings.v2/.tv.device.DeviceActivity",
                        1, 2, false),
                new DashboardTile("account", R.string.account,
                        "am start -n com.amazon.tv.settings.v2/.tv.my_account.MyAccountActivity",
                        1, 2, false),
                new DashboardTile("accessibility", R.string.accessibility,
                        "am start -n com.amazon.tv.settings.v2/.tv.accessibility.AccessibilityActivity",
                        2, 2, false),
                new DashboardTile("manage_apps", R.string.manage_apps,
                        "am start -a android.settings.MANAGE_APPLICATIONS_SETTINGS",
                        1, 2, true)
        );
    }

    private void checkRoot() {
        rootShell.execute("id", result -> runOnUiThread(() -> {
            rootAvailable = result.success && result.output.contains("uid=0");
            if (!rootAvailable) {
                showToast(R.string.root_not_available);
            }
        }));
    }

    private void checkForUpdate() {
        updateManager.checkForUpdate(update -> runOnUiThread(() -> showUpdateDialog(update)));
    }

    private void showUpdateDialog(UpdateInfo update) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_update);

        dialog.findViewById(R.id.update_dialog_panel).setBackground(panelBackground());
        ((TextView) dialog.findViewById(R.id.update_dialog_title)).setTextColor(currentTheme.title(this));
        ((TextView) dialog.findViewById(R.id.update_dialog_version)).setText(
                getString(R.string.update_message, update.version)
        );
        ((TextView) dialog.findViewById(R.id.update_dialog_version)).setTextColor(currentTheme.subtitle(this));
        ((TextView) dialog.findViewById(R.id.update_dialog_changelog_label))
                .setTextColor(currentTheme.title(this));
        TextView changelog = dialog.findViewById(R.id.update_dialog_changelog);
        changelog.setText(update.changelog.isEmpty() ? getString(R.string.no_changelog) : update.changelog);
        changelog.setTextColor(currentTheme.tileText(this));

        Button cancel = dialog.findViewById(R.id.button_not_now);
        Button install = dialog.findViewById(R.id.button_update);
        applyControlTheme(cancel);
        applyThemeChoice(install, currentTheme);
        cancel.setOnClickListener(view -> dialog.dismiss());
        install.setOnClickListener(view -> {
            dialog.dismiss();
            installUpdate(update);
        });

        dialog.setOnShowListener(ignored -> install.requestFocus());
        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    private void installUpdate(UpdateInfo update) {
        showToast(R.string.update_downloading);
        updateManager.downloadAndInstall(update, rootShell, success -> runOnUiThread(() ->
                showToast(success ? R.string.update_installed : R.string.update_failed)
        ));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (dashboardAdapter != null && !dashboardAdapter.isEditing()) {
            focusTile(0);
        }
    }

    @Override
    public void onTileClicked(DashboardTile tile) {
        rootShell.execute(tile.command, result -> runOnUiThread(() -> {
            if (!result.success) {
                showToast(R.string.intent_failed);
            }
        }));
    }

    @Override
    public boolean onMoveRequested(int fromPosition, int keyCode) {
        int targetPosition = targetPosition(fromPosition, keyCode);
        if (targetPosition < 0 || targetPosition >= dashboardAdapter.getItemCount()) {
            return true;
        }

        dashboardAdapter.swapItems(fromPosition, targetPosition);
        dashboardAdapter.setMovingPosition(targetPosition);
        dashboardGrid.getLayoutManager().requestLayout();

        dashboardGrid.post(() -> {
            RecyclerView.ViewHolder holder =
                    dashboardGrid.findViewHolderForAdapterPosition(targetPosition);
            if (holder != null) {
                holder.itemView.requestFocus();
            }
        });
        return true;
    }

    @Override
    public void onMoveToggleRequested(int position) {
        if (dashboardAdapter.isMoving()) {
            dashboardAdapter.stopMoving();
            tileRepository.save(dashboardAdapter.getTiles());
            showToast(R.string.position_saved);
        } else {
            dashboardAdapter.beginMoving(position);
            focusTile(position);
            showToast(R.string.move_tile);
        }
    }

    @Override
    public void onTileSizeChangeRequested(int position) {
        dashboardAdapter.cycleSize(position);
        tileRepository.save(dashboardAdapter.getTiles());
        dashboardGrid.getLayoutManager().requestLayout();
        showToast(R.string.tile_size_changed);
    }

    private int targetPosition(int position, int keyCode) {
        RecyclerView.ViewHolder currentHolder =
                dashboardGrid.findViewHolderForAdapterPosition(position);
        if (currentHolder == null) {
            return position;
        }

        View current = currentHolder.itemView;
        int currentX = (current.getLeft() + current.getRight()) / 2;
        int currentY = (current.getTop() + current.getBottom()) / 2;
        int closestPosition = position;
        int bestDistance = Integer.MAX_VALUE;

        for (int index = 0; index < dashboardGrid.getChildCount(); index++) {
            View candidate = dashboardGrid.getChildAt(index);
            int candidatePosition = dashboardGrid.getChildAdapterPosition(candidate);
            if (candidatePosition == RecyclerView.NO_POSITION || candidatePosition == position) {
                continue;
            }

            int candidateX = (candidate.getLeft() + candidate.getRight()) / 2;
            int candidateY = (candidate.getTop() + candidate.getBottom()) / 2;
            int horizontalDistance = Math.abs(candidateX - currentX);
            int verticalDistance = Math.abs(candidateY - currentY);
            boolean isInDirection;
            int distance;

            switch (keyCode) {
                case KeyEvent.KEYCODE_DPAD_LEFT:
                    isInDirection = candidateX < currentX;
                    distance = directionalDistance(
                            candidate.getTop() < current.getBottom()
                                    && candidate.getBottom() > current.getTop(),
                            horizontalDistance,
                            verticalDistance
                    );
                    break;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    isInDirection = candidateX > currentX;
                    distance = directionalDistance(
                            candidate.getTop() < current.getBottom()
                                    && candidate.getBottom() > current.getTop(),
                            horizontalDistance,
                            verticalDistance
                    );
                    break;
                case KeyEvent.KEYCODE_DPAD_UP:
                    isInDirection = candidateY < currentY;
                    distance = directionalDistance(
                            candidate.getLeft() < current.getRight()
                                    && candidate.getRight() > current.getLeft(),
                            verticalDistance,
                            horizontalDistance
                    );
                    break;
                case KeyEvent.KEYCODE_DPAD_DOWN:
                    isInDirection = candidateY > currentY;
                    distance = directionalDistance(
                            candidate.getLeft() < current.getRight()
                                    && candidate.getRight() > current.getLeft(),
                            verticalDistance,
                            horizontalDistance
                    );
                    break;
                default:
                    return position;
            }

            if (isInDirection && distance < bestDistance) {
                bestDistance = distance;
                closestPosition = candidatePosition;
            }
        }
        return closestPosition;
    }

    private int directionalDistance(boolean aligned, int primaryDistance, int secondaryDistance) {
        // Keep left/right moves in the visible row and up/down moves in the visible column.
        return (aligned ? 0 : 1_000_000) + primaryDistance * 10 + secondaryDistance;
    }

    @Override
    public void onBackPressed() {
        if (dashboardAdapter.isEditing()) {
            finishEditing();
            return;
        }
        super.onBackPressed();
    }

    private void showToast(int stringRes) {
        Toast.makeText(this, stringRes, Toast.LENGTH_LONG).show();
    }

    private void toggleEditMode() {
        if (dashboardAdapter.isEditing()) {
            finishEditing();
        } else {
            startEditing(0);
        }
    }

    private void startEditing(int position) {
        dashboardAdapter.beginEditing();
        ((Button) findViewById(R.id.button_edit)).setText(R.string.done);
        findViewById(R.id.edit_hint).setVisibility(View.VISIBLE);
        focusTile(position);
        showToast(R.string.edit_mode_started);
    }

    private void finishEditing() {
        if (dashboardAdapter.isMoving()) {
            tileRepository.save(dashboardAdapter.getTiles());
        }
        dashboardAdapter.endEditing();
        ((Button) findViewById(R.id.button_edit)).setText(R.string.edit);
        findViewById(R.id.edit_hint).setVisibility(View.GONE);
        tileRepository.save(dashboardAdapter.getTiles());
        showToast(R.string.edit_mode_finished);
    }

    private void showThemePicker() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_theme_picker);

        View panel = dialog.findViewById(R.id.theme_dialog_panel);
        panel.setBackground(panelBackground());
        ((TextView) dialog.findViewById(R.id.theme_dialog_title)).setTextColor(currentTheme.title(this));
        configureThemeChoice(dialog, R.id.button_theme_graphite, DashboardTheme.GRAPHITE);
        configureThemeChoice(dialog, R.id.button_theme_ocean, DashboardTheme.OCEAN);
        configureThemeChoice(dialog, R.id.button_theme_light, DashboardTheme.LIGHT);

        dialog.setOnShowListener(ignored ->
                dialog.findViewById(R.id.button_theme_graphite).requestFocus()
        );
        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    private void configureThemeChoice(Dialog dialog, int buttonId, DashboardTheme theme) {
        Button button = dialog.findViewById(buttonId);
        applyThemeChoice(button, theme);
        button.setOnClickListener(view -> selectTheme(theme, dialog));
    }

    private void selectTheme(DashboardTheme theme, Dialog dialog) {
        currentTheme = theme;
        tileRepository.saveTheme(currentTheme);
        applyTheme();
        dialog.dismiss();
        findViewById(R.id.button_theme).requestFocus();
    }

    private void applyTheme() {
        findViewById(R.id.dashboard_root).setBackground(panelBackground());
        ((TextView) findViewById(R.id.section_label)).setTextColor(currentTheme.accent(this));
        ((TextView) findViewById(R.id.dashboard_title)).setTextColor(currentTheme.title(this));
        ((TextView) findViewById(R.id.dashboard_subtitle)).setTextColor(currentTheme.subtitle(this));
        ((TextView) findViewById(R.id.credits)).setTextColor(currentTheme.subtitle(this));
        ((TextView) findViewById(R.id.app_version)).setTextColor(currentTheme.subtitle(this));
        ((TextView) findViewById(R.id.edit_hint)).setTextColor(currentTheme.accent(this));
        applyControlTheme((Button) findViewById(R.id.button_theme));
        applyControlTheme((Button) findViewById(R.id.button_edit));
        applyControlTheme((Button) findViewById(R.id.button_close));
        dashboardAdapter.setTheme(currentTheme);
    }

    private void applyControlTheme(Button button) {
        StateListDrawable background = new StateListDrawable();
        background.addState(new int[]{android.R.attr.state_focused},
                controlShape(currentTheme.focus(this), 0));
        background.addState(new int[]{},
                controlShape(currentTheme.surface(this), currentTheme.accent(this)));
        button.setBackground(background);
        button.setTextColor(new ColorStateList(
                new int[][]{new int[]{android.R.attr.state_focused}, new int[]{}},
                new int[]{currentTheme.focusText(this), currentTheme.tileText(this)}
        ));
    }

    private GradientDrawable controlShape(int color, int strokeColor) {
        GradientDrawable shape = new GradientDrawable();
        shape.setColor(color);
        shape.setCornerRadius(dp(8));
        if (strokeColor != 0) {
            shape.setStroke(dp(1), strokeColor);
        }
        return shape;
    }

    private void applyThemeChoice(Button button, DashboardTheme theme) {
        boolean selected = currentTheme == theme;
        StateListDrawable background = new StateListDrawable();
        background.addState(new int[]{android.R.attr.state_focused},
                controlShape(currentTheme.focus(this), 0));
        background.addState(new int[]{}, controlShape(
                selected ? currentTheme.accent(this) : currentTheme.surface(this),
                selected ? 0 : currentTheme.accent(this)
        ));
        button.setBackground(background);
        button.setTextColor(new ColorStateList(
                new int[][]{new int[]{android.R.attr.state_focused}, new int[]{}},
                new int[]{currentTheme.focusText(this),
                        selected ? currentTheme.focusText(this) : currentTheme.tileText(this)}
        ));
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private GradientDrawable panelBackground() {
        GradientDrawable panel = new GradientDrawable();
        panel.setColor(currentTheme.background(this));
        panel.setCornerRadius(dp(18));
        return panel;
    }

    private void focusTile(int position) {
        dashboardGrid.post(() -> {
            RecyclerView.ViewHolder holder = dashboardGrid.findViewHolderForAdapterPosition(position);
            if (holder != null) {
                holder.itemView.requestFocus();
            }
        });
    }

    @Override
    protected void onDestroy() {
        updateManager.shutdown();
        rootShell.shutdown();
        super.onDestroy();
    }
}
