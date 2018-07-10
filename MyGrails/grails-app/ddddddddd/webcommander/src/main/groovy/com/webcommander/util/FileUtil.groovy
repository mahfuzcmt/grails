package com.webcommander.util

import groovy.io.FileType
import groovy.io.FileVisitResult
import org.apache.commons.io.FileUtils

import java.nio.file.Files

class FileUtil {
    static boolean isModifiedSince(File file, Long time) {
        file = new File(file.absolutePath) //if path relative than last modified not works
        if(!file.exists()) {
            return false
        }
        if(file.lastModified() > time) {
            return true
        }
        boolean modified = false
        file.traverse type: FileType.DIRECTORIES, { _file ->
            _file = new File(_file.absolutePath) //if path relative than last modified not works
            if (_file.lastModified() > time) {
                modified = true
                return FileVisitResult.TERMINATE
            }
        }
        return modified
    }

    static String firstModifiedSince(File file, Long time) {
        file = new File(file.absolutePath) //if path relative than last modified not works
        if(!file.exists()) {
            return ""
        }
        def fileVictim = { dir ->
            return dir.listFiles().findResult { _file ->
                if (_file.file && _file.lastModified() > time) {
                    return _file.absolutePath
                }
            }
        }
        if(file.lastModified() > time) {
            return fileVictim(file)
        }
        String path = ""
        file.traverse type: FileType.DIRECTORIES, { _file ->
            _file = new File(_file.absolutePath) //if path relative than last modified not works
            if (_file.lastModified() > time) {
                path = fileVictim(_file)
                return FileVisitResult.TERMINATE
            }
        }
        return path
    }

    static boolean move(File source, File dest) {
        if(source.isDirectory()) {
            dest.mkdirs()
        } else if(source.isFile()) {
            return Files.move(source.toPath(), dest.toPath()) != null
        }
        source.traverse { file ->
            move file, new File(dest, file.name)
        }
        dest.delete()
        return true
    }

    static boolean deleteQuietly(File file) {
        if (file == null) {
            return false
        }
        try {
            if (file.isDirectory()) {
                FileUtils.cleanDirectory(file)
            }
        } catch (final Exception ignored) {
        }

        try {
            return file.delete()
        } catch (Exception ignored) {
            return false
        }
    }
}