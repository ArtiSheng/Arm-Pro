/*
 * Copyright (c) 2020. Armadillo
 */

package armadillo.studio.ui.signer;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import armadillo.studio.R;
import armadillo.studio.activity.Selete;
import armadillo.studio.adapter.SignerAdapter;
import armadillo.studio.common.base.BaseFragment;
import armadillo.studio.common.enums.SeleteFileType;
import armadillo.studio.common.jks.CertCreator;
import armadillo.studio.common.utils.StreamUtils;
import armadillo.studio.model.signer.KeyInfo;
import armadillo.studio.widget.LoadingDialog;
import armadillo.studio.widget.RoundCornerDialog;
import butterknife.BindView;
import butterknife.OnClick;

public class SignerFragment extends BaseFragment<SignerViewModel> implements SwipeRefreshLayout.OnRefreshListener {
    @BindView(R.id.refresh)
    SwipeRefreshLayout refresh;
    @BindView(R.id.add)
    FloatingActionButton add;
    @BindView(R.id.Import)
    FloatingActionButton Import;
    @BindView(R.id.recycler)
    RecyclerView recycler;
    private final List<File> signer = new ArrayList<>();
    private SignerAdapter adapter;

    @Override
    protected Class<SignerViewModel> BindViewModel() {
        return SignerViewModel.class;
    }

    @Override
    protected int BindXML() {
        return R.layout.fragment_signer;
    }

    @SuppressLint("InflateParams")
    @Override
    protected void BindData() {
        signer.addAll(viewModel.getValue());
        refresh.setOnRefreshListener(this);
        adapter = new SignerAdapter(R.layout.item_signer, signer);
        recycler.setLayoutManager(new LinearLayoutManager(requireActivity()));
        recycler.setAdapter(adapter);
        recycler.setHasFixedSize(true);
        adapter.setAnimationEnable(true);
        adapter.setAnimationFirstOnly(false);
        adapter.setAnimationWithDefault(BaseQuickAdapter.AnimationType.AlphaIn);
        adapter.setEmptyView(R.layout.status_empty);
        adapter.setFooterView(LayoutInflater.from(requireActivity()).inflate(R.layout.status_footer, null));
        adapter.addChildLongClickViewIds(R.id.cardview);
        adapter.setOnItemChildLongClickListener((adapter, view, position) -> {
            new AlertDialog.Builder(requireActivity())
                    .setTitle(getString(R.string.dialog_editor) + signer.get(position).getName().replace(".key", ""))
                    .setSingleChoiceItems(new String[]{getString(R.string.dialog_delete), getString(R.string.menu_export)}, -1, (dialogInterface, i) -> {
                        dialogInterface.dismiss();
                        switch (i) {
                            /**
                             * 删除
                             */
                            case 0: {
                                if (signer.get(position).getName().equals("default.key"))
                                    return;
                                if (signer.get(position).delete()) {
                                    signer.remove(position);
                                    adapter.notifyItemRemoved(position);
                                } else
                                    Toast.makeText(requireActivity(), R.string.delete_fail, Toast.LENGTH_LONG).show();
                            }
                            break;
                            /**
                             * 导出
                             */
                            case 1: {
                                File jks = signer.get(position);
                                File outJks = new File(Environment.getExternalStorageDirectory(), jks.getName().replace(".key", ".jks"));
                                try (FileOutputStream outputStream = new FileOutputStream(outJks)) {
                                    KeyInfo keyInfo = new Gson().fromJson(new String(StreamUtils.toByte(new FileInputStream(jks)), StandardCharsets.UTF_8), KeyInfo.class);
                                    outputStream.write(Base64.decode(keyInfo.getSigner(), Base64.NO_WRAP));
                                    Toast.makeText(requireActivity(), String.format(getString(R.string.export_success) + "%s", outJks.getAbsolutePath()), Toast.LENGTH_LONG).show();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Toast.makeText(requireActivity(), String.format(getString(R.string.exception), e.getMessage()), Toast.LENGTH_LONG).show();
                                }
                            }
                            break;
                        }
                    })
                    .setPositiveButton(R.string.cancel, null)
                    .show();
            return true;
        });
    }

    @Override
    public void onRefresh() {
        signer.clear();
        signer.addAll(viewModel.getValue());
        adapter.notifyDataSetChanged();
        refresh.setRefreshing(false);
    }

    @OnClick({
            R.id.add,
            R.id.Import
    })
    public void OnClick(@NotNull View view) {
        switch (view.getId()) {
            case R.id.add: {
                RoundCornerDialog jksdialog = new RoundCornerDialog(requireActivity())
                        .SetView(R.layout.dialog_create_jks)
                        .AddChildClickViewIds(R.id.Next)
                        .Show()
                        .SetOnChildClickListener((view1, dialog) -> {
                            String pass = Objects.requireNonNull(Objects.requireNonNull((TextInputEditText) dialog.getView().findViewById(R.id.password_toggle)).getText()).toString();
                            String alias = Objects.requireNonNull(Objects.requireNonNull((TextInputEditText) dialog.getView().findViewById(R.id.alias_toggle)).getText()).toString();
                            String alias_pass = Objects.requireNonNull(Objects.requireNonNull((TextInputEditText) dialog.getView().findViewById(R.id.alias_pass_toggle)).getText()).toString();
                            if (pass.isEmpty() || alias.isEmpty() || alias_pass.isEmpty()) {
                                Toast.makeText(requireActivity(), R.string.rule_not, Toast.LENGTH_LONG).show();
                            } else {
                                File jksFile = new File(System.getProperty("jks.dir"), alias + ".key");
                                if (jksFile.exists()) {
                                    Toast.makeText(requireActivity(), R.string.jks_exists, Toast.LENGTH_LONG).show();
                                    return;
                                }
                                LoadingDialog.getInstance().show(requireActivity());
                                new Thread(() -> {
                                    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                                         FileOutputStream jks = new FileOutputStream(jksFile)) {
                                        CertCreator.CreateJKS(outputStream, pass, alias, alias_pass);
                                        KeyInfo keyInfo = new KeyInfo(pass, alias, alias_pass, Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP));
                                        jks.write(new Gson().toJson(keyInfo).getBytes());
                                        jks.flush();
                                        new Handler(Looper.getMainLooper()).post(() -> {
                                            signer.add(0, jksFile);
                                            adapter.notifyItemInserted(0);
                                            dialog.dismiss();
                                        });
                                    } catch (Exception e) {
                                        new Handler(Looper.getMainLooper()).post(() -> {
                                            Toast.makeText(requireActivity(), String.format(getString(R.string.exception), e.getMessage()), Toast.LENGTH_LONG).show();
                                        });
                                    }
                                    LoadingDialog.getInstance().hide();
                                }).start();
                            }
                        });
                Objects.requireNonNull(jksdialog.getWindow()).clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
            }
            break;
            case R.id.Import: {
                Intent intent = new Intent(requireActivity(), Selete.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable("data", SeleteFileType.JKS);
                intent.putExtras(bundle);
                startActivityForResult(intent, 8000);
            }
            break;
        }
    }

    private void ImportJKS(String Path) {
        RoundCornerDialog jksdialog = new RoundCornerDialog(requireActivity())
                .SetView(R.layout.dialog_create_jks)
                .AddChildClickViewIds(R.id.Next)
                .Show()
                .SetOnChildClickListener((view, dialog) -> {
                    String pass = Objects.requireNonNull(Objects.requireNonNull((TextInputEditText) dialog.getView().findViewById(R.id.password_toggle)).getText()).toString();
                    String alias = Objects.requireNonNull(Objects.requireNonNull((TextInputEditText) dialog.getView().findViewById(R.id.alias_toggle)).getText()).toString();
                    String alias_pass = Objects.requireNonNull(Objects.requireNonNull((TextInputEditText) dialog.getView().findViewById(R.id.alias_pass_toggle)).getText()).toString();
                    if (pass.isEmpty() || alias.isEmpty() || alias_pass.isEmpty()) {
                        Toast.makeText(requireActivity(), R.string.rule_not, Toast.LENGTH_LONG).show();
                    } else {
                        try {
                            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
                            keyStore.load(new FileInputStream(Path), pass.toCharArray());
                            Key privateKey = keyStore.getKey(alias, alias_pass.toCharArray());
                            if (privateKey == null)
                                throw new Exception();
                            File jksFile = new File(System.getProperty("jks.dir"), alias + ".key");
                            if (jksFile.exists()) {
                                Toast.makeText(requireActivity(), R.string.jks_exists, Toast.LENGTH_LONG).show();
                                return;
                            }
                            FileOutputStream jks = new FileOutputStream(jksFile);
                            KeyInfo keyInfo = new KeyInfo(pass, alias, alias_pass, Base64.encodeToString(StreamUtils.toByte(new FileInputStream(Path)), Base64.NO_WRAP));
                            jks.write(new Gson().toJson(keyInfo).getBytes());
                            jks.flush();
                            jks.close();
                            signer.add(0, jksFile);
                            adapter.notifyItemInserted(0);
                            dialog.dismiss();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(requireActivity(), R.string.jks_pass_fail, Toast.LENGTH_LONG).show();
                        }
                    }
                });
        Objects.requireNonNull(jksdialog.getWindow()).clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e(TAG, requestCode + " " + resultCode);
        if (data != null) {
            if (requestCode == 8000 && resultCode == 100) {
                String Path = data.getStringExtra("path");
                if (Path != null)
                    ImportJKS(Path);
            }
        }
    }
}
