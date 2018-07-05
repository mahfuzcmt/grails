<meta name="layout" content="main"/>
<div class="card">
    <div class="card-header">
        <h2 class="card-title">Student</h2></h2>
    </div>
    <div class="card-body">
        <g:form controller="student" action="update">
            <g:hiddenField name="id" value="${student.id}"/>
            <g:render template="form"/>
            <div class="form-action-panel">
                <g:submitButton class="btn btn-primary" name="login" value="Update"/>
                <g:link controller="student" action="index" class="btn btn-primary">Cancel</g:link>
            </div>
        </g:form>
    </div>
</div>