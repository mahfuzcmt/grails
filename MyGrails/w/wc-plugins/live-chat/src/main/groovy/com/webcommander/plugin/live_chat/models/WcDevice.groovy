package com.webcommander.plugin.live_chat.models

import javapns.devices.implementations.basic.BasicDevice

/**
 * Created by sajedur on 11-12-2014.
 */
class WcDevice extends BasicDevice {
    Boolean isActive

    public WcDevice(String deviceId, Boolean isActive) {
        super(deviceId, true)
        this.isActive = isActive
    }
}
