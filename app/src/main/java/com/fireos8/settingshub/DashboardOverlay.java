package com.fireos8.settingshub;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.List;

public final class DashboardOverlay implements DashboardAdapter.Listener {

    private static final int GRID_COLUMNS = 6;

    private final Context appContext;
    private final WindowManager windowManager;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private View overlayView;
    private ViewGroup dialogHost;
    private RecyclerView dashboardGrid;
    private DashboardAdapter dashboardAdapter;
    private TileRepository tileRepository;
    private RootShell rootShell;
    private UpdateManager updateManager;
    private boolean rootAvailable;
    private DashboardTheme currentTheme;
    private Runnable closeListener;

    public DashboardOverlay(Context context) {
        appContext = context.getApplicationContext();
        windowManager = (WindowManager) appContext.getSystemService(Context.WINDOW_SERVICE);
    }

    public void setCloseListener(Runnable closeListener) {
        this.closeListener = closeListener;
    }

    public boolean isShowing() {
        return overlayView != null;
    }

    public void show() {
        if (overlayView != null) {
            return;
        }

        overlayView = LayoutInflater.from(appContext).inflate(R.layout.activity_main, null);
        dialogHost = overlayView.findViewById(R.id.dialog_host);
        dashboardGrid = overlayView.findViewById(R.id.dashboard_grid);
        tileRepository = new TileRepository(appContext);
        rootShell = new RootShell();
        updateManager = new UpdateManager(appContext);
        currentTheme = DashboardTheme.fromPreference(tileRepository.loadTheme());

        ((TextView) overlayView.findViewById(R.id.app_version)).setText(
                appContext.getString(R.string.version_format, BuildConfig.VERSION_NAME)
        );

        dashboardAdapter = new DashboardAdapter(tileRepository.load(defaultTiles()), this);
        GridLayoutManager layoutManager = new GridLayoutManager(appContext, GRID_COLUMNS);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return dashboardAdapter.getItem(position).spanX;
            }
        });
        dashboardGrid.setLayoutManager(layoutManager);
        dashboardGrid.setAdapter(dashboardAdapter);

        overlayView.findViewById(R.id.button_close).setOnClickListener(view -> close());
        overlayView.findViewById(R.id.button_edit).setOnClickListener(view -> toggleEditMode());
        overlayView.findViewById(R.id.button_theme).setOnClickListener(view -> showThemePicker());

        overlayView.setFocusableInTouchMode(true);
        overlayView.setOnKeyListener((view, keyCode, event) -> {
            if (keyCode != KeyEvent.KEYCODE_BACK || event.getAction() != KeyEvent.ACTION_UP) {
                return false;
            }
            return handleBack();
        });

        applyTheme();

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                overlayType(),
                0,
                PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.CENTER;

        // The view must be attached before animating, otherwise the animation is dropped.
        windowManager.addView(overlayView, params);
        overlayView.findViewById(R.id.dashboard_root).startAnimation(
                AnimationUtils.loadAnimation(appContext, R.anim.slide_in_from_bottom)
        );

        focusTile(0);
        checkRoot();
        checkForUpdate();
    }

    private void close() {
        hide();
        if (closeListener != null) {
            closeListener.run();
        }
    }

    public void hide() {
        if (overlayView == null) {
            return;
        }
        updateManager.shutdown();
        rootShell.shutdown();
        windowManager.removeView(overlayView);
        overlayView = null;
        dialogHost = null;
    }

    private int overlayType() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }
        return WindowManager.LayoutParams.TYPE_PHONE;
    }

    private boolean handleBack() {
        if (dialogHost.getVisibility() == View.VISIBLE) {
            closeDialog();
            return true;
        }
        if (dashboardAdapter.isEditing()) {
            finishEditing();
            return true;
        }
        close();
        return true;
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
        rootShell.execute("id", result -> runOnUi(() -> {
            rootAvailable = result.success && result.output.contains("uid=0");
            if (!rootAvailable) {
                showToast(R.string.root_not_available);
            }
        }));
    }

    private void checkForUpdate() {
        updateManager.checkForUpdate(update -> runOnUi(() -> showUpdateDialog(update)));
    }

    private void showUpdateDialog(UpdateInfo update) {
        View dialog = LayoutInflater.from(appContext)
                .inflate(R.layout.dialog_update, dialogHost, false);

        dialog.findViewById(R.id.update_dialog_panel).setBackground(panelBackground());
        ((TextView) dialog.findViewById(R.id.update_dialog_title)).setTextColor(currentTheme.title(appContext));
        ((TextView) dialog.findViewById(R.id.update_dialog_version)).setText(
                appContext.getString(R.string.update_message, update.version)
        );
        ((TextView) dialog.findViewById(R.id.update_dialog_version))
                .setTextColor(currentTheme.subtitle(appContext));
        ((TextView) dialog.findViewById(R.id.update_dialog_changelog_label))
                .setTextColor(currentTheme.title(appContext));

        TextView changelog = dialog.findViewById(R.id.update_dialog_changelog);
        changelog.setText(update.changelog.isEmpty()
                ? appContext.getString(R.string.no_changelog)
                : update.changelog);
        changelog.setTextColor(currentTheme.tileText(appContext));

        Button cancel = dialog.findViewById(R.id.button_not_now);
        Button install = dialog.findViewById(R.id.button_update);
        applyControlTheme(cancel);
        applyThemeChoice(install, currentTheme);
        cancel.setOnClickListener(view -> closeDialog());
        install.setOnClickListener(view -> {
            closeDialog();
            installUpdate(update);
        });

        openDialog(dialog, install);
    }

    private void installUpdate(UpdateInfo update) {
        showToast(R.string.update_downloading);
        updateManager.downloadAndInstall(update, rootShell, success -> runOnUi(() ->
                showToast(success ? R.string.update_installed : R.string.update_failed)
        ));
    }

    @Override
    public void onTileClicked(DashboardTile tile) {
        rootShell.execute(tile.command, result -> runOnUi(() -> {
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
        focusTile(targetPosition);
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
                            horizontalDistance, verticalDistance);
                    break;
                case KeyEvent.KEYCODE_DPAD_RIGHT:
                    isInDirection = candidateX > currentX;
                    distance = directionalDistance(
                            candidate.getTop() < current.getBottom()
                                    && candidate.getBottom() > current.getTop(),
                            horizontalDistance, verticalDistance);
                    break;
                case KeyEvent.KEYCODE_DPAD_UP:
                    isInDirection = candidateY < currentY;
                    distance = directionalDistance(
                            candidate.getLeft() < current.getRight()
                                    && candidate.getRight() > current.getLeft(),
                            verticalDistance, horizontalDistance);
                    break;
                case KeyEvent.KEYCODE_DPAD_DOWN:
                    isInDirection = candidateY > currentY;
                    distance = directionalDistance(
                            candidate.getLeft() < current.getRight()
                                    && candidate.getRight() > current.getLeft(),
                            verticalDistance, horizontalDistance);
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
        return (aligned ? 0 : 1_000_000) + primaryDistance * 10 + secondaryDistance;
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
        ((Button) overlayView.findViewById(R.id.button_edit)).setText(R.string.done);
        overlayView.findViewById(R.id.edit_hint).setVisibility(View.VISIBLE);
        focusTile(position);
        showToast(R.string.edit_mode_started);
    }

    private void finishEditing() {
        if (dashboardAdapter.isMoving()) {
            tileRepository.save(dashboardAdapter.getTiles());
        }
        dashboardAdapter.endEditing();
        ((Button) overlayView.findViewById(R.id.button_edit)).setText(R.string.edit);
        overlayView.findViewById(R.id.edit_hint).setVisibility(View.GONE);
        tileRepository.save(dashboardAdapter.getTiles());
        showToast(R.string.edit_mode_finished);
    }

    private void showThemePicker() {
        View dialog = LayoutInflater.from(appContext)
                .inflate(R.layout.dialog_theme_picker, dialogHost, false);

        dialog.findViewById(R.id.theme_dialog_panel).setBackground(panelBackground());
        ((TextView) dialog.findViewById(R.id.theme_dialog_title))
                .setTextColor(currentTheme.title(appContext));
        configureThemeChoice(dialog, R.id.button_theme_graphite, DashboardTheme.GRAPHITE);
        configureThemeChoice(dialog, R.id.button_theme_ocean, DashboardTheme.OCEAN);
        configureThemeChoice(dialog, R.id.button_theme_light, DashboardTheme.LIGHT);

        View first = dialog.findViewById(R.id.button_theme_graphite);
        openDialog(dialog, first);
    }

    private void configureThemeChoice(View dialog, int buttonId, DashboardTheme theme) {
        Button button = dialog.findViewById(buttonId);
        applyThemeChoice(button, theme);
        button.setOnClickListener(view -> selectTheme(theme));
    }

    private void selectTheme(DashboardTheme theme) {
        currentTheme = theme;
        tileRepository.saveTheme(currentTheme);
        applyTheme();
        closeDialog();
        overlayView.findViewById(R.id.button_theme).requestFocus();
    }

    private void openDialog(View dialog, View initialFocus) {
        closeDialog();
        dialogHost.removeAllViews();
        dialogHost.addView(dialog, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER
        ));
        dialogHost.setVisibility(View.VISIBLE);
        dialogHost.bringToFront();
        initialFocus.post(initialFocus::requestFocus);
    }

    private void closeDialog() {
        dialogHost.removeAllViews();
        dialogHost.setVisibility(View.GONE);
    }

    private void applyTheme() {
        overlayView.findViewById(R.id.dashboard_root).setBackground(panelBackground());
        ((TextView) overlayView.findViewById(R.id.section_label))
                .setTextColor(currentTheme.accent(appContext));
        ((TextView) overlayView.findViewById(R.id.dashboard_title))
                .setTextColor(currentTheme.title(appContext));
        ((TextView) overlayView.findViewById(R.id.dashboard_subtitle))
                .setTextColor(currentTheme.subtitle(appContext));
        ((TextView) overlayView.findViewById(R.id.credits))
                .setTextColor(currentTheme.subtitle(appContext));
        ((TextView) overlayView.findViewById(R.id.app_version))
                .setTextColor(currentTheme.subtitle(appContext));
        ((TextView) overlayView.findViewById(R.id.edit_hint))
                .setTextColor(currentTheme.accent(appContext));
        applyControlTheme((Button) overlayView.findViewById(R.id.button_theme));
        applyControlTheme((Button) overlayView.findViewById(R.id.button_edit));
        applyControlTheme((Button) overlayView.findViewById(R.id.button_close));
        dashboardAdapter.setTheme(currentTheme);
    }

    private void applyControlTheme(Button button) {
        StateListDrawable background = new StateListDrawable();
        background.addState(new int[]{android.R.attr.state_focused},
                controlShape(currentTheme.focus(appContext), 0));
        background.addState(new int[]{},
                controlShape(currentTheme.surface(appContext), currentTheme.accent(appContext)));
        button.setBackground(background);
        button.setTextColor(new ColorStateList(
                new int[][]{new int[]{android.R.attr.state_focused}, new int[]{}},
                new int[]{currentTheme.focusText(appContext), currentTheme.tileText(appContext)}
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
                controlShape(currentTheme.focus(appContext), 0));
        background.addState(new int[]{}, controlShape(
                selected ? currentTheme.accent(appContext) : currentTheme.surface(appContext),
                selected ? 0 : currentTheme.accent(appContext)
        ));
        button.setBackground(background);
        button.setTextColor(new ColorStateList(
                new int[][]{new int[]{android.R.attr.state_focused}, new int[]{}},
                new int[]{currentTheme.focusText(appContext),
                        selected ? currentTheme.focusText(appContext)
                                : currentTheme.tileText(appContext)}
        ));
    }

    private int dp(int value) {
        return (int) (value * appContext.getResources().getDisplayMetrics().density + 0.5f);
    }

    private GradientDrawable panelBackground() {
        GradientDrawable panel = new GradientDrawable();
        panel.setColor(currentTheme.background(appContext));
        panel.setCornerRadius(dp(18));
        return panel;
    }

    private void focusTile(int position) {
        dashboardGrid.post(() -> {
            RecyclerView.ViewHolder holder =
                    dashboardGrid.findViewHolderForAdapterPosition(position);
            if (holder != null) {
                holder.itemView.requestFocus();
            }
        });
    }

    private void showToast(int stringRes) {
        Toast.makeText(appContext, stringRes, Toast.LENGTH_LONG).show();
    }

    private void runOnUi(Runnable action) {
        mainHandler.post(action);
    }
}
