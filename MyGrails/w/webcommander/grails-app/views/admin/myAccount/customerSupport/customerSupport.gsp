<%@ page import="com.webcommander.converter.json.JSON" %>
<div class="toolbar-share">
</div>
<div class="app-tab-content-container">
    <div class="header">
        <span class="heading-title"><g:message code="customer.support"/></span>
        <div class="toolbar toolbar-right">
            <div class="tool-group toolbar-btn create create-new-message"><g:message code="new.message"/></div>
        </div>
    </div>
    <div class="body">
        <div class="table">
            <g:each in="${messages}" var="message">
                <div class="support-message message-thread" message-id="${message.id}">
                    <div class="intro">
                        <div class="person">
                            <span class="avater"></span>
                            <div class="message-info">
                                <span class="name">${message.customerName}</span>
                                <div class="date">${JSON.dateFormatter.parse(message.created).toAdminFormat(true, false, session.timezone)}</div>
                            </div>
                        </div>
                        <span class="btn reply-message"><g:message code="reply"/></span>
                        <span class="tool-icon expander"></span>
                    </div>
                    <div class="message">${message.message}</div>
                    <div class="message-replies">
                        <g:each in="${message.supportMessageReplies}" var="reply">
                            <div class="reply message-thread">
                                <div class="intro">
                                    <div class="person">
                                        <span class="avater"></span>
                                    </div>
                                    <div class="message-info">
                                        <span class="name">${reply.responderName}</span>
                                        <div class="date">${JSON.dateFormatter.parse(reply.created).toAdminFormat(true, false, session.timezone)}</div>
                                    </div>
                                </div>
                                <div class="message">${reply.message}</div>
                            </div>
                        </g:each>
                    </div>
                </div>
            </g:each>
        </div>
    </div>

    <div class="footer">
        <ui:perPageCountSelector prepand="${['5': '05']}"/>
        <paginator total="${count}" offset="${params.offset}" max="${params.max}"></paginator>
    </div>
</div>
