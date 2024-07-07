package armadillo.studio.ui.home;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import armadillo.studio.common.base.BaseViewModel;
import armadillo.studio.common.utils.AppUtils;
import armadillo.studio.common.utils.StreamUtils;
import armadillo.studio.model.task.TaskInfo;

public class HomeViewModel extends BaseViewModel<List<TaskLiveData>> {
    private List<TaskLiveData> mValue;

    public HomeViewModel() {
        mValue = new ArrayList<>();
    }

    @Override
    public List<TaskLiveData> getValue() {
        mValue.clear();
        for (File task : Objects.requireNonNull(new File(Objects.requireNonNull(System.getProperty("task.dir"))).listFiles())) {
            try {
                TaskInfo info = new Gson().fromJson(new String(StreamUtils.toByte(new FileInputStream(task))), TaskInfo.class);
                if (info.getSrc() == null)
                    task.delete();
                else if (AppUtils.isApkFile(info.getSrc()))
                    mValue.add(new TaskLiveData(info));
                else
                    task.delete();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Collections.sort(mValue, (f1, f2) -> (int) (f2.getTask().getTime() - f1.getTask().getTime()));
        return mValue;
    }

}