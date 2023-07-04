package flc.upload.service;

import flc.upload.model.Bookmark;
import flc.upload.model.BookmarkVO;

import java.util.List;

public interface BookmarkService {

    void addBookmark(Bookmark bookmark);

    void deleteBookmarkById(Integer id);

    void updateBookmark(Bookmark bookmark);

    List<BookmarkVO> getStructuredBookmarks();

    void fetchBookmarkTitle(Integer id);
}
