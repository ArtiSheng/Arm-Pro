package armadillo.studio.ui.selete.file;

import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import armadillo.studio.common.base.BaseViewModel;

public class FileViewModel extends BaseViewModel<List<File>> {
    private List<File> mValue;
    private File root;

    public FileViewModel() {
        mValue = new ArrayList<>();
    }

    @Override
    public List<File> getValue() {
        mValue.clear();
        if (root == null || root.listFiles() == null)
            root = Environment.getExternalStorageDirectory();
        List<File> asList = Arrays.asList(Objects.requireNonNull(root.listFiles()));
        Collections.sort(asList, (o1, o2) -> {
            if (o1.isDirectory() && o2.isFile())
                return 1;
            if (o1.isFile() && o2.isDirectory())
                return -1;
            return o1.getName().compareTo(o2.getName());
        });
        for (File file : asList) {
            if (file.getName().startsWith("."))
                continue;
            mValue.add(file);
        }
        return mValue;
    }

    public void setRoot(File root) {
        this.root = root;
    }

    public File getRoot() {
        return root;
    }
}
