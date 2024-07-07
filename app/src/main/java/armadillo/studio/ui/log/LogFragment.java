package armadillo.studio.ui.log;

import armadillo.studio.R;
import armadillo.studio.common.base.BaseFragment;
import armadillo.studio.common.utils.Github;
import br.tiagohm.markdownview.MarkdownView;
import butterknife.BindView;

public class LogFragment extends BaseFragment<LogViewModel> {
    @BindView(R.id.markdown_view)
    MarkdownView mMarkdownView;

    @Override
    protected Class<LogViewModel> BindViewModel() {
        return LogViewModel.class;
    }

    @Override
    protected int BindXML() {
        return R.layout.fragment_log;
    }

    @Override
    protected void BindData() {
        viewModel.getAboutBody(requireActivity(), stringLiveData -> stringLiveData.observe(getViewLifecycleOwner(), s -> {
            mMarkdownView.addStyleSheet(new Github());
            mMarkdownView.loadMarkdown(s);
        }));
    }
}
