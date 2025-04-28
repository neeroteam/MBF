package mby;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.fragment.app.FragmentActivity;
import androidx.leanback.app.BrowseSupportFragment;
import androidx.leanback.widget.ArrayObjectAdapter;
import androidx.leanback.widget.HeaderItem;
import androidx.leanback.widget.ListRow;
import androidx.leanback.widget.ListRowPresenter;
import androidx.leanback.widget.OnItemViewClickedListener;
import androidx.leanback.widget.Presenter;
import androidx.leanback.widget.Row;
import androidx.leanback.widget.RowPresenter;
import java.util.List;
import java.util.Map;
import android.widget.ImageButton;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.widget.ProgressBar;

public class MainActivity extends FragmentActivity {
    private static final String TAG = "MainActivity";
    private ArrayObjectAdapter rowsAdapter;
    private ServerManager serverManager;
    private ProgressBar progressBarLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        setContentView(R.layout.activity_main);

        progressBarLoading = findViewById(R.id.progressBarLoading);
        progressBarLoading.setVisibility(ProgressBar.GONE);

        ImageButton settingsButton = findViewById(R.id.settings_button);
        settingsButton.setOnClickListener(v -> showSettingsDialog());

        BrowseSupportFragment browseSupportFragment = (BrowseSupportFragment) getSupportFragmentManager()
                .findFragmentById(R.id.browse_fragment);

        setupUIElements(browseSupportFragment);
        loadCategories();
        setupEventListeners(browseSupportFragment);
    }

    private void setupUIElements(BrowseSupportFragment browseSupportFragment) {
        Log.d(TAG, "setupUIElements");
        browseSupportFragment.setTitle("Мой Контент");
        browseSupportFragment.setHeadersState(BrowseSupportFragment.HEADERS_ENABLED);
        browseSupportFragment.setHeadersTransitionOnBackEnabled(true);
        browseSupportFragment.setBrandColor(getResources().getColor(R.color.fastlane_background));
    }

    private void loadCategories() {
        Log.d(TAG, "loadCategories");
        rowsAdapter = new ArrayObjectAdapter(new ListRowPresenter());
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        java.util.Set<String> servers = prefs.getStringSet("servers", new java.util.HashSet<>(java.util.Arrays.asList("192.168.0.122")));
        serverManager = new ServerManager(new java.util.ArrayList<>(servers));

        // Сбросить адаптер перед загрузкой новых данных
        BrowseSupportFragment browseSupportFragment = (BrowseSupportFragment) getSupportFragmentManager()
            .findFragmentById(R.id.browse_fragment);
        if (browseSupportFragment != null) {
            browseSupportFragment.setAdapter(null);
        }
        if (progressBarLoading != null) progressBarLoading.setVisibility(ProgressBar.VISIBLE);

        serverManager.loadCategories(new ServerManager.OnCategoriesLoadedListener() {
            @Override
            public void onCategoriesLoaded(List<Category> categories) {
                Log.d(TAG, "onCategoriesLoaded: " + categories.size() + " категорий");
                runOnUiThread(() -> {
                    updateRowsAdapter(categories);
                    if (progressBarLoading != null) progressBarLoading.setVisibility(ProgressBar.GONE);
                });
            }
            @Override
            public void onPartialCategoriesLoaded(List<Category> categories, int loaded, int total) {
                runOnUiThread(() -> {
                    updateRowsAdapter(categories);
                    if (progressBarLoading != null) progressBarLoading.setVisibility(ProgressBar.VISIBLE);
                });
            }
            @Override
            public void onError(String error) {
                Log.e(TAG, "Ошибка загрузки категорий: " + error);
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, error, Toast.LENGTH_LONG).show();
                    if (progressBarLoading != null) progressBarLoading.setVisibility(ProgressBar.GONE);
                });
            }
        });
    }

    private void updateRowsAdapter(List<Category> categories) {
        rowsAdapter.clear();
        for (int i = 0; i < categories.size(); i++) {
            Category category = categories.get(i);
            ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(new MoviePresenter());
            for (Movie movie : category.getItems()) {
                listRowAdapter.add(movie);
            }
            HeaderItem header = new HeaderItem(i, category.getTitle());
            rowsAdapter.add(new ListRow(header, listRowAdapter));
        }
        BrowseSupportFragment browseSupportFragment = (BrowseSupportFragment) getSupportFragmentManager()
            .findFragmentById(R.id.browse_fragment);
        if (browseSupportFragment != null) {
            browseSupportFragment.setAdapter(rowsAdapter);
        }
    }

    private void setupEventListeners(BrowseSupportFragment browseSupportFragment) {
        Log.d(TAG, "setupEventListeners");
        browseSupportFragment.setOnItemViewClickedListener(new OnItemViewClickedListener() {
            @Override
            public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                    RowPresenter.ViewHolder rowViewHolder, Row row) {
                if (item instanceof Movie) {
                    Movie movie = (Movie) item;
                    Log.d(TAG, "Выбран фильм: " + movie.getTitle());
                    Map<String, String> videos = movie.getVideos();
                    String[] qualities = videos.keySet().toArray(new String[0]);
                    new android.app.AlertDialog.Builder(MainActivity.this)
                        .setTitle("Выберите качество")
                        .setItems(qualities, (dialog, which) -> {
                            String quality = qualities[which];
                            String url = videos.get(quality);
                            if (url == null || url.equals("-") || url.trim().isEmpty()) {
                                Log.d(TAG, "Файл для качества " + quality + " отсутствует");
                                Toast.makeText(MainActivity.this, "Файл для этого качества отсутствует", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            Log.d(TAG, "Запуск видео: " + url);
                            try {
                                Intent intent = new Intent(MainActivity.this, VideoPlayerActivity.class);
                                intent.putExtra("videoUrl", url);
                                intent.putExtra("title", movie.getTitle());
                                startActivity(intent);
                            } catch (Exception e) {
                                Log.e(TAG, "Ошибка запуска видео", e);
                                Toast.makeText(MainActivity.this, 
                                    "Ошибка запуска видео: " + e.getMessage(), 
                                    Toast.LENGTH_LONG).show();
                            }
                        })
                        .show();
                }
            }
        });
    }

    private void showSettingsDialog() {
        String[] items = {"Серверы", "Видео", "Debug"};
        new android.app.AlertDialog.Builder(this)
            .setTitle("Настройки")
            .setItems(items, (dialog, which) -> {
                switch (which) {
                    case 0:
                        showServersDialog();
                        break;
                    case 1:
                        showVideoSettingsDialog();
                        break;
                    case 2:
                        showDebugDialog();
                        break;
                }
            })
            .setNegativeButton("Отмена", null)
            .show();
    }

    private void showServersDialog() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        java.util.Set<String> servers = new java.util.HashSet<>(prefs.getStringSet("servers", new java.util.HashSet<>(java.util.Arrays.asList("192.168.0.122"))));
        String[] serverArray = servers.toArray(new String[0]);

        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Список серверов");
        builder.setItems(serverArray, null);
        builder.setPositiveButton("Добавить", (dialog, which) -> showAddServerDialog(servers));
        builder.setNegativeButton("Удалить", (dialog, which) -> showRemoveServerDialog(servers));
        builder.setNeutralButton("Закрыть", null);
        builder.show();
    }

    private void showAddServerDialog(java.util.Set<String> servers) {
        final android.widget.EditText input = new android.widget.EditText(this);
        input.setHint("IP или домен, например: 192.168.0.122 или maybeyoou.ddns.net");
        new android.app.AlertDialog.Builder(this)
            .setTitle("Добавить сервер")
            .setView(input)
            .setPositiveButton("OK", (dialog, which) -> {
                String ip = input.getText().toString().trim();
                if (!ip.isEmpty()) {
                    servers.add(ip);
                    PreferenceManager.getDefaultSharedPreferences(this).edit().putStringSet("servers", servers).apply();
                }
            })
            .setNegativeButton("Отмена", null)
            .show();
    }

    private void showRemoveServerDialog(java.util.Set<String> servers) {
        String[] serverArray = servers.toArray(new String[0]);
        new android.app.AlertDialog.Builder(this)
            .setTitle("Удалить сервер")
            .setItems(serverArray, (dialog, which) -> {
                servers.remove(serverArray[which]);
                PreferenceManager.getDefaultSharedPreferences(this).edit().putStringSet("servers", servers).apply();
            })
            .setNegativeButton("Отмена", null)
            .show();
    }

    private void showVideoSettingsDialog() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        float[] speedSteps = {0.25f, 0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f};
        String[] speedLabels = {"0.25x", "0.5x", "0.75x", "1x", "1.25x", "1.5x", "1.75x", "2x"};
        float currentSpeed = prefs.getFloat("default_speed", 1.0f);
        int checked = 3; // по умолчанию 1x
        for (int i = 0; i < speedSteps.length; i++) {
            if (Math.abs(speedSteps[i] - currentSpeed) < 0.01f) {
                checked = i;
                break;
            }
        }
        new android.app.AlertDialog.Builder(this)
            .setTitle("Стандартная скорость воспроизведения")
            .setSingleChoiceItems(speedLabels, checked, (dialog, which) -> {
                prefs.edit().putFloat("default_speed", speedSteps[which]).apply();
                dialog.dismiss();
            })
            .setNegativeButton("Отмена", null)
            .show();
    }

    private void showDebugDialog() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Debug");
        builder.setMessage("version 1.0 by maybeyoou");
        builder.setPositiveButton("Переподключиться к серверам", (dialog, which) -> {
            loadCategories();
        });
        builder.setNegativeButton("Закрыть", null);
        builder.show();
    }
} 