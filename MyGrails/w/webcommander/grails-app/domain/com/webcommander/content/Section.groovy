package com.webcommander.content

class Section {

    Long id
    String name
    Section parent

    Collection articles = []

    static hasMany = [articles: Article]

    static constraints = {
        name(blank: false, size: 2..100, unique: true)
        articles(nullable: true)
        parent(nullable: true)
    }

    static mapping = {
        articles cache: true
    }

    @Override
    boolean equals(Object o){
        if(!o instanceof Article){
            return false
        }
        if(id && o.id){
            return id == o.id
        }
        if(name){
            return name == o.name
        }
        return super.equals(o);
    }

    @Override
    int hashCode(){
        if (id) {
            return ("Section: " + id).hashCode();
        }
        if (name) {
            return ("Section: " + name).hashCode();
        }
        return super.hashCode()
    }
}
