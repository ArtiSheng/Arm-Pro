package armadillo.studio.common.base.callback;


import java.util.List;

import armadillo.studio.model.apk.PackageInfos;

public interface GetAppCallBack {
    void Next(List<PackageInfos> list);
}
