/*
 * Copyright (c) 2020. Armadillo
 */

package armadillo.studio.activity;

import android.widget.Toast;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;

import armadillo.studio.R;
import armadillo.studio.common.base.BaseActivity;
import armadillo.studio.model.sys.Help;
import armadillo.studio.common.utils.Github;
import br.tiagohm.markdownview.MarkdownView;
import butterknife.BindView;

public class Helper extends BaseActivity<Help> {
    @BindView(R.id.markdown_view)
    MarkdownView mMarkdownView;

    @Override
    protected int BindXML() {
        return R.layout.activity_helper;
    }

    @Override
    protected boolean AutoLoadData() {
        return true;
    }

    @Override
    public void BindData(@NotNull Help data) {
        mMarkdownView.addStyleSheet(new Github());
        mMarkdownView.loadMarkdown(data.getData());
    }

    @Override
    public void onError(@NonNull Throwable throwable) {
        Toast.makeText(this, R.string.loading_data_error, Toast.LENGTH_LONG).show();
        onBackPressed();
    }
}
