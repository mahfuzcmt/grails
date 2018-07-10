package com.webcommander.util

import com.webcommander.converter.XML
import com.webcommander.converter.json.JSON

/**
 * Created by zobair on 04/02/2015.
 */
class RestProcessor {
    void rest(String response) {
        def controller = this.hasProperty("owner") ? this.owner : this
        if(controller.params.format) {
            if(controller.params.format == "xml") {
                controller.response.contentType = "text/xml"
            } else if(controller.params.format == "json") {
                controller.response.contentType = "application/json"
            }
        } else {
            controller.request.withMime {
                xml {
                    controller.response.contentType = "text/xml"
                }
                json {
                    controller.response.contentType = "application/json"
                }
            }
        }
        controller.render(response);
    }

    void rest(Map response, Map config = [:]) {
        def processed
        def controller = this.hasProperty("owner") ? this.owner : this
        if(controller.params.format) {
            if(controller.params.format == "xml") {
                processed = new XML(response, config).toString();
                controller.response.contentType = "text/xml"
            } else if(controller.params.format == "json") {
                controller.response.contentType = "application/json"
                processed = new JSON(response, config).toString();
            }
        } else {
            controller.request.withMime {
                html {
                    processed = new JSON(response, config).toString();
                }
                xml {
                    processed = new XML(response, config).toString();
                    controller.response.contentType = "text/xml"
                }
                json {
                    controller.response.contentType = "application/json"
                    processed = new JSON(response, config).toString();
                }
            }
        }
        controller.render(processed);
    }
}
