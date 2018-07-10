package com.webcommander.task

import org.grails.plugins.web.mime.FormatInterceptor

import java.util.concurrent.ConcurrentHashMap

/**
 * Created with IntelliJ IDEA.
 * User: akter
 * Date: 11/27/13
 * Time: 3:40 PM
 * To change this template use File | Settings | File Templates.
 */
class MultiLoggerTask extends Task {
    public final static long serialVersionUID = 4661949360367704076;

    private Map<String, TaskLogger> loggers = new ConcurrentHashMap()

    MultiLoggerTask(String name) {
        super(name);
    }

    public void setActiveLogger(String name) {
        def logger = loggers[name];
        if(!logger) {
            logger = loggers[name] = new TaskLogger();
        }
        taskLogger = logger;
    }

    public TaskLogger getLogger(String name) {
        return loggers[name];
    }

    public Map<String, TaskLogger> getLoggers() {
        return loggers
    }

    public static MultiLoggerTask getInstance(File serializedFile) {
        FileInputStream str = new FileInputStream(serializedFile)
        ObjectInputStream oIn = new ObjectInputStream(str) {
            @Override
            protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
                if(desc.name == "com.webcommander.task.MultiLoggerTask") {
                    return MultiLoggerTask
                }
                return super.resolveClass(desc)
            }
        }
        return oIn.readObject()
    }

    public void async(Closure runner) {
        super.async {
            LinkedHashMap<String, Closure> loggers = null
            def original = runner.delegate
            try {
                final interceptor = new FormatInterceptor()
                runner.delegate = interceptor
                runner.resolveStrategy = Closure.DELEGATE_ONLY
                runner.call()
                loggers = interceptor.formatOptions
            } finally {
                runner.delegate = original
                runner.resolveStrategy = Closure.OWNER_FIRST
            }
            loggers.each { name, _runner ->
                setActiveLogger(name)
                _runner.call(name)
            }
        }
    }
}