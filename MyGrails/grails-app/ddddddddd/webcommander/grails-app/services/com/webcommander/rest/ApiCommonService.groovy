package com.webcommander.rest

import com.webcommander.common.FileService
import com.webcommander.common.MetaTag
import com.webcommander.events.AppEventManager
import com.webcommander.rest.throwable.ApiException
import com.webcommander.webcommerce.Category
import com.webcommander.webcommerce.CategoryService
import com.webcommander.webcommerce.Product
import com.webcommander.webcommerce.ProductService
import grails.core.GrailsDomainClassProperty
import grails.gorm.transactions.Transactional
import grails.util.Holders
import grails.util.TypeConvertingMap
import org.apache.commons.httpclient.HttpStatus
import org.grails.core.DefaultGrailsDomainClass
import org.springframework.web.multipart.MultipartFile

@Transactional
class ApiCommonService {
    ProductService productService
    FileService fileService
    CategoryService categoryService
    private List<String> commonUpdateRestricted = ["updated", "created", "id"]

    private Boolean beforeProductUpdate(Product product, Map fields) {
        Boolean success = true
        if(fields.images) {
            Map images = fields.images;
            TypeConvertingMap params = new TypeConvertingMap();
            params.id = product.id
            params.imageId = images.orderSequence ?: []
            params.altText = []
            params.altTextId = []
            (images.altText ?: [:]).each{key, value->
                params.altTextId.add(key)
                params.altText.add(value)
            }
            params."remove-images" = images.remove ?: []
            success = productService.updateImages(params)
            if(images.add && success) {
                List<MultipartFile> files = []
                images.add.each {
                    files.add(fileService.downloadAsMultipartFile(it))
                }
                success = productService.saveImages(product, files)
            }
            fields.remove("images")
        }

        if (fields.videos && success) {
            Map videos = fields.videos;
            TypeConvertingMap params = new TypeConvertingMap();
            if(videos.remove) {
                params.id = product.id;
                params."remove-videos" = videos.remove
                success = productService.updateVideos(params)
            }
            if(videos.add && success) {
                List<MultipartFile> files = []
                videos.add.each {
                    files.add(fileService.downloadAsMultipartFile(it))
                }
                success = productService.saveVideos(product, files)
            }
            fields.remove("videos")
        }

        if(fields.metaTags && success) {
            Map metaTags = fields.metaTags
            if(metaTags.remove) {
                success = MetaTag.where {
                    id in metaTags.remove.toLong()
                }.deleteAll() > 0;
            }
            if(metaTags.add && success) {
                metaTags.add.each {
                    MetaTag mt = new MetaTag(name: it.name, value: it.value);
                    mt.save()
                    product.metaTags.add(mt);
                }
            }
            fields.remove("metaTags")
        }
        return success

    }

    private Boolean beforeCategoryUpdate(Category category, Map fields) {
        if(fields.containsKey("image")) {
            MultipartFile multipartFile;
            if(fields.image) {
                multipartFile = fileService.downloadAsMultipartFile(fields.image)
            }
            categoryService.updateImage(category, multipartFile, true)
            fields.remove("image")
        }
        return true
    }

    public<Domain> Boolean saveEntity(Domain object, Map params) {
        if(object == null) {
            throw new ApiException("request.object.not.found", HttpStatus.SC_NOT_FOUND)
        }
        DefaultGrailsDomainClass domainClass = Holders.grailsApplication.getDomainClass(object.class.canonicalName);
        String domainName = domainClass.name
        if(this.respondsTo("before${domainName}Update") && !this."before${domainName}Update"(object, params.fields)) {
            return false
        }
//        List updateRestrictedFields = commonUpdateRestricted + (GrailsClassUtils.getStaticPropertyValue(ob.class, "updateRestrictedFields") ?: []);
        params.fields.each { fieldName, field ->
//            if(!field in updateRestrictedFields) {
                GrailsDomainClassProperty classProperty = domainClass.getPersistentProperty(fieldName);
                DefaultGrailsDomainClass refDomainClass = classProperty.referencedDomainClass
                if (refDomainClass && (classProperty.oneToMany || classProperty.manyToMany)) {
                    Class refClass = refDomainClass.clazz
                    List values = []
                    if(!field instanceof List && field) {
                        values.add(field)
                    } else if(field instanceof List || field instanceof Object[]){
                        values = field
                    }
                    List toRemove = new ArrayList(object."${fieldName}" ?: [])
                    toRemove.each {
                        object."removeFrom${fieldName.capitalize()}"(it)
                    }
                    values.each {
                        def refObject = refClass.get(it);
                        object."addTo${fieldName.capitalize()}"(refObject)
                    }
                } else if(refDomainClass) {
                    Class refClass = refDomainClass.clazz
                    object."${fieldName}" = field ? refClass.get(field) : null;
                } else {
                    if (classProperty.type.isAssignableFrom(Double)) {
                        field = field as Double
                    } else if (classProperty.type.isAssignableFrom(Integer)) {
                        field = field as Integer
                    } else if (classProperty.type.isAssignableFrom(Date) && !(field instanceof Date)) {
                        field = field.toDate()
                    }
                    object."${fieldName}" = field
                }
//            }
        }
        if(this.respondsTo("after${domainName}Update") && !this."after${domainName}Update"(object, params.fields)) {
            return false
        }
        if(object.validate()) {
            object.save()
            if(!object.hasErrors()) {
                AppEventManager.fire("${domainName.toLowerCase()}-update", [object.id])
                return true
            }
            return false
        }
        object.discard();
        StringBuilder errorMessage = new StringBuilder()
        object.errors.allErrors.each {
            errorMessage.append("Error on '${it.field}' field, reject value: [${it.rejectedValue}].\r\n")
        }
        throw new ApiException(errorMessage.toString())
    }

    private Boolean afterProductUpdate(Product product, Map fields) {
        if(product.parent && !product.parents.find { it == product.parent}) {
            product.addToParents(Category.get(product.parent.id))
        }
        return true
    }
}
