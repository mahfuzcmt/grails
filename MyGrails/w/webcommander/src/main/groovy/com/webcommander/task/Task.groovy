package com.webcommander.task

import com.webcommander.constants.NamedConstants
import com.webcommander.events.AppEventManager
import com.webcommander.util.StringUtil
import grails.async.Promise
import com.webcommander.tenant.Thread
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * Created with IntelliJ IDEA.
 * User: akter
 * Date: 11/3/13
 * Time: 5:04 PM
 * To change this template use File | Settings | File Templates.
 */
class Task<T> implements Serializable, Promise<T> {

    public final static long serialVersionUID = 4661949260367704076;

    String token
    String name
    String status
    String detail_url
    String detail_viewer
    String detail_status_url
    Integer progress
    Integer totalRecord = 0
    Integer recordComplete = 0
    Integer errorCount = 0
    TaskLogger taskLogger
    Map meta = new ConcurrentHashMap()

    private Task thenTask

    Task(String name) {
        this.name = name
        token = "TASK-" + StringUtil.uuid
        progress = 0
        taskLogger = new TaskLogger()
    }

    @Override
    int hashCode() {
        if (name) {
            return ("Task: " + name).hashCode();
        }
        return super.hashCode()
    }

    @Override
    boolean equals(Object o) {
        if (!o instanceof Task) {
            return false
        }
        if (token) {
            return token == o.token;
        }
        return super.equals(o);
    }

    @Override
    boolean cancel(boolean mayInterruptIfRunning) {
        return false
    }

    @Override
    boolean isCancelled() {
        return false
    }

    @Override
    boolean isDone() {
        return false
    }

    T get() {
        return null
    }

    T get(long timeout, TimeUnit units) throws Throwable {
        return null
    }

    Promise<T> leftShift(Closure callable) {
        then callable
    }

    @Override
    Promise<T> accept(T value) {
        return null
    }

    Promise onComplete(Closure callable) {
        AppEventManager.on(token + "-task-complete", token, callable)
        return this
    }

    Promise onError(Closure callable) {
        AppEventManager.on(token + "-task-error", token, callable)
        return this
    }

    Promise then(Closure runnable) {
        return then("Next Of - " + name, runnable)
    }

    Promise then(String name, Closure runnable) {
        Task task = new Task(name)
        Task lastThenTask = this
        while(lastThenTask.thenTask) {
            lastThenTask = lastThenTask.thenTask
        }
        lastThenTask.thenTask = task
        AppEventManager.on(token + "-task-complete", token, {
            task.async runnable
        })
        AppEventManager.on(token + "-task-error", token, { t ->
            AppEventManager.fire(task.token + "-task-error", [t])
            AppEventManager.off("*", task.token)
        })
        return task;
    }

    public void async(Closure runner) {
        TaskManager.add(this)
        def _this = this;
        Thread.start {
            try {
                status = NamedConstants.TASK_STATUS.RUNNING;
                runner.call(_this)
                status = NamedConstants.TASK_STATUS.COMPLETE
                AppEventManager.fire(token + "-task-complete")
            } catch (Throwable t) {
                status = NamedConstants.TASK_STATUS.ABORTED
                AppEventManager.fire(token + "-task-error", [t])
            } finally {
                TaskManager.remove(_this)
                AppEventManager.off("*", token)
            }
        }
    }

    public void serialize(File dumpFile) {
        dumpFile.parentFile.mkdirs()
        dumpFile.withOutputStream { out ->
            ObjectOutputStream oOut = new ObjectOutputStream(out)
            oOut.writeObject(this)
            oOut.close()
        }
    }

    public static Task getInstance(File serializedFile) {
        ObjectInputStream oIn = new ObjectInputStream(new FileInputStream(serializedFile)) {
            @Override
            protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
                if(desc.name == "com.webcommander.task.Task") {
                    return Task
                }
                return super.resolveClass(desc)
            }
        }
        return oIn.readObject()
    }
}