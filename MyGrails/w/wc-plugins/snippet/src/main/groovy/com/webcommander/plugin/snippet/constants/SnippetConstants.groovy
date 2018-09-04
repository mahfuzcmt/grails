package com.webcommander.plugin.snippet.constants

class SnippetConstants {
    static Map SNIPPET_TEMPLATE_CATEGORY = [
        BENEFITS: 'benefits',
        CONTACT: 'contact',
        CONTENT_BLOCKS: 'content_blocks',
        FEATURE: 'feature',
        HEADERFOOTER: 'headerFooter',
        HEADINGS: 'headings',
        INTROS: 'intros',
        OUR_CLIENTS: 'our_clients',
        SERVICES: 'services',
        SHOWCASE: 'showcase',
        SOCIAL_MEDIA: "social_media",
        TEAM: "team",
        TESTIMONIALS: 'testimonials'
    ];

    static Map SNIPPET_TEMPLATE_CATEGORY_NAMES = [
        (SNIPPET_TEMPLATE_CATEGORY.BENEFITS): 'benefits',
        (SNIPPET_TEMPLATE_CATEGORY.CONTACT): 'contact',
        (SNIPPET_TEMPLATE_CATEGORY.CONTENT_BLOCKS): 'content.blocks',
        (SNIPPET_TEMPLATE_CATEGORY.FEATURE): 'feature',
        (SNIPPET_TEMPLATE_CATEGORY.HEADERFOOTER): 'headerFooter',
        (SNIPPET_TEMPLATE_CATEGORY.HEADINGS): 'headings',
        (SNIPPET_TEMPLATE_CATEGORY.INTROS): 'intros',
        (SNIPPET_TEMPLATE_CATEGORY.OUR_CLIENTS): 'our.clients',
        (SNIPPET_TEMPLATE_CATEGORY.SERVICES): 'services',
        (SNIPPET_TEMPLATE_CATEGORY.SHOWCASE): 'showcase',
        (SNIPPET_TEMPLATE_CATEGORY.SOCIAL_MEDIA): "social.media",
        (SNIPPET_TEMPLATE_CATEGORY.TEAM): 'team',
        (SNIPPET_TEMPLATE_CATEGORY.TESTIMONIALS): 'testimonials'
    ];

    static Map SNIPPET_REPOSITORY_TYPE = [
          STANDARD: "standard",
          LOCAL: "local",
          TEMPLATE: "template",
          ARCHIVE: "archive"
    ];

    static Map SNIPPET_REPOSITORY_NAMES = [
            (SNIPPET_REPOSITORY_TYPE.STANDARD): "standard",
            (SNIPPET_REPOSITORY_TYPE.LOCAL): "local",
    ];
}
