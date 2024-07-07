/*
 * Copyright (c) 2020. Armadillo
 */

package armadillo.studio.ui.software;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.chad.library.adapter.base.BaseQuickAdapter;

import java.util.ArrayList;
import java.util.List;

import armadillo.studio.R;
import armadillo.studio.activity.soft.SoftInfo;
import armadillo.studio.adapter.SoftwareAdapter;
import armadillo.studio.common.base.BaseFragment;
import armadillo.studio.common.base.callback.SocketCallBack;
import armadillo.studio.helper.SocketHelper;
import armadillo.studio.model.Basic;
import armadillo.studio.widget.LoadingDialog;
import butterknife.BindView;

public class SoftwareFragment extends BaseFragment<SoftwareViewModel> implements SwipeRefreshLayout.OnRefreshListener {
    @BindView(R.id.refresh)
    SwipeRefreshLayout refresh;
    @BindView(R.id.recycler)
    RecyclerView recycler;
    private final int requestCode = 1000;
    private final List<SoftwareLiveDate> dataList = new ArrayList<>();
    private SoftwareAdapter softwareAdapter;
    @Override
    protected Class<SoftwareViewModel> BindViewModel() {
        return SoftwareViewModel.class;
    }

    /**
     * 绑定布局
     *
     * @return
     */
    @Override
    protected int BindXML() {
        return R.layout.fragment_software;
    }

    /**
     * 绑定View
     */
    @SuppressLint("InflateParams")
    @Override
    protected void BindData() {
        refresh.setOnRefreshListener(this);
        softwareAdapter = new SoftwareAdapter(R.layout.item_soft, dataList);
        recycler.setLayoutManager(new LinearLayoutManager(requireActivity()));
        recycler.setAdapter(softwareAdapter);
        recycler.setHasFixedSize(true);
        softwareAdapter.setAnimationEnable(true);
        softwareAdapter.setAnimationFirstOnly(false);
        softwareAdapter.setAnimationWithDefault(BaseQuickAdapter.AnimationType.AlphaIn);
        softwareAdapter.setEmptyView(R.layout.status_empty);
        softwareAdapter.setFooterView(LayoutInflater.from(requireActivity()).inflate(R.layout.status_footer, null));
        softwareAdapter.addChildLongClickViewIds(R.id.cardview);
        softwareAdapter.addChildClickViewIds(R.id.cardview);
        softwareAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            if (view.getId() != R.id.cardview) return;
            Intent intent = new Intent(requireActivity(), SoftInfo.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("data", dataList.get(position).getData());
            intent.putExtras(bundle);
            startActivityForResult(intent, requestCode);
        });
        softwareAdapter.setOnItemChildLongClickListener((adapter, view, position) -> {
            if (view.getId() != R.id.cardview) return true;
            new AlertDialog.Builder(requireActivity())
                    .setTitle(R.string.dialog_tips)
                    .setMessage(R.string.dialog_delete_soft)
                    .setPositiveButton(R.string.cancel, null)
                    .setNegativeButton(R.string.ok, (dialogInterface, i) -> {
                        LoadingDialog.getInstance().show(requireActivity());
                        SocketHelper.UserHelper.DeleteSoft(new SocketCallBack<Basic>() {
                            @Override
                            public void next(Basic body) {
                                LoadingDialog.getInstance().hide();
                                Toast.makeText(requireActivity(), body.getMsg(), Toast.LENGTH_LONG).show();
                                if (body.getCode() == 200) {
                                    softwareAdapter.notifyItemRemoved(position);
                                    dataList.remove(position);
                                }
                            }

                            @Override
                            public void error(Throwable throwable) {
                                LoadingDialog.getInstance().hide();
                                Toast.makeText(requireActivity(), String.format(getString(R.string.exception), throwable.getMessage()), Toast.LENGTH_LONG).show();
                            }
                        }, dataList.get(position).getData().getAppkey());
                    })
                    .show();
            return true;
        });
        InitData();
    }

    /**
     * 初始化数据
     */
    @SuppressLint("InflateParams")
    private void InitData() {
        viewModel.getData(requireActivity(), softwareLiveDates -> {
            dataList.addAll(softwareLiveDates);
            softwareAdapter.notifyDataSetChanged();
        });
        softwareAdapter.getLoadMoreModule().setOnLoadMoreListener(() -> {
            if (refresh.isRefreshing()) return;
            viewModel.offset = viewModel.offset + viewModel.limit;
            viewModel.getData(softwareLiveDates -> {
                if (softwareLiveDates.size() == 0) {
                    softwareAdapter.getLoadMoreModule().loadMoreEnd();
                    softwareAdapter.getLoadMoreModule().setEnableLoadMore(false);
                    softwareAdapter.setFooterView(LayoutInflater.from(requireActivity()).inflate(R.layout.status_footer, null));
                    return;
                }
                dataList.addAll(softwareLiveDates);
                softwareAdapter.notifyItemRangeInserted(dataList.size() - softwareLiveDates.size(), softwareLiveDates.size());
                softwareAdapter.getLoadMoreModule().loadMoreComplete();
            });
        });
    }

    /**
     * 刷新数据
     */
    @Override
    public void onRefresh() {
        dataList.clear();
        viewModel.offset = 0;
        viewModel.getData(softwareLiveDates -> {
            dataList.addAll(softwareLiveDates);
            softwareAdapter.notifyDataSetChanged();
            refresh.setRefreshing(false);
            softwareAdapter.getLoadMoreModule().setEnableLoadMore(true);
            softwareAdapter.removeAllFooterView();
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == requestCode && resultCode == 200)
            onRefresh();
    }
}
