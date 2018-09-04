package com.webcommander.task

import com.webcommander.constants.NamedConstants

/**
 * Created with IntelliJ IDEA.
 * User: akter
 * Date: 11/4/13
 * Time: 2:33 PM
 * To change this template use File | Settings | File Templates.
 */
class TaskLogger implements Serializable {

    public final static long serialVersionUID = 4661949360367714076;

    public static class Log implements Serializable {
        public final static long serialVersionUID = 4661949360367704077;

        String type
        String logFor
        String msg
        Serializable[] args
    }

    List<Log> logs = []
    Map<String, List<Log>> logVsType = [:]

    public void success(String logFor, String msg, Object[] args = null) {
        log(NamedConstants.TASK_LOGGER_STATUS.SUCCESS, logFor, msg, args)
    }

    public void warning(String logFor, String msg, Object[] args = null) {
        log(NamedConstants.TASK_LOGGER_STATUS.WARNING, logFor, msg, args)
    }

    public void error(String logFor, String msg, Object[] args = null) {
        log(NamedConstants.TASK_LOGGER_STATUS.ERROR, logFor, msg, args)
    }

    private void log(String type, String logFor, String msg, Object[] args) {
        def log = new Log(type: type, logFor: logFor, msg: msg, args: args)
        List typeLog = logVsType[type]
        if(!typeLog) {
            typeLog = logVsType[type] = []
        }
        typeLog.add(log)
        this.logs.add(log)
    }

    public List getSuccess() {
        return (List)logVsType[NamedConstants.TASK_LOGGER_STATUS.SUCCESS]
    }

    public List getWarning() {
        return (List)logVsType[NamedConstants.TASK_LOGGER_STATUS.WARNING]
    }

    public List getError() {
        return (List)logVsType[NamedConstants.TASK_LOGGER_STATUS.ERROR]
    }

    public List getLogs(String type) {
        return (List)logVsType[type]
    }
}