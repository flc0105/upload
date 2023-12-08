package flc.upload.controller;

import flc.upload.annotation.Log;
import flc.upload.annotation.Permission;
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
    @ApiOperation("添加分享码")
    @Permission
    @PostMapping("/add")
    public Result<?> add(@RequestParam("path") String path) throws Exception {
        return shareCodeService.add(path);
    }

    @Log
    @ApiOperation("删除分享码")
    @Permission
    @PostMapping("/delete")
    public Result<?> delete(@RequestParam("code") String code) throws Exception {
        return shareCodeService.delete(code);
    }

    @ApiOperation("获取分享码列表")
    @Permission
    @PostMapping("/list")
    public Result<?> list() throws Exception {
        return shareCodeService.findAll();
    }

    @Log
    @ApiOperation("查询分享码")
    @Permission
    @PostMapping("/get")
    public Result<?> get(@RequestParam("code") String code) throws Exception {
        return shareCodeService.findByCode(code);
    }
}
