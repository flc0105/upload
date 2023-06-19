package flc.upload.controller;

import flc.upload.model.Bookmark;
import flc.upload.model.Result;
import flc.upload.service.BookmarkService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@Api(tags = "书签")
@RequestMapping("/bookmark")
public class BookmarkController {
    private final BookmarkService bookmarkService;

    public BookmarkController(BookmarkService bookmarkService) {
        this.bookmarkService = bookmarkService;
    }

    @ApiOperation("书签_添加")
    @PostMapping("/add")
    public Result add(@RequestParam("url") String url) throws Exception {
        return bookmarkService.add(url);
    }

    @ApiOperation("书签_删除")
    @PostMapping("/delete")
    public Result delete(@RequestParam("id") Integer id) throws Exception {
        return bookmarkService.delete(id);
    }

    @ApiOperation("书签_更新")
    @PostMapping("/update")
    public Result update(@RequestParam("id") Integer id) throws Exception {
        return bookmarkService.update(id);
    }

    @ApiOperation("书签_查询所有")
    @PostMapping("/list")
    public Result list() throws Exception {
        return bookmarkService.findAll();
    }

    @ApiOperation("书签_批量添加")
    @PostMapping("/bulkAdd")
    public Result bulkAdd(@ApiParam("JSON字符串") @RequestParam("data") String data) throws Exception {
        return bookmarkService.bulkAdd(data);
    }

    @ApiOperation("书签_更新所有")
    @PostMapping("/updateAll")
    public Result updateAll() throws Exception {
        return bookmarkService.updateAll();
    }

    @ApiOperation("书签_修改")
    @PostMapping("/rename")
    public Result rename(@RequestBody Bookmark bookmark) throws Exception {
        return bookmarkService.rename(bookmark.getId(), bookmark.getTitle(), bookmark.getUrl());
    }

    @ApiOperation("书签_添加标签")
    @PostMapping("/addTags")
    public Result addTags(@RequestParam Integer bookmarkId, @RequestParam("tagIds") List<Integer> tagIds) throws Exception {
        return bookmarkService.addTags(bookmarkId, tagIds);
    }

    @ApiOperation("标签_查询所有")
    @PostMapping("/findAllTags")
    public Result findAllTags() throws Exception {
        return bookmarkService.findAllTags();
    }

    @ApiOperation("书签_根据标签查询")
    @PostMapping("/findByTags")
    public Result findByTags(@RequestParam List<Integer> tagIds) throws Exception {
        return bookmarkService.findBookmarksByTags(tagIds);
    }
}
