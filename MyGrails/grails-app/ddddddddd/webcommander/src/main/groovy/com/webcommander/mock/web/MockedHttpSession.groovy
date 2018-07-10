package com.webcommander.mock.web

import com.webcommander.util.AppUtil
import com.webcommander.util.StringUtil
import grails.gsp.PageRenderer
import grails.util.Holders
import grails.web.servlet.mvc.GrailsHttpSession
import org.grails.web.servlet.mvc.GrailsWebRequest
import org.springframework.web.context.request.RequestContextHolder

import javax.servlet.ServletContext
import javax.servlet.http.HttpServletRequest

/**
 * Created by LocalZobair on 08/11/2016.*/
class MockedHttpSession extends GrailsHttpSession {

    MockedHttpSession() { super(null) }

    private LinkedHashMap attributes = [:]

    Object getAttribute(String name) {
        attributes[name]
    }

    Enumeration getAttributeNames() {
        Collections.enumeration(attributes.keySet())
    }

    long getCreationTime() {
        0
    }

    String getId() {
        StringUtil.uuid
    }

    long getLastAccessedTime() {
        0
    }

    int getMaxInactiveInterval() {
        0
    }

    ServletContext getServletContext() {
        Holders.servletContext
    }

    @Deprecated
    javax.servlet.http.HttpSessionContext getSessionContext() {
        null
    }

    @Deprecated
    Object getValue(String name) {
        null
    }

    @Deprecated
    String[] getValueNames() {
        [] as String[]
    }

    @Deprecated
    void putValue(String name, Object value) {}

    @Deprecated
    void removeValue(String name) {}

    void invalidate() {}

    boolean isNew() { true }

    void removeAttribute(String name) {
        attributes.remove(name)
    }

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpSession#setAttribute(java.lang.String, java.lang.Object)
     */

    void setAttribute(String name, Object value) {
        attributes[name] = value
    }

    void setMaxInactiveInterval(int arg0) {}
}

class MockWebRequest {
    static getMockedRequest(HttpServletRequest _request) {
        new GrailsWebRequest(_request, PageRenderer.PageRenderResponseCreator.createInstance(new PrintWriter(new StringWriter())), Holders.servletContext) {
            private MockedHttpSession mockSession

            GrailsHttpSession getSession() {
                MockedHttpSession __session = this.mockSession
                if (__session == null) {
                    __session = this.mockSession = new MockedHttpSession()
                }
                return __session
            }
        }
    }
}