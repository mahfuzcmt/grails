<div style="text-align: center; margin-top: 100px;"><h3 style="display: inline-block; padding: 30px 60px; font-size: 24px;"><g:message code="you.redirected.shortly.payment.provider"/></h3></div>
<form method="post" action="https://www.payway.com.au/MakePayment">
    <input type="hidden" name="biller_code" value="${biller_code}">
    <input type="hidden" name="token" value="${token}">
</form>
<script type="text/javascript">
    document.forms[0].submit();
</script>