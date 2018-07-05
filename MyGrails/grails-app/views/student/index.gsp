<meta name="layout" content="main"/>

%{--<div style="padding:10px">--}%
    %{--<g:if test="${flash.error}">--}%
        %{--<div class="alert alert-error fade in">--}%
            %{--<a href="#" class="close" data-dismiss="alert" aria-label="close">&times;</a>--}%
            %{--${flash.error}</div>--}%
    %{--</g:if>--}%
    %{--<g:if test="${flash.message}">--}%
        %{--<div class="alert alert-info fade in">--}%
            %{--${flash.message}--}%
            %{--<a href="#" class="close" data-dismiss="alert" aria-label="close">&times;</a>--}%
        %{--</div>--}%
    %{--</g:if>--}%

%{--</div>--}%


<div class="card">
    <div class="card-header">
        <h2 class="card-title">List of Students</h2></h2>
        <span class="float-right">


            <div class="btn-group">
                <g:link controller="student" action="create" class="btn btn-success">New</g:link>

            </div>
        </span>
    </div>
    <div class="card-body">
        <table class="table table-stripedd">
            <thead>
            <tr>
                <g:sortableColumn property="name" title="Name"/>
                <g:sortableColumn property="cellNo" title="Cell No"/>
                <g:sortableColumn property="address" title="Address"/>
                <th class="action-row">Action </th>
            </tr>
            </thead>
            <tbody>
                <g:each in="${student}" var="info">
                    <tr>
                        <td>${info.name}</td>
                        <td>${info.cellNo}</td>
                        <td>${info.address}</td>
                        <td>
                            <g:link controller="student" action="edit" class="btn btn-secondary" id="${info.id}">Edit</g:link>
                            <g:link controller="student" action="delete" id="${info.id}" class="btn btn-secondary delete-confirmation">Delete</g:link>
                        </td>
                    </tr>
                </g:each>
            </tbody>
        </table>
        <div class="paginate">
            <g:paginate total="${total ?: 0}" />
        </div>
    </div>
</div>