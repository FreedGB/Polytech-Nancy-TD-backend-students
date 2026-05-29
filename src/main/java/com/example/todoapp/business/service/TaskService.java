package com.example.todoapp.business.service;

import com.example.todoapp.business.model.Task;
import com.example.todoapp.dao.TaskDao;

import java.util.Collection;
import java.util.Optional;

public class TaskService {

    private final TaskDao taskDao = new TaskDao();

    public Task createTask(Task input) {
        int newId = this.taskDao.calculateNextId();
        Task newTask = new Task(newId, input.title(), input.description(), input.done());
        return this.taskDao.save(newTask);
    }

    public Optional<Task> getTaskById(int id) {
        return this.taskDao.findById(id);
    }

    public void deleteTaskById(int id) {
        this.taskDao.deleteTaskById(id);
    }

    public void updateTaskById(int id, String newTitle, String newDesc, boolean newDone) {
        this.taskDao.updateTaskById(id, newTitle, newDesc, newDone);
    }

    public Collection<Task> getTasksList(boolean todoOnly) {
        return this.taskDao.getTasksList(todoOnly);
    }
}
