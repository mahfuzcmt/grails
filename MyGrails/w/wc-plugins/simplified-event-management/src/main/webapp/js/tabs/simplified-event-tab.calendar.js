 bm.onReady(app.tabs, "simplifiedEvent", function() {
    app.tabs.simplifiedEvent.Calendar = function (content, parentTab) {
        this.body = content;
        this.parentTab = parentTab;
    };

    var _ct =  app.tabs.simplifiedEvent.Calendar.prototype;

    _ct.reload = function() {
        this.parentTab.body.find('.bmui-tab').tabify('reload', 'calendar');
    };

    _ct.init = function () {
        var _self = this;
        var namespace = "event-management-calendar";
        app.global_event.on("event-create." + namespace + " event-update." + namespace, function() {
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
            container.find('.selector').html("").hide();
            _self.initCalendar(calendarType);
        });
        _self.initCalendar(calendarType);

        function resetActiveLink(link) {
            $.each(container.find('.calendar-type'), function(idx, val) {
                $(val).removeClass('active')
            });
            $(link).addClass('active')
        }
    };

    _ct.initCalendar = function(calendarType, selectedValue) {
        var _self = this;
        if(selectedValue == undefined || selectedValue != '0') {
            var dateFormat = this.body.find('.dateFormat').val().replaceAll('E', 'd');
            var timeFormat = this.body.find('.timeFormat').val().replace('aaa', 'TT');
            bm.ajax({
                url: app.baseUrl + 'simplifiedEventAdmin/loadAllEventData',
                data: {calendarType: calendarType, selectedValue: selectedValue},
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
                            _self.calendarWrapper.find(".fc-day").children().css('min-height', '182px');
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
                        }
                    });
                }
            });
        } else {
            _self.calendarWrapper.html('');
        }
    }
});


