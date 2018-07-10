package com.webcommander.throwables

/**
 * Created by zobair on 18/12/13.*/
class AttachmentExistanceException extends Exception {
    Map attachmentInfo
    public AttachmentExistanceException(Map attachmentInfo) {
        this.attachmentInfo = attachmentInfo
    }
}