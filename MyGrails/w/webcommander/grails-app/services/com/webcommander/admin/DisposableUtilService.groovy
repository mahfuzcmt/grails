package com.webcommander.admin

import com.webcommander.ApplicationTagLib
import com.webcommander.Page
import com.webcommander.constants.DomainConstants
import com.webcommander.content.*
import com.webcommander.design.Layout
import com.webcommander.design.LayoutService
import com.webcommander.events.AppEventManager
import com.webcommander.models.blueprints.DisposableUtilServiceModel as DisposableUtil
import com.webcommander.task.MultiLoggerTask
import com.webcommander.task.TaskService
import com.webcommander.throwables.ApplicationRuntimeException
import com.webcommander.webcommerce.*
import grails.gorm.transactions.Transactional
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

import java.util.concurrent.ConcurrentHashMap

@Transactional
class DisposableUtilService {
    TaskService taskService
    CategoryService categoryService
    ContentService contentService
    ProductService productService
    AlbumService albumService
    NavigationService navigationService
    PageService pageService
    LayoutService layoutService

    @Autowired
    @Qualifier("com.webcommander.ApplicationTagLib")
    ApplicationTagLib app

    private ConcurrentHashMap<String, DisposableUtil> disposableUtilFactory = new ConcurrentHashMap<String, DisposableUtil>()

    void putDisposableUtilFactory(String type, DisposableUtil utilService) {
      //  disposableUtilFactory.put(type, utilService)
    }


    /* Count */

    Integer countItems(String itemType) {
        DisposableUtil service = disposableUtilFactory.get(itemType)
        if(service) {
            return service.countDisposableItems(itemType)
        }
        if(this.respondsTo("count${itemType.capitalize()}TypeItem")) {
            return  this."count${itemType.capitalize()}TypeItem"()
        }
        return 0
    }

    Integer countPageTypeItem() {
        return pageService.getPagesCount([isDisposable: "true"])
    }

    Integer countLayoutTypeItem() {
        return layoutService.getLayoutCount([layoutType: "disposable"])
    }

    Integer countCategoryTypeItem() {
        return categoryService.getCategoriesCount([isDisposable: "true"])
    }

    Integer countArticleTypeItem() {
        return contentService.getArticlesCount([isDisposable: "true"])
    }

    Integer countAlbumTypeItem() {
        return albumService.getAlbumCount([isDisposable: "true"])
    }

    Integer countProductTypeItem() {
        return productService.getProductsCount([isDisposable: "true"])
    }

    Integer countNavigationTypeItem() {
        return navigationService.getNavigationCount([isDisposable: "true"])
    }

    /*Remove*/
    void removeItems(String itemType, MultiLoggerTask task) {
        DisposableUtil service = disposableUtilFactory.get(itemType)
        if(service) {
            service.removeDisposableItems(itemType, task)
        } else if(this.respondsTo("remove${itemType.capitalize()}TypeItem")) {
            this."remove${itemType.capitalize()}TypeItem"(task)
        }
    }

    void removeLayoutTypeItem(MultiLoggerTask task) {
        Layout.withNewSession {session ->
            List<Layout> layouts = layoutService.getLayouts([layoutType: "disposable"])
            for (Layout layout : layouts) {
                try {
                    layoutService.deleteLayout(layout.id, "yes", "included")
                    session.flush()
                    task.progress = taskService.countProgress(task.totalRecord, ++task.recordComplete)
                    task.taskLogger.success("Layout: $layout.name", "")
                    task.meta.successCount++
                } catch (Exception e) {
                    task.progress = taskService.countProgress(task.totalRecord, ++task.recordComplete)
                    task.taskLogger.error("Layout: $layout.name", e.message)
                    task.meta.errorCount++;
                }
            }
        }
    }

    void removePageTypeItem(MultiLoggerTask task) {
        Page.withNewSession {session ->
            List<Page> pages = pageService.getPages([isDisposable: "true"])
            for (Page page : pages) {
                try {
                    pageService.putPageInTrash(page.id, "yes", "included")
                    AppEventManager.fire("before-page-delete", [page.id])
                    pageService.deletePage(page.id)
                    AppEventManager.fire("page-delete", [page.id])
                    session.flush()
                    task.progress = taskService.countProgress(task.totalRecord, ++task.recordComplete)
                    task.taskLogger.success("Page: $page.name", "")
                    task.meta.successCount++
                } catch (Exception e) {
                    task.progress = taskService.countProgress(task.totalRecord, ++task.recordComplete)
                    task.taskLogger.error("Page: $page.name", e.message)
                    task.meta.errorCount++;
                }
            }
        }
    }

    void removeArticleTypeItem(MultiLoggerTask task) {
        Article.withNewSession {session ->
            List<Article> articles = contentService.getArticles([isDisposable: "true"])
            for (Article article : articles) {
                try {
                    contentService.putArticleInTrash(article.id, true)
                    session.flush()
                    task.progress = taskService.countProgress(task.totalRecord, ++task.recordComplete)
                    task.taskLogger.success("Article: $article.name", "")
                    task.meta.successCount++
                } catch (Exception e) {
                    task.progress = taskService.countProgress(task.totalRecord, ++task.recordComplete)
                    task.taskLogger.error("Article: $article.name", e.message)
                    task.meta.errorCount++;
                }
            }
        }
    }

    void removeAlbumTypeItem(MultiLoggerTask task) {
        Album.withNewSession {session ->
            List<Album> albums  = albumService.getAlbums([isDisposable: "true"])
            for (Album album : albums) {
                try {
                    albumService.putAlbumInTrash(album.id, "yes", "include")
                    session.flush()
                    task.progress = taskService.countProgress(task.totalRecord, ++task.recordComplete)
                    task.taskLogger.success("Manufacturer: $album.name", "")
                    task.meta.successCount++
                } catch (Exception e) {
                    task.progress = taskService.countProgress(task.totalRecord, ++task.recordComplete)
                    task.taskLogger.error("Manufacturer: $album.name", e.message)
                    task.meta.errorCount++;
                }
            }
        }
    }

    void removeProductTypeItem(MultiLoggerTask task) {
        Product.withNewSession {session ->
            List<Product> products  = productService.getProducts([isDisposable: "true"])
            for (Product product : products) {
                try {
                    productService.putProductInTrash(product.id, "yes", "include")
                    session.flush()
                    task.progress = taskService.countProgress(task.totalRecord, ++task.recordComplete)
                    task.taskLogger.success("Product: $product.name", "")
                    task.meta.successCount++
                } catch (Exception e) {
                    task.progress = taskService.countProgress(task.totalRecord, ++task.recordComplete)
                    task.taskLogger.error("Product: $product.name", e.message)
                    task.meta.errorCount++;
                }
            }
        }
    }

    void removeNavigationTypeItem(MultiLoggerTask task) {
        Navigation.withNewSession {session ->
            List<Navigation> navigations  = navigationService.getNavigations([isDisposable: "true"])
            for (Navigation navigation : navigations) {
                try {
                    navigationService.putNavigationInTrash(navigation.id, "yes", "include")
                    session.flush()
                    task.progress = taskService.countProgress(task.totalRecord, ++task.recordComplete)
                    task.taskLogger.success("Navigation: $navigation.name", "")
                    task.meta.successCount++
                } catch (Exception e) {
                    task.progress = taskService.countProgress(task.totalRecord, ++task.recordComplete)
                    task.taskLogger.error("Navigation: $navigation.name", e.message)
                    task.meta.errorCount++;
                }
            }
        }
    }

    void removeCategoryTypeItem(MultiLoggerTask task) {
        Category.withNewSession {session ->
            List<Category> categories  = categoryService.getCategories([isDisposable: "true"])
            for (Category category : categories) {
                try {
                    categoryService.putCategoryInTrash(category.id, "yes", "include")
                    session.flush()
                    task.progress = taskService.countProgress(task.totalRecord, ++task.recordComplete)
                    task.taskLogger.success("Category: $category.name", "")
                    task.meta.successCount++
                } catch (Exception e) {
                    task.progress = taskService.countProgress(task.totalRecord, ++task.recordComplete)
                    task.taskLogger.error("Category: $category.name", e.message)
                    task.meta.errorCount++;
                }
            }
        }
    }

    Integer countTotalItems(List types) {
        Integer total = 0
        for(String type: types) {
            total += countItems(type)
        }
        return total
    }

    def initImport() {
        List types = ["page", "layout"]
        types.addAll DomainConstants.WIDGET_CONTENT_TYPE.values().toList().reverse()
        Integer total = countTotalItems(types);
        if(total <= 0) {
            throw new ApplicationRuntimeException("no.disposable.found")
        }
        MultiLoggerTask task = new MultiLoggerTask("Clear Disposable Items")
        task.detail_url = app.relativeBaseUrl() + "taskCommon/progressView";
        task.detail_status_url = app.relativeBaseUrl() + "taskCommon/progressStatus";
        task.detail_viewer = "app.tabs.setting.import_status_viewer"
        task.meta = [
                successCount: 0,
                warningCount: 0,
                errorCount  : 0,
        ]
        task.totalRecord = total
        task.onComplete {
            taskService.saveLogToSession(task);
            taskService.saveMultiLoggerTaskLog(task,  ["Status", "Name", "Remark"]);
            Thread.sleep(15000);
        }

        task.onError { Throwable t ->
            taskService.saveLogToSession(task);
            taskService.saveMultiLoggerTaskLog(task,  ["Status", "Name", "Remark"]);
            Thread.sleep(15000);
        }

        task.async {
            for(String type : types) {
                "$type" {
                    removeItems(it, task)
                }
            }
        }
        return task
    }
}
