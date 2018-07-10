<g:applyLayout name="_productwidget">
    <form class="custom-field-container">
        <h4 class="title"><g:message code="custom.fields"/> </h4>
        <div class="form-row  ">
            <label>Field 2:</label>
            <input type="text" name="custom.Field 1" value="" placeholder="" validation="email" >
        </div>
        <div class="form-row  ">
            <label>Field 2:</label>
            <select  name="custom.Field 2">
                <option value="Option1">Option1</option>
                <option value="Option2">Option2</option>
                <option value="Option3">Option3</option>
            </select>
        </div>
        <div class="form-row  ">
            <label>Field 3:</label>
            <span class="radio-group">
                <input type="radio" name="custom.Field 3" value="Radio1"> &nbsp; &nbsp; Radio1
            &nbsp; &nbsp; &nbsp; &nbsp;
                <input type="radio" name="custom.Field 3" value="Radio2"> &nbsp; &nbsp; Radio2
            &nbsp; &nbsp; &nbsp; &nbsp;
                <input type="radio" name="custom.Field 3" value="Radio3"> &nbsp; &nbsp; Radio3
            &nbsp; &nbsp; &nbsp; &nbsp;
            </span>
        </div>
    </form>
</g:applyLayout>
