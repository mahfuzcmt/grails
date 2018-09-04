package com.webcommander.task

import com.webcommander.constants.NamedConstants
import com.webcommander.manager.CacheManager

class TaskManager {

    private static List<Task> getTaskList() {
        List<Task> taskList = CacheManager.get(NamedConstants.CACHE.TENANT_STATIC, "task_list")
        if(taskList == null) {
            CacheManager.cache(NamedConstants.CACHE.TENANT_STATIC, taskList = Collections.synchronizedList([]), -1, "task_list")
        }
        return taskList
    }


    static void remove(task) {
        taskList.remove(task)
    }

    static void add(Task task) {
        taskList.add(task)
    }

    static List<Task> getTasks() {
        return Collections.unmodifiableList(taskList)
    }

    static Task getByName(String name) {
        for(Task task : taskList) {
            if(name.equals(task.name)) {
                return task
            }
        }
        return null
    }

    static Task getByToken(String token) {
        for(Task task : taskList) {
            if(token.equals(task.token)) {
                return task
            }
        }
        return null
    }
}
