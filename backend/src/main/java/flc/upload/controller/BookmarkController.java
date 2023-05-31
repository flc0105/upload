package flc.upload.controller;

import flc.upload.model.Result;
import flc.upload.service.BookmarkService;
import flc.upload.service.impl.BookmarkServiceImpl;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/bookmark")
public class BookmarkController {
    private final BookmarkService bookmarkService;

    public BookmarkController(BookmarkService bookmarkService) {
        this.bookmarkService = bookmarkService;
    }

    @PostMapping("/add")
    public Result add(@RequestParam("url") String url) throws Exception {
        return bookmarkService.add(url);
    }

    @PostMapping("/delete")
    public Result delete(@RequestParam("id") Integer id) throws Exception {
        return bookmarkService.delete(id);
    }

    @PostMapping("/update")
    public Result update(@RequestParam("id") Integer id) throws Exception {
        return bookmarkService.update(id);
    }

    @PostMapping("/list")
    public Result list() throws Exception {
        return bookmarkService.findAll();
    }

    @PostMapping("/bulkAdd")
    public Result bulkAdd(@RequestParam("data") String data) throws Exception {
        return bookmarkService.bulkAdd(data);
    }

    @PostMapping("/updateAll")
    public Result updateAll() throws Exception {
        return bookmarkService.updateAll();
    }

    @PostMapping("/rename")
    public Result rename(@RequestParam("id") Integer id, @RequestParam("title") String title) throws Exception {
        return bookmarkService.rename(id, title);
    }
}
