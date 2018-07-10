Variables
    1. tenantId = (default 00000000)


1. Technical Aspect
    a. Check Domain/Database Table add and remove
        a. Check create and remove schema and their order, schema path: source\wc-plugins\{plugin-name}\src\main\webapp\WEB-INF\sql
    b. Check site config
        a. SELECT * FROM site_config WHERE type = {plugin-name}
    c. Check modifiable resources add remove
        a. Path source\src\main\webapp\WEB-INF\modifiable-resources\{tenantId}
    d. Check Resources
        a. clue: check *ResourceTagLib exist or not
        b. Path source\src\main\webapp\resources\{tenantId}\*
    e. Check cache 
        a. Create some CRUD, then uninstall the plugin and then reinstall then check previous are exist or not



Plugin Migration 
---------------------    
1. Check constant Name and domain
2. Check Resource

TenantContext.eachParallelWithWait(tenantInit)

private static final PLUGIN_UNDERSCORE_NAME = "abandoned_cart"

List domain_constants = [
        [constant: "", key: "", value: ""]
]

List named_constants = [
        [constant: "", key: "", value: ""]
]

List license_constants = [
        [constant: "", key: "", value: ""]
]

List gallery_constants = [
        [constant: "", key: "", value: ""]
]

DomainConstants.addConstant(domain_constants)
NamedConstants.addConstant(named_constants)
LicenseConstants.addConstant(license_constants)
Galleries.addConstant(gallery_constants)

DomainConstants.removeConstant(domain_constants)
NamedConstants.removeConstant(named_constants)
LicenseConstants.removeConstant(license_constants)
Galleries.removeConstant(gallery_constants)


BlogResourceTagLib.RESOURCES_PATH.each { resource ->
    util.deleteResourceFolders(resource.value)
}

public static final RESOURCES_PATH = [
     "" : "",
]