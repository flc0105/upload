package flc.upload.controller;

import flc.upload.annotation.Log;
import flc.upload.model.Bookmark;
import flc.upload.model.Result;
import flc.upload.service.BookmarkService;
import flc.upload.util.ResponseUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Api(tags = "书签")
@RestController
@RequestMapping("/bookmarks")
public class BookmarkController {

    private final BookmarkService bookmarkService;

    @Autowired
    public BookmarkController(BookmarkService bookmarkService) {
        this.bookmarkService = bookmarkService;
    }

    @ApiOperation("书签_添加")
    @Log
    @PostMapping
    public Result<?> addBookmark(@RequestBody Bookmark bookmark) {
        bookmarkService.add(bookmark);
        return ResponseUtil.buildSuccessResult("add.success", bookmark.getId());
    }

    @ApiOperation("书签_获取站点标题和图标")
    @Log
    @PostMapping("/{id}")
    public Result<?> updateBookmarkWithParsedData(@PathVariable("id") Integer id) {
        bookmarkService.updateBookmarkWithParsedData(id);
        return ResponseUtil.buildSuccessResult("query.success");
    }

    @ApiOperation("书签_删除")
    @Log
    @DeleteMapping("/{id}")
    public Result<?> deleteBookmark(@PathVariable("id") Integer id) {
        bookmarkService.deleteById(id);
        return ResponseUtil.buildSuccessResult("delete.success");
    }

    @ApiOperation("书签_修改")
    @Log
    @PutMapping("/{id}")
    public Result<?> updateBookmark(@PathVariable("id") Integer id, @RequestBody Bookmark bookmark) {
        bookmark.setId(id);
        bookmarkService.updateBookmark(bookmark);
        return ResponseUtil.buildSuccessResult("update.success");
    }

    @ApiOperation("书签_查询所有")
    @Log
    @GetMapping
    public Result<?> getAllBookmarks() {
        return ResponseUtil.buildSuccessResult("query.success", bookmarkService.getStructuredBookmarks());
    }

    @ApiOperation("书签_导入")
    @Log
    @PostMapping("/import")
    public Result<?> importBookmarks(@RequestParam String data) {
        bookmarkService.importBookmarks(data);
        return ResponseUtil.buildSuccessResult("add.success");
    }

    @ApiOperation("书签_导出excel")
    @Log
    @GetMapping("/excel")
    public void exportBookmarksToExcel(HttpServletResponse response) throws IOException {
        bookmarkService.exportBookmarksToExcel(response);
    }

}
