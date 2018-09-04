<%@ page import="com.webcommander.plugin.google_trusted_store.Constants" %>
<!-- BEGIN: Google Trusted Stores -->
<script type="text/javascript">
    var gts = gts || [];

    gts.push(["id", "${config.store_id}"]);
    gts.push(["badge_position", "${config.badge_position}"]);
    gts.push(["locale", "${config.locale}"]);
    <g:if test="${config.badge_position == Constants.BADGE_POSITION.USER_DEFINED}">
        gts.push(["badge_container", "${config.badge_container}"]);
    </g:if>
    (function() {
        var gts = document.createElement("script");
        gts.type = "text/javascript";
        gts.async = true;
        gts.defer = true;
        gts.src = "https://www.googlecommerce.com/trustedstores/api/js";
        var s = document.getElementsByTagName("script")[0];
        s.parentNode.insertBefore(gts, s);
    })();
</script>
<!-- END: Google Trusted Stores -->