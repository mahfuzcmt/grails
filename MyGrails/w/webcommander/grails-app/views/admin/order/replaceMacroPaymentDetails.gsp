<%@ page import="com.webcommander.webcommerce.PaymentGateway" %>
<table style="border-collapse:collapse; width:700px;" border="0" cellspacing="0" cellpadding="0" align="left">
    <tr>
        <th style="padding:5px; background-color: #3997D6;  border-left: 1px solid #3997D6; border-right: 1px solid #3997D6; border-top: 1px solid #3997D6; color: #FFFFFF; text-align:center;font-family:Arial, Helvetica, sans-serif; font-size:13px; line-height:22px;"><g:message code="payment.date"/></th>
        <th style="padding:5px; background-color: #3997D6;  border-left: 1px solid #3997D6; border-right: 1px solid #3997D6; border-top: 1px solid #3997D6; color: #FFFFFF; text-align:center;font-family:Arial, Helvetica, sans-serif; font-size:13px; line-height:22px;"><g:message code="payment.method"/> </th>
        <th style="padding:5px; background-color: #3997D6;  border-left: 1px solid #3997D6; border-right: 1px solid #3997D6; border-top: 1px solid #3997D6; color: #FFFFFF; text-align:center;font-family:Arial, Helvetica, sans-serif; font-size:13px; line-height:22px;"><g:message code="track.info"/></th>
        <th style="padding:5px; background-color: #3997D6;  border-left: 1px solid #3997D6; border-right: 1px solid #3997D6; border-top: 1px solid #3997D6; color: #FFFFFF; text-align:center;font-family:Arial, Helvetica, sans-serif; font-size:13px; line-height:22px;"><g:message code="payer.info"/></th>
        <th style="padding:5px; background-color: #3997D6;  border-left: 1px solid #3997D6; border-right: 1px solid #3997D6; border-top: 1px solid #3997D6; color: #FFFFFF; text-align:center;font-family:Arial, Helvetica, sans-serif; font-size:13px; line-height:22px;"><g:message code="status"/></th>
        <th style="padding:5px; background-color: #3997D6;  border-left: 1px solid #3997D6; border-right: 1px solid #3997D6; border-top: 1px solid #3997D6; color: #FFFFFF; text-align:center;font-family:Arial, Helvetica, sans-serif; font-size:13px; line-height:22px;"><g:message code="amount"/></th>
    </tr>
    <g:each in="${payments}" var="payment">
        <tr>
            <td style="padding:5px; border:1px #e6e6e6 solid; text-align:center;font-family:Arial, Helvetica, sans-serif; font-size:13px; line-height:22px;">${payment.payingDate.toEmailFormat()}</td>
            <td style="padding:5px; border:1px #e6e6e6 solid; text-align:center;font-family:Arial, Helvetica, sans-serif; font-size:13px; line-height:22px;"><g:message code="${PaymentGateway.findByCode(payment.gatewayCode).name}" locale="${Locale.getDefault()}"/></td>
            <td style="padding:5px; border:1px #e6e6e6 solid; text-align:center;font-family:Arial, Helvetica, sans-serif; font-size:13px; line-height:22px;">${payment.trackInfo}</td>
            <td style="padding:5px; border:1px #e6e6e6 solid; text-align:center;font-family:Arial, Helvetica, sans-serif; font-size:13px; line-height:22px;">${payment.payerInfo}</td>
            <td style="padding:5px; border:1px #e6e6e6 solid; text-align:center;font-family:Arial, Helvetica, sans-serif; font-size:13px; line-height:22px;"><g:message code="${payment.status}"
                                                                                                                                                                        locale="${Locale.getDefault()}"/></td>
            <td style="padding:5px; border:1px #e6e6e6 solid; text-align:center;font-family:Arial, Helvetica, sans-serif; font-size:13px; line-height:22px;">${payment.amount.toAdminPrice()}</td>
        </tr>
    </g:each>
</table>