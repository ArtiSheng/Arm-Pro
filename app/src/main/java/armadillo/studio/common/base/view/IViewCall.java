/*
 * Copyright (c) 2021. Armadillo
 */

package armadillo.studio.common.base.view;

import androidx.annotation.NonNull;

public interface IViewCall<T> {
    void BindData(T data);

    void Loading();

    void HideLoading();

    void toast(String msg);

    void onError(@NonNull Throwable throwable);
}
