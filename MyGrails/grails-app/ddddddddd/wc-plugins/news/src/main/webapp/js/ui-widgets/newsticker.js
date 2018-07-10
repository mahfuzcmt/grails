(function ($) {
    $.fn.newsticker = function (options) {
        var defaults = {
            delay: 4000,
            autoHideToolbox: true,
            display: 'vertical_scroll' // sv- scroll  vertical, sh- scroll horizontal, refresh- random show, fade- fade out and fade in
        };

        return $(this).each(function () {
            var configuration = $.extend(defaults, options);
            var $this = $(this);
            var display = $this.attr("transition");
            if (display) {
                configuration.display = display;
            }

            var delay = parseInt($this.attr("speed"), 10);
            if (delay) {
                configuration.delay = delay;
            }
            var rand = $this.attr("direction") == "random";
            configuration.rand = rand;
            var height = $this.attr("height");
            if (height != "") {
                try {
                    height = parseInt(height, 10);
                    if (isNaN(height)) {
                        height = 250;
                    }
                } catch (j) {
                    height = 250;
                }
            } else {
                height = 250;
            }
            configuration.height = height + "px";
            if (configuration.display == "vertical_scroll") {
                displayVertical.call($this, configuration);
            } else if (configuration.display == "horizontal_scroll") {
                displayHorizontal.call($this, configuration);
            } else if (configuration.display == "fade") {
                displayFadeOut.call($this, configuration);
            }
        });

        function displayVertical(options) {
            this.css("overflow", "hidden");
            this.children().each(function(i) {
                $(this).height(options.height);
            });

            var isScrollable = false;
            var onMouse = false;
            var elemTotalHeight = 0;
            this.find(".item").each(function (i) {
                elemTotalHeight += $(this).outerHeight(true);
            });
            if (elemTotalHeight > parseInt(options.height, 10))
                isScrollable = true;

            // insert controls
            var controls = $('<ul class="ticker-controls"><li class="item previous"><a href="#previous">Prev</a></li><li class="item next"><a href="#next">Next</a></li></ul>')
                .appendTo(this.parents(".wi-news").find(".header"));
            if (options.autoHideToolbox)
                controls.hide();

            controls.find(".previous").click(function () {
                rotatePrevious();
                return false;
            });
            controls.find(".next").click(function () {
                rotateNext();
                return false;
            });
            this.mouseover(function () {
                onMouse = true;
            });
            this.mouseout(function () {
                onMouse = false;
            });
            var $this = this;

            function beginRotation() {
                $this.data("timer", setInterval(function () {
                    rotateNext();
                }, options.delay));
            }

            function rotateNext() {
                var childHeight = $this.children().filter(":first-child").outerHeight(true);
                var animParams = {scrollTop: childHeight};
                if(!onMouse){
                    $this.animate(animParams, 500, function () {
                        if (options.rand) {
                            var count = $this.children().length;
                            var next = Math.floor(Math.random() * (count - 1));
                            var firstNode = $this.children().eq(next).remove();
                            $this.append(firstNode);
                            $this.scrollTop(0);

                        } else {
                            var firstNode = $this.children().filter(":first-child");
                            $this.children().filter(":first-child").remove();
                            $this.scrollTop(0);
                            $this.append(firstNode);
                        }
                    });
                }
            }

            function rotatePrevious() {
                $this.prepend($this.children().filter(":last-child").hide());
                $this.children().filter(":first-child").slideDown(500);
            }

            if (isScrollable && !onMouse) {
                this.parents(".wi-news").mouseover(function () {
                    clearInterval($this.data("timer"));
                    if (isScrollable && options.autoHideToolbox)
                        controls.show();
                });

                this.parents(".wi-news").mouseout(function () {
                    beginRotation();
                    if (options.autoHideToolbox)
                        controls.hide();
                });

                beginRotation();
            }
        }

        function displayHorizontal(options) {
            this.css("overflow", "hidden");
            this.wrap("<div></div>");

            var isScrollable = this.children().length > 1;
            var onMouse = false;
            var prevRand = -1;
            var $this = this;
            this.find(".item").each(function () {
                $(this).css({"width": $this.width(), "white-space": "normal", display: "inline-block", height: options.height});
            });
            this.css("white-space", "nowrap");

            // insert controls
            var controls = $('<ul class="ticker-controls"><li class="item previous"><a href="#previous">Prev</a></li><li class="item next"><a href="#next">Next</a></li></ul>')
                .appendTo(this.parents(".wi-news").find(".header"));
            controls.hide();

            controls.find(".previous").click(function () {
                rotatePrevious();
                return false;
            });
            controls.find(".next").click(function () {
                rotateNext();
                return false;
            });

            function beginRotation() {
                $this.data("timer", setInterval(function () {
                    rotateNext();
                }, options.delay));
            }

            this.mouseover(function () {
                onMouse = true;
            });
            this.mouseout(function () {
                onMouse = false;
            });
            function rotateNext() {
                var childWidth = $this.children().filter(":first-child").outerWidth(true);
                var animParams = {scrollLeft: childWidth};
                if ( !onMouse ) {
                    $this.animate(animParams, 500, function () {
                        if (options.rand) {
                            var count = $this.children().length;
                            var next = Math.floor(Math.random() * (count - 1));
                            while( next == 0 ){
                                next = Math.floor(Math.random() * (count - 1));
                            }
                            var nextNode = $this.children().eq(next).remove();
                            $this.children().filter(":first-child").after(nextNode);
                            var firstNode = $this.children().filter(":first-child");
                            $this.children().filter(":first-child").remove();
                            $this.scrollLeft(0);
                            $this.append(firstNode);
                        } else {
                            var firstNode = $this.children().filter(":first-child");
                            $this.children().filter(":first-child").remove();
                            $this.scrollLeft(0);
                            $this.append(firstNode);
                        }
                    });
                }
            }

            function rotatePrevious() {
                var childWidth = $this.children().filter(":last-child").outerWidth(true);
                $this.prepend($this.children().filter(":last-child"));
                $this.scrollLeft(childWidth);
                $this.animate({scrollLeft: 0}, 500);
            }

            if (isScrollable && !onMouse) {
                this.parents(".wi-news").mouseover(function () {
                    clearInterval($this.data("timer"));
                    controls.show();
                });

                this.parents(".wi-news").mouseout(function () {
                    beginRotation();
                    controls.hide();
                });

                beginRotation();
            }
        }

        function displayFadeOut(options) {
            this.css("overflow", "hidden");
            this.wrap("<div></div>");

            var isScrollable = this.children().length > 1;
            var onMouse = false;
            this.children().each(function (i) {
                $(this).height(options.height);
            });

            // insert controls
            var controls = $('<ul class="ticker-controls"><li class="item previous"><a href="#previous">Prev</a></li><li class="item next"><a href="#next">Next</a></li></ul>')
                .appendTo(this.parents(".wi-news").find(".header"));
            if (options.autoHideToolbox)
                controls.hide();

            controls.find(".previous").click(function () {
                rotatePrevious();
                return false;
            });
            controls.find(".next").click(function () {
                rotateNext();
                return false;
            });
            this.mouseover(function () {
                onMouse = true;
            });
            this.mouseout(function () {
                onMouse = false;
            });
            var $this = this;

            function beginRotation() {
                $this.data("timer", setInterval(function () {
                    rotateNext();
                }, options.delay));
            }

            function rotateNext() {
                if(!onMouse){
                    if (options.rand) {
                        $this.fadeOut(200);
                        var count = $this.children().length;
                        var next = Math.floor(Math.random() * (count - 1));
                        var firstNode = $this.children().eq(next).remove();
                        $this.append(firstNode);
                        $this.fadeIn(200);
                    } else {
                        $this.fadeOut(200);
                        var firstNode = $this.children().filter(":first-child");
                        $this.children().filter(":first-child").remove();
                        $this.append(firstNode);
                        $this.fadeIn(200);
                    }
                }

            }

            function rotatePrevious() {
                $this.fadeOut(300);
                $this.prepend($this.children().filter(":last-child"));
                $this.fadeIn(300);
            }

            if (isScrollable && !onMouse) {
                this.parents(".wi-news").mouseover(function () {
                    clearInterval($this.data("timer"));
                    if (isScrollable && options.autoHideToolbox)
                        controls.show();
                });

                this.parents(".wi-news").mouseout(function () {
                    beginRotation();
                    if (options.autoHideToolbox)
                        controls.hide();
                });

                beginRotation();
            }
        }

    }
})(jQuery);

$(function() {
    $('.news-list').newsticker();
});