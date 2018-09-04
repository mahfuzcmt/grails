package com.webcommander.adapter

import grails.core.GrailsApplicationLifeCycleAdapter

class CommanderLifeCycleAdapter extends GrailsApplicationLifeCycleAdapter {
    @Override
    Closure doWithSpring() {
        return super.doWithSpring()
    }

    @Override
    void onStartup(Map<String, Object> event) {

    }
}
