var UndoRedoFactory = (function() {
    var DEFAULTS = {
        hotkey_enabled: true,
        hotkey_active: true,
        changeState: function() {}
    }

    var instanceCount = 0;

    var UndoRedoManager = function() {
        this.states = []
        this.index = 0
        this.hash = ++instanceCount
    }

    var _u = UndoRedoManager.prototype

    _u.bindGlobalNNavigator = function() {
        var _self = this;
        if(this.config.hotkey_enabled) {
            $(document).on("keyup." + this.hash + ".ctrl_z", function() {
                if(_self.config.hotkey_active) {
                    _self.undo()
                }
            }).on("keyup." + this.hash + ".ctrl_y", function() {
                if(_self.config.hotkey_active) {
                    _self.redo()
                }
            })
        }
        if(this.undo_btn) {
            this.undo_btn.on("click", function() {
                _self.undo()
            })
        }
        if(this.redo_btn) {
            this.redo_btn.on("click", function() {
                _self.redo()
            })
        }
    }

    _u.activateHotkey = function() {
        this.config.hotkey_active = true
    }

    _u.deactivateHotkey = function() {
        this.config.hotkey_active = false
    }

    _u.destroy = function() {
        if(this.config.hotkey_enabled) {
            $(document).off("." + this.hash)
        }
        if(this.undo_btn) {
            this.undo_btn.off("click")
        }
        if(this.redo_btn) {
            this.redo_btn.off("click")
        }
    }

    _u.undo = function() {
        if(this.index == 0) {
            return;
        }
        this.index--
        var state = this.states[this.index];
        this.undo_btn.trigger("before_undo", state)
        if(state.doer) {
            state.doer.undo.call(state)
        } else {
            this.config.changeState(state.type, state.previous, state.current)
        }
        if(!this.index) {
            this.undo_btn.addClass("disabled").attr("disabled", "disabled")
        }
        if(this.redo_btn && this.redo_btn.is(".disabled")) {
            this.redo_btn.removeClass("disabled").removeAttr("disabled")
        }
        if(this.index == 0 && this.undo_btn) {
            this.undo_btn.trigger("undo_bottom_reached")
        }
        this.undo_btn.trigger("undo", state)
    }

    _u.redo = function() {
        if(this.index == this.states.length) {
            return;
        }
        var state = this.states[this.index];
        this.redo_btn.trigger("before_redo", state)
        if(state.doer) {
            state.doer.redo.call(state)
        } else {
            this.config.changeState(state.type, state.current, state.previous)
        }
        this.index++
        if(this.index == this.states.length) {
            this.redo_btn.addClass("disabled").attr("disabled", "disabled")
        }
        if(this.undo_btn && this.undo_btn.is(".disabled")) {
            this.undo_btn.removeClass("disabled").removeAttr("disabled")
        }
        if(this.index == 0 && this.states.length) {
            this.undo_btn.trigger("redo_top_reached")
        }
        this.redo_btn.trigger("redo", state)
    }

    _u.reset = function() {
        this.index = 0;
        this.states = []
        if(this.redo_btn && !this.redo_btn.is(".disabled")) {
            this.redo_btn.addClass("disabled").attr("disabled", "disabled")
        }
        if(this.undo_btn && !this.undo_btn.is(".disabled")) {
            this.undo_btn.addClass("disabled").attr("disabled", "disabled")
        }
    }

    _u.push = function(type, previous, current, doer) {
        if(this.index != this.states.length) {
            this.states.splice(this.index)
        }
        if(typeof type == "object" && type.undo) {
            doer = type
            type = undefined
        }
        this.states.push({
            type: type,
            previous: previous,
            current: current,
            doer: doer
        })
        this.index++
        if(this.undo_btn && this.undo_btn.is(".disabled")) {
            this.undo_btn.removeClass("disabled").removeAttr("disabled")
        }
        if(this.redo_btn && !this.redo_btn.is(".disabled")) {
            this.redo_btn.addClass("disabled").attr("disabled", "disabled")
        }
    }

    _u.isAvailable = function() {
        return this.states.length
    }

    return {
        createManager: function(undoNavigator, redoNavigator, options) {
            var manager = new UndoRedoManager()
            manager.config = $.extend({}, DEFAULTS, options)
            manager.undo_btn = undoNavigator
            manager.redo_btn = redoNavigator
            manager.bindGlobalNNavigator();
            return manager
        }
    }
})()