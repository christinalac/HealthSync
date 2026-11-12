package com.example.healthsync;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class FreeExerciseDbApi {

    private static final String API_URL =
            "https://"
                    + "raw"
                    + ".githubusercontent"
                    + ".com/yuhonas/free-exercise-db/main/dist/exercises.json";


    private static final List<ExerciseCatalog.Entry> cachedExercises = new ArrayList<>();

    public interface Callback {
        void onSuccess(List<ExerciseCatalog.Entry> exercises);
        void onError(Exception e);
    }

    public static void fetchExercises(Callback callback) {
        if (!cachedExercises.isEmpty()) {
            callback.onSuccess(new ArrayList<>(cachedExercises));
            return;
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                URL url = new URL(API_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", "HealthSync-Android");
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream())
                );
                StringBuilder jsonBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonBuilder.append(line);
                }
                reader.close();

                JSONArray array = new JSONArray(jsonBuilder.toString());
                List<ExerciseCatalog.Entry> parsedList = new ArrayList<>();

                for (int i = 0; i < array.length(); i++) {
                    JSONObject obj = array.getJSONObject(i);

                    String name = obj.optString("name", "Unknown Exercise");
                    String category = capitalize(obj.optString("category", "Strength"));

                    List<String> muscles = new ArrayList<>();
                    String rawPrimary = "";

                    JSONArray primary = obj.optJSONArray("primaryMuscles");
                    if (primary != null && primary.length() > 0) {
                        rawPrimary = primary.getString(0);
                        for (int j = 0; j < primary.length(); j++) {
                            String m = capitalize(primary.getString(j));
                            if (!muscles.contains(m)) muscles.add(m);
                        }
                    }

                    JSONArray secondary = obj.optJSONArray("secondaryMuscles");
                    if (secondary != null) {
                        for (int j = 0; j < secondary.length(); j++) {
                            String m = capitalize(secondary.getString(j));
                            if (!muscles.contains(m)) muscles.add(m);
                        }
                    }

                    // Map primary muscle to your filter chips (Arms, Legs, Back, Chest, Shoulders, Core)
                    String filterGroup = mapToFilterGroup(rawPrimary);

                    while (muscles.size() < 3) {
                        if (!muscles.contains(category)) {
                            muscles.add(category);
                        } else {
                            muscles.add("Fitness");
                        }
                    }
                    String[] tags = new String[]{muscles.get(0), muscles.get(1), muscles.get(2)};

                    String tip = category;
                    JSONArray instructions = obj.optJSONArray("instructions");
                    if (instructions != null && instructions.length() > 0) {
                        tip = instructions.getString(0);
                    }

                    parsedList.add(new ExerciseCatalog.Entry(name, filterGroup, "3x10", tags, tip));
                }

                new Handler(Looper.getMainLooper()).post(() -> {
                    cachedExercises.clear();
                    cachedExercises.addAll(parsedList);
                    callback.onSuccess(new ArrayList<>(cachedExercises));
                });

            } catch (Exception e) {
                Log.e("FreeExerciseDbApi", "API fetch failed", e);
                new Handler(Looper.getMainLooper()).post(() -> callback.onError(e));
            }
        });
    }

    private static String mapToFilterGroup(String muscle) {
        if (muscle == null) return ExerciseCatalog.FILTER_CORE;
        switch (muscle.toLowerCase(Locale.ROOT).trim()) {
            case "biceps":
            case "triceps":
            case "forearms":
                return ExerciseCatalog.FILTER_ARMS;
            case "quadriceps":
            case "hamstrings":
            case "calves":
            case "glutes":
            case "adductors":
            case "abductors":
                return ExerciseCatalog.FILTER_LEGS;
            case "chest":
                return ExerciseCatalog.FILTER_CHEST;
            case "lats":
            case "middle back":
            case "lower back":
            case "traps":
                return ExerciseCatalog.FILTER_BACK;
            case "shoulders":
            case "neck":
                return ExerciseCatalog.FILTER_SHOULDERS;
            case "abdominals":
            default:
                return ExerciseCatalog.FILTER_CORE;
        }
    }

    private static String capitalize(String text) {
        if (text == null || text.isEmpty()) return text;
        return text.substring(0, 1).toUpperCase(Locale.ROOT) + text.substring(1);
    }
}
