<!DOCTYPE html>
<html lang="en">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <app:stylesheet href="css/font-awesome/css/font-awesome.min.css"/>
    <app:stylesheet href="plugins/snippet/css/site/base.css"/>
    <app:stylesheet href="plugins/snippet/css/editor/snippet-editor.css"/>
    <link rel="stylesheet" type="text/css" href="${app.customResourceBaseUrl()}snippetAdmin/snippetCss?id=${params.id}&templateUUID=${params.templateUUID}&repositoryType=${params.repositoryType}"/>
    <script type="text/javascript">
        function pageLoaded() {
            var $ = window.parent.$;
            $("#snippet-page-${params.id}").trigger("sure-load")
        }
    </script>
    <app:javascript src="js/jquery/jquery.min.js"/>
</head>
<body class="widget-snippet" onload="pageLoaded()">${snippetContent}</body>
</html>