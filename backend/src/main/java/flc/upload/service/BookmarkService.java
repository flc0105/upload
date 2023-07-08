package flc.upload.service;

import flc.upload.model.Bookmark;
import flc.upload.model.BookmarkVO;

import java.util.List;

public interface BookmarkService {

    void add(Bookmark bookmark);

    void deleteById(Integer id);

    void updateBookmark(Bookmark bookmark);

    List<BookmarkVO> getStructuredBookmarks();

    void updateBookmarkWithParsedData(Integer id);
}
