package com.webcommander.content

import com.webcommander.admin.TrashService
import com.webcommander.annotations.Initializable
import com.webcommander.common.CommonService
import com.webcommander.common.ImageService
import com.webcommander.constants.DomainConstants
import com.webcommander.events.AppEventManager
import com.webcommander.installation.template.TemplateContent
import com.webcommander.manager.HookManager
import com.webcommander.manager.PathManager
import com.webcommander.tenant.TenantContext
import com.webcommander.throwables.ApplicationRuntimeException
import com.webcommander.util.AppUtil
import com.webcommander.util.PdfUtil
import com.webcommander.util.TrashUtil
import grails.gorm.transactions.Transactional
import org.apache.commons.io.FilenameUtils
import org.springframework.web.multipart.MultipartFile

@Initializable
class AlbumService {
    CommonService commonService
    TrashService trashService
    ImageService imageService

    static {
        AppEventManager.on("album-update", { Long id ->
            Album album = Album.get(id)
            album.isDisposable = false
            album.save();
        });
    }

    static void initialize() {
        AppEventManager.on("before-template-install", {
            List<TemplateContent> contents = TemplateContent.createCriteria().list {
                eq("contentType", DomainConstants.WIDGET_CONTENT_TYPE.ALBUM)
            }
            if(contents) {
                Album.where {
                    inList("id", contents.contentId)
                }.updateAll(isDisposable: true)
                contents*.delete()
            }
        });
    }

    private static Map _DYNAMIC_CONSTANT_HOLDER = [:]

    private static Map getDYNAMIC_CONSTANT() {
        Map dynamic = _DYNAMIC_CONSTANT_HOLDER[TenantContext.currentTenant]
        if(!dynamic) {
            dynamic = _DYNAMIC_CONSTANT_HOLDER[TenantContext.currentTenant] = [:].withDefault {[:]}
        }
        return dynamic
    }

    static addConstant(List list) {
        list.each { config ->
            DYNAMIC_CONSTANT[config.constant].put(config.key, config.value)
        }
    }

    static removeConstant(List list) {
        list.each { config ->
            DYNAMIC_CONSTANT[config.constant].remove(config.key)
        }
    }

    static getDomains() {
        return [
                (DomainConstants.NAVIGATION_ITEM_TYPE.PAGE)        : "pageService",
                (DomainConstants.NAVIGATION_ITEM_TYPE.PRODUCT)     : "productService",
                (DomainConstants.NAVIGATION_ITEM_TYPE.CATEGORY)    : "categoryService",
        ].with {it + getDYNAMIC_CONSTANT().domains}
    }

    private getCriteriaClosureForImage(Map params) {
        Long id = params.id.toLong(0)
        return {
            if (params.searchText) {
                ilike("name", "%${params.searchText.trim().encodeAsLikeText()}%")
            }
            parent {
                eq("id", id)
                eq("isInTrash", false)
            }
            not {
                ilike("name", "%${"thumb".trim().encodeAsLikeText()}%")
            }
            order(params.sort ?: "idx", params.dir ?: "asc")
            order("name")
        }
    }

    AlbumImage getImage(Long id) {
        return AlbumImage.get(id)
    }

    Integer getAlbumImageCount(Map params) {
        return AlbumImage.createCriteria().count {
            and getCriteriaClosureForImage(params)
        }
    }

    List<AlbumImage> getAlbumImagesList(Map params, Map listMap) {
        return AlbumImage.createCriteria().list(listMap) {
            and getCriteriaClosureForImage(params)
        }
    }

    private Closure getCriteriaClosureForTrash(Map params) {
        def session = AppUtil.session;
        Closure closure = {
            if (params.searchText) {
                ilike("name", "%${params.searchText.trim().encodeAsLikeText()}%")
            }
            if (params.deletedFrom) {
                Date date = params.deletedFrom.dayStart.gmt(session.timezone);
                ge("updated", date);
            }
            if (params.deletedTo) {
                Date date = params.deletedTo.dayEnd.gmt(session.timezone);
                le("updated", date);
            }
            eq("isInTrash", true);
        }
        return closure;
    }

    @Transactional
    boolean deleteImage(Long imageId) {
        AlbumImage albumImage = getImage(imageId)
        Long albumId = albumImage.parent.id
        AppEventManager.fire("before-album-image-delete", [albumImage.id])
        List deleteList = [albumImage.name, "thumb-" + albumImage.name, "gallery-" + albumImage.name]
        deleteList.each { image ->
            String path = PathManager.getResourceRoot("albums/album-" + "${albumId}" + "/${image}")
            File file = new File(path)
            if (file.exists()) {
                file.delete()
            }
        }
        albumImage.delete()
        AppEventManager.fire("album-update", [albumId])
        return true
    }

    @Transactional
    int deleteSelectedImage(List<String> ids) {
        int removeCount = 0;
        ids.each { id ->
            try {
                if(deleteImage(id)) {
                    removeCount++;
                }
            } catch(Throwable ignored) {}
        }
        return removeCount
    }

    @Transactional
    boolean renameImage(Long imageId, Long albumId, String name) {
        AlbumImage albumImage = getImage(imageId)
        String path = PathManager.getResourceRoot("albums/album-" + "${albumId}" + "/${albumImage.name}")
        File prevImage = new File(path)
        File newImage = new File(PathManager.getResourceRoot("albums/album-" + "${albumId}" + "/${name}.${FilenameUtils.getExtension(path)}"))
        prevImage.renameTo(newImage)
        path = PathManager.getResourceRoot("albums/album-" + "${albumId}" + "/gallery-${albumImage.name}")
        prevImage = new File(path)
        newImage = new File(PathManager.getResourceRoot("albums/album-" + "${albumId}" + "/gallery-${name}.${FilenameUtils.getExtension(path)}"))
        prevImage.renameTo(newImage)
        path = PathManager.getResourceRoot("albums/album-" + "${albumId}" + "/thumb-${albumImage.name}")
        prevImage = new File(path)
        newImage = new File(PathManager.getResourceRoot("albums/album-" + "${albumId}" + "/thumb-${name}.${FilenameUtils.getExtension(path)}"))
        prevImage.renameTo(newImage)
        albumImage.name = name + "." + FilenameUtils.getExtension(path)
        albumImage.save()
        if (albumImage.hasErrors()) {
            return false
        }
        AppEventManager.fire("album-update", [albumId])
        return true
    }

    @Transactional
    boolean deleteAlbum(Long id) {
        Album album = Album.get(id)
        AppEventManager.fire("before-album-delete", [album.id])
        AlbumImage.where {
            parent == album
        }.list().each {
            it.delete()
        }
        album.delete(flash:true)
        if (!album.hasErrors()) {
            String path = PathManager.getResourceRoot("albums/album-" + "${id}")
            File dir = new File(path)
            dir.deleteDir()
        }
        return !album.hasErrors()
    }

    Album getAlbum(Long id) {
        return Album.get(id)
    }

    @Transactional
    def save(Map params) {
        Long id = params.id.toLong(0)
        if (!commonService.isUnique(Album, [id: id, field: "name", value: params.name])) {
            throw new ApplicationRuntimeException("album.name.exists")
        }
        Album album = id ? Album.proxy(id) : new Album()
        album.name = params.name
        if (!params.id) {
            album.thumbX = params['thumbX'].toInteger()
            album.thumbY = params['thumbY'].toInteger()
        }
        album.description = params.description
        album.save()
        if (album.hasErrors()) {
            return false
        }
        if (id) AppEventManager.fire("album-update", [id])
        return album.id
    }

    void setGalleryThumb(Album album) {
        ImageService.RESIZABLE_IMAGE_SIZES['album-image'].gallery = [album.thumbX, album.thumbY]
    }

    @Transactional
    Map createAlbumFromPdf(Map params) {
        Map result = [status: "error", message: "album.save.failed"]
        MultipartFile pdfFile = params.albumPdf
        if (pdfFile == null || pdfFile.contentType != "application/pdf") {
            result.message = "invalid.file.format"
            return result
        }
        Album album = new Album()
        album.name = pdfFile.originalFilename.substring(0, pdfFile.originalFilename.lastIndexOf("."))
        album.name = commonService.getNameForDomain(album)
        album.save()
        if (album.hasErrors()) {
            return result
        }
        try {
            String albumDirPath = PathManager.getResourceRoot("albums/album-" + album.id)
            String uploadedFilePath = albumDirPath + File.separator + pdfFile.originalFilename
            File albumDir = new File(albumDirPath)
            if (!albumDir.exists()) {
                albumDir.mkdirs()
            }
            InputStream inputStream = pdfFile.inputStream
            OutputStream outputStream = new FileOutputStream(uploadedFilePath)
            outputStream << inputStream
            outputStream.close()
            inputStream.close()
            AppEventManager.fire("custom-resource-file-uploaded", [uploadedFilePath])
            try {
                PdfUtil.pdfToImage(uploadedFilePath,
                        albumDirPath + File.separator + pdfFile.originalFilename.substring(0, pdfFile.originalFilename.lastIndexOf(".")) + "-{#}.png")
                // {#} image counter
                new File(uploadedFilePath).delete()
                albumDir.traverse { img ->
                    try {
                        AlbumImage image = new AlbumImage(name: img.name, parent: album, idx: getIndexForNewImage(album))
                        imageService.createResizedCopies(img.absolutePath, image, ImageService.RESIZABLE_IMAGE_SIZES["album-image"])
                        image.save()
                    } catch (Exception exc) {
                        result.status = "warning"
                    }
                }
            } catch (Exception exc) {
                deleteAlbum(album.id)
                result.message = "pdf.to.image.conversion.failed"
                return result
            }
        } catch (Exception exc) {
            deleteAlbum(album.id)
            result.message = "pdf.upload.failed"
            return result
        }
        if (result.status == "warning") {
            result.message = "few.album.image.or.thumbnail.create.failed"
            return result
        }
        result.status = "success"
        result.message = "album.save.success"
        return result
    }

    @Transactional
    def saveImageProperties(Map params) {
        Long id = params.id.toLong(0)
        AlbumImage albumImage = getImage(id)
        if (!albumImage) {
            return false
        }
        albumImage.altText = params.altText
        albumImage.description = params.description
        albumImage.linkType = params.linkType

        if (params.linkType == "") {
            albumImage.linkTo = null
            albumImage.linkTarget = ""
        } else if (params.linkType == "custom_link") {
            albumImage.linkTo = params.customLink
            albumImage.linkTarget = params.linkTarget
        } else {
            albumImage.linkTo = params.linkTo
            albumImage.linkTarget = params.linkTarget
        }
        albumImage.merge()
        Map response = [success: !albumImage.hasErrors()]
        response = HookManager.hook("after-album-image-property-update", response, params)
        if (response.success) AppEventManager.fire("album-update", [albumImage.parent.id])
        return response.success
    }

    @Transactional
    public boolean putAlbumInTrash(Long id, String at2_reply, String at1_reply) {
        TrashUtil.preProcessPutInTrash("album", id, at2_reply != null, at1_reply != null)
        Album album = Album.get(id);
        return trashService.putObjectInTrash("album", album, at2_reply);
    }

    public Long countAlbumsInTrash() {
        return Album.createCriteria().count {
            eq("isInTrash", true)
        }
    }

    public Map getAlbumsInTrash(int offset, int max, String sort, String dir) {
        return [Album: Album.createCriteria().list(offset: offset, max: max) {
            eq("isInTrash", true)
            order(sort ?: "name", dir ?: "asc")
        }.collect {
            [id: it.id, name: it.name, updated: it.updated]
        }]
    }

    public Long countAlbumsInTrash(Map params) {
        return Album.createCriteria().count {
            and getCriteriaClosureForTrash(params)
        }
    }

    public Map getAlbumsInTrash(Map params) {
        def listMap = [offset: params.offset, max: params.max];
        return [Album: Album.createCriteria().list(listMap) {
            and getCriteriaClosureForTrash(params)
            order(params.sort ?: "name", params.dir ?: "asc")
        }.collect {
            [id: it.id, name: it.name, updated: it.updated]
        }]
    }

    @Transactional
    public boolean restoreAlbumFromTrash(Long id) {
        Album album = Album.get(id);
        return trashService.restoreObjectFromTrash(album)
    }

    @Transactional
    public Long restoreAlbumFromTrash(def field, def value) {
        Album album = Album.createCriteria().get {
            eq(field, value)
        }
        album.isInTrash = false;
        album.merge();
        return album.id;
    }

    public boolean deleteTrashItemAndSaveCurrent(def field, def value) {
        Album album = Album.createCriteria().get {
            eq(field, value)
        }
        deleteAlbum(album.id);
        return !album.hasErrors();
    }

    Closure getCriteriaClosure(Map params) {
        return {
            if (params.searchText) {
                ilike("name", "%${params.searchText.trim().encodeAsLikeText()}%")
            }
            if (params.isDisposable != "true") {
                eq("isDisposable", false)
            } else {
                eq("isDisposable", true)
            }
            eq("isInTrash", false);
        }
    }

    Integer getAlbumCount(Map params) {
        return Album.createCriteria().count {
            and getCriteriaClosure(params)
        }
    }

    List<Album> getAlbums(Map params) {
        return Album.createCriteria().list {
            and getCriteriaClosure(params)
        }
    }

    def getAlbums() {
        return Album.where {
            isInTrash == false;
        }.list().collect {
            return [id: it.id, name: it.name, parent: null, hasChild: false]
        }
    }


    @Transactional
    public void changeOrder(Long id, Integer newOrderValue) {
        AlbumImage image = AlbumImage.get(id);
        image.idx = newOrderValue
        image.merge()
    }

    Integer getIndexForNewImage(Album album) {
        def idx = AlbumImage.createCriteria().list {
            projections {
                max("idx")
            }
            eq("parent", album)
        }
        return idx[0] != null ? idx[0] + 1 : 1
    }
}
