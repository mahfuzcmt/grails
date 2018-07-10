<div class="multiple-video-thumbs">
    <g:each var="video" in="${videos}">
        <g:set var="image" value="${video.name.substring(0, video.name.lastIndexOf(".")) + ".jpg"}"/>
        <div class="thumb-video" video-ref="${app.customResourceBaseUrl() + video.urlInfix + video.name}">
        <img data-videoid="${video.id}" class="video-thumbnail" alt="${video.name}"
             src="${app.customResourceBaseUrl() + video.urlInfix + "video-thumb/" + image}"/>
        </div>
    </g:each>
</div>
<div class="player-block">
    <video class="my-video video-js">
    </video>
</div>
