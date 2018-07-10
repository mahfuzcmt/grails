/**
 * Created by sanjoy on 6/19/2014.
 */
(function(){
    function performFacebookOperation(operation){
        var appPermissionScopes = "public_profile, email, user_friends, publish_actions";
        FB.getLoginStatus(function(response){
            if(response.status == 'connected'){
                operation.call();
            }else {
                FB.login(function(resp){
                    operation.call();
                }, {
                    scope: appPermissionScopes
                })
            }
        })
    }
    window.facebookWidget = {
        pageInfo: null,
        init:function(){
            facebookWidget.attachFacebookEvents();
        },
        attachFacebookEvents:function() {
            $(".facebook-buttons-container .fb-add-to-page").on("click", function(){
                performFacebookOperation(function(){
                    FB.ui({
                        method: 'pagetab'
                    })
                })
            });
            $(".facebook-buttons-container .fb-share").on("click", function(){
                performFacebookOperation(function(){
                    FB.ui({
                        method: 'share',
                        href: location.href
                    }, function(resp){

                    })
                })
            });
            $(".facebook-buttons-container .fb-invite").on("click", function(){
                var elm = $(this);
                performFacebookOperation(function(){
                    FB.ui({
                        method: 'apprequests',
                        message: elm.data('message')
                    }, function(){

                    })
                })
            })

        }
    }
    window.facebookWidget.init();
}());