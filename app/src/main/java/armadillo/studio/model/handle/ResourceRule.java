package armadillo.studio.model.handle;

import java.io.Serializable;
import java.util.Objects;

public class ResourceRule implements Serializable {
    private String name;
    private String startWith;
    private String endWith;

    public ResourceRule(String name) {
        this.name = name;
    }

    public ResourceRule(String startWith, String endWith) {
        this.startWith = startWith;
        this.endWith = endWith;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStartWith() {
        return startWith;
    }

    public void setStartWith(String startWith) {
        this.startWith = startWith;
    }

    public String getEndWith() {
        return endWith;
    }

    public void setEndWith(String endWith) {
        this.endWith = endWith;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResourceRule that = (ResourceRule) o;
        if (name != null)
            return Objects.equals(name, that.name);
        else if (startWith != null && endWith == null)
            return Objects.equals(startWith, that.startWith);
        else if (startWith == null && endWith != null)
            return Objects.equals(endWith, that.endWith);
        else
            return Objects.equals(startWith, that.startWith) &&
                    Objects.equals(endWith, that.endWith);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, startWith, endWith);
    }

    @Override
    public String toString() {
        return "ResourceRule{" +
                "name='" + name + '\'' +
                ", startWith='" + startWith + '\'' +
                ", endWith='" + endWith + '\'' +
                '}';
    }
}
