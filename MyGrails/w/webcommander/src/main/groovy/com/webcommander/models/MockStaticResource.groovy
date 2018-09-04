package com.webcommander.models;

import com.webcommander.models.blueprints.AbstractStaticResource;

class MockStaticResource extends AbstractStaticResource {
    String resourceName
    String baseUrl
    String relativeUrl

    boolean uploadToCloud
}
