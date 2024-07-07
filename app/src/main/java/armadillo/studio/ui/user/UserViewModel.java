/*
 * Copyright (c) 2020. Armadillo
 */

package armadillo.studio.ui.user;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import armadillo.studio.R;
import armadillo.studio.common.base.BaseViewModel;
import armadillo.studio.common.base.callback.SocketCallBack;
import armadillo.studio.common.base.view.ViewModelCallBack;
import armadillo.studio.helper.SocketHelper;
import armadillo.studio.model.sys.Other;
import armadillo.studio.widget.LoadingDialog;

public class UserViewModel extends BaseViewModel<Other> {
    private Other value;

    public void getData(Context context, ViewModelCallBack<Other> callBack) {
        LoadingDialog.getInstance().show(context);
        SocketHelper.UserHelper.GetOther(new SocketCallBack<Other>() {
            @Override
            public void next(Other body) {
                LoadingDialog.getInstance().hide();
                if (body.getCode() == 200)
                    callBack.next(body);
                else
                    error(null);
            }

            @Override
            public void error(Throwable throwable) {
                LoadingDialog.getInstance().hide();
                Toast.makeText(context, R.string.loading_data_error, Toast.LENGTH_LONG).show();
                ((Activity) context).onBackPressed();
            }
        });
    }

    @Override
    public Other getValue() {
        return value;
    }
}
