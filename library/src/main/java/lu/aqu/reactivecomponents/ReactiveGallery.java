package lu.aqu.reactivecomponents;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PagerSnapHelper;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.List;

public class ReactiveGallery extends RelativeLayout {

    private Adapter mImageAdapter;
    private Indicators mIndicators;
    private ImageLoader mImageLoader;

    public ReactiveGallery(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.getTheme().obtainStyledAttributes(attrs, R.styleable.ReactiveGallery, 0, 0);

        Drawable indicatorActive = null;
        Drawable indicatorInactive = null;
        final int indicatorSpacing;
        try {
            if (a.hasValue(R.styleable.ReactiveGallery_indicatorActive)) {
                indicatorActive = a.getDrawable(R.styleable.ReactiveGallery_indicatorActive);
            }

            if (a.hasValue(R.styleable.ReactiveGallery_indicatorInactive)) {
                indicatorInactive = a.getDrawable(R.styleable.ReactiveGallery_indicatorInactive);
            }

            indicatorSpacing = a.getDimensionPixelSize(R.styleable.ReactiveGallery_indicatorSpacing,
                    getResources().getDimensionPixelSize(R.dimen.indicator_spacing));
        } finally {
            a.recycle();
        }

        setGravity(CENTER_HORIZONTAL);

        setupRecyclerView();
        setupIndicators(indicatorActive, indicatorInactive, indicatorSpacing);
    }

    private void setupIndicators(@Nullable Drawable indicatorActive, @Nullable Drawable indicatorInactive,
                                 int indicatorSpacing) {
        mIndicators = new Indicators(getContext(), indicatorActive, indicatorInactive, indicatorSpacing);

        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(ALIGN_PARENT_BOTTOM);

        int marginBottom = getResources().getDimensionPixelSize(R.dimen.indicator_margin_bottom);
        layoutParams.setMargins(0, 0, 0, marginBottom);

        addView(mIndicators, layoutParams);
    }

    private void setupRecyclerView() {
        mImageAdapter = new Adapter();

        RecyclerView gallery = new RecyclerView(getContext());
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(),
                LinearLayoutManager.HORIZONTAL, false);
        gallery.setLayoutManager(layoutManager);
        gallery.setAdapter(mImageAdapter);

        gallery.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                mIndicators.setPage(layoutManager.findLastVisibleItemPosition());
            }
        });

        PagerSnapHelper pagerSnapHelper = new PagerSnapHelper();
        pagerSnapHelper.attachToRecyclerView(gallery);

        addView(gallery, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
    }

    /**
     * sets the class that receives a callback, whenever an image URL should be loaded
     *
     * @param imageLoader ImageLoader implementation to load an image
     */
    public void setImageLoader(ImageLoader imageLoader) {
        mImageLoader = imageLoader;
    }

    /**
     * adds an image url to the gallery
     *
     * @param url
     */
    public void add(String url) {
        if (url != null) {
            mImageAdapter.add(url);
            mIndicators.addDot();
        }
    }

    /**
     * adds image urls to the gallery
     *
     * @param urls
     */
    public void add(List<String> urls) {
        if (urls != null) {
            mImageAdapter.addAll(urls);
            mIndicators.addDots(urls.size());
        }
    }

    private class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

        private final List<String> urls = new ArrayList<>();

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = (LayoutInflater) parent.getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View view = inflater.inflate(R.layout.list_item_reactive_gallery, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.progressBar.setVisibility(VISIBLE);
            holder.imageView.setVisibility(GONE);

            if (mImageLoader != null) {
                mImageLoader.loadImage(urls.get(position), new LoadingCallback() {
                    @Override
                    public void onLoadFinished(Drawable drawable) {
                        holder.progressBar.setVisibility(GONE);
                        holder.imageView.setVisibility(VISIBLE);
                        holder.imageView.setImageDrawable(drawable);
                    }
                });
            }
        }

        public void add(String url) {
            urls.add(url);
            notifyItemInserted(urls.size());
        }

        public void addAll(List<String> urls) {
            final int startPos = this.urls.size();
            this.urls.addAll(urls);
            notifyItemRangeInserted(startPos, urls.size());
        }

        @Override
        public int getItemCount() {
            return urls.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            private ImageView imageView;
            private ProgressBar progressBar;

            public ViewHolder(View itemView) {
                super(itemView);

                imageView = itemView.findViewById(R.id.image_view);
                progressBar = itemView.findViewById(R.id.progress_bar);
            }
        }
    }

    private static class Indicators extends LinearLayout {

        private final Drawable indicatorActive;
        private final Drawable indicatorInactive;
        private final int indicatorSpacing;
        private List<ImageView> dots = new ArrayList<>();

        public Indicators(Context context, @Nullable Drawable indicatorActive, @Nullable Drawable indicatorInactive, int indicatorSpacing) {
            super(context);

            this.indicatorActive = (indicatorActive != null)
                    ? indicatorActive
                    : loadDrawable(R.drawable.indicator_active);

            this.indicatorInactive = (indicatorInactive != null)
                    ? indicatorInactive
                    : loadDrawable(R.drawable.indicator_inactive);

            this.indicatorSpacing = indicatorSpacing;

            setOrientation(LinearLayout.HORIZONTAL);
            setGravity(Gravity.CENTER);
            setClickable(false);
        }

        @SuppressWarnings("deprecation")
        private Drawable loadDrawable(@DrawableRes int resId) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                return getContext().getDrawable(resId);
            } else {
                return getResources().getDrawable(resId);
            }
        }

        public void addDot() {
            ImageView dot = new ImageView(getContext());
            dot.setImageDrawable(indicatorInactive);

            LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT);
            params.setMargins(indicatorSpacing, 0, indicatorSpacing, 0);

            dots.add(dot);
            addView(dot, params);
        }

        public void addDots(int count) {
            for (int i = 0; i < count; i++) {
                addDot();
            }
        }

        public void setPage(int page) {
            for (int i = 0; i < dots.size(); i++) {
                dots.get(i).setImageDrawable((page == i) ? indicatorActive : indicatorInactive);
            }
        }
    }

    public interface ImageLoader {

        /**
         * @param url      the image URL which shall be loaded
         * @param callback to send the loaded drawable to the image view
         */
        void loadImage(String url, LoadingCallback callback);

    }

    /**
     * callback class passed to the imageloader for setting a drawable of an image view
     */
    public interface LoadingCallback {

        void onLoadFinished(Drawable drawable);

    }
}
