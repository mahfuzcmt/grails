<meta name="layout" content="main"/>
<div class="card">
    <div class="card-header">
       <h2 class="card-title">Student</h2></h2>
    </div>
    <div class="card-body">
        <g:form controller="student" action="save">
            <g:render template="form"/>
            <div class="form-action-panel">
                <g:submitButton class="btn btn-primary" name="login" value="Save"/>
                <g:link controller="student" action="index" class="btn btn-primary">Cancell</g:link>
            </div>
        </g:form>
    </div>
</div>