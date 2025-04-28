package mby;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.view.Gravity;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.ui.StyledPlayerView;
import android.app.AlertDialog;
import android.widget.ProgressBar;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class VideoPlayerActivity extends AppCompatActivity {
    private ExoPlayer player;
    private StyledPlayerView playerView;
    private ImageButton playPauseButton;
    private float currentSpeed = 1.0f;
    private boolean isPlaying = true;
    private final float[] speedSteps = {0.25f, 0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f};
    private int speedIndex = 3; // 1.0x по умолчанию
    private TextView speedTextView;
    private ProgressBar progressBar;
    private AlertDialog speedDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        String videoUrl = getIntent().getStringExtra("videoUrl");
        if (videoUrl == null) {
            finish();
            return;
        }

        // Получаем стандартную скорость из настроек
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        currentSpeed = prefs.getFloat("default_speed", 1.0f);
        for (int i = 0; i < speedSteps.length; i++) {
            if (Math.abs(speedSteps[i] - currentSpeed) < 0.01f) {
                speedIndex = i;
                break;
            }
        }

        // Инициализация ExoPlayer
        player = new ExoPlayer.Builder(this).build();
        playerView = findViewById(R.id.player_view);
        playerView.setPlayer(player);

        // Создание медиа-элемента
        MediaItem mediaItem = MediaItem.fromUri(videoUrl);
        player.setMediaItem(mediaItem);
        player.prepare();
        player.setPlaybackParameters(new PlaybackParameters(currentSpeed));
        player.play();

        // Инициализация кнопок управления
        playPauseButton = findViewById(R.id.play_pause_button);

        playPauseButton.setOnClickListener(v -> togglePlayPause());

        // Обработка нажатия кнопки "назад"
        playerView.setControllerVisibilityListener(
            new StyledPlayerView.ControllerVisibilityListener() {
                @Override
                public void onVisibilityChanged(int visibility) {
                    if (visibility == View.VISIBLE || speedDialog != null) {
                        playPauseButton.setVisibility(View.VISIBLE);
                    } else {
                        playPauseButton.setVisibility(View.GONE);
                    }
                }
            }
        );

        // Навешиваю обработчик на иконку настроек ExoPlayer
        View settingsButton = playerView.findViewById(com.google.android.exoplayer2.ui.R.id.exo_settings);
        if (settingsButton != null) {
            settingsButton.setOnClickListener(v -> showSpeedMenu());
        }

        speedTextView = new TextView(this);
        speedTextView.setTextColor(0xFFFFFFFF);
        speedTextView.setTextSize(18);
        speedTextView.setBackgroundColor(0x80000000);
        speedTextView.setPadding(24, 8, 24, 8);
        speedTextView.setVisibility(View.GONE);
        FrameLayout rootLayout = (FrameLayout) findViewById(android.R.id.content);
        LayoutParams params = new LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT
        );
        params.gravity = Gravity.TOP | Gravity.END;
        params.topMargin = 32;
        params.rightMargin = 32;
        rootLayout.addView(speedTextView, params);

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
    }

    private void togglePlayPause() {
        if (isPlaying) {
            player.pause();
            playPauseButton.setImageResource(R.drawable.ic_play);
        } else {
            player.play();
            playPauseButton.setImageResource(R.drawable.ic_pause);
        }
        isPlaying = !isPlaying;
    }

    private void showSpeedMenu() {
        String[] speedLabels = {"0.25x", "0.5x", "0.75x", "1x", "1.25x", "1.5x", "1.75x", "2x"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Скорость воспроизведения");
        builder.setSingleChoiceItems(speedLabels, speedIndex, (dialog, which) -> {
            speedIndex = which;
            currentSpeed = speedSteps[speedIndex];
            player.setPlaybackParameters(new PlaybackParameters(currentSpeed));
            showSpeed();
            dialog.dismiss();
            speedDialog = null;
        });
        builder.setNegativeButton("Отмена", (dialog, which) -> {
            dialog.dismiss();
            speedDialog = null;
        });
        speedDialog = builder.create();
        speedDialog.show();
    }

    private void showSpeed() {
        speedTextView.setText(String.format("x%.2f", currentSpeed));
        speedTextView.setVisibility(View.VISIBLE);
        speedTextView.removeCallbacks(hideSpeedRunnable);
        speedTextView.postDelayed(hideSpeedRunnable, 1200);
    }

    private final Runnable hideSpeedRunnable = new Runnable() {
        @Override
        public void run() {
            speedTextView.setVisibility(View.GONE);
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.release();
        }
    }
} 