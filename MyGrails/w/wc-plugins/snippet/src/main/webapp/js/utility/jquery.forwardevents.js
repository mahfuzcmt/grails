(function ($) {
    function triggerEvent($elem, eventType, event, relatedTarget) {
        var ev = new $.Event(eventType, {originalEvent: event})
        //log($elem)
        $elem.trigger(ev);
    };

    $.fn.forwardevents = function (settings) {
        var instance = this, doc = settings.document ? settings.document : document;
        return this.each(function () {
            var $this = $(this), lastEventListener;
            $this.on('mouseout', function (e) {
                if (lastEventListener) {
                    triggerEvent(lastEventListener, 'mouseout', e, $this[0]);
                }
            }).on('mousemove', function (e) {
                if ($this.is(':visible')) {
                    var be = e.originalEvent, et = be.type, x = be.clientX, y = be.clientY, eventListener;
                    e.stopPropagation();
                    $this.hide();
                    eventListener = $(doc.elementFromPoint(x, y));
                    $this.show();
                    if (!eventListener) {
                        triggerEvent(lastEventListener, 'mouseout', e);
                        lastEventListener = eventListener;
                        return;
                    }
                    triggerEvent(eventListener, et, e);
                    if (lastEventListener && (eventListener[0] !== lastEventListener[0])) {
                            triggerEvent(lastEventListener, 'mouseout', e, eventListener[0]);
                    }
                    lastEventListener = eventListener;
                }
            });
        });
    }
})(jQuery);
