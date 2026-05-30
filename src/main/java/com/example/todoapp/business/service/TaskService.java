package com.example.todoapp.business.service;

import com.example.todoapp.JsonUtils;
import com.example.todoapp.business.model.Task;
import com.example.todoapp.dao.TaskDao;
import com.example.todoapp.dto.TaskGetDTO;
import com.example.todoapp.dto.TaskPostDTO;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

public class TaskService {

    private final TaskDao taskDao = new TaskDao();

    public Task createTask(TaskPostDTO input) {
        return this.taskDao.save(input);
    }

    public Optional<TaskGetDTO> getTaskById(int id) {
        Optional<Task> task = this.taskDao.findById(id);
        if (task.isEmpty()) {
            return Optional.empty();
        }
        TaskGetDTO output = new TaskGetDTO(task.get().id(), task.get().title(), task.get().description(), task.get().done());
        return Optional.of(output);
    }

    public void deleteTaskById(int id) {
        this.taskDao.deleteTaskById(id);
    }

    public void updateTaskById(int id, String newTitle, String newDesc, boolean newDone) {
        this.taskDao.updateTaskById(id, newTitle, newDesc, newDone);
    }

    public Collection<TaskGetDTO> getTasksList(boolean todoOnly) {
        Collection<Task> tasksList = this.taskDao.getTasksList(todoOnly);
        Collection<TaskGetDTO> output = new ArrayList<>();

        for (Task task : tasksList) {
            output.add(new TaskGetDTO(task.id(), task.title(), task.description(), task.done()));
        }
        return output;
    }
}
