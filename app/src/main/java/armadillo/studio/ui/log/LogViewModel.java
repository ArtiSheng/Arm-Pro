package armadillo.studio.ui.log;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.widget.Toast;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.text.SimpleDateFormat;
import java.util.Date;

import armadillo.studio.R;
import armadillo.studio.common.base.BaseViewModel;
import armadillo.studio.common.base.callback.SocketCallBack;
import armadillo.studio.common.base.view.ViewModelCallBack;
import armadillo.studio.helper.SocketHelper;
import armadillo.studio.model.sys.Notice;
import armadillo.studio.widget.LoadingDialog;

public class LogViewModel extends BaseViewModel<LiveData<String>> {
    private MutableLiveData<String> Value;

    public LogViewModel() {
        Value = new MutableLiveData<>();
    }

    @SuppressLint("SimpleDateFormat")
    public void getAboutBody(Context context, ViewModelCallBack<LiveData<String>> callBack) {
        LoadingDialog.getInstance().show(context);
        SocketHelper.SysHelper.GetAllNotice(new SocketCallBack<Notice>() {
            @Override
            public void next(Notice body) {
                LoadingDialog.getInstance().hide();
                if (body.getCode() == 200) {
                    StringBuilder buffer = new StringBuilder();
                     SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    for (Notice.data var : body.getData()) {
                        buffer.append("> ### ").append(var.getTitle()).append("\n> ###### ").append(simpleDateFormat.format(new Date(var.getTime()))).append("\n");
                        buffer.append(var.getMsg()).append("\n");
                        buffer.append("___\n");
                    }
                    Value.setValue(buffer.toString());
                    callBack.next(Value);
                } else
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

    public LiveData<String> getValue() {
        return Value;
    }
}
