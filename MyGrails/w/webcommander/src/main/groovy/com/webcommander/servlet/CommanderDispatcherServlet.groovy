package com.webcommander.servlet

import org.grails.web.servlet.mvc.GrailsDispatcherServlet

import javax.servlet.ServletException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class CommanderDispatcherServlet extends GrailsDispatcherServlet {
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if(["PROPFIND", "MKCOL", "MOVE"].contains(req.getMethod())) {
            processRequest(req, resp);
        } else {
            super.service(req, resp);
        }
    }
}