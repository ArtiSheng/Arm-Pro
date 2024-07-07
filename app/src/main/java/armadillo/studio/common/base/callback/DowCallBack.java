package armadillo.studio.common.base.callback;

public interface DowCallBack {
    void onStart();

    void onProgress(int progress);

    void onFinish(byte[] bytes);

    void onFail(String errorInfo);

}
