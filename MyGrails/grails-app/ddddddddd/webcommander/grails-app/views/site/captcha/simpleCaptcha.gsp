<div class='simpleCaptcha-container'>
    <div class="form-row simple_captcha_help_text">
        <label>&nbsp;</label>
        <span><g:message code="type.the.characters.you.see.in.the.picture.below"/></span>
    </div>
    <div class="form-row simple-captcha ">
        <label for="captcha">&nbsp;</label>
        <span class="captcha">
            <img src="${app.relativeBaseUrl()}simpleCaptcha/captcha"/>
            <span class="simple-captcha-reload">
                <span class="simple-captcha-reload icon"></span>
                <span class="simple-captcha-reload text"><g:message code="show.another.code"/></span>
            </span>
        </span>
    </div>
    <div class="form-row simple_captcha_input_field mandatory">
        <label for="captcha"><g:message code="enter.the.code"/>:</label>
        <g:textField name="captcha" validation="required" autocomplete="off"/>
    </div>
    <div class="form-row simple_captcha_help_text">
        <label>&nbsp;</label>
        <span><g:message code="letters.are.not.case.sensitive"/></span>
    </div>
</div>