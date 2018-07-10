/**
 * Created by sajed on 1/13/14.
 */
app.tabs.review = function(configs) {
    this.text = $.i18n.prop("product.review");
    this.tip = $.i18n.prop("manage.review.rating");
    this.ui_class = "product-review";
    this.ajax_url = app.baseUrl + "productReviewAdmin/loadAppView";
    app.tabs.review._super.constructor.apply(this, arguments);
}

app.ribbons.administration.push({
    text: $.i18n.prop("product.review"),
    processor: app.tabs.review,
    ui_class: "product-review",
    license: "allow_review_rating_feature",
    ecommerce: true
})

app.tabs.review.inherit(app.SingleTableTab)

var _r = app.tabs.review.prototype;
var popupElement;
var raty;
_r.menu_entries = [
    {
        text: $.i18n.prop("active"),
        ui_class: "active change",
        action: "active"
    },
    {
        text: $.i18n.prop("inactive"),
        ui_class: "inactive change",
        action: "inactive"
    },
    {
        text: $.i18n.prop('view.review'),
        ui_class: 'view view-review',
        action: 'view-review'
    },
    {
        text: $.i18n.prop("remove"),
        ui_class: "remove"
    }
];

_r.sortable = {
    list: {
        "1": "p.name",
        "2": "created",
        "4": "rating",
        "5": "isActive"
    },
    dir: "up"
};

_r.afterTableReload = function(){
    this.body.find(".rating").raty({
        path: app.systemResourceUrl + "plugins/product-review/images/raty",
        score:  function() {
            return $(this).attr('score');
        },
        readOnly: true
    })
};

_r.onMenuOpen = function(navigator) {
    var menu = this.tabulator.menu;
    var itemList = [
        {
            key: "product_review.change.status",
            class: "change"
        },
        {
            key: "product_review.remove",
            class: "remove"
        }
    ];
    app.checkPermission(menu, itemList);

    if(navigator.is(".active")) {
        menu.find(".menu-item.active").hide();
        menu.find(".menu-item.inactive").not(".disabled").show();
    } else{
        menu.find(".menu-item.active").not(".disabled").show();
        menu.find(".menu-item.inactive").hide();
    }
}

_r.onActionClick = function (action, data) {
    switch (action) {
        case "edit":
            this.editReview(data.id, data.name)
            break;
        case "active":
            this.changeStatus(data.id, data.name, "active")
            break;
        case "inactive":
            this.changeStatus(data.id, data.name, "inactive")
            break;
        case 'view-review':
            this.viewReview(data.id)
            break;
        case "remove":
            this.deleteReview(data.id, data.name)
            break;
    }
};

(function(){
    function attachEvent() {
        var _self = this;
        this.body.find(".rating").raty({
            path: app.systemResourceUrl + "plugins/product-review/images/raty",
            score:  function() {
                return $(this).attr('score');
            },
            readOnly: true
        })
    }

    app.global_event.on("advanced-filter-loaded", function(evt, popup) {
        popup.find(".rating").raty({
            path: app.systemResourceUrl + "plugins/product-review/images/raty",
            half: true,
            cancel: true,
            size: 24
        }).each(function (rateDom) {
            var $this = $(this);
            var name = $this.attr("name");
            if(name) {
                $this.find("input[name=score]").attr("name", name);
            }
        });
    })

    _r.init = function () {
        app.tabs.review._super.init.call(this);
        attachEvent.call(this);
    };

    app.global_event.on('after-ribbon-render', function(e, ribbonBar) {
        if(app.isPermitted("product_review.view.list", {})) {
            ribbonBar.enable("review-rating");
        } else {
            ribbonBar.disable("review-rating");
        }
    });
})();

_r.editReview = function(id) {
    var _self = this;
    bm.editPopup(app.baseUrl + "productReviewAdmin/edit", $.i18n.prop("edit.review"), "", {id: id}, {
        events: {
            content_loaded: function (popup) {
                popupElement = this;
                attachEventForPopup(popupElement);
                popup.preSubmit = function(params) {
                    var x = 100;
                }

            }
        },
        success: function () {
            _self.reload();
        },
        beforeSubmit: function(form, setting, popup){
            var score = raty.raty('score');
            form.find("input[name=score]").val(score)
        }
    });
}
_r.viewReview = function(id){
    var popup = bm.viewPopup(app.baseUrl + "productReviewAdmin/view", {id: id}, {width: 800})
    popup.on("content_loaded", function() {
        var dom = popup.getDom();
        dom.find(".rating").raty({
            half: true,
            path: app.systemResourceUrl + "plugins/product-review/images/raty",
            score:  function() {
                return $(this).attr('score');
            },
            readOnly: true

        })

    })

}
_r.deleteReview = function(id) {
    var _self = this;
    bm.confirm($.i18n.prop("confirm.product.review.delete",[name]), function () {
        bm.ajax({
            url: app.baseUrl + "productReviewAdmin/delete",
            data: {id: id},
            success: function () {
                _self.reload()
            }
        })
    }, function () {
    });

}

_r.changeStatus = function(id, name, status) {
    var _self = this;
    bm.confirm($.i18n.prop("confirm.change.product.review.change.status",[name]), function () {
        bm.ajax({
            url: app.baseUrl + "productReviewAdmin/changeStatus",
            data: {id: id, status: status},
            success: function () {
                _self.reload()
            }
        })
    }, function () {
    });

}
_r.onSelectedActionClick = function(action, selecteds) {
    switch (action) {
        case "remove":
            this.deleteReview(selecteds.collect("id"))
            break;
    }
}

_r.advanceSearchUrl = app.baseUrl + "productReviewAdmin/advanceFilter";
_r.advanceSearchTitle = $.i18n.prop("product.review");

function attachEventForPopup(popup) {
    raty = popup.find(".rating").raty({
        half: true,
        path: app.systemResourceUrl + "plugins/product-review/images/raty",
        score:  function() {
            return $(this).attr('score');
        }
    })
}