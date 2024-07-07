/*
 * Copyright (c) 2020. Armadillo
 */

package armadillo.studio.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.google.gson.Gson;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import armadillo.studio.CloudApp;
import armadillo.studio.R;
import armadillo.studio.model.signer.KeyInfo;
import armadillo.studio.common.utils.StreamUtils;

public class SignerAdapter extends BaseQuickAdapter<File, BaseViewHolder> {
    public SignerAdapter(int layoutResId, @Nullable List<File> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, File file) {
        try {
            KeyInfo keyInfo = new Gson().fromJson(new String(StreamUtils.toByte(new FileInputStream(file)), StandardCharsets.UTF_8), KeyInfo.class);
            baseViewHolder
                    .setText(R.id.Name, file.getName().replace(".key", ""))
                    .setText(R.id.Alias, CloudApp.getContext().getString(R.string.singer_alias) + keyInfo.getAlias())
                    .setText(R.id.Pass, CloudApp.getContext().getString(R.string.singer_password) + keyInfo.getPassWord());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
