package armadillo.studio.common.enums;

public enum UserEnums {
    TokenSuccess(200),
    TokenInvalid(404);

    UserEnums(int type) {
        this.type = type;
    }

    private int type;

    public int getType() {
        return type;
    }

}
