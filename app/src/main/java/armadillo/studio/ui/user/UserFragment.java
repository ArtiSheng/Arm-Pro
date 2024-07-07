package armadillo.studio.ui.user;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.text.TextUtilsCompat;
import androidx.core.view.ViewCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.google.android.material.textfield.TextInputEditText;

import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Objects;

import armadillo.studio.CloudApp;
import armadillo.studio.R;
import armadillo.studio.common.base.BaseFragment;
import armadillo.studio.common.base.callback.SocketCallBack;
import armadillo.studio.common.utils.GlideRoundTransform;
import armadillo.studio.common.manager.UserDetailManager;
import armadillo.studio.helper.SocketHelper;
import armadillo.studio.model.Basic;
import armadillo.studio.model.sys.Other;
import armadillo.studio.widget.BaleDialog;
import armadillo.studio.widget.LoadingDialog;
import armadillo.studio.widget.RoundCornerDialog;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.content.Context.CLIPBOARD_SERVICE;

public class UserFragment extends BaseFragment<UserViewModel> {
    @BindView(R.id.image)
    AppCompatImageView imageView;
    @BindView(R.id.vip)
    AppCompatImageView vip;
    @BindView(R.id.exptime)
    AppCompatTextView exptime;
    @BindView(R.id.username)
    AppCompatTextView username;
    @BindView(R.id.day_task_info)
    AppCompatTextView day_task_info;
    @BindView(R.id.day_task_progress)
    ProgressBar day_task_progress;
    @BindView(R.id.day_available_task_info)
    AppCompatTextView day_available_task_info;
    @BindView(R.id.day_available_task_progress)
    ProgressBar day_available_task_progress;
    @BindView(R.id.total_apps_info)
    AppCompatTextView total_apps_info;
    @BindView(R.id.total_apps_progress)
    ProgressBar total_apps_progress;
    @BindView(R.id.next_level_info)
    AppCompatTextView next_level_info;
    @BindView(R.id.next_level_progress)
    ProgressBar next_level_progress;

    private Other other;

    @Override
    protected Class<UserViewModel> BindViewModel() {
        return UserViewModel.class;
    }

    @Override
    protected int BindXML() {
        return R.layout.fragment_user;
    }

    @SuppressLint({"SimpleDateFormat", "InflateParams", "SetTextI18n"})
    @Override
    protected void BindData() {
        viewModel.getData(requireActivity(), other -> {
            this.other = other;
            day_task_progress.setMax(other.getData().getTotal_task());
            day_task_progress.setProgress(other.getData().getDay_task());
            day_task_info.setText(String.valueOf(other.getData().getDay_task()));

            day_available_task_progress.setMax(other.getData().getTotal_task());
            day_available_task_progress.setProgress(other.getData().getTotal_task() - other.getData().getDay_task());
            day_available_task_info.setText(String.valueOf(other.getData().getTotal_task() - other.getData().getDay_task()));

            total_apps_progress.setMax(100);
            total_apps_progress.setProgress(other.getData().getTotal_apps());
            total_apps_info.setText(String.valueOf(other.getData().getTotal_apps()));
            if (UserDetailManager.getInstance().getAvatar() == null || UserDetailManager.getInstance().getAvatar().isEmpty())
                Glide.with(this)
                        .load(R.mipmap.ic_launcher)
                        .transform(new CenterCrop(), new GlideRoundTransform())
                        .into(imageView);
            else
                Glide.with(this)
                        .load(UserDetailManager.getInstance().getAvatar())
                        .transform(new CenterCrop(), new GlideRoundTransform())
                        .into(imageView);
            username.setText(UserDetailManager.getInstance().getUserName() + "(" + UserDetailManager.getInstance().getUserId() + ")");
            try {
                long time = Objects.requireNonNull(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(UserDetailManager.getInstance().getVipTime())).getTime();
                long lifetime = Objects.requireNonNull(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2030-01-01 00:00:00")).getTime();
                if (time < lifetime)
                    exptime.setText(UserDetailManager.getInstance().getVipTime());
                else
                    exptime.setText(R.string.exptime_lifetime);
                if (time < System.currentTimeMillis()) {
                    next_level_progress.setMax(100);
                    next_level_progress.setProgress(0);
                    next_level_info.setText("0%");
                } else {
                    Glide.with(this).load(R.drawable.ic_vip).into(vip);
                    next_level_progress.setMax(1);
                    next_level_progress.setProgress(1);
                    next_level_info.setText(String.format("%s", "%100"));
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        });
    }

    @OnClick({R.id.pay, R.id.card_buy, R.id.changePass})
    public void submit(@NotNull View clickView) {
        switch (clickView.getId()) {
            case R.id.pay: {
                if (other == null)
                    return;
                View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_verify_code, null);
                new AlertDialog.Builder(requireContext(), R.style.cornerdialog)
                        .setView(view)
                        .show();
                TextInputEditText card1 = view.findViewById(R.id.card_1);
                TextInputEditText card2 = view.findViewById(R.id.card_2);
                TextInputEditText card3 = view.findViewById(R.id.card_3);
                TextInputEditText card4 = view.findViewById(R.id.card_4);
                AppCompatTextView card_price = view.findViewById(R.id.card_price);
                card_price.setText(other.getData().getCard_price());
                ClipboardManager cm = (ClipboardManager) requireContext().getSystemService(CLIPBOARD_SERVICE);
                if (cm != null) {
                    ClipData data = cm.getPrimaryClip();
                    if (data != null) {
                        if (data.getItemCount() > 0) {
                            ClipData.Item item = data.getItemAt(0);
                            String content = item.getText().toString();
                            if (content.length() == 19 && content.contains("-")) {
                                String[] card = content.split("-");
                                if (card.length >= 4) {
                                    card1.setText(card[0]);
                                    card2.setText(card[1]);
                                    card3.setText(card[2]);
                                    card4.setText(card[3]);
                                }
                            }
                        }
                    }
                }
                view.findViewById(R.id.verify).setOnClickListener(v1 -> {
                    if (Objects.requireNonNull(card1.getText()).toString().trim().isEmpty()
                            || Objects.requireNonNull(card2.getText()).toString().trim().isEmpty()
                            || Objects.requireNonNull(card3.getText()).toString().trim().isEmpty()
                            || Objects.requireNonNull(card4.getText()).toString().trim().isEmpty())
                        return;

                    String card = card1.getText().toString().trim()
                            + "-" + card2.getText().toString().trim()
                            + "-" + card3.getText().toString().trim()
                            + "-" + card4.getText().toString().trim();
                    LoadingDialog.getInstance().show(requireActivity());
                    SocketHelper.UserHelper.UserPay(new SocketCallBack<Basic>() {
                        @Override
                        public void next(Basic body) {
                            LoadingDialog.getInstance().hide();
                            if (body.getCode() == 200)
                                BaleDialog.ShowInfo(requireActivity(), body.getMsg());
                            else
                                Toast.makeText(requireActivity(), body.getMsg(), Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void error(Throwable throwable) {
                            LoadingDialog.getInstance().hide();
                            Toast.makeText(requireActivity(), String.format(getString(R.string.exception), throwable.getMessage()), Toast.LENGTH_LONG).show();
                        }
                    }, card);
                });
            }
            break;
            case R.id.card_buy: {
                if (other == null)
                    return;
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(other.getData().getCard_buy_url()));
                if (intent.resolveActivity(requireActivity().getPackageManager()) != null)
                    startActivity(intent);
            }
            break;
            case R.id.changePass: {
                RoundCornerDialog change = new RoundCornerDialog(requireActivity())
                        .SetView(R.layout.dialog_change_pass)
                        .AddChildClickViewIds(R.id.change)
                        .SetOnChildClickListener((view1, change_dialog) -> {
                            TextInputEditText pass, new_pass;
                            pass = change_dialog.getView().findViewById(R.id.change_password);
                            new_pass = change_dialog.getView().findViewById(R.id.change_newpassword);
                            String pass_text, new_pass_text;
                            pass_text = Objects.requireNonNull(pass.getText()).toString();
                            new_pass_text = Objects.requireNonNull(new_pass.getText()).toString();
                            if (TextUtils.isEmpty(pass_text)
                                    || TextUtils.isEmpty(new_pass_text))
                                Toast.makeText(requireActivity(), R.string.rule_not, Toast.LENGTH_LONG).show();
                            else if (pass_text.length() < 5 || new_pass_text.length() < 5)
                                Toast.makeText(requireActivity(), R.string.length_fail, Toast.LENGTH_LONG).show();
                            else {
                                LoadingDialog.getInstance().show(requireActivity());
                                SocketHelper.UserHelper.UserChangePass(new SocketCallBack<Basic>() {
                                    @Override
                                    public void next(Basic body) {
                                        Toast.makeText(CloudApp.getContext(), body.getMsg(), Toast.LENGTH_LONG).show();
                                        LoadingDialog.getInstance().hide();
                                    }

                                    @Override
                                    public void error(Throwable throwable) {
                                        Toast.makeText(CloudApp.getContext(), String.format(getString(R.string.login_error), throwable.getMessage()), Toast.LENGTH_LONG).show();
                                        LoadingDialog.getInstance().hide();
                                    }
                                }, pass_text, new_pass_text);
                            }
                        })
                        .Show();
                Objects.requireNonNull(change.getWindow()).clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
                Objects.requireNonNull(change.getWindow()).setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            }
            break;
        }
    }

    @NotNull
    private String Percent(int y, int z) {
        double baiy = y * 1.0;
        double baiz = z * 1.0;
        double fen = baiy / baiz;
        DecimalFormat df1 = new DecimalFormat("##%");
        return df1.format(fen);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.user, menu);
    }

    @SuppressLint("IntentReset")
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_group:
                if (other != null) {
                    Intent browserIntent = new Intent(Intent.ACTION_DEFAULT, Uri.parse("mqqapi://card/show_pslcard?src_type=internal&version=1&uin=" + other.getData().getGroup() + "&card_type=group&source=qrcode"));
                    if (browserIntent.resolveActivity(requireActivity().getPackageManager()) != null)
                        startActivity(browserIntent);
                }
                return true;
            case R.id.menu_telegram:
                if (other != null) {
                    Intent browserIntent = new Intent(Intent.ACTION_DEFAULT, Uri.parse(other.getData().getTelegram_url()));
                    if (browserIntent.resolveActivity(requireActivity().getPackageManager()) != null)
                        startActivity(browserIntent);
                }
                return true;
            case R.id.menu_email: {
                Intent intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:"));
                intent.putExtra(Intent.EXTRA_EMAIL, R.string.nav_header_subtitle);
                intent.putExtra(Intent.EXTRA_SUBJECT, "Ultima");
                if (intent.resolveActivity(requireActivity().getPackageManager()) != null)
                    startActivity(intent);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean isRtl() {
        return TextUtilsCompat.getLayoutDirectionFromLocale(
                requireActivity().getResources().getConfiguration().locale) == ViewCompat.LAYOUT_DIRECTION_RTL;
    }
}
