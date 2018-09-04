package com.webcommander.plugin.tab_accordion

import com.webcommander.ApplicationTagLib
import com.webcommander.common.CommanderMailService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

class TabAccordionService {
    CommanderMailService commanderMailService
    @Autowired
    @Qualifier("com.webcommander.ApplicationTagLib")
    ApplicationTagLib app


}
