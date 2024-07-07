package armadillo.studio.adapter;

import android.text.TextUtils;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import armadillo.studio.CloudApp;
import armadillo.studio.R;
import armadillo.studio.model.apk.PackageInfos;

public class SoftAdapter extends BaseQuickAdapter<PackageInfos, BaseViewHolder> implements Filterable {
    private SearchSoftFilter searchSoftFilter;
    private List<PackageInfos> searchList = new ArrayList<>();
    private List<PackageInfos> srcList = new ArrayList<>();

    public SoftAdapter(int layoutResId, @Nullable List<PackageInfos> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, @NotNull PackageInfos packageInfos) {
        Glide.with(CloudApp.getContext())
                .load(packageInfos.getIco())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .placeholder(R.drawable.file_file)
                .into((ImageView) baseViewHolder.getView(R.id.avatar));
        baseViewHolder.setText(R.id.name, packageInfos.getName())
                .setText(R.id.ver, packageInfos.getPackageInfo().versionName)
                .setText(R.id.ver_int, Integer.valueOf(packageInfos.getPackageInfo().versionCode).toString())
                .setText(R.id.size, packageInfos.getSize())
                .setText(R.id.packagename, packageInfos.getPackageInfo().packageName)
                .setText(R.id.jiagu, packageInfos.getJiagu());
    }

    @Override
    public Filter getFilter() {
        if (searchSoftFilter == null)
            searchSoftFilter = new SearchSoftFilter();
        return searchSoftFilter;
    }

    private class SearchSoftFilter extends Filter {
        private final String TAG = SearchSoftFilter.class.getSimpleName();

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            if (srcList.size() == 0)
                srcList.addAll(getData());
            FilterResults results = new FilterResults();
            searchList.clear();
            if (!TextUtils.isEmpty(constraint)) {
                for (PackageInfos infos : srcList) {
                    if (infos.getName().toLowerCase().contains(constraint.toString().toLowerCase())
                            || infos.getPackageInfo().versionName.toLowerCase().contains(constraint.toString().toLowerCase())
                            || infos.getPackageInfo().packageName.toLowerCase().contains(constraint.toString().toLowerCase()))
                        searchList.add(infos);
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
                List<PackageInfos> new_data = (List<PackageInfos>) results.values;
                setList(new_data);
            }
        }
    }
}
