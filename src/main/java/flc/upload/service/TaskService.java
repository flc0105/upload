package flc.upload.service;

import flc.upload.mapper.TaskMapper;
import flc.upload.model.Result;
import flc.upload.model.Task;
import org.springframework.stereotype.Service;

@Service
public class TaskService {
    private TaskMapper taskMapper;

    public TaskService(TaskMapper taskMapper) {
        this.taskMapper = taskMapper;
    }

    public Result add(Task task) throws Exception {
        return new Result<>(taskMapper.add(task) != 0, null);
    }

    public Result delete(Integer id) throws Exception {
        return new Result<>(taskMapper.delete(id) != 0, null);
    }

    public Result update(Task task) throws Exception {
        return new Result<>(taskMapper.update(task) != 0, null);
    }

    public Result findAll() throws Exception {
        return new Result<>(true, null, taskMapper.findAll());
    }
}
