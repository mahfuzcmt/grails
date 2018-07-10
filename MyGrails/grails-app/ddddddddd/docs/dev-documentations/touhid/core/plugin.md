### About Plugin

Installation: 
    1. Physical Installation: Need to Put a plugin zip into WEB-INF/install-plugins directory 
    2. Soft Installation: Basically if any plugin physically exist in system but it may not active for
        current tenant, soft installation responsible for active it for the tenant 

Uninstallation
    1. Soft uninstall: the plugin will physically exist in system but remove all tenant specific resource, tables
        will be remove 


### Related Files
Controller 
    1. PluginController
        
Service
    1. ProvisionAPIService : For communicate with Provisioning system
    2. PluginService : basically perform database related operation and communicate with PluginManager
        
Domain
    1. ActivePlugin
    
Classes
    1. PluginManager
    2. ActivePlugin



### Need to Refactoring
1. PluginManager
    a. constant
    b. Hard Uninstall