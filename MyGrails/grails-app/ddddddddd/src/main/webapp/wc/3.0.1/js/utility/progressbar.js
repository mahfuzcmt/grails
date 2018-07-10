var ProgressBar = function(parent, calculateInPercent) {
    this.parent = parent;
    this.maxValue = 100;
    this.position = 0;
    this.calculateInPercent = calculateInPercent;
}

ProgressBar.prototype.render = function() {
    $(this.parent).empty();
    $(this.parent).append("<div class='progress-bar'></div>");
    $(this.parent).find("div").append("<div class='completed'></div>");
}

ProgressBar.prototype.setMax = function(value) {
    this.maxValue = value;
    this.update();
}

ProgressBar.prototype.update = function() {
    var percent = this.calculateInPercent ? this.position : (this.position / this.maxValue) * 100;
    if(percent > 100) {
        percent = 100;
    }
    $(this.parent).find(".completed").css("width", percent + "%");
}

ProgressBar.prototype.setPosition = function(value) {
    this.position = value;
    this.update();
}

ProgressBar.prototype.advance = function(value) {
    this.position = this.calculateInPercent ? value : this.position + value;
    this.update();
}

ProgressBar.prototype.complete = function() {
    this.position = this.maxValue;
    this.update();
}
