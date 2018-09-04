<form class="cloud-storage-config-form create-edit-form" id="cloudStorageConfigForm"  action="${app.relativeBaseUrl()}cloudStorage/saveConfigs">
    <input type="hidden" name="type" value="cloud-storage">
    <div class="form-section">
        <div class="form-section-info">
            <h3><g:message code="aws.s3.config"/></h3>
            <div class="info-content"><g:message code="section.text.aws.s3.config.info"/></div>
        </div>
        <div class="form-section-container">
            <div class="configuration-panel nested-panel">
                <div class="form-row">
                    <input type="checkbox" class="single" name="cloud-storage.aws_is_enabled" uncheck-value="false" value="true" ${configs?.aws_is_enabled ? "checked='checked'" : ""} toggle-target="aws-s3-settings">
                    <span><g:message code="enable.aws.s3"/></span>
                </div>
                <div class="aws-s3-settings nested-content">
                    <div class="aws-resource-panel nested-panel">
                        <div class="form-row">
                            <input type="checkbox" class="single" name="cloud-storage.aws_enable_resource_bucket" uncheck-value="false" value="true" ${configs?.aws_enable_resource_bucket ? "checked='checked'" : ""} toggle-target="on-resource-enable">
                            <span><g:message code="enable.resource.bucket"/></span>
                        </div>
                        <div class="on-resource-enable nested-content">
                            <div class="form-row">
                                <label><g:message code="bucket.name"/></label>
                                <input type="text" class="small" validation="required@if{self::visible}" name="cloud-storage.aws_resource_bucket_name" value="${configs?.aws_resource_bucket_name}">
                            </div>
                            <div class="form-row">
                                <label><g:message code="bucket.access.key"/></label>
                                <input type="text" class="small" validation="required@if{self::visible}" name="cloud-storage.aws_resource_bucket_access_key" value="${configs?.aws_resource_bucket_access_key}">
                            </div>
                            <div class="form-row">
                                <label><g:message code="bucket.secret.key"/></label>
                                <input type="text" class="small" validation="required@if{self::visible}" name="cloud-storage.aws_resource_bucket_secret_key" value="${configs?.aws_resource_bucket_secret_key}">
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</form>