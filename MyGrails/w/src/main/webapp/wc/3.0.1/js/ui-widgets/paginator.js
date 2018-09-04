$.paginator_config = {
    el: undefined,
    onPageClick: undefined,
    next: "",
    prev: "",
    first: "",
    last: ""
};

var Paginator = function (element, config) {
    this.showPages = 7;
    var tag = $(element);
    $.extend(this, $.paginator_config, config);
    var total = parseInt(tag.attr("total"), 10);
    total = total > 0 ? total : 0;
    var itemsPerPage = parseInt(tag.attr("max"), 10);
    var offset = parseInt(tag.attr("offset"), 10);
    offset = offset > 0 ? offset : 0;
    var currentPage = Math.floor(offset / itemsPerPage) + 1;
    var pages = tag.attr("pages");
    if (pages) {
        try {
            this.showPages = parseInt(pages, 10);
        } finally {
        }
    }
    this.el = $("<span></span>");
    this.el.attr("class", tag.attr("class"));
    tag.replaceWith(this.el);
    this.el.addClass(this.className || "pagination"); // for custom design or override css
    this.el.data(tag.data());
    this.el.data("wcui-pagination", this);
    this.update(total, itemsPerPage, currentPage);
};

var _p = Paginator.prototype;

(function () {
    function renderPageRange(strict) {
        this.el.empty();

        var fstElm;
        if (this.first !== false) {
            fstElm = $('<span page="1" class="first ' + (this.currentPage == 1 ? 'disabled' : '') + '"></span>');
            fstElm.text(this.first);
            this.el.append(fstElm);
        }
        var prevElm;
        if (this.prev !== false) {
            prevElm = $('<span page="' + (this.currentPage - 1) + '" class="prev ' + (this.currentPage == 1 ? 'disabled' : '') + '"></span>');
            prevElm.text(this.prev);
            this.el.append(prevElm);
        }

        if (!strict) {
            if (this.currentPage < this.startpage) {
                this.startpage = this.currentPage;
                this.endpage = this.startpage + this.showPages - 1;
            }

            if (this.currentPage > this.endpage) {
                this.endpage = this.currentPage;
                this.startpage = this.endpage - this.showPages + 1;
            }
        }

        if (this.startpage != 1) {
            this.el.append('<span class="prev-more" page="prev-pages">...</span>');
        }

        for (var i = this.startpage; i <= this.endpage; i++) {
            if (i == this.currentPage)
                this.el.append('<span page="' + i + '" class="page-number current">' + i + '</span>');
            else
                this.el.append('<span page="' + i + '" class="page-number">' + i + '</span>');
        }

        if (this.endpage != this.pageCount) {
            this.el.append('<span class="next-more" page="next-pages">...</span>')
        }

        var nxtElm
        if (this.next !== false) {
            nxtElm = $('<span page="' + (this.currentPage + 1) + '" class="next ' + (this.currentPage == this.pageCount ? 'disabled' : '') + '"></span>')
            nxtElm.text(this.next)
            this.el.append(nxtElm)
        }
        var lstElm
        if (this.last !== false) {
            lstElm = $('<span page="' + this.pageCount + '" class="last ' + (this.currentPage == this.pageCount ? 'disabled' : '') + '"></span>')
            lstElm.text(this.last)
            this.el.append(lstElm)
        }

        this.el.find('span').click(this, function (ev) {
            if ($(this).hasClass('current') || $(this).hasClass('disabled')) {
                return
            }
            var paginator = ev.data
            var pageNo = $(this).attr('page')
            if (pageNo == "prev-pages") {
                paginator.startpage = paginator.startpage > paginator.showPages ? paginator.startpage - paginator.showPages : 1
                paginator.endpage = paginator.pageCount < (paginator.showPages + 1) ? paginator.pageCount : (paginator.startpage + (paginator.showPages - 1))
                renderPageRange.call(paginator, true)
                return
            }
            if (pageNo == "next-pages") {
                paginator.startpage = paginator.startpage + (paginator.showPages - 1) < paginator.pageCount ? paginator.startpage + paginator.showPages : paginator.startpage
                paginator.startpage = (paginator.startpage + (paginator.showPages - 1) > paginator.pageCount) ? paginator.pageCount - (paginator.showPages - 1) : paginator.startpage
                paginator.endpage = paginator.pageCount < (paginator.showPages + 1) ? paginator.pageCount : (paginator.startpage + (paginator.showPages - 1))
                renderPageRange.call(paginator, true)
                return
            }
            pageNo = parseInt(pageNo, 10)
            paginator.currentPage = pageNo
            if ($(this).hasClass('page-number')) {
                $(this).siblings('.current').removeClass("current")
                $(this).addClass("current")
                if (nxtElm) {
                    nxtElm[(pageNo == paginator.pageCount ? "add" : "remove") + "Class"]("disabled")
                    nxtElm.attr("page", "" + (paginator.currentPage + 1))
                }
                if (lstElm) {
                    lstElm[(pageNo == paginator.pageCount ? "add" : "remove") + "Class"]("disabled")
                }
                if (prevElm) {
                    prevElm[(pageNo == 1 ? "add" : "remove") + "Class"]("disabled")
                    prevElm.attr("page", "" + (paginator.currentPage - 1))
                }
                if (fstElm) {
                    fstElm[(pageNo == 1 ? "add" : "remove") + "Class"]("disabled")
                }
            } else {
                renderPageRange.call(paginator)
            }
            if (paginator.onPageClick) {
                paginator.onPageClick.call(paginator.el, pageNo)
            }
        })
    }

    _p.update = function (total, itemsPerPage, currentPage) {
        var paginator = this.el
        if (typeof total == "number") {
            this.total = total > 0 ? total : 0
        }
        if (typeof itemsPerPage == "number") {
            this.all = itemsPerPage == -1
            this.itemsPerPage = itemsPerPage > 1 ? itemsPerPage : (itemsPerPage == -1 ? this.total : 1)
        }
        this.pageCount = this.all ? (this.total == 0 ? 0 : 1) : Math.ceil(this.total / this.itemsPerPage)
        if (typeof currentPage == "number") {
            this.currentPage = currentPage > 1 ? (currentPage <= this.pageCount ? currentPage : this.pageCount) : 1
        } else {
            this.currentPage = this.currentPage > 1 ? (this.currentPage <= this.pageCount ? this.currentPage : this.pageCount) : 1
        }
        if (this.pageCount < 2) {
            paginator.empty()
            return
        }
        var middlePage = Math.ceil(this.showPages / 2)
        this.startpage = (currentPage > middlePage && this.pageCount > this.showPages) ? currentPage - (middlePage - 1) : 1
        this.startpage = (this.startpage + (this.showPages - 1) > this.pageCount && this.startpage != 1) ? this.pageCount - (this.showPages - 1) : this.startpage
        this.endpage = this.pageCount < (this.showPages + 1) ? this.pageCount : (this.startpage + (this.showPages - 1))

        renderPageRange.call(this)
    }
})()

_p.getPageCount = function () {
    return this.pageCount
}

_p.getCurrentPage = function () {
    return this.currentPage
}

_p.setCurrentPage = function (currentPage, forceReload) {
    if (typeof currentPage != "undefined") {
        if (currentPage < 1) {
            currentPage = 1
        }
        if (this.currentPage != currentPage || forceReload) {
            this.update(undefined, undefined, currentPage)
            this.onPageClick.call(this.el, this.currentPage)
        }
    }
}

_p.getTotal = function () {
    return this.total
}

_p.setTotal = function (total) {
    if (typeof total != "undefined") {
        this.update(total, undefined, undefined)
    }
}

_p.getItemsPerPage = function () {
    return this.itemsPerPage
}

_p.setItemsPerPage = function (itemsPerPage) {
    if (typeof itemsPerPage != "undefined") {
        this.update(undefined, itemsPerPage, undefined)
    }
}

$.prototype.paginator = function (config) {
    if (!this.length) {
        return this
    }
    return this.each(function () {
        new Paginator(this, config)
    })
}