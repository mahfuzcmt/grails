package mygrails

class Student {

    Integer id
    String name
    String cellNo
    String address


    static constraints = {
        address(nullable: true, blank: true)
    }
}
