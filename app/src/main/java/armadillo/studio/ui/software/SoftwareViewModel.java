/*
 * Copyright (c) 2020. Armadillo
 */

package armadillo.studio.ui.software;

import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import armadillo.studio.R;
import armadillo.studio.common.base.BaseViewModel;
import armadillo.studio.common.base.callback.SocketCallBack;
import armadillo.studio.common.base.view.ViewModelCallBack;
import armadillo.studio.helper.SocketHelper;
import armadillo.studio.model.soft.UserSoft;
import armadillo.studio.widget.LoadingDialog;

public class SoftwareViewModel extends BaseViewModel<List<SoftwareLiveDate>> {
    private List<SoftwareLiveDate> value;
    public int offset = 0;
    public int limit = 10;

    public SoftwareViewModel() {
        value = new ArrayList<>();
    }

    @Override
    public List<SoftwareLiveDate> getValue() {
        return value;
    }

    public void getData(Context context, ViewModelCallBack<List<SoftwareLiveDate>> callBack) {
        LoadingDialog.getInstance().show(context);
        value.clear();
        SocketHelper.UserHelper.GetSoft(new SocketCallBack<UserSoft>() {
            @Override
            public void next(UserSoft body) {
                LoadingDialog.getInstance().hide();
                if (body.getCode() == 200) {
                    for (UserSoft.data data : body.getData())
                        value.add(new SoftwareLiveDate(data));
                    callBack.next(value);
                } else
                    error(null);
            }

            @Override
            public void error(Throwable throwable) {
                LoadingDialog.getInstance().hide();
                Toast.makeText(context, R.string.loading_data_error, Toast.LENGTH_LONG).show();
                ((Activity) context).onBackPressed();
            }
        }, offset, limit);
    }

    public void getData(ViewModelCallBack<List<SoftwareLiveDate>> callBack) {
        SocketHelper.UserHelper.GetSoft(new SocketCallBack<UserSoft>() {
            @Override
            public void next(UserSoft body) {
                LoadingDialog.getInstance().hide();
                if (body.getCode() == 200) {
                    List<SoftwareLiveDate> dates = new ArrayList<>();
                    for (UserSoft.data data : body.getData())
                        dates.add(new SoftwareLiveDate(data));
                    callBack.next(dates);
                } else
                    callBack.next(Collections.emptyList());
            }

            @Override
            public void error(Throwable throwable) {
                callBack.next(Collections.emptyList());
            }
        }, offset, limit);
    }
}
