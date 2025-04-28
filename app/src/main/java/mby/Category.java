package mby;

import java.util.List;

public class Category {
    private String id;
    private String title;
    private List<Movie> items;

    public Category(String id, String title, List<Movie> items) {
        this.id = id;
        this.title = title;
        this.items = items;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public List<Movie> getItems() {
        return items;
    }
} 