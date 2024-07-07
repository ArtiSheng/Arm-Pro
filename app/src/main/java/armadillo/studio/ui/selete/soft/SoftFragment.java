package armadillo.studio.ui.selete.soft;

import android.content.Intent;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import armadillo.studio.R;
import armadillo.studio.adapter.SoftAdapter;
import armadillo.studio.common.base.BaseFragment;
import armadillo.studio.model.apk.PackageInfos;
import butterknife.BindView;
import butterknife.OnClick;

public class SoftFragment extends BaseFragment<SoftViewModel> implements SwipeRefreshLayout.OnRefreshListener, View.OnClickListener {
    @BindView(R.id.recycler)
    RecyclerView recycler;
    @BindView(R.id.refresh)
    SwipeRefreshLayout refresh;
    @BindView(R.id.add)
    FloatingActionButton search;
    private SoftAdapter softAdapter;
    private MaterialSearchView searchView;
    private List<PackageInfos> data = new ArrayList<>();
    private boolean isload = false;

    @Override
    protected Class<SoftViewModel> BindViewModel() {
        return SoftViewModel.class;
    }

    @Override
    protected int BindXML() {
        return R.layout.fragment_home;
    }

    @Override
    protected void BindData() {
        searchView = requireActivity().findViewById(R.id.search_view);
        refresh.setEnabled(false);
        search.setImageResource(R.drawable.ic_search);
        softAdapter = new SoftAdapter(R.layout.item_app, data);
        recycler.setLayoutManager(new LinearLayoutManager(requireActivity()));
        recycler.setHasFixedSize(true);
        recycler.setAdapter(softAdapter);
        softAdapter.setEmptyView(R.layout.status_loading);
        softAdapter.setAnimationEnable(true);
        softAdapter.setAnimationFirstOnly(false);
        softAdapter.setAnimationWithDefault(BaseQuickAdapter.AnimationType.AlphaIn);
        softAdapter.addChildClickViewIds(R.id.cardview);
        softAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            if (view.getId() != R.id.cardview) return;
            if (refresh.isRefreshing()) return;
            PackageInfos infos = data.get(position);
            if (infos.isJiagu_flag()) {
                new AlertDialog.Builder(requireActivity())
                        .setMessage(infos.getName() + "\n该应用很有可能是" + infos.getJiagu() + "不建议你选择加固的应用")
                        .setPositiveButton(R.string.dialog_have_selete, (dialogInterface, i) -> {
                            Intent intent = new Intent();
                            intent.putExtra("path", infos.getPackageInfo().applicationInfo.sourceDir);
                            requireActivity().setResult(100, intent);
                            requireActivity().finishAfterTransition();
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();
            } else {
                new AlertDialog.Builder(requireActivity())
                        .setMessage(getString(R.string.dialog_selete) + infos.getName())
                        .setPositiveButton(R.string.ok, (dialogInterface, i) -> {
                            Intent intent = new Intent();
                            intent.putExtra("path", infos.getPackageInfo().applicationInfo.sourceDir);
                            requireActivity().setResult(100, intent);
                            requireActivity().finishAfterTransition();
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show();
            }
        });
        refresh.setOnRefreshListener(this);
        viewModel.getAll(list -> {
            refresh.setEnabled(true);
            data.addAll(list);
            softAdapter.notifyDataSetChanged();
            isload = true;
            search.setOnClickListener(this);
        }, requireActivity());
    }

    @OnClick(R.id.add)
    public void onClick(@NotNull View view) {
        switch (view.getId()) {
            case R.id.add:
                if (!searchView.isSearchOpen())
                    searchView.showSearch();
                searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        softAdapter.getFilter().filter(newText);
                        return true;
                    }
                });
                break;
        }
    }

    @Override
    public void onRefresh() {
        if (!isload) {
            refresh.setRefreshing(false);
            return;
        }
        data.clear();
        viewModel.getAll(list -> {
            data.addAll(list);
            softAdapter.notifyDataSetChanged();
            refresh.setRefreshing(false);
        }, requireActivity());
    }
}
