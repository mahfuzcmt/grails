package com.webcommander.controllers.task

import com.webcommander.task.Task
import com.webcommander.task.TaskService
import grails.converters.JSON

class TaskController {

    TaskService taskService

    def runningTasks() {
        List<Task> taskList = taskService.runningTasks()
        List tasks = []
        taskList.eachWithIndex { task, i ->
            Map taskProp = [
                token : task.token,
                name : task.name,
                detail_url: task.detail_url,
                detail_status_url: task.detail_status_url,
                detail_viewer: task.detail_viewer
            ]
            tasks.add(taskProp)
        }
        render(tasks as JSON)
    }

    def progressCount() {
        String token = params.token
        Task task = taskService.getByToken(token)
        if(task) {
            render([status: task.status, done : task.progress, total: task.totalRecord, complete: task.recordComplete, error: task.errorCount] as JSON)
        } else {
            render([status: "error", message: g.message(code: "no.task.found")] as JSON)
        }
    }

    def loadRunningTask() {
        String token = params.taskToken
        Task task = TaskManager.getByToken(token)
        render(view: "/admin/task/runningTask", model: [task: task])
    }
}
