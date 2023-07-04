package flc.upload.controller;

import flc.upload.model.Bookmark;
import flc.upload.model.BookmarkVO;
import flc.upload.model.Result;
import flc.upload.service.BookmarkService;
import flc.upload.util.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
        if (bookmark.isBookmark()) {
            bookmark.setUrl(bookmark.getUrl().contains("://") ? bookmark.getUrl() : "http://" + bookmark.getUrl());
        }
        bookmarkService.addBookmark(bookmark);
        return ResponseUtil.buildSuccessResult("add.success", bookmark.getId());
    }

    @PostMapping("/{id}")
    public Result<?> fetchBookmark(@PathVariable("id") Integer id) {
        bookmarkService.fetchBookmark(id);
        return ResponseUtil.buildSuccessResult("query.success");
    }


    @DeleteMapping("/{id}")
    public Result<?> deleteBookmark(@PathVariable("id") Integer id) {
        bookmarkService.deleteBookmarkById(id);
        return ResponseUtil.buildSuccessResult("delete.success");
    }

    @PutMapping("/{id}")
    public void updateBookmark(@PathVariable("id") Integer id, @RequestBody Bookmark bookmark) {
        bookmark.setId(id);
        bookmarkService.updateBookmark(bookmark);
    }

    @GetMapping
    public Result<List<BookmarkVO>> getAllBookmarks() {
        return ResponseUtil.buildSuccessResult("query.success", bookmarkService.getStructuredBookmarks());
    }
}
