package com.fireos8.settingshub;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class TileRepository {

    private static final String PREFERENCES = "dashboard_tiles";
    private static final String TILE_CONFIG = "tile_config";
    private static final String THEME = "theme";
    private final SharedPreferences preferences;

    public TileRepository(Context context) {
        preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE);
    }

    public List<DashboardTile> load(List<DashboardTile> defaults) {
        String savedConfig = preferences.getString(TILE_CONFIG, null);
        if (savedConfig == null) {
            return new ArrayList<>(defaults);
        }

        Map<String, DashboardTile> defaultsById = new HashMap<>();
        for (DashboardTile tile : defaults) {
            defaultsById.put(tile.id, tile);
        }

        try {
            JSONArray savedTiles = new JSONArray(savedConfig);
            List<DashboardTile> loadedTiles = new ArrayList<>();
            Set<String> seenIds = new HashSet<>();

            for (int index = 0; index < savedTiles.length(); index++) {
                JSONObject savedTile = savedTiles.getJSONObject(index);
                DashboardTile defaultTile = defaultsById.get(savedTile.getString("id"));
                if (defaultTile == null || !seenIds.add(defaultTile.id)) {
                    continue;
                }

                int spanX = clamp(savedTile.optInt("spanX", defaultTile.spanX), 1, 2);
                int spanY = 2;
                loadedTiles.add(defaultTile.withSpans(spanX, spanY));
            }

            for (DashboardTile defaultTile : defaults) {
                if (!seenIds.contains(defaultTile.id)) {
                    loadedTiles.add(defaultTile);
                }
            }
            return loadedTiles;
        } catch (JSONException exception) {
            return new ArrayList<>(defaults);
        }
    }

    public void save(List<DashboardTile> tiles) {
        JSONArray savedTiles = new JSONArray();
        for (DashboardTile tile : tiles) {
            JSONObject savedTile = new JSONObject();
            try {
                savedTile.put("id", tile.id);
                savedTile.put("spanX", tile.spanX);
                savedTile.put("spanY", tile.spanY);
                savedTiles.put(savedTile);
            } catch (JSONException ignored) {
                // JSONObject only receives local primitive values here.
            }
        }
        preferences.edit().putString(TILE_CONFIG, savedTiles.toString()).apply();
    }

    public String loadTheme() {
        return preferences.getString(THEME, DashboardTheme.GRAPHITE.preferenceValue);
    }

    public void saveTheme(DashboardTheme theme) {
        preferences.edit().putString(THEME, theme.preferenceValue).apply();
    }

    private int clamp(int value, int minimum, int maximum) {
        return Math.max(minimum, Math.min(maximum, value));
    }
}
