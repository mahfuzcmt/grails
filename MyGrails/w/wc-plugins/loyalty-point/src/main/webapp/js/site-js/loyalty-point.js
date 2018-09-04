// TODO: Need code Improvement
$(function(){
    var currentLoyaltyView = app.baseUrl + "loyaltyPoint/customerProfile";
    function loadView(callback){
        bm.ajax({
            url: currentLoyaltyView,
            dataType: "html",
            success: function(resp){
                resp = $(resp)
                $(".loyalty_point").html(resp);
                if(callback){
                    callback(resp);
                }
            }
        })
    }

    function attachClaimRewardsView(view) {
        view.find("form").form({
            ajax: true,
            preSubmit: function(ajaxSettings) {
                $.extend(ajaxSettings, {
                    success: function(resp) {
                        currentLoyaltyView = app.baseUrl + "loyaltyPoint/customerProfile"
                        loadView(function(){
                            var dom = $(".loyalty_point  .loyality_point_quentity")
                            showStatus(dom, resp.status, resp.message);
                        });
                    },
                    error: function(xht, status, resp) {
                        var dom = $(".loyalty_point  .loyality_point_quentity")
                        showStatus(dom, resp.status, resp.message);
                    }
                })
            }
        });
    }

    function showStatus(dom, status, message) {
        var statusDom = dom.find(".message-block");
        if(!statusDom.length) {
            statusDom = dom.prepend('<span class="message-block"></span>').find(".message-block");
        }
        statusDom.removeClass('success').removeClass('error').removeClass('info').addClass(status);
        statusDom.append(message);
        setTimeout(function(){
            statusDom.remove();
        }, app.config.customer_profile_message_display_time)
    }

    $(document).on("click", ".loyalty-button", function(){
        var target = $(this).attr("target");
        currentLoyaltyView = app.baseUrl + "loyaltyPoint/" + target
        loadView(function(view) {
            if(target == "claimRewards") {
                attachClaimRewardsView(view)
            }
        })
    })


    $(document).on("click", ".referral-copy-button", function () {
        var referralInviteLinkSection = $(".referral-invite-link-section")
        var successful = bm.copy(referralInviteLinkSection.find(".invite-link").val())
        var panel = $(".loyalty_point ")
        successful ? renderMessage(panel, $.i18n.prop("copied.to.clipboard"), "success") : renderMessage(panel, $.i18n.prop("can.not.copy"), "error");
    })

    $(document).on("click", ".social-media-wrapper.invite-link-social-media .social-media-share", function() {
        var $this = $(this), type = $this.attr("type"), url = $this.attr("url");
        if(type == "email") {
            inviteAFriendToSignUp(url)
        } else {
            shareOnSocialMedea(type,  url, "", "")
        }
    })

    function inviteAFriendToSignUp(url) {
        bm.ajax({
            url: app.baseUrl + "loyaltyPoint/inviteFriend",
            dataType: 'html',
            type: 'post',
            success: function(resp) {
                var content = $(resp);
                content.find(".invite-friend-popup").form({
                    ajax: true,
                    preSubmit: function(ajaxSettings) {
                        $.extend(ajaxSettings, {
                            success: function(resp){
                                var successPopupContent = $(resp.html);
                                renderGlobalSitePopup(successPopupContent, {clazz: "tell-friend-success"})
                            },
                            error: function(a, b, resp) {
                                renderMessage(content.find(".body .message-container"), resp.message, "error")
                            }
                        })
                    }
                });
                renderGlobalSitePopup(content, {clazz: "tell_friend_popup"});
            }
        })
    }

    var referralCodeForm = $(".shopping-cartitem .referral-code-form");
    if(referralCodeForm.length) {
        var msgBlock = referralCodeForm.find('.message-block');
        setTimeout(function () {
            msgBlock.remove()
        }, 5000);
    }

    $(document).on('focusout', 'input[name=reference_number]', function() {
        if($(this).val()) {
            if(!valid($(this))) {
                return
            }
            bm.ajax({
                url: app.baseUrl + 'loyaltyPoint/validateReferralCode',
                data: {referralCode: $(this).val()},
                success: function(resp) {
                    console.log(resp)
                }
            });
        }
    });

    function valid(input) {
        var errorObj = ValidationField.validateAs(input, input.attr("validation"));
        if (errorObj) {
            errorHighlight(input);
            return false;
        }
        return true;
    }

    function errorHighlight(input) {
        input.addClass("error-highlight");
        setTimeout(function () {
            input.removeClass("error-highlight");
        }, 1000);
    }

    app.global_event.on('update-cart', function(e, page) {
        bm.ajax({
            url: app.baseUrl + "loyaltyPoint/updateCartLoyaltyPoint",
            data: [],
            success: function(resp) {
                if(resp.message) {
                    page.find(".loyalty-point").text(resp.message);
                }
            }
        });
    });
});