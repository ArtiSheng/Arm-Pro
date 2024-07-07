/*
 * Copyright (c) 2021. Armadillo
 */

package armadillo.studio.common.manager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import armadillo.studio.common.log.logger;
import armadillo.studio.helper.SocketHelper;
import armadillo.studio.model.sys.TaskInfo;
import armadillo.studio.ui.home.TaskLiveData;

public class TaskDetailManager {
    private final HashSet<TaskLiveData> observers = new HashSet<>();
    private final HashMap<String, ScheduledFuture<?>> futureHashMap = new HashMap<>();
    private volatile static TaskDetailManager instance;

    public static TaskDetailManager getInstance() {
        if (instance == null) {
            synchronized (TaskDetailManager.class) {
                if (instance == null) {
                    instance = new TaskDetailManager();
                }
            }
        }
        return instance;
    }

    public void register(final TaskLiveData data) {
        if (observers.contains(data)) {
            logger.d(String.format("任务:%s已存在", data.getTask().getName()));
            return;
        }
        logger.d(String.format("添加轮询任务:%s", data.getTask().getName()));
        observers.add(data);
        ScheduledExecutorService scheduledThreadPool = Executors.newSingleThreadScheduledExecutor();
        ScheduledFuture<?> future = scheduledThreadPool.scheduleWithFixedDelay(() -> {
            SocketHelper.SysHelper.GetTaskInfo(body -> {
                if (body.getCode() == 200) {
                    for (TaskInfo.data info : body.getData()) {
                        if (!data.getTask().getRecord().contains(info.getMsg())) {
                            data.getTask().getRecord().add(info.getMsg());
                            data.getTask().setState(info.getCode());
                        }
                        if (info.getCode() == 200 || info.getCode() == 404) {
                            logger.d(String.format("任务:%s,已完成", data.getTask().getName()));
                            data.setTask(data.getTask());
                            unregister(data);
                            return;
                        }
                    }
                    data.setTask(data.getTask());
                    logger.d(String.format("任务:%s,更新", data.getTask().getName()));
                } else {
                    data.getTask().getRecord().add(body.getMsg());
                    data.getTask().setState(body.getCode());
                    data.setTask(data.getTask());
                    logger.d(String.format("任务:%s,失败", data.getTask().getName()));
                    unregister(data);
                }
            }, data.getTask().getUuid());
        }, 0, 5, TimeUnit.SECONDS);
        futureHashMap.put(data.getTask().getUuid(), future);
    }

    public void unregister(TaskLiveData data) {
        observers.remove(data);
        if (futureHashMap.get(data.getTask().getUuid()) != null) {
            logger.d(String.format("取消任务:%s", data.getTask().getName()));
            Objects.requireNonNull(futureHashMap.get(data.getTask().getUuid())).cancel(true);
        }
        futureHashMap.remove(data.getTask().getUuid());
    }

    public void unregisterAll() {
        logger.d("移除所有轮询任务");
        observers.clear();
        for (Map.Entry<String, ScheduledFuture<?>> entry : futureHashMap.entrySet())
            entry.getValue().cancel(true);
        futureHashMap.clear();
    }
}
