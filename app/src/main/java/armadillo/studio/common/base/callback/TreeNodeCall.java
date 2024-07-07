/*
 * Copyright (c) 2020. Armadillo
 */

package armadillo.studio.common.base.callback;

public interface TreeNodeCall<T> {
    void BindData(T data);

    void Loading();

    void Error(Throwable throwable);
}
