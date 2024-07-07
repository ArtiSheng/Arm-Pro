package armadillo.studio.ui.selete.file;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Environment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.miguelcatalan.materialsearchview.MaterialSearchView;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import armadillo.studio.CloudApp;
import armadillo.studio.R;
import armadillo.studio.adapter.FileAdapter;
import armadillo.studio.common.base.BaseFragment;
import armadillo.studio.common.enums.SeleteFileType;
import butterknife.BindView;
import butterknife.OnClick;

public class FileFragment extends BaseFragment<FileViewModel> implements SwipeRefreshLayout.OnRefreshListener {
    @BindView(R.id.recycler)
    RecyclerView recycler;
    @BindView(R.id.refresh)
    SwipeRefreshLayout refresh;
    @BindView(R.id.add)
    FloatingActionButton search;
    private FileAdapter fileAdapter;
    private MaterialSearchView searchView;
    private final List<File> data = new ArrayList<>();
    private View headerView;
    private final SeleteFileType seleteFileType;

    public FileFragment(SeleteFileType seleteFileType) {
        this.seleteFileType = seleteFileType;
    }

    @Override
    protected Class<FileViewModel> BindViewModel() {
        return FileViewModel.class;
    }

    @Override
    protected int BindXML() {
        return R.layout.fragment_home;
    }

    @SuppressLint("InflateParams")
    @Override
    protected void BindData() {
        searchView = Objects.requireNonNull(requireActivity()).findViewById(R.id.search_view);
        headerView = LayoutInflater.from(requireActivity()).inflate(R.layout.item_file, null);
        ((TextView) headerView.findViewById(R.id.name)).setText("...");
        Glide.with(CloudApp.getContext())
                .load(R.drawable.file_dir)
                .circleCrop()
                .into((ImageView) headerView.findViewById(R.id.avatar));
        search.setImageResource(R.drawable.ic_search);
        fileAdapter = new FileAdapter(R.layout.item_file, data);
        recycler.setLayoutManager(new LinearLayoutManager(requireActivity()));
        recycler.setHasFixedSize(true);
        recycler.setAdapter(fileAdapter);
        fileAdapter.addHeaderView(headerView);
        fileAdapter.setFooterView(LayoutInflater.from(requireActivity()).inflate(R.layout.status_footer, null));
        fileAdapter.setAnimationEnable(true);
        fileAdapter.setAnimationFirstOnly(false);
        fileAdapter.setAnimationWithDefault(BaseQuickAdapter.AnimationType.AlphaIn);
        viewModel.setRoot(new File(Objects.requireNonNull(CloudApp.getSharedPreferences().getString("last_dir", Environment.getExternalStorageDirectory().getAbsolutePath()))));
        data.addAll(viewModel.getValue());
        fileAdapter.notifyDataSetChanged();
        refresh.setOnRefreshListener(this);
        headerView.setOnClickListener(view -> {
            if (Environment.getExternalStorageDirectory().getAbsolutePath().equals(viewModel.getRoot().getAbsolutePath()))
                return;
            refresh.setRefreshing(true);
            data.clear();
            viewModel.setRoot(viewModel.getRoot().getParentFile());
            data.addAll(viewModel.getValue());
            fileAdapter.notifyDataSetChanged();
            refresh.setRefreshing(false);
            if (searchView.isSearchOpen())
                fileAdapter.setSrcList(data);
        });
        fileAdapter.addChildClickViewIds(R.id.cardview);
        fileAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            if (view.getId() != R.id.cardview) return;
            File var = data.get(position);
            if (Objects.requireNonNull(var).isDirectory()) {
                refresh.setRefreshing(true);
                viewModel.setRoot(data.get(position));
                data.clear();
                data.addAll(viewModel.getValue());
                fileAdapter.notifyDataSetChanged();
                refresh.setRefreshing(false);
                if (searchView.isSearchOpen())
                    fileAdapter.setSrcList(data);
            } else {
                switch (seleteFileType) {
                    case APK: {
                        if (var.getName().toLowerCase().endsWith(".apk"))
                            setResultDialog(var);
                    }
                    break;
                    case JKS: {
                        if (var.getName().toLowerCase().endsWith(".jks"))
                            setResultDialog(var);
                    }
                    break;
                }
            }
        });
    }
    @OnClick({
            R.id.add
    })
    public void OnClick(@NotNull View view){
        switch (view.getId()) {
            case R.id.add:
                if (!searchView.isSearchOpen()) {
                    searchView.showSearch();
                    fileAdapter.setSrcList(data);
                }
                searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        fileAdapter.getFilter().filter(newText);
                        return true;
                    }
                });
                break;
        }
    }

    @Override
    public void onRefresh() {
        data.clear();
        data.addAll(viewModel.getValue());
        fileAdapter.notifyDataSetChanged();
        refresh.setRefreshing(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        requireView().setFocusableInTouchMode(true);
        requireView().requestFocus();
        requireView().setOnKeyListener((view, i, keyEvent) -> {
            if (keyEvent.getAction() == KeyEvent.ACTION_DOWN && i == KeyEvent.KEYCODE_BACK) {
                if (Environment.getExternalStorageDirectory().getAbsolutePath().equals(viewModel.getRoot().getAbsolutePath())) {
                    return false;
                } else {
                    refresh.setRefreshing(true);
                    data.clear();
                    viewModel.setRoot(viewModel.getRoot().getParentFile());
                    data.addAll(viewModel.getValue());
                    fileAdapter.notifyDataSetChanged();
                    refresh.setRefreshing(false);
                }
                return true;
            }
            return false;
        });
    }

    private void setResultDialog(@NotNull File var) {
        new AlertDialog.Builder(requireActivity())
                .setMessage(getString(R.string.dialog_selete) + var.getName())
                .setPositiveButton(R.string.ok, (dialogInterface, i) -> {
                    Intent intent = new Intent();
                    intent.putExtra("path", var.getAbsolutePath());
                    requireActivity().setResult(100, intent);
                    requireActivity().finishAfterTransition();
                    CloudApp.getEditor().putString("last_dir", viewModel.getRoot().getAbsolutePath()).apply();
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
