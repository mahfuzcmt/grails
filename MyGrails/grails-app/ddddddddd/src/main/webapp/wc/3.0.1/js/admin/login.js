$(function() {
    var form = $(".authentication-form");
    form.form({}).find("input[type='checkbox']").checkbox();
    $(".new-password-form .new-password").change(function() {
        $(".new-password-form .match-password").trigger("validate");
    });
    form.find("#user-name, #password").val("");
})