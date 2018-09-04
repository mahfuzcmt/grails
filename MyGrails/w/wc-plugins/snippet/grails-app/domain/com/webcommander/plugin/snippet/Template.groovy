package com.webcommander.plugin.snippet

import com.webcommander.common.LargeData

class Template {
    String uuid
    LargeData info
    LargeData html
    LargeData css

    static constraints = {
        uuid unique: true
    }
}