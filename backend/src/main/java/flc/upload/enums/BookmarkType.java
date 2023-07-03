package flc.upload.enums;

public enum BookmarkType {
    DIRECTORY(0),  // 目录
    BOOKMARK(1);   // 书签

    private final int value;

    BookmarkType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
