package mby;

import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class ServerManager {
    private static final String TAG = "ServerManager";
    private final List<String> serverUrls;

    public ServerManager(List<String> serverUrls) {
        this.serverUrls = serverUrls;
    }

    public interface OnCategoriesLoadedListener {
        void onCategoriesLoaded(List<Category> categories);
        void onPartialCategoriesLoaded(List<Category> categories, int loaded, int total);
        void onError(String error);
    }

    public void loadCategories(OnCategoriesLoadedListener listener) {
        new Thread(() -> {
            Map<String, Category> categoryMap = new HashMap<>();
            int total = serverUrls.size();
            int loaded = 0;
            try {
                for (String baseUrl : serverUrls) {
                    String url = ensureUrl(baseUrl) + "categories.json";
                    Log.d(TAG, "URL категорий: " + url);
            try {
                        URL categoriesUrl = new URL(url);
                        HttpURLConnection connection = (HttpURLConnection) categoriesUrl.openConnection();
                connection.setRequestMethod("GET");
                        Log.d(TAG, "Код ответа: " + connection.getResponseCode());

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                        Log.d(TAG, "Получен ответ: " + response.toString());
                        JSONObject categoriesJson = new JSONObject(response.toString());
                        Iterator<String> keys = categoriesJson.keys();
                        while (keys.hasNext()) {
                            String categoryId = keys.next();
                            String categoryTitle = categoriesJson.getString(categoryId);
                            Log.d(TAG, "Загрузка категории: " + categoryId + " - " + categoryTitle);
                            List<Movie> movies = loadCategoryContent(baseUrl, categoryId);
                            if (categoryMap.containsKey(categoryTitle)) {
                                categoryMap.get(categoryTitle).getItems().addAll(movies);
                            } else {
                                categoryMap.put(categoryTitle, new Category(categoryId, categoryTitle, new ArrayList<>(movies)));
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Ошибка загрузки категорий с сервера: " + baseUrl, e);
                    }
                    loaded++;
                    listener.onPartialCategoriesLoaded(new ArrayList<>(categoryMap.values()), loaded, total);
                }
                Log.d(TAG, "Загружено уникальных категорий: " + categoryMap.size());
                listener.onCategoriesLoaded(new ArrayList<>(categoryMap.values()));
            } catch (Exception e) {
                Log.e(TAG, "Ошибка загрузки категорий", e);
                listener.onError("Ошибка загрузки: " + e.getMessage());
            }
        }).start();
    }

    private List<Movie> loadCategoryContent(String baseUrl, String categoryId) {
        List<Movie> movies = new ArrayList<>();
        try {
            String url = ensureUrl(baseUrl) + categoryId + "/content.json";
            Log.d(TAG, "URL контента: " + url);
            URL contentUrl = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) contentUrl.openConnection();
            connection.setRequestMethod("GET");
            Log.d(TAG, "Код ответа: " + connection.getResponseCode());

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            Log.d(TAG, "Получен контент: " + response.toString());
            JSONObject contentJson = new JSONObject(response.toString());
            JSONArray items = contentJson.getJSONArray("items");

            for (int i = 0; i < items.length(); i++) {
                JSONObject obj = items.getJSONObject(i);
                String title = obj.getString("title");
                JSONObject videosObj = obj.getJSONObject("videos");
                Map<String, String> videos = new HashMap<>();
                Iterator<String> keys = videosObj.keys();
                while (keys.hasNext()) {
                    String quality = keys.next();
                    String file = videosObj.getString(quality);
                    if (file.equals("-") || file.trim().isEmpty()) continue;
                    String videoUrl = ensureUrl(baseUrl) + categoryId + "/" + file;
                    videos.put(quality, videoUrl);
                }
                String poster = obj.getString("poster");
                String posterUrl = ensureUrl(baseUrl) + categoryId + "/" + poster;
                String defaultVideoUrl = videos.get("720p");

                movies.add(new Movie(title, defaultVideoUrl, posterUrl, videos));
                Log.d(TAG, "Добавлен фильм: " + title);
            }
            Log.d(TAG, "Загружено фильмов: " + movies.size());
        } catch (Exception e) {
            Log.e(TAG, "Ошибка загрузки контента категории: " + categoryId + " с сервера: " + baseUrl, e);
        }
        return movies;
    }

    private String ensureUrl(String baseUrl) {
        String url = baseUrl.trim();
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            url = "http://" + url;
        }
        if (!url.endsWith("/")) {
            url = url + "/";
        }
        return url;
    }
} 