package flc.upload.controller;

import flc.upload.model.Paste;
import flc.upload.model.Result;
import flc.upload.service.PasteService;
import flc.upload.util.CookieUtil;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/paste")
public class PasteController {
    private PasteService pasteService;

    public PasteController(PasteService pasteService) {
        this.pasteService = pasteService;
    }

    @PostMapping("/add")
    public Result add(Paste paste) throws Exception {
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
        return pasteService.findAll();
    }

    @PostMapping("/get")
    public Result get(Integer id) throws Exception {
        return pasteService.findById(id);
    }

    @RequestMapping("/last")
    public Result last() throws Exception {
        return pasteService.findLast();
    }
}
