package flc.upload.controller;

import flc.upload.annotation.Log;
import flc.upload.model.Paste;
import flc.upload.model.Result;
import flc.upload.service.PasteService;
import flc.upload.service.impl.PasteServiceImpl;
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

    @Log
    @PostMapping("/add")
    public Result add(@RequestParam("title") String title, @RequestParam("text") String text, @RequestParam(value = "expiredDate", required = false) String expiredDate, @RequestParam(value = "isPrivate", required = false) boolean isPrivate, HttpServletRequest request) throws Exception {
        Paste paste = new Paste();
        paste.setTitle(title);
        paste.setText(text);
        paste.setExpireDate(expiredDate);
        paste.setPrivate(isPrivate);
        return pasteService.add(paste);
    }

    @Log
    @PostMapping("/delete")
    public Result delete(Integer id, HttpServletRequest request) throws Exception {
        return pasteService.delete(id, CookieUtil.getCookie("token", request));
    }

    @Log
    @PostMapping("/update")
    public Result update(Paste paste, HttpServletRequest request) throws Exception {
        return pasteService.update(paste, CookieUtil.getCookie("token", request));
    }

    @PostMapping("/list")
    public Result list() throws Exception {
        pasteService.deleteExpired();
        return pasteService.findAll();
    }

    @Log
    @PostMapping("/get")
    public Result get(Integer id, HttpServletRequest request) throws Exception {
        pasteService.deleteExpired();
        return pasteService.findById(id);
    }


    @Log
    @GetMapping("/get/{id}")
    public String getById(@PathVariable Integer id, HttpServletRequest request) throws Exception {
        pasteService.deleteExpired();
        Paste paste = (Paste) pasteService.findById(id).getDetail();
        if (paste == null) {
            return "404";
        }
        return paste.getText();
    }

    @RequestMapping("/last")
    public String last() throws Exception {
        pasteService.deleteExpired();
        Paste paste = (Paste) pasteService.findLast().getDetail();
        if (paste == null) {
            return "404";
        }
        return paste.getText();
    }

    @Scheduled(fixedRate = 60 * 60 * 1000)
    public void scheduledDeleteExpired() throws Exception {
        pasteService.deleteExpired();
    }

}
