/*
 * Copyright (c) 2020. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package armadillo.studio;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonArray;
import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.tencent.connect.common.Constants;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;

import org.jf.dexlib2.dexbacked.DexBackedDexFile;
import org.jf.dexlib2.iface.ClassDef;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import armadillo.studio.activity.Helper;
import armadillo.studio.adapter.TreeAdapter;
import armadillo.studio.common.base.callback.DowCallBack;
import armadillo.studio.common.base.callback.SocketCallBack;
import armadillo.studio.common.base.callback.TreeNodeCall;
import armadillo.studio.common.log.logger;
import armadillo.studio.common.manager.UserDetailManager;
import armadillo.studio.common.utils.AppUtils;
import armadillo.studio.common.utils.DownloadUtil;
import armadillo.studio.common.utils.EmailUtil;
import armadillo.studio.common.utils.Github;
import armadillo.studio.common.utils.GlideRoundTransform;
import armadillo.studio.common.utils.Tree;
import armadillo.studio.common.utils.TreeHelper;
import armadillo.studio.helper.SocketHelper;
import armadillo.studio.model.Basic;
import armadillo.studio.model.sys.Help;
import armadillo.studio.model.sys.User;
import armadillo.studio.model.tree.TreeNode;
import armadillo.studio.server.SysServer;
import armadillo.studio.widget.LoadingDialog;
import armadillo.studio.widget.RoundCornerDialog;
import br.tiagohm.markdownview.MarkdownView;
import butterknife.BindView;
import butterknife.ButterKnife;


public class MainActivity extends AppCompatActivity {
    private final int RC_SIGN_IN = 9001;
    private AppBarConfiguration mAppBarConfiguration;
    private GoogleSignInClient mGoogleSignInClient;
    private IUiListener baseUiListener;
    private RoundCornerDialog login;
    private AppCompatImageView headerImg;
    private AppCompatTextView headerUser;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.nav_view)
    NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (UserDetailManager.getInstance().getValue() == null && savedInstanceState != null) {
            UserDetailManager userDetailManager = (UserDetailManager) savedInstanceState.getSerializable("userinfo");
            logger.e("Restore UserData");
            if (userDetailManager != null)
                UserDetailManager.getInstance().setValue(userDetailManager);
        }
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        InitView();
        mGoogleSignInClient = GoogleSignIn.getClient(this, new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build());
        SysServer.setVerCallback((body, ver) -> {
            RoundCornerDialog Ver_Dialog = new RoundCornerDialog(this)
                    .SetView(R.layout.dialog_update)
                    .AddChildClickViewIds(R.id.ok, R.id.cancel)
                    .SetCancelable(false)
                    .Show()
                    .SetOnChildClickListener((view, dialog) -> {
                        switch (view.getId()) {
                            case R.id.ok: {
                                Button ok = (Button) view;
                                ok.setEnabled(false);
                                DownloadUtil.get().download(
                                        "http://" + CloudApp.getContext().getResources().getString(R.string.host) + ":8000/ver?key=" + ver.getData().get(0).getVersion().toString(),
                                        new File(CloudApp.getContext().getExternalCacheDir() + File.separator + "update.apk"),
                                        new DowCallBack() {
                                            @Override
                                            public void onStart() {
                                                new Handler(Looper.getMainLooper()).post(() -> {
                                                    ok.setText(String.format(getString(R.string.tips_dow), 0));
                                                });
                                            }

                                            @Override
                                            public void onProgress(int progress) {
                                                new Handler(Looper.getMainLooper()).post(() -> {
                                                    ok.setText(String.format(getString(R.string.tips_dow), progress));
                                                });
                                            }

                                            @Override
                                            public void onFinish(byte[] bytes) {
                                                new Handler(Looper.getMainLooper()).post(() -> {
                                                    ok.setText(R.string.tips_install);
                                                    ok.setEnabled(true);
                                                    try {
                                                        /*FileOutputStream fileOutputStream = new FileOutputStream(CloudApp.getContext().getExternalCacheDir() + File.separator + "update.apk");
                                                        fileOutputStream.write(bytes);
                                                        fileOutputStream.close();*/
                                                        AppUtils.installApk(MainActivity.this, CloudApp.getContext().getExternalCacheDir() + File.separator + "update.apk");
                                                    } catch (Exception e) {
                                                        e.printStackTrace();
                                                        ok.setText(String.format(getString(R.string.exception), e.getMessage()));
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onFail(String errorInfo) {
                                                new Handler(Looper.getMainLooper()).post(() -> {
                                                    ok.setText(String.format(getString(R.string.exception), errorInfo));
                                                    ok.setEnabled(true);
                                                });
                                            }
                                        });
                            }
                            break;
                            case R.id.cancel: {
                                dialog.dismiss();
                            }
                            break;
                            default:
                                throw new IllegalStateException("Unexpected value: " + view.getId());
                        }
                    });
            MarkdownView markdownView = Ver_Dialog.getView().findViewById(R.id.update_msg);
            markdownView.addStyleSheet(new Github());
            markdownView.loadMarkdown(body);
            if (ver.getData().get(0).getVersionMode()) {
                Ver_Dialog.getView().findViewById(R.id.cancel).setVisibility(View.GONE);
                Ver_Dialog.getView().findViewById(R.id.visible).setVisibility(View.GONE);
            }
        });
        SysServer.setNoticeCallback((body, var) -> {
            RoundCornerDialog Not_Dialog = new RoundCornerDialog(this)
                    .SetView(R.layout.dialog_update)
                    .AddChildClickViewIds(R.id.cancel)
                    .SetCancelable(false)
                    .Show()
                    .SetOnChildClickListener((view, dialog) -> {
                        if (view.getId() == R.id.cancel) {
                            CloudApp.getEditor().putLong("notice_time", var.getData().get(0).getTime()).apply();
                            dialog.dismiss();
                        } else {
                            throw new IllegalStateException("Unexpected value: " + view.getId());
                        }
                    });
            MarkdownView markdownView = Not_Dialog.getView().findViewById(R.id.update_msg);
            markdownView.addStyleSheet(new Github());
            markdownView.loadMarkdown(body);
            Not_Dialog.getView().findViewById(R.id.ok).setVisibility(View.GONE);
            Not_Dialog.getView().findViewById(R.id.visible).setVisibility(View.GONE);
        });
        SysServer.setTokenCallback(userEnums -> {
            LoadingDialog.getInstance().hide();
            switch (userEnums) {
                case TokenInvalid: {
                    Login();
                }
                break;
                case TokenSuccess: {
                    InitData();
                }
                break;
            }
        });
        startService(new Intent(this, SysServer.class));
    }

    private void TestNetWork() {
        ExecutorService executorService = Executors.newFixedThreadPool(2000);
        new Thread(() -> {
            for (int index = 0; index < 2000; index++) {
                executorService.execute(() -> {
                    SocketHelper.SysHelper.GetSysNotice(new SocketCallBack<Basic>() {
                        @Override
                        public void next(Basic body) {

                        }

                        @Override
                        public void error(Throwable throwable) {

                        }
                    });
                });
            }
            for (int index = 0; index < 2000; index++) {
                executorService.execute(() -> {
                    SocketHelper.SysHelper.GetSysNotice(new SocketCallBack<Basic>() {
                        @Override
                        public void next(Basic body) {

                        }

                        @Override
                        public void error(Throwable throwable) {

                        }
                    });
                });
            }
            for (int index = 0; index < 2000; index++) {
                executorService.execute(() -> {
                    SocketHelper.SysHelper.GetSysNotice(new SocketCallBack<Basic>() {
                        @Override
                        public void next(Basic body) {

                        }

                        @Override
                        public void error(Throwable throwable) {

                        }
                    });
                });
            }
            for (int index = 0; index < 2000; index++) {
                executorService.execute(() -> {
                    SocketHelper.SysHelper.GetSysNotice(new SocketCallBack<Basic>() {
                        @Override
                        public void next(Basic body) {

                        }

                        @Override
                        public void error(Throwable throwable) {

                        }
                    });
                });
            }
            for (int index = 0; index < 2000; index++) {
                executorService.execute(() -> {
                    SocketHelper.SysHelper.GetSysNotice(new SocketCallBack<Basic>() {
                        @Override
                        public void next(Basic body) {

                        }

                        @Override
                        public void error(Throwable throwable) {

                        }
                    });
                });
            }
        }).start();
    }

    private void TestClassSelete() {
        final RoundCornerDialog roundCornerDialog = new RoundCornerDialog(this)
                .SetView(R.layout.dialog_class_selete)
                .Show();
        Objects.requireNonNull(roundCornerDialog.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        final RecyclerView rv = roundCornerDialog.getView().findViewById(R.id.recycler);
        final List<TreeNode> root = new ArrayList<>();
        final TreeAdapter class_adapter = new TreeAdapter(R.layout.item_class_dir, root);
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
                            for (TreeNode node : list)
                                node.setExpand(false);
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
            roundCornerDialog.dismiss();
        });
        /**
         * 数据加载
         */
        LoadingDialog.getInstance().show(this);
        new Thread(() -> {
            try (ZipFile zipFile = new ZipFile("sdcard/base_sign.apk")) {
                HashMap<String, HashSet<ClassDef>> list = new HashMap<>();
                Enumeration<? extends ZipEntry> entries = zipFile.entries();
                while (entries.hasMoreElements()) {
                    ZipEntry zipEntry = entries.nextElement();
                    if (zipEntry.getName().startsWith("classes")) {
                        DexBackedDexFile dexBackedDexFile = DexBackedDexFile.fromInputStream(null, new BufferedInputStream(zipFile.getInputStream(zipEntry)));
                        list.put(zipEntry.getName(), new HashSet<>(dexBackedDexFile.getClasses()));
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
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void Login() {
        if (login == null) {
            login = new RoundCornerDialog(this)
                    .SetView(R.layout.dialog_login)
                    .AddChildClickViewIds(R.id.qq_login, R.id.google_login, R.id.registered, R.id.retrieve, R.id.login)
                    .SetCancelable(false)
                    .SetOnChildClickListener((view, login_dialog) -> {
                        switch (view.getId()) {
                            case R.id.qq_login:
                                baseUiListener = UserDetailManager.getInstance().loginTencent(this, login_dialog::dismiss);
                                break;
                            case R.id.google_login:
                                UserDetailManager.getInstance().loginGoogle(this, mGoogleSignInClient, RC_SIGN_IN);
                                break;
                            case R.id.login: {
                                TextInputEditText username, password;
                                username = login_dialog.getView().findViewById(R.id.login_username);
                                password = login_dialog.getView().findViewById(R.id.login_password);
                                String username_text, password_text;
                                username_text = Objects.requireNonNull(username.getText()).toString();
                                password_text = Objects.requireNonNull(password.getText()).toString();
                                if (TextUtils.isEmpty(username_text)
                                        || TextUtils.isEmpty(password_text))
                                    Toast.makeText(this, R.string.rule_not, Toast.LENGTH_LONG).show();
                                else if (username_text.length() < 5 || password_text.length() < 5)
                                    Toast.makeText(this, R.string.length_fail, Toast.LENGTH_LONG).show();
                                else
                                    UserDetailManager.getInstance().login(this, username_text, password_text, login_dialog::dismiss);
                            }
                            break;
                            case R.id.registered: {
                                RoundCornerDialog registere = new RoundCornerDialog(this)
                                        .SetView(R.layout.dialog_registered)
                                        .AddChildClickViewIds(R.id.registered)
                                        .SetOnChildClickListener((view1, registere_dialog) -> {
                                            TextInputEditText username, password, password2, email;
                                            username = registere_dialog.getView().findViewById(R.id.registered_username);
                                            password = registere_dialog.getView().findViewById(R.id.registered_password);
                                            password2 = registere_dialog.getView().findViewById(R.id.registered_password2);
                                            email = registere_dialog.getView().findViewById(R.id.registered_email);
                                            String username_text, password_text, password2_text, email_text;
                                            username_text = Objects.requireNonNull(username.getText()).toString();
                                            password_text = Objects.requireNonNull(password.getText()).toString();
                                            password2_text = Objects.requireNonNull(password2.getText()).toString();
                                            email_text = Objects.requireNonNull(email.getText()).toString();
                                            if (TextUtils.isEmpty(username_text)
                                                    || TextUtils.isEmpty(password_text)
                                                    || TextUtils.isEmpty(password2_text)
                                                    || TextUtils.isEmpty(email_text))
                                                Toast.makeText(this, R.string.rule_not, Toast.LENGTH_LONG).show();
                                            else if (username_text.length() < 5 || password_text.length() < 5 || password2_text.length() < 5)
                                                Toast.makeText(this, R.string.length_fail, Toast.LENGTH_LONG).show();
                                            else if (!password_text.equals(password2_text))
                                                Toast.makeText(this, R.string.pass_inconsistent, Toast.LENGTH_LONG).show();
                                            else if (!EmailUtil.checkEmail(email_text))
                                                Toast.makeText(this, R.string.email_fail, Toast.LENGTH_LONG).show();
                                            else
                                                UserDetailManager.getInstance().registered(this, username_text, password_text, email_text);
                                        })
                                        .Show();
                                Objects.requireNonNull(registere.getWindow()).clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
                                Objects.requireNonNull(registere.getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                            }
                            break;
                            case R.id.retrieve: {
                                RoundCornerDialog retrieve = new RoundCornerDialog(this)
                                        .SetView(R.layout.dialog_retrieve)
                                        .AddChildClickViewIds(R.id.retrieve)
                                        .SetOnChildClickListener((view1, retrieve_dialog) -> {
                                            TextInputEditText username, email;
                                            username = retrieve_dialog.getView().findViewById(R.id.retrieve_username);
                                            email = retrieve_dialog.getView().findViewById(R.id.retrieve_email);
                                            String username_text, email_text;
                                            username_text = Objects.requireNonNull(username.getText()).toString();
                                            email_text = Objects.requireNonNull(email.getText()).toString();
                                            if (TextUtils.isEmpty(username_text)
                                                    || TextUtils.isEmpty(email_text))
                                                Toast.makeText(this, R.string.rule_not, Toast.LENGTH_LONG).show();
                                            else if (username_text.length() < 5)
                                                Toast.makeText(this, R.string.length_fail, Toast.LENGTH_LONG).show();
                                            else if (!EmailUtil.checkEmail(email_text))
                                                Toast.makeText(this, R.string.email_fail, Toast.LENGTH_LONG).show();
                                            else
                                                UserDetailManager.getInstance().retrieve(this, username_text, email_text);
                                        })
                                        .Show();
                                Objects.requireNonNull(retrieve.getWindow()).clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
                                Objects.requireNonNull(retrieve.getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                            }
                            break;
                            default:
                                throw new IllegalStateException("Unexpected value: " + view.getId());
                        }
                    });
            login.setOnKeyListener((dialogInterface, i, keyEvent) -> {
                if (i == KeyEvent.KEYCODE_BACK) {
                    onBackPressed();
                    return true;
                }
                return false;
            });
            login.setOnDismissListener(dialogInterface -> {
                InitData();
            });
        }
        login.Show();
        Objects.requireNonNull(login.getWindow()).clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        Objects.requireNonNull(login.getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    private void InitView() {
        headerImg = navigationView.getHeaderView(0).findViewById(R.id.imageView);
        headerUser = navigationView.getHeaderView(0).findViewById(R.id.username);
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        setSupportActionBar(toolbar);
        mAppBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph())
                .setOpenableLayout(drawer)
                .build();
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        LoadingDialog.getInstance().show(this);
    }

    private void InitData() {
        firstHelper();
        if (UserDetailManager.getInstance().getAvatar() != null && !UserDetailManager.getInstance().getAvatar().isEmpty()) {
            headerUser.setText(UserDetailManager.getInstance().getUserName());
            Glide.with(MainActivity.this)
                    .load(UserDetailManager.getInstance().getAvatar())
                    .transform(new CenterCrop(), new GlideRoundTransform())
                    .into(headerImg);
        } else {
            headerUser.setText(UserDetailManager.getInstance().getUserName());
            Glide.with(this)
                    .load(R.mipmap.ic_launcher)
                    .transform(new CenterCrop(), new GlideRoundTransform())
                    .into(headerImg);
        }
        headerImg.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.dialog_tips)
                    .setMessage(R.string.logout)
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.ok, (temp, which) -> {
                        Glide.with(this)
                                .load(R.mipmap.ic_launcher)
                                .transform(new CenterCrop(), new GlideRoundTransform())
                                .into(headerImg);
                        headerUser.setText(R.string.app_name);
                        CloudApp.getEditor().clear().apply();
                        if (drawer.isDrawerOpen(GravityCompat.START))
                            drawer.closeDrawer(GravityCompat.START);
                        Login();
                    })
                    .show();
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(this, SysServer.class));
    }

    @SuppressLint("DefaultLocale")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        logger.e(String.format("requestCode:%d resultCode:%d", requestCode, resultCode));
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                logger.e(String.format("Google Login Result:%s", Objects.requireNonNull(account).toString()));
                SocketHelper.UserHelper.UserLogin(new SocketCallBack<User>() {
                    @Override
                    public void next(User body) {
                        if (body.getCode() == 200) {
                            if (login != null && login.isShowing())
                                login.dismiss();
                            UserDetailManager.getInstance().setCookie(body.getData().getToken());
                            UserDetailManager.getInstance().setAvatar(Objects.requireNonNull(Objects.requireNonNull(account).getPhotoUrl()).toString());
                            UserDetailManager.getInstance().setUserId(body.getData().getId());
                            UserDetailManager.getInstance().setVipTime(body.getData().getExpireTime());
                            UserDetailManager.getInstance().setUserName(account.getDisplayName());
                            UserDetailManager.getInstance().setValue(UserDetailManager.getInstance());
                            Toast.makeText(CloudApp.getContext(), String.format(getString(R.string.login_success), Objects.requireNonNull(account).getDisplayName()), Toast.LENGTH_LONG).show();
                            CloudApp.getEditor()
                                    .putString("token", body.getData().getToken())
                                    .putString("img", Objects.requireNonNull(account.getPhotoUrl()).toString())
                                    .putString("username", account.getDisplayName())
                                    .apply();
                            LoadingDialog.getInstance().hide();
                        } else
                            Toast.makeText(MainActivity.this, String.format(getString(R.string.login_error), body.getMsg()), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void error(Throwable throwable) {
                        Toast.makeText(MainActivity.this, String.format(getString(R.string.login_error), throwable.getMessage()), Toast.LENGTH_LONG).show();
                        LoadingDialog.getInstance().hide();
                    }
                }, Objects.requireNonNull(account).getId());
            } catch (ApiException e) {
                e.printStackTrace();
                LoadingDialog.getInstance().hide();
                Toast.makeText(this, R.string.google_login_failed, Toast.LENGTH_LONG).show();
            }
        } else if (baseUiListener != null) {
            Tencent.onActivityResultData(requestCode, resultCode, data, baseUiListener);
            if (requestCode == Constants.REQUEST_API && resultCode == Constants.REQUEST_LOGIN)
                Tencent.handleResultData(data, baseUiListener);
        }
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START))
            drawer.closeDrawer(GravityCompat.START);
        else
            super.onBackPressed();
    }

    private void firstHelper() {
        if (CloudApp.getSharedPreferences().getBoolean("first_start", true)) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.dialog_tips)
                    .setMessage(R.string.handle_desc)
                    .setCancelable(false)
                    .setPositiveButton(R.string.ok, (dialogInterface, i) -> {
                        LoadingDialog.getInstance().show(this);
                        SocketHelper.SysHelper.GetHelper(new SocketCallBack<Help>() {
                            @Override
                            public void next(Help body) {
                                LoadingDialog.getInstance().hide();
                                if (body.getCode() == 200) {
                                    CloudApp.getEditor().putBoolean("first_start", false).apply();
                                    Intent intent = new Intent(MainActivity.this, Helper.class);
                                    Bundle bundle = new Bundle();
                                    bundle.putSerializable("data", body);
                                    intent.putExtras(bundle);
                                    startActivity(intent);
                                } else
                                    Toast.makeText(MainActivity.this, body.getMsg(), Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void error(Throwable throwable) {
                                CloudApp.getEditor().putBoolean("first_start", false).apply();
                                LoadingDialog.getInstance().hide();
                                Toast.makeText(MainActivity.this, String.format(getString(R.string.exception), throwable.getMessage()), Toast.LENGTH_SHORT).show();
                            }
                        });
                    })
                    .show();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (UserDetailManager.getInstance().getValue() != null) {
            logger.e("Save UserData");
            outState.putSerializable("userinfo", UserDetailManager.getInstance().getValue());
        }
    }
}
