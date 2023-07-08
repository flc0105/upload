package flc.upload.controller;

import flc.upload.model.Bookmark;
import flc.upload.model.BookmarkVO;
import flc.upload.model.Result;
import flc.upload.service.BookmarkService;
import flc.upload.util.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@RestController
@RequestMapping("/bookmarks")
public class BookmarkController {

    private final BookmarkService bookmarkService;

    @Autowired
    public BookmarkController(BookmarkService bookmarkService) {
        this.bookmarkService = bookmarkService;
    }

    @PostMapping
    public Result<Integer> addBookmark(@RequestBody Bookmark bookmark) {
        bookmarkService.add(bookmark);
        return ResponseUtil.buildSuccessResult("add.success", bookmark.getId());
    }

    @PostMapping("/{id}")
    public Result<?> updateBookmarkWithParsedData(@PathVariable("id") Integer id) {
        bookmarkService.updateBookmarkWithParsedData(id);
        return ResponseUtil.buildSuccessResult("query.success");
    }


    @DeleteMapping("/{id}")
    public Result<?> deleteBookmark(@PathVariable("id") Integer id) {
        bookmarkService.deleteById(id);
        return ResponseUtil.buildSuccessResult("delete.success");
    }

    @PutMapping("/{id}")
    public Result<?> updateBookmark(@PathVariable("id") Integer id, @RequestBody Bookmark bookmark) {
        bookmark.setId(id);
        bookmarkService.updateBookmark(bookmark);
        return ResponseUtil.buildSuccessResult("update.success");
    }

    @GetMapping
    public Result<List<BookmarkVO>> getAllBookmarks() {
        return ResponseUtil.buildSuccessResult("query.success", bookmarkService.getStructuredBookmarks());
    }


    @PostMapping("/import")
    public Result<?> importBookmarks(@RequestParam String data) {
        bookmarkService.importBookmarks(data);
        return ResponseUtil.buildSuccessResult("add.success");
    }

    @GetMapping("/excel")
    public void exportBookmarksToExcel(HttpServletResponse response) {
        bookmarkService.exportBookmarksToExcel(response);
    }

}
