package armadillo.studio.ui.about;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import armadillo.studio.BuildConfig;
import armadillo.studio.common.base.BaseViewModel;

public class AboutViewModel extends BaseViewModel<LiveData<String>> {
    private MutableLiveData<String> Value;

    public AboutViewModel() {
        Value = new MutableLiveData<>();
        Value.setValue(String.format("%s(Build Time:%s)", BuildConfig.VERSION_NAME, BuildConfig.versionDateTime));
    }

    @Override
    public LiveData<String> getValue() {
        return Value;
    }
}
