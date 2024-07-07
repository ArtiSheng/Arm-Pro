/*
 * Copyright (c) 2020. Armadillo
 */

package armadillo.studio.activity;

import android.annotation.SuppressLint;
import android.content.pm.PackageInfo;
import android.content.pm.Signature;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.qiniu.android.storage.UploadOptions;

import org.jetbrains.annotations.NotNull;
import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.iface.ClassDef;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.security.cert.X509Certificate;

import armadillo.studio.CloudApp;
import armadillo.studio.R;
import armadillo.studio.adapter.ActivityAdapter;
import armadillo.studio.adapter.HandleAdapter;
import armadillo.studio.adapter.TreeAdapter;
import armadillo.studio.common.axml.ManifestParse;
import armadillo.studio.common.base.BaseActivity;
import armadillo.studio.common.base.callback.SocketCallBack;
import armadillo.studio.common.base.callback.TreeNodeCall;
import armadillo.studio.common.log.logger;
import armadillo.studio.common.utils.AppUtils;
import armadillo.studio.common.utils.ArchiveZip;
import armadillo.studio.common.utils.MD5Utils;
import armadillo.studio.common.utils.Tree;
import armadillo.studio.common.utils.TreeHelper;
import armadillo.studio.common.utils.dputil;
import armadillo.studio.data.InjectData;
import armadillo.studio.helper.InjectInfo;
import armadillo.studio.helper.SocketHelper;
import armadillo.studio.model.Basic;
import armadillo.studio.model.handle.ConfigRule;
import armadillo.studio.model.handle.HandleNode;
import armadillo.studio.model.handle.Node;
import armadillo.studio.model.handle.ResourceRule;
import armadillo.studio.model.sys.Task;
import armadillo.studio.model.sys.Upload;
import armadillo.studio.model.task.TaskInfo;
import armadillo.studio.model.tree.TreeNode;
import armadillo.studio.widget.LoadingDialog;
import armadillo.studio.widget.RoundCornerDialog;
import butterknife.BindInt;
import butterknife.BindView;
import butterknife.OnClick;

public class Handle extends BaseActivity<List<Object>> {
    @BindView(R.id.recycler)
    RecyclerView recycler;
    @BindView(R.id.submit)
    FloatingActionButton submit;
    @BindInt(R.integer.upload_max)
    int max;
    private HandleAdapter<?> handleAdapter;
    private final JsonObject rule = new JsonObject();
    private String Path;
    private volatile boolean isCancel;

    @Override
    protected int BindXML() {
        return R.layout.activity_handle;
    }

    @OnClick(R.id.submit)
    @SuppressLint("InflateParams")
    public void OnClick(View clickView) {
        if (handleAdapter.getSingleIndex() != -1) {
            Object child = getData().get(handleAdapter.getSingleIndex());
            if (child instanceof Node) {
                final Node node = (Node) child;
                if (node.getChild() != null && node.getChild().size() > 0) {
                    List<RadioButton> radioButtons = new ArrayList<>();
                    for (Node item : node.getChild())
                        addRadioButton(radioButtons, String.format("%s(%s)", item.getName(), item.getDesc()));
                    RoundCornerDialog roundCornerDialog = new RoundCornerDialog(this)
                            .SetView(R.layout.item_radiogroup)
                            .AddChildClickViewIds(R.id.Next)
                            .Show()
                            .SetOnChildClickListener((view, dialog) -> {
                                for (int i = 0; i < radioButtons.size(); i++) {
                                    RadioButton radioButton = radioButtons.get(i);
                                    if (radioButton.isChecked() && !node.getChild().get(i).isSeleteActivity()) {
                                        Node ChildNode = node.getChild().get(i);
                                        logger.d(String.format("当前选中子节点:%s", radioButton.getText()));
                                        if (rule.has(Long.toString(node.getType())))
                                            rule.remove(Long.toString(node.getType()));
                                        rule.addProperty(Long.toString(node.getType()), ChildNode.getType());
                                        if (ChildNode.getConfigRule() != null && ChildNode.getConfigRule().size() > 0) {
                                            logger.d(String.format("子节点:%s 配置规则", ChildNode.getName()));
                                            ShowConfigRule(ChildNode.getConfigRule(), dialog, Lists.newArrayList(node, ChildNode));
                                        } else {
                                            dialog.dismiss();
                                            Submit(Lists.newArrayList(node, ChildNode));
                                        }
                                        return;
                                    } else if (radioButton.isChecked() && node.getChild().get(i).isSeleteActivity()) {
                                        logger.d(String.format("子节点:%s Activity选择", radioButton.getText()));
                                        try (ZipFile zipFile = new ZipFile(Path)) {
                                            Node item = node.getChild().get(i);
                                            RoundCornerDialog activity_selete = new RoundCornerDialog(this)
                                                    .SetView(R.layout.dialog_selete)
                                                    .SetText(R.id.title, R.string.selete_main_activity)
                                                    .Show();
                                            List<String> activitys = ManifestParse.parseManifestActivity(zipFile.getInputStream(new ZipEntry("AndroidManifest.xml")));
                                            RecyclerView recyclerView = activity_selete.getView().findViewById(R.id.recycler);
                                            ActivityAdapter activityAdapter = new ActivityAdapter(R.layout.item_selete_signer, activitys);
                                            recyclerView.setLayoutManager(new LinearLayoutManager(this));
                                            recyclerView.setHasFixedSize(true);
                                            recyclerView.setAdapter(activityAdapter);
                                            activityAdapter.addChildClickViewIds(R.id.cardview);
                                            activityAdapter.setOnItemChildClickListener((adapter, view2, position) -> {
                                                if (rule.has(Long.toString(node.getType())))
                                                    rule.remove(Long.toString(node.getType()));
                                                rule.addProperty(Long.toString(node.getType()), item.getType());
                                                if (rule.has("ActivityClass"))
                                                    rule.remove("ActivityClass");
                                                rule.addProperty("ActivityClass", activitys.get(position));
                                                activity_selete.dismiss();
                                                if (item.getConfigRule() != null && item.getConfigRule().size() > 0) {
                                                    ShowConfigRule(item.getConfigRule(), dialog, Lists.newArrayList(node, item));
                                                } else {
                                                    dialog.dismiss();
                                                    Submit(Lists.newArrayList(node, item));
                                                }
                                            });
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            Toast.makeText(this, String.format(getString(R.string.exception), e.getMessage()), Toast.LENGTH_SHORT).show();
                                        }
                                        return;
                                    }
                                }
                                Toast.makeText(this, R.string.not_handle, Toast.LENGTH_LONG).show();
                            });
                    RadioGroup radioGroup = roundCornerDialog.getView().findViewById(R.id.RadioGroup);
                    for (RadioButton radioButton : radioButtons)
                        radioGroup.addView(radioButton);
                } else
                    Submit(Lists.newArrayList(node));
            }
        } else if (handleAdapter.getSelete().size() > 0)
            Submit(new ArrayList<>(handleAdapter.getSelete()));
        else
            Toast.makeText(this, R.string.not_handle, Toast.LENGTH_LONG).show();
    }

    @SuppressLint("InflateParams")
    private void ShowConfigRule(@NotNull List<ConfigRule> Rule, RoundCornerDialog dialog, List<Node> childNode) {
        List<View> views = new ArrayList<>();
        RoundCornerDialog model = new RoundCornerDialog(this)
                .SetView(R.layout.dialog_model)
                .AddChildClickViewIds(R.id.Next)
                .Show()
                .SetOnChildClickListener((view1, rule_dialog) -> {
                    for (View view : views) {
                        if (view instanceof TextInputLayout) {
                            TextInputLayout layout = (TextInputLayout) view;
                            if (Objects.requireNonNull(layout.getEditText()).getText().toString().isEmpty()) {
                                Toast.makeText(this, R.string.rule_not, Toast.LENGTH_LONG).show();
                                return;
                            }
                            rule.addProperty(layout.getTag().toString(), layout.getEditText().getText().toString());
                        } else if (view instanceof CheckBox) {
                            CheckBox checkBox = (CheckBox) view;
                            rule.addProperty(checkBox.getTag().toString(), checkBox.isChecked());
                        }
                    }
                    rule_dialog.dismiss();
                    dialog.dismiss();
                    Submit(childNode);
                });
        Objects.requireNonNull(model.getWindow()).clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        Objects.requireNonNull(model.getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        LinearLayout root_view = model.getView().findViewById(R.id.views);
        for (ConfigRule configRule : Rule) {
            if (configRule.isCheckbox()) {
                View RootView = LayoutInflater.from(this).inflate(R.layout.item_model_rule_checkbox, null);
                CheckBox checkBox = RootView.findViewById(R.id.checkBox);
                checkBox.setChecked(configRule.isChecked());
                checkBox.setText(configRule.getDesc());
                checkBox.setTag(configRule.getName());
                views.add(checkBox);
                root_view.addView(RootView);
            } else {
                View RootView = LayoutInflater.from(this).inflate(R.layout.item_model_rule_edittext, null);
                TextInputLayout InputLayout = RootView.findViewById(R.id.InputLayout);
                Objects.requireNonNull(InputLayout).setHint(configRule.getDesc());
                Objects.requireNonNull(InputLayout.getEditText()).setText(configRule.getDef());
                Objects.requireNonNull(InputLayout).setTag(configRule.getName());
                views.add(InputLayout);
                root_view.addView(RootView);
            }
        }
    }

    private void addRadioButton(@NotNull List<RadioButton> radioButtons, String format) {
        RadioButton radioButton = new RadioButton(this);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, dputil.dip2px(this, 5), 0, 0);
        radioButton.setText(format);
        radioButton.setLayoutParams(lp);
        radioButtons.add(radioButton);
    }

    @SuppressLint({"SetTextI18n", "InflateParams"})
    private void InitListener() {
        if (handleAdapter != null)
            handleAdapter.setOnItemChildClickListener((adapter, view, position) -> {
                logger.d(String.format("当前配置规则:%s", new Gson().toJson(rule)));
                final Object child = data.get(position);
                if (child instanceof HandleNode) {
                    final HandleNode handleNode = (HandleNode) child;
                    if (handleNode.isExpand()) {
                        for (Node item : handleNode.getChild())
                            data.remove(item);
                        handleAdapter.notifyItemRangeRemoved(position + 1, handleNode.getChild().size());
                    } else {
                        for (Node item : handleNode.getChild())
                            item.setParent(handleNode);
                        data.addAll(position + 1, handleNode.getChild());
                        handleAdapter.notifyItemRangeInserted(position + 1, handleNode.getChild().size());
                    }
                    handleNode.setExpand(!handleNode.isExpand());
                } else if (child instanceof Node) {
                    final Node node = (Node) child;
                    if (node.getType() == -1) {
                        Toast.makeText(this, R.string.test_handle, Toast.LENGTH_LONG).show();
                        return;
                    }
                    if (node.getParent().isSingle()) {
                        if (node.isSelected())
                            return;
                        node.setSelected(true);
                        final RadioButton radio = (RadioButton) handleAdapter.getViewByPosition(position, R.id.radio);
                        if (radio != null && !radio.isChecked())
                            radio.setChecked(true);
                        UpdateSingleSelete(node, position);
                        UpdateMultipleSelete();
                    } else {
                        final CheckBox checkbox = (CheckBox) handleAdapter.getViewByPosition(position, R.id.checked);
                        if (checkbox != null) {
                            checkbox.setChecked(!checkbox.isChecked());
                            if (checkbox.isChecked())
                                handleAdapter.getSelete().add(node);
                            else
                                handleAdapter.getSelete().remove(node);
                        }
                        if (handleAdapter.getSingleIndex() != -1)
                            UpdateSingleSelete(node, -1);
                    }
                    if (node.isSeleteClass()) {
                        if (!node.getParent().isSingle() && !handleAdapter.getSelete().contains(node)) {
                            RemoveRule(node);
                            return;
                        }
                        RoundCornerDialog roundCornerDialog = new RoundCornerDialog(this)
                                .SetView(R.layout.dialog_class_selete)
                                .Show();
                        Objects.requireNonNull(roundCornerDialog.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                        RecyclerView rv = roundCornerDialog.getView().findViewById(R.id.recycler);
                        List<TreeNode> root = new ArrayList<>();
                        TreeAdapter class_adapter = new TreeAdapter(R.layout.item_class_dir, root);
                        class_adapter.addChildClickViewIds(R.id.root);
                        class_adapter.setOnCheckedChangeListener((Position, Checked) -> {
                            TreeHelper.ChooseNode(class_adapter, root, root.get(Position), Checked);
                        });
                        class_adapter.setOnItemChildClickListener((adapter1, viewmode, class_position) -> {
                            if (viewmode.getId() == R.id.root) {
                                TreeNode treeNode = root.get(class_position);
                                if (!treeNode.isClass()) {
                                    /**
                                     * 是否有子节点
                                     */
                                    if (treeNode.isChild()) {
                                        /**
                                         * 展开状态就关闭
                                         */
                                        if (treeNode.isExpand()) {
                                            List<TreeNode> list = new ArrayList<>();
                                            TreeHelper.CloseNode(root.get(class_position), list);
                                            for (TreeNode item : list)
                                                item.setExpand(false);
                                            int index = 0;
                                            for (TreeNode item : list) {
                                                if (root.remove(item))
                                                    index++;
                                            }
                                            viewmode.findViewById(R.id.iv_key).setRotation(0);
                                            class_adapter.notifyItemRangeRemoved(class_position + 1, index);
                                        }
                                        /**
                                         * 关闭状态就展开
                                         */
                                        else {
                                            root.addAll(class_position + 1, treeNode.getChild());
                                            viewmode.findViewById(R.id.iv_key).setRotation(45);
                                            class_adapter.notifyItemRangeInserted(class_position + 1, treeNode.getChild().size());
                                        }
                                        treeNode.setExpand(!treeNode.isExpand());
                                    }
                                } else {
                                    treeNode.setChoose(!treeNode.isChoose());
                                    MaterialCheckBox checkBox = viewmode.findViewById(R.id.checkBox);
                                    checkBox.setChecked(treeNode.isChoose());
                                }
                            }
                        });
                        rv.setLayoutManager(new LinearLayoutManager(this));
                        rv.setHasFixedSize(true);
                        rv.setAdapter(class_adapter);
                        /**
                         * 点击事件
                         */
                        roundCornerDialog.getView().findViewById(R.id.Next).setOnClickListener(v -> {
                            final HashSet<String> strings = new HashSet<>();
                            TreeHelper.GetSeleteNode(root, strings);
                            final JsonArray array = new JsonArray();
                            for (String string : strings)
                                array.add(string);
                            rule.remove(Long.toString(node.getType()));
                            rule.add(Long.toString(node.getType()), array);
                            roundCornerDialog.dismiss();
                        });
                        /**
                         * 销毁监听
                         */
                        roundCornerDialog.setOnDismissListener(dialog1 -> {
                            final HashSet<String> strings = new HashSet<>();
                            TreeHelper.GetSeleteNode(root, strings);
                            if (rule.getAsJsonArray(Long.toString(node.getType())) == null
                                    || rule.getAsJsonArray(Long.toString(node.getType())).size() == 0
                                    || strings.size() == 0) {
                                rule.remove(Long.toString(node.getType()));
                                if (node.getParent().isSingle()) {
                                    node.setSelected(false);
                                    RadioButton radio = (RadioButton) handleAdapter.getViewByPosition(position, R.id.radio);
                                    if (radio != null) {
                                        radio.setChecked(false);
                                        handleAdapter.setSingleIndex(-1);
                                    }
                                } else {
                                    CheckBox checkbox = (CheckBox) handleAdapter.getViewByPosition(position, R.id.checked);
                                    if (checkbox != null) {
                                        checkbox.setChecked(false);
                                        handleAdapter.getSelete().remove(node);
                                    }
                                }
                            }
                        });
                        /**
                         * 数据加载
                         */
                        LoadingDialog.getInstance().show(this);
                        CloudApp.getCachedThreadPool().execute(() -> {
                            try (ZipFile zipFile = new ZipFile(Path)) {
                                HashMap<String, HashSet<ClassDef>> list = new HashMap<>();
                                Enumeration<? extends ZipEntry> entries = zipFile.entries();
                                while (entries.hasMoreElements()) {
                                    ZipEntry zipEntry = entries.nextElement();
                                    for (ResourceRule resourceRule : node.getSeleteClassRule()) {
                                        if (resourceRule.getName() != null && zipEntry.getName().equals(resourceRule.getName())) {
                                            DexBackedDexFile dexBackedDexFile = DexBackedDexFile.fromInputStream(null, new BufferedInputStream(zipFile.getInputStream(zipEntry)));
                                            list.put(zipEntry.getName(), new HashSet<>(dexBackedDexFile.getClasses()));
                                        } else if (resourceRule.getStartWith() != null
                                                && resourceRule.getEndWith() == null
                                                && zipEntry.getName().startsWith(resourceRule.getStartWith())) {
                                            DexBackedDexFile dexBackedDexFile = DexBackedDexFile.fromInputStream(null, new BufferedInputStream(zipFile.getInputStream(zipEntry)));
                                            list.put(zipEntry.getName(), new HashSet<>(dexBackedDexFile.getClasses()));
                                        } else if (resourceRule.getEndWith() != null
                                                && resourceRule.getStartWith() == null
                                                && zipEntry.getName().endsWith(resourceRule.getEndWith())) {
                                            DexBackedDexFile dexBackedDexFile = DexBackedDexFile.fromInputStream(null, new BufferedInputStream(zipFile.getInputStream(zipEntry)));
                                            list.put(zipEntry.getName(), new HashSet<>(dexBackedDexFile.getClasses()));
                                        } else if (resourceRule.getEndWith() != null
                                                && resourceRule.getStartWith() != null
                                                && zipEntry.getName().startsWith(resourceRule.getStartWith())
                                                && zipEntry.getName().endsWith(resourceRule.getEndWith())) {
                                            DexBackedDexFile dexBackedDexFile = DexBackedDexFile.fromInputStream(null, new BufferedInputStream(zipFile.getInputStream(zipEntry)));
                                            list.put(zipEntry.getName(), new HashSet<>(dexBackedDexFile.getClasses()));
                                        }
                                    }
                                }
                                new Tree(list, new TreeNodeCall<List<TreeNode>>() {
                                    @Override
                                    public void BindData(List<TreeNode> data) {
                                        new Handler(Looper.getMainLooper()).post(() -> {
                                            LoadingDialog.getInstance().hide();
                                            root.addAll(data);
                                            class_adapter.notifyDataSetChanged();
                                        });
                                    }

                                    @Override
                                    public void Loading() {
                                    }

                                    @Override
                                    public void Error(Throwable throwable) {
                                        new Handler(Looper.getMainLooper()).post(() -> {
                                            LoadingDialog.getInstance().hide();
                                            Toast.makeText(Handle.this, String.format(getString(R.string.exception), throwable.getMessage()), Toast.LENGTH_LONG).show();
                                        });
                                    }
                                });
                            } catch (IOException e) {
                                e.printStackTrace();
                                new Handler(Looper.getMainLooper()).post(() -> {
                                    LoadingDialog.getInstance().hide();
                                    roundCornerDialog.dismiss();
                                    Toast.makeText(Handle.this, String.format(getString(R.string.exception), e.getMessage()), Toast.LENGTH_LONG).show();
                                });
                            }
                        });
                    } else if (node.getConfigRule() != null && node.getConfigRule().size() > 0) {
                        final List<TextInputLayout> views = new ArrayList<>();
                        final RoundCornerDialog model = new RoundCornerDialog(this)
                                .SetView(R.layout.dialog_model)
                                .AddChildClickViewIds(R.id.Next)
                                .Show()
                                .SetOnChildClickListener((view1, rule_dialog) -> {
                                    for (TextInputLayout layout : views) {
                                        if (Objects.requireNonNull(layout.getEditText()).getText().toString().isEmpty()) {
                                            Toast.makeText(this, R.string.rule_not, Toast.LENGTH_LONG).show();
                                            return;
                                        }
                                        rule.addProperty(layout.getTag().toString(), layout.getEditText().getText().toString());
                                    }
                                    rule_dialog.dismiss();
                                });
                        model.setOnDismissListener(dialog -> {
                            if (!rule.has(node.getConfigRule().get(0).getName())) {
                                if (node.getParent().isSingle()) {
                                    node.setSelected(false);
                                    final RadioButton radio = (RadioButton) handleAdapter.getViewByPosition(position, R.id.radio);
                                    if (radio != null) {
                                        radio.setChecked(false);
                                        handleAdapter.setSingleIndex(-1);
                                    }
                                } else {
                                    final CheckBox checkbox = (CheckBox) handleAdapter.getViewByPosition(position, R.id.checked);
                                    if (checkbox != null) {
                                        checkbox.setChecked(false);
                                        handleAdapter.getSelete().remove(node);
                                    }
                                }
                            }
                        });
                        Objects.requireNonNull(model.getWindow()).clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
                        Objects.requireNonNull(model.getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                        final LinearLayout root_view = model.getView().findViewById(R.id.views);
                        for (ConfigRule configRule : node.getConfigRule()) {
                            View InputView = LayoutInflater.from(this).inflate(R.layout.item_model_rule_edittext, null);
                            TextInputLayout InputLayout = InputView.findViewById(R.id.InputLayout);
                            Objects.requireNonNull(InputLayout).setHint(configRule.getName() + (configRule.getDesc().isEmpty() ? "" : "(" + configRule.getDesc() + ")"));
                            Objects.requireNonNull(InputLayout.getEditText()).setText(configRule.getDef());
                            Objects.requireNonNull(InputLayout).setTag(configRule.getName());
                            views.add(InputLayout);
                            root_view.addView(InputView);
                        }
                    }
                }
            });
        recycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (submit != null) {
                    if (dy > dx)
                        submit.hide();
                    else
                        submit.show();
                }
            }
        });

    }

    private void UpdateSingleSelete(Node node, int position) {
        for (Object item : data) {
            if (item instanceof HandleNode) {
                HandleNode handleNode = (HandleNode) item;
                for (Node next : handleNode.getChild()) {
                    if (next != node && next.isSelected()) {
                        next.setSelected(false);
                        RemoveRule(next);
                        int next_position = handleAdapter.getNodePosition(next);
                        if (next_position != -1) {
                            if (recycler.isComputingLayout())
                                recycler.post(() -> handleAdapter.notifyItemChanged(next_position));
                            else
                                handleAdapter.notifyItemChanged(next_position);
                        }
                    }
                }
            }
        }
        handleAdapter.setSingleIndex(position);
    }

    private void UpdateMultipleSelete() {
        if (handleAdapter.getSelete().size() > 0) {
            Iterator<Node> iterator = handleAdapter.getSelete().iterator();
            while (iterator.hasNext()) {
                Node next = iterator.next();
                RemoveRule(next);
                int next_position = handleAdapter.getNodePosition(next);
                iterator.remove();
                if (next_position != -1) {
                    if (recycler.isComputingLayout())
                        recycler.post(() -> handleAdapter.notifyItemChanged(next_position));
                    else
                        handleAdapter.notifyItemChanged(next_position);
                }
            }
        }
    }

    private void RemoveRule(@NotNull Node node) {
        if (rule.has(Long.toString(node.getType())))
            rule.remove(Long.toString(node.getType()));
        if (node.getConfigRule() != null && node.getConfigRule().size() > 0) {
            for (ConfigRule configRule : node.getConfigRule()) {
                if (rule.has(configRule.getName()))
                    rule.remove(configRule.getName());
            }
        }
    }

    @Override
    protected boolean AutoLoadData() {
        return true;
    }

    @Override
    public void BindData(@NotNull List<Object> data) {
        for (int i = 0; i < data.size(); i++) {
            Object o = data.get(i);
            if (o instanceof HandleNode) {
                HandleNode handleNode = (HandleNode) o;
                for (Node node1 : handleNode.getChild())
                    node1.setParent(handleNode);
                handleNode.setExpand(true);
                data.addAll(i + 1, handleNode.getChild());
            }
        }
        handleAdapter = new HandleAdapter<>(R.layout.item_handle, data);
        handleAdapter.setAnimationEnable(true);
        handleAdapter.setAnimationFirstOnly(false);
        handleAdapter.setAnimationWithDefault(BaseQuickAdapter.AnimationType.AlphaIn);
        handleAdapter.setFooterView(LayoutInflater.from(this).inflate(R.layout.status_footer, null));
        handleAdapter.addChildClickViewIds(R.id.cardview);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setHasFixedSize(true);
        recycler.setAdapter(handleAdapter);
        Path = getIntent().getStringExtra("Path");
        if (Path == null)
            onError(new Exception());
        else
            InitListener();
    }

    @Override
    public void onError(@NonNull Throwable throwable) {
        Toast.makeText(this, R.string.loading_data_error, Toast.LENGTH_LONG).show();
        onBackPressed();
    }

    private void Submit(final List<Node> nodes) {
        LoadingDialog.getInstance().show(this);
        CloudApp.getCachedThreadPool().execute(() -> {
            HashSet<ResourceRule> resourceRules = new HashSet<>();
            for (Node node : nodes) {
                if (node.getResourceRule() != null && node.getResourceRule().size() > 0)
                    resourceRules.addAll(node.getResourceRule());
            }
            InjectData injectData = ArchiveZip.AutoArchive(resourceRules, Path);
            /**
             * 校验必要数据
             */
            if (!injectData.isOk()) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    LoadingDialog.getInstance().hide();
                    Toast.makeText(this, String.format(getString(R.string.exception), injectData.getThrowable().getMessage()), Toast.LENGTH_SHORT).show();
                });
                return;
            } else if (injectData.getSize() > max) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    LoadingDialog.getInstance().hide();
                    Toast.makeText(this, R.string.upload_max, Toast.LENGTH_LONG).show();
                });
                return;
            }
            for (Node node : nodes) {
                if (node.isReadApkName()) {
                    if (rule != null) {
                        if (!rule.has("name")) {
                            PackageInfo info = AppUtils.GetPackageInfo(Path);
                            rule.addProperty("name", Objects.requireNonNull(info).applicationInfo.loadLabel(CloudApp.getContext().getPackageManager()).toString());
                        }
                    }
                }
                if (node.isReadSigner()) {
                    if (rule != null) {
                        if (!rule.has("app_signer")) {
                            try {
                                PackageInfo info = AppUtils.GetPackageInfoSigner(Path);
                                Signature[] signatures = Objects.requireNonNull(info).signatures;
                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                DataOutputStream dos = new DataOutputStream(baos);
                                dos.write(signatures.length);
                                for (Signature signature : signatures) {
                                    X509Certificate x509 = X509Certificate.getInstance(signature.toByteArray());
                                    byte[] x509Encoded = x509.getEncoded();
                                    dos.writeInt(x509Encoded.length);
                                    dos.write(x509Encoded);
                                }
                                rule.addProperty("app_signer", Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP));
                                Log.e(TAG, new Gson().toJson(rule));
                                baos.close();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
            try {
                if (injectData.getOtherRule().size() > 0 && rule != null) {
                    for (Map.Entry<String, JsonElement> entry : injectData.getOtherRule().entrySet()) {
                        rule.add(entry.getKey(), entry.getValue());
                    }
                }
                final List<Node> handleNode = new ArrayList<>();
                for (Node node : nodes) {
                    if (node.getParent() != null)
                        handleNode.add(node);
                }
                final InjectInfo injectInfo = new InjectInfo(
                        handleNode,
                        null,
                        rule,
                        MD5Utils.encryptionMD5(injectData.getFile().get() == null ? new File(Path) : injectData.getFile().get()));
                SocketHelper.SysHelper.GetUploadToken(new SocketCallBack<Upload>() {
                    @Override
                    public void next(final Upload body) {
                        if (body.getCode() == 200) {
                            /**
                             * 缓存任务
                             */
                            if (body.getData().isCache()) {
                                LoadingDialog.getInstance().hide();
                                final TaskInfo info = new TaskInfo(new File(Path), body.getData().getUuid(), injectInfo.getHandleEnums());
                                try (FileOutputStream fileOutputStream = new FileOutputStream(new File(System.getProperty("task.dir"), body.getData().getUuid()))) {
                                    fileOutputStream.write(new Gson().toJson(info).getBytes());
                                } catch (Exception e) {
                                    Toast.makeText(Handle.this, String.format(getString(R.string.exception), e.getMessage()), Toast.LENGTH_SHORT).show();
                                    return;
                                }
                                setResult(200);
                                finish();
                            }
                            /**
                             * 开始上传
                             */
                            else {
                                isCancel = false;
                                LoadingDialog.getInstance().showCancelProgress(Handle.this, getString(R.string.task_upload), (dialogInterface, i) -> {
                                    isCancel = true;
                                    LoadingDialog.getInstance().hide();
                                });
                                /**
                                 * 本地上传
                                 */
                                if ("LocalUpload".equals(body.getData().getToken())) {
                                    SocketHelper.SysHelper.Upload(new SocketCallBack<Basic>() {
                                                                      @Override
                                                                      public void next(Basic basic) {
                                                                          if (injectData.getFile().get() != null)
                                                                              injectData.getFile().get().delete();
                                                                          if (basic.getCode() == 200) {
                                                                              injectInfo.setUuid(body.getData().getUuid());
                                                                              SocketHelper.SysHelper.SubmitTask(
                                                                                      new SocketCallBack<Task>() {
                                                                                          @Override
                                                                                          public void next(Task body) {
                                                                                              LoadingDialog.getInstance().hide();
                                                                                              if (body.getCode() == 200) {
                                                                                                  TaskInfo info = new TaskInfo(new File(Path), body.getData(), injectInfo.getHandleEnums());
                                                                                                  try (FileOutputStream fileOutputStream = new FileOutputStream(new File(System.getProperty("task.dir"), body.getData()))) {
                                                                                                      fileOutputStream.write(new Gson().toJson(info).getBytes());
                                                                                                  } catch (IOException e) {
                                                                                                      Toast.makeText(Handle.this, String.format(getString(R.string.exception), e.getMessage()), Toast.LENGTH_LONG).show();
                                                                                                      return;
                                                                                                  }
                                                                                                  setResult(200);
                                                                                                  finish();
                                                                                              } else
                                                                                                  Toast.makeText(Handle.this, body.getMsg(), Toast.LENGTH_LONG).show();
                                                                                          }

                                                                                          @Override
                                                                                          public void error(Throwable e) {
                                                                                              e.printStackTrace();
                                                                                              LoadingDialog.getInstance().hide();
                                                                                              Toast.makeText(Handle.this, String.format(getString(R.string.exception), e.getMessage()), Toast.LENGTH_LONG).show();
                                                                                          }
                                                                                      },
                                                                                      injectInfo,
                                                                                      Objects.requireNonNull(injectData.getPackName()));
                                                                          } else {
                                                                              LoadingDialog.getInstance().hide();
                                                                              Toast.makeText(Handle.this, String.format(getString(R.string.exception), basic.getMsg()), Toast.LENGTH_SHORT).show();
                                                                          }
                                                                      }

                                                                      @Override
                                                                      public void error(Throwable throwable) {
                                                                          LoadingDialog.getInstance().hide();
                                                                          Toast.makeText(Handle.this, String.format(getString(R.string.exception), throwable.getMessage()), Toast.LENGTH_SHORT).show();
                                                                      }
                                                                  },
                                            injectData.getFile().get() == null ? new File(Path) : injectData.getFile().get(),
                                            body.getData().getUuid(),
                                            progress -> LoadingDialog.getInstance().setProgress(progress),
                                            () -> isCancel);
                                }
                                /**
                                 * OSS七牛云上传
                                 */
                                else {
                                    CloudApp.getUploadManager().put(
                                            injectData.getFile().get() == null ? new File(Path) : injectData.getFile().get(),
                                            body.getData().getUuid(),
                                            body.getData().getToken(),
                                            (key, info, response) -> {
                                                if (injectData.getFile().get() != null)
                                                    injectData.getFile().get().delete();
                                                if (info.isOK()) {
                                                    injectInfo.setUuid(body.getData().getUuid());
                                                    SocketHelper.SysHelper.SubmitTask(
                                                            new SocketCallBack<Task>() {
                                                                @Override
                                                                public void next(Task body) {
                                                                    LoadingDialog.getInstance().hide();
                                                                    if (body.getCode() == 200) {
                                                                        TaskInfo info = new TaskInfo(new File(Path), body.getData(), injectInfo.getHandleEnums());
                                                                        try (FileOutputStream fileOutputStream = new FileOutputStream(new File(System.getProperty("task.dir"), body.getData()))) {
                                                                            fileOutputStream.write(new Gson().toJson(info).getBytes());
                                                                        } catch (IOException e) {
                                                                            Toast.makeText(Handle.this, String.format(getString(R.string.exception), e.getMessage()), Toast.LENGTH_LONG).show();
                                                                            return;
                                                                        }
                                                                        setResult(200);
                                                                        finish();
                                                                    } else
                                                                        Toast.makeText(Handle.this, body.getMsg(), Toast.LENGTH_LONG).show();
                                                                }

                                                                @Override
                                                                public void error(Throwable e) {
                                                                    e.printStackTrace();
                                                                    LoadingDialog.getInstance().hide();
                                                                    Toast.makeText(Handle.this, String.format(getString(R.string.exception), e.getMessage()), Toast.LENGTH_LONG).show();
                                                                }
                                                            },
                                                            injectInfo,
                                                            Objects.requireNonNull(injectData.getPackName()));
                                                } else {
                                                    LoadingDialog.getInstance().hide();
                                                    Toast.makeText(Handle.this, String.format(getString(R.string.exception), info.error), Toast.LENGTH_SHORT).show();
                                                }
                                            },
                                            new UploadOptions(null, null, false, (key, percent) -> {
                                                LoadingDialog.getInstance().setProgress((int) (percent * 100));
                                            }, () -> isCancel));
                                }
                            }
                        } else {
                            LoadingDialog.getInstance().hide();
                            Toast.makeText(Handle.this, body.getMsg(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void error(Throwable throwable) {
                        LoadingDialog.getInstance().hide();
                        Toast.makeText(Handle.this, String.format(getString(R.string.exception), throwable.getMessage()), Toast.LENGTH_SHORT).show();
                    }
                }, injectInfo, Objects.requireNonNull(injectData).getPackName());
            } catch (Exception e) {
                e.printStackTrace();
                new Handler(Looper.getMainLooper()).post(() -> {
                    LoadingDialog.getInstance().hide();
                    Toast.makeText(this, String.format(getString(R.string.exception), e.getMessage()), Toast.LENGTH_SHORT).show();
                });
            }
        });
    }
}
