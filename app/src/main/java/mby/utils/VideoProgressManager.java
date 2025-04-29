package mby.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.google.gson.Gson;

public class VideoProgressManager {
    private static final String PREFS_NAME = "VideoProgressPrefs";
    private static final String PROGRESS_KEY_PREFIX = "video_progress_";
    private static final String TAG = "VideoProgressManager";

    private final SharedPreferences sharedPreferences;
    private final Gson gson;

    public VideoProgressManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public void saveProgress(String videoId, long position, long duration) {
        try {
            VideoProgress progress = new VideoProgress(videoId, position, duration);
            String json = gson.toJson(progress);
            sharedPreferences.edit()
                    .putString(PROGRESS_KEY_PREFIX + videoId, json)
                    .apply();
            Log.d(TAG, "Saved progress for video " + videoId + ": " + position + "/" + duration);
        } catch (Exception e) {
            Log.e(TAG, "Error saving progress for video " + videoId, e);
        }
    }

    public VideoProgress getProgress(String videoId) {
        try {
            String json = sharedPreferences.getString(PROGRESS_KEY_PREFIX + videoId, null);
            if (json != null) {
                return gson.fromJson(json, VideoProgress.class);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting progress for video " + videoId, e);
        }
        return null;
    }

    public long getLastPosition(String videoId) {
        VideoProgress progress = getProgress(videoId);
        return progress != null ? progress.getLastPosition() : 0;
    }

    public boolean isVideoCompleted(String videoId) {
        VideoProgress progress = getProgress(videoId);
        return progress != null && progress.isCompleted();
    }

    public void clearProgress(String videoId) {
        try {
            sharedPreferences.edit()
                    .remove(PROGRESS_KEY_PREFIX + videoId)
                    .apply();
            Log.d(TAG, "Cleared progress for video " + videoId);
        } catch (Exception e) {
            Log.e(TAG, "Error clearing progress for video " + videoId, e);
        }
    }

    public void clearAllProgress() {
        try {
            sharedPreferences.edit().clear().apply();
            Log.d(TAG, "Cleared all video progress");
        } catch (Exception e) {
            Log.e(TAG, "Error clearing all video progress", e);
        }
    }
} 