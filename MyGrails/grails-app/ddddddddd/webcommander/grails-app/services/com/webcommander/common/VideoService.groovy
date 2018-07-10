package com.webcommander.common

import com.webcommander.converter.FLVConverter
import com.webcommander.events.AppEventManager
import com.webcommander.throwables.ApplicationRuntimeException
import grails.gorm.transactions.Transactional
import org.springframework.web.multipart.MultipartFile

@Transactional
class VideoService {

    def uploadVideo(MultipartFile uploadedVideo, String filePath, String type, String fileName, Long expectedSize) {
        Long size = Math.ceil(uploadedVideo.getSize());
        if ( size > expectedSize) {
            throw new ApplicationRuntimeException("uploaded.file.size.larger.than.expected", [convertToByteNotation(size), convertToByteNotation(expectedSize)])
        }
        String originalFilename = fileName ?: uploadedVideo.originalFilename;

        storeFile(uploadedVideo.inputStream, originalFilename, filePath)
    }

    void storeFile(InputStream uploadedStream, String uploadedName, def entityDirectory) {
        if (!new File(entityDirectory).exists()) {
            new File(entityDirectory).mkdirs()
            def file = new File(entityDirectory)
            file.setWritable(true, true)
        }
        String originalImagePath = entityDirectory + File.separator + uploadedName
        OutputStream out = new FileOutputStream(originalImagePath)
        out << uploadedStream
        out.close()
        uploadedStream.close()
        AppEventManager.fire("custom-resource-file-uploaded", [originalImagePath])
        String thumbDir = entityDirectory + File.separator + "video-thumb"
        File thumbDirFile = new File(thumbDir)
        if(!thumbDirFile.exists()) {
            thumbDirFile.mkdirs()
        }
        String thumbFile = thumbDir + File.separator + uploadedName.substring(0, uploadedName.indexOf(".")) + ".jpg";
        makeThumb(originalImagePath, thumbFile)
    }

    public makeThumb(String inputFile, String thumbFile){
        FLVConverter flvConverter = new FLVConverter("ffmpeg");
        try {
            flvConverter.makeThumb(inputFile, thumbFile, 150, 100, 0, 0, 1)
            AppEventManager.fire("custom-resource-file-uploaded", [thumbFile]);
        }catch (Exception err){
            log.error(err.message, err)
        }
    }

    String convertToByteNotation(Long size) {
        if (size < 1024) {
            return size.toString() + " B";
        }
        size = size / 1024;
        if (size < 1024) {
            return size.toString() + " KB";
        }
        size = size / 1024;
        if (size < 1024) {
            return size.toString() + " MB";
        }
        size = size / 1024;
        return size.toString() + " GB";
    }
}
