package flc.upload.controller;

import flc.upload.model.Paste;
import flc.upload.model.Result;
import flc.upload.service.PasteService;
import flc.upload.util.CookieUtil;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/paste")
public class PasteController {
    private PasteService pasteService;

    public PasteController(PasteService pasteService) {
        this.pasteService = pasteService;
    }

//    @PostMapping("/add")
//    public Result add(Paste paste) throws Exception {
//        return pasteService.add(paste);
//    }

    @PostMapping("/add")
    public Result add(@RequestParam("title") String title, @RequestParam("text") String text, @RequestParam(value = "expiredDate", required = false) String expiredDate, @RequestParam(value = "isPrivate", required = false) boolean isPrivate) throws Exception {
        Paste paste = new Paste();
        paste.setTitle(title);
        paste.setText(text);
        paste.setExpireDate(expiredDate);
        paste.setPrivate(isPrivate);
        return pasteService.add(paste);
    }

    @PostMapping("/delete")
    public Result delete(Integer id, HttpServletRequest request) throws Exception {
        return pasteService.delete(id, CookieUtil.getCookie("token", request));
    }

    @PostMapping("/update")
    public Result update(Paste paste, HttpServletRequest request) throws Exception {
        return pasteService.update(paste, CookieUtil.getCookie("token", request));
    }

    @PostMapping("/list")
    public Result list() throws Exception {
        pasteService.deleteExpired();
        return pasteService.findAll();
    }

    @PostMapping("/get")
    public Result get(Integer id) throws Exception {
        pasteService.deleteExpired();
        return pasteService.findById(id);
    }

    @RequestMapping("/last")
    public Result last() throws Exception {
        return pasteService.findLast();
    }

    @Scheduled(fixedRate = 60 * 60 * 1000)
    public void scheduledDeleteExpired() throws Exception {
        pasteService.deleteExpired();
    }

}
