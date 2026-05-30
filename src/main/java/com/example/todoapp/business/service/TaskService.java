package com.example.todoapp.business.service;

import com.example.todoapp.business.model.Task;
import com.example.todoapp.dao.TaskDao;
import com.example.todoapp.dto.TaskOutputDTO;
import com.example.todoapp.dto.TaskPostDTO;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

public class TaskService {

    private final TaskDao taskDao = new TaskDao();

    public TaskOutputDTO createTask(TaskPostDTO input) {
        Task task =this.taskDao.save(input);
        return new TaskOutputDTO(task.id(), task.title(), task.description(), task.done());
    }

    public Optional<TaskOutputDTO> getTaskById(int id) {
        Optional<Task> task = this.taskDao.findById(id);
        if (task.isEmpty()) {
            return Optional.empty();
        }
        TaskOutputDTO output = new TaskOutputDTO(task.get().id(), task.get().title(), task.get().description(), task.get().done());
        return Optional.of(output);
    }

    public void deleteTaskById(int id) {
        this.taskDao.deleteTaskById(id);
    }

    public void updateTaskById(int id, String newTitle, String newDesc, boolean newDone) {
        this.taskDao.updateTaskById(id, newTitle, newDesc, newDone);
    }

    public Collection<TaskOutputDTO> getTasksList(boolean todoOnly) {
        Collection<Task> tasksList = this.taskDao.getTasksList(todoOnly);
        Collection<TaskOutputDTO> output = new ArrayList<>();

        for (Task task : tasksList) {
            output.add(new TaskOutputDTO(task.id(), task.title(), task.description(), task.done()));
        }
        return output;
    }
}
