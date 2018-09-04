<script src=${config.mode == 'live' ? "https://www.secure-afterpay.com.au/afterpay.js" : "https://www-sandbox.secure-afterpay.com.au/afterpay.js"} async></script>
<script>
    window.onload = function() {
        AfterPay.init();
        AfterPay.display({token: "${token}"});
    };
</script>