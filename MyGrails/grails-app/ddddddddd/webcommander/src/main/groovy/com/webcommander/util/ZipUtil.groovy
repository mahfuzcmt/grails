package com.webcommander.util

import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipUtil {

    static public void zipFolder(File srcFolder, ZipOutputStream zipOutputStream) throws Exception {
        addFolderToZip("", srcFolder, zipOutputStream);
        zipOutputStream.close();
    }

    static void addStreamToZip(String path, InputStream inputStream, ZipOutputStream zipOutputStream, String fileName) throws Exception {
        zipOutputStream.putNextEntry(new ZipEntry(path + fileName));
        zipOutputStream << inputStream
    }

    private static void addFileToZip(String path, File srcFile, ZipOutputStream zipOutputStream) throws Exception {
        if (srcFile.isDirectory()) {
            addFolderToZip(path + srcFile.getName() + "/", srcFile, zipOutputStream);
        } else {
            FileInputStream fis = new FileInputStream(srcFile);
            addStreamToZip(path, fis, zipOutputStream, srcFile.getName())
        }
    }

    private static void addFolderToZip(String path, File srcFolder, ZipOutputStream zipOutputStream) throws Exception {
        srcFolder.list().each { fileName ->
            File file = new File(srcFolder.absolutePath + File.separator + fileName)
            addFileToZip(path, file, zipOutputStream);
        }
    }

    static void addToZip(String path, File srcFile, ZipOutputStream zipOutputStream) throws Exception  {
        if(srcFile.isDirectory()) {
            addFolderToZip(path, srcFile, zipOutputStream)
        } else {
            addFileToZip(path, srcFile, zipOutputStream)
        }
    }

    static void extract(InputStream inputStream, String basePath) {
        ZipInputStream zipInputStream = new ZipInputStream(inputStream);
        ZipEntry zipEntry =  zipInputStream.nextEntry;
        while (zipEntry) {
            if(!zipEntry.isDirectory()) {
                File outFile = new File(basePath, zipEntry.name);
                if(!outFile.parentFile.exists()) {
                    outFile.parentFile.mkdirs();
                }
                FileOutputStream outputStream = new FileOutputStream(outFile);
                outputStream << zipInputStream;
                zipInputStream.closeEntry();
                outputStream.close()
            }
            zipEntry = zipInputStream.nextEntry;
        }
        zipInputStream.close();
    }
}