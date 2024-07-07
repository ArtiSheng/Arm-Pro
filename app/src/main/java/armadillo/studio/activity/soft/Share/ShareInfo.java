/*
 * Copyright (c) 2021. Armadillo
 */

package armadillo.studio.activity.soft.Share;

import androidx.annotation.NonNull;

import armadillo.studio.R;
import armadillo.studio.common.base.BaseActivity;
import armadillo.studio.model.soft.UserSoft;

public class ShareInfo extends BaseActivity<UserSoft.data> {
    @Override
    protected int BindXML() {
        return R.layout.activity_share_info;
    }

    @Override
    protected boolean AutoLoadData() {
        return false;
    }

    @Override
    public void BindData(UserSoft.data data) {

    }

    @Override
    public void onError(@NonNull Throwable throwable) {

    }
}
