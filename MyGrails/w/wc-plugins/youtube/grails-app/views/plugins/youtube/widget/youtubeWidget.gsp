<g:applyLayout name="_widget">
    <div class="video">
        <iframe width="${config.width}" height="${config.height}"
                src="https://www.youtube.com/embed/${widget.content}?showinfo=${config.showInfo}&rel=${config.showSuggesion}&autoplay=${config.autoPlay}&controls=${config.showControls}"
                frameborder="0" ${config.allowFullScreen == "yes" ? "allowfullscreen" : ""}>
        </iframe>
    </div>
</g:applyLayout>