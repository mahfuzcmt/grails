(function() {
    var Task = function(token, name, block) {
        this.token = token
        this.name = name
        this.statusBlock = block
        this.dom = $("<span class='" + token + "'><label class='task-label'>" + this.name + "</label><span class='task-progress-bar'></span></span>")
        this.progress = new ProgressBar(this.dom.find(".task-progress-bar"))
        this.progress.render()
    }

    Task.prototype.fetchStatus = function() {
        var _self = this;
        var progressCount = function() {
            bm.ajax({
                url: app.baseUrl + "task/progressCount",
                data: {token: _self.token},
                success: function(resp) {
                    _self.progress.setPosition(resp.done)
                    if(resp.done == 100) {
                        StatusBarManager.remove(_self.token)
                        delete tasks[_self.token]
                    } else {
                        setTimeout(progressCount, 1000)
                    }
                },
                error: function() {
                    StatusBarManager.remove(_self.token)
                    delete tasks[_self.token]
                }
            })
        }
        progressCount()
    }

    var tasks = []
    window.TaskManager = {
        init: function() {
            bm.ajax({
                url: app.baseUrl + "task/runningTasks",
                success: function(resp) {
                    tasks = loadTasks(resp)
                }
            })
        },
        createTask: function(elem) {
            var task = new Task(elem.token, elem.name, {});
            var block = StatusBarManager.allocate(elem.token, "task-status", elem.detail_url ? function() {
                bm.taskPopup(elem.detail_url, elem, {width: 800, clazz: "task-popup " + elem.name.sanitize()})
            } : null);
            task.fetchStatus();
            block.set(task.dom);
            task.statusBlock = block;
            tasks[elem.token] = task;
            return task;
        }
    };

    function loadTasks(taskListObj) {
        tasks = []
        $.each(taskListObj, function(index, elem) {
            var task = new Task(elem.token, elem.name, {})
            var block = StatusBarManager.allocate(elem.token, "task-status", elem.detail_url ? function() {
                if(elem.detail_viewer) {
                    try {
                        elem.detail_viewer = eval(elem.detail_viewer)
                    } finally {}
                }
                bm.taskPopup(elem.detail_url, elem, {width: 800, clazz: "task-popup " + elem.name.sanitize()})
            } : null)
            task.fetchStatus()
            block.set(task.dom)
            task.statusBlock = block
            task.progress.render()
            tasks[elem.token] = task
        })
        return tasks
    }
})()