<%@ page import="com.webcommander.manager.PathManager; com.webcommander.webcommerce.PaymentGatewayMeta; com.webcommander.util.AppUtil" %>
<div class="logo credit-card">
    <g:if test="${creditCardConfig["cardType"] == "custom"}">
        <g:if test="${new File(PathManager.getResourceRoot("payment-gateway/CRD/custom_logo.png")).exists()}">
            <div class="vertical-aligner custom-logo"></div><img src="${app.customResourceBaseUrl()}resources/payment-gateway/CRD/custom_logo.png">
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