1. Copy the application.yml from source-root/docs/sample/application.yml to the source-root/grails-app/conf/config/application.yml
(if the config directory not available then please create the config directory under the conf directory).
2. put the below configuration into the application.yml

```yml
dataSource:
    host: localhost
    name: multi_tenant_init_db
    username: root
    password: ""
```

here we named the database multi_tenant_init_db, you may adjust your mysql host, username and password.

3. Create a database named multi_tenant_init_db into your mysql database.
4. now start the WebCommander using idea or command prompt
5. wait until application start successfully, after that stop the application 
6. dump the multi_tenant_init_db database from mysql
7. create a database named wc_multi_tenant_resolver and import the schema located at source-root/docs/dev-documentations/multi-tenant/wc-multi-tenant-resolver-schema.sql
8. we will create 2 tenant a. tenant1.webcommander.local, b. tenant2.webcommander.local
9. add below host entry 
```
127.0.0.1       tenant1.webcommander.local
127.0.0.1       tenant2.webcommander.local
```
10. create a database tenant1_webcommander_local and import the multi_tenant_init_db dump which you created earlier. Select the database tenant1_webcommander_local and run
below sql
```sql
DELETE FROM site_config WHERE type = 'administration' AND config_key = 'baseurl';
INSERT INTO site_config (type, config_key, value) VALUES ('administration', 'baseurl', 'http://tenant1.webcommander.local');
```

11. create another database tenant2_webcommander_local and again import the multi_tenant_init_db dump. Select the database tenant2_webcommander_local and run
below sql
```sql
DELETE FROM site_config WHERE type = 'administration' AND config_key = 'baseurl';
INSERT INTO site_config (type, config_key, value) VALUES ('administration', 'baseurl', 'http://tenant2.webcommander.local');
```

12. add the below sql into the wc_multi_tenant_resolver database
```sql
INSERT INTO `tenant_info` (`name`, `db_name`, `username`, `password`, `host`, `server`, `id`, `active`) VALUES ('tenant1.webcommander.local', 'tenant1_webcommander_local', 'root', '', 'localhost', 'mysql', 'tenant01', b'1');
INSERT INTO `tenant_info` (`name`, `db_name`, `username`, `password`, `host`, `server`, `id`, `active`) VALUES ('tenant2.webcommander.local', 'tenant2_webcommander_local', 'root', '', 'localhost', 'mysql', 'tenant02', b'1');
INSERT INTO `wc_multi_tenant_resolver`.`tenant_alias` (`tenant_name`, `alias`) VALUES ('tenant1.webcommander.local', 'tenant1.webcommander.local');
INSERT INTO `wc_multi_tenant_resolver`.`tenant_alias` (`tenant_name`, `alias`) VALUES ('tenant2.webcommander.local', 'tenant2.webcommander.local');
```
Please adjust database configuration if your mysql host, username and password not localhost, root empty

13. adjust the below configuration into you application.yml located at source-root/grails-app/conf/config/application.yml
Please stop your all of application which has running on 80 port such as xampp or other apache httpd server, because we are going to run the app using 80 port

```
dataSource:
    host: localhost
    name: multi_tenant_init_db
    username: root
    password: ""
server:
  port: "80"
webcommander:
    multiTenant:
        enabled: true
        tenantResolverDataSource:
            server: mysql
            host: localhost
            name: wc_multi_tenant_resolver
            username: root
            password: ""
```

14. say bismillah and start WebCommander, wait until the system start, then brows from browser 
```
Tenant 1 
Site: http://tenant1.webcommander.local
Admin: http://tenant1.webcommander.local/admin
admin@webcommander.com/admin


Tenant 2
Site: http://tenant2.webcommander.local
Admin: http://tenant2.webcommander.local/admin
admin@webcommander.com/admin
``
