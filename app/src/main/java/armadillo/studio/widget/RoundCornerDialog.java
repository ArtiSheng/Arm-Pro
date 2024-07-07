package armadillo.studio.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;

import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashSet;
import java.util.Objects;

import armadillo.studio.BuildConfig;
import armadillo.studio.CloudApp;
import armadillo.studio.R;
import armadillo.studio.common.manager.UserDetailManager;
import armadillo.studio.common.utils.GlideRoundTransform;

public class RoundCornerDialog extends AlertDialog {
    private View view;
    private View root;
    private LinearLayout linearLayout;
    private onChildClickListener onChildClickListener;
    private onChildCheckedChangeListener onChildCheckedChangeListener;
    private LinkedHashSet<Integer> ChildClickViewIds = new LinkedHashSet<>();
    private LinkedHashSet<Integer> ChildCheckedChangeViewIds = new LinkedHashSet<>();

    public RoundCornerDialog AddChildCheckedChangeViewIds(@NotNull @IdRes int... childCheckedChangeViewIds) {
        for (int id : childCheckedChangeViewIds)
            this.ChildCheckedChangeViewIds.add(id);
        return this;
    }

    public RoundCornerDialog AddChildClickViewIds(@NotNull @IdRes int... childClickViewIds) {
        for (int id : childClickViewIds)
            this.ChildClickViewIds.add(id);
        return this;
    }

    public RoundCornerDialog SetText(@IdRes int id, String text) {
        ((TextView) view.findViewById(id)).setText(text);
        return this;
    }

    public RoundCornerDialog SetText(@IdRes int id, @StringRes int res_id) {
        ((TextView) view.findViewById(id)).setText(getContext().getString(res_id));
        return this;
    }

    public RoundCornerDialog SetOnChildClickListener(RoundCornerDialog.onChildClickListener onChildClickListener) {
        this.onChildClickListener = onChildClickListener;
        return this;
    }

    public RoundCornerDialog SetOnCheckedChangeListener(onChildCheckedChangeListener onChildCheckedChangeListener) {
        this.onChildCheckedChangeListener = onChildCheckedChangeListener;
        return this;
    }

    public interface onChildClickListener {
        void onClick(@NonNull View view, @NonNull RoundCornerDialog dialog);
    }

    public interface onChildCheckedChangeListener {
        void onCheckedChanged(@NonNull View view, @NonNull RoundCornerDialog dialog, boolean isChecked);
    }

    @SuppressLint({"InflateParams", "SetTextI18n"})
    public RoundCornerDialog(@NonNull Context context) {
        super(context);
        root = getLayoutInflater().inflate(R.layout.dialog_root, null);
        linearLayout = root.findViewById(R.id.root);
        View header = getLayoutInflater().inflate(R.layout.dialog_header, null);
        if (UserDetailManager.getInstance().getAvatar() != null && !UserDetailManager.getInstance().getAvatar().isEmpty())
            Glide.with(CloudApp.getContext())
                    .load(UserDetailManager.getInstance().getAvatar())
                    .transform(new CenterCrop(), new GlideRoundTransform())
                    .into((ImageView) header.findViewById(R.id.img));
        else
            Glide.with(CloudApp.getContext())
                    .load(R.mipmap.ic_launcher)
                    .transform(new CenterCrop(), new GlideRoundTransform())
                    .into((ImageView) header.findViewById(R.id.img));
        TextView name = header.findViewById(R.id.name);
        name.setText(String.format("%s(%s)\n%s", getContext().getString(R.string.app_name), BuildConfig.VERSION_NAME, getContext().getString(R.string.dialog_desc)));
        linearLayout.addView(header);
        Objects.requireNonNull(getWindow()).setWindowAnimations(R.style.dialogWindowAnim);
    }

    public RoundCornerDialog SetNewView(View view) {
        this.view = view;
        return this;
    }

    public RoundCornerDialog SetView(@LayoutRes int resid) {
        this.view = getLayoutInflater().inflate(resid, null);
        return this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        linearLayout.addView(view);
        setContentView(root);
        Objects.requireNonNull(getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    public RoundCornerDialog Show() {
        super.show();
        Objects.requireNonNull(getWindow()).setLayout(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        for (int id : ChildClickViewIds)
            root.findViewById(id).setOnClickListener(view -> {
                if (onChildClickListener != null)
                    onChildClickListener.onClick(view, this);
            });

        for (int id : ChildCheckedChangeViewIds)
            ((CheckBox) root.findViewById(id)).setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (onChildCheckedChangeListener != null)
                    onChildCheckedChangeListener.onCheckedChanged(buttonView, this, isChecked);
            });
        return this;
    }

    public View getView() {
        return root;
    }

    public RoundCornerDialog SetCancelable(boolean flag) {
        setCancelable(flag);
        return this;
    }
}
