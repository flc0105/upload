package flc.upload.service;

import flc.upload.model.Bookmark;
import flc.upload.model.BookmarkVO;

import java.util.List;
import java.util.Map;

public interface BookmarkService {

    void addBookmark(Bookmark bookmark);

    void deleteBookmarkById(Integer id);

    void updateBookmark(Bookmark bookmark);

    List<Bookmark> getBookmarksByParentId(Integer parentId);

    List<Bookmark> getAllBookmarks();

//    List<Map<String, Object>> getStructuredBookmarks();
    List<BookmarkVO> getStructuredBookmarks();
}
