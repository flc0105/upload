package flc.upload.controller;

import flc.upload.annotation.Log;
import flc.upload.annotation.Token;
import flc.upload.model.Paste;
import flc.upload.model.Result;
import flc.upload.service.PasteService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

@RestController
@Api(tags = "Paste")
@CrossOrigin(origins = "*")
@RequestMapping("/paste")
public class PasteController {
    private final PasteService pasteService;

    public PasteController(PasteService pasteService) {
        this.pasteService = pasteService;
    }

    @Log
    @ApiOperation("Paste_添加")
    @PostMapping("/add")
    public Result add(@RequestBody Paste paste) throws Exception {
        return pasteService.add(paste);
    }

    @Log
    @Token
    @ApiOperation("Paste_删除")
    @PostMapping("/delete")
    public Result delete(Integer id) throws Exception {
        return pasteService.delete(id);
    }

    @Log
    @Token
    @ApiOperation("Paste_修改")
    @PostMapping("/update")
    public Result update(@Valid @RequestBody Paste paste) throws Exception {
        return pasteService.update(paste);
    }

    @Log
    @ApiOperation("Paste_查询所有")
    @PostMapping("/list")
    public Result list() throws Exception {
        pasteService.deleteExpired();
        return pasteService.findAll();
    }

    @Log
    @ApiOperation("Paste_根据id查询")
    @PostMapping("/get")
    public Result get(Integer id, HttpServletRequest request) throws Exception {
        pasteService.deleteExpired();
        return pasteService.findById(id);
    }


    @Log
    @ApiOperation("Paste_根据id获取文本")
    @GetMapping("/get/{id}")
    public String getById(@PathVariable Integer id, HttpServletRequest request) throws Exception {
        pasteService.deleteExpired();
        Paste paste = (Paste) pasteService.findById(id).getDetail();
        if (paste == null) {
            return "404";
        }
        return paste.getText();
    }

    @Log
    @ApiOperation("Paste_查询最后添加的文本")
    @RequestMapping(value = "/last", method = {RequestMethod.GET, RequestMethod.POST})
    public String last() throws Exception {
        pasteService.deleteExpired();
        Paste paste = (Paste) pasteService.findLast().getDetail();
        if (paste == null) {
            return "404";
        }
        return paste.getText();
    }

    @Log
    @Token
    @ApiOperation("Paste_查询私密")
    @PostMapping("/unlisted")
    public Result unlisted() throws Exception {
        pasteService.deleteExpired();
        return pasteService.findUnlisted();
    }

    @Scheduled(fixedRate = 60 * 60 * 1000)
    public void scheduledDeleteExpired() throws Exception {
        pasteService.deleteExpired();
    }

}
