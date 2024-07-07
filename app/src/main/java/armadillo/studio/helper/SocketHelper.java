package armadillo.studio.helper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Base64;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.qiniu.android.http.CancellationHandler;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.GZIPOutputStream;

import armadillo.studio.BuildConfig;
import armadillo.studio.CloudApp;
import armadillo.studio.R;
import armadillo.studio.common.base.BaseSocket;
import armadillo.studio.common.base.callback.SocketCallBack;
import armadillo.studio.common.base.callback.TaskCallBack;
import armadillo.studio.common.base.callback.UpProgressHandler;
import armadillo.studio.common.enums.SocketTypeEnums;
import armadillo.studio.common.enums.SoftEnums;
import armadillo.studio.common.exception.MagicException;
import armadillo.studio.common.log.logger;
import armadillo.studio.common.manager.UserDetailManager;
import armadillo.studio.common.rsa.RSASignature;
import armadillo.studio.common.rsa.RSAUtils;
import armadillo.studio.common.utils.AppUtils;
import armadillo.studio.common.utils.MD5Utils;
import armadillo.studio.common.utils.StreamUtils;
import armadillo.studio.model.handle.Node;
import armadillo.studio.model.soft.SoftSingleCardInfo;
import armadillo.studio.model.soft.SoftSingleTrialInfo;
import armadillo.studio.model.sys.TaskInfo;

public class SocketHelper {
    private static final String TAG = SocketHelper.class.getSimpleName();
    private static final int timeout = 1000 * 15;
    private static final String magic = "Armadillo";
    private static final ExecutorService executor = Executors.newFixedThreadPool(256);
    private static final SharedPreferences share = CloudApp.getContext().getSharedPreferences("network", Context.MODE_PRIVATE);
    private static final boolean GZIP = false;

    public static class SysHelper {
        /**
         * 获取任务状态信息
         *
         * @param taskCallBack
         * @param uuid
         */
        public static void GetTaskInfo(TaskCallBack<TaskInfo> taskCallBack, String uuid) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("uuid", uuid);
            BasicRequest(taskCallBack, map, SocketTypeEnums.GETTASKINFO);
        }

        /**
         * 获取系统公告
         *
         * @param socketCallBack
         */
        public static void GetSysNotice(SocketCallBack<?> socketCallBack) {
            BasicRequest(socketCallBack, null, SocketTypeEnums.GETlATNOTICE);
        }

        /**
         * 获取系统所有公告
         *
         * @param socketCallBack
         */
        public static void GetAllNotice(SocketCallBack<?> socketCallBack) {
            BasicRequest(socketCallBack, null, SocketTypeEnums.GETALLNOTICE);
        }

        /**
         * 获取最新版本
         *
         * @param socketCallBack
         */
        public static void GetVersion(SocketCallBack<?> socketCallBack) {
            BasicRequest(socketCallBack, null, SocketTypeEnums.GETNEWVER);
        }

        /**
         * 提交新任务
         *
         * @param socketCallBack
         * @param injectInfo
         * @param PackName
         */
        public static void SubmitTask(SocketCallBack<?> socketCallBack, @NotNull InjectInfo injectInfo, String PackName) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("token", injectInfo.getToken());
            map.put("p", PackName);
            long type = 0;
            for (Node handleEnum : injectInfo.getHandleEnums())
                type = type | handleEnum.getType();
            map.put("type", type);
            if (injectInfo.getRule() != null && injectInfo.getRule().size() > 0)
                map.put("task_md5", injectInfo.getMd5() + "-rule-" + MD5Utils.encryptionMD5(new Gson().toJson(injectInfo.getRule()).getBytes()) + "-cache-" + type);
            else
                map.put("task_md5", injectInfo.getMd5() + "-cache-" + type);
            if (injectInfo.getRule() != null)
                map.put("rule", Base64.encodeToString(new Gson().toJson(injectInfo.getRule()).getBytes(), Base64.NO_WRAP));
            map.put("uuid", injectInfo.getUuid());
            BasicRequest(socketCallBack, map, SocketTypeEnums.SUBMITTASK);
        }

        /**
         * 服务器带宽上传
         *
         * @param socketCallBack
         * @param data
         * @param uuid
         * @param upProgressHandler
         * @param cancellationHandler
         */
        public static void Upload(SocketCallBack<?> socketCallBack, Object data, String uuid, UpProgressHandler upProgressHandler, CancellationHandler cancellationHandler) {
            executor.submit(() -> {
                BaseSocket socket = initSocket();
                logger.d(String.format("content: %d", socket.getS_port()));
                if (!socket.isConnected()) {
                    if (socketCallBack != null)
                        Error(socket, socketCallBack, new ConnectException(CloudApp.getContext().getString(R.string.connection_fail)));
                } else {
                    try (DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                         DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());) {
                        dataOutputStream.writeBytes(magic);
                        dataOutputStream.writeInt(SocketTypeEnums.LOCALUPLOAD.getType());
                        int MaxLen = 0;
                        int WriterLen = 0;
                        if (data instanceof File) {
                            MaxLen = StreamUtils.toSize(new FileInputStream((File) data));
                            dataOutputStream.writeInt(MaxLen + uuid.getBytes().length + 4);
                        } else if (data instanceof byte[]) {
                            MaxLen = ((byte[]) data).length;
                            dataOutputStream.writeInt(MaxLen + uuid.getBytes().length + 4);
                        }
                        dataOutputStream.writeInt(0);
                        dataOutputStream.writeInt(uuid.getBytes().length);
                        dataOutputStream.write(uuid.getBytes());
                        dataOutputStream.flush();
                        logger.d("开始写流");
                        if (data instanceof File) {
                            FileInputStream inputStream = new FileInputStream((File) data);
                            byte[] bs = new byte[1024 * 8];
                            int len = 0;
                            while ((len = inputStream.read(bs)) != -1) {
                                if (cancellationHandler.isCancelled()) {
                                    socket.close();
                                    break;
                                }
                                dataOutputStream.write(bs, 0, len);
                                dataOutputStream.flush();
                                WriterLen += len;
                                upProgressHandler.progress((int) (WriterLen * 1.0f / MaxLen * 100));
                            }
                            inputStream.close();
                        } else if (data instanceof byte[]) {
                            ByteArrayInputStream inputStream = new ByteArrayInputStream((byte[]) data);
                            byte[] bs = new byte[1024 * 8];
                            int len = 0;
                            while ((len = inputStream.read(bs)) != -1) {
                                if (cancellationHandler.isCancelled()) {
                                    socket.close();
                                    break;
                                }
                                dataOutputStream.write(bs, 0, len);
                                dataOutputStream.flush();
                                WriterLen += len;
                                upProgressHandler.progress((int) (WriterLen * 1.0f / MaxLen * 100));
                            }
                            inputStream.close();
                        }
                        if (!cancellationHandler.isCancelled()) {
                            if (socketCallBack != null)
                                ReaderData(socket, socketCallBack, dataInputStream);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        if (socketCallBack != null)
                            Error(socket, socketCallBack, new IOException(CloudApp.getContext().getString(R.string.connection_fail)));
                    } finally {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }

        /**
         * 释放任务
         *
         * @param uuid
         */
        public static void FreeTask(String uuid) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("uuid", uuid);
            BasicRequest(null, map, SocketTypeEnums.FREETASK);
        }

        /**
         * 获取上传Token
         *
         * @param socketCallBack
         * @param injectInfo
         * @param PackName
         */
        public static void GetUploadToken(SocketCallBack<?> socketCallBack, @NotNull InjectInfo injectInfo, String PackName) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("token", UserDetailManager.getInstance().getCookie());
            map.put("p", PackName);
            long type = 0;
            for (Node handleEnum : injectInfo.getHandleEnums())
                type = type | handleEnum.getType();
            map.put("type", type);
            if (injectInfo.getRule() != null && injectInfo.getRule().size() > 0)
                map.put("task_md5", injectInfo.getMd5() + "-rule-" + MD5Utils.encryptionMD5(new Gson().toJson(injectInfo.getRule()).getBytes()) + "-cache-" + type);
            else
                map.put("task_md5", injectInfo.getMd5() + "-cache-" + type);
            if (injectInfo.getRule() != null && injectInfo.getRule().size() > 0)
                map.put("rule", Base64.encodeToString(new Gson().toJson(injectInfo.getRule()).getBytes(), Base64.NO_WRAP));
            BasicRequest(socketCallBack, map, SocketTypeEnums.GETUPLOADTOKEN);
        }

        /**
         * 获取帮助文档
         *
         * @param socketCallBack
         */
        public static void GetHelper(SocketCallBack<?> socketCallBack) {
            BasicRequest(socketCallBack, null, SocketTypeEnums.GETHELPER);
        }

        /**
         * 获取功能列表
         *
         * @param socketCallBack
         */
        public static void GetHandle(SocketCallBack<?> socketCallBack) {
            BasicRequest(socketCallBack, null, SocketTypeEnums.GETHANDLE);
        }
    }

    public static class UserHelper {
        /**
         * 第三方授权登录
         *
         * @param socketCallBack
         * @param openid
         */
        public static void UserLogin(SocketCallBack<?> socketCallBack, String openid) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("openid", openid);
            BasicRequest(socketCallBack, map, SocketTypeEnums.QQLOGIN);
        }

        /**
         * 账号密码登录
         *
         * @param socketCallBack
         * @param username
         * @param password
         */
        public static void UserNameLogin(SocketCallBack<?> socketCallBack, String username, String password) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("username", username);
            map.put("password", password);
            BasicRequest(socketCallBack, map, SocketTypeEnums.USERLOGIN);
        }

        /**
         * 账号密码注册
         *
         * @param socketCallBack
         * @param username
         * @param password
         * @param email
         */
        public static void UserRegistered(SocketCallBack<?> socketCallBack, String username, String password, String email) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("username", username);
            map.put("password", password);
            map.put("email", email);
            BasicRequest(socketCallBack, map, SocketTypeEnums.USERREG);
        }

        /**
         * 账号找回密码
         *
         * @param socketCallBack
         * @param username
         * @param email
         */
        public static void UserRetrieve(SocketCallBack<?> socketCallBack, String username, String email) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("username", username);
            map.put("email", email);
            BasicRequest(socketCallBack, map, SocketTypeEnums.USERRET);
        }

        /**
         * 账号修改密码
         *
         * @param socketCallBack
         * @param pass
         * @param new_pass
         */
        public static void UserChangePass(SocketCallBack<?> socketCallBack, String pass, String new_pass) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("password", pass);
            map.put("newpassword", new_pass);
            map.put("token", UserDetailManager.getInstance().getCookie());
            BasicRequest(socketCallBack, map, SocketTypeEnums.USERCHANGEPASS);
        }

        /**
         * Token校验
         *
         * @param socketCallBack
         */
        public static void UserToken(SocketCallBack<?> socketCallBack) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("token", UserDetailManager.getInstance().getCookie());
            BasicRequest(socketCallBack, map, SocketTypeEnums.TOKENCHECK);
        }

        /**
         * 获取个人信息
         *
         * @param socketCallBack
         */
        public static void GetOther(SocketCallBack<?> socketCallBack) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("token", UserDetailManager.getInstance().getCookie());
            BasicRequest(socketCallBack, map, SocketTypeEnums.GETOTHER);
        }

        /**
         * 卡密充值
         *
         * @param socketCallBack
         * @param card
         */
        public static void UserPay(SocketCallBack<?> socketCallBack, String card) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("token", UserDetailManager.getInstance().getCookie());
            map.put("card", card);
            BasicRequest(socketCallBack, map, SocketTypeEnums.USERPAY);
        }

        /**
         * 获取用户应用
         *
         * @param socketCallBack
         * @param offset
         * @param limit
         */
        public static void GetSoft(SocketCallBack<?> socketCallBack, int offset, int limit) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("token", UserDetailManager.getInstance().getCookie());
            map.put("offset", offset);
            map.put("limit", limit);
            BasicRequest(socketCallBack, map, SocketTypeEnums.GETSOFT);
        }

        /**
         * 保存应用模块开关
         *
         * @param socketCallBack
         * @param appkey
         * @param handle
         */
        public static void SaveSoftHandle(SocketCallBack<?> socketCallBack, String appkey, int handle) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("token", UserDetailManager.getInstance().getCookie());
            map.put("key", appkey);
            map.put("handle", handle);
            BasicRequest(socketCallBack, map, SocketTypeEnums.SAVESOFTHANDLE);
        }

        /**
         * 获取应用模块信息
         *
         * @param socketCallBack
         * @param appkey
         * @param softEnums
         */
        public static void GetSoftModelInfo(SocketCallBack<?> socketCallBack, String appkey, @NotNull SoftEnums softEnums) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("token", UserDetailManager.getInstance().getCookie());
            map.put("key", appkey);
            map.put("flag", softEnums.getType());
            BasicRequest(socketCallBack, map, SocketTypeEnums.GETSOFTMODELINFO);
        }

        /**
         * 保存应用模块信息
         *
         * @param socketCallBack
         * @param appkey
         * @param softEnums
         * @param info
         */
        public static void SaveSoftModelInfo(SocketCallBack<?> socketCallBack, String appkey, @NotNull SoftEnums softEnums, @NotNull String info) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("token", UserDetailManager.getInstance().getCookie());
            map.put("key", appkey);
            map.put("flag", softEnums.getType());
            map.put("info", Base64.encodeToString(info.getBytes(), Base64.NO_WRAP));
            BasicRequest(socketCallBack, map, SocketTypeEnums.SAVEMODELINFO);
        }

        /**
         * 删除应用
         *
         * @param socketCallBack
         * @param appkey
         */
        public static void DeleteSoft(SocketCallBack<?> socketCallBack, String appkey) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("token", UserDetailManager.getInstance().getCookie());
            map.put("key", appkey);
            BasicRequest(socketCallBack, map, SocketTypeEnums.DELETESOFT);
        }

        /**
         * 创建单码卡密
         *
         * @param socketCallBack
         * @param appkey
         * @param count
         * @param type
         * @param value
         * @param mark
         */
        public static void CreateSingleCard(SocketCallBack<?> socketCallBack, String appkey, int count, int type, int value, String mark) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("token", UserDetailManager.getInstance().getCookie());
            map.put("key", appkey);
            map.put("count", count);
            map.put("value", value);
            map.put("type", type);
            map.put("mark", mark);
            map.put("flag", 1);
            BasicRequest(socketCallBack, map, SocketTypeEnums.SINGLECARDMANAGEN);
        }

        /**
         * 获取单码卡密
         *
         * @param socketCallBack
         * @param appkey
         * @param offset
         * @param limit
         */
        public static void GetSoftSingleCard(SocketCallBack<?> socketCallBack, String appkey, int offset, int limit) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("token", UserDetailManager.getInstance().getCookie());
            map.put("key", appkey);
            map.put("offset", offset);
            map.put("limit", limit);
            map.put("flag", 2);
            BasicRequest(socketCallBack, map, SocketTypeEnums.SINGLECARDMANAGEN);
        }

        /**
         * 删除卡密
         *
         * @param socketCallBack
         * @param appkey
         * @param cardInfos
         */
        public static void DeleteSoftSingleCards(SocketCallBack<?> socketCallBack, String appkey, List<SoftSingleCardInfo.data> cardInfos) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("token", UserDetailManager.getInstance().getCookie());
            map.put("key", appkey);
            map.put("flag", 3);
            map.put("info", Base64.encodeToString(new Gson().toJson(cardInfos).getBytes(), Base64.NO_WRAP));
            BasicRequest(socketCallBack, map, SocketTypeEnums.SINGLECARDMANAGEN);
        }

        /**
         * 更新卡密
         *
         * @param socketCallBack
         * @param appkey
         * @param cardInfos
         */
        public static void UpdateSoftSingleCards(SocketCallBack<?> socketCallBack, String appkey, List<SoftSingleCardInfo.data> cardInfos) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("token", UserDetailManager.getInstance().getCookie());
            map.put("key", appkey);
            map.put("flag", 4);
            map.put("info", Base64.encodeToString(new Gson().toJson(cardInfos).getBytes(), Base64.NO_WRAP));
            BasicRequest(socketCallBack, map, SocketTypeEnums.SINGLECARDMANAGEN);
        }

        /**
         * 获取单码试用信息
         *
         * @param socketCallBack
         * @param appkey
         * @param offset
         * @param limit
         */
        public static void GetSoftSingleTrial(SocketCallBack<?> socketCallBack, String appkey, int offset, int limit) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("token", UserDetailManager.getInstance().getCookie());
            map.put("key", appkey);
            map.put("offset", offset);
            map.put("limit", limit);
            map.put("flag", 1);
            BasicRequest(socketCallBack, map, SocketTypeEnums.SINGLETRIALMANAGEN);
        }

        /**
         * 删除试用信息
         *
         * @param socketCallBack
         * @param appkey
         * @param trialInfos
         */
        public static void DeleteSoftSingleTrials(SocketCallBack<?> socketCallBack, String appkey, List<SoftSingleTrialInfo.data> trialInfos) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("token", UserDetailManager.getInstance().getCookie());
            map.put("key", appkey);
            map.put("flag", 2);
            map.put("info", Base64.encodeToString(new Gson().toJson(trialInfos).getBytes(), Base64.NO_WRAP));
            BasicRequest(socketCallBack, map, SocketTypeEnums.SINGLETRIALMANAGEN);
        }

        /**
         * 更新试用信息
         *
         * @param socketCallBack
         * @param appkey
         * @param trialInfos
         */
        public static void UpdateSoftSingleTrials(SocketCallBack<?> socketCallBack, String appkey, List<SoftSingleTrialInfo.data> trialInfos) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("token", UserDetailManager.getInstance().getCookie());
            map.put("key", appkey);
            map.put("flag", 3);
            map.put("info", Base64.encodeToString(new Gson().toJson(trialInfos).getBytes(), Base64.NO_WRAP));
            BasicRequest(socketCallBack, map, SocketTypeEnums.SINGLETRIALMANAGEN);
        }
    }

    /**
     * 基础请求
     *
     * @param socketCallBack
     * @param map
     * @param typeEnums
     */
    @SuppressLint("DefaultLocale")
    private static void BasicRequest(Object socketCallBack, HashMap<String, Object> map, SocketTypeEnums typeEnums) {
        executor.submit(() -> {
            BaseSocket socket = initSocket();
            logger.d(String.format("content: %d", socket.getS_port()));
            if (!socket.isConnected()) {
                if (socketCallBack != null)
                    Error(socket, socketCallBack, new ConnectException(CloudApp.getContext().getString(R.string.connection_fail)));
            } else {
                try (DataInputStream dataInputStream = new DataInputStream(socket.getInputStream());
                     DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());) {
                    if (GZIP) {
                        byte[] data = WriteData(map, false);
                        byte[] GzipData = toGZipData(data);
                        byte[] sign = RSASignature.sign(GzipData);
                        dataOutputStream.writeBytes(magic);
                        dataOutputStream.writeInt(typeEnums.getType());
                        dataOutputStream.writeInt(GzipData.length);
                        dataOutputStream.writeInt(sign.length);
                        dataOutputStream.write(GzipData);
                        dataOutputStream.write(sign);
                    } else {
                        byte[] data = WriteData(map, true);
                        byte[] sign = RSASignature.sign(data);
                        dataOutputStream.writeBytes(magic);
                        dataOutputStream.writeInt(typeEnums.getType());
                        dataOutputStream.writeInt(data.length);
                        dataOutputStream.writeInt(sign.length);
                        dataOutputStream.write(data);
                        dataOutputStream.write(sign);
                    }
                    dataOutputStream.flush();
                    if (socketCallBack != null)
                        ReaderData(socket, socketCallBack, dataInputStream);
                } catch (Exception e) {
                    e.printStackTrace();
                    if (socketCallBack != null)
                        Error(socket, socketCallBack, new IOException(CloudApp.getContext().getString(R.string.connection_fail)));
                } finally {
                    try {
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @NotNull
    @SuppressLint("DefaultLocale")
    private static byte[] toGZipData(@NotNull byte[] data) throws Exception {
        ByteArrayOutputStream array = new ByteArrayOutputStream();
        GZIPOutputStream gzipOutputStream = new GZIPOutputStream(array);
        gzipOutputStream.write(data);
        gzipOutputStream.finish();
        gzipOutputStream.close();
        return array.toByteArray();
    }

    /**
     * 写数据
     *
     * @param map
     * @return
     * @throws Exception
     */
    @NotNull
    private static byte[] WriteData(HashMap<String, Object> map, boolean encryption) throws Exception {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("uid", System.currentTimeMillis());
        jsonObject.put("status_machine", Build.BRAND + "(" + Build.MODEL + ")");
        jsonObject.put("version", BuildConfig.VERSION_CODE);
        jsonObject.put("android_id", AppUtils.GetAndroidId());
        jsonObject.put("u", CloudApp.getContext().getResources().getConfiguration().locale.getLanguage());
        jsonObject.put("encoding", "UTF-8");
        if (UserDetailManager.getInstance().getUserName() != null && !UserDetailManager.getInstance().getUserName().isEmpty())
            jsonObject.put("u_name", UserDetailManager.getInstance().getUserName());
        if (map != null) {
            for (Map.Entry<String, Object> entry : map.entrySet())
                jsonObject.put(entry.getKey(), entry.getValue());
        }
        if (!encryption)
            return jsonObject.toString().getBytes();
        else
            return RSAUtils.encrypt(jsonObject.toString().getBytes(), RSAUtils.getPrivateKey(Base64.decode(CloudApp.getContext().getString(R.string.sign_key), Base64.NO_WRAP)));
    }

    /**
     * 读数据
     *
     * @param socketCallBack
     * @param dataInputStream
     * @throws Exception
     */
    private static void ReaderData(BaseSocket socket, @NotNull Object socketCallBack, @NotNull DataInputStream dataInputStream) {
        try {
            Class<?> clz = null;
            if (socketCallBack instanceof SocketCallBack) {
                Type[] genericInterfaces = socketCallBack.getClass().getGenericInterfaces();
                clz = (Class<?>) ((ParameterizedType) genericInterfaces[0]).getActualTypeArguments()[0];
            } else if (socketCallBack instanceof TaskCallBack)
                clz = TaskInfo.class;
            byte[] magic_bytes = new byte[9];
            dataInputStream.readFully(magic_bytes);
            String server_magic = new String(magic_bytes, StandardCharsets.UTF_8);
            if (!server_magic.equals(magic))
                Error(socket, socketCallBack, new MagicException(CloudApp.getContext().getString(R.string.request_exception)));
            else {
                int len = dataInputStream.readInt();
                byte[] bytes = new byte[len];
                dataInputStream.readFully(bytes);
                Next(socket, socketCallBack, new String(RSAUtils.decrypt(bytes, RSAUtils.getPublicKey(Base64.decode(CloudApp.getContext().getString(R.string.key).getBytes(), Base64.NO_WRAP))), StandardCharsets.UTF_8), clz);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Error(socket, socketCallBack, new IOException(CloudApp.getContext().getString(R.string.request_exception)));
        }
    }

    /**
     * 默认回调
     *
     * @param callBack
     * @param body
     * @param clz
     */
    private static void Next(BaseSocket socket, Object callBack, String body, Class<?> clz) {
        new Handler(Looper.getMainLooper()).post(() -> {
            if (callBack instanceof SocketCallBack) {
                try {
                    logger.d(body);
                    SocketCallBack<?> socketCallBack = (SocketCallBack<?>) callBack;
                    socketCallBack.next(new Gson().fromJson(body, (Type) clz));
                } catch (Exception e) {
                    e.printStackTrace();
                    Error(socket, callBack, new JsonParseException(CloudApp.getContext().getString(R.string.parsing_failed)));
                }
            } else if (callBack instanceof TaskCallBack) {
                try {
                    logger.d(body);
                    TaskCallBack<?> taskCallBack = (TaskCallBack<?>) callBack;
                    taskCallBack.Next(new Gson().fromJson(body, (Type) clz));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 失败回调
     *
     * @param callBack
     * @param body
     */
    @SuppressLint("CommitPrefEdits")
    private static void Error(@NotNull BaseSocket socket, Object callBack, Throwable body) {
        if (body instanceof ConnectException) {
            Set<String> fail = share.getStringSet("fail", new HashSet<>());
            SharedPreferences.Editor editor = share.edit();
            Objects.requireNonNull(fail).add(String.valueOf(socket.getS_port()));
            editor.putStringSet("fail", fail).apply();
        }
        new Handler(Looper.getMainLooper()).post(() -> {
            if (callBack instanceof SocketCallBack) {
                SocketCallBack<?> socketCallBack = (SocketCallBack<?>) callBack;
                socketCallBack.error(body);
            }
        });
    }

    /**
     * 初始化Socket
     *
     * @return
     * @throws IOException
     */
    @NotNull
    private static BaseSocket initSocket() {
        BaseSocket socket = new BaseSocket(getRandom());
        try {
            socket.setTcpNoDelay(true);
            socket.setSoTimeout(timeout);
            socket.setOOBInline(true);
            socket.setKeepAlive(true);
            socket.setReceiveBufferSize(1024 * 1024 * 20);
            socket.setSendBufferSize(1024 * 1024 * 20);
            socket.connect(new InetSocketAddress(InetAddress.getByName(CloudApp.getContext().getString(R.string.host)), socket.getS_port()), 10000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return socket;
    }

    /**
     * 获取随机端口 过滤掉失败的端口
     *
     * @return
     */
    private static int getRandom() {
        Random random = new Random();
        int max = CloudApp.getContext().getResources().getInteger(R.integer.endprot);
        int min = CloudApp.getContext().getResources().getInteger(R.integer.startprot);
        int result = random.nextInt(max) % (max - min + 1) + min;
        Set<String> fail = share.getStringSet("fail", new HashSet<>());
        if (Objects.requireNonNull(fail).size() >= max - min) {
            SharedPreferences.Editor editor = share.edit();
            editor.clear().apply();
            fail.clear();
        }
        while (fail.contains(String.valueOf(result))) {
            logger.d(String.format("Radom fail:%d new Radom Port", result));
            result = random.nextInt(max) % (max - min + 1) + min;
        }
        return result;
    }
}
