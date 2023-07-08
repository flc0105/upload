package flc.upload.service;

import flc.upload.model.Bookmark;
import flc.upload.model.BookmarkVO;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

public interface BookmarkService {

    void add(Bookmark bookmark);

    void deleteById(Integer id);

    void updateBookmark(Bookmark bookmark);

    List<BookmarkVO> getStructuredBookmarks();

    void updateBookmarkWithParsedData(Integer id);

    void importBookmarks(String json);

    void exportBookmarksToExcel(HttpServletResponse response);
}
