package flc.upload.controller;

import flc.upload.model.Result;
import flc.upload.model.Text;
import flc.upload.service.TextService;
import flc.upload.util.AuthUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@RestController
public class TextController {
    @Autowired
    TextService textService;

    @PostMapping("/getText")
    public List<Text> getText() {
        return textService.findAll();
    }

    @PostMapping("/addText")
    public Result addText(Text text) {
        text.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        try {
            textService.add(text);
        } catch (Exception e) {
            return new Result(e.getMessage(), false);
        }
        return new Result(true);
    }

    @PostMapping("/deleteText")
    public Result deleteText(Integer id, HttpServletRequest request, HttpServletResponse response) {
        Result result = new Result(false);
        if (!AuthUtil.verifyToken(request, response)) {
            result.setMsg("没有权限");
            return result;
        }
        textService.delete(id);
        result.setSuccess(true);
        return result;
    }
}
