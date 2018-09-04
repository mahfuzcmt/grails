package com.webcommander.admin

import com.webcommander.events.AppEventManager

class City {

    Long id
    String postCode
    String name

    static belongsTo = [state: State]

    static mapping = {
        sort ("state.id": "asc")
    }

    static constraints = {
        name(blank: false, size: 2..100)
        postCode(blank: false, maxSize: 50)
    }

    static void initialize() {
        def _init = {
//            if(City.count() == 0) {
//                String fileLocation = Holders.servletContext.getRealPath("WEB-INF")
//                DataSource dataSource = Holders.applicationContext.getBean("dataSource")
//                Sql sql = new Sql(dataSource)
//                new File(fileLocation + "/dbEntries/city.sql").eachLine { citySql ->
//                    sql.execute(citySql)
//                }
//            }
        }
        if (State.count()) {
            _init()
        } else {
            AppEventManager.one("state-bootstrap-init", "bootstrap-init", _init)
        }
    }
}
