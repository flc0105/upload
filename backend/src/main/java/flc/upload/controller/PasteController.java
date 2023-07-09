package flc.upload.controller;

import flc.upload.annotation.Log;
import flc.upload.annotation.Permission;
import flc.upload.model.Paste;
import flc.upload.model.Result;
import flc.upload.service.PasteService;
import flc.upload.util.InternationalizationUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Objects;

@Api(tags = "Paste")
@CrossOrigin(origins = "*")
@RequestMapping("/paste")
@RestController
public class PasteController {
    private final PasteService pasteService;

    public PasteController(PasteService pasteService) {
        this.pasteService = pasteService;
    }

    @ApiOperation("Paste_添加")
    @Log
    @Permission
    @PostMapping("/add")
    public Result<?> add(@RequestBody Paste paste) throws Exception {
        return pasteService.add(paste);
    }

    @ApiOperation("Paste_删除")
    @Log
    @Permission
    @PostMapping("/delete")
    public Result<?> delete(Integer id) throws Exception {
        return pasteService.delete(id);
    }

    @ApiOperation("Paste_修改")
    @Log
    @Permission
    @PostMapping("/update")
    public Result<?> update(@Valid @RequestBody Paste paste) throws Exception {
        return pasteService.update(paste);
    }

    @ApiOperation("Paste_查询所有")
    @Log
    @Permission
    @GetMapping("/list")
    public Result<?> list() throws Exception {
        pasteService.deleteExpired();
        return pasteService.findAll();
    }

    @ApiOperation("Paste_根据id查询")
    @Log
    @Permission
    @GetMapping("/get")
    public Result<?> get(Integer id) throws Exception {
        return pasteService.findById(id);
    }

    @ApiOperation("Paste_根据id查询")
    @Log
//    @Permission
    @GetMapping("/get/{id}")
    public String getText(@PathVariable Integer id) throws Exception {
        Paste paste = (Paste) Objects.requireNonNull(pasteService.findById(id).getDetail(), InternationalizationUtil.translate("query.failure"));
        return paste.getText();
    }

    @ApiOperation("Paste_查询最后添加")
    @Log
    @Permission
    @GetMapping(value = "/last")
    public String last() throws Exception {
        Paste paste = (Paste) Objects.requireNonNull(pasteService.findLast().getDetail(), InternationalizationUtil.translate("query.failure"));
        return paste.getText();
    }

    @ApiOperation("Paste_查询私密")
    @Log
    @Permission
    @GetMapping("/private")
    public Result<?> findPrivate() throws Exception {
        return pasteService.findPrivate();
    }

    @Scheduled(fixedRate = 60 * 60 * 1000) // 每小时执行一次
    public void deleteExpired() throws Exception {
        pasteService.deleteExpired();
    }

    @ApiOperation("Paste_导入")
    @Log
    @Permission
    @PostMapping("/import")
    public Result<?> importPastes(@RequestParam String json) throws Exception {
        return pasteService.importPastes(json);
    }

}
