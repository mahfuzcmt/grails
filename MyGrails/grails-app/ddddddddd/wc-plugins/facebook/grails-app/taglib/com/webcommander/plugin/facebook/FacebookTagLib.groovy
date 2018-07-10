package com.webcommander.plugin.facebook

import com.webcommander.constants.DomainConstants
import com.webcommander.util.AppUtil

class FacebookTagLib {

    static namespace = "fb"

    def layoutHead = { Map attrs, body ->
        out << body();
        Map configs = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.FACEBOOK)
        String base = app.relativeBaseUrl();
        String cssPath = servletContext.getRealPath("template/css");
        File templateCssFile = new File(cssPath, "facebook.css");
        String templateCss = templateCssFile.exists() ? "template/css/facebook.css" : null;
        out << """<script type='text/javascript'>
                window.fbAsyncInit = function() {
                    FB.init({
                        appId      : '${configs.appId}',
                        xfbml      : true,
                        version    : 'v2.0'
                    });
                    try {
                        var cssUrl = app.baseUrl + 'plugins/facebook/css/app/facebook.css';
                        var templateCssUrl = ${templateCss ? "app.baseUrl + '" + templateCss + "'" : "false"};
                        FB.Canvas.Prefetcher.addStaticResource(cssUrl);
                        if(templateCssUrl){
                            FB.Canvas.Prefetcher.addStaticResource(templateCssUrl);
                        }
                        FB.Canvas.getPageInfo(function(info) {
                            if(window.facebookWidget) {
                                facebookWidget.pageInfo = info;
                            }
                            if(templateCssUrl){
                                \$('head').find('#template-base').attr('href', templateCssUrl);
                            }else{
                                \$('head').find('#template-base').remove();
                            }
                            \$('head').append('<link rel=\"stylesheet\" type=\"text/css\" href=\"' + cssUrl + '\">');
                        })
                    } catch (e) {
                    }
                };
                (function(d, s, id) {
                    var js, fjs = d.getElementsByTagName(s)[0];
                    if (d.getElementById(id)) {return;}
                    js = d.createElement(s); js.id = id;
                    js.async = true;
                    js.defer=true;
                    js.src = \"//connect.facebook.net/en_US/sdk.js\";
                    fjs.parentNode.insertBefore(js, fjs);
                })(document, 'script', 'facebook-jssdk');
                </script>"""
    }
}
