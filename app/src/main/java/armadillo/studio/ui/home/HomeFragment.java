package armadillo.studio.ui.home;

import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.core.app.ActivityOptionsCompat;
import androidx.lifecycle.LiveData;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.Serializable;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import armadillo.studio.CloudApp;
import armadillo.studio.R;
import armadillo.studio.activity.Handle;
import armadillo.studio.activity.Helper;
import armadillo.studio.activity.Selete;
import armadillo.studio.adapter.ArchiveAdapter;
import armadillo.studio.adapter.TaskAdapter;
import armadillo.studio.common.base.BaseFragment;
import armadillo.studio.common.base.callback.DowCallBack;
import armadillo.studio.common.base.callback.SocketCallBack;
import armadillo.studio.common.enums.SeleteFileType;
import armadillo.studio.common.enums.SignerEnums;
import armadillo.studio.common.jks.SignerApk;
import armadillo.studio.common.log.logger;
import armadillo.studio.common.manager.TaskDetailManager;
import armadillo.studio.common.manager.UserDetailManager;
import armadillo.studio.common.utils.Accessibility;
import armadillo.studio.common.utils.AppUtils;
import armadillo.studio.common.utils.ArchiveZip;
import armadillo.studio.common.utils.DownloadUtil;
import armadillo.studio.common.utils.FileSize;
import armadillo.studio.common.utils.StreamUtils;
import armadillo.studio.helper.SocketHelper;
import armadillo.studio.model.handle.HandleNode;
import armadillo.studio.model.signer.KeyFile;
import armadillo.studio.model.signer.KeyInfo;
import armadillo.studio.model.sys.Help;
import armadillo.studio.server.TopServer;
import armadillo.studio.widget.BaleDialog;
import armadillo.studio.widget.LoadingDialog;
import armadillo.studio.widget.RoundCornerDialog;
import butterknife.BindView;
import butterknife.OnClick;

public class HomeFragment extends BaseFragment<HomeViewModel> implements SwipeRefreshLayout.OnRefreshListener {
    @BindView(R.id.refresh)
    SwipeRefreshLayout refresh;
    @BindView(R.id.add)
    FloatingActionButton add;
    @BindView(R.id.recycler)
    RecyclerView recycler;
    private final List<TaskLiveData> liveData = new ArrayList<>();
    private TaskAdapter taskAdapter;
    private volatile List<Object> handles;

    @Override
    protected Class<HomeViewModel> BindViewModel() {
        return HomeViewModel.class;
    }

    @Override
    protected int BindXML() {
        return R.layout.fragment_home;
    }

    @SuppressLint("InflateParams")
    @Override
    protected void BindData() {
        taskAdapter = new TaskAdapter(R.layout.item_task, liveData, getViewLifecycleOwner());
        recycler.setLayoutManager(new LinearLayoutManager(requireActivity()));
        recycler.setAdapter(taskAdapter);
        recycler.setHasFixedSize(true);
        taskAdapter.setAnimationEnable(true);
        taskAdapter.setAnimationFirstOnly(false);
        taskAdapter.setAnimationWithDefault(BaseQuickAdapter.AnimationType.AlphaIn);
        taskAdapter.setEmptyView(R.layout.task_no_item);
        taskAdapter.setFooterView(LayoutInflater.from(requireActivity()).inflate(R.layout.status_footer, null));
        taskAdapter.addChildLongClickViewIds(R.id.cardview);
        taskAdapter.addChildClickViewIds(R.id.cardview);
        /**
         * 任务长按事件
         */
        taskAdapter.setOnItemChildLongClickListener((adapter, view, position) -> {
            if (view.getId() != R.id.cardview) return true;
            if (refresh.isRefreshing()) return true;
            new AlertDialog.Builder(requireActivity())
                    .setTitle(R.string.dialog_tips)
                    .setMessage(R.string.delete_task)
                    .setPositiveButton(R.string.cancel, null)
                    .setNegativeButton(R.string.ok, (dialogInterface, i) -> {
                        TaskDetailManager.getInstance().unregister(liveData.get(position));
                        new File(System.getProperty("task.dir"), liveData.get(position).getTask().getUuid()).delete();
                        liveData.get(position).getTask().getOld().delete();
                        if (liveData.get(position).getTask().getState() != 200 && liveData.get(position).getTask().getState() != 404)
                            SocketHelper.SysHelper.FreeTask(liveData.get(position).getTask().getUuid());
                        liveData.remove(position);
                        taskAdapter.notifyItemRemoved(position);
                        taskAdapter.notifyItemRangeChanged(position, liveData.size() - position);
                    })
                    .show();
            return true;
        });
        /**
         * 任务点击事件
         */
        taskAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            if (view.getId() != R.id.cardview) return;
            armadillo.studio.model.task.TaskInfo taskInfo = liveData.get(position).getTask();
            RoundCornerDialog archive = new RoundCornerDialog(requireActivity())
                    .SetView(R.layout.dialog_archive)
                    .SetText(R.id.name, taskInfo.getName())
                    .SetText(R.id.packagename, taskInfo.getPackagename())
                    .AddChildClickViewIds(R.id.archive)
                    .Show()
                    .SetOnChildClickListener((view1, dialog) -> {
                        if (taskInfo.getState() == 200) {
                            File sussecc = taskInfo.getOld();
                            /**
                             * 下载资源文件
                             */
                            if (!ArchiveZip.isZipFile(sussecc)) {
                                Toast.makeText(requireActivity(), R.string.dow_res, Toast.LENGTH_LONG).show();
                                LoadingDialog.getInstance().showProgress(requireActivity());
                                DownloadUtil.get().download("http://" + CloudApp.getContext().getResources().getString(R.string.host) + ":8000/file?key=" + taskInfo.getUuid(), taskInfo.getOld(), new DowCallBack() {
                                    @Override
                                    public void onStart() {

                                    }

                                    @Override
                                    public void onProgress(int progress) {
                                        LoadingDialog.getInstance().setProgress(progress);
                                    }

                                    @Override
                                    public void onFinish(byte[] bytes) {
                                        SocketHelper.SysHelper.FreeTask(taskInfo.getUuid());
                                        new Handler(Looper.getMainLooper()).post(() -> {
                                            LoadingDialog.getInstance().hideProgress();
                                            Toast.makeText(requireActivity(), R.string.res_dow_sussecc, Toast.LENGTH_LONG).show();
                                            if (ArchiveZip.isZipFile(taskInfo.getOld()))
                                                Glide.with(requireActivity()).load(R.drawable.ic_archive).into((ImageView) view1);
                                        });
                                    }

                                    @Override
                                    public void onFail(String errorInfo) {
                                        if (sussecc.exists())
                                            sussecc.delete();
                                        new Handler(Looper.getMainLooper()).post(() -> {
                                            LoadingDialog.getInstance().hideProgress();
                                            Toast.makeText(requireActivity(), String.format(getString(R.string.exception), errorInfo), Toast.LENGTH_LONG).show();
                                        });
                                    }
                                });
                            }
                            /**
                             * 打包资源文件
                             */
                            else {
                                if ((taskInfo.getHandle() & 1) != 0 || (taskInfo.getHandle() & 0x200000000L) != 0)
                                    ArchiveZip.ArchiveOld(requireActivity(), taskInfo, null, 0);
                                else {
                                    /**
                                     * 选择签名文件
                                     */
                                    View root_view = LayoutInflater.from(requireActivity()).inflate(R.layout.dialog_apk_signer, null);
                                    AppCompatSpinner signer_spinner = root_view.findViewById(R.id.signer_key_spinner);
                                    AppCompatSpinner signer_mode_spinner = root_view.findViewById(R.id.signer_mode_spinner);
                                    List<KeyFile> files = new ArrayList<>();
                                    {
                                        for (File file : Objects.requireNonNull(new File(Objects.requireNonNull(System.getProperty("jks.dir"))).listFiles())) {
                                            if (file.getName().endsWith(".key"))
                                                files.add(new KeyFile(file.getAbsolutePath()));
                                        }
                                        Collections.sort(files, (File o1, File o2) -> {
                                            if (o1.lastModified() <= o2.lastModified())
                                                return 1;
                                            else
                                                return -1;
                                        });
                                    }
                                    ArrayAdapter<KeyFile> spinner_adapter = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_spinner_item, files);
                                    spinner_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                    Objects.requireNonNull(signer_spinner).setAdapter(spinner_adapter);
                                    new AlertDialog.Builder(requireActivity())
                                            .setView(root_view)
                                            .setNegativeButton(R.string.cancel, null)
                                            .setPositiveButton(R.string.ok, (dialog1, which) -> {
                                                int signer_type = 0;
                                                switch (signer_mode_spinner.getSelectedItemPosition()) {
                                                    case 0:
                                                        signer_type = SignerEnums.V1.getType();
                                                        break;
                                                    case 1:
                                                        signer_type = SignerEnums.V1.getType() | SignerEnums.V2.getType();
                                                        break;
                                                    case 2:
                                                        signer_type = SignerEnums.V2.getType();
                                                        break;
                                                }
                                                ArchiveZip.ArchiveOld(
                                                        requireActivity(),
                                                        taskInfo,
                                                        files.get(signer_spinner.getSelectedItemPosition()),
                                                        signer_type);
                                            })
                                            .show();
                                }
                            }
                        } else if (taskInfo.getState() == 404) {
                            ClipboardManager cm = (ClipboardManager) requireActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                            ClipData mClipData = ClipData.newPlainText("Label", Arrays.toString(taskInfo.getRecord().toArray(new String[0])));
                            Objects.requireNonNull(cm).setPrimaryClip(mClipData);
                            Toast.makeText(requireActivity(), R.string.copy_success, Toast.LENGTH_LONG).show();
                        }
                    });
            /**
             * 读取原文件大小
             */
            CloudApp.getCachedThreadPool().execute(() -> {
                String size = FileSize.getAutoFileOrFileSize(taskInfo.getSrc());
                new Handler(Looper.getMainLooper()).post(() -> {
                    archive.SetText(R.id.size, size);
                });
            });
            /**
             * 加载图标
             */
            Glide.with(requireActivity()).load(Base64.decode(taskInfo.getIco(), Base64.NO_WRAP)).into((ImageView) archive.getView().findViewById(R.id.avatar));
            /**
             * 加载处理事件
             */
            {
                RecyclerView type_recycler = archive.getView().findViewById(R.id.type_recycler);
                ArchiveAdapter type_adapter = new ArchiveAdapter(R.layout.item_task_info, taskInfo.getDesc());
                type_recycler.setHasFixedSize(true);
                type_recycler.setLayoutManager(new LinearLayoutManager(requireActivity()));
                type_recycler.setAdapter(type_adapter);
            }
            /**
             * 加载任务状态
             */
            {
                RecyclerView recycler = archive.getView().findViewById(R.id.recycler);
                List<String> record = new ArrayList<>();
                ArchiveAdapter archiveAdapter = new ArchiveAdapter(R.layout.item_task_info, record);
                recycler.setHasFixedSize(true);
                recycler.setLayoutManager(new LinearLayoutManager(requireActivity()));
                recycler.setAdapter(archiveAdapter);
                if (taskInfo.getState() != 200 && taskInfo.getState() != 404) {
                    ArchiveObserver observer = new ArchiveObserver(record, archiveAdapter);
                    liveData.get(position).observe(getViewLifecycleOwner(), observer);
                    archive.setOnDismissListener(dialogInterface -> {
                        liveData.get(position).removeObserver(observer);
                    });
                } else {
                    record.addAll(taskInfo.getRecord());
                    archiveAdapter.notifyDataSetChanged();
                }
            }
            /**
             * 资源是否下载完成
             */
            {
                if (taskInfo.getState() == 404)
                    Glide.with(requireActivity()).load(R.drawable.ic_content_copy_black).into((ImageView) archive.getView().findViewById(R.id.archive));
                else if (ArchiveZip.isZipFile(taskInfo.getOld()))
                    Glide.with(requireActivity()).load(R.drawable.ic_archive).into((ImageView) archive.getView().findViewById(R.id.archive));
                else
                    Glide.with(requireActivity()).load(R.drawable.ic_dow_res).into((ImageView) archive.getView().findViewById(R.id.archive));
            }
        });
        /**
         * 监听是否登录成功
         */
        UserDetailManager.getInstance().observe(getViewLifecycleOwner(), userDetailManager -> {
            if (userDetailManager.getValue() != null)
                InitData();
        });
    }

    private static class ArchiveObserver implements androidx.lifecycle.Observer<armadillo.studio.model.task.TaskInfo> {
        private List<String> record;
        private ArchiveAdapter archiveAdapter;

        ArchiveObserver(List<String> record, ArchiveAdapter archiveAdapter) {
            this.record = record;
            this.archiveAdapter = archiveAdapter;
        }

        @Override
        public void onChanged(@NotNull armadillo.studio.model.task.TaskInfo taskInfo) {
            logger.d("Archive 任务信息更新");
            boolean flag = false;
            for (String s : taskInfo.getRecord()) {
                if (!record.contains(s)) {
                    logger.d(s);
                    record.add(s);
                    flag = true;
                }
            }
            if (flag)
                archiveAdapter.notifyDataSetChanged();
        }
    }

    private void InitData() {
        refresh.setOnRefreshListener(this);
        liveData.clear();
        liveData.addAll(viewModel.getValue());
        taskAdapter.notifyDataSetChanged();
        LoopTask();
    }

    private void LoopTask() {
        TaskDetailManager.getInstance().unregisterAll();
        for (TaskLiveData data : liveData) {
            if (data.getTask().getState() == 200
                    || data.getTask().getState() == 404
                    || AppUtils.isApkFile(data.getTask().getOld())
                    || !AppUtils.isApkFile(data.getTask().getSrc()))
                continue;
            TaskDetailManager.getInstance().register(data);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        for (LiveData<?> data : viewModel.getValue())
            if (data.hasObservers())
                data.removeObservers(getViewLifecycleOwner());
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_auxiliary:
                if (Build.VERSION.SDK_INT >= 23) {
                    if (Settings.canDrawOverlays(requireActivity())) {
                        if (Accessibility.isAccessibilitySettingsOn(requireActivity())) {
                            new AlertDialog.Builder(requireActivity())
                                    .setTitle(R.string.dialog_tips)
                                    .setMessage(R.string.not_auxiliary)
                                    .setNegativeButton(R.string.open, (dialogInterface, i) -> {
                                        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                                        startActivity(intent);
                                    })
                                    .setPositiveButton(R.string.cancel, null)
                                    .show();
                        } else {
                            requireActivity().startService(new Intent(requireActivity(), TopServer.class));
                        }
                    } else {
                        new AlertDialog.Builder(requireActivity())
                                .setTitle(R.string.dialog_tips)
                                .setMessage(R.string.not_windows)
                                .setNegativeButton(R.string.open, (dialogInterface, i) -> {
                                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + requireActivity().getPackageName()));
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                })
                                .setPositiveButton(R.string.cancel, null)
                                .show();
                    }
                } else {
                    if (Accessibility.isAccessibilitySettingsOn(requireActivity())) {
                        new AlertDialog.Builder(requireActivity())
                                .setTitle(R.string.dialog_tips)
                                .setMessage(R.string.not_auxiliary)
                                .setNegativeButton(R.string.open, (dialogInterface, i) -> {
                                    Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                                    startActivity(intent);
                                })
                                .setPositiveButton(R.string.cancel, null)
                                .show();
                    } else
                        requireActivity().startService(new Intent(requireActivity(), TopServer.class));
                }
                return true;
            case R.id.menu_signer:
                Intent intent = new Intent(requireActivity(), Selete.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("data", SeleteFileType.APK);
                intent.putExtras(bundle);
                startActivityForResult(intent, 6000);
                return true;
            case R.id.menu_helper: {
                LoadingDialog.getInstance().show(requireActivity());
                SocketHelper.SysHelper.GetHelper(new SocketCallBack<Help>() {
                    @Override
                    public void next(Help body) {
                        LoadingDialog.getInstance().hide();
                        if (body.getCode() == 200) {
                            Intent intent = new Intent(requireActivity(), Helper.class);
                            Bundle bundle = new Bundle();
                            bundle.putSerializable("data", body);
                            intent.putExtras(bundle);
                            startActivity(intent);
                        } else
                            Toast.makeText(requireActivity(), body.getMsg(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void error(Throwable throwable) {
                        LoadingDialog.getInstance().hide();
                        Toast.makeText(requireActivity(), String.format(getString(R.string.exception), throwable.getMessage()), Toast.LENGTH_SHORT).show();
                    }
                });
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {
        for (LiveData<?> data : viewModel.getValue())
            if (data.hasObservers())
                data.removeObservers(getViewLifecycleOwner());
        liveData.clear();
        liveData.addAll(viewModel.getValue());
        taskAdapter.notifyDataSetChanged();
        refresh.setRefreshing(false);
        LoopTask();
    }

    @OnClick(R.id.add)
    public void onClick(@NotNull View view) {
        if (view.getId() == R.id.add) {
            Intent intent = new Intent(requireActivity(), Selete.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("data", SeleteFileType.APK);
            intent.putExtras(bundle);
            startActivityForResult(intent, 100, ActivityOptions.makeSceneTransitionAnimation(requireActivity(), view, "add").toBundle());
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            if (requestCode == 100 && resultCode == 100) {
                String Path = data.getStringExtra("path");
                if (Path != null) {
                    if (handles == null) {
                        LoadingDialog.getInstance().show(requireActivity());
                        SocketHelper.SysHelper.GetHandle(new SocketCallBack<Help>() {
                            @Override
                            public void next(Help body) {
                                LoadingDialog.getInstance().hide();
                                if (body.getCode() == 200) {
                                    Type listType = new TypeToken<List<HandleNode>>() {
                                    }.getType();
                                    handles = new Gson().fromJson(new String(Base64.decode(body.getData(), Base64.NO_WRAP)), listType);
                                    Intent intent = new Intent(requireActivity(), Handle.class);
                                    Bundle bundle = new Bundle();
                                    bundle.putSerializable("data", (Serializable) handles);
                                    bundle.putString("Path", Path);
                                    intent.putExtras(bundle);
                                    startActivityForResult(intent, 5000, ActivityOptions.makeSceneTransitionAnimation(requireActivity(), add, "add").toBundle());
                                    return;
                                }
                                Toast.makeText(requireActivity(), body.getMsg(), Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void error(Throwable throwable) {
                                LoadingDialog.getInstance().hide();
                                Toast.makeText(requireActivity(), String.format(getString(R.string.exception), throwable.getMessage()), Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        Intent intent = new Intent(requireActivity(), Handle.class);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("data", (Serializable) handles);
                        bundle.putString("Path", Path);
                        intent.putExtras(bundle);
                        add.post(() -> {
                            startActivityForResult(intent, 5000, ActivityOptions.makeSceneTransitionAnimation(requireActivity(), add, "add").toBundle());
                        });
                    }
                }
            } else if (requestCode == 6000 && resultCode == 100) {
                String Path = data.getStringExtra("path");
                if (Path != null)
                    SignerDialog(Path);
            }
        } else if (requestCode == 5000 && resultCode == 200)
            onRefresh();
    }

    private void SignerDialog(final String Path) {
        /**
         * 选择签名文件
         */
        View root_view = LayoutInflater.from(requireActivity()).inflate(R.layout.dialog_apk_signer, null);
        AppCompatSpinner signer_spinner = root_view.findViewById(R.id.signer_key_spinner);
        AppCompatSpinner signer_mode_spinner = root_view.findViewById(R.id.signer_mode_spinner);
        List<KeyFile> files = new ArrayList<>();
        {
            for (File file : Objects.requireNonNull(new File(Objects.requireNonNull(System.getProperty("jks.dir"))).listFiles())) {
                if (file.getName().endsWith(".key"))
                    files.add(new KeyFile(file.getAbsolutePath()));
            }
            Collections.sort(files, (File o1, File o2) -> {
                if (o1.lastModified() <= o2.lastModified())
                    return 1;
                else
                    return -1;
            });
        }
        ArrayAdapter<KeyFile> spinner_adapter = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_spinner_item, files);
        spinner_adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Objects.requireNonNull(signer_spinner).setAdapter(spinner_adapter);
        new AlertDialog.Builder(requireActivity())
                .setView(root_view)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.ok, (dialog1, which) -> {
                    int signer_type = 0;
                    switch (signer_mode_spinner.getSelectedItemPosition()) {
                        case 0:
                            signer_type = SignerEnums.V1.getType();
                            break;
                        case 1:
                            signer_type = SignerEnums.V1.getType() | SignerEnums.V2.getType();
                            break;
                        case 2:
                            signer_type = SignerEnums.V2.getType();
                            break;
                    }
                    try {
                        KeyInfo keyInfo = new Gson().fromJson(new String(StreamUtils.toByte(new FileInputStream(files.get(signer_spinner.getSelectedItemPosition()))), StandardCharsets.UTF_8), KeyInfo.class);
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                            BaleDialog.ShowInfo(requireActivity(), getString(R.string.signer_ver_fail));
                            return;
                        }
                        BaleDialog.ShowDialog(getActivity(), SignerApk.SignApk(new File(Path),
                                new ByteArrayInputStream(Base64.decode(keyInfo.getSigner(), Base64.NO_WRAP)),
                                keyInfo.getPassWord(),
                                keyInfo.getAliasPass(),
                                false, signer_type));
                    } catch (Exception e) {
                        e.printStackTrace();
                        BaleDialog.ShowErrorDialog(getActivity(), e);
                        Log.e(TAG, String.format(getString(R.string.exception), e.getMessage()));
                    }
                })
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        LoopTask();
    }
}
