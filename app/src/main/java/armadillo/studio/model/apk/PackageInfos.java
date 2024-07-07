package armadillo.studio.model.apk;

import android.content.pm.PackageInfo;
import android.graphics.drawable.Drawable;

public class PackageInfos {
    private PackageInfo packageInfo;
    private Drawable ico;
    private String name;
    private String size;
    private String jiagu;
    private boolean jiagu_flag;

    public PackageInfo getPackageInfo() {
        return packageInfo;
    }

    public void setPackageInfo(PackageInfo packageInfo) {
        this.packageInfo = packageInfo;
    }

    public Drawable getIco() {
        return ico;
    }

    public void setIco(Drawable ico) {
        this.ico = ico;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getJiagu() {
        return jiagu;
    }

    public void setJiagu(String jiagu) {
        this.jiagu = jiagu;
    }

    public boolean isJiagu_flag() {
        return jiagu_flag;
    }

    public void setJiagu_flag(boolean jiagu_flag) {
        this.jiagu_flag = jiagu_flag;
    }
}
