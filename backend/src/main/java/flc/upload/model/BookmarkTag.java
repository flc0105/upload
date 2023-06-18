package flc.upload.model;

public class BookmarkTag {

    private Integer id;

    private Bookmark bookmark;

    private Tag tag;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Bookmark getBookmark() {
        return bookmark;
    }

    public void setBookmark(Bookmark bookmark) {
        this.bookmark = bookmark;
    }

    public Tag getTag() {
        return tag;
    }

    public void setTag(Tag tag) {
        this.tag = tag;
    }

    @Override
    public String toString() {
        return "BookmarkTag{" +
                "id=" + id +
                ", bookmark=" + bookmark +
                ", tag=" + tag +
                '}';
    }
}
