package armadillo.studio.ui.about;

import androidx.appcompat.widget.AppCompatTextView;

import armadillo.studio.R;
import armadillo.studio.common.base.BaseFragment;
import butterknife.BindView;

public class AboutFragment extends BaseFragment<AboutViewModel> {
    @BindView(R.id.version)
    AppCompatTextView Version;

    @Override
    protected Class<AboutViewModel> BindViewModel() {
        return AboutViewModel.class;
    }

    @Override
    protected int BindXML() {
        return R.layout.fragment_about;
    }

    @Override
    protected void BindData() {
        viewModel.getValue().observe(getViewLifecycleOwner(), s -> Version.setText(s));
    }
}
