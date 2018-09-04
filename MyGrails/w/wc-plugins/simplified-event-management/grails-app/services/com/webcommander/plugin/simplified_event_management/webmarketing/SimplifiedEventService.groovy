package com.webcommander.plugin.simplified_event_management.webmarketing

import com.webcommander.JSONSerializableList
import com.webcommander.annotations.Initializable
import com.webcommander.common.CommanderMailService
import com.webcommander.common.MetaTag
import com.webcommander.common.ImageService
import com.webcommander.constants.DomainConstants
import com.webcommander.constants.NamedConstants
import com.webcommander.events.AppEventManager
import com.webcommander.manager.CartManager
import com.webcommander.manager.HookManager
import com.webcommander.manager.PathManager
import com.webcommander.models.Cart
import com.webcommander.plugin.simplified_event_management.SimplifiedEventCheckoutField
import com.webcommander.plugin.simplified_event_management.SimplifiedEventCheckoutFieldsTitle
import com.webcommander.plugin.simplified_event_management.SimplifiedEventCustomFieldData
import com.webcommander.plugin.simplified_event_management.SimplifiedEventImage
import com.webcommander.plugin.simplified_event_management.TicketInventorAdjustment
import com.webcommander.plugin.simplified_event_management.model.CartSimplifiedEventTicket
import com.webcommander.plugin.simplified_event_management.model.SimplifiedEventData
import com.webcommander.throwables.ApplicationRuntimeException
import com.webcommander.throwables.UnconfiguredWidgetExceptions
import com.webcommander.util.AppUtil
import com.webcommander.webcommerce.Order
import com.webcommander.webcommerce.TaxProfile
import com.webcommander.widget.Widget
import grails.converters.JSON
import com.webcommander.plugin.simplified_event_management.SimplifiedEvent
import grails.transaction.NotTransactional
import grails.gorm.transactions.Transactional
import grails.util.Holders
import org.apache.commons.io.FilenameUtils
import grails.web.databinding.DataBindingUtils
import grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.multipart.MultipartFile

@Initializable
class SimplifiedEventService {
    ImageService imageService
    CommanderMailService commanderMailService

    static void initialize() {
        HookManager.register("taxProfile-delete-at2-count") { response, id ->
            int eventCount = SimplifiedEvent.where {
                taxProfile.id == id
            }.count()
            if(eventCount) {
                response.events = eventCount
            }
            return response
        }
        HookManager.register("taxProfile-delete-at2-list") { response, id ->
            List events = SimplifiedEvent.createCriteria().list {
                projections {
                    property("name")
                }
                eq("taxProfile.id", id)
            }
            if(events.size()) {
                response.events = events
            }
            return response
        }
        AppEventManager.on("before-taxProfile-delete", { id ->
            TaxProfile profile = TaxProfile.proxy(id);
            SimplifiedEvent.where {
                taxProfile == profile
            }.updateAll([taxProfile: null])
        })

        AppEventManager.on("before-simplified-event-delete", { id ->
            SimplifiedEventCheckoutField.createCriteria().list {
                eq("event.id", id)
            }*.delete()
        })

        AppEventManager.on("before-simplified-event-delete", { id ->
            SimplifiedEventCheckoutFieldsTitle.createCriteria().list {
                eq("event.id", id)
            }*.delete()
        })

        AppEventManager.on("before-simplified-event-delete", { id ->
            SimplifiedEventCustomFieldData.createCriteria().list {
                eq("event.id", id)
            }*.delete()
        })
    }

    static {
        AppEventManager.on("order-create order-update", {Long orderId ->
            def updateStockConfig = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.E_COMMERCE, "update_ticket_stock")
            if (updateStockConfig == DomainConstants.UPDATE_STOCK.AFTER_ORDER) {
                SimplifiedEventService _this = Holders.grailsApplication.mainContext.getBean(SimplifiedEventService)
                _this.adjustTicketSoldQuantity(orderId)
            }
        });
        AppEventManager.on("paid-for-cart", { Collection<Cart> carts ->
            def updateStockConfig = AppUtil.getConfig(DomainConstants.SITE_CONFIG_TYPES.E_COMMERCE, "update_ticket_stock")
            if (updateStockConfig == DomainConstants.UPDATE_STOCK.AFTER_PAYMENT) {
                SimplifiedEventService _this = Holders.grailsApplication.mainContext.getBean(SimplifiedEventService)
                carts.each {Cart cart->
                    _this.adjustTicketSoldQuantity(cart.orderId)
                }
            }
        });
        AppEventManager.on("before-order-update", {Long orderId ->
            SimplifiedEventService _this = Holders.grailsApplication.mainContext.getBean(SimplifiedEventService)
            _this.reAdjustTicketSoldQuantity(orderId)
        });

        AppEventManager.on("paid-for-order", { Order order->
            SimplifiedEventService _this = Holders.grailsApplication.mainContext.getBean(SimplifiedEventService)
            _this.adjustTicketSoldQuantity(order.id)
        });

        HookManager.register("resolveCartObject-simplified_event_ticket", { object, cartItem ->
            return new CartSimplifiedEventTicket(cartItem.itemId);
        });

        HookManager.register("addToCart-simplified_event_ticket", {def cartItem ->
            CartSimplifiedEventTicket simplifiedEventTicket = new CartSimplifiedEventTicket(cartItem.itemId);
            CartManager.addToCart(AppUtil.session.id, simplifiedEventTicket, cartItem.quantity);
        });

    }

    def saveEvent(Map params) {
        Long id = params.id ? params.id.toLong(0) : null;
        if(id) {
            Integer soldTickets = SimplifiedEvent.proxy(id).totalSoldTicket.toInteger();
            if(soldTickets > params.maxTicket.toInteger()) {
                throw new ApplicationRuntimeException("event.update.error", [soldTickets]);
            }
        }
        SimplifiedEvent event = id ? SimplifiedEvent.get(id) : new SimplifiedEvent()
        def session = AppUtil.session;
        event.name = params.name
        event.heading = params.heading
        event.title = params.title
        event.isPublic = params.isPublic.toBoolean()
        event.summary = params.summary
        event.description = params.description
        event.ticketPrice = params.ticketPrice.toDouble()
        event.maxTicket = params.maxTicket.toInteger()
        event.showGoogleMap = params.showGoogleMap.toBoolean()
        if(params.showGoogleMap.toBoolean()) {
            event.latitude = params.latitude.toDouble()
            event.longitude = params.longitude.toDouble()
        }
        event.taxProfile = params.taxProfile ? TaxProfile.proxy(params.taxProfile) : null
        event.maxTicketPerPerson = params.maxTicketPerPerson? params.maxTicketPerPerson.toInteger() : null
        event.address = params.address
        event.startTime =  params.startTime?.toDate()?.gmt(AppUtil.session.timezone)
        event.endTime = params.endTime?.toDate()?.gmt(AppUtil.session.timezone)
        event.metaTags*.delete()
        event.metaTags = [];
        def tag_names = params.list("tag_name");
        def tag_values = params.list("tag_content");
        for (int i = 0; i < tag_names.size(); i++) {
            MetaTag metaTag = new MetaTag(name: tag_names[i], value: tag_values[i]);
            metaTag.save()
            event.metaTags.add(metaTag)
        }
        event.save()
        if(!event.hasErrors()){
            return event.id
        } else {
            return null
        }
    }

    public String processFilePath(String filePath) {
        File dir = new File(filePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return filePath
    }

    public String processImageName(String filePath, String originalFilename) {
        String name = FilenameUtils.getBaseName(originalFilename);
        String attempt = name;
        Integer tryCount = 0;
        String extension = FilenameUtils.getExtension(originalFilename);
        while(true) {
            File targetFile = new File(filePath, attempt + "." + extension)
            if(!targetFile.exists()) {
                break;
            }
            attempt = name + "_" + (++tryCount);
        }
        return attempt + "." + extension;
    }

    boolean saveEventFile(MultipartFile inputFile, String removeFile, Long eventId) {
        SimplifiedEvent event = SimplifiedEvent.get(eventId);
        String filePath
        filePath = processFilePath(PathManager.getResourceRoot("simplified-event/event-${eventId}/personalized"));

        File dir = new File(filePath)

        if(removeFile) {
            dir.traverse {file ->
                file.delete()
                event.file = null
            }
        }
        if(inputFile) {
            if (!dir.exists()) {
                dir.mkdirs()
            }else {
                dir.traverse {file ->
                    file.delete()
                }
            }
            String name = processImageName(filePath, inputFile.originalFilename)
            String originalFilePath = filePath + File.separator + name;
            OutputStream out = new FileOutputStream(originalFilePath);
            InputStream uploadedStream = inputFile.inputStream
            out << uploadedStream;
            out.close();
            uploadedStream.close();

            event.file = name
            event.merge()
            return !event.hasErrors();
        }else {
            return true
        }

    }

    boolean saveEventImages(Long id, List<MultipartFile> images, List removeImgIds) {
        SimplifiedEvent event = SimplifiedEvent.get(id)
        List<String> names = removeImgIds.size() > 0 ? SimplifiedEventImage.where {
            id in removeImgIds
        }.list().name : []

        boolean success = removeEventImages(removeImgIds)

        if(images?.size()) {
            String filePath = processFilePath( PathManager.getResourceRoot("simplified-event/event-${event?.id}/images") )
            images.each {
                String name = processImageName(filePath, it.originalFilename)
                SimplifiedEventImage image = new SimplifiedEventImage(name: name, event: event, idx: getIndexForNewImage(event), baseUrl: filePath)
                imageService.uploadImage(it, NamedConstants.IMAGE_RESIZE_TYPE.EVENT_IMAGE, image)
                image.save()
                event.addToImages(image)
            }
        }
        event.merge()
        return !event.hasErrors()
    }

    @Transactional
    Boolean removeEventImages(List<Long> ids) {
        boolean success
        if(ids.size() > 0) {
            def criteria = SimplifiedEventImage.where {
                id in ids
            }
            List<SimplifiedEventImage> eventImages = criteria.list()
            success = criteria.deleteAll() > 0
            eventImages*.afterDelete()
        } else {
            success = true
        }
        return success
    }

    Integer getEventsCount(Map params) {
        return SimplifiedEvent.createCriteria().count {
            and getEventCriteriaClosure(params)
        }
    }

    List<SimplifiedEvent> getEvents(Map params) {
        Map listMap = [max: params.max, offset: params.offset]
        return SimplifiedEvent.createCriteria().list(listMap) {
            and getEventCriteriaClosure(params)
            order(params.sort ?: "name", params.dir ?: "asc");
        }
    }

    boolean deleteEvent(Long id){
        AppEventManager.fire("before-simplified-event-delete", [id])
        SimplifiedEvent event = SimplifiedEvent.get(id)
        event.delete()
        AppEventManager.fire("simplified-event-delete", [id])
        return !event.hasErrors()
    }

    Integer deleteSelectedEvents(List ids) {
        int removeCount = 0;
        ids.each { id ->
            SimplifiedEvent.withNewSession { session ->
                if(deleteEvent(id)) {
                    removeCount++;
                }
                session.flush()
            }
        }
        return removeCount
    }

    public boolean sendPersonalizedProgram(String email, SimplifiedEvent event, String customerName) {
        Map macrosAndTemplate = commanderMailService.getMacrosAndTemplateByIdentifier("personalized-program-for-simplified-event")
        if(!macrosAndTemplate.emailTemplate.active) {
            return;
        }
        Map refinedMacros = macrosAndTemplate.commonMacros
        macrosAndTemplate.macros.each {
            switch (it.key.toString()) {
                case "customer_name":
                    refinedMacros[it.key] = customerName
                    break;
                case "event_name":
                    refinedMacros[it.key] = event.name
                    break;
                case "event_start_date":
                    refinedMacros[it.key] = event.startTime.toEmailFormat()
                    break;
            }
        }
        List<File> attachments = []
        try {
            if(event.file) {
                String path = PathManager.getResourceRoot("simplified-event/event-${event.id}/personalized/${event.file}")
                attachments.add(new File(path))
                commanderMailService.sendMail(macrosAndTemplate.emailTemplate, macrosAndTemplate.activeHtml, macrosAndTemplate.activeText, refinedMacros, attachments, email)
            } else {
                commanderMailService.sendMail(macrosAndTemplate.emailTemplate, macrosAndTemplate.activeHtml, macrosAndTemplate.activeText, refinedMacros, email)
            }
        } catch(Exception e) {
            return false
        }
        return true
    }

    private Closure getEventCriteriaClosure(Map params) {
        def session = AppUtil.session;
        return {
            if (params.searchText) {
                ilike("name", "%${params.searchText.trim().encodeAsLikeText()}%");
            }
            if (params.name) {
                ilike("name", "%${params.name.trim().encodeAsLikeText()}%");
            }
            if(params.isPublic != null) {
                eq("isPublic", params.isPublic)
            }
            if(params.startTime) {
                Date date = params.startTime.dayStart.gmt(session.timezone);
                ge("startTime", date)
            }
            if(params.endTime) {
                Date date = params.endTime.dayEnd.gmt(session.timezone)
                le("endTime", date)
            }
            if(params.eventIds != null) {
                if(params.eventIds.size() == 0) {
                    eq("id", 0L)
                } else {
                    inList("id", params.eventIds)
                }
            }
        }
    }

    public Collection<SimplifiedEvent> getEventsForWidgetContent(List<Long> contentIds) {
        Collection<SimplifiedEvent> events = SimplifiedEvent.createCriteria().list {
            and getEventCriteriaClosure([eventIds: contentIds])
        }
        return events
    }

    List getStatusOfEvents(List<SimplifiedEvent> events) {
        List status = []
        String tempStatus
        events.each { event ->
            tempStatus = isEventCompleted(event) ? "complete" : (isEventActive(event) ? "active" : "unconfirmed")
            status.add(tempStatus)
        }
        return status
    }

    boolean isEventCompleted(SimplifiedEvent event) {
        Calendar calendar = Calendar.getInstance()
        Date currentTime = calendar.getTime().gmt(AppUtil.session.timezone)
        if(currentTime > event.endTime.gmt(AppUtil.session.timezone)) {
            return true
        }
    }

    boolean isEventActive(SimplifiedEvent event) {
        if(event.isPublic) {
            return true
        }
    }

    public String ticketPriceWithCurrency(SimplifiedEvent event) {
        return AppUtil.session.currency?.symbol?:AppUtil.baseCurrency.symbol + " " + (getLowestTicketPrice(event) == getHighestTicketPrice(event) ?
                getLowestTicketPrice(event).toCurrency().toPrice() : getLowestTicketPrice(event).toCurrency().toPrice() + "-" + getHighestTicketPrice(event).toCurrency().toPrice())
    }

    public Double getLowestTicketPrice(SimplifiedEvent event) {
        Double price = 0d
        price = event.ticketPrice
        return price
    }

    public Double getHighestTicketPrice(SimplifiedEvent event) {
        Double price = 0d
        price = event.ticketPrice
        return price
    }

    public String decimalToAlphabet(int target) {
        StringBuilder res = new StringBuilder();
        while(target > 0) {
            target--;
            int rem = target % 26;
            res.insert(0, (char)(rem + ('A' as char)));
            target = (target - rem) / 26;
        }
        return res.toString();
    }

    public Map renderEventWidget(Widget widget) {
        def config
        if (widget.params) {
            config = JSON.parse(widget.params)
        } else {
            throw new UnconfiguredWidgetExceptions()
        }
        String view = "/plugins/simplified_event_management/widget/event"
        Map model = [widget: widget, config: config]

        GrailsParameterMap _rparams = RequestContextHolder.currentRequestAttributes().params;
        Map widgetParams = [:]
        widgetParams.isPublic = true
        if(config.selectionType != "all") {
            List<Long> eventIds = widget.widgetContent.contentId.collect { it.toLong() }
            widgetParams.eventIds = eventIds
        }

        if(config.displayType == "basic-calendar" || config.displayType == "advance-calendar") {
            widgetParams.offset = 0
            widgetParams.max = -1
            widgetParams.startTime = new Date().gmt()
            List<SimplifiedEvent> events = getEvents(widgetParams)
            List<SimplifiedEventData> eventDataList = new JSONSerializableList<SimplifiedEventData>()
            events.each { event ->
                eventDataList.add(new SimplifiedEventData(event))
            }

            model += [events: events, eventDataList: eventDataList.serialize()]
        } else {
            Date startTime = new Date().gmt()
            Collection<SimplifiedEvent> allActiveEvents = []
            Collection<SimplifiedEvent> allEvents
            if(widgetParams.eventIds != null) {
                allEvents = widgetParams.eventIds ? SimplifiedEvent.findAllByIdInList(widgetParams.eventIds) : []
            } else {
                allEvents = SimplifiedEvent.findAll()
            }
            allEvents.each { event ->
                if(event.isPublic) {
                    if(event.startTime >= startTime) {
                        allActiveEvents.add(event)
                    }
                }
            }
            allActiveEvents.sort {
                it.startTime
            }
            if(config.listViewType == "paginated") {
                Map paginationProps = [offset: 0, max: 0, url_prefix: "evwd-" + widget.id]
                paginationProps.offset = _rparams.int(paginationProps.url_prefix + "-offset") ?: 0
                config.itemsPerPage = config.itemsPerPage ?: "10"
                paginationProps.max = _rparams.int(paginationProps.url_prefix + "-max") ?: config.itemsPerPage.toInteger(0)
                model += paginationProps
                _rparams.max = widgetParams.max = (_rparams[paginationProps.url_prefix + "-max"] ?: paginationProps.max).toInteger()
                _rparams.offset = widgetParams.offset = (_rparams[paginationProps.url_prefix + "-offset"] ?: paginationProps.offset).toInteger()
                def count = allActiveEvents.size()
                Collection<SimplifiedEvent> events = []
                if(count > 0) {
                    if(widgetParams.max < 1) {
                        widgetParams.max = count
                        widgetParams.offset = 0
                    }
                    Integer lastIndex = count < (widgetParams.offset + widgetParams.max) ? count : widgetParams.offset + widgetParams.max
                    events = allActiveEvents[widgetParams.offset .. (lastIndex - 1)]
                }
                model += [events: events, count: count]
            } else {
                model += [events: allActiveEvents]
            }
        }
        view += config.displayType.toString().indexOf('-calendar') != -1 ? "/calendarView" : "/listView"
        return [view: view, model: model]
    }

    void adjustTicketSoldQuantity(Long orderId) {
        Order order = Order.get(orderId);
        List<TicketInventorAdjustment> adjustments = TicketInventorAdjustment.findAllByOrder(order)
        if(adjustments) {
            return;
        }
        order.items.each {
            SimplifiedEvent event
            if(it.productType ==  NamedConstants.CART_OBJECT_TYPES.SIMPLIFIED_EVENT_TICKET && (event = SimplifiedEvent.get(it.productId))) {
                event.totalSoldTicket += it.quantity;
                String message = "After order# " + orderId;
                TicketInventorAdjustment adjustment = new TicketInventorAdjustment(changeQuantity: -1 * it.quantity, order: order, event: event, note: message);
                adjustment.save();
                event.save();
            }
        }
    }

    void reAdjustTicketSoldQuantity(Long orderId) {
        Order order = Order.get(orderId);
        List<TicketInventorAdjustment> adjustments = TicketInventorAdjustment.findAllByOrder(order)
        adjustments.each {
            it.event.totalSoldTicket += it.changeQuantity;
            it.save()
        }
        adjustments*.delete()
    }

    @Transactional
    Boolean saveCustomField(Map params) {
        SimplifiedEventCheckoutField field;
        if(params.id) {
            field = SimplifiedEventCheckoutField.get(params.id);
            field.options.clear();
        }else {
            field = new SimplifiedEventCheckoutField();
        }
        DataBindingUtils.bindObjectToInstance(field, params, null, ["id", "options"], null);
        params.list("options").each { option ->
            if(option) { //preventing addition of empty string
                field.options.add(option)
            }
        }
        field.save();
        return !field.hasErrors();
    }

    Boolean deleteCustomField(Long id) {
        SimplifiedEventCheckoutField field = SimplifiedEventCheckoutField.get(id);
        field.delete();
        return !field.hasErrors();
    }

    Boolean saveCustomFieldTitle(Map params) {
        def entity = SimplifiedEvent.proxy(params.eventId);
        def title = SimplifiedEventCheckoutFieldsTitle.findByEvent(entity);
        if(title && !params.title) {
            title.delete()
        } else if(params.title) {
            if(!title) {
                title = new SimplifiedEventCheckoutFieldsTitle();
                title["event"] = entity
            }
            title.title = params.title
            title.save()
        } else {
            return true
        }
        return !title.hasErrors()
    }

    @NotTransactional
    def getFieldsOrTitle(Long eventId, Boolean getTitle = false) {
        if(getTitle) {
            String title = SimplifiedEventCheckoutFieldsTitle.findByEvent(SimplifiedEvent.get(eventId))?.title;
            return title;
        }else {
            List<SimplifiedEventCheckoutField> fields = SimplifiedEventCheckoutField.createCriteria().list {
                eq("event.id", eventId)
            }
            return [fields: fields.unique {a, b -> a.name <=> b.name}]
        }
    }

    Integer getIndexForNewImage(SimplifiedEvent event) {
        def idx = SimplifiedEventImage.createCriteria().list {
            projections {
                max("idx")
            }
            eq("event", event)
        }
        return idx[0] != null ? idx[0] + 1 : 1
    }
}
