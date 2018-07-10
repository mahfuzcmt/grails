package com.webcommander.models.blueprints

import com.webcommander.task.MultiLoggerTask

interface DisposableUtilServiceModel {
    Integer countDisposableItems(String itemType)
    void removeDisposableItems(String itemType, MultiLoggerTask task)
}