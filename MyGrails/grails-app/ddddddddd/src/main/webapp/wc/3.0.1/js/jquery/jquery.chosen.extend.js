(function() {
    bm.onReady(window, "VALIDATION_RULES", function() {
        VALIDATION_RULES["chosen-required"] = {
            check : function(value, param, input) {
                return input.find("input").length > 0
            },
            msg_template : VALIDATION_RULES.required.msg_template
        }
    })

    $.fn.extend({
        multitxtchosen: function(options) {
            if(!this.length) {
                return this;
            }
            return $(this).each(function() {
                if (!($(this)).hasClass("chosen-done")) {
                    if(options.type == "multi" && !options.name) {
                        options.name = $(this).attr("name");
                    }
                    var inst = new MultiTxtChosen(this, options);
                    $(this).data("wcui-multitxtchosen", inst);
                    return inst;
                }
            });
        },
        multispecialchosen: function(options) {
            if(!this.length) {
                return this;
            }
            return this.each(function(input_field) {
                var $this;

                $this = $(this);
                if (!$this.hasClass("chosen-done")) {
                    var selected = $this[input_field].getAttribute("selected");
                    options.selectedItem = options.highlighted
                    return $this.data('wcui-chosen', new SpecialMultiChosen(this, options));
                }
            });
        }
    });

    var CSVParser = {
        to_array: function(container) {
            var value = container.val();
            if(value == "") {
                return [];
            }
            return value.exSplit();
        }
    };

    var MultiTextParser = {
        to_array: function(container) {
            var value = [];
            container.find("input").each(function() {
                value.push(this.value);
            });
            return value;
        }
    }

    var MultiTxtChosen = (function() {
        function Chosen() {
            arguments[0].multiple = true;
            Chosen._super.constructor.apply(this, arguments);
        }

        Chosen.inherit(window.Chosen);

        Chosen.prototype.results_build = function() {
            var content, data, _i, _len, _ref;
            this.parsing = true;
            this.selected_option_count = 0;
            this.selected_Invalid_count = 0;
            if(this.options.type == "multi") {
                this.results_data = MultiTextParser.to_array(this.form_field_jq);
                this.form_field_jq.html("");
            } else if(this.options.type == "csv") {
                this.results_data = CSVParser.to_array(this.form_field_jq);
                this.csv_as_array = [];
                this.form_field_jq.val("");
            }
            content = '';
            _ref = this.results_data;
            for (_i = 0, _len = _ref.length; _i < _len; _i++) {
                data = _ref[_i];
                this.choice_build({
                    array_index: "_" + new Date().getTime() + "_",
                    html: data
                });
            }
            this.search_field_disabled();
            this.show_search_field_default();
            this.search_field_scale();
            this.search_results.html(content);
            return this.parsing = false;
        };

        Chosen.prototype.getSelectedCount = function() {
            this.results_data.length;
        }

        Chosen.prototype.choice_build = function(item) {
            if($.trim(item.html) == "") {
                return;
            }
            var choice_id, link,
                _this = this;
            choice_id = this.container_id + "_c_" + item.array_index;
            var valid = true;
            if(this.check_duplicate(item.html)) {
                return;
            }
            if(this.form_field_jq.is('[chosen-validation]')) {
                var rule = this.form_field_jq.attr('chosen-validation')
                var error = ValidationField.validateAs($("<input>").val(item.html), rule)
                valid = !error;
            }
            var choice = $('<li class="search-choice ' + (valid ? '' : 'invalid') + '" id="' + choice_id + '"><span>' + item.html.htmlEncode() +
                '</span><a href="javascript:void(0)" class="search-choice-close" data-option-array-index="' + item.array_index + '"></a></li>')
            this.search_container.before(choice);
            link = choice.find("a").first();
            if(valid) {
                this.selected_option_count++;
                if(this.options.type == "multi") {
                    this.form_field_jq.append("<input id='result_" + choice_id + "' type='hidden' name='" + this.options.name + "' value='" + item.html + "'>");
                } else if(this.options.type == "csv") {
                    var cVal = this.form_field_jq.val();
                    var aVal = item.html;
                    this.csv_as_array.push(aVal);
                    aVal = aVal.indexOf(",") > -1 || aVal.charAt(0) == "(" ? "(" + aVal + ")" : aVal
                    if(cVal.length > 0) {
                        aVal = cVal + "," + aVal;
                    }
                    this.form_field_jq.val(aVal);
                }
                this.form_field_jq.trigger("change")
            } else {
                this.selected_Invalid_count++;
            }
            return link.attr("chosen-option", true).click(function(evt) {
                _this.choice_destroy_link_click(evt);
                _this.search_field_scale();
                _this.form_field_jq.trigger("change")
            });
        };

        Chosen.prototype.check_duplicate = function(text) {
            if(this.options.type == "multi") {
                return this.form_field_jq.find("[value='" + text + "']").length ? true : false
            } else if(this.options.type == "csv") {
                return $.inArray(text, this.csv_as_array) != -1;
            }
            return false;
        }

        Chosen.prototype.choice_destroy = function(link) {
            this.show_search_field_default();
            var li = link.closest('li');
            if(this.options.type == "multi") {
                this.form_field_jq.find("#result_" + li.attr("id")).remove();
            } else if(this.options.type == "csv") {
                var ind = $.inArray(li.find("span:first").html(), this.csv_as_array);
                this.csv_as_array.splice(ind, 1);
                this.form_field_jq.val(this.csv_as_array.exJoin());
            }
            this.selected_option_count--;
            return li.first().remove();
        };

        Chosen.prototype.close_field = function() {
            if(this.search_field.val()) {
                this.choice_build({
                    array_index: "_" + new Date().getTime() + "_",
                    html: this.search_field.val()
                });
                this.search_field.val("");
            }
            return Chosen._super.close_field.call(this);
        };

        Chosen.prototype.input_blur = function (evt) {
            var _this = this;
            if (!this.mouse_on_container) {
                this.active_field = false;
                return setTimeout((function () {
                    return _this.blur_test();
                }), 100);
            } else {
                _this.close_field();
                _this.input_focus(evt);
            }
        };

        Chosen.prototype.keydown_checker = function(evt) {
            var stroke, _ref;
            stroke = (_ref = evt.which) != null ? _ref : evt.keyCode;
            this.search_field_scale();
            if (stroke !== 8 && this.pending_backstroke) this.clear_backstroke();
            switch (stroke) {
                case 8:
                    this.backstroke_length = this.search_field.val().length;
                    break;
                case 9:
                case 13:
                case 32:
                case 45:
                    this.choice_build({
                        array_index: "_" + new Date().getTime() + "_",
                        html: this.search_field.val()
                    });
                    this.search_field.val("");
                    evt.preventDefault();
                    break;
                case 38:
                case 40:
                    evt.preventDefault();
                    break;
            }
        };

        Chosen.prototype.keyup_checker = function (evt) {
            var stroke, _ref;
            stroke = (_ref = evt.which) != null ? _ref : evt.keyCode;
            this.search_field_scale();
            switch (stroke) {
                case 8:
                    if (this.is_multiple && this.backstroke_length < 1 && (this.choices_count() > 0 || this.selected_Invalid_count > 0)) {
                        return this.keydown_backstroke();
                    } else if (!this.pending_backstroke) {
                        this.result_clear_highlight();
                        return this.results_search();
                    }
                    break;
                case 13:
                    evt.preventDefault();
                    if (this.results_showing) {
                        return this.result_select(evt);
                    }
                    break;
                case 27:
                    if (this.results_showing) {
                        this.results_hide();
                    }
                    return true;
                case 9:
                case 38:
                case 40:
                case 16:
                case 91:
                case 17:
                    break;
                default:
                    return this.results_search();
            }
        };

        Chosen.prototype.clear_all_selection = function() {
            this.container.find(".chosen-choices .search-choice .search-choice-close").trigger("click")
        };

        /** Empty implementation to give support for Abstract chosen **/
        Chosen.prototype.choices_click = Chosen.prototype.result_select = Chosen.prototype.winnow_results = function() {};

        return Chosen;

    })();

    var SpecialMultiChosen = (function() {
        function Chosen() {
            arguments[0].multiple = true;
            Chosen._super.constructor.apply(this, arguments);
        }

        Chosen.inherit(window.Chosen)

        Chosen.prototype.set_up_html = function () {
            var container_classes, container_props;

            container_classes = ["chosen-container"];
            container_classes.push("chosen-container-" + (this.is_multiple ? "multi" : "single"));
            if (this.inherit_select_classes && this.form_field.className) {
                container_classes.push(this.form_field.className);
            }
            if (this.is_rtl) {
                container_classes.push("chosen-rtl");
            }
            container_props = {
                'class': container_classes.join(' '),
                'title': this.form_field.title
            };
            if (this.form_field.id.length) {
                container_props.id = this.form_field.id.replace(/[^\w]/g, '_') + "_chosen";
            }
            this.container = $("<div />", container_props);
            if (this.form_field_jq.attr("validation") && !this.form_field_jq.is("[error-on]")) {
                this.form_field_jq.attr('error-on', this.container_id);
            }
            var hiddenInput = $("<input/>", {
                type: "hidden",
                name: this.options.hiddenfieldname || "highlighted",
                value: ""
            })
            if (this.is_multiple) {
                this.container.html('<ul class="chosen-choices"><li class="search-field"><input type="text" value="' + this.default_text + '" class="default" autocomplete="off" style="width:25px;" /></li></ul><div class="chosen-drop"><ul class="chosen-results"></ul></div>');
                this.container.find(".chosen-choices").append(hiddenInput)
            } else {
                this.container.html('<a class="chosen-single chosen-default" tabindex="-1"><span>' + this.default_text + '</span><div><b></b></div></a><div class="chosen-drop"><div class="chosen-search"><input type="text" autocomplete="off" /></div><ul class="chosen-results"></ul></div>');
            }
            var chosenResult = this.container.find(".chosen-results")
            chosenResult.scrollbar({
                vertical: {
                    offset: -2,
                    height: "auto"
                }
            })
            chosenResult.css({maxHeight: 120 + "px"});
            this.form_field_jq.hide().after(this.container);
            this.dropdown = this.container.find('div.chosen-drop').first();
            this.search_field = this.container.find('input').first();
            this.search_results = this.container.find('ul.chosen-results').first();
            this.search_field_scale();
            if (this.is_multiple) {
                this.search_choices = this.container.find('ul.chosen-choices').first();
                this.search_container = this.container.find('li.search-field').first();
            } else {
                this.search_container = this.container.find('div.chosen-search').first();
                this.selected_item = this.container.find('.chosen-single').first();
            }
            this.results_build();
            this.set_tab_index();
            this.set_label_behavior();

            return this.form_field_jq.trigger("chosen:ready", {
                chosen: this
            });
        };

        Chosen.prototype.choice_build = function(item) {
            var choice, close_link,
                _this = this;

            if((_this.choices_count() == 1 && _this.active_field) || (_this.options.selectedItem == item.value && !_this.active_field) ) {
                choice = $('<li />', {
                    "class": "search-choice"
                }).html("<span class='selected' rel='"+ item.array_index +"'>" + item.html + "</span>");
                _this.changeSelected(choice.find("span"), false);

            } else {
                choice = $('<li />', {
                    "class": "search-choice"
                }).html("<span rel='"+ item.array_index +"'>" + item.html + "</span>")
            }

            choice.find("span").click(function(evt){
                if($(this).closest('.chosen-container').hasClass('chosen-disabled')) {
                    return false;
                }
                return _this.choice_click(evt, this);
            });
            if (item.disabled) {
                choice.addClass('search-choice-disabled');
            } else {
                close_link = $('<a />', {
                    href: '#',
                    "class": 'search-choice-close',
                    'data-option-array-index': item.array_index
                });
                close_link.bind('click.chosen', function(evt) {
                    if($(this).closest('.chosen-container').hasClass('chosen-disabled')) {
                        return false;
                    }
                    return _this.choice_destroy_link_click(evt);
                });
                choice.append(close_link);
            }
            return this.search_container.before(choice);
        };

        Chosen.prototype.result_deselect = function(pos) {
            var result, result_data, parent_val;

            result_data = this.results_data[pos];
            var name = this.options.hiddenfieldname || "highlighted";
            parent_val = this.search_choices.find("input[name='" + name +"']").val();
            if(parent_val == result_data.value) {
                this.search_choices.find("input[type=hidden]").val("")
            }
            if (!this.form_field.options[result_data.options_index].disabled) {
                result_data.selected = false;
                this.form_field.options[result_data.options_index].selected = false;
                this.selected_option_count = null;
                result = $("#" + this.container_id + "_o_" + pos);
                result.removeClass("result-selected").addClass("active-result").show();
                this.result_clear_highlight();
                this.winnow_results();
                this.form_field_jq.trigger("change", {
                    deselected: this.form_field.options[result_data.options_index].value
                });
                this.search_field_scale();
                return true;
            } else {
                return false;
            }
        };

        Chosen.prototype.changeSelected = function(spn, triggerChange) {
            spn = $(spn)
            var pos, result_data, parent, choices = this.search_choices;
            pos = spn.attr("rel");
            result_data = this.results_data[pos];
            choices.find(".highlighted").removeClass("highlighted")
            spn.closest("li").addClass("highlighted");
            parent = choices.find("input[type=hidden]");
            parent.val(result_data.value)
            if(triggerChange !== false) {
                parent.trigger("change")
            }
            choices.append(parent)
        }

        Chosen.prototype.choice_click = function(evt, spn){
            this.changeSelected(spn);
            return false;
        };
        return Chosen
    })()
})();
