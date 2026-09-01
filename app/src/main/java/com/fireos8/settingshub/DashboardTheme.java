package com.fireos8.settingshub;

import android.content.Context;

import androidx.core.content.ContextCompat;

public enum DashboardTheme {
    GRAPHITE("graphite", R.color.graphite_background, R.color.graphite_surface,
            R.color.graphite_focus, R.color.graphite_title, R.color.graphite_subtitle,
            R.color.graphite_accent, R.color.graphite_tile_text, R.color.graphite_focus_text),
    OCEAN("ocean", R.color.ocean_background, R.color.ocean_surface,
            R.color.ocean_focus, R.color.ocean_title, R.color.ocean_subtitle,
            R.color.ocean_accent, R.color.ocean_tile_text, R.color.ocean_focus_text),
    LIGHT("light", R.color.light_background, R.color.light_surface,
            R.color.light_focus, R.color.light_title, R.color.light_subtitle,
            R.color.light_accent, R.color.light_tile_text, R.color.light_focus_text);

    public final String preferenceValue;
    private final int backgroundRes;
    private final int surfaceRes;
    private final int focusRes;
    private final int titleRes;
    private final int subtitleRes;
    private final int accentRes;
    private final int tileTextRes;
    private final int focusTextRes;

    DashboardTheme(String preferenceValue, int backgroundRes, int surfaceRes, int focusRes,
                   int titleRes, int subtitleRes, int accentRes, int tileTextRes,
                   int focusTextRes) {
        this.preferenceValue = preferenceValue;
        this.backgroundRes = backgroundRes;
        this.surfaceRes = surfaceRes;
        this.focusRes = focusRes;
        this.titleRes = titleRes;
        this.subtitleRes = subtitleRes;
        this.accentRes = accentRes;
        this.tileTextRes = tileTextRes;
        this.focusTextRes = focusTextRes;
    }

    public int background(Context context) { return color(context, backgroundRes); }
    public int surface(Context context) { return color(context, surfaceRes); }
    public int focus(Context context) { return color(context, focusRes); }
    public int title(Context context) { return color(context, titleRes); }
    public int subtitle(Context context) { return color(context, subtitleRes); }
    public int accent(Context context) { return color(context, accentRes); }
    public int tileText(Context context) { return color(context, tileTextRes); }
    public int focusText(Context context) { return color(context, focusTextRes); }

    public static DashboardTheme fromPreference(String value) {
        for (DashboardTheme theme : values()) {
            if (theme.preferenceValue.equals(value)) {
                return theme;
            }
        }
        return GRAPHITE;
    }

    private int color(Context context, int colorRes) {
        return ContextCompat.getColor(context, colorRes);
    }
}
