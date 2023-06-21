package flc.upload.enums;

public enum PermissionType {
    ADMIN(1),
    ANONYMOUS(0);

    private final int value;

    PermissionType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}


