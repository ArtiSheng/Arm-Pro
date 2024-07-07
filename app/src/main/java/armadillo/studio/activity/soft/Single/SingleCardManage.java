/*
 * Copyright (c) 2020. Armadillo
 */

package armadillo.studio.activity.soft.Single;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.common.collect.Lists;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import armadillo.studio.R;
import armadillo.studio.adapter.CardInfoAdapter;
import armadillo.studio.common.base.BaseActivity;
import armadillo.studio.common.base.callback.SocketCallBack;
import armadillo.studio.common.enums.SingleCardTypeEnums;
import armadillo.studio.helper.SocketHelper;
import armadillo.studio.model.Basic;
import armadillo.studio.model.soft.SoftSingleCardInfo;
import armadillo.studio.model.soft.UserSoft;
import armadillo.studio.widget.RoundCornerDialog;
import butterknife.BindView;
import butterknife.OnClick;

public class SingleCardManage extends BaseActivity<UserSoft.data> {
    @BindView(R.id.refresh)
    SwipeRefreshLayout refresh;
    @BindView(R.id.add)
    FloatingActionButton add;
    @BindView(R.id.bottom_app_bar)
    BottomAppBar bottomAppBar;
    @BindView(R.id.search_view)
    MaterialSearchView searchView;
    @BindView(R.id.recycler)
    RecyclerView recycler;
    private CardInfoAdapter adapter;
    private int offset = 0;
    private int limit = 10;
    private List<SoftSingleCardInfo.data> cardInfos = new ArrayList<>();
    private volatile boolean searchflag = false;

    @Override
    protected int BindXML() {
        return R.layout.activity_single_card;
    }

    @OnClick(R.id.add)
    public void OnClick(View view) {
        RoundCornerDialog create_card = new RoundCornerDialog(this)
                .SetView(R.layout.dialog_create_single_card)
                .SetText(R.id.title, R.string.dialog_single_create_card)
                .AddChildClickViewIds(R.id.Next)
                .Show()
                .SetOnChildClickListener((view1, dialog) -> {
                    TextInputEditText durationView = dialog.getView().findViewById(R.id.duration);
                    TextInputEditText countView = dialog.getView().findViewById(R.id.count);
                    TextInputEditText markView = dialog.getView().findViewById(R.id.mark);
                    AppCompatSpinner type = dialog.getView().findViewById(R.id.type);
                    if (Objects.requireNonNull(durationView.getText()).toString().isEmpty()
                            || Objects.requireNonNull(countView.getText()).toString().isEmpty())
                        return;
                    int duration = Integer.parseInt(Objects.requireNonNull(durationView.getText()).toString());
                    int count = Integer.parseInt(Objects.requireNonNull(countView.getText()).toString());
                    String mark = Objects.requireNonNull(markView.getText()).toString().trim();
                    if (duration > 99)
                        Toast.makeText(SingleCardManage.this, R.string.single_duration_max, Toast.LENGTH_LONG).show();
                    else if (count > 99)
                        Toast.makeText(SingleCardManage.this, R.string.single_count_max, Toast.LENGTH_LONG).show();
                    else if (duration == 0 || count == 0)
                        Toast.makeText(SingleCardManage.this, R.string.single_min_fail, Toast.LENGTH_LONG).show();
                    else {
                        Loading();
                        SocketHelper.UserHelper.CreateSingleCard(new SocketCallBack<Basic>() {
                            @Override
                            public void next(Basic body) {
                                HideLoading();
                                Toast.makeText(SingleCardManage.this, body.getMsg(), Toast.LENGTH_LONG).show();
                                dialog.dismiss();
                            }

                            @Override
                            public void error(Throwable throwable) {
                                HideLoading();
                                Toast.makeText(SingleCardManage.this, String.format(getString(R.string.exception), throwable.getMessage()), Toast.LENGTH_LONG).show();
                            }
                        }, getData().getAppkey(), count, type.getSelectedItemPosition() + 1, duration, mark);
                    }
                });
        Objects.requireNonNull(create_card.getWindow()).clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
    }

    @Override
    protected boolean AutoLoadData() {
        return true;
    }

    @Override
    @SuppressLint({"InflateParams", "SimpleDateFormat"})
    public void BindData(@NotNull UserSoft.data data) {
        Objects.requireNonNull(getSupportActionBar()).setTitle(data.getName());
        recycler.setLayoutManager(new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        adapter = new CardInfoAdapter(R.layout.item_card_info, cardInfos);
        recycler.setAdapter(adapter);
        recycler.setHasFixedSize(true);
        adapter.setAnimationEnable(true);
        adapter.setAnimationFirstOnly(false);
        adapter.setAnimationWithDefault(BaseQuickAdapter.AnimationType.AlphaIn);
        adapter.setEmptyView(R.layout.status_empty);
        adapter.setFooterView(LayoutInflater.from(this).inflate(R.layout.status_footer, null));
        adapter.addChildLongClickViewIds(R.id.cardview);
        adapter.addChildClickViewIds(R.id.cardview);
        /**
         * 刷新卡密
         */
        refresh.setOnRefreshListener(() -> {
            offset = 0;
            limit = 10;
            SocketHelper.UserHelper.GetSoftSingleCard(new SocketCallBack<SoftSingleCardInfo>() {
                @Override
                public void next(SoftSingleCardInfo body) {
                    refresh.setRefreshing(false);
                    if (body.getCode() != 200) {
                        Toast.makeText(SingleCardManage.this, body.getMsg(), Toast.LENGTH_LONG).show();
                    } else {
                        cardInfos.clear();
                        cardInfos.addAll(body.getData());
                        adapter.notifyDataSetChanged();
                        adapter.getLoadMoreModule().setEnableLoadMore(true);
                        adapter.getLoadMoreModule().loadMoreToLoading();
                    }
                }

                @Override
                public void error(Throwable throwable) {
                    refresh.setRefreshing(false);
                    Toast.makeText(SingleCardManage.this, String.format(getString(R.string.exception), throwable.getMessage()), Toast.LENGTH_LONG).show();
                }
            }, data.getAppkey(), offset, limit);
        });
        /**
         * 加载更多
         */
        adapter.getLoadMoreModule().setOnLoadMoreListener(() -> {
            if (refresh.isRefreshing() || searchflag) return;
            offset += limit;
            Log.e(TAG, "index " + offset);
            SocketHelper.UserHelper.GetSoftSingleCard(new SocketCallBack<SoftSingleCardInfo>() {

                @Override
                public void next(SoftSingleCardInfo body) {
                    if (body.getData().size() == 0) {
                        adapter.getLoadMoreModule().loadMoreEnd();
                        adapter.getLoadMoreModule().setEnableLoadMore(false);
                        adapter.setFooterView(LayoutInflater.from(SingleCardManage.this).inflate(R.layout.status_footer, null));
                        return;
                    }
                    cardInfos.addAll(body.getData());
                    adapter.notifyItemRangeInserted(cardInfos.size() - body.getData().size(), body.getData().size());
                    adapter.getLoadMoreModule().loadMoreComplete();
                }

                @Override
                public void error(Throwable throwable) {
                    Toast.makeText(SingleCardManage.this, String.format(getString(R.string.exception), throwable.getMessage()), Toast.LENGTH_LONG).show();
                    adapter.getLoadMoreModule().loadMoreComplete();
                }
            }, data.getAppkey(), offset, limit);
        });
        /**
         * 长按卡密
         */
        adapter.setOnItemChildLongClickListener((adapter, view, position) -> {
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.dialog_editor) + cardInfos.get(position).getCard())
                    .setSingleChoiceItems(new String[]{getString(R.string.dialog_delete), getString(R.string.dialog_frozen), getString(R.string.dialog_untie)}, -1, (dialogInterface, i) -> {
                        dialogInterface.dismiss();
                        switch (i) {
                            /**
                             * 删除卡密
                             */
                            case 0: {
                                Loading();
                                SocketHelper.UserHelper.DeleteSoftSingleCards(new SocketCallBack<Basic>() {
                                    @Override
                                    public void next(Basic body) {
                                        HideLoading();
                                        Toast.makeText(SingleCardManage.this, body.getMsg(), Toast.LENGTH_LONG).show();
                                        if (body.getCode() == 200) {
                                            adapter.notifyItemRemoved(position);
                                            cardInfos.remove(position);
                                        }
                                    }

                                    @Override
                                    public void error(Throwable throwable) {
                                        HideLoading();
                                        Toast.makeText(SingleCardManage.this, String.format(getString(R.string.exception), throwable.getMessage()), Toast.LENGTH_LONG).show();
                                    }
                                }, data.getAppkey(), Lists.newArrayList(cardInfos.get(position)));
                            }
                            break;
                            /**
                             * 修改卡密为冻结/正常
                             */
                            case 1: {
                                SoftSingleCardInfo.data card = cardInfos.get(position);
                                card.setUsable(!card.getUsable());
                                Loading();
                                SocketHelper.UserHelper.UpdateSoftSingleCards(new SocketCallBack<Basic>() {
                                    @Override
                                    public void next(Basic body) {
                                        HideLoading();
                                        Toast.makeText(SingleCardManage.this, body.getMsg(), Toast.LENGTH_LONG).show();
                                        if (body.getCode() == 200)
                                            adapter.notifyItemChanged(position);
                                    }

                                    @Override
                                    public void error(Throwable throwable) {
                                        HideLoading();
                                        Toast.makeText(SingleCardManage.this, String.format(getString(R.string.exception), throwable.getMessage()), Toast.LENGTH_LONG).show();
                                    }
                                }, data.getAppkey(), Lists.newArrayList(card));
                            }
                            break;
                            /**
                             * 解绑卡密
                             */
                            case 2: {
                                SoftSingleCardInfo.data card = cardInfos.get(position);
                                card.setMac(null);
                                Loading();
                                SocketHelper.UserHelper.UpdateSoftSingleCards(new SocketCallBack<Basic>() {
                                    @Override
                                    public void next(Basic body) {
                                        HideLoading();
                                        Toast.makeText(SingleCardManage.this, body.getMsg(), Toast.LENGTH_LONG).show();
                                        if (body.getCode() == 200)
                                            adapter.notifyItemChanged(position);
                                    }

                                    @Override
                                    public void error(Throwable throwable) {
                                        HideLoading();
                                        Toast.makeText(SingleCardManage.this, String.format(getString(R.string.exception), throwable.getMessage()), Toast.LENGTH_LONG).show();
                                    }
                                }, data.getAppkey(), Lists.newArrayList(card));
                            }
                            break;
                        }
                    })
                    .setPositiveButton(R.string.cancel, null)
                    .show();
            return true;
        });
        /**
         * 复制卡密
         */
        adapter.setOnItemChildClickListener((adapter, view, position) -> {
            ClipboardManager cm = (ClipboardManager) this.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData mClipData = ClipData.newPlainText("Label", cardInfos.get(position).getCard());
            Objects.requireNonNull(cm).setPrimaryClip(mClipData);
            Toast.makeText(this, R.string.copy_success, Toast.LENGTH_LONG).show();
        });
        /**
         * 搜索卡密
         */
        bottomAppBar.setNavigationOnClickListener(view -> {
            if (!searchView.isSearchOpen()) {
                searchView.showSearch();
                searchflag = true;
                adapter.setSrcList(cardInfos);
            }
            searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    adapter.getFilter().filter(newText);
                    return true;
                }
            });
        });
        /**
         * 菜单事件
         */
        bottomAppBar.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                /**
                 * 删除全部卡密
                 */
                case R.id.menu_delete: {
                    if (cardInfos.size() == 0)
                        Toast.makeText(this, "Data Null", Toast.LENGTH_LONG).show();
                    else
                        new AlertDialog.Builder(this)
                                .setTitle(R.string.dialog_tips)
                                .setMessage(R.string.dialog_delete_single_card_all)
                                .setPositiveButton(R.string.cancel, null)
                                .setNegativeButton(R.string.ok, (dialogInterface, i) -> {
                                    Loading();
                                    SocketHelper.UserHelper.DeleteSoftSingleCards(new SocketCallBack<Basic>() {
                                        @Override
                                        public void next(Basic body) {
                                            HideLoading();
                                            Toast.makeText(SingleCardManage.this, body.getMsg(), Toast.LENGTH_LONG).show();
                                            if (body.getCode() == 200) {
                                                cardInfos.clear();
                                                adapter.notifyDataSetChanged();
                                            }
                                        }

                                        @Override
                                        public void error(Throwable throwable) {
                                            HideLoading();
                                            Toast.makeText(SingleCardManage.this, String.format(getString(R.string.exception), throwable.getMessage()), Toast.LENGTH_LONG).show();
                                        }
                                    }, data.getAppkey(), cardInfos);
                                })
                                .show();
                }
                break;
                /**
                 * 导出卡密
                 */
                case R.id.menu_export: {
                    if (cardInfos.size() == 0)
                        Toast.makeText(this, "Data Null", Toast.LENGTH_LONG).show();
                    else {
                        new AlertDialog.Builder(this)
                                .setTitle(R.string.dialog_tips)
                                .setMessage(R.string.dialog_export_all)
                                .setPositiveButton(R.string.cancel, null)
                                .setNegativeButton(R.string.menu_export, (dialogInterface, i) -> {
                                    StringBuilder buffer = new StringBuilder();
                                    for (SoftSingleCardInfo.data cardInfo : cardInfos)
                                        buffer.append(getString(R.string.single_item_card))
                                                .append(cardInfo.getCard())
                                                .append(" ")
                                                .append(getString(R.string.single_item_type))
                                                .append(SingleCardTypeEnums.getFlags(cardInfo.getType()))
                                                .append("\n");
                                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd HH:mm");
                                    try {
                                        File tmp = new File(Environment.getExternalStorageDirectory(), getData().getName() + "-" + simpleDateFormat.format(new Date()) + ".txt");
                                        FileOutputStream exp = new FileOutputStream(tmp);
                                        exp.write(buffer.toString().getBytes());
                                        exp.close();
                                        new AlertDialog.Builder(this)
                                                .setTitle(R.string.dialog_tips)
                                                .setMessage(getString(R.string.dialog_export_success) + tmp.getAbsolutePath())
                                                .setPositiveButton(R.string.cancel, null)
                                                .show();
                                    } catch (Exception throwable) {
                                        throwable.printStackTrace();
                                        Toast.makeText(this, String.format(getString(R.string.exception), throwable.getMessage()), Toast.LENGTH_LONG).show();
                                    }
                                })
                                .setNeutralButton(R.string.dialog_copy, (dialogInterface, i) -> {
                                    StringBuilder buffer = new StringBuilder();
                                    for (SoftSingleCardInfo.data cardInfo : cardInfos)
                                        buffer.append(getString(R.string.single_item_card))
                                                .append(cardInfo.getCard())
                                                .append(" ")
                                                .append(getString(R.string.single_item_type))
                                                .append(SingleCardTypeEnums.getFlags(cardInfo.getType()))
                                                .append("\n");
                                    ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                                    ClipData mClipData = ClipData.newPlainText("Label", buffer.toString());
                                    cm.setPrimaryClip(mClipData);
                                    Toast.makeText(this, R.string.copy_success, Toast.LENGTH_LONG).show();
                                })
                                .show();
                    }
                }
                break;
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
        /**
         * 搜索栏关闭事件
         */
        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {

            }

            @Override
            public void onSearchViewClosed() {
                if (adapter.getLoadMoreModule().isLoading())
                    adapter.getLoadMoreModule().loadMoreComplete();
                searchflag = false;
            }
        });
        Loading();
        SocketHelper.UserHelper.GetSoftSingleCard(new SocketCallBack<SoftSingleCardInfo>() {
            @Override
            public void next(SoftSingleCardInfo body) {
                HideLoading();
                if (body.getCode() != 200) {
                    Toast.makeText(SingleCardManage.this, body.getMsg(), Toast.LENGTH_LONG).show();
                } else {
                    cardInfos.clear();
                    cardInfos.addAll(body.getData());
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void error(Throwable throwable) {
                HideLoading();
                Toast.makeText(SingleCardManage.this, String.format(getString(R.string.exception), throwable.getMessage()), Toast.LENGTH_LONG).show();
                SingleCardManage.this.finish();
            }
        }, data.getAppkey(), offset, limit);
    }

    @Override
    public void onError(@NonNull Throwable throwable) {
        Toast.makeText(this, String.format(getString(R.string.exception), throwable.getMessage()), Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    public void onBackPressed() {
        if (searchView.isSearchOpen())
            searchView.closeSearch();
        else
            super.onBackPressed();
    }
}
