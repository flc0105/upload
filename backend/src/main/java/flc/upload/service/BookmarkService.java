package flc.upload.service;

import flc.upload.model.Bookmark;
import flc.upload.model.BookmarkVO;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface BookmarkService {

    void add(Bookmark bookmark);

    void deleteById(Integer id);

    void updateBookmark(Bookmark bookmark);

    List<BookmarkVO> getStructuredBookmarks();

     Map<String, Object> findByParentId(Integer id);

    List<Bookmark> getParentPathById(Integer id);

    List<Bookmark> filterBookmarks(String keyword, Integer parentId);

    void updateBookmarkWithParsedData(Integer id);

    void importBookmarks(String json);

    void exportBookmarksToExcel(HttpServletResponse response) throws IOException;

    List<Bookmark> getDeadLinks();
}
