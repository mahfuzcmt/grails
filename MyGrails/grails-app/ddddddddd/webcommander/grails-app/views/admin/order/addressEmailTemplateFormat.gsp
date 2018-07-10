<g:if test="${address}">
    ${address.firstName.encodeAsBMHTML() + " " + (address.lastName ? address.lastName.encodeAsBMHTML() : "")}<br />
    ${address.addressLine1.encodeAsBMHTML()}<br />
    ${address.city ? address.city + ", ": ""}${address.state ? address.state.name.encodeAsBMHTML() + ", ": ""}${address.postCode ?: ""}<br />
    ${address.country.name.encodeAsBMHTML()}<br />
    Email: <a style="font-family:Arial, Helvetica, sans-serif; font-size:13px; line-height: 20px; margin-bottom:0;" href="${address.email}">${address.email}</a>
</g:if>
