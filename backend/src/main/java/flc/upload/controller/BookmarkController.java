package flc.upload.controller;

import flc.upload.model.Bookmark;
import flc.upload.model.BookmarkVO;
import flc.upload.model.Result;
import flc.upload.service.BookmarkService;
import flc.upload.util.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/bookmarks")
public class BookmarkController {

    private final BookmarkService bookmarkService;

    @Autowired
    public BookmarkController(BookmarkService bookmarkService) {
        this.bookmarkService = bookmarkService;
    }

    @PostMapping
    public void addBookmark(@RequestBody Bookmark bookmark) {
        bookmarkService.addBookmark(bookmark);
    }

    @DeleteMapping("/{id}")
    public void deleteBookmark(@PathVariable("id") Integer id) {
        bookmarkService.deleteBookmarkById(id);
    }

    @PutMapping("/{id}")
    public void updateBookmark(@PathVariable("id") Integer id, @RequestBody Bookmark bookmark) {
        bookmark.setId(id);
        bookmarkService.updateBookmark(bookmark);
    }

//    @GetMapping
//    public List<Bookmark> getBookmarksByParentId(@RequestParam("parentId") Integer parentId) {
//        return bookmarkService.getBookmarksByParentId(parentId);
//    }

    @GetMapping
    public Result<List<BookmarkVO>> getAllBookmarks() {
        return ResponseUtil.buildSuccessResult("query.success", bookmarkService.getStructuredBookmarks());
    }
}
