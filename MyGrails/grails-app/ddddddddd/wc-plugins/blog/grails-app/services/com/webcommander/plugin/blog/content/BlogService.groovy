package com.webcommander.plugin.blog.content

import com.webcommander.ApplicationTagLib
import com.webcommander.admin.*
import com.webcommander.annotations.Initializable
import com.webcommander.common.CommanderMailService
import com.webcommander.common.CommonService
import com.webcommander.common.ImageService
import com.webcommander.common.MetaTag
import com.webcommander.config.StoreDetail
import com.webcommander.constants.DomainConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.content.NavigationItem
import com.webcommander.events.AppEventManager
import com.webcommander.installation.template.TemplateContent
import com.webcommander.manager.HookManager
import com.webcommander.manager.PathManager
import com.webcommander.models.RestrictionPolicy
import com.webcommander.models.blueprints.DisposableUtilServiceModel
import com.webcommander.task.MultiLoggerTask
import com.webcommander.task.TaskService
import com.webcommander.tenant.Thread
import com.webcommander.throwables.ApplicationRuntimeException
import com.webcommander.util.AppUtil
import com.webcommander.util.SortAndSearchUtil
import com.webcommander.util.TrashUtil
import grails.gorm.transactions.Transactional
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.web.multipart.MultipartFile

@Initializable
class BlogService implements DisposableUtilServiceModel {
    TrashService trashService
    ImageService imageService
    CommonService commonService
    CommanderMailService commanderMailService
    TaskService taskService

    @Autowired()
    @Qualifier("com.webcommander.ApplicationTagLib")
    ApplicationTagLib app

    static void initialize() {


        AppEventManager.on("before-blog-post-put-in-trash", { postId ->
            BlogPost blogPost = BlogPost.proxy(postId);
            List<BlogCategory> categories = BlogCategory.createCriteria().list {
                posts {
                    eq("id", postId)
                }
            };
            categories.each {
                blogPost.removeFromCategories(it)
            }
            blogPost.merge();
        });

        AppEventManager.on("before-template-install", {
            List<TemplateContent> contents = TemplateContent.createCriteria().list {
                eq("contentType", DomainConstants.WIDGET_CONTENT_TYPE.BLOG_CATEGORY)
            }
            if(contents) {
                BlogCategory.where {
                    inList("id", contents.contentId)
                }.updateAll(isDisposable: true)
                contents*.delete()
            }
        });

        HookManager.register("blogPost-put-trash-at2-count", { response, id ->
            int comments = BlogPost.proxy(id).comments.size()
            if(comments) {
                response.comments = comments
            }
            return response
        })
        HookManager.register("blogPost-put-trash-at2-list", { response, id ->
            List comments = BlogComment.createCriteria().list {
                projections {
                    property("name")
                }
                post {
                    eq ("id", id)
                }
            }
            if(comments.size()) {
                response.comments = comments
            }
            return response
        })

        AppEventManager.on("before-operator-delete", { id ->
            BlogPost.executeUpdate("update BlogPost p set p.author = null where p.author.id = :uid", [uid: id]);
        });
        AppEventManager.on("before-customer-delete", { id ->
            Customer customer = Customer.proxy(id)
            BlogPost.createCriteria().list {
                customers {
                    eq("id", id)
                }
            }.each {
                it.customers.remove(customer)
                it.merge()
            }
        });
        HookManager.register("blog-category-delete-at1-count") { response, id ->
            int count = BlogPost.createCriteria().count {
                categories {
                    eq("id", id)
                }
            }
            if(count) {
                response.posts = count
            }
            return response;
        }
        HookManager.register("blog-category-delete-at1-list") { response, id ->
            List posts = BlogPost.createCriteria().listDistinct {
                projections {
                    property("name")
                }

                categories {
                    eq("id", id)
                }
            }
            if(posts.size()) {
                response.posts = posts
            }
            return response;
        }
        AppEventManager.on("before-template-install", {
            List<TemplateContent> contents = TemplateContent.createCriteria().list {
                eq("contentType", DomainConstants.WIDGET_CONTENT_TYPE.BLOG_POST)
            }
            if(contents) {
                BlogPost.where {
                    inList("id", contents.contentId)
                }.updateAll(isDisposable: true)
                contents*.delete()
            }
        });
    }

    static {
        HookManager.register("beforeManageUserPermission beforeSaveUserPermission", { Map response, Map params ->
            if(params.type == "blog") {
                Long admin = AppUtil.session.admin
                response.deniedPolicy = new RestrictionPolicy(type: "blog", permission: "edit.permission")
                response.allowed = RoleService.getInstance().isPermitted(admin, response.deniedPolicy, params)
            }
            return response
        })
    }

    private Closure getCriteriaClosureForPost(Map params) {
        def session = AppUtil.session;
        Closure closure = {
            if (params.searchText) {
                ilike("name", "%${params.searchText.trim().encodeAsLikeText()}%")
            }
            if (params.visibility) {
                eq("visibility", params.visibility)
            }
            if(params.createdBy) {
                eq("author.id", params.createdBy.toLong())
            }
            if(params.ids) {
                inList("id", params.list("ids").collect {it.toLong()})
            }
            if (params.category) {
                categories {
                    eq("id", params.category.toLong())
                }
            }
            if (params.dateFrom) {
                Date date = params.dateFrom.dayStart.gmt(session.timezone);
                ge("date", date);
            }
            if (params.dateTo) {
                Date date = params.dateTo.dayEnd.gmt(session.timezone);
                le("date", date);
            }
            if (params.createdFrom) {
                Date date = params.createdFrom.dayStart.gmt(session.timezone);
                ge("created", date);
            }
            if (params.createdTo) {
                Date date = params.createdTo.dayEnd.gmt(session.timezone);
                le("created", date);
            }
            if (params.updatedFrom) {
                Date date = params.updatedFrom.dayStart.gmt(session.timezone);
                ge("updated", date);
            }
            if (params.updatedTo) {
                Date date = params.updatedTo.dayEnd.gmt(session.timezone);
                le("updated", date);
            }
            if(params.isPublished) {
                eq("isPublished" , params.isPublished.toBoolean())
            }
            if(params.isDisposable != "true") {
                eq("isDisposable", false)
            } else {
                eq("isDisposable", true)
            }
            eq("isInTrash", false);
        }
        return closure;
    }

    private Closure getCriteriaClosureForCategory(Map params) {
        def session = AppUtil.session;
        Closure closure = {
            if (params.searchText) {
                ilike("name", "%${params.searchText.trim().encodeAsLikeText()}%")
            }
            if (params.createdFrom) {
                Date date = params.createdFrom.dayStart.gmt(session.timezone);
                ge("created", date);
            }
            if (params.createdTo) {
                Date date = params.createdTo.dayEnd.gmt(session.timezone);
                le("created", date);
            }
            if (params.updatedFrom) {
                Date date = params.updatedFrom.dayStart.gmt(session.timezone);
                ge("updated", date);
            }
            if (params.updatedTo) {
                Date date = params.updatedTo.dayEnd.gmt(session.timezone);
                le("updated", date);
            }
            if(params.isDisposable != "true") {
                eq("isDisposable", false)
            } else {
                eq("isDisposable", true)
            }
        }
        return closure;
    }

    private Closure getCriteriaClosureForComments(Map params) {
        def session = AppUtil.session;
        Closure closure = {
            createAlias("post", "post")
            if (params.name) {
                ilike("name", "%${params.name.trim().encodeAsLikeText()}%")
            }
            if (params.email) {
                ilike("email", "%${params.email.trim().encodeAsLikeText()}%")
            }
            if (params.content) {
                ilike("content", "%${params.content.trim().encodeAsLikeText()}%")
            }
            if (params.post) {
                eq("post.id", params.post.toLong())
            }
            if (params.status){
                eq("status", params.status)
            }
            if (params.isSpam){
                eq("isSpam", params.isSpam == "true" ? true : false)
            }
            if (params.createdFrom) {
                Date date = params.createdFrom.dayStart.gmt(session.timezone);
                ge("created", date);
            }
            if (params.createdTo) {
                Date date = params.createdTo.dayEnd.gmt(session.timezone);
                le("created", date);
            }
            if (params.updatedFrom) {
                Date date = params.updatedFrom.dayStart.gmt(session.timezone);
                ge("updated", date);
            }
            if (params.updatedTo) {
                Date date = params.updatedTo.dayEnd.gmt(session.timezone);
                le("updated", date);
            }
            eq("post.isInTrash", false)
            eq("post.isDisposable", false)
            if (params.searchText) {
                or {
                    ilike("post.name", "%${params.searchText.trim().encodeAsLikeText()}%")
                    ilike("name", "%${params.searchText.trim().encodeAsLikeText()}%")
                    ilike("email", "%${params.searchText.trim().encodeAsLikeText()}%")
                    ilike("content", "%${params.searchText.trim().encodeAsLikeText()}%")
                }
            }
        }
        return closure;
    }

    Integer getPostsCount (Map params) {
        return BlogPost.createCriteria().count {
            and getCriteriaClosureForPost(params)
        }
    }

    List<BlogPost> getPosts (Map params) {
        def listMap = [max: params.max, offset: params.offset];
        return BlogPost.createCriteria().list(listMap) {
            and getCriteriaClosureForPost(params);
            order(params.sort ?: "name", params.dir ?: "asc");
        }
    }

    List<BlogPost> getBlogPostsInOrder(List<Long> ids) {
        Map emptyMap = ids.collectEntries { [(it): null] }
        List<BlogPost> posts = ids ? BlogPost.findAllByIdInList(ids) : []
        posts.each {
            emptyMap[it.id] = it;
        }
        return emptyMap.values() as List;
    }

    Integer getCategoriesCount (Map params) {
        Closure closure = getCriteriaClosureForCategory(params);
        return BlogCategory.createCriteria().get {
            projections {
                and closure;
                rowCount();
            }
        }
    }

    List<BlogCategory> getBlogCategories (Map params) {
        def listMap = [max: params.max, offset: params.offset];
        Closure closure = getCriteriaClosureForCategory(params);
        return BlogCategory.createCriteria().list(listMap) {
            and closure
            order(params.sort ?: "name", params.dir ?: "asc");
        }
    }

    Integer getCommentsCount (Map params) {
        Closure closure = getCriteriaClosureForComments(params);
        return BlogComment.createCriteria().get {
            projections {
                and closure;
                rowCount();
            }
        }
    }

    List<BlogComment> getComments (Map params) {
        def listMap = [max: params.max, offset: params.offset];
        Closure closure = getCriteriaClosureForComments(params);
        return BlogComment.createCriteria().list(listMap) {
            and closure
            order(params.sort ?: "name", params.dir ?: "asc");
        }
    }

    @Transactional
    def saveCategory (Map params, uploadedFile) {
        Long id = params.id.toLong(0)
        BlogCategory blogCategory = id ? BlogCategory.get(id) : new BlogCategory();
        blogCategory.name = params.name.trim();
        blogCategory.url = params.url?.sanitize() ?: commonService.getUrlForDomain(blogCategory);
        if (!commonService.isUnique(blogCategory, "url")) {
            throw new ApplicationRuntimeException("blog.category.url.exists", "alert")
        }
        blogCategory.description = params.description
        blogCategory.isDisposable = false
        updateImage(blogCategory, uploadedFile, params["remove-image"] != null)

        if (id) {
            blogCategory.merge()
        } else {
            blogCategory.save();
        }
        if(!blogCategory.hasErrors()) {
            if(id) AppEventManager.fire("blog-category-update", [id])
            return true
        }
        return false
    }

    @Transactional
    void updateImage(BlogCategory blogCategory, MultipartFile imgFile, Boolean removeOld) {
        if (removeOld) {
            blogCategory.removeResource()
            blogCategory.image = null
        }
        if (imgFile) {
            if(!blogCategory.id) {
                blogCategory.save()
            }
            blogCategory.removeResource()
            blogCategory.image = imgFile.originalFilename
            imageService.uploadImage(imgFile, NamedConstants.IMAGE_RESIZE_TYPE.BLOG_CATEGORY_IMAGE, blogCategory, 2 * 1024 * 1024);
        }
    }

    @Transactional
    def savePost (Map params, def uploadedFile) {
        Long id = params.id.toLong(0)
        BlogPost post = id ? BlogPost.get(id) : new BlogPost();
        post.name = params.name.trim();
        post.url = params.url?.sanitize() ?: commonService.getUrlForDomain(post);
        if (!commonService.isUnique(post, "url")) {
            throw new ApplicationRuntimeException("blog.url.exists", "alert")
        }
        post.content = params.content
        post.visibility = params.visibility
        post.visibleTo = params.visibleTo
        post.isDisposable = false
        post.isPublished = params.isPublished.toBoolean()
        post.date = params.date.dayStart.gmt(AppUtil.session.timezone)
        if(post.visibility != "restricted") {
            post.visibleTo = null
        }
        post.url = (id && !checkBlogPostForConflict(id, post.url)) ? post.url : commonService.getUrlForDomain(post);
        post.metaTags*.delete()
        post.metaTags = [];
        def tag_names = params.list("tag_name");
        def tag_values = params.list("tag_content");
        for (int i = 0; i < tag_names.size(); i++) {
            MetaTag metaTag = new MetaTag(name: tag_names[i], value: tag_values[i]);
            metaTag.save()
            post.metaTags.add(metaTag)
        }
        post.categories.each {
            it.posts.remove(post)
        }
        List<Long> categories = params.list("categories").collect{ it.toLong() }
        post.customers = []
        post.groups = []
        if(post.visibility == "restricted" && post.visibleTo == "selected") {
            if(params.customer) {
                def customerIds = params.list("customer").collect {it.toLong()}
                customerIds.each {  it ->
                    post.addToCustomers(Customer.get(it.toLong()))
                }
            }
            if(params.customerGroup) {
                def groupIds =  params.list("customerGroup").collect {it.toLong()}
                groupIds.each {  it ->
                    post.addToGroups(CustomerGroup.get(it.toLong()))
                }
            }
        }
        if (!id) {
            post.author = Operator.get(AppUtil.getLoggedOperator())
        }
        if(categories) {
            List<BlogCategory> blogCategories = BlogCategory.findAllByIdInList(categories)
            blogCategories.each {
                it.posts.add(post)
                //it.save()
            }
            post.categories = blogCategories
        }
        post.save()
        if(!post.hasErrors()) {
            if (id) {
                AppEventManager.fire("blog-post-update", [id])
            }
            if(params["remove-image"]){
                if (post.id) {
                    File imgDir = new File(PathManager.getResourceRoot("blog-post/post-${post.id}"));
                    if (imgDir.exists()) {
                        imgDir.traverse {
                            it.delete();
                        }
                        imgDir.delete()
                    }
                }
                post.image = null;
            }
            if (uploadedFile?.originalFilename) {
                post.image = uploadedFile.originalFilename;
                try{
                    post.removeResource()
                    imageService.uploadImage(uploadedFile, NamedConstants.IMAGE_RESIZE_TYPE.BLOG_POST_IMAGE, post, 2 * 1024 * 1024)
                }catch (Exception e){
                    throw new ApplicationRuntimeException("image.upload.failure")
                }
                post.save()
            }
        }
        return post.hasErrors() ? null : post;
    }

    @Transactional
    def saveComment (Map params, Map config) {
        BlogPost post = BlogPost.get(params.postId)
        BlogComment blogComment = new BlogComment()
        blogComment.email = params.email
        blogComment.name = params.name.trim()
        blogComment.content = params.content
        blogComment.post = post
        blogComment.isSpam = isCommentSpam(blogComment, config.black_list, config.black_list_no.toLong(0))
        if(config.comment_moderator_approval == "false") {
            blogComment.status = "approved"
        }
        blogComment.save()
        if (!blogComment.hasErrors()) {
            sendNewCommentMail(blogComment)
            return true
        }
        return false
    }

    @Transactional
    def updateComment(Map params) {
        BlogComment blogComment = BlogComment.get(params.id.toLong())
        if(params.status) {
            blogComment.status = params.status
        }
        if(params.isSpam == "true") {
            blogComment.isSpam = true
            blogComment.status = 'spam'
        }
        else if(params.isSpam == "false"){
            blogComment.isSpam = false
            blogComment.status = 'pending'
        }


        blogComment.merge()
        if(!blogComment.hasErrors()) {
            if(params.status == "approved") {
                sendCommentApprovalMail(blogComment)
            }
            return true
        }
        return false
    }

    private Boolean isCommentSpam(BlogComment comment, String blackList, Long blackListNo){
        def spam = false;
        if (!blackList || !blackListNo) {
            return false;
        }
        def count = blackListNo
        def list = blackList.split(",");
        list.each {
            it = it.trim();
            if(comment.content.count(it) >= count) {
                spam = true;
                return;
            }
            if(comment.name.count(it) >= count){
                spam = true;
                return;
            }
            if(comment.email.count(it) >= count){
                spam = true;
                return;
            }
        }
        return spam;

    }

    private Boolean checkBlogPostForConflict(Long id, String url){
        return BlogPost.createCriteria().count{
            if(id != 0){
                ne("id", id)
            }
            eq("url", url)
        } > 0
    }

    @Transactional
    def deleteBlogPost (Long id) {
        BlogPost blogPost = BlogPost.get(id)
        def categoryIds = blogPost.categories.collect{ it.id }

        categoryIds.each {
            BlogCategory blogCategory = BlogCategory.get(it)
            blogPost.removeFromCategories(blogCategory)
        }
        blogPost.delete()
        if (!blogPost.hasErrors()) {
            File resDir = new File(PathManager.getResourceRoot("blog-post/post-${id}"));
            if (resDir.exists()) {
                resDir.deleteDir()
            }
        }
        return !blogPost.hasErrors()
    }

    @Transactional
    def deleteBlogCategory (Long id, String at2_reply, String at1_reply) {
        TrashUtil.preProcessFinalDelete("blog-category", id, at2_reply != null, at1_reply != null)
        BlogCategory blogCategory = BlogCategory.get(id)
        if(at1_reply == "include") {
            blogCategory.posts.each {
                putBlogPostInTrash(it.id, "yes", "include")
            }
        }
        List<BlogPost> posts = BlogPost.createCriteria().list {
            categories {
                eq("id", id)
            }
        }
        posts*.removeFromCategories(blogCategory)
        posts*.merge();
        blogCategory.delete()
        if (!blogCategory.hasErrors()) {
            File resDir = new File(PathManager.getResourceRoot("blog-category/category-${id}"));
            if (resDir.exists()) {
                resDir.deleteDir()
            }
        }
        return !blogCategory.hasErrors()
    }

    @Transactional
    def putBlogPostInTrash(Long id, String at2_reply, String at1_reply) {
        TrashUtil.preProcessPutInTrash("blogPost", id, at2_reply != null, at1_reply != null)
        return trashService.putObjectInTrash("blogPost", BlogPost.proxy(id), at1_reply)
    }

    @Transactional
    def putSelectedBlogPostsInTrash(List<String> ids) {
        boolean deleted = true;
        ids.each { id ->
            deleted = putBlogPostInTrash(id, "yes", "include")
            if(!deleted){
                return false;
            }
        }
        return deleted;
    }

    @Transactional
    def deleteSelectedComments(List<Long> ids) {
        boolean deleted = true;
        ids.each { id ->
            deleted = deleteBlogComment(id)
            if(!deleted){
                return false;
            }
        }
        return deleted;
    }

    @Transactional
    def deleteSelectedCategories(List<Long> ids) {
        boolean deleted = true;
        ids.each { id ->
            deleted = deleteBlogCategory(id, "exclude", "no")
            if(!deleted){
                return false;
            }
        }
        return deleted;
    }

    public Long countBlogPostsInTrash() {
        return BlogPost.createCriteria().count {
            eq("isInTrash", true)
        }
    }

    public Map getBlogPostsInTrash(int offset, int max, String sort, String dir) {
        return [BlogPost: BlogPost.createCriteria().list(offset: offset, max: max) {
            eq("isInTrash", true)
            order(sort?:"name", dir?:"asc")
        }.collect {
            [id: it.id, name: it.name, updated: it.updated]
        }]
    }

    public Map getBlogPostsInTrash(Map params) {
        def listMap = [ offset: params.offset, max: params.max];
        return [BlogPost: BlogPost.createCriteria().list(listMap) {
            and getCriteriaClosureForTrash(params)
            order(params.sort?:"name", params.dir?:"asc")
        }.collect{
            [id: it.id, name: it.name, updated: it.updated]
        }]
    }

    public Long countBlogPostsInTrash(Map params) {
        return BlogPost.createCriteria().count {
            and getCriteriaClosureForTrash(params)
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
    public boolean deleteTrashItemAndSaveCurrent(def field, def value) {
        BlogPost blogPost = BlogPost.createCriteria().get{
            eq(field, value)
        }
        deleteBlogPost(blogPost.id);
        return !blogPost.hasErrors();
    }

    @Transactional
    public boolean restoreBlogPostFromTrash(Long id) {
        BlogPost blogPost = BlogPost.get(id);
        if (!blogPost) {
            return false;
        }
        blogPost.isInTrash = false;
        blogPost.merge();
        return !blogPost.hasErrors();
    }

    @Transactional
    public Long restoreBlogPostFromTrash(String field,String value) {
        BlogPost blogPost = BlogPost.createCriteria().get{
            eq(field, value);
        }
        blogPost.isInTrash = false;
        blogPost.merge();
        return blogPost.id;
    }

    public List<NavigationItem> generateNavigationItemsFromBlogCategories() {
        List<BlogCategory> categories = getBlogCategories([:])
        List<NavigationItem> items = []
        int idx = 0;
        categories.each{ category ->
            NavigationItem navigationItem = new NavigationItem();
            navigationItem.itemType = DomainConstants.NAVIGATION_ITEM_TYPE.BLOG_CATEGORY
            navigationItem.itemRef = category.id
            navigationItem.label = category.name
            navigationItem.target = "_self"
            navigationItem.idx = idx++;
            items.add(navigationItem)
        }
        return items;
    }

    public String getNavigationLinkForBlogCategory(String id) {
        BlogCategory blogCategory = BlogCategory.get(id)
        return blogCategory ? app.relativeBaseUrl() + "blog-category/" + blogCategory.url : "#"
    }

    @Transactional
    public Boolean deleteBlogComment(Long id) {
        BlogComment blogComment = BlogComment.get(id)
        blogComment.delete()
        return !blogComment.hasErrors()
    }

    public def getAvailabilityFilterCriteria(List<Long> ids, Long customerId, Map filterMap) {
        return BlogPost.where {
            if(ids) {
                id in ids
            }
            isInTrash == false
            isPublished == true
            isDisposable == false
            if(filterMap.dateRange) {
                date <= filterMap.dateRange.end
                date >= filterMap.dateRange.start
            }
            if (!AppUtil.request.editMode && !filterMap.forAdmin) {
                if(customerId) {
                    def post = BlogPost
                    (
                            visibility == 'open' || (
                                    visibility != 'hidden' && (
                                            visibleTo == 'all' ||
                                                    exists (BlogPost.where {
                                                        def blog = BlogPost
                                                        eqProperty "blog.id", "post.id"
                                                        customers.id == customerId
                                                    }.id()) ||
                                                    exists (BlogPost.where {
                                                        def blog = BlogPost
                                                        eqProperty "blog.id", "post.id"
                                                        groups.id in CustomerGroup.where {
                                                            customers.id == customerId
                                                        }.id()
                                                    }.id())
                                    )
                            )
                    )
                } else {
                    visibility == 'open'
                }
            }
            if(filterMap.recent) {
                order "date", "desc"
            } else {
                order "name"
            }
        }
    }
    public List filterOutAvailableBlogPost(List<Long> ids, Long customerId, Map filterMap) {
        if(ids != null && !ids.size()) {
            return []
        }
        def query = getAvailabilityFilterCriteria(ids, customerId, filterMap);
        query.alias = "post";
        return query.list([offset: filterMap["offset"] ?: 0, max: filterMap["max"] ?: -1])
    }

    public Integer countAvailableBlogPost(List<Long> ids, Long customerId, Map filterMap) {
        if(ids != null && !ids.size()) {
            return 0
        }
        def query = getAvailabilityFilterCriteria(ids, customerId, filterMap);
        return query.count();
    }

    public List<BlogPost> getPostsInOrder(List<Long> postIds, Map filters = [:]) {
        if(postIds.size() == 0) { return [] }
        List<BlogPost> posts = BlogPost.findAllByIdInListAndIsPublishedAndIsInTrash(postIds, true, false, filters);
        posts = SortAndSearchUtil.sortInCustomOrder(posts, "id", postIds)
        return posts
    }

    public Integer getCountPosts(List<Long> postIds) {
        return BlogPost.countByIdInListAndIsPublishedAndIsInTrash(postIds, true, false)
    }

    public Boolean isActive(String ref) {
        return AppUtil.request.blogCategory == ref.toLong()
    }

    def sendNewCommentMail(BlogComment comment) {
        def storeDetail = StoreDetail.first();
        Map macrosAndTemplate = commanderMailService.getMacrosAndTemplateByIdentifier("new-blog-comment")
        if(!macrosAndTemplate.emailTemplate.active) {
            return;
        }
        Map refinedMacros = macrosAndTemplate.commonMacros
        macrosAndTemplate.macros.each {
            switch (it.key.toString()) {
                case "post_title" :
                    refinedMacros[it.key] = comment.post.name;
                    break;
                case "writer_name" :
                    refinedMacros[it.key] = comment.name;
                    break;
                case "writer_email" :
                    refinedMacros[it.key] = comment.email
                    break;
                case "comment_content" :
                    refinedMacros[it.key] = comment.content.encodeAsBMHTML()
                    break;
                case "comment_status" :
                    refinedMacros[it.key] = comment.status
                    break;
                case "comment_time":
                    Date currentTime = new Date().gmt();
                    refinedMacros[it.key] = currentTime.toEmailFormat();
                    break;
            }
        }
        String recipient = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.BLOG, "moderator_email")
        if(!recipient) {
            recipient = storeDetail.address.email
        }
        Thread.start {
            AppUtil.initialDummyRequest()
            BlogComment.withNewSession {
                commanderMailService.sendMail(macrosAndTemplate.emailTemplate, macrosAndTemplate.activeHtml, macrosAndTemplate.activeText, refinedMacros, recipient)
            }
        }
    }

    def sendCommentApprovalMail(BlogComment comment) {
        if (!comment.email) {
            return
        }
        Map macrosAndTemplate = commanderMailService.getMacrosAndTemplateByIdentifier("blog-comment-approved")
        if(!macrosAndTemplate.emailTemplate.active) {
            return;
        }
        Map refinedMacros = macrosAndTemplate.commonMacros
        macrosAndTemplate.macros.each {
            switch (it.key.toString()) {
                case "post_title" :
                    refinedMacros[it.key] = comment.post.name;
                    break;
                case "writer_name" :
                    refinedMacros[it.key] = comment.name;
                    break;
            }
        }
        String recipient = comment.email
        Thread.start {
            AppUtil.initialDummyRequest()
            BlogComment.withNewSession {
                commanderMailService.sendMail(macrosAndTemplate.emailTemplate, macrosAndTemplate.activeHtml, macrosAndTemplate.activeText, refinedMacros, recipient)
            }
        }
    }

    @Override
    Integer countDisposableItems(String itemType) {
        switch (itemType) {
            case "blogPost":
                return getPostsCount([isDisposable: "true"])
            case "blogCategory":
                return getCategoriesCount([isDisposable: "true"])

        }
        return 0
    }

    @Override
    @Transactional
    void removeDisposableItems(String itemType, MultiLoggerTask task) {
        switch (itemType) {
            case "blogPost":
                this.removeDisposablePosts(task)
                break
            case "blogCategory":
                this.removeDisposableCategories(task)
                break
        }
    }

    void removeDisposablePosts(MultiLoggerTask task) {
        BlogPost.withNewSession {session ->
            List<BlogPost> posts  = this.getPosts([isDisposable: "true"])
            for (BlogPost category : posts) {
                try {
                    this.putBlogPostInTrash(category.id, "yes", "include")
                    session.flush()
                    task.progress = taskService.countProgress(task.totalRecord, ++task.recordComplete)
                    task.taskLogger.success("Blog Post: $category.name", "")
                    task.meta.successCount++
                } catch (Exception e) {
                    task.progress = taskService.countProgress(task.totalRecord, ++task.recordComplete)
                    task.taskLogger.error("Blog Post: $category.name", e.message)
                    task.meta.errorCount++;
                }
            }
        }
    }

    void removeDisposableCategories(MultiLoggerTask task) {
        BlogCategory.withNewSession {session ->
            List<BlogCategory> categories  = this.getBlogCategories([isDisposable: "true"])
            for (BlogCategory category : categories) {
                try {
                    this.deleteBlogCategory(category.id, "yes", "include")
                    session.flush()
                    task.progress = taskService.countProgress(task.totalRecord, ++task.recordComplete)
                    task.taskLogger.success("Blog Category: $category.name", "")
                    task.meta.successCount++
                } catch (Exception e) {
                    task.progress = taskService.countProgress(task.totalRecord, ++task.recordComplete)
                    task.taskLogger.error("Blog Category: $category.name", e.message)
                    task.meta.errorCount++;
                }
            }
        }
    }

    @Transactional
    def changeCommentStatus(List<Long> ids, String status) {
        Integer count = 0;
        ids.each {
            BlogComment blogComment = BlogComment.get(it)
            blogComment.status = status
            count++;
        }
        return count;
    }

    @Transactional
    def changeSpam(List<Long> ids, Boolean spam) {
        Integer count = 0;
        ids.each {
            BlogComment blogComment = BlogComment.get(it)
            if(spam) {
                blogComment.isSpam = spam
                blogComment.status = 'spam'
            }
            else {
                blogComment.isSpam = spam
                blogComment.status = 'pending'
            }
            count++;
        }
        return count;
    }

    @Transactional
    def changePostStatus(List<Long> ids, Boolean status) {
        Integer count = 0;
        ids.each {
            BlogPost blogPost = BlogPost.get(it)
            blogPost.isPublished = status
            count++;
        }
        return count;
    }
}
