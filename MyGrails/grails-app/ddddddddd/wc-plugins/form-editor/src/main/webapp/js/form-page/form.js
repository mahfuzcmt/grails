//TODO: need improvement
$(function () {
    $.extend(VALIDATION_RULES, {
        "file-extensions" : {
            check : function(value, extensions, input) {
                var files = input[0].files, file = files.length > 0 ? files[0] : null
                if(file) {
                    var names = file.name.split("."), fileExt = names.length > 1 ? names.pop() : undefined,
                        isFound = extensions.indexOf(fileExt)
                    if(isFound < 0) {
                        return {msg_params: [extensions.join(", ")]}
                    }
                }
                return true;
            },
            msg_template : "invalid.file.extension"
        },
        "file-size": {
            check: function(value, params, input) {
                var size = parseFloat(params[0]) * ( params[1].trim() == "kb" ? 1024 : 1024 * 1024),  files = input[0].files, file = files.length > 0 ? files[0] : null
                if(file && file.size > size) {
                    return {msg_params: [params[0] + params[1]]}
                }
                return true;
            },
            msg_template : "size.limit.exceeded"
        },
        "date-range": {
            check: function (value, params, input) {
                value = value ? value.split(" ")[0] : "";
                var startDate = (params[0] && params[0] != " ") ? new Date(params[0]) : "";
                var endDate = (params[1] && params[1] != " ") ? new Date(params[1]) : "";
                var selectedDate = (value && value != " ") ? new Date(value) : "";

                if(!((selectedDate >= startDate) && (selectedDate <= endDate)) && startDate && endDate)
                    return {msg_template: "date.not.in.range", msg_params: [params[0], params[1]]};
                else if( startDate == "" && endDate && selectedDate > endDate )
                    return {msg_template: "date.not.less", msg_params: [params[1]]};
                else if( endDate == "" && startDate && selectedDate < startDate )
                    return {msg_template: "date.not.greater", msg_params: [params[0]]};
                return true;
            }
        },
        "time-range": {
            check: function (value, params, input) {
                var startTime, endTime, selectedTime;
                value = !isNaN(new Date(value)) ? value.split(" ")[1] : value;
                startTime = getTimeInMinute(params[0]);
                endTime = getTimeInMinute(params[1].split(":")[0] == '00' ? params[1].replace('00:', '24:') : params[1]);
                selectedTime = getTimeInMinute(value);

                function getTimeInMinute (time) {
                    var arr = (time && time != " ") ? time.split(":") : [];
                    var hour = parseInt(arr[0]);
                    var minute = parseInt(arr[1]);
                    var period = arr[2] ? arr[2].toLowerCase() : arr[2];
                    var result = null;
                    if( period == 'am' || period == 'pm' ) {
                        result = period == 'am' ? (hour == 12 ? 0 : hour)*60 + minute : (hour == 12 ? hour : hour+12)*60 + minute;
                    } else {
                        result = hour*60 + minute;
                    }
                    return result;
                }

                if ( !((selectedTime >= startTime) && (selectedTime <= endTime)) && startTime && endTime )
                    return {msg_template: "time.not.in.range", msg_params: [params[0], params[1]]};
                else if( !startTime && endTime && selectedTime > endTime )
                    return {msg_template: "time.not.less", msg_params: [params[1]]};
                else if( !endTime && startTime && selectedTime < startTime )
                    return {msg_template: "time.not.greater", msg_params: [params[0]]};
                return true;
            }
        }
    });

    app.config.form_editor_submission_response_display_time = 5000;


    $(".custom-form").each(function () {
        var thisForm = $(this);
        var beforeHandler = thisForm.find("span.before-form-submit").text();
        var msgBlock = thisForm.find("span.before-form-submit"), senderEmail = thisForm.find("[type=hidden].sender-email-hidden-filed"),
            senderEmailFieldUUID = thisForm.find("[type=hidden][name=senderEmailFieldUUID]").val(),
            conditionsHolder = JSON.parse(thisForm.find(".conditions-cache").text());

        thisForm.find(senderEmailFieldUUID ? ("#ff-" + senderEmailFieldUUID) : "").on("change", function() {
            senderEmail.val($(this).val())
        });

        function applyCondition(fieldId) {
            var conditions = conditionsHolder[fieldId], field = thisForm.find("#fw-" + fieldId + " [name^='submit.']"),
                fieldValue = field.attr("type") == "radio" ? field.filter(":checked").val() : field.val() ;
            conditions.every(function(index, condition) {
                var dependent = thisForm.find("#fw-" + condition.dependentFieldUUID), result;
                if(condition.targetOption == fieldValue) {
                    result = condition.action
                } else {
                    result = condition.action == "show" ? "hide" : "show";
                };
                if(result ==  "show") {
                    dependent.find("input, select").removeAttr("disabled")
                    dependent.show()
                } else {
                    dependent.find("input, select").attr("disabled", "disabled")
                    dependent.hide()
                }
            });
        }

        $.each(conditionsHolder, function(key, value) {
            applyCondition(key);
            thisForm.find("#fw-" + key + " [name^='submit.']").on("change", function() {
                applyCondition(key);
            })
        });


        thisForm.form({
            preSubmit: function() {
                try {
                    var form = thisForm //form is here to be used by handler
                    eval(beforeHandler)
                } catch (error) {
                }
            },
            ajax: {
                success: function (response) {
                    thisForm.trigger("reset");
                    var msgDiv = $('<div class="message-block info-message">' + response.message + '</div>');
                    thisForm.find(".message-block").remove();
                    msgBlock.before(msgDiv);
                    msgDiv.scrollHere();
                    if(response.hasCaptcha) {
                        app.captchaUtil.reloadCaptcha(response.captchaType);
                    }
                    var afterHandler = thisForm.find("span.after-form-submit").text();
                    try {
                        var form = thisForm //form is here to be used by handler
                        eval(afterHandler)
                    } catch (error) {
                    }
                    setTimeout(function () {
                        msgDiv.remove();
                    }, app.config.form_editor_submission_response_display_time);
                },
                error: function (a, b, response) {
                    var msgDiv = $('<div class="message-block error-message">' + response.message + '</div>');
                    thisForm.find(".message-block").remove();
                    msgBlock.before(msgDiv);
                    msgDiv.scrollHere();
                    if(response.hasCaptcha) {
                        app.captchaUtil.reloadCaptcha(response.captchaType);
                    }
                    setTimeout(function () {
                        msgDiv.remove();
                    }, app.config.form_editor_submission_response_display_time);
                }
            }
        });

        function showFileInfo(dropzone, file) {
            dropzone.find(":not(.dropzone-text)").remove()
            var preview = '<div class="file-preview"><span class="file-name">' + file.name + '</span><span>' + file.size.toByteNotation() + '</span></div>'
            dropzone.append(preview)
            dropzone.find(".dropzone-text").hide()
            dropzone.addClass("file-added")
        }

        function removeFileInfo(dropzone) {
            dropzone.find(":not(.dropzone-text)").remove()
            dropzone.removeClass("file-added")
            dropzone.find(".dropzone-text").show()
        }

        var fileFields = thisForm.find("input[type=file]")
        fileFields.on("change", function(evt) {
            var file = this.files[0]
            var dropzone = $(this).siblings(".dropzone");
            if(file) {
                showFileInfo(dropzone, file)
            } else {
                removeFileInfo(dropzone)
            }
        });
        thisForm.on("reset", function() {
            setTimeout(function(){
                fileFields.each(function() {
                    var dropzone = $(this).siblings(".dropzone")
                    removeFileInfo(dropzone)
                })
            }, 100)
        })
    });

    bm.onReady(window, "moment", function () {
        bm.onReady($.fn, "combodate", function () {
            $('.dropdown-time-picker').each(function () {
                $(this).combodate({
                    firstItem: 'name',
                    minuteStep: 1
                });
            });
            $('.dropdown-date-picker').each(function () {
                $(this).combodate({
                    firstItem: 'name',
                    smartDays: true,
                    maxYear: new Date().getFullYear()
                });
            });
            $('.dropdown-date-time-picker').each(function () {
                $(this).combodate({
                    firstItem: 'name',
                    smartDays: true,
                    maxYear: new Date().getFullYear(),
                    minuteStep: 1
                });
            });
        });
    });
});

