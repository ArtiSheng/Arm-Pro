package armadillo.studio.common.base.callback;

public interface SocketCallBack<T> {
    void next(T body);

    void error(Throwable throwable);
}
