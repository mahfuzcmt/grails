 bm.onReady(app.tabs, "event", function() {
    app.tabs.event.Calendar = function (content, parentTab) {
        this.body = content;
        this.parentTab = parentTab;
    };

    var _ct =  app.tabs.event.Calendar.prototype;

    _ct.reload = function() {
        this.parentTab.body.find('.bmui-tab').tabify('reload', 'calendar');
    };

    _ct.init = function () {
        var _self = this;
        var namespace = "event-management-calendar";
        app.global_event.on("event-create." + namespace + " event-update." + namespace + " equipment-invitation-approved." + namespace +
            " venue-location-invitation-approved." + namespace +
            " event-session-updated." + namespace + " equipment-invitation-request-updated." + namespace, function() {
            _self.reload()
        });
        _self.parentTab.on("close", function() {
            app.global_event.off("." + namespace)
        });
        var container = _self.body;
        var calendarType = "public";
        _self.calendarWrapper = container.find('.calendar-container');
        container.find('.calendar-type').click(function() {
            resetActiveLink(this);
            _self.calendarWrapper.html('');
            calendarType = $(this).attr('type');
            if(calendarType != 'public') {
                loadVenueOrEquipmentCalendar()
            } else {
                container.find('.selector').html("").hide();
                _self.initCalendar(calendarType)
            }
        });
        _self.initCalendar(calendarType);

        function resetActiveLink(link) {
            $.each(container.find('.calendar-type'), function(idx, val) {
                $(val).removeClass('active')
            });
            $(link).addClass('active')
        }

        function loadVenueOrEquipmentCalendar() {
            bm.ajax({
                url: app.baseUrl + 'eventAdmin/' + (calendarType == 'venue' ? 'loadVenueAsJSON' : 'loadEquipmentAsJSON'),
                success: function(optMap) {
                    if(calendarType == 'venue') {
                        var opts = '';
                        $.each(optMap, function(key, val) {
                            opts += '<option value="' + key + '">' + val + '</option>'
                        });
                        if(!$.isEmptyObject(optMap)) {
                            var newSelect = $('<select class="venue" name="venue.id"></select>').append(opts);
                            container.find('.selector').html(newSelect).show();
                            newSelect.chosen({disable_search: true});
                            container.find('select.venue').change(function() {
                                var selectedVenue = $(this).val();
                                if(selectedVenue) {
                                    var childSelector = container.find('.selector .child-selector');
                                    if(!childSelector.length) {
                                        childSelector = $('<span class="child-selector"></span>');
                                        container.find('.selector').append(childSelector);
                                    }
                                    bm.ajax({
                                        url: app.baseUrl + 'eventAdmin/loadVenueLocationAsJSON',
                                        data: {'venue.id': selectedVenue},
                                        success: function(locations) {
                                            if(!$.isEmptyObject(locations)) {
                                                var opts = '<option value="-1">' + $.i18n.prop('events.of.all.venue.location') + '</option>';
                                                $.each(locations, function(key, val) {
                                                    opts += '<option value="' + key + '">' + val + '</option>'
                                                });
                                                childSelector.html('<select class="venue-location" name="location.id">' + opts + '</select>');
                                                childSelector.find('select.venue-location').show().chosen({disable_search: true});
                                            } else {
                                                childSelector.html('<span class="error not-found">' + $.i18n.prop('no.venue.location.found') + '</span>').show();
                                            }
                                            childSelector.find('select.venue-location').change(function() {
                                                _self.initCalendar('location', $(this).val(), selectedVenue);
                                            });
                                            childSelector.find('select.venue-location').trigger('change');
                                        }
                                    });
                                } else {
                                    container.find('.child-selector').remove();
                                    _self.calendarWrapper.html('');
                                }
                            });
                            container.find('select.venue').trigger('change');
                        } else {
                            container.find('.selector').html('<span class="error not-found">' + $.i18n.prop('no.venue.found') + '</span>').show();
                        }
                    } else {
                        var opts = '';
                        $.each(optMap, function(key, val) {
                            opts += '<option value="' + key + '">' + val + '</option>'
                        });
                        if($.isEmptyObject(optMap)) {
                            container.find('.selector').html('<span class="error not-found">' + $.i18n.prop('no.equipment.found') + '</span>').show()
                        } else {
                            var newSelect = $('<select class="equipment" name="equipment.id"></select>').append(opts);
                            container.find('.selector').html(newSelect).show();
                            newSelect.chosen({disable_search: true});
                            container.find('select.equipment').change(function() {
                                _self.initCalendar('equipment', $(this).val());
                            });
                            container.find('select.equipment').trigger('change');
                        }
                    }
                }
            })
        }
    };

    _ct.initCalendar = function(calendarType, selectedValue, venueAsParent) {
        var _self = this;
        if(selectedValue == undefined || selectedValue != '0') {
            var dateFormat = this.body.find('.dateFormat').val().replaceAll('E', 'd');
            var timeFormat = this.body.find('.timeFormat').val().replace('aaa', 'TT');
            bm.ajax({
                url: app.baseUrl + 'eventAdmin/loadAllEventData',
                data: {calendarType: calendarType, selectedValue: selectedValue, venueAsParent: venueAsParent},
                dataType: 'json',
                success: function(resp) {
                    _self.calendarWrapper.html('');
                    var dayKeys = ['sunday', 'monday', 'tuesday', 'wednesday', 'thursday', 'friday', 'saturday'];
                    var dayNames = [];
                    for(var i = 0; i < dayKeys.length; i++) {
                        dayNames.push($.i18n.prop(dayKeys[i]))
                    }
                    _self.calendarWrapper.fullCalendar({
                        viewDisplay: function(view) {
                            if(view.name === 'agendaWeek' || view.name === 'agendaDay') {
                                view.setHeight(9999);
                            }
                        },
                        header: {
                            left: 'month,agendaWeek,agendaDay',
                            center: 'title',
                            right: 'today prev,next'
                        },
                        buttonText: {
                            today: $.i18n.prop('today'),
                            month: $.i18n.prop('month'),
                            week: $.i18n.prop('week'),
                            day: $.i18n.prop('day')
                        },
                        dayNames: dayNames,
                        editable: false,
                        events: resp,
                        eventRender: function(event, element) {
                            var html = '<span class="title">' + bm.htmlEncode(event.title) + '</span> ' +
                                '<span class="start-time">' + $.fullCalendar.formatDate(event.start, timeFormat) + '</span>';
                            element.html(html);
                            _self.calendarWrapper.find(".fc-day").children().css('min-height', '182px');
                        }
                    });
                }
            });
        } else {
            _self.calendarWrapper.html('');
        }
    }
});


