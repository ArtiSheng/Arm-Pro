package armadillo.studio.common.base;

import androidx.lifecycle.ViewModel;

public abstract class BaseViewModel<T> extends ViewModel {
    public abstract T getValue();
}
