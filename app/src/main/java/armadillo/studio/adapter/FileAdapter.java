package armadillo.studio.adapter;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import armadillo.studio.CloudApp;
import armadillo.studio.R;
import armadillo.studio.common.utils.AppUtils;
import armadillo.studio.common.utils.FileSize;
import armadillo.studio.common.utils.GlideRoundTransform;

public class FileAdapter extends BaseQuickAdapter<File, BaseViewHolder> implements Filterable {
    private static final String TAG = FileAdapter.class.getSimpleName();
    private SearchFileFilter searchFileFilter;
    private List<File> searchList = new ArrayList<>();
    private List<File> srcList = new ArrayList<>();

    public FileAdapter(int layoutResId, @Nullable List<File> data) {
        super(layoutResId, data);
    }

    @SuppressLint("SimpleDateFormat")
    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, @NotNull File file) {
        if (file.isDirectory())
            Glide.with(CloudApp.getContext())
                    .load(R.drawable.file_dir)
                    .circleCrop()
                    .into((ImageView) baseViewHolder.getView(R.id.avatar));
        else if (file.getName().toLowerCase().endsWith(".apk")) {
            CloudApp.getCachedThreadPool().execute(() -> {
                Glide.with(CloudApp.getContext())
                        .load(AppUtils.getApkDrawable(file.getAbsolutePath()))
                        .into(new CustomTarget<Drawable>() {
                            @Override
                            public void onResourceReady(@NonNull Drawable resource, @androidx.annotation.Nullable Transition<? super Drawable> transition) {
                                Glide.with(CloudApp.getContext())
                                        .load(resource)
                                        .transform(new CenterCrop(), new GlideRoundTransform())
                                        .placeholder(R.mipmap.ic_launcher)
                                        .into((ImageView) baseViewHolder.getView(R.id.avatar));
                            }

                            @Override
                            public void onLoadCleared(@androidx.annotation.Nullable Drawable placeholder) {
                                Glide.with(CloudApp.getContext())
                                        .load(placeholder)
                                        .transform(new CenterCrop(), new GlideRoundTransform())
                                        .placeholder(R.mipmap.ic_launcher)
                                        .into((ImageView) baseViewHolder.getView(R.id.avatar));
                            }
                        });
            });
        } else if (file.getName().toLowerCase().endsWith(".mp4")
                || file.getName().toLowerCase().endsWith(".mov")
                || file.getName().toLowerCase().endsWith(".rmvb")
                || file.getName().toLowerCase().endsWith(".ts")
                || file.getName().toLowerCase().endsWith(".wmv")
                || file.getName().toLowerCase().endsWith(".3gp")
                || file.getName().toLowerCase().endsWith(".avi")
                || file.getName().toLowerCase().endsWith(".flv")
                || file.getName().toLowerCase().endsWith(".mkv")
                || file.getName().toLowerCase().endsWith(".swf")
                || file.getName().toLowerCase().endsWith(".vob"))
            Glide.with(CloudApp.getContext())
                    .load(R.drawable.file_video)
                    .transform(new CenterCrop(), new GlideRoundTransform())
                    .into((ImageView) baseViewHolder.getView(R.id.avatar));
        else
            Glide.with(CloudApp.getContext())
                    .load(R.drawable.file_file)
                    .transform(new CenterCrop(), new GlideRoundTransform())
                    .into((ImageView) baseViewHolder.getView(R.id.avatar));
        baseViewHolder.setText(R.id.name, file.getName())
                .setText(R.id.time, new SimpleDateFormat("MM-dd hh:mm:ss").format(new Date(file.lastModified())) + "  " + FileSize.getAutoFileOrFileSize(file));
    }

    @Override
    public Filter getFilter() {
        if (searchFileFilter == null)
            searchFileFilter = new SearchFileFilter();
        return searchFileFilter;
    }

    private class SearchFileFilter extends Filter {
        private final String TAG = SearchFileFilter.class.getSimpleName();

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            if (srcList.size() == 0)
                srcList.addAll(getData());
            FilterResults results = new FilterResults();
            searchList.clear();
            if (!TextUtils.isEmpty(constraint)) {
                for (File file : srcList) {
                    if (file.getName().toLowerCase().contains(constraint.toString().toLowerCase()))
                        searchList.add(file);
                }
                results.values = searchList;
                results.count = searchList.size();
            } else {
                results.values = srcList;
                results.count = srcList.size();
            }
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            if (results != null) {
                List<File> new_data = (List<File>) results.values;
                setList(new_data);
            }
        }
    }

    public void setSrcList(List<File> newList) {
        srcList.clear();
        srcList.addAll(newList);
    }
}
