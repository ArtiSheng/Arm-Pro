/*
 * Copyright (c) 2020. Armadillo
 */

package armadillo.studio.ui.signer;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import armadillo.studio.common.base.BaseViewModel;

public class SignerViewModel extends BaseViewModel<List<File>> {
    private final List<File> signer;

    public SignerViewModel() {
        signer = new ArrayList<>();
    }

    @Override
    public List<File> getValue() {
        signer.clear();
        for (File file : Objects.requireNonNull(new File(Objects.requireNonNull(System.getProperty("jks.dir"))).listFiles())) {
            if (file.getName().endsWith(".key"))
                signer.add(file);
        }
        Collections.sort(signer, (File o1, File o2) -> {
            if (o1.lastModified() <= o2.lastModified())
                return 1;
            else
                return -1;
        });
        return signer;
    }
}
