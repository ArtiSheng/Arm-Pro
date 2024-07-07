package armadillo.studio.server;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;

import armadillo.studio.CloudApp;
import armadillo.studio.common.base.callback.SocketCallBack;
import armadillo.studio.common.enums.UserEnums;
import armadillo.studio.common.manager.UserDetailManager;
import armadillo.studio.helper.SocketHelper;
import armadillo.studio.model.sys.Notice;
import armadillo.studio.model.sys.User;
import armadillo.studio.model.sys.Ver;

public class SysServer extends Service {
    private final String TAG = SysServer.class.getSimpleName();
    private static VerCallback verCallback;
    private static NoticeCallback noticeCallback;
    private static TokenCallback tokenCallback;

    public static void setTokenCallback(TokenCallback tokenCallback) {
        SysServer.tokenCallback = tokenCallback;
    }

    public static void setVerCallback(SysServer.VerCallback call) {
        verCallback = call;
    }

    public static void setNoticeCallback(NoticeCallback noticeCallback) {
        SysServer.noticeCallback = noticeCallback;
    }

    public interface VerCallback {
        void Next(String body, Ver ver);
    }

    public interface NoticeCallback {
        void Next(String body, Notice var);
    }

    public interface TokenCallback {
        void Next(UserEnums userEnums);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        InitVer();
        InitToken();
    }

    private void InitToken() {
        UserDetailManager.getInstance().setCookie(CloudApp.getSharedPreferences().getString("token", null));
        UserDetailManager.getInstance().setAvatar(CloudApp.getSharedPreferences().getString("img", null));
        UserDetailManager.getInstance().setUserName(CloudApp.getSharedPreferences().getString("username", null));
        if (UserDetailManager.getInstance().getCookie() == null || UserDetailManager.getInstance().getCookie().isEmpty()) {
            if (tokenCallback != null)
                tokenCallback.Next(UserEnums.TokenInvalid);
        } else {
            SocketHelper.UserHelper.UserToken(new SocketCallBack<User>() {
                @Override
                public void next(User body) {
                    if (body.getCode() == 200) {
                        UserDetailManager.getInstance().setCookie(body.getData().getToken());
                        UserDetailManager.getInstance().setVipTime(body.getData().getExpireTime());
                        UserDetailManager.getInstance().setUserId(body.getData().getId());
                        UserDetailManager.getInstance().setValue(UserDetailManager.getInstance());
                        CloudApp.getEditor().putString("token", body.getData().getToken()).apply();
                        if (tokenCallback != null)
                            tokenCallback.Next(UserEnums.TokenSuccess);
                    } else {
                        if (tokenCallback != null)
                            tokenCallback.Next(UserEnums.TokenInvalid);
                    }
                }

                @Override
                public void error(Throwable throwable) {
                    tokenCallback.Next(UserEnums.TokenInvalid);
                }
            });
        }
    }

    private void InitVer() {
        SocketHelper.SysHelper.GetVersion(new SocketCallBack<Ver>() {
            @Override
            public void next(Ver body) {
                if (body.getCode() == 200) {
                    String buffer = "### " + body.getData().get(0).getVersionName() + "\n" +
                            body.getData().get(0).getVersionMsg();
                    if (verCallback != null)
                        verCallback.Next(buffer, body);
                } else
                    InitNotice();
            }

            @Override
            public void error(Throwable throwable) {
                InitNotice();
            }
        });
    }

    private void InitNotice() {
        SocketHelper.SysHelper.GetSysNotice(new SocketCallBack<Notice>() {
            @Override
            public void next(Notice body) {
                if (body.getCode() == 200)
                    if (noticeCallback != null && body.getData().get(0).getTime() > CloudApp.getSharedPreferences().getLong("notice_time", 0)) {
                        String buffer = "### " + body.getData().get(0).getTitle() + "\n" +
                                body.getData().get(0).getMsg();
                        noticeCallback.Next(buffer, body);
                    }
            }

            @Override
            public void error(Throwable throwable) {

            }
        });
    }
}
