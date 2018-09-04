package com.webcommander.controllers.admin.content

import com.webcommander.common.CommonService
import com.webcommander.constants.DomainConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.content.Album
import com.webcommander.content.AlbumImage
import com.webcommander.content.AlbumService
import com.webcommander.events.AppEventManager
import com.webcommander.manager.PathManager
import com.webcommander.models.MockStaticResource
import com.webcommander.tenant.TenantContext
import com.webcommander.throwables.AttachmentExistanceException
import com.webcommander.util.Base64DataInputStream
import com.webcommander.util.StringUtil
import com.webcommander.webcommerce.CategoryService
import com.webcommander.webcommerce.ProductService
import grails.converters.JSON
import com.webcommander.common.ImageService
import grails.util.Holders
import org.apache.commons.io.FilenameUtils
import org.springframework.web.multipart.MultipartFile

class AlbumController {

    AlbumService albumService
    ImageService imageService
    CommonService commonService
    ProductService productService
    CategoryService categoryService

    def loadAppView() {
        def count = albumService.getAlbumCount([:])
        render(view: "/admin/album/explorerPanel", model: [count: count]);
    }

    def leftPanel() {
        Album album = Album.findByIsInTrashAndIsDisposable(false, false)
        Long selected = params.selected ? params.selected.toLong() : (album ? album.id : 0)
        def count = albumService.getAlbumCount(params)
        List albums = albumService.getAlbums(params);
        render(view: "/admin/album/leftPanel", model: [count: count, albumList: albums, selected: selected])
    }

    def explorerView() {
        params.max = params.max?: "10";
        params.offset = params.offset?: "0"

        Integer albumImageCount = albumService.getAlbumImageCount(params)

        List<AlbumImage> albumImages = commonService.withOffset(params.max, params.offset, albumImageCount) { max, offset, _albumImageCount ->
                params.offset = offset
                params.max = max
                return albumService.getAlbumImagesList(params, [offset: offset, max: max])
        }
        render(view: "/admin/album/explorerView",model: [
                        albumImages : albumImages,
                        albumId: params.id.toLong(0),
                        albumImageCount: albumImageCount
                ])
    }

    def listView() {
        Album album = Album.findByIsInTrashAndIsDisposable(false, false)
        params.id = params.id ?:  album ? album.id + "": "0"
        params.max = params.max ?: "10";
        Integer count = albumService.getAlbumImageCount(params)
        List<AlbumImage> albumImages = commonService.withOffset(params.max, params.offset, count) { max, offset, _albumImageCount ->
            params.offset = offset
            params.max = max
            return albumService.getAlbumImagesList(params, [offset: offset, max: max])
        }
        render(view: "/admin/album/listView", model: [albumImages: albumImages, count: count]);
    }

    def changeImageOrder() {
        long id = params.long("id");
        int value = params.int("value");
        albumService.changeOrder(id, value)
        render([status: "success"] as JSON)
    }

    def updateImageContent() {
        String base64data = params.image;
        try {
            String relativeLocation = params.url
            String location = appResource.getRootPhysicalPath(extension: relativeLocation)
            final InputStream stream = new Base64DataInputStream(new ByteArrayInputStream(base64data.getBytes()));
            File file = new File(location + ".png");
            file.createNewFile();
            file.withDataOutputStream { _out ->
                _out << stream
            }
            stream.close()
            imageService.reformat(location + ".png", location)
            String resourceName = file.name.substring(0, file.name.length() - 4)
            file.delete()
            MockStaticResource resource = new MockStaticResource(relativeUrl: relativeLocation, resourceName: resourceName)
            imageService.createResizedCopies(location, resource ,ImageService.RESIZABLE_IMAGE_SIZES."album-image")
        } catch(Throwable t) {
            render([status: "error", message: g.message(code: "image.save.error")] as JSON)
            return;
        }
        render([status: "success", message: g.message(code: "image.save.successful")] as JSON)
    }

    def create() {
        render(view: "/admin/album/infoEdit", model: [album: new Album()])
    }

    def save() {
        params.remove("controller")
        params.remove("action")
        if(params.deleteTrashItem) {
            def field = params.deleteTrashItem.collect{it.key}[0];
            def value = params[field];
            albumService.deleteTrashItemAndSaveCurrent(field, value)
        }
        def id = albumService.save(params)
        if (id) {
            render([status: "success", message: g.message(code: "album.save.success"), id: id] as JSON)
        } else {
            render([status: "error", message: g.message(code: "album.save.failed")] as JSON)
        }
    }

    def editAlbum() {
        Long id = params.long("id")
        Album album = albumService.getAlbum(id)
        render(view: "/admin/album/infoEdit",model: [album: album])
    }

    def importPdf() {
        render(view: "/admin/album/importPdf")
    }

    def savePdf() {
        Map result = albumService.createAlbumFromPdf(params)
        render([status: result.status, message: g.message(code: result.message)] as JSON)
    }

    def deleteImage() {
        Long imageId = params.long("id")
        AlbumImage albumImage = AlbumImage.get(imageId)
        boolean deleted = albumService.deleteImage(imageId)
        if(deleted) {
            render([status: "success", message: g.message(code: "image.delete.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "image.delete.failure")] as JSON)
        }
    }

    def deleteSelectedImage() {
        List<Long> ids = []
        params.list("ids").each {
            ids.add(it.toLong(0))
        }
        int deleteCount = albumService.deleteSelectedImage(ids);
        int total = ids.size();
        if (deleteCount == total) {
            render([status: "success", message: g.message(code: "selected.images.delete.success")] as JSON)
        } else if(deleteCount == 0) {
            render([status: "error", message: g.message(code: "selected.images.could.not.delete")] as JSON)
        } else {
            render([status: "warning", message: g.message(code: "selected.not.deleted", args: [total - deleteCount, total, g.message(code: "image")])] as JSON)
        }
    }

    def imageProperties() {
        AlbumImage albumImage = albumService.getImage(params.id.toLong(0))
        render(view: "/admin/album/imageProperties", model: [albumImage: albumImage])
    }

    def renameImage() {
        String name = FilenameUtils.getBaseName(params.name)
        render(view: "/admin/album/renameImage", model: [id: params.id, name: name])
    }

    def saveImageName() {
        Long imageId = params.long("id")
        String name = params.name
        AlbumImage albumImage = AlbumImage.get(imageId)
        Long albumId = albumImage.parent.id
        boolean isRenamed = albumService.renameImage(imageId, albumId, name)
        if(isRenamed) {
            render([status: "success", message: g.message(code: "image.rename.success")] as JSON)
        } else {
            render([status: "error", message: g.message(code: "image.rename.failure")] as JSON)
        }
    }

    public def loadReferenceSelectorBasedOnType() {
        String type = params.linkType
        def items = []
        if(!["", DomainConstants.NAVIGATION_ITEM_TYPE.EMAIL, DomainConstants.NAVIGATION_ITEM_TYPE.URL, DomainConstants.NAVIGATION_ITEM_TYPE.AUTO_GENERATED_PAGE].contains(type)) {
            def consumer = Holders.applicationContext.getBean(albumService.domains[type])
            String domain = StringUtil.getCapitalizedAndPluralName(type)
            items = consumer.getClass().getDeclaredMethod("get" + domain, Map as Class[]).invoke(consumer, [[:]] as Object[])
        }
        render (view: "/admin/album/referenceSelector", model: [items: items, type: type, linkTo: params.linkTo])
    }

    def saveImageProperties() {
        if(albumService.saveImageProperties(params)){
            render([status: "success", message: g.message(code: "image.save.successful")] as JSON)
        } else {
            render([status: "error",message: g.message(code: "image.save.error")]as JSON)
        }
    }

    def delete() {
        try {
            if(albumService.putAlbumInTrash(params.long("id"), params.at2_reply, params.at1_reply)) {
                render([status: "success", message: g.message(code: "album.delete.successful")] as JSON)
            } else {
                render([status: "error", message: g.message(code: "album.delete.failure")])
            }
        } catch(AttachmentExistanceException att) {
            render([status: "error", error_code: "attachment.exists", attachments: att.attachmentInfo] as JSON)
        }
    }

    def uploadImages() {
        long id = params.long("id")
        render (view: "/admin/album/uploadImages", model: [albumId: id])
    }

    def updateImages() {
        Long id = params.long("id")
        Album album = Album.get(id)
        def path = PathManager.getResourceRoot("albums/album-" + params.id)
        List<MultipartFile> uploadedImages = request.getMultiFileMap().albumImage
        List<String> emptyFiles = new ArrayList<String>()
        List<String> notImageFiles = new ArrayList<String>()
        if (uploadedImages != null) {
            uploadedImages.each { image ->
                String baseName = image.originalFilename;
                String mimeType = URLConnection.guessContentTypeFromName(baseName) ?: "";
                if(image.isEmpty()) {
                    emptyFiles.add(baseName)
                } else if(!mimeType.startsWith("image")) {
                    notImageFiles.add(baseName)
                } else {
                    int extIndex = baseName.lastIndexOf('.');
                    String ext;
                    if(extIndex > -1) {
                        ext = baseName.substring(extIndex + 1);
                        baseName = baseName.substring(0, extIndex)
                    }
                    String attemptName = baseName;
                    int attemptCount = 1;
                    while(AlbumImage.findByNameAndParent(attemptName + (ext ? "." + ext : ""), album) != null) {
                        attemptName = baseName + "-" + attemptCount++
                    }
                    String newName = attemptName + "." + ext
                    albumService.setGalleryThumb(album)
                    AlbumImage albumImage = new AlbumImage(name: newName, parent: album, idx: albumService.getIndexForNewImage(album))
                    imageService.uploadImage(image, NamedConstants.IMAGE_RESIZE_TYPE.ALBUM_IMAGE, albumImage)
                    albumImage.save()
                }
            }
            if(emptyFiles.size() > 0 || notImageFiles.size() > 0) {
                String jsonMsg = g.message(code: "few.errors.save.image")
                if(emptyFiles.size() > 0) {
                    jsonMsg += "<br>Empty Files : ${emptyFiles[0]}"
                    for(int i = 1; i < emptyFiles.size(); i++) {
                        jsonMsg = jsonMsg + ", " + emptyFiles[i]
                    }
                }
                if(notImageFiles.size() > 0) {
                    jsonMsg += "<br>Not Image Files : ${notImageFiles[0]}"
                    for(int i = 1; i < notImageFiles.size(); i++) {
                        jsonMsg = jsonMsg + ", " + notImageFiles[i]
                    }
                }
                render([status: "error", message: jsonMsg] as JSON)
            }
        }
        AppEventManager.fire("album-update", [album.id])
        render([status: "success", message: g.message(code: "image.save.successful")] as JSON)
    }

    def isUnique() {
        render(commonService.responseForUniqueField(Album, params.long("id"), params.field, params.value) as JSON)
    }

    def restoreFromTrash() {
        String field = params.field;
        String value = params.value;
        Long id = albumService.restoreAlbumFromTrash(field,value);
        if(id) {
            render([status: "success", message: g.message(code: "restored.successfully", args: ["Album"]), type: "album", id: id] as JSON)
        }
    }

    def advanceFilter() {
        render(view: "/admin/album/filter")
    }


}


