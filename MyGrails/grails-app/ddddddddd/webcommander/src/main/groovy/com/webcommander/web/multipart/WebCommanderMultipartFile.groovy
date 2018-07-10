package com.webcommander.web.multipart

/**
 * Created by sajedur on 10/5/2015.
 */


import org.springframework.util.Assert;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.multipart.MultipartFile;

public class WebCommanderMultipartFile implements MultipartFile {

    private final String name;

    private String originalFilename;

    private String contentType;

    private final byte[] content;

    public WebCommanderMultipartFile(String name, byte[] content) {
        this(name, "", null, content);
    }

    public WebCommanderMultipartFile(String name, InputStream contentStream) throws IOException {
        this(name, name, URLConnection.guessContentTypeFromStream(contentStream), FileCopyUtils.copyToByteArray(contentStream));
    }

    public WebCommanderMultipartFile(String name, String originalFilename, String contentType, byte[] content) {
        Assert.hasLength(name, "Name must not be null");
        this.name = name;
        this.originalFilename = (originalFilename != null ? originalFilename : "");
        this.contentType = contentType;
        this.content = (content != null ? content : new byte[0]);
    }


    public WebCommanderMultipartFile(String name, String originalFilename, String contentType, InputStream contentStream)
            throws IOException {

        this(name, originalFilename, contentType, FileCopyUtils.copyToByteArray(contentStream));
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getOriginalFilename() {
        return this.originalFilename;
    }

    @Override
    public String getContentType() {
        return this.contentType;
    }

    @Override
    public boolean isEmpty() {
        return (this.content.length == 0);
    }

    @Override
    public long getSize() {
        return this.content.length;
    }

    @Override
    public byte[] getBytes() throws IOException {
        return this.content;
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(this.content);
    }

    @Override
    public void transferTo(File dest) throws IOException, IllegalStateException {
        FileCopyUtils.copy(this.content, dest);
    }

}
