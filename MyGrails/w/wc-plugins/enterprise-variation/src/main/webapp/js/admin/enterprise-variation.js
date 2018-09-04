var EnterpriseVariation = (function (panel) {
    var pId = panel.find(".product-id").val();
    return {
        edit : function(id, combination) {
            var tab = app.Tab.getTab("tab-edit-variation-product-" + id);
            if (!tab) {
                tab = new app.tabs.editEnterpriseVariation({
                    variation: {
                        id: id,
                        pId: pId,
                        combination: combination
                    },
                    id: "tab-edit-variation-product-" + id
                });
                tab.render();
            }
            tab.setActive();
        }
    }
})