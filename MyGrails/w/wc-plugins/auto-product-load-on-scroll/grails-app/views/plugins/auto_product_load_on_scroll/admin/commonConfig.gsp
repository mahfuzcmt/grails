<div class='form-row ${toggleTarget}-none'>
    <input type='checkbox' class="single" toggle-target='enable-load-more' name='${configType ? configType + "." : ""}enable_load_more' value='true' uncheck-value='false' ${config["enable_load_more"] == "true" ?
            "checked='true'" : ""}>
    <span>${g.message([code: 'auto.load.scroll'])}</span>
</div>

<div class='form-row enable-load-more mandatory'>
    <label>${g.message([code: 'show.item'])}<span class="suggestion"> e.g. 5</span> </label>
    <input type='text' class='medium' name='${configType? configType + "." : ""}initial_item' validation='skip@if{self::hidden} required number min[5]'
           maxlength="9" restrict="numeric" value='${config["initial_item"] ?: "10"}'>
</div>

<div class='form-row enable-load-more mandatory'>
    <label>${g.message([code: 'item.on.scroll'])}<span class="suggestion"> e.g. 5</span></label>
    <input type='text' class='medium' name='${configType? configType + "." : ""}item_on_scroll'
           maxlength="9" restrict="numeric" validation='skip@if{self::hidden} required number min[5]' value='${config["item_on_scroll"] ?: "5"}'>
</div>