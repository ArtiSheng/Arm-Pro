package armadillo.studio.model.task;

import android.content.pm.PackageInfo;
import android.util.Base64;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;

import armadillo.studio.CloudApp;
import armadillo.studio.model.handle.Node;
import armadillo.studio.common.utils.AppUtils;

public class TaskInfo {
    private File src;
    private File old;
    private String uuid;
    private LinkedHashSet<String> record;
    /**
     * 200 处理完成
     * 404 处理错误
     * 300 等待中
     * 100 处理中
     */
    private int state;
    private List<String> desc;
    private long handle = 0;
    private String ico;
    private long time;
    private String packagename;
    private String name;
    private String ver;


    public TaskInfo(File src, String uuid, List<Node> enums) {
        this.src = src;
        this.old = new File(System.getProperty("apk.dir"), uuid);
        this.record = new LinkedHashSet<>();
        this.uuid = uuid;
        this.state = 300;
        this.desc = new ArrayList<>();
        for (Node handleEnums : enums)
            this.desc.add(handleEnums.getName());
        for (Node handleEnum : enums)
            handle = handle | handleEnum.getType();
        this.ico = Base64.encodeToString(AppUtils.drawableToByte(AppUtils.getApkDrawable(src.getAbsolutePath())), Base64.NO_WRAP);
        this.time = System.currentTimeMillis();
        PackageInfo info = AppUtils.GetPackageInfo(src.getAbsolutePath());
        this.packagename = info.packageName;
        this.ver = info.versionName;
        this.name = info.applicationInfo.loadLabel(CloudApp.getContext().getPackageManager()).toString();
    }

    public File getSrc() {
        return src;
    }

    public void setSrc(File src) {
        this.src = src;
    }

    public File getOld() {
        return old;
    }

    public void setOld(File old) {
        this.old = old;
    }

    public HashSet<String> getRecord() {
        return record;
    }

    public void setRecord(LinkedHashSet<String> record) {
        this.record = record;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public List<String> getDesc() {
        return desc;
    }

    public void setDesc(List<String> desc) {
        this.desc = desc;
    }

    public String getIco() {
        return ico;
    }

    public void setIco(String ico) {
        this.ico = ico;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getPackagename() {
        return packagename;
    }

    public void setPackagename(String packagename) {
        this.packagename = packagename;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVer() {
        return ver;
    }

    public void setVer(String ver) {
        this.ver = ver;
    }

    public long getHandle() {
        return handle;
    }

    public void setHandle(int handle) {
        this.handle = handle;
    }
}
