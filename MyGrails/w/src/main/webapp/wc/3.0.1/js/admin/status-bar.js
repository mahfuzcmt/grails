(function() {
    var blocks = {}

    var StatusBlock = function(statusId, clazz, onClick) {
        this.id = statusId
        this.clazz = clazz
        this.handler = onClick
        this.render()
    }

    StatusBlock.prototype.render = function() {
        var el = this.el = $("<span class='status-block'></span>");
        $("#status-bar").append(el)
        if(this.clazz) {
            el.addClass(this.clazz)
        }
        if(this.handler) {
            el.click(this.handler)
        }
    }

    StatusBlock.prototype.set = function(content) {
        this.el.html(content)
    }

    StatusBlock.prototype.remove = function() {
        this.el.remove()
        delete blocks[this.id]
    }

    StatusBlock.prototype.hide = function() {
        this.el.hide()
    }

    StatusBlock.prototype.show = function() {
        this.el.show()
    }

    window.StatusBarManager = {
        allocate: function(statusId, clazz, onClick) {
            return blocks[statusId] = new StatusBlock(statusId, clazz, onClick)
        },
        get: function(statusId) {
            return blocks[statusId];
        },
        set: function(statusId, content) {
            blocks[statusId].set(content)
        },
        remove: function(statusId) {
            var block = blocks[statusId]
            if(block) {
                block.remove();
            }
        },
        hide: function(statusId) {
            blocks[statusId].hide()
        },
        show: function(statusId) {
            blocks[statusId].show()
        }
    }
})()
