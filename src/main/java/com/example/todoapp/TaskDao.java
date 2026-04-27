package com.example.todoapp;

import java.util.*;

/**
 * Data Access Object for {@link Task} model.
 */
public class TaskDao {

    private final Map<Integer, Task> storage = new HashMap<>();

    {
        save(new Task(1, "Réviser DS de maths", "Séries numériques et probabilités.", false));
        save(new Task(2, "Valider mon PIVE", "PIVE Club Poker.", true));
        save(new Task(3, "Choisir mon parcours de 4A", "SIR ou SIA ?", false));
    }

    /**
     * Persist {@link Task} model.
     * @param task task to save.
     * @return task model.
     */
    public Task save(Task task) {
        storage.put(task.id(), task);
        return task;
    }

    /**
     * Retrieve {@link Task} model by id.
     * @param id identifier of the {@link Task}.
     * @return {@link Task} model wrapped by Optional.
     */
    public Optional<Task> findById(int id) {
        return Optional.ofNullable(storage.get(id));
    }

    /**
     * Retrieve the list of all {@link Task}s.
     * @return {@link Task} collection
     */
    public Collection<Task> getTasksList(boolean todoOnly) {
        Collection<Task> tasksList = new ArrayList<>();

        for (Task task : storage.values()) {
            if (todoOnly) {
                // So we only add not done tasks
                if (!task.done()) {
                    tasksList.add(task);
                }
            }
            else {
                // We add all of them
                tasksList.add(task);
            }
        }

        return tasksList;
    }

    /**
     * Remove a {@link Task} by its id
     * @param id identifier of the {@link Task}.
     */
    public void deleteTaskById(int id) {
        storage.remove(id);
    }
}
