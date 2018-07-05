package mygrails

import grails.converters.JSON
import grails.web.servlet.mvc.GrailsParameterMap
import utils.AppUtil

class StudentController {

   StudentService studentService


    def index() {
        def response = studentService.list(params)
        [student: response.list, total:response.count]
    }


   def save(){
       flash.message = studentService.save(params)
       redirect(controller: "student", action: "index")

   }

    def show(Integer id) {
        def response = studentService.get(id)
        print(response)
        [student: response]
    }


    def edit(Integer id) {
        if (flash.redirectParams) {
            [student: flash.redirectParams]
        } else {
            def response = studentService.get(id)
            if (!response) {
                flash.message = AppUtil.infoMessage("Invalid Data")
                redirect(controller: "student", action: "index")
            } else {
                [student: response]
            }
        }
    }


    def update() {
        def response = studentService.get(params.id)
        if (!response){
            flash.message = AppUtil.infoMessage("Invalid. Data")
            redirect(controller: "student", action: "index")
        } else{
            response = studentService.update(response, params)
            flash.message = AppUtil.infoMessage("Successfully Updated Student Info")
            redirect(controller: "student", action: "index")
        }
    }


    def delete(Integer id) {
        def response = studentService.get(id)
        if (!response){
            flash.message = AppUtil.infoMessage("Invalid Data")
            redirect(controller: "student", action: "index")
        } else{
            response = studentService.delete(response)
            flash.message = AppUtil.infoMessage("Student Info Deleted")
            redirect(controller: "student", action: "index")
        }
    }


    def create() {
        [student: flash.redirectParams]
    }


    def getNumber() {
        [numbers: []]
    }

    def deleteNumber(Integer id){
        render(studentService.deleteCellNumber(id) as JSON)
    }





}
