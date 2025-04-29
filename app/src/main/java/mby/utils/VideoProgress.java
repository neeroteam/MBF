package mby.utils;

public class VideoProgress {
    private String videoId;
    private long lastPosition; // последняя позиция просмотра в миллисекундах
    private long totalDuration; // общая длительность видео
    private boolean isCompleted; // просмотрено ли видео полностью

    public VideoProgress(String videoId, long lastPosition, long totalDuration) {
        this.videoId = videoId;
        this.lastPosition = lastPosition;
        this.totalDuration = totalDuration;
        this.isCompleted = lastPosition >= totalDuration * 0.9; // считаем просмотренным, если посмотрели 90%
    }

    public String getVideoId() {
        return videoId;
    }

    public long getLastPosition() {
        return lastPosition;
    }

    public long getTotalDuration() {
        return totalDuration;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void updateProgress(long position, long duration) {
        this.lastPosition = position;
        this.totalDuration = duration;
        this.isCompleted = position >= duration * 0.9;
    }
} 