package flc.upload.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;

import flc.upload.annotation.Permission;

import flc.upload.manager.OperationLogManager;
import flc.upload.model.OperationLog;
import flc.upload.model.Result;

import flc.upload.util.ResponseUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

@Api(tags = "操作日志")
@RequestMapping("/log")
@RestController
public class OperationLogController {

    private final OperationLogManager operationLogManager;

    public OperationLogController(OperationLogManager operationLogManager) {
        this.operationLogManager = operationLogManager;
    }


    @ApiOperation("获取日志列表")
    @Permission
    @GetMapping("/list")
    public Result<?> listLogs() {
        return ResponseUtil.buildSuccessResult("query.success", operationLogManager.list());
    }

    @ApiOperation("分页查询日志")
    @Permission
    @GetMapping("/page")
    public Result<IPage<OperationLog>> pageLogs(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseUtil.buildSuccessResult("query.success", operationLogManager.page(current, size));
    }

    @ApiOperation("删除日志")
    @Permission
    @PostMapping("/delete")
    public Result<?> deleteLog(Integer id) {
        operationLogManager.deleteById(id);
        return ResponseUtil.buildSuccessResult("delete.success");
    }

    @ApiOperation("清空日志")
    @Permission
    @PostMapping("/clear")
    public Result<?> deleteAllLogs() {
        operationLogManager.deleteAll();
        return ResponseUtil.buildSuccessResult("delete.success");
    }

}
