/*
 * Copyright (c) 2020. Armadillo
 */

package armadillo.studio.adapter;

import android.annotation.SuppressLint;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Filter;
import android.widget.Filterable;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.module.LoadMoreModule;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import armadillo.studio.R;
import armadillo.studio.common.enums.SingleCardTypeEnums;
import armadillo.studio.model.soft.SoftSingleCardInfo;

public class CardInfoAdapter extends BaseQuickAdapter<SoftSingleCardInfo.data, BaseViewHolder> implements LoadMoreModule, Filterable {
    private SeachCardFilter seachCardFilter;
    private List<SoftSingleCardInfo.data> searchList = new ArrayList<>();
    private List<SoftSingleCardInfo.data> srcList = new ArrayList<>();

    public CardInfoAdapter(int layoutResId, @Nullable List<SoftSingleCardInfo.data> data) {
        super(layoutResId, data);
    }

    @SuppressLint("SimpleDateFormat")
    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, @NotNull SoftSingleCardInfo.data data) {
        baseViewHolder.setText(R.id.card, data.getCard())
                .setText(R.id.type, SingleCardTypeEnums.getFlags(data.getType()))
                .setTextColorRes(R.id.card, data.getUsrCount() == 0 ? R.color.colorAccent : R.color.red)
                .setText(R.id.value, data.getValue().toString())
                .setText(R.id.mark, data.getMark())
                .setText(R.id.usable, data.getUsable() ? getContext().getString(R.string.single_card_not_frozen) : getContext().getString(R.string.single_card_frozen))
                .setTextColorRes(R.id.usable, data.getUsable() ? R.color.colorAccent : R.color.red)
                .setGone(R.id.usable_view, data.getUsrCount() == 0);
        if (data.getUsrCount() > 0)
            baseViewHolder.setText(R.id.mac, data.getMac())
                    .setText(R.id.usrCount, data.getUsrCount().toString())
                    .setText(R.id.usrTime, data.getUsrTime());
    }

    @Override
    public Filter getFilter() {
        if (seachCardFilter == null)
            seachCardFilter = new SeachCardFilter();
        return seachCardFilter;
    }

    private class SeachCardFilter extends Filter {
        private final String TAG = SeachCardFilter.class.getSimpleName();

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            if (srcList.size() == 0)
                srcList.addAll(getData());
            FilterResults results = new FilterResults();
            searchList.clear();
            if (!TextUtils.isEmpty(constraint)) {
                Pattern status_pattern = Pattern.compile("status:[0-1]{1}|状态:[0-1]{1}");
                Matcher status_matcher = status_pattern.matcher(constraint);
                Pattern type_pattern = Pattern.compile("type:[1-6]{1}|类型:[1-6]{1}");
                Matcher type_matcher = type_pattern.matcher(constraint);
                /**
                 * 状态搜索
                 */
                if (status_matcher.find()) {
                    String[] data = status_matcher.group(0).split(":");
                    Log.e(TAG, "搜索状态:" + (data[1].equals("1") ? "true" : "false"));
                    boolean status = data[1].equals("1");
                    for (SoftSingleCardInfo.data info : srcList) {
                        if (info.getUsable() == status)
                            searchList.add(info);
                    }
                }
                /**
                 * 卡类搜索
                 */
                else if (type_matcher.find()) {
                    String[] data = type_matcher.group(0).split(":");
                    Log.e(TAG, "搜索类型:" + Integer.parseInt(data[1]));
                    for (SoftSingleCardInfo.data info : srcList) {
                        if (info.getType() == Integer.parseInt(data[1]))
                            searchList.add(info);
                    }
                }
                /**
                 * 模糊搜索
                 */
                else {
                    Log.e(TAG, "模糊搜索");
                    for (SoftSingleCardInfo.data info : srcList) {
                        if (info.getCard().toLowerCase().contains(constraint.toString().toLowerCase())
                                || info.getMark().toLowerCase().contains(constraint.toString().toLowerCase())
                                || info.getValue().toString().contains(constraint.toString()))
                            searchList.add(info);
                    }
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
                List<SoftSingleCardInfo.data> new_data = (List<SoftSingleCardInfo.data>) results.values;
                setList(new_data);
            }
        }
    }

    public void setSrcList(List<SoftSingleCardInfo.data> newList) {
        srcList.clear();
        srcList.addAll(newList);
    }
}
