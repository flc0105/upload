package flc.upload.controller;

import flc.upload.annotation.Log;
import flc.upload.model.Result;
import flc.upload.service.ShareCodeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

@Api(tags = "文件分享码")
@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/shareCode")
public class ShareCodeController {
    private final ShareCodeService shareCodeService;

    public ShareCodeController(ShareCodeService shareCodeService) {
        this.shareCodeService = shareCodeService;
    }

    @Log
    @ApiOperation("分享码_添加")
    @PostMapping("/add")
    public Result add(@RequestParam("path") String path) throws Exception {
        return shareCodeService.add(path);
    }

    @Log
    @ApiOperation("分享码_删除")
    @PostMapping("/delete")
    public Result delete(@RequestParam("code") String code) throws Exception {
        return shareCodeService.delete(code);
    }

    @Log
    @ApiOperation("分享码_查询所有")
    @PostMapping("/list")
    public Result list() throws Exception {
        return shareCodeService.findAll();
    }

    @Log
    @ApiOperation("分享码_根据分享码查询")
    @PostMapping("/get")
    public Result get(@RequestParam("code") String code) throws Exception {
        return shareCodeService.findByCode(code);
    }
}
