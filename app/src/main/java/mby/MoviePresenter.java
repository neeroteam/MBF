package mby;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.leanback.widget.Presenter;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.load.engine.GlideException;
import android.util.Log;
import androidx.annotation.Nullable;
import com.bumptech.glide.load.DataSource;
import android.graphics.drawable.Drawable;

public class MoviePresenter extends Presenter {
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_movie, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, Object item) {
        Movie movie = (Movie) item;
        View view = viewHolder.view;
        
        TextView titleView = view.findViewById(R.id.movie_title);
        ImageView posterView = view.findViewById(R.id.movie_poster);
        
        titleView.setText(movie.getTitle());
        posterView.setBackgroundColor(0xFFFF0000);
        Glide.with(view.getContext())
            .load(movie.getPosterUrl())
            .centerCrop()
            .listener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    Log.e("GlideError", "Ошибка загрузки постера: " + model, e);
                    return false;
                }
                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    return false;
                }
            })
            .into(posterView);
    }

    @Override
    public void onUnbindViewHolder(ViewHolder viewHolder) {
        // Очистка ресурсов при необходимости
    }
} 