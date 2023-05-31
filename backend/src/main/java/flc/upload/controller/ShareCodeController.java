package flc.upload.controller;

import flc.upload.model.Result;
import flc.upload.service.ShareCodeService;
import flc.upload.service.impl.ShareCodeServiceImpl;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/shareCode")
public class ShareCodeController {
    private final ShareCodeService shareCodeService;

    public ShareCodeController(ShareCodeService shareCodeService) {
        this.shareCodeService = shareCodeService;
    }

    @PostMapping("/add")
    public Result add(@RequestParam("path") String path) throws Exception {
        return shareCodeService.add(path);
    }

    @PostMapping("/delete")
    public Result delete(@RequestParam("code") String code) throws Exception {
        return shareCodeService.delete(code);
    }

    @PostMapping("/list")
    public Result list() throws Exception {
        return shareCodeService.findAll();
    }

    @PostMapping("/get")
    public Result get(@RequestParam("code") String code) throws Exception {
        return shareCodeService.findByCode(code);
    }
}
