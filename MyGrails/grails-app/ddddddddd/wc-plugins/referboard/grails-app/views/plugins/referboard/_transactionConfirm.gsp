<script type="text/javascript">
    var referPluginS = document.createElement('script');
    referPluginS.async = true;
    referPluginS.type = 'text/javascript';
    var useSSL = 'https:' == document.location.protocol;
    referPluginS.src = (useSSL ? 'https:' : 'http:') + '//www.referboard.com/js/referButton/refer_popup.js';
    var node = document.getElementsByTagName('script')[0];
    node.parentNode.insertBefore(referPluginS, node);
    var Refer_Opentop = Refer_Opentop || {};
    Refer_Opentop.cmd = Refer_Opentop.cmd || [];

    var tracking_params = {
        rkey: "${postbackData.rkey}",
        id: "${postbackData.id}",
        cid: "${postbackData.cid}",
        email: "${postbackData.email}",
        amount: "${order.grandTotal}",
        verify: 1,
        buyer_ip: "${postbackData.buyer_ip}",
        buy_history:"0",
        currency: '${postbackData.currency}',
        tid:"${postbackData.tid}",
        extra:"freetext",
        frontend:1,
    };
    Refer_Opentop.cmd.push(function(){
        Refer_Opentop.updateBuyHistoryCookies(tracking_params.buy_history);
        Refer_Opentop.successCallback(tracking_params);
    });
</script>