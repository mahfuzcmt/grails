$(function(){

    if($("body").attr("onload")) {
        $("body").addClass("edit-mode");
    }
    else {
        $("body").addClass("front-end");
    }

    $(window).scroll(function(){
        var dc = $(document).scrollTop();
        if (dc > 86){
            $('.header > .widget-container').addClass('fixed-menu');
        }
        else {
            $('.header > .widget-container').removeClass('fixed-menu');
        }
    });
/*top up start*/
    $("body").append( $( "<span class='goToTop fa fa-angle-up' style='display: none'></span>" ));
    $(".goToTop").click(function(){
        $('html, body').animate({
            scrollTop: $("body").offset().top
        }, 1000);
        return false;
    });
    $(window).scroll(function(){
        if($(document).scrollTop() == 0){
            $(".goToTop").fadeOut("slow");
        }
        else{
            $(".goToTop").fadeIn("slow");
        }
    });
    /*end top up start*/


    /*start menu iteam */
    var dc = $(document).scrollTop();
    var wh = $(window).height();

    $(".home_menu .nav-wrapper > .navigation-item > a").click(function(e) {
        e.preventDefault();
        var section = $(this).attr("href");
        $("html, body").animate({
            scrollTop: $(section).offset().top
        }, 1500);
    });
    /*end menu iteam*/


});