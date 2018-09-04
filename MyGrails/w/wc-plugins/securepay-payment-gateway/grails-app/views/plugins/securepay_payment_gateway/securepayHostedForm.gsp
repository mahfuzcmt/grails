<div style="text-align: center; margin-top: 100px;"><h3 style="display: inline-block; padding: 30px 60px; font-size: 24px;"><g:message code="you.redirected.shortly.payment.provider"/></h3></div>
<form method="post" action="${requestUrl}">
    <g:each in="${models}" var="model">
        <input type="hidden" name="${model.key.encodeAsBMHTML()}" value="${model.value.encodeAsBMHTML()}">
    </g:each>
</form>
<script type="text/javascript">
    document.forms[0].submit()
</script>