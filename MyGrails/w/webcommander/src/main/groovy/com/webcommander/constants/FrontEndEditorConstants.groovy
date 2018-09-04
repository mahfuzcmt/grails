package com.webcommander.constants

class FrontEndEditorConstants {
    static widgetContentType = [
            COLUMN: 'column',
            WIDGET: 'widget'
    ]
    static widgets = [
            twoColumn  : [label: "2 Column", inlineEdit: false, type: widgetContentType.COLUMN, inPopUpTab: false],
            threeColumn: [label: "3 Column", inlineEdit: false, type: widgetContentType.COLUMN, inPopUpTab: false],
            spacer     : [label: "Spacer", inlineEdit: true, type: widgetContentType.WIDGET, popupTitle: 'widget.spacer', inPopUpTab: false],
            html       : [label: "Content", inlineEdit: true, type: widgetContentType.WIDGET, popupTitle: 'widget.html', inPopUpTab: true],
            article    : [label: "Article", inlineEdit: false, type: widgetContentType.WIDGET, popupTitle: 'widget.article', inPopUpTab: false],
            image      : [label: "Image", inlineEdit: false, type: widgetContentType.WIDGET, popupTitle: 'widget.image', inPopUpTab: true],
            navigation : [label: "Navigation", inlineEdit: false, popup: true, type: widgetContentType.WIDGET, popupTitle: 'widget.navigation', inPopUpTab: true],
            product    : [label: "Product", inlineEdit: false, type: widgetContentType.WIDGET, popupTitle: 'widget.product', inPopUpTab: true],
            category   : [label: "Category", inlineEdit: false, type: widgetContentType.WIDGET, popupTitle: 'widget.category', inPopUpTab: true],
            gallery    : [label: "Gallery", inlineEdit: false, type: widgetContentType.WIDGET, popupTitle: 'widget.gallery', inPopUpTab: true]
    ]
}
