<!doctype html>
<html>
<head>
    <meta name="layout" content="main"/>
    <title>Welcome to Grails</title>
</head>
<body>
    <content tag="nav">
        <li class="dropdown">
            <a href="#" class="dropdown-toggle" data-toggle="dropdown" role="button" aria-haspopup="true" aria-expanded="false">Drop Down Menu <span class="caret"></span></a>
            <ul class="dropdown-menu">
                <li><a href="#">Sub Menu 1</a></li>
                <li><a href="#">Sub Menu 2</a></li>
                <li><a href="#">Sub Menu 3</a></li>
            </ul>
        </li>
    </content>


    <div id="content" role="main">

        <div class="card" style="padding:20px;">
            <div class="card-header">
                <span class="float-right">
                    <div class="btn-group">
                        <g:link action="index" controller="student" class="btn btn-primary">Get Data</g:link>
                    </div>
                </span>
            </div>

            <div class="card-body">
                <div>
                    <section class="row colset-2-its">
                        <br><br>
                        <g:form controller="student" action="save">

                            <label>Name: </label>
                            <g:textField name="name"/><br/>

                            <label>Cell No: </label>
                            <g:textField name="cellNo"/><br/>

                            <label>Address: </label>
                            <g:textField name="address"/><br/>

                            <g:actionSubmit value="Save"/>
                        </g:form>
                    </section>
                </div>
            </div>
        </div>

    </div>
</body>
</html>
