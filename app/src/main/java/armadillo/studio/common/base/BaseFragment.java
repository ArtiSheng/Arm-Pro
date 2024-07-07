package armadillo.studio.common.base;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import armadillo.studio.common.log.logger;
import butterknife.ButterKnife;


public abstract class BaseFragment<T extends BaseViewModel<?>> extends Fragment {
    public final String TAG = BaseFragment.class.getSimpleName();
    protected T viewModel;
    public View root;

    protected abstract Class<T> BindViewModel();

    protected abstract int BindXML();

    protected abstract void BindData();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (viewModel == null)
            viewModel = new ViewModelProvider(this).get(BindViewModel());
        if (root == null) {
            root = inflater.inflate(BindXML(), container, false);
            ButterKnife.bind(this, root);
            BindData();
        } else
            ButterKnife.bind(this, root);
        return root;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onDestroy() {
        if (viewModel.getValue() != null) {
            if (viewModel.getValue() instanceof LiveData) {
                if (viewModel.getValue() == null)
                    return;
                LiveData<?> liveData = (LiveData<?>) viewModel.getValue();
                if (liveData.hasObservers()) {
                    liveData.removeObservers(getViewLifecycleOwner());
                    logger.d("移除所有观察者");
                }
            }
        }
        super.onDestroy();
    }
}
