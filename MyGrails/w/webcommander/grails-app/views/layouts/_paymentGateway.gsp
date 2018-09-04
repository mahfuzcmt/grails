<%@ page import="com.webcommander.AppResourceTagLib; com.webcommander.manager.PathManager; com.webcommander.webcommerce.PaymentGatewayMeta; com.webcommander.util.AppUtil" %>
<div class="logo credit-card">
    <g:if test="${creditCardConfig["cardType"] == "custom"}">
        <g:set var="physicalPath" value="${appResource.getResourcePhysicalPath(extension: appResource.getPaymentGatewayCardRelativePath(card: AppResourceTagLib.CREDIT_CARD))}"/>
        <g:if test="${new File("${physicalPath}").exists()}">
            <div class="vertical-aligner custom-logo"></div><img src="${appResource.getPaymentGatewayCardLogoPath(cardType : AppResourceTagLib.CREDIT_CARD)}">
        </g:if>
    </g:if>
    <g:else>
        <g:set var="creditCards" value="${creditCardConfig["creditCards"] ? creditCardConfig["creditCards"].split(",") : [] }"/>
        <g:each in="${creditCards}" var="card">
            <div class="vertical-aligner ${card}-card"></div><img src="${app.customResourceBaseUrl()}${PathManager.getStaticResourceURLRoot()}images/payment-cards/${card}_card.png">
        </g:each>
    </g:else>
</div>
<g:layoutBody/>