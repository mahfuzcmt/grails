<table style="border-collapse:collapse; width:700px;" border="0" cellspacing="0" cellpadding="0" align="left">
    <g:each in="${customData}" var="data">
        <tr>
            <td style="padding:5px; border:1px #e6e6e6 solid; text-align:center;font-family:Arial, Helvetica, sans-serif; font-size:13px; line-height:22px;">${data.fieldName.encodeAsBMHTML()}</td>
            <td style="padding:5px; border:1px #e6e6e6 solid; text-align:center;font-family:Arial, Helvetica, sans-serif; font-size:13px; line-height:22px;">${data.fieldValue.encodeAsBMHTML()}</td>
        </tr>
    </g:each>
</table>