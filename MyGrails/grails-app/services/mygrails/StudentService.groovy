package mygrails

import grails.gorm.transactions.Transactional
import grails.web.servlet.mvc.GrailsParameterMap

@Transactional
class StudentService {

    def save(GrailsParameterMap params) {
        print(params)
        Student std = new Student(params)
        std.save(flush: true)
        return "Successfully Saved Student Info"
    }


    def get(Serializable id) {
        return Student.get(id)
    }


    def list(GrailsParameterMap params) {
        params.max = params.max ?: 10
        List<Student> dataList = Student.createCriteria().list(params) {
            if (params?.colName && params?.colValue) {
                like(params.colName, "%" + params.colValue + "%")
            }
            if (!params.sort) {
                order("id", "desc")
            }
        }
        return [list: dataList, count: Student.count()]
    }


    def update(Student std, GrailsParameterMap params) {
        std.properties = params
        std.save(flush: true)
       return
    }


    def delete(Student std) {
        try {
            std.delete(flush: true)
        } catch (Exception e) {
            println(e.getMessage())
            return false
        }
        return true
    }




    def deleteCellNumber(Serializable id) {
        Student number = Student.get(id)
        if (number) {
            number.delete(flush: true)
            return flash.message = "Cell Number Deleted"
        }
        return flash.message ="Unable to Delete Cell Number"
    }



}
