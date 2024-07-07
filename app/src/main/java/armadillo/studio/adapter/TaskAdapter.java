package armadillo.studio.adapter;

import android.util.Base64;
import android.util.Log;
import android.widget.ImageView;

import androidx.lifecycle.LifecycleOwner;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

import armadillo.studio.CloudApp;
import armadillo.studio.R;
import armadillo.studio.ui.home.TaskLiveData;
import armadillo.studio.common.utils.TimeUtils;

public class TaskAdapter extends BaseQuickAdapter<TaskLiveData, BaseViewHolder> {
    private final String TAG = TaskAdapter.class.getSimpleName();
    private LifecycleOwner owner;

    public TaskAdapter(int layoutResId, @Nullable List<TaskLiveData> data, LifecycleOwner owner) {
        super(layoutResId, data);
        this.owner = owner;
    }

    @Override
    protected void convert(@NotNull BaseViewHolder baseViewHolder, @NotNull TaskLiveData taskInfo) {
        long time = System.currentTimeMillis() - TimeUtils.strtolong(taskInfo.getTask().getTime());
        String old_time = null;
        if (time <= 1000 * 60)
            old_time = String.format(getContext().getString(R.string.task_time_second), time / 1000);
        if (time >= 1000 * 60 && time < 1000 * 60 * 60)
            old_time = String.format(getContext().getString(R.string.task_time_minute), time / 1000 / 60);
        if (time >= 1000 * 60 * 60 && time < 1000 * 60 * 60 * 24)
            old_time = String.format(getContext().getString(R.string.task_time_hour), time / 1000 / 60 / 60);
        if (time >= 1000 * 60 * 60 * 24)
            old_time = String.format(getContext().getString(R.string.task_time_day), time / 1000 / 60 / 60 / 24);
        baseViewHolder
                .setText(R.id.packagename,
                        String.format("%s >> %s", taskInfo.getTask().getPackagename(), Arrays.toString(taskInfo.getTask().getDesc().toArray(new String[0]))))
                .setText(R.id.name,
                        String.format("%s • %s", taskInfo.getTask().getName(), taskInfo.getTask().getVer()))
                .setText(R.id.time, old_time);
        baseViewHolder.getView(R.id.state).setSelected(true);
        switch (taskInfo.getTask().getState()) {
            /**
             * 200 处理完成
             * 404 处理错误
             * 300 排队中
             * 100 处理中
             */
            case 200:
                baseViewHolder.setText(R.id.state, getContext().getString(R.string.status_complete));
                Glide.with(CloudApp.getContext()).load(R.drawable.ic_tick).into((ImageView) baseViewHolder.getView(R.id.state_img));
                break;
            case 404:
                baseViewHolder.setText(R.id.state, getContext().getString(R.string.status_fail));
                Glide.with(CloudApp.getContext()).load(R.drawable.ic_close).into((ImageView) baseViewHolder.getView(R.id.state_img));
                break;
            case 300:
                baseViewHolder.setText(R.id.state, getContext().getString(R.string.status_wait));
                Glide.with(CloudApp.getContext()).load(R.drawable.ic_question).into((ImageView) baseViewHolder.getView(R.id.state_img));
                break;
            case 100:
                baseViewHolder.setText(R.id.state, getContext().getString(R.string.status_processing));
                Glide.with(CloudApp.getContext()).load(R.drawable.ic_question).into((ImageView) baseViewHolder.getView(R.id.state_img));
                break;
        }
        Glide.with(CloudApp.getContext()).load(Base64.decode(taskInfo.getTask().getIco(), Base64.NO_WRAP)).into((ImageView) baseViewHolder.getView(R.id.avatar));
        if (taskInfo.getTask().getState() != 200 && taskInfo.getTask().getState() != 404) {
            if (owner != null) {
                if (taskInfo.hasObservers())
                    taskInfo.removeObservers(owner);
                taskInfo.observe(owner, taskInfo1 -> {
                    Log.i(TAG, taskInfo1.getUuid() + " 数据发送变化");
                    switch (taskInfo1.getState()) {
                        case 200:
                            baseViewHolder.setText(R.id.state, getContext().getString(R.string.status_complete));
                            Glide.with(CloudApp.getContext()).load(R.drawable.ic_tick).into((ImageView) baseViewHolder.getView(R.id.state_img));
                            break;
                        case 404:
                            baseViewHolder.setText(R.id.state, getContext().getString(R.string.status_fail));
                            Glide.with(CloudApp.getContext()).load(R.drawable.ic_close).into((ImageView) baseViewHolder.getView(R.id.state_img));
                            break;
                        case 300:
                            baseViewHolder.setText(R.id.state, getContext().getString(R.string.status_wait));
                            Glide.with(CloudApp.getContext()).load(R.drawable.ic_question).into((ImageView) baseViewHolder.getView(R.id.state_img));
                            break;
                        case 100:
                            baseViewHolder.setText(R.id.state, getContext().getString(R.string.status_processing));
                            Glide.with(CloudApp.getContext()).load(R.drawable.ic_question).into((ImageView) baseViewHolder.getView(R.id.state_img));
                            break;
                    }
                });
            }
        }
    }
}
