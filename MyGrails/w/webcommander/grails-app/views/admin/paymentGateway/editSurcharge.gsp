<%@ page import="com.webcommander.util.StringUtil" %>
<div class='multi-conditions'>
    <div class="multi-column two-column left-right-selector-panel">
        <div class="columns first-column">
            <div class="column-content" validation-attr="condition-validation">
                <div class="body">
                    <div class="form-row mandatory" >
                        <label><g:message code="range"/><span class="suggestion"> e.g. 1 - 100</span></label>
                        <g:set var="ref1" value="${StringUtil.uuid}"/>
                        <g:set var="ref2" value="${StringUtil.uuid}"/>
                        <div class="twice-input-row">
                            <input type="text" class="small conditionFrom from" restrict="decimal" id="${ref1}" condition-validation="required  gt[0] number max[99999999] maxlength[16]" maxlength="16"/><span>-</span><input type="text" class="small conditionTo to" restrict="decimal" id="${ref2}" condition-validation="max[99999999] gt[0] maxlength[16] required number compare[${ref1}, number, gte]" depends="#${ref1}" maxlength="16"/>
                        </div>
                    </div>
                    <div class="form-row mandatory">
                        <label><g:message code="surcharge.amount"/><span class="suggestion"> e. g. 10</span></label>
                        <input type="text" class="small surcharge-amount" condition-validation="max[99999999] gt[0] maxlength[16] required number" restrict="decimal" maxlength="16"/>
                    </div>
                </div>
                <div class="inline-button-line">
                    <button type="button" class="submit-button addCondition"><g:message code="add"/></button>
                </div>
            </div>
        </div><div class="columns last-column">
        <div class="column-content selected-panel table-view">
            <table>
                <colgroup>
                    <col style="width: 80%">
                    <col style="width: 20%">
                </colgroup>
                <tr>
                    <th><g:message code="surcharge.range"/></th>
                    <th class="actions-column"><span class="tool-icon remove-all"></span></th>
                </tr>
                <g:each in="${surchargeRangeList}" var="surchargeRange">
                    <g:set var="name"
                           value="${g.message(code: "from") + ": " + surchargeRange.orderAmountFrom.toAdminPrice() + "\n" +
                                   g.message(code: "to") + ": " + surchargeRange.orderAmountTo.toAdminPrice() + "\n" +
                                   g.message(code: "surcharge.amount") + ": " + surchargeRange.surchargeAmount.toAdminPrice()
                           }"/>
                    <tr data-id="${surchargeRange.id}">
                        <td>${name.encodeAsBMHTML()}</td>
                        <td class="actions-column" type="metatag" item = "item">
                            <span class="action-navigator collapsed"></span>
                            <input type="hidden" name="from" value="${surchargeRange.orderAmountFrom.toAdminPrice()}"/>
                            <input type="hidden" name="to" value="${surchargeRange.orderAmountTo.toAdminPrice()}"/>
                            <input type="hidden" name="surcharge-amount" value="${surchargeRange.surchargeAmount.toAdminPrice()}"/>
                        </td>
                    </tr>
                </g:each>
            </table>
            <div class="pagination-line">
                <paginator total="${surchargeRangeList?.size()}" offset="0" max="${10}"></paginator>
            </div>
            <div class="hidden">
                <div class="action-column-dice-content">
                    <table>
                        <tr>
                            <td></td>
                            <td class="actions-column">
                                <span class="action-navigator collapsed"></span>
                                <input type="hidden"/>
                                <input type="hidden"/>
                                <input type="hidden"/>
                            </td>
                        </tr>
                    </table>
                </div>
            </div>
        </div>
    </div>
    </div>
</div>