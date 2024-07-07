package armadillo.studio.ui.home;

import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import armadillo.studio.CloudApp;
import armadillo.studio.model.task.TaskInfo;

public class TaskLiveData extends LiveData<TaskInfo> {
    private TaskInfo task;

    public TaskLiveData(TaskInfo value) {
        super(value);
        task = value;
        try (FileOutputStream outputStream = new FileOutputStream(new File(System.getProperty("task.dir"), task.getUuid()))) {
            outputStream.write(new Gson().toJson(task).getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public TaskInfo getTask() {
        return task;
    }

    public void setTask(TaskInfo task) {
        this.task = task;
        if (Looper.myLooper() == Looper.getMainLooper())
            setValue(task);
        else
            postValue(task);
        try (FileOutputStream outputStream = new FileOutputStream(new File(System.getProperty("task.dir"), task.getUuid()))) {
            outputStream.write(new Gson().toJson(task).getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
