package com.webcommander.config

import com.webcommander.Page

class StorePageAssoc {
    Long id
    Boolean isActive = true
    StoreDetail store
    Page page
    Page parent // Parent page reference
}
