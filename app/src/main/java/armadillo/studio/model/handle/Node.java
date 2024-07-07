package armadillo.studio.model.handle;

import java.io.Serializable;
import java.util.List;

public class Node implements Serializable {
    private String name;
    private String desc;
    private String icon;
    private int vip;
    private long type;
    private List<ConfigRule> configRule;
    private List<ResourceRule> resourceRule;
    private boolean isSeleteClass;
    private boolean isSeleteActivity;
    private boolean isReadSigner;
    private boolean isReadApkName;
    private List<ResourceRule> seleteClassRule;
    private List<Node> child;
    private boolean isSelected;
    private HandleNode parent;

    public Node(String name, String desc, String icon, int vip, long type) {
        this.name = name;
        this.desc = desc;
        this.icon = icon;
        this.vip = vip;
        this.type = type;
    }

    public Node(String name, String desc, String icon, int vip, long type, boolean isSeleteClass) {
        this.name = name;
        this.desc = desc;
        this.icon = icon;
        this.vip = vip;
        this.type = type;
        this.isSeleteClass = isSeleteClass;
    }

    public Node(String name, String desc, String icon, int vip, long type, List<ResourceRule> resourceRule) {
        this.name = name;
        this.desc = desc;
        this.icon = icon;
        this.vip = vip;
        this.type = type;
        this.resourceRule = resourceRule;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public int getVip() {
        return vip;
    }

    public void setVip(int vip) {
        this.vip = vip;
    }

    public long getType() {
        return type;
    }

    public void setType(long type) {
        this.type = type;
    }

    public List<ConfigRule> getConfigRule() {
        return configRule;
    }

    public void setConfigRule(List<ConfigRule> configRule) {
        this.configRule = configRule;
    }

    public List<ResourceRule> getResourceRule() {
        return resourceRule;
    }

    public void setResourceRule(List<ResourceRule> resourceRule) {
        this.resourceRule = resourceRule;
    }

    public boolean isSeleteClass() {
        return isSeleteClass;
    }

    public void setSeleteClass(boolean seleteClass) {
        isSeleteClass = seleteClass;
    }

    public List<ResourceRule> getSeleteClassRule() {
        return seleteClassRule;
    }

    public void setSeleteClassRule(List<ResourceRule> seleteClassRule) {
        this.seleteClassRule = seleteClassRule;
    }

    public List<Node> getChild() {
        return child;
    }

    public void setChild(List<Node> child) {
        this.child = child;
    }

    public HandleNode getParent() {
        return parent;
    }

    public void setParent(HandleNode parent) {
        this.parent = parent;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public boolean isSeleteActivity() {
        return isSeleteActivity;
    }

    public void setSeleteActivity(boolean seleteActivity) {
        isSeleteActivity = seleteActivity;
    }

    public boolean isReadSigner() {
        return isReadSigner;
    }

    public void setReadSigner(boolean readSigner) {
        isReadSigner = readSigner;
    }

    public boolean isReadApkName() {
        return isReadApkName;
    }

    public void setReadApkName(boolean readApkName) {
        isReadApkName = readApkName;
    }
}
