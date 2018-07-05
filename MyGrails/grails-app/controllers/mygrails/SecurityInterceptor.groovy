package mygrails

class SecurityInterceptor {


    SecurityInterceptor() {
        matchAll().excludes(controller: "student")
    }


}
