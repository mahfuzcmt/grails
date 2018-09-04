$(function() {
    app.initCalendarWidget = function(container, config, eventDataList) {
        var dayKeys = ['sunday', 'monday', 'tuesday', 'wednesday', 'thursday', 'friday', 'saturday'];
        var dayKeysSuffix = config.dayChar == -1 ? '' : (config.dayChar == 1 ? '.single' : '.short');
        var dayNames = [];
        for(var i = 0; i < dayKeys.length; i++) {
            dayNames.push($.i18n.prop(dayKeys[i] + dayKeysSuffix))
        }

        var clickCallback;
        if(config.detailsOn == 'click') {
            clickCallback = function(calEvent, jsEvent, view) {
                var content = eventSummary([calEvent]);
                var event_popup;
                content.find(".close-popup").click(function () {
                    event_popup.close();
                });
                event_popup = content.popup({
                    is_fixed: true,
                    is_always_up: true,
                    auto_close: 40000,
                    clazz: "popup"
                }).obj(POPUP);
                app.initEventDetailsRequest(content.find('.request-info.button'));
            }
        }

        var daysHaveEvent = [];
        $.each(eventDataList, function(idx, event) {
            event.start = event.start.replace(/-/g, '/').replace('T', ' ');
            event.end = event.end.replace(/-/g, '/').replace('T', ' ');
            daysHaveEvent.push($.fullCalendar.formatDate(new Date(event.start), 'yyyy/MM/dd'));
        });
        container.html('');
        container.fullCalendar({
            header: {
                left: 'prev',
                center: 'title',
                right: 'next'
            },
            dayNames: dayNames,
            columnFormat: {
                month: 'dddd'
            },
            editable: false,
            events: eventDataList,
            eventClick: clickCallback,
            viewDisplay: function(view) {
                var eventDays = view.element.find('td.fc-event-day');
                eventDays.first().trigger('click')
            },
            eventRender: function(event, element) {
                var html = '<span class="title">' + bm.htmlEncode(event.title) + '</span> ' + ' ' +
                    '<span class="start-time">' + 'Start time : ' + $.fullCalendar.formatDate(event.start, config.timeFormat) + '</span>' + ' ' +
                    '<span class="end-time">' + 'End time : ' + $.fullCalendar.formatDate(event.end, config.timeFormat) + '</span>' + ' ' +
                    '<span class="ticketPrice">' + 'Ticket price : ' + bm.htmlEncode(event.ticketPrice) + '</span>';
                element.html(html);
                if(!config.isBasicCalendar && config.detailsOn == 'mouseover') {
                    var content = eventSummary([event]);
                    var tip = element.tip({
                        text: content,
                        sustain_on_hover: true,
                        render: function(content) {
                            app.initEventDetailsRequest(content.find('.request-info.button'));
                        }
                    });
                    app.initEventDetailsRequest(content.find('.request-info.button'));
                }
            },
            dayRender: function(date, cell) {
                if(config.isBasicCalendar) {
                    var cellDate = cell.data('date').replace(/-/g, '/').replace('T', ' ');
                    if(daysHaveEvent.contains(cellDate)) {
                        cell.addClass('fc-event-day');
                        renderBasicEvent(date, cell);
                    }
                }
            }
        });

        if(config.isBasicCalendar) {
            container.fullCalendar('removeEvents');
            container.find('.fc-day-content').remove();
        }

        function renderBasicEvent(now, cell) {
            var events = [];
            var afterADay = new Date(new Date(now).setDate(now.getDate() + 1));
            $.each(eventDataList, function(idx, event){
                if(new Date(event.start) >= now && new Date(event.start) < afterADay) {
                    events.push(event)
                }
            });
            var content = eventSummary(events);
            if(config.detailsOn == 'click') {
                var sidebar = container.closest('.calendar-wrap').find('.sidebar');
                cell.click(function() {
                    sidebar.html(content);
                });
            } else if(config.detailsOn == 'mouseover') {
                cell.tip({text: content, sustain_on_hover: true, render: function(content) {
                    app.initEventDetailsRequest(content.find('.request-info.button'));
                }});
            }
            app.initEventDetailsRequest(content.find('.request-info.button'));
        }

        function eventSummary(events) {
            var dom = $('<div class="event-summary-wrap"></div>');
            $.each(events, function(idx, event) {
                var header = $('<div class="header"></div>');
                if(config.detailsOn == 'click') {
                    if(!config.isBasicCalendar) {
                        header.append('<span class="close-popup close-icon"></span>');
                    }
                } else {
                    dom.addClass('tooltip');
                    if(events.length>1) {
                        dom.addClass('scrollable-event-popup');
                    }
                }
                header.append('<span class="title">' + bm.htmlEncode(event.title) +'</span>');

                var body = $('<div class="body"></div>');
                var multiColumn = $('<div class="multi-column"></div>');
                var imgPath = app.baseUrl + 'resources/event/' + (event.image ? 'event-' + event.id + '/images/100-' + event.image : 'default/100-default.png');
                multiColumn.append('<div class="column-left"><span class="img-preview">' +
                    '<img src="' + imgPath + '">' +
                    '</span></div>');
                var rightColumn = $('<div class="column-right"></div>');
                rightColumn.append('<div class="info-row">' +
                    '<label>' + $.i18n.prop('start.time') + ':</label>' + '<span>' + $.fullCalendar.formatDate(new Date(event.start), config.dateTimeFormat) + '</span>' +
                    '</div>');
                if(config.showPrice && event.ticketPrice != '0') {
                    rightColumn.append('<div class="info-row">' +
                        '<label>' + $.i18n.prop('ticket.price') + ':</label>' + '<span>' + event.ticketPrice + '</span>' +
                        '</div>');
                }
                if(event.summary) {
                    rightColumn.append('<div class="summary">' + event.summary + '</div>');
                }
                body.append(multiColumn.append(rightColumn));
                var footer = $('<div class="footer"><div>');
                var buttonLine = $('<span class="button-item"></div>');
                if(config.showRequestInfo > 0 && event.file) {
                    buttonLine.append('<span class="button request-info" event-id="' + event.id + '" event-name="' + event.title + '">' +
                        $.i18n.prop('request.for.details') + '</span> &nbsp; ')
                }
                if(config.showBookNow) {
                    buttonLine.append('<span><a href="' + app.baseUrl + 'event/' + event.id + '" class="button book-now">' + $.i18n.prop("book.now") +
                        '</a></span>');
                }
                if(config.showRequestInfo || config.showBookNow) {
                    footer.append(buttonLine);
                }
                dom.append(header).append(body).append(footer);
            });
            return dom;
        }
    };
});