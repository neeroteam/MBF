package mby;

import java.util.Map;

public class Movie {
    private String title;
    private String videoUrl;
    private String posterUrl;
    private Map<String, String> videos;

    public Movie(String title, String videoUrl, String posterUrl, Map<String, String> videos) {
        this.title = title;
        this.videoUrl = videoUrl;
        this.posterUrl = posterUrl;
        this.videos = videos;
    }

    public String getTitle() {
        return title;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public String getPosterUrl() {
        return posterUrl;
    }

    public Map<String, String> getVideos() {
        return videos;
    }
} 