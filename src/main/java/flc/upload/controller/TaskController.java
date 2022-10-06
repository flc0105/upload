package flc.upload.controller;

import flc.upload.model.Result;
import flc.upload.model.Task;
import flc.upload.service.TaskService;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/task")
public class TaskController {
    private TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping("/add")
    public Result add(@RequestParam("text") String text) throws Exception {
        return taskService.add(new Task(text, false));
    }

    @PostMapping("/delete")
    public Result delete(@RequestParam("id") Integer id) throws Exception {
        return taskService.delete(id);
    }

    @PostMapping("/check")
    public Result check(@RequestParam("id") Integer id, @RequestParam("checked") boolean checked) throws Exception {
        return taskService.update(new Task(id, null, checked));
    }

    @PostMapping("/list")
    public Result list() throws Exception {
        return taskService.findAll();
    }
}
