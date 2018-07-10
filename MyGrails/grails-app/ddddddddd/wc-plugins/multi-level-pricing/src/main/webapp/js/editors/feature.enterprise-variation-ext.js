(function(){
    var _ev = app.tabs.editEnterpriseVariation;
    var priceStock = _ev.tabInitFunctions["priceStock"];

    function attachRowEvent(row) {
        row.find(".remove").on("click", function () {
            row.remove()
        })
    }

    _ev.tabInitFunctions["priceStock"]  = function (panel) {
        priceStock.apply(this, arguments);
        var _form = panel.find("form"), pricePanel = panel.find(".multi-level-prices"), template = panel.find(".multi-level-price.template").remove(),
            addPriceBtn = panel.find(".add-price"), validator = _form.data("validator-inst"), nextRowId = pricePanel.find(".row").length + 1;
        addPriceBtn.on("click", function() {
            var row = template.clone();
            row.removeClass("hidden template");
            row.find("select").removeClass("raw");
            row.updateUi();
            row.find("[name]").each(function () {
                var $this = $(this);
                $this.attr("name", "mlp." + nextRowId + "." + $this.attr("name"))
            });
            pricePanel.append(row);
            attachRowEvent(row);
            nextRowId++;
            validator.reInit()
        });
        pricePanel.find(".row").each(function () {
            attachRowEvent($(this))
        })
    };
})();
