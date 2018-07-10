var CHECKERS = {
    validateByLength: function (value, rangeValue, type) {
        var status = true;
        if (!value) {
            return true;
        } else {
            value = value.trim()
        }
        switch (type) {
            case "range" :
                if (value < parseFloat(rangeValue[0], 10) || value > parseFloat(rangeValue[1], 10)) {
                    status = false;
                }
                break;
            case "max":
                if (value > parseFloat(rangeValue[0], 10)) {
                    status = false;
                }
                break;
            case "min":
                if (value < parseFloat(rangeValue[0], 10)) {
                    status = false;
                }
                break;
            case "multipleOf":
                if (value % parseInt(rangeValue[0], 10) != 0) {
                    status = false;
                }
                break;
            case "gt":
                if (value <= parseFloat(rangeValue[0], 10)) {
                    status = false;
                }
                break;
            case "gte":
                if (value < parseFloat(rangeValue[0], 10)) {
                    status = false;
                }
                break;
            case "lt":
                if (value >= parseFloat(rangeValue[0], 10)) {
                    status = false;
                }
                break;
            case "lte":
                if (value > parseFloat(rangeValue[0], 10)) {
                    status = false;
                }
                break;
            case "rangelength":
                var count = value.byteCount()
                if (count < parseFloat(rangeValue[0], 10) || count > parseFloat(rangeValue[1], 10)) {
                    status = false;
                }
                break;
            case "maxlength":
                if (value.byteCount() > parseFloat(rangeValue[0], 10)) {
                    status = false;
                }
                break;
            case "minlength":
                if (value.byteCount() < parseFloat(rangeValue[0], 10)) {
                    status = false;
                }
                break;
            case "default":
                status = false;
                break;
        }
        return status;
    },
    validateByCompare: function (value, compareWith, type, method) {
        var compareWithText = compareWith;
        var status = true;
        var msg_code;
        var alphabetToDecimal = function (target) {
            var res = 0;
            target = target.split('').reverse().join('');
            for (var i = target.length - 1; i >= 0; i--) {
                res += Math.pow(26, i) * (target.charCodeAt(i) - 'A'.charCodeAt(0) + 1);
            }
            return res;
        };
        var decimalToAlphabet = function (target) {
            var res = "";
            while (target > 0) {
                target--;
                var rem = target % 26;
                res = String.fromCharCode(rem + 'A'.charCodeAt(0)) + res;
                target = (target - rem) / 26;
            }
            return res;
        };
        if (type == "number") {
            if (value.length > 16 || compareWith.length > 16) {
                return {msg_template: $.i18n.prop('num.of.digits.less.than.n', [16])}
            }
            value = parseFloat(value, 10);
            compareWith = parseFloat(compareWith, 10);
        } else if (type === "date") {
            value = Date.parse(value);
            compareWith = Date.parse(compareWith);
        } else if (type == "alphabetic") {
            compareWithText = decimalToAlphabet(compareWith);
            value = alphabetToDecimal(value);
        }
        switch (method) {
            case "gt":
                if (value <= compareWith) {
                    status = false;
                    msg_code = "value.must.greater";
                }
                break;
            case "gte":
                if (value < compareWith) {
                    status = false;
                    msg_code = "value.must.greater.equal";
                }
                break;
            case "eq":
                if (value != compareWith) {
                    status = false;
                    msg_code = "value.must.equal";
                }
                break;
            case "ne":
                if (value == compareWith) {
                    status = false;
                    msg_code = "value.must.not.equal";
                }
                break;
            case "lte":
                if (value > compareWith) {
                    status = false;
                    msg_code = "value.must.less.equal";
                }
                break;
            case "lt":
                if (value >= compareWith) {
                    status = false;
                    msg_code = "value.must.less";
                }
                break;
            case "mod":
                if (value % compareWith != 0) {
                    status = false;
                    msg_code = "value.must.multiple.of";
                }
                break;
            case "default":
                status = false;
                msg_code = "unsupported.comparison.type";
                break;
        }
        return status || {msg_template: msg_code, msg_params: [compareWithText]};
    }
}

var VALIDATION_RULES = {
    alphabetic: {
        check: function (value) {
            if (value)
                return /^[a-zA-Z ]+$/.test(value);
            return true;
        },
        msg_template: "enter.valid.alphabetic"
    },
    alphanumeric: {
        check: function (value) {
            if (value)
                return /^[a-zA-Z0-9]+$/.test(value);
            return true;
        },
        msg_template: "enter.valid.alphanumeric"
    },
    cardnumber: {
        check: function (value, ranges) {
            value = $.trim(value);
            var regex = {
                visa: /^4[0-9]{12}(?:[0-9]{3})?$/,
                master: /^5[1-5][0-9]{14}$/,
                diners: /^3(?:0[0-5]|[68][0-9])[0-9]{11}$/,
                unionpay: /^(62|88)\d+$/,
                amex: /^3[47][0-9]{13}$/
            }
            ranges = ranges.filter("this != ''")
            if (ranges.length == 0) {
                return true
            }
            var result = false
            for (var i = 0; i < ranges.length; i++) {
                if (regex[ranges[i].trim()].test(value)) {
                    result = true
                    break;
                }
            }
            return result
        },
        msg_template: "incorrect.card.number"
    },
    commaSeparated4digit: {
        check: function (value) {
            if (value)
                return /^[1-9]\d{0,3}(?:(?:,[1-9]\d{0,3})*)$/.test(value);
            return true;
        },
        msg_template: "enter.comma.seperated.four.digit.numeric"
    },
    compare: {
        check: function (value, ranges) {
            if (!value) {
                return true;
            }
            var validated = CHECKERS.validateByCompare(value, $("#" + ranges[0].trim()).val(), ranges[1].trim(), ranges[2].trim())
            if (validated !== true) {
                return {msg_template: validated.msg_template, msg_params: validated.msg_params};
            }
            return true
        }
    },
    eq: {
        check: function (value, params) {
            var compareWith = $("#" + params[0].trim()).val();
            if(compareWith && !value){
                return {msg_template: "field.required"}
            }
            if(value != compareWith) {
                return {msg_params: [compareWith]}
            }
            return true
        },
        msg_template: "value.must.equal"
    },
    creditCardExpiryDate: {
        check: function (value, ranges) {
            if (!value || !$("#" + ranges[0].trim()).val()) {
                return true;
            }
            var currentMonth = new Date().getMonth() + 1;
            var currentYear = new Date().getFullYear().toString().substring(2, 4);
            var inputMonth = value;
            var inputYear = $("#" + ranges[0].trim()).val();
            if (inputYear < currentYear || (inputMonth < currentMonth && inputYear <= currentYear)) {
                return {msg_template: "card.expired"};
            }
            return true;
        }
    },
    creditcard: {
        check: function (value) {
            if (/[^0-9-]+/.test(value))
                return false;
            var nCheck = 0,
                nDigit = 0,
                bEven = false;
            value = value.replace(/\D/g, "");
            for (var n = value.length - 1; n >= 0; n--) {
                var cDigit = value.charAt(n);
                var nDigit = parseInt(cDigit, 10);
                if (bEven) {
                    if ((nDigit *= 2) > 9)
                        nDigit -= 9;
                }
                nCheck += nDigit;
                bEven = !bEven;
            }
            if ((nCheck % 10) == 0)
                return true
            return false
        },
        msg_template: "enter.valid.creditcard"
    },
    cvv: {
        check: function (value) {
            if (value) {
                return value.length > 2 && value.length < 5 && /^\d+$/.test(value);
            }
            return true;
        },
        msg_template: "cvv.invalid"
    },
    date: {
        check: function (value, param) {
            if (!value) {
                return true;
            }
            var format = "yyyy-MM-dd";
            if (param) {
                format = param;
            }
            this.msg_params = [format];
            //dependant date.js
            return !!Date.parseExact(value, format);
        },
        msg_template: "date.should.formatted"
    },
    digits: {
        check: function (value) {
            value = $.trim(value);
            if (value) {
                if (value.length > 16) {
                    this.msg_template = "";
                    return false;
                }
                return /^\d+$/.test(value);
            }
            return true;
        },
        msg_template: "only.digits.allowed"
    },
    either_required: {
        check: function (value, param) {
            var otherValue = $("#" + param[0]).val();
            if (value || otherValue)
                return true;
            else {
                this.msg_params = [param[1], param[2]];
                return false;
            }
        },
        msg_template: "either.required"
    },
    email: {
        check: function (value, single) {
            if (value) {
                var emailPattern = "(([^<>()[\\]\\\\.,;:\\s@\"]+(\\.[^<>()[\\]\\\\.,;:\\s@\"]+)*)|(\".+\"))@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))";
                var withNamePattern = "[^<>,;]+\\s*<" + emailPattern + ">";
                var jointEmailPattern = "((" + emailPattern + ")|(" + withNamePattern + "))";
                var repeatJointEmailPattern = single ? jointEmailPattern : (jointEmailPattern + "([,;]\\s*" + jointEmailPattern + ")*;?");
                return new RegExp("^\\s*" + repeatJointEmailPattern + "\\s*$").test(value);
            }
            return true;
        },
        msg_template: "enter.valid.email"
    },
    single_email: {
        check: function (value) {
            if (value) {
                return VALIDATION_RULES.email.check(value, true);
            }
            return true;
        },
        msg_template: "enter.valid.email"
    },
    empty: {
        check: function (value) {
            if ($.trim(value)) {
                return false;
            } else {
                return true;
            }
        },
        msg_template: "field.must.empty"
    },
    eval: {
        check: function (value, param) {
            try {
                return eval(param[0]);
            } catch (k) {
                return false;
            }
        },
        msg_template: "entry.invalid"
    },
    "drop-file-required": {
        check: function (value, param, input) {
            if (value) {
                return true;
            }
            var name = input.attr("name")
            var form = input.closest("form");
            var fileData = form.data("form-extra-data");
            if (!fileData) {
                return false;
            }
            if (window.File) {
                if (fileData.value instanceof File && fileData.name == name) {
                    return true;
                }
                if ($.isArray(fileData)) {
                    var fileobj;
                    fileData.every(function () {
                        if (this.value instanceof window.File && this.name == name) {
                            fileobj = this;
                            return false;
                        }
                    })
                    if (fileobj) {
                        return true;
                    }
                }
            }
            return false;
        },
        msg_template: "no.file.selected"
    },
    "function": {
        check: function (value, param, input) {
            try {
                return bm.prop(window, param[0]).call(input, value, VALIDATION_RULES["function"]);
            } catch (k) {
                return false;
            }
        },
        msg_template: "entry.invalid"
    },
    gt: {
        check: function (value, param) {
            var message = CHECKERS.validateByLength(value, param, "gt");
            if (message !== true) {
                return {msg_params: param};
            }
            return true;
        },
        msg_template: "value.must.greater"
    },
    gte: {
        check: function (value, param) {
            var message = CHECKERS.validateByLength(value, param, "gte");
            if (message !== true) {
                return {msg_params: param};
            }
            return true;
        },
        msg_template: "value.must.greater.equal"
    },
    lt: {
        check: function (value, param) {
            var message = CHECKERS.validateByLength(value, param, "lt");
            if (message !== true) {
                return {msg_params: param};
            }
            return true;
        },
        msg_template: "value.must.less"
    },
    lte: {
        check: function (value, param) {
            var message = CHECKERS.validateByLength(value, param, "lte");
            if (message !== true) {
                return {msg_params: param};
            }
            return true;
        },
        msg_template: "value.must.less.equal"
    },
    hexColor: {
        check: function (value) {
            value = $.trim(value);
            if (value)
                return /^#?[0-9a-fA-F]{6}$/.test(value);
        },
        msg_template: "invalid.hexadecimal.color"
    },
    hexDigit: {
        check: function (value) {
            value = $.trim(value);
            if (value) {
                return /[0-9a-fA-F]$/.test(value);
            } else {
                return true;
            }
        },
        msg_template: "invalid.hexadecimal.digit"
    },
    identifier: {
        check: function (value, param) {
            if (value) {
                if (param == "multi") {
                    return /^[a-zA-Z_][a-zA-Z_0-9]*(\s+[a-zA-Z_][a-zA-Z_0-9]*)*$/.test(value);
                }
                return /^[a-zA-Z_][a-zA-Z_0-9]*$/.test(value);
            }
            return true;
        },
        msg_template: "only.alphanumeric.underscore.supported"
    },
    least_selection: {
        check: function (x, y, field) {
            return !!field.find(":checked").length
        },
        msg_template: "least.option.must.select"
    },
    match: {
        /**
         * @param value
         * @param param supported format is - <regular expression>,<sample value> <br>
         *     <regular expression>
         *     <blockquote>Any regular expression that should be matched with input value</blockquote>
         *     <sample value>
         *     <blockquote>A sample value to display in error message</blockquote>
         *     N.B. - if any comma(,) be exist in sample or expression that must be enclosed within ( and ) <br>
         *     e.g. - for 1,2,3 - (1,2,3)
         */
        check: function (value, param) {
            if (value) {
                var exp = new RegExp(param[0]);
                if (!exp.test(value)) {
                    return {msg_template: "invalid.format.use.like", msg_params: [param[1]]};
                }
            }
            return true;
        }
    },
    max: {
        check: function (value, param) {
            var message = CHECKERS.validateByLength(value, param, "max");
            if (message !== true) {
                return {msg_params: param};
            }
            return true;
        },
        msg_template: "value.must.less.equal"
    },
    maxlength: {
        check: function (value, param) {
            var message = CHECKERS.validateByLength(value, param, "maxlength");
            if (message !== true) {
                return {msg_params: param};
            }
            return true;
        },
        msg_template: "enter.no.more.characters"
    },
    maxprecision: {
        check: function (value, param1) {
            var integer = param1.length == 1 ? undefined : param1[0]
            var floated = param1.length == 1 ? param1[0] : param1[1]
            var regex = new RegExp("^(\\+|-)?\\d" + (integer ? "{0," + integer + "}" : "*") + "(?:\\.\\d{0," + floated + "})?$")
            var msg_params
            if (integer) {
                this.msg_template = "maximum.number.precision.allowed"
                msg_params = [integer, floated]
            } else {
                this.msg_template = "maximum.precision.allowed"
                msg_params = [floated]
            }
            if (!regex.test(value)) {
                return {msg_params: msg_params};
            }
            return true;
        }
    },
    price: {
        check: function (value) {
            return VALIDATION_RULES.maxprecision.check.apply(this, [value, [9, +app.maxPricePrecision]]);
        }
    },
    min: {
        check: function (value, param) {
            var message = CHECKERS.validateByLength(value, param, "min");
            if (message !== true) {
                return {msg_params: param};
            }
            return true;
        },
        msg_template: "value.must.greater.equal"
    },
    minlength: {
        check: function (value, param) {
            var message = CHECKERS.validateByLength(value, param, "minlength");
            if (message !== true) {
                return {msg_params: param};
            }
            return true;
        },
        msg_template: "enter.least.characters"
    },
    multipleOf: {
        check: function (value, param) {
            var message = CHECKERS.validateByLength(value, param, "multipleOf");
            if (message !== true) {
                return {msg_params: param};
            }
            return true;
        },
        msg_template: "value.must.multiple.of"
    },
    number: {
        check: function (value) {
            value = $.trim(value);
            if (value) {
                return /^(\+|-)?\d*(?:\.\d+)?$/.test(value);
            }
            return true;
        },
        msg_template: "enter.valid.number"
    },
    percent_number: {
        check: function (value) {
            value = $.trim(value);
            if (value == "%") {
                return false
            } else if (value) {
                return /^(\+|-)?\d*(?:\.\d+)?%?$/.test(value);
            }
            return true;
        },
        msg_template: "enter.valid.number"
    },
    partial_url: {
        check: function (value) {
            value = $.trim(value);
            if (value)
                return /^(((https?|ftp):)?\/\/(((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:)*@)?(((\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.(\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.(\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.(\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5]))|((([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.?)+)(:\d*)?))?(\/((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:|@)+(\/(([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:|@)*)*)?)?(\?((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:|@)|[\uE000-\uF8FF]|\/|\?)*)?(\#((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:|@)|\/|\?)*)?$/i.test(value)
            return true;
        },
        msg_template: "enter.valid.url"
    },
    phone: {
        check: function (value) {
            value = $.trim(value);
            if (value)
                return /^\+?[0-9\(\)\-\s]+$/.test(value);
            return true;
        },
        msg_template: "phone.invalid"
    },
    range: {
        check: function (value, param) {
            var message = CHECKERS.validateByLength(value, param, "range");
            if (message !== true) {
                return {msg_params: param};
            }
            return true;
        },
        msg_template: "value.must.between"
    },
    rangelength: {
        check: function (value, param) {
            var message = CHECKERS.validateByLength(value, param, "rangelength");
            if (message !== true) {
                return {msg_params: param};
            }
            return true;
        },
        msg_template: "value.must.length.between"
    },
    required: {
        check: function (value) {
            if ($.trim(value)) {
                return true;
            } else {
                return false;
            }
        },
        msg_template: "field.required"
    },
    restricted_chars: {
        check: function (value, param) {
            var index = 0;
            if (value) {
                $.each(param, function () {
                    index = value.indexOf(this);
                    if (index > -1) {
                        return false;
                    }
                });
                if (index > -1) {
                    return {msg_params: [param.join(",")]};
                }
            }
            return true;
        },
        msg_template: "characters.not.supported"
    },
    url: {
        check: function (value) {
            value = $.trim(value);
            if (value)
                return /^(https?|ftp):\/\/(((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:)*@)?(((\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.(\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.(\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.(\d|[1-9]\d|1\d\d|2[0-4]\d|25[0-5]))|((([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])*([a-z]|\d|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])))\.?)+)(:\d*)?)(\/((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:|@)+(\/(([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:|@)*)*)?)?(\?((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:|@)|[\uE000-\uF8FF]|\/|\?)*)?(\#((([a-z]|\d|-|\.|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:|@)|\/|\?)*)?$/i.test(value)
            return true;
        },
        msg_template: "enter.valid.url"
    },
    domain_name: {
        check: function (value) {
            if (value) {
                return value.match(/^(([a-zA-Z0-9]|[a-zA-Z0-9][a-zA-Z0-9\-]*[a-zA-Z0-9])\.)*([A-Za-z0-9]|[A-Za-z0-9][A-Za-z0-9\-]*[A-Za-z0-9])$/) != null
            }
            return true
        },
        msg_template: "enter.valid.domain.name"
    },
    url_folder: {
        check: function (value) {
            if (value)
                return /^(([a-z]|\d|-|_|~|[\u00A0-\uD7FF\uF900-\uFDCF\uFDF0-\uFFEF])|(%[\da-f]{2})|[!\$&'\(\)\*\+,;=]|:|@)+$/i.test(value);
            return true;
        },
        msg_template: "enter.valid.url.path"
    },
    non_symbolic: {
        check: function (value) {
            if (value)
                return /^( *[a-zA-Z0-9\-_]+)+$/.test(value);
            return true;
        },
        msg_template: "enter.valid.non.symbolic"
    },
    // special rule to skip further processing
    skip: {},
    // special rule to skip next rule processing
    skip_next: {},
    // special rule to skip further processing if any condition is false
    check: {},
    // special rule to skip next rule processing if any condition is false
    check_next: {},
    // special to always fail
    fail: {}
}

var ValidationRule = (function () {
    function fetchParam(str, index) {
        var skip = true;
        var terminate = index;
        while (skip) {
            terminate = str.indexOf("]", terminate);
            if (terminate == -1) {
                throw Error("No Param Terminator ']'");
            }
            skip = str.charAt(terminate + 1) == "]";
            if (skip) {
                terminate += 2
            }
        }
        return str.substring(index, terminate);
    }

    function fetchIfOrNot(str, index) {
        var returnMap = {condition: {}};
        var condition_char = str.charAt(index)
        if (condition_char == 'i') {
            returnMap.condition.type = "if";
            index += 3;
        } else if (condition_char == 'v') {
            returnMap.condition.type = "if";
            returnMap.parsedIndex = index + 6;
            returnMap.condition.check_type = "self";
            returnMap.condition.selector = ":visible";
            return returnMap;
        } else if (condition_char == 'h') {
            returnMap.condition.type = "if";
            returnMap.parsedIndex = index + 5;
            returnMap.condition.check_type = "self";
            returnMap.condition.selector = ":hidden";
            return returnMap;
        } else {
            returnMap.condition.type = "not";
            index += 4;
        }

        var right;
        if (str.charAt(index) == 'g') {
            index += 7;
            right = str.indexOf("}", index);
            if (right == -1) {
                throw Error("Invalid Rule Format/Name");
            }
            returnMap.condition.check_type = "global";
            returnMap.condition.selector = str.substring(index, right);
        } else if (str.charAt(index) == 't') {
            index += 5;
            right = str.indexOf("}", index);
            if (right == -1) {
                throw Error("Invalid Rule Format/Name");
            }
            returnMap.condition.check_type = "this";
            returnMap.condition.selector = str.substring(index, right);
        } else if (str.charAt(index) == 's') {
            index += 5;
            right = str.indexOf("}", index);
            if (right == -1) {
                throw Error("Invalid Rule Format/Name");
            }
            returnMap.condition.check_type = "self";
            returnMap.condition.selector = str.substring(index, right);
        } else {
            returnMap.condition.check_type = "rule";
            index += 5;
            var right = str.indexOf("}", index + 1);
            var lbra = str.indexOf("{", index + 1);
            while (right != -1 && (lbra != -1 && lbra < right)) {
                right = str.indexOf("}", right + 1);
                lbra = str.indexOf("{", lbra + 1);
            }
            if (right == -1) {
                throw Error("Invalid Rule Format/Name");
            }
            var validRuleString = str.substring(index, right);
            returnMap.condition.rules = ValidationRule.createRules(validRuleString);
        }
        returnMap.parsedIndex = right;
        return returnMap;
    }

    return {
        addRule: function (name, rule) {
            VALIDATION_RULES[name] = rule;
        },
        getRule: function (name) {
            return VALIDATION_RULES[name];
        },
        createRules: function (str, startFrom) {
            startFrom = startFrom || 0;
            var len = str.length;
            if (startFrom >= len) {
                return [];
            }
            var space = str.indexOf(" ", startFrom);
            if (space == -1) {
                space = len;
            }
            var openBracket = str.indexOf("[", startFrom);
            if (openBracket == -1) {
                openBracket = len;
            }
            var at = str.indexOf("@", startFrom);
            if (at == -1) {
                at = len;
            }
            var closeBrace = str.indexOf("}", startFrom);
            if (closeBrace == -1) {
                closeBrace = len;
            }
            var startBrace = str.indexOf("{", startFrom);
            if (startBrace == -1) {
                startBrace = len;
            }
            var name = str.substring(startFrom, Math.min(openBracket, at, space, closeBrace, startBrace));
            if (name == "") {
                return ValidationRule.createRules(str, startFrom + 1);
            }
            var rule = VALIDATION_RULES[name];
            if (rule == null) {
                throw Error("Invalid Rule Format/Name");
            }
            var v_rules = [];
            var rule = {rule: rule};
            v_rules.push(rule);
            var cursor = startFrom + name.length;

            var fetchAllCondition = function () {
                while (str.charAt(at) == "@") {
                    var ifornot = fetchIfOrNot(str, at + 1);
                    if (!rule.condition) {
                        rule.condition = ifornot.condition;
                    } else if ($.isArray(rule.condition)) {
                        rule.condition.push(ifornot.condition);
                    } else {
                        rule.condition = [rule.condition, ifornot.condition]
                    }
                    at = ifornot.parsedIndex + 1;
                }
                cursor = at;
            }

            if (cursor == openBracket && openBracket != len) {
                var param = fetchParam(str, openBracket + 1);
                cursor += param.length + 2;
                rule.param = param.replace(/\]\]/g, "]").exSplit();
                if (at == cursor && openBracket != len) {
                    fetchAllCondition();
                }
            } else if (at == cursor && startBrace != len) {
                fetchAllCondition();
            }

            if (cursor != len && str.charAt(cursor) == " ") {
                var hrules = ValidationRule.createRules(str, cursor + 1);
                $.each(hrules, function () {
                    v_rules.push(this);
                })
            }
            return v_rules;
        }
    }
})();

(function () {
    var DEFAULTS = {
        validate_on: { //blur, change, immediate, keyup, keydown
            text: ["blur"],
            password: ["blur"],
            select: ["change"],
            radio: ["change"],
            file: ["change"],
            checkbox: ["change"],
            container: ["change"]
        },
        validate_on_call_only: false,
        clear_on_focus: true,
        show_error: true,
        attribute_name: "validation",
        error_class: "validation-error",
        re_init_on_valid: false,
        error_position: "after", //before, after, parent-after, parent-before, positioned-up, positioned-below, positioned-right
        error_parent_ref_class: undefined,
        show_error_close: true,
        error_template: "<div class='errorlist'><span class='pointer'></span><span class='close-btn'></span><div class='message-block message-text'></div></div>",
        displayError: undefined //override of defined showError method
    }

    var instance_counter = 0;

    function checkValidators(field, validators) {
        var len = validators.length;
        var error = null;

        for (var type = 0; type < len; type++) {
            var validator = validators[type];
            if (typeof validator.error != "undefined") {
                return validator.error;
            }
            var check = true;
            if (validator.condition) {
                var conditions = $.isArray(validator.condition) ? validator.condition : [validator.condition];
                var condCount = conditions.length;
                for (var p = condCount - 1; p > -1; p--) {
                    var pass = true;
                    var condition = conditions[p];
                    switch (condition.check_type) {
                        case "global":
                            if ($(condition.selector).length == 0) {
                                pass = false;
                            }
                            break;
                        case "self":
                            if (condition.selector == ":blank") {
                                var val = field.val();
                                pass = $.trim(val) == "";
                            } else {
                                pass = field.is(condition.selector);
                            }
                            break;
                        case "this":
                            pass = field.find(condition.selector).length != 0
                            break;
                        case "rule":
                            pass = checkValidators(field, condition.rules) == null;
                    }
                    if ((condition.type == "if" && !pass) || (condition.type == "not" && pass)) {
                        check = false;
                        break;
                    }
                }
            }
            var rule = validator.rule;
            if (rule == VALIDATION_RULES.skip) {
                if (check) {
                    break;
                } else {
                    continue;
                }
            }
            if (rule == VALIDATION_RULES.skip_next) {
                if (check) {
                    type++;
                }
                continue;
            }
            if (rule == VALIDATION_RULES.check) {
                if (check) {
                    continue;
                } else {
                    break;
                }
            }
            if (rule == VALIDATION_RULES.check_next) {
                if (!check) {
                    type++;
                }
                continue;
            }
            if (rule == VALIDATION_RULES.fail) {
                if (check) {
                    var params = field.attr("message_params_" + type) || field.attr("message_params");
                    if (params) {
                        params = params.split(" ")
                    }
                    return {
                        msg_template: field.attr("message_template_" + type) || field.attr("message_template") || "entry.invalid",
                        msg_params: params || [],
                        rule: rule
                    };
                } else {
                    continue;
                }
            }

            if (check) {
                var param = validator.param;
                var isvalid = rule.check(field.val(), param, field);
                if (isvalid !== true) {
                    var template = field.attr("message_template_" + type) || field.attr("message_template");
                    if (!template) {
                        template = isvalid.msg_template || rule.msg_template;
                    }
                    if (!template) {
                        template = "";
                    }

                    var msgParams = field.attr("message_params_" + type) || field.attr("message_params");
                    if (!msgParams) {
                        msgParams = isvalid.msg_params || rule.msg_params;
                    } else {
                        msgParams = msgParams.exSplit(" ");
                    }
                    if (!msgParams) {
                        msgParams = [];
                    }

                    error = {
                        msg_template: template,
                        msg_params: msgParams,
                        rule: rule
                    }
                }
            }

            if (error != null) {
                break;
            }
        }

        return error;
    }

    /*
     * Panel factory
     */
    var Panel = function (panel/*$_Dom*/, options/*Object*/) {
        this.fields = [];
        this.elm = panel;
        this.instance_count = instance_counter++;
        var tagOptions = panel.config("validation")
        $.extend(true, this, DEFAULTS, tagOptions, options);
        this.attribute_name = tagOptions.attr || this.attribute_name;
        this.attach(panel.find("[" + this.attribute_name + "]"));
    };
    window.ValidationPanel = Panel;

    Panel.prototype = {
        attach: function (fields/*$_Dom*/, options/*Object*/) {
            var _panel = this;
            fields.each(function () {
                var _field = $(this);
                _field.on("jqclean", function () {
                    _panel.detach(_field)
                });
                var field = new Field(_field, _panel, options)
                _field.data("validator-filed-inst", field)
                _panel.fields.push(field);
            });
        },
        isValid: function () {
            $.each(this.fields, function () {
                this.validate();
            });
            var rtrn = true;
            $.each(this.fields, function () {
                if (!this.valid) {
                    rtrn = false;
                    return false;  // to break;
                }
            });
            return rtrn;
        },
        detach: function (elem) {
            if (elem.length) {
                elem = elem[0];
            }
            var rIndex = -1;
            $.each(this.fields, function (index) {
                if (this.elm[0] == elem) {
                    rIndex = index;
                    this.destroy();
                    return false;
                }
            });
            if (rIndex > -1) {
                this.fields.splice(rIndex, 1);
            }
        },
        getValidatorRules: function (elem) {
            if (elem.length) {
                elem = elem[0];
            }
            var returnData;
            $.each(this.fields, function () {
                if (this.elm[0] == elem) {
                    returnData = this.validatorRules;
                    return false;
                }
            });
            return returnData;
        },
        addValidatorRule: function (elem, ruleString, message) {
            if (elem.length) {
                elem = elem[0];
            }
            var found = false;
            $.each(this.fields, function () {
                if (this.elm[0] == elem) {
                    var rules;
                    if (typeof ruleString == "string") {
                        rules = ValidationRule.createRules(ruleString);
                    } else {
                        rules = [{rule: {check: ruleString, msg_template: message}}];
                    }
                    var tt_ = this;
                    $.each(rules, function () {
                        tt_.validatorRules.push(this);
                    });
                    found = true;
                    return false;
                }
            })
            if (!found) {
                this.attach($(elem), this);
            }
        },
        destroy: function () {
            $.each(this.fields, function () {
                this.destroy();
            })
            this.fields = [];
        },
        reInit: function () {
            this.destroy();
            this.attach(this.elm.find("[" + this.attribute_name + "]"), this);
        },
        position: function () {
            this.fields.each(function () {
                if (!this.valid) {
                    this.showError(this.error)
                }
            })
        },
        state: function () {
            return this.fields.collect("valid").all()
        }
    };

    /*
     * Field factory
     */
    var Field = function (field/*$_Dom*/, panel/*Object*/, options) {
        this.elm = field;
        var tagOptions = field.config("validation")
        this.options = $.extend(true, {}, bm.omit(panel, ["fields", "elm", "instance_count"], true), tagOptions, options);
        this.instance_count = instance_counter++;
        this.valid = true;
        if (this.options.displayError) {
            this.showError = this.options.displayError;
        }
        this.options.error_position = field.attr("error-position") || (panel.elm ? panel.elm.attr("error-position") : undefined) || this.options.error_position;
        this.options.validate_on_call_only = field.attr("validate-on") ? field.attr("validate-on") == "call-only" : (this.options.validate_on == "call-only" || this.options.validate_on_call_only);

        if (this.options.attribute_name) {
            var validation = field.attr(this.options.attribute_name);
            if ($.trim(validation) == "") {
                this.validatorRules = [];
            } else {
                try {
                    this.validatorRules = ValidationRule.createRules(validation);
                } catch (ex) {
                    this.validatorRules = [
                        {error: "invalid.rule.format.name"}
                    ];
                    return;
                }
            }
        } else {
            this.validatorRules = [];
        }

        if (!this.options.detached) {
            this.initEvents();
        }
    };

    window.ValidationField = {
        createDetachedField: function (field, options) {
            options = $.extend({detached: true}, options);
            return new Field(field, $.extend(true, {}, DEFAULTS, options));
        },
        validateAs: function (field/*$_Dom*/, ruleString/*String*/) {
            try {
                return checkValidators(field, ValidationRule.createRules(ruleString))
            } catch (ex) {
                return {
                    msg_template: $.i18n.prop("unable.validate"),
                    msg_params: [],
                    rule: undefined
                };
            }
        }
    };

    Field.prototype = {
        initEvents: function () {
            var field = this.elm;

            var evNamespace = ".validator_" + this.instance_count;
            field.off(evNamespace);
            var obj = this;
            field.on("validate" + evNamespace, function () {
                obj.validate();
            });

            if (!this.options.validate_on_call_only) {
                function bindEvent(field, evNamespace) {
                    var tagType;
                    if (field.is("select")) {
                        tagType = "select";
                    } else if (field.is("textarea")) {
                        tagType = "text";
                    } else if (field.is("[type='file']")) {
                        tagType = "file";
                    } else if (field.is("[type='text']")) {
                        tagType = "text";
                    } else if (field.is("[type='password']")) {
                        tagType = "password";
                    } else if (field.is("[type='radio']")) {
                        tagType = "radio";
                    } else if (field.is("[type='checkbox']")) {
                        tagType = "checkbox";
                    } else {
                        tagType = "container";
                    }
                    var evName = field.attr("validate-on")
                    if (evName) {
                        evName = evName.split(",");
                    } else {
                        evName = obj.options.validate_on[tagType];
                    }
                    if (!$.isArray(evName)) {
                        evName = [evName];
                    }
                    //blur, change, immediate, keyup, keydown
                    $.each(evName, function () {
                        switch ("" + this) {
                            case "immediate":
                                field.on("ichange" + evNamespace, function () {
                                    obj.validate();
                                });
                                break;
                            default:
                                field.on(this + evNamespace, function () {
                                    obj.validate();
                                });
                        }
                    });
                }

                bindEvent(field, evNamespace);
                var dependsOn = this.elm.attr("depends");
                if (dependsOn) {
                    dependsOn = $(dependsOn);
                    if (dependsOn.length) {
                        var id = field.attr("id");
                        if (!id) {
                            id = bm.getUUID();
                            field.attr("id", id);
                        }
                        var evNamespace = ".validator_" + this.instance_count + "_" + id;
                        field.off(evNamespace);
                        bindEvent(dependsOn, evNamespace)
                    }
                }
            }

            if (this.options.clear_on_focus) {
                field.bind("focus" + evNamespace, function () {
                    obj.clear();
                });
            }
        },
        validate: function () {
            var obj = this,
                field = obj.elm;
            var validators = obj.validatorRules;
            if (!validators) {
                return true;
            }
            obj.clear();

            function sendError(error) {
                obj.valid = false;
                obj.error = error;
                error.validator = obj;
                var proceed = obj.elm.trigger("invalid", error);
                if (proceed === false) {
                    return;
                }
                if (obj.options.show_error) {
                    obj.showError(error);
                }
            }

            try {
                var error = checkValidators(field, validators);
                if (error != null) {
                    sendError(error);
                } else {
                    obj.valid = true;
                    obj.elm.trigger("valid");
                }
            } catch (k) {
                sendError({
                    msg_template: k.message,
                    msg_params: [],
                    rule: undefined
                })
            }

            return obj.valid
        },
        clear: function () {
            if (!this.errorBlock) {
                return;
            }
            var errorOn = this.elm.removeClass(this.options.error_class).attr("error-on");
            if (errorOn) {
                errorOn = $("#" + errorOn);
                errorOn.removeClass(this.options.error_class);
            } else {
                errorOn = this.elm
            }
            if (this.options.error_position == "inline") {
                var inlineErrorOn = errorOn
                if (errorOn.find(".inline-error-target").length) {
                    inlineErrorOn = errorOn.find(".inline-error-target")
                }
                if (inlineErrorOn.attr("type") == "password") {
                    var cloned = errorOn[0].inlineErrorProxy
                    if (cloned) {
                        cloned.remove()
                        delete errorOn[0].inlineErrorProxy
                    }
                    errorOn.css("visibility", "visible")
                } else {
                    var cache = inlineErrorOn.attr("value-cache");
                    inlineErrorOn.val(cache);
                    inlineErrorOn.removeAttr("value-cache");
                }
            }
            var otherErrors = this.errorBlock.siblings(".message-block");
            if (!otherErrors.length) {
                this.errorBlock.closest(".errorlist").remove();
                this.elm.removeClass("error-field-error")
            } else {
                this.errorBlock.remove();
            }
            this.errorBlock = null;
            this.elm.trigger("clear-error");
        },
        destroy: function () {
            this.clear();
            this.elm.off(".validator_" + this.instance_count);
        },
        showError: function (error) {
            var errorlist
            if (this.errorBlock) {
                this.clear();
            }
            if (typeof this.options.error_template == "function") {
                errorlist = this.options.error_template.call(this, error);
            } else {
                errorlist = $(this.options.error_template);
            }
            this.elm.addClass("error-field-error");
            var errorOn = this.elm.addClass(this.options.error_class).attr("error-on");
            if (errorOn) {
                errorOn = $("#" + errorOn);
                errorOn.addClass(this.options.error_class);
            } else {
                errorOn = this.elm;
            }
            var errorBlock = errorlist.find(".message-block");
            var messageBlock = errorBlock.is(".message-text") ? errorBlock : errorBlock.find(".message-text");
            messageBlock.append($.i18n.prop(error.msg_template, error.msg_params));
            //after, before, parent-after, parent-before, positioned-up, positioned-below, positioned-right
            errorlist.addClass(this.options.error_position);
            switch (this.options.error_position) {
                case "inline":
                    if (errorOn.find(".inline-error-target").length) {
                        errorOn = errorOn.find(".inline-error-target")
                    }
                    if (errorOn.attr("type") == "password") {
                        var cloned = errorOn.clone()
                        cloned.attr("type", "text")
                        errorOn.after(cloned)
                        cloned.val(messageBlock.text()).addClass("inline-error")
                        cloned.focus(function () {
                            cloned.remove()
                            errorOn.css("visibility", "visible")[0].focus()
                            delete errorOn[0].inlineErrorProxy
                        }).css({position: "absolute"}).offset(errorOn.offset())
                        errorOn.css("visibility", "hidden")
                        errorOn[0].inlineErrorProxy = cloned
                    } else {
                        var valueCache = errorOn.val();
                        errorOn.val(messageBlock.text()).addClass("inline-error");
                        errorOn.attr("value-cache", valueCache);
                    }
                    break;
                case "after":
                    var nextDiv = errorOn.next(".errorlist");
                    if (nextDiv.length) {
                        nextDiv.find(".message-block:last").after(errorBlock)
                    } else {
                        errorOn.after(errorlist);
                    }
                    break;
                case "before":
                    var prevDiv = errorOn.prev(".errorlist");
                    if (prevDiv.length) {
                        prevDiv.find(".message-block:last").after(errorBlock)
                    } else {
                        errorOn.before(errorlist);
                    }
                    break;
                case "parent-after":
                    var parentDiv;
                    if (this.options.error_parent_ref_class) {
                        parentDiv = errorOn.closest("." + this.options.error_parent_ref_class);
                    } else {
                        parentDiv = errorOn.parent();
                    }
                    var prevDiv = parentDiv.next(".errorlist");
                    if (prevDiv.length) {
                        prevDiv.find(".message-block:last").after(errorBlock)
                    } else {
                        parentDiv.after(errorlist);
                    }
                    break;
                case "parent-before":
                    var parentDiv;
                    if (this.options.error_parent_ref_class) {
                        parentDiv = errorOn.closest("." + this.options.error_parent_ref_class);
                    } else {
                        parentDiv = errorOn.parent();
                    }
                    var prevDiv = parentDiv.prev(".errorlist");
                    if (prevDiv.length) {
                        prevDiv.find(".message-block:last").after(errorBlock)
                    } else {
                        parentDiv.before(errorlist);
                    }
                    break;
                case "none":
                    break;
                default:
                    errorlist.addClass("positioned-error");
                    var position = this.options.error_position.substring(11);
                    var nextDiv = errorOn.next(".errorlist");
                    if (nextDiv.length) {
                        nextDiv.find(".message-block:last").after(errorBlock)
                    } else {
                        errorOn.after(errorlist);
                        nextDiv = errorlist;
                    }
                    switch (position) {
                        case "up":
                            nextDiv.position({
                                my: "left bottom",
                                at: "left+50 top-" + nextDiv.bottomRib(true, false, false),
                                of: errorOn,
                                collision: "none"
                            });
                            break;
                        case "below":
                            nextDiv.position({
                                my: "left top",
                                at: "left+50 bottom+" + nextDiv.topRib(true, false, false),
                                of: errorOn,
                                collision: "none"
                            });
                            break;
                        case "right":
                            nextDiv.position({
                                my: "left top",
                                at: "right+" + nextDiv.leftRib(true, false, false) + " top-" + nextDiv.outerHeight() * .33,
                                of: errorOn,
                                collision: "none"
                            });
                            break;
                        case "left":
                            nextDiv.position({
                                my: "right top",
                                at: "left-" + nextDiv.leftRib(true, false, false) + " top-" + nextDiv.outerHeight() * .33,
                                of: errorOn,
                                collision: "none"
                            });
                    }
            }
            this.errorBlock = errorBlock;
            if (this.options.show_error_close) {
                errorlist.find(".close-btn").click(function () {
                    errorlist.hide();
                })
            } else {
                errorlist.find(".close-btn").remove();
            }
        },
        isValid: function() {
            this.validate()
            return this.valid
        }
    };

    /*
     Validation extends by jQuery prototype
     */
    $.extend($.fn, {
        attachValidator: function (options) {
            return this.each(function () {
                var elm = $(this);
                var panel = new Panel(elm, options || {});
                elm.data("validator-inst", panel);
            });
        },
        valid: function (options) {
            var isValid = true;
            this.each(function () {
                var _self = $(this);
                var validator = _self.data("validator-inst") || _self.data("validator-filed-inst");
                if (!validator) {
                    _self.attachValidator(options || {});
                    validator = _self.data("validator-inst");
                } else if (typeof options == "string") {
                    return validator[options]()
                } else if (validator.re_init_on_valid) {
                    validator.reInit();
                }
                var _isValid = validator.isValid();
                isValid = isValid && _isValid;
            });
            return isValid;
        },
        clearValidator: function () {
            this.each(function () {
                var elm = $(this);
                var panel = elm.data("validator-inst");
                if (panel) {
                    panel.destroy();
                    elm.removeData("validator")
                }
            });
        },
        updateValidator: function (options) {
            this.clearValidator();
            return this.attachValidator(options || {});
        },
        clearValidationMessage: function (options) {
            this.find('.errorlist').remove();
            this.find('.validation-error').removeClass("validation-error").removeClass("error-field-error");
        }
    });

    window.validation = {
        setDefaults: function (defaults) {
            $.extend(DEFAULTS, defaults, {validate_on: $.extend({}, DEFAULTS.validate_on, defaults.validate_on)});
        }
    }
})();