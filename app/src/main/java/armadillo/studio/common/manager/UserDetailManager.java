/*
 * Copyright (c) 2021. Armadillo
 */

package armadillo.studio.common.manager;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import androidx.lifecycle.LiveData;

import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.tencent.connect.UserInfo;
import com.tencent.connect.auth.QQToken;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.UiError;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

import armadillo.studio.CloudApp;
import armadillo.studio.R;
import armadillo.studio.common.base.callback.LoginCallBack;
import armadillo.studio.common.base.callback.SocketCallBack;
import armadillo.studio.common.log.logger;
import armadillo.studio.helper.SocketHelper;
import armadillo.studio.model.Basic;
import armadillo.studio.model.sys.User;
import armadillo.studio.widget.LoadingDialog;

public class UserDetailManager extends LiveData<UserDetailManager> implements Serializable {
    private String UserName;
    private String Avatar;
    private String VipTime;
    private int UserId;
    private String Cookie;
    private volatile static UserDetailManager instance;

    public static UserDetailManager getInstance() {
        if (instance == null) {
            synchronized (UserDetailManager.class) {
                if (instance == null) {
                    instance = new UserDetailManager();
                }
            }
        }
        return instance;
    }

    public void setUserName(String userName) {
        UserName = userName;
    }

    public void setAvatar(String avatar) {
        Avatar = avatar;
    }

    public void setVipTime(String vipTime) {
        VipTime = vipTime;
    }

    public void setUserId(int userId) {
        UserId = userId;
    }

    public void setCookie(String cookie) {
        Cookie = cookie;
    }

    public String getUserName() {
        return UserName;
    }

    public String getAvatar() {
        return Avatar;
    }

    public String getVipTime() {
        return VipTime;
    }

    public int getUserId() {
        return UserId;
    }

    public String getCookie() {
        return Cookie;
    }

    public IUiListener loginTencent(Activity activity, LoginCallBack loginCallBack) {
        LoadingDialog.getInstance().show(activity);
        IUiListener listener = new IUiListener() {
            @Override
            public void onComplete(Object response) {
                JSONObject obj = (JSONObject) response;
                logger.e(String.format("Tencent Login Result:%s", obj.toString()));
                try {
                    final String openID = obj.getString("openid");
                    final String accessToken = obj.getString("access_token");
                    final String expires = obj.getString("expires_in");
                    CloudApp.getTencent().setOpenId(openID);
                    CloudApp.getTencent().setAccessToken(accessToken, expires);
                    CloudApp.getEditor().putString("openid", openID)
                            .putString("accesstoken", accessToken)
                            .putString("expires", expires)
                            .apply();
                    final QQToken qqToken = CloudApp.getTencent().getQQToken();
                    final UserInfo mUserInfo = new UserInfo(activity, qqToken);
                    mUserInfo.getUserInfo(new IUiListener() {
                        @Override
                        public void onComplete(Object response) {
                            JSONObject jsonObject = (JSONObject) response;
                            SocketHelper.UserHelper.UserLogin(new SocketCallBack<User>() {
                                @Override
                                public void next(User body) {
                                    if (body.getCode() == 200) {
                                        try {
                                            Cookie = body.getData().getToken();
                                            UserName = jsonObject.getString("nickname");
                                            Avatar = jsonObject.getString("figureurl_qq_2");
                                            VipTime = body.getData().getExpireTime();
                                            UserId = body.getData().getId();
                                            Toast.makeText(activity, String.format(activity.getString(R.string.login_success), UserName), Toast.LENGTH_LONG).show();
                                            CloudApp.getEditor()
                                                    .putString("token", Cookie)
                                                    .putString("img", Avatar)
                                                    .putString("username", UserName)
                                                    .apply();
                                            setValue(getInstance());
                                            loginCallBack.Next();
                                        } catch (JSONException e) {
                                            Toast.makeText(activity, String.format(activity.getString(R.string.login_error), e.getMessage()), Toast.LENGTH_LONG).show();
                                        }
                                    } else
                                        Toast.makeText(activity, String.format(activity.getString(R.string.login_error), body.getMsg()), Toast.LENGTH_LONG).show();
                                    LoadingDialog.getInstance().hide();
                                }

                                @Override
                                public void error(Throwable throwable) {
                                    Toast.makeText(activity, String.format(activity.getString(R.string.login_error), throwable.getMessage()), Toast.LENGTH_LONG).show();
                                    LoadingDialog.getInstance().hide();
                                }
                            }, openID);
                        }

                        @Override
                        public void onError(@NotNull UiError uiError) {
                            Toast.makeText(activity, String.format(activity.getString(R.string.login_error), uiError.errorMessage), Toast.LENGTH_LONG).show();
                            LoadingDialog.getInstance().hide();
                        }

                        @Override
                        public void onCancel() {
                            LoadingDialog.getInstance().hide();
                        }
                    });
                } catch (JSONException e) {
                    Toast.makeText(activity, String.format(activity.getString(R.string.login_error), e.getMessage()), Toast.LENGTH_LONG).show();
                    LoadingDialog.getInstance().hide();
                }
            }

            @Override
            public void onError(@NotNull UiError uiError) {
                Toast.makeText(activity, String.format(activity.getString(R.string.login_error), uiError.errorMessage), Toast.LENGTH_LONG).show();
                LoadingDialog.getInstance().hide();
            }

            @Override
            public void onCancel() {
                LoadingDialog.getInstance().hide();
            }
        };
        CloudApp.getTencent().login(activity, "all", listener);
        return listener;
    }

    public void loginGoogle(Activity activity, @NotNull GoogleSignInClient mGoogleSignInClient, int RC_SIGN_IN) {
        LoadingDialog.getInstance().show(activity);
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        if (signInIntent.resolveActivity(activity.getPackageManager()) != null)
            activity.startActivityForResult(signInIntent, RC_SIGN_IN);
        else
            LoadingDialog.getInstance().hide();
    }

    public void login(Activity activity, String user, String pass, LoginCallBack loginCallBack) {
        LoadingDialog.getInstance().show(activity);
        SocketHelper.UserHelper.UserNameLogin(new SocketCallBack<User>() {
            @Override
            public void next(User body) {
                logger.e(String.format("Login Result:%s", body.toString()));
                if (body.getCode() == 200) {
                    Toast.makeText(activity, String.format(activity.getString(R.string.login_success), body.getData().getUsername()), Toast.LENGTH_LONG).show();
                    Cookie = body.getData().getToken();
                    UserName = body.getData().getUsername();
                    VipTime = body.getData().getExpireTime();
                    UserId = body.getData().getId();
                    setValue(getInstance());
                    CloudApp.getEditor()
                            .putString("token", Cookie)
                            .putString("img", "")
                            .putString("username", UserName)
                            .apply();
                    loginCallBack.Next();
                } else
                    Toast.makeText(activity, String.format(activity.getString(R.string.login_error), body.getMsg()), Toast.LENGTH_LONG).show();
                LoadingDialog.getInstance().hide();
            }

            @Override
            public void error(Throwable throwable) {
                Toast.makeText(activity, String.format(activity.getString(R.string.login_error), throwable.getMessage()), Toast.LENGTH_LONG).show();
                LoadingDialog.getInstance().hide();
            }
        }, user, pass);
    }

    public void registered(Activity activity, String user, String pass, String email) {
        LoadingDialog.getInstance().show(activity);
        SocketHelper.UserHelper.UserRegistered(new SocketCallBack<Basic>() {
            @Override
            public void next(Basic body) {
                Toast.makeText(activity, body.getMsg(), Toast.LENGTH_LONG).show();
                LoadingDialog.getInstance().hide();
            }

            @Override
            public void error(Throwable throwable) {
                Toast.makeText(activity, String.format(activity.getString(R.string.login_error), throwable.getMessage()), Toast.LENGTH_LONG).show();
                LoadingDialog.getInstance().hide();
            }
        }, user, pass, email);
    }

    public void retrieve(Activity activity, String user, String email) {
        LoadingDialog.getInstance().show(activity);
        SocketHelper.UserHelper.UserRetrieve(new SocketCallBack<Basic>() {
            @Override
            public void next(Basic body) {
                Toast.makeText(activity, body.getMsg(), Toast.LENGTH_LONG).show();
                LoadingDialog.getInstance().hide();
            }

            @Override
            public void error(Throwable throwable) {
                Toast.makeText(activity, String.format(activity.getString(R.string.login_error), throwable.getMessage()), Toast.LENGTH_LONG).show();
                LoadingDialog.getInstance().hide();
            }
        }, user, email);
    }

    public void setValue(UserDetailManager value) {
        super.setValue(value);
    }

    public void clear() {
        setValue(null);
    }
}
