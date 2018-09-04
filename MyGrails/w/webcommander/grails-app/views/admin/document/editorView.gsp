<%@ page import="com.webcommander.constants.DomainConstants; com.webcommander.constants.NamedConstants" %>
<div class="left-detail-panel bmui-tab">
    <div class="bmui-tab-header-container top-side-header">
        <div class="bmui-tab-header" data-tabify-tab-id="doc-designer-left-tab-doc-details">
            <span class="title"><g:message code="document.details"/></span>
        </div>
        <div class="bmui-tab-header" data-tabify-tab-id="doc-designer-left-tab-component-list">
            <span class="title"><g:message code="component"/></span>
        </div>
    </div>
    <div class="bmui-tab-body-container">
        <div id="bmui-tab-doc-designer-left-tab-doc-details" class="left-tab-container">
            <div class="create-edit-form document-details-from" action="${app.customResourceBaseUrl()}document/save">
                <input type="hidden" name="type" value="${document.type}"/>
                <div class="form-row">
                    <label><g:message code="name"/></label>
                    <input type="text" class="unique" name="name" validation="required maxlength[200]" maxlength="200" value="${params.layoutUsed.toBoolean() ? "" : document.name.encodeAsBMHTML()}" unique-action="isUnique?id=${document.id}">
                </div>
                <div class="form-row">
                    <label><g:message code="description"/></label>
                    <textarea name="description" validation="maxlength[1500]" maxlength="1500">${document.description.encodeAsBMHTML()}</textarea>
                </div>
                <div class="form-row">
                    <label><g:message code="paper.size"/></label>
                    <g:select name="paperSize" from="${NamedConstants.DOCUMENT_PAPER_SIZE.values()}" keys="${NamedConstants.DOCUMENT_PAPER_SIZE.keySet()}"/>
                </div>
                <div class="double-input-row">
                    <div class="form-row">
                        <label><g:message code="margin.top"/></label>
                        <span class="unit"><g:message code="mm"/></span>
                        <input type="text" name="padding-top" value="" restrict="decimal" validation="required max[200]">
                    </div><div class="form-row">
                        <label><g:message code="margin.right"/></label>
                        <span class="unit"><g:message code="mm"/></span>
                        <input type="text" name="padding-right" value="" validation="required max[200]">
                    </div>
                </div>
                <div class="double-input-row">
                    <div class="form-row">
                        <label><g:message code="margin.bottom"/></label>
                        <span class="unit"><g:message code="mm"/></span>
                        <input type="text" name="padding-bottom" value="" validation="required max[200]">
                    </div><div class="form-row">
                        <label><g:message code="margin.left"/></label>
                        <span class="unit"><g:message code="mm"/></span>
                        <input type="text" name="padding-left" value="" validation="required max[200]">
                    </div>
                </div>
                <div class="form-row">
                    <label><g:message code="font.family"/></label>
                    <g:select name="fontFamily" from="${NamedConstants.DOCUMENT_FONT_FAMILIES.values()}" keys="${NamedConstants.DOCUMENT_FONT_FAMILIES.keySet()}"/>
                </div>
                <div class="form-row">
                    <label><g:message code="font.size"/></label>
                    <g:select name="fontSize" from="${(5..30).collect {it + " px"}}" keys="${(5..30)}"/>
                </div>
            </div>
        </div>
        <div id="bmui-tab-doc-designer-left-tab-component-list" class="left-tab-container">
            <div class="component-body accordion-panel">
                <div class="label-bar"><a class="toggle-icon"></a>
                    <g:message code="basic.components"/>
                </div>
                <div class="accordion-item basic-component">
                    <div class="component-pair">
                        <span class="component-item icon text" data-min-width="50px" data-type="text">
                            <span class="title"><g:message code="text"/></span>
                        </span>
                        <g:if test="${document.type == 'invoice'}">
                            <span class="component-item icon full-width-component invoice-table" data-width="100%" data-type="invoice_table" data-resizable="false">
                                <span class="title"><g:message code="invoice.table"/></span>
                            </span>
                        </g:if>
                        <g:elseif test="${document.type == 'order'}">
                            <span class="component-item icon full-width-component order-table" data-width="100%" data-type="order_table" data-resizable="false">
                                <span class="title"><g:message code="order.table"/></span>
                            </span>
                        </g:elseif>
                        <g:elseif test="${document.type == 'delivery_docket'}">
                            <span class="component-item icon full-width-component delivery-docket-table" data-width="100%" data-type="delivery_docket_table" data-resizable="false">
                                <span class="title"><g:message code="delivery.docket.table"/></span>
                            </span>
                        </g:elseif>
                        <g:elseif test="${document.type == 'picking_slip'}">
                            <span class="component-item icon full-width-component picking-slip-table" data-width="100%" data-type="picking_slip_table" data-resizable="false">
                                <span class="title"><g:message code="picking.slip.table"/></span>
                            </span>
                        </g:elseif>
                    </div>
                    <div class="component-pair">
                        <span class="component-item icon image" data-type="image">
                            <span class="title"><g:message code="image"/></span>
                        </span>
                        <span class="component-item icon line" data-type="line">
                            <span class="title"><g:message code="line"/></span>
                        </span>
                    </div>
                    <div class="component-pair">
                        <span class="component-item icon area" data-type="area">
                            <span class="title"><g:message code="area"/></span>
                        </span>
                        <span class="component-item icon data-table" data-type="data_table" data-resizable="false">
                            <span class="title"><g:message code="data.table"/></span>
                        </span>
                    </div>
                </div>
            </div>
        </div>
    </div>
</div>
<div class="editor-main-panel">
    <g:if test="${document.content}">
        ${document.content}
    </g:if>
    <g:else>
        <div class="editing-area" style="padding-top: 20mm; padding-right: 20mm; padding-bottom: 20mm; padding-left: 20mm; width: 215.9mm; height: 279.4mm; overflow: hidden;"></div>
    </g:else>
</div>
<div class="toolbar-template" style="display: none">
    <div class="tool-group toolbar-btn cancel">
        <g:message code="cancel"/>
    </div>
    <div class="tool-group toolbar-btn reset">
        <g:message code="reset"/>
    </div>
    <div class="tool-group toolbar-btn preview">
        <g:message code="preview"/>
    </div>
    <div class="tool-group toolbar-btn save document-details-from-submit">
        <g:message code="save"/>
    </div>
</div>