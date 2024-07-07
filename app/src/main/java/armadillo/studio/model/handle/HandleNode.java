package armadillo.studio.model.handle;

import java.io.Serializable;
import java.util.List;

public class HandleNode implements Serializable {
    private String name;
    private boolean isSingle;
    private boolean isExpand;
    private List<Node> child;

    public HandleNode(String name) {
        this.name = name;
    }

    public HandleNode(String name, List<Node> child) {
        this.name = name;
        this.child = child;
    }

    public HandleNode(String name, boolean isSingle) {
        this.name = name;
        this.isSingle = isSingle;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isSingle() {
        return isSingle;
    }

    public void setSingle(boolean single) {
        isSingle = single;
    }

    public List<Node> getChild() {
        return child;
    }

    public void setChild(List<Node> child) {
        this.child = child;
    }

    public boolean isExpand() {
        return isExpand;
    }

    public void setExpand(boolean expand) {
        isExpand = expand;
    }
}
