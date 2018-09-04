package com.webcommander.plugin.myob.properties

import com.webcommander.util.AppUtil
import grails.util.Holders

/**
 * Created by sanjoy on 3/3/14.
 */
class MyOBProperties extends Properties{
    public static final TOKEN_URL = "myob.token.url";
    public static final AUTHORIZATION_URL = "myob.authorization.url";
    public static final DATABASE_URL = "myob.database.url";

    private MyOBProperties(){

    }

    public static FileInputStream getPropertiesInputStream(){
        String propFilePath = Holders.servletContext.getRealPath("/WEB-INF/system-resources/myob-static-resources/myob.properties")
        File propFile = new File(propFilePath)
        return new FileInputStream(propFile)
    }

    public static MyOBProperties getInstance(File propertiesFile = null){
        FileInputStream fileInputStream = propertiesFile ? new FileInputStream(propertiesFile) : propertiesInputStream
        MyOBProperties myOBProperties = new MyOBProperties();
        myOBProperties.load(fileInputStream)
        return myOBProperties;
    }

    @Override
    public synchronized void load(InputStream inputStream){
        super.load(inputStream);
    }

    public String getTokenUrl() {
        return getProperty(TOKEN_URL)
    }

    public String getAuthorizationUrl() {
        return getProperty(AUTHORIZATION_URL)
    }

    public String getDatabaseUrl() {
        return getProperty(DATABASE_URL)
    }
}
