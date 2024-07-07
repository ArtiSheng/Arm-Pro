/*
 * Copyright (c) 2020. Armadillo
 */

package armadillo.studio.activity.soft.Single;

import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.collect.Lists;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import armadillo.studio.R;
import armadillo.studio.adapter.TrialInfoAdapter;
import armadillo.studio.common.base.BaseActivity;
import armadillo.studio.common.base.callback.SocketCallBack;
import armadillo.studio.helper.SocketHelper;
import armadillo.studio.model.Basic;
import armadillo.studio.model.soft.SoftSingleTrialInfo;
import armadillo.studio.model.soft.UserSoft;
import butterknife.BindView;
import butterknife.OnClick;

public class SingleTrialManage extends BaseActivity<UserSoft.data> {
    @BindView(R.id.refresh)
    SwipeRefreshLayout refresh;
    @BindView(R.id.delete)
    FloatingActionButton deleteAll;
    @BindView(R.id.bottom_app_bar)
    BottomAppBar bottomAppBar;
    @BindView(R.id.recycler)
    RecyclerView recycler;
    private List<SoftSingleTrialInfo.data> trialInfos = new ArrayList<>();
    private TrialInfoAdapter adapter;
    private int offset = 0;
    private int limit = 10;

    @Override
    protected int BindXML() {
        return R.layout.activity_single_trial;
    }

    @OnClick(R.id.delete)
    public void OnClick(View view) {
        if (trialInfos.size() == 0)
            Toast.makeText(this, "Data Null", Toast.LENGTH_LONG).show();
        else
            new AlertDialog.Builder(this)
                    .setTitle(R.string.dialog_tips)
                    .setMessage(R.string.dialog_delete_single_trial_all)
                    .setPositiveButton(R.string.cancel, null)
                    .setNegativeButton(R.string.ok, (dialogInterface, i) -> {
                        Loading();
                        SocketHelper.UserHelper.DeleteSoftSingleTrials(new SocketCallBack<Basic>() {
                            @Override
                            public void next(Basic body) {
                                HideLoading();
                                Toast.makeText(SingleTrialManage.this, body.getMsg(), Toast.LENGTH_LONG).show();
                                if (body.getCode() == 200) {
                                    trialInfos.clear();
                                    adapter.notifyDataSetChanged();
                                }
                            }

                            @Override
                            public void error(Throwable throwable) {
                                HideLoading();
                                Toast.makeText(SingleTrialManage.this, String.format(getString(R.string.exception), throwable.getMessage()), Toast.LENGTH_LONG).show();
                            }
                        }, data.getAppkey(), trialInfos);
                    })
                    .show();
    }

    @Override
    protected boolean AutoLoadData() {
        return true;
    }

    @Override
    public void BindData(@NotNull UserSoft.data data) {
        Objects.requireNonNull(getSupportActionBar()).setTitle(data.getName());
        recycler.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        adapter = new TrialInfoAdapter(R.layout.item_trial_info, trialInfos);
        recycler.setAdapter(adapter);
        recycler.setHasFixedSize(true);
        adapter.setAnimationEnable(true);
        adapter.setAnimationFirstOnly(false);
        adapter.setAnimationWithDefault(BaseQuickAdapter.AnimationType.AlphaIn);
        adapter.setEmptyView(R.layout.status_empty);
        adapter.setFooterView(LayoutInflater.from(this).inflate(R.layout.status_footer, null));
        adapter.addChildLongClickViewIds(R.id.cardview);
        refresh.setOnRefreshListener(() -> {
            offset = 0;
            limit = 10;
            SocketHelper.UserHelper.GetSoftSingleTrial(new SocketCallBack<SoftSingleTrialInfo>() {
                @Override
                public void next(SoftSingleTrialInfo body) {
                    refresh.setRefreshing(false);
                    if (body.getCode() != 200) {
                        Toast.makeText(SingleTrialManage.this, body.getMsg(), Toast.LENGTH_LONG).show();
                    } else {
                        trialInfos.clear();
                        trialInfos.addAll(body.getData());
                        adapter.notifyDataSetChanged();
                        adapter.getLoadMoreModule().setEnableLoadMore(true);
                        adapter.getLoadMoreModule().loadMoreToLoading();
                    }
                }

                @Override
                public void error(Throwable throwable) {
                    refresh.setRefreshing(false);
                    Toast.makeText(SingleTrialManage.this, String.format(getString(R.string.exception), throwable.getMessage()), Toast.LENGTH_LONG).show();
                }
            }, data.getAppkey(), offset, limit);
        });
        adapter.getLoadMoreModule().setOnLoadMoreListener(() -> {
            if (refresh.isRefreshing()) return;
            offset += limit;
            Log.e(TAG, "index " + offset);
            SocketHelper.UserHelper.GetSoftSingleCard(new SocketCallBack<SoftSingleTrialInfo>() {

                @Override
                public void next(SoftSingleTrialInfo body) {
                    if (body.getData().size() == 0) {
                        adapter.getLoadMoreModule().loadMoreEnd();
                        adapter.getLoadMoreModule().setEnableLoadMore(false);
                        adapter.setFooterView(LayoutInflater.from(SingleTrialManage.this).inflate(R.layout.status_footer, null));
                        return;
                    }
                    trialInfos.addAll(body.getData());
                    adapter.notifyItemRangeInserted(trialInfos.size() - body.getData().size(), body.getData().size());
                    adapter.getLoadMoreModule().loadMoreComplete();
                }

                @Override
                public void error(Throwable throwable) {
                    Toast.makeText(SingleTrialManage.this, String.format(getString(R.string.exception), throwable.getMessage()), Toast.LENGTH_LONG).show();
                    adapter.getLoadMoreModule().loadMoreComplete();
                }
            }, data.getAppkey(), offset, limit);
        });
        adapter.setOnItemChildLongClickListener((adapter, view, position) -> {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.dialog_editor) + trialInfos.get(position).getMac())
                    .setSingleChoiceItems(new String[]{getString(R.string.dialog_delete), getString(R.string.dialog_editor_trial_count)}, -1, (dialogInterface, i) -> {
                        dialogInterface.dismiss();
                        switch (i) {
                            /**
                             * 删除
                             */
                            case 0: {
                                Loading();
                                SocketHelper.UserHelper.DeleteSoftSingleTrials(new SocketCallBack<Basic>() {
                                    @Override
                                    public void next(Basic body) {
                                        HideLoading();
                                        Toast.makeText(SingleTrialManage.this, body.getMsg(), Toast.LENGTH_LONG).show();
                                        if (body.getCode() == 200) {
                                            adapter.notifyItemRemoved(position);
                                            trialInfos.remove(position);
                                        }
                                    }

                                    @Override
                                    public void error(Throwable throwable) {
                                        HideLoading();
                                        Toast.makeText(SingleTrialManage.this, String.format(getString(R.string.exception), throwable.getMessage()), Toast.LENGTH_LONG).show();
                                    }
                                }, data.getAppkey(), Lists.newArrayList(trialInfos.get(position)));
                            }
                            break;
                            /**
                             * 修改试用次数
                             */
                            case 1: {
                                EditText count_text = new EditText(SingleTrialManage.this);
                                count_text.setInputType(InputType.TYPE_CLASS_NUMBER);
                                count_text.setHint(R.string.single_item_trial_count);
                                new AlertDialog.Builder(this)
                                        .setTitle(getString(R.string.dialog_editor) + trialInfos.get(position).getMac())
                                        .setView(count_text)
                                        .setNegativeButton(R.string.ok, (dialogInterface1, i1) -> {
                                            if (count_text.getText().toString().isEmpty())
                                                return;
                                            SoftSingleTrialInfo.data trial = trialInfos.get(position);
                                            trial.setCount(Integer.parseInt(count_text.getText().toString()));
                                            Loading();
                                            SocketHelper.UserHelper.UpdateSoftSingleTrials(new SocketCallBack<Basic>() {
                                                @Override
                                                public void next(Basic body) {
                                                    HideLoading();
                                                    Toast.makeText(SingleTrialManage.this, body.getMsg(), Toast.LENGTH_LONG).show();
                                                    if (body.getCode() == 200)
                                                        adapter.notifyItemChanged(position);
                                                }

                                                @Override
                                                public void error(Throwable throwable) {
                                                    HideLoading();
                                                    Toast.makeText(SingleTrialManage.this, String.format(getString(R.string.exception), throwable.getMessage()), Toast.LENGTH_LONG).show();
                                                }
                                            }, data.getAppkey(), Lists.newArrayList(trial));
                                        })
                                        .setPositiveButton(R.string.cancel, null)
                                        .show();
                            }
                            break;
                        }
                    })
                    .setPositiveButton(R.string.cancel, null)
                    .show();
            return true;
        });
        bottomAppBar.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                /**
                 * 过滤
                 */
                case R.id.menu_sort: {
                    Toast.makeText(this, R.string.wait_handle, Toast.LENGTH_LONG).show();
                }
                break;
            }
            return true;
        });
        Loading();
        SocketHelper.UserHelper.GetSoftSingleTrial(new SocketCallBack<SoftSingleTrialInfo>() {
            @Override
            public void next(SoftSingleTrialInfo body) {
                HideLoading();
                if (body.getCode() != 200) {
                    Toast.makeText(SingleTrialManage.this, body.getMsg(), Toast.LENGTH_LONG).show();
                } else {
                    trialInfos.clear();
                    trialInfos.addAll(body.getData());
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void error(Throwable throwable) {
                HideLoading();
                Toast.makeText(SingleTrialManage.this, String.format(getString(R.string.exception), throwable.getMessage()), Toast.LENGTH_LONG).show();
                SingleTrialManage.this.finish();
            }
        }, data.getAppkey(), offset, limit);
    }

    @Override
    public void onError(@NonNull Throwable throwable) {
        Toast.makeText(this, String.format(getString(R.string.exception), throwable.getMessage()), Toast.LENGTH_LONG).show();
        finish();
    }
}
