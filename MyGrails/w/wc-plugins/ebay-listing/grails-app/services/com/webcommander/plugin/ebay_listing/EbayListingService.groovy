package com.webcommander.plugin.ebay_listing

import com.ebay.sdk.ApiContext
import com.ebay.sdk.call.GetItemCall
import com.ebay.soap.eBLBaseComponents.CategoryType
import com.ebay.soap.eBLBaseComponents.ItemType
import com.webcommander.JSONSerializableList
import com.webcommander.annotations.Initializable
import com.webcommander.common.CommonService
import com.webcommander.events.AppEventManager
import com.webcommander.plugin.ebay_listing.Job.SchedulerJob
import com.webcommander.plugin.ebay_listing.admin.mapping.EbayCategoryProfileMapping
import com.webcommander.plugin.ebay_listing.admin.mapping.EbayItemMapping
import com.webcommander.plugin.ebay_listing.admin.mapping.EbayProfileMapping
import com.webcommander.plugin.ebay_listing.admin.webmarketing.*
import com.webcommander.plugin.ebay_listing.constants.DomainConstants
import com.webcommander.plugin.ebay_listing.ebay_api.EbayApiService
import com.webcommander.plugin.ebay_listing.model.EbayCategoryData
import com.webcommander.throwables.ApplicationRuntimeException
import com.webcommander.util.AppUtil
import com.webcommander.util.DomainUtil
import com.webcommander.util.TrashUtil
import com.webcommander.webcommerce.Category
import com.webcommander.webcommerce.Product
import com.webcommander.webcommerce.ProductService
import grails.gorm.transactions.Transactional
import org.quartz.*

import java.util.concurrent.ConcurrentHashMap

@Initializable
class EbayListingService {
    CommonService commonService
    ProductService productService
    Scheduler quartzScheduler
    static transactional = false

    private static CategoryType[] ebay_category_types
    private static List<EbayCategoryData> ebay_category_tree
    private static Map<String, EbayCategoryData> ebay_category_map


    static void initialize() {
        AppEventManager.on("before-product-delete", { id ->
            List<EbayCategoryProfileMapping> mappings = EbayCategoryProfileMapping.createCriteria().list {
                eq("category.id", id)
            }
            mappings*.delete();
        });
        AppEventManager.on("before-ebayListingProfile-delete", {profile ->
            EbayCategoryProfileMapping.findAllByListingProfile(profile)*.delete()
        });

        AppEventManager.on("before-product-delete", { id ->
            List<EbayItemMapping> mappings = EbayItemMapping.createCriteria().list {
                eq("product.id", id)
            }
            mappings*.delete();
        });

        AppEventManager.on("before-ebayListingProfile-delete", {EbayListingProfile profile ->
            EbayListingProfileSetting toRemove = profile.setting
            if(!toRemove) {
                return ;
            }
            profile.setting = null;
            profile.save();
            toRemove.delete();
        });

        Long toRemove;
        AppEventManager.on("before-ebayListingProfile-delete", {EbayListingProfile profile ->
            toRemove = profile.safePaymentMethod.id;
            profile.availablePaymentMethods.metaValues.each {
                it*.delete();
            }
            profile.availablePaymentMethods*.delete();
        });
        AppEventManager.on("ebayListingProfile-delete", {
            EbayPaymentMethod method = EbayPaymentMethod.get(toRemove);
            method.metaValues*.delete();
            method.delete();
        });

        Long _toRemove;
        AppEventManager.on("before-ebayListingProfile-delete", {EbayListingProfile profile ->
            _toRemove = profile.postage.id
        });
        AppEventManager.on("ebayListingProfile-delete", {
            EbayPostage postage = EbayPostage.get(_toRemove);
            postage.delete();
        });

        Long __toRemove;
        AppEventManager.on("before-ebayListingProfile-delete", {EbayListingProfile profile ->
            __toRemove = profile.pricing.id
        });
        AppEventManager.on("ebayListingProfile-delete", {
            EbayPricing pricing = EbayPricing.get(__toRemove);
            AppEventManager.fire("before-ebayPricing-delete", [pricing])
            pricing.delete();
        });

        AppEventManager.on("before-ebayPricing-delete", {EbayPricing pricing ->
            EbayPricingProfile toDelete1 = pricing.buyNowPrice;
            EbayPricingProfile toDelete2 = pricing.startingPrice;
            pricing.buyNowPrice = null;
            pricing.startingPrice = null;
            pricing.save();
            if(toDelete1) {
                toDelete1.delete();
            }
            if(toDelete2) {
                toDelete2.delete();
            }
        })

        AppEventManager.on("before-product-delete", { id ->
            List<EbayProfileMapping> mappings = EbayProfileMapping.createCriteria().list {
                eq("product.id", id)
            }
            mappings*.delete();
        });
        AppEventManager.on("before-ebayListingProfile-delete", {profile ->
            EbayProfileMapping.findAllByListingProfile(profile)*.delete()
        });

        Long ___toRemove;
        AppEventManager.on("before-ebayListingProfile-delete", {EbayListingProfile profile ->
            ___toRemove = profile.returnPolicy.id;
        });
        AppEventManager.on("ebayListingProfile-delete", {
            EbayReturnPolicy.get(___toRemove).delete();
        });
    }

    private Closure getCriteriaClosure(Map params) {
        return {
            if (params.searchText) {
                ilike("name", "%${params.searchText.trim().encodeAsLikeText()}%")
            }
        }
    }

    public Integer getProfileCount(Map params) {
        return EbayListingProfile.createCriteria().count {
            and getCriteriaClosure(params)
        }
    }

    public List<EbayListingProfile> getProfiles(Map params) {
        def listMap = [max: params.max, offset: params.offset];
        return EbayListingProfile.createCriteria().list(listMap) {
            and getCriteriaClosure(params)
            order(params.sort ?: "name", params.dir ?: "asc")
        }
    }

    @Transactional
    public EbayListingProfile createProfile(Map params) {
        Map configs = AppUtil.getConfig(com.webcommander.constants.DomainConstants.SITE_CONFIG_TYPES.EBAY)
        EbayListingProfileSetting profileSetting
        if(configs.devid && configs.appid && configs.certid && configs.user_token) {
            profileSetting = new EbayListingProfileSetting(mode: configs.mode, ebaySite: configs.ebay_site, devId: configs.devid, appId: configs.appid, certId: configs.certid,
                    userToken: configs.user_token).save()
        }
        return new EbayListingProfile(
                name: params.name,
                pricing: new EbayPricing(buyNowPrice: new EbayPricingProfile(type: DomainConstants.PRICING_PROFILE_TYPE.PRODUCT_DEFAULT_PRICE).save()).save(),
                safePaymentMethod: new EbayPaymentMethod(name: "PayPal").save(),
                postage: new EbayPostage().save(),
                returnPolicy: new EbayReturnPolicy().save(),
                setting: profileSetting
        ).save()
    }

    @Transactional
    public Boolean updateBasic(Map params) {
        EbayListingProfile profile = EbayListingProfile.get(params.profileId.toLong(0))
        profile.name = params.name
        profile.note = params.note
        profile.primaryCategory = params.int("primaryCategory")
        profile.secondaryCategory = params.int("secondaryCategory")
        profile.itemCondition = params.int("itemCondition")
        profile.useProductImage = params.useProductImage == "true"
        profile.save()
        return !profile.hasErrors()
    }

    @Transactional
    public Boolean updatePricing(Map params) {
        EbayListingProfile profile = EbayListingProfile.get(params.profileId.toLong(0))
        EbayPricing pricing = profile.pricing
        pricing.type = params.type
        pricing.buyNowPrice = updatePricingProfile(pricing.buyNowPrice ?: new EbayPricingProfile(), params.buyItNowPrice)
        if(pricing.type == DomainConstants.PRICING_TYPE.AUCTION) {
            pricing.startingPrice = updatePricingProfile(pricing.startingPrice ?: new EbayPricingProfile(), params.startingPrice)
        } else {
            pricing.startingPrice = null
        }
        pricing.sellToQuantityType = params.sellToQuantityType
        pricing.quantity = params.quantity.toInteger(1)
        pricing.duration = params.duration.toInteger(7)
        pricing.isPrivateListing = params.isPrivateListing == "true"
        pricing.save()
        return !profile.hasErrors() && !pricing.hasErrors()
    }

    @Transactional
    public EbayPricingProfile updatePricingProfile(EbayPricingProfile pricingProfile, Map params) {
        pricingProfile.type = params.type
        pricingProfile.additionalType = params.additionalType
        pricingProfile.additionalAmount = params.additionalAmount.toDouble(0.0)
        pricingProfile.newAmount = params.newAmount.toDouble(0.0)
        pricingProfile.save()
        return pricingProfile
    }

    @Transactional
    public Boolean updatePaymentMethod(Map params) {
        EbayListingProfile profile = EbayListingProfile.get(params.profileId.toLong(0))
        profile.safePaymentMethod.metaValues*.delete()
        profile.safePaymentMethod.metaValues = []
        profile.safePaymentMethod.metaValues.add(new EbayMetaValue(name: "email", value: params.payPalEmail).save())
        profile.availablePaymentMethods*.delete()
        profile.availablePaymentMethods = []
        params.list("paymentMethod").each {
            profile.availablePaymentMethods.add(new EbayPaymentMethod(name: it))
        }
        return !profile.hasErrors()
    }

    @Transactional
    public Boolean updatePostage(Map params) {
        EbayListingProfile profile = EbayListingProfile.get(params.profileId.toLong(0))
        EbayPostage postage = profile.postage
        postage.shippingCost = params.shippingCost.toDouble(0.0)
        postage.handlingCost = params.handlingCost.toDouble(0.0)
        postage.enableGetItFast = params.enableGetItFast == "true"
        postage.save()
        return !profile.hasErrors()
    }

    @Transactional
    public Boolean updateReturnPolicy(Map params) {
        EbayListingProfile profile = EbayListingProfile.get(params.profileId.toLong(0))
        EbayReturnPolicy returnPolicy = profile.returnPolicy
        returnPolicy.acceptReturn = params.acceptReturn == "true"
        if(returnPolicy.acceptReturn) {
            returnPolicy.returnWithin = params.returnWithin
            returnPolicy.refundType = params.refundType
            returnPolicy.returnShippingPaidBy = params.returnShippingPaidBy
            returnPolicy.additionalReturnPolicyNote = params.additionalReturnPolicyNote?.encodeAsBMHTML()
        } else {
            returnPolicy.returnWithin = null
            returnPolicy.refundType = null
            returnPolicy.returnShippingPaidBy = null
            returnPolicy.additionalReturnPolicyNote = null
        }
        returnPolicy.save()
        return !profile.hasErrors()
    }

    @Transactional
    public Boolean updateSchedule(Map params) {
        EbayUpdateSchedule schedule = EbayUpdateSchedule.first();
        schedule.enableScheduleListing = params.enableScheduleListing == "true"
        schedule.scheduleBy = params.scheduleBy
        schedule.months = []
        schedule.days = []
        schedule.dates = []
        schedule.hours = []
        schedule.minutes = []
        if(schedule.enableScheduleListing) {
            schedule.months = params.months.collect{ it.toInteger() }
            schedule.days = params.days.collect{ it.toInteger() }
            schedule.dates = params.dates.collect{ it.toInteger() }
            schedule.hours = params.hours.collect{ it.toInteger() }
            schedule.minutes = params.minutes.collect{ it.toInteger() }
        }
        schedule.save()
        return !schedule.hasErrors()
    }

    @Transactional
    public Boolean updateSettings(Map params) {
        EbayListingProfile profile = EbayListingProfile.get(params.profileId.toLong(0))
        profile.useDefaultSetting = params.useDefaultSetting == "true"
        EbayListingProfileSetting setting = profile.setting ?: new EbayListingProfileSetting()
        if(profile.useDefaultSetting) {
            Map configs = AppUtil.getConfig(com.webcommander.constants.DomainConstants.SITE_CONFIG_TYPES.EBAY)
            if(configs.devid && configs.appid && configs.certid && configs.user_token) {
                setting.mode = configs.mode
                setting.ebaySite = configs.ebay_site
                setting.devId = configs.devid
                setting.appId = configs.appid
                setting.certId = configs.certid
                setting.userToken = configs.user_token
            } else {
                return !profile.hasErrors()
            }
        } else {
            setting.mode = params.mode
            setting.ebaySite = params.ebaySite
            setting.devId = params.devId
            setting.appId = params.appId
            setting.certId = params.certId
            setting.userToken = params.userToken
        }
        setting.save()
        profile.setting = setting
        profile.save()
        return !profile.hasErrors()
    }

    public CategoryType[] getEbayCategoryTypes(EbayListingProfile profile, Boolean forceLoad = false) {
        if(!ebay_category_types || forceLoad) {
            try {
                ebay_category_types = EbayApiService.loadEbayCategories(profile)
            } catch(Exception e) {
                throw new ApplicationRuntimeException("ebay.category.load.failed")
            }
        }
        return ebay_category_types
    }

    public Map<String, EbayCategoryData> getEbayCategoryMap(EbayListingProfile profile, Boolean forceLoad = false) {
        if(!ebay_category_map || forceLoad) {
            ebay_category_map = new ConcurrentHashMap<>()
            for(CategoryType categoryType : getEbayCategoryTypes(profile, forceLoad)) {
                for(int i = 0; i < categoryType.categoryParentID.length; i++) {
                    EbayCategoryData category = new EbayCategoryData(categoryType, i)
                    ebay_category_map[category.id] = category
                }
            }
        }
        return ebay_category_map
    }

    public List<EbayCategoryData> getEbayCategoryTree(EbayListingProfile profile, Boolean forceLoad = false) {
        if(!ebay_category_tree || forceLoad) {
            ebay_category_tree = new JSONSerializableList<>()
            ebay_category_tree = buildCategoryTree(profile, forceLoad)
        }
        return ebay_category_tree
    }

    public List<EbayCategoryData> buildCategoryTree(EbayListingProfile profile, Boolean forceLoad = false) {
        List<EbayCategoryData> categoryTree = new JSONSerializableList<EbayCategoryData>()
        Map<String, EbayCategoryData> categoryMap = getEbayCategoryMap(profile, forceLoad)
        categoryMap.each { ID, category ->
            if(category.parentId == "0") {
                categoryTree.add(category)
            } else {
                EbayCategoryData parent = categoryMap[category.parentId]
                parent.children.add(category)
            }
        }
        Collections.sort(categoryTree)
        return categoryTree
    }

    public List<EbayCategoryData> getCategoryBreadcrumb(EbayListingProfile profile, Boolean isPrimaryCategory = true) {
        Map<String, EbayCategoryData> categoryMap = getEbayCategoryMap(profile)
        EbayCategoryData category = categoryMap[isPrimaryCategory ? profile.primaryCategory + "" : profile.secondaryCategory + ""]
        List<EbayCategoryData> breadcrumb = []
        try {
            while(category.parentId != "0") {
                breadcrumb.add(category)
                category = categoryMap[category.parentId]
            }
            breadcrumb.add(category)
        } catch(Exception e) {
            throw new ApplicationRuntimeException("cannot.fetch.ebay.category")
        }
        return breadcrumb
    }

    @Transactional
    boolean deleteProfile(Long id, String at1, String at2){
        TrashUtil.preProcessFinalDelete("ebayListingProfile", id, at2 != null, at1 != null)
        EbayListingProfile profile = EbayListingProfile.proxy(id);
        AppEventManager.fire("before-ebayListingProfile-delete", [profile]);
        profile.delete()
        AppEventManager.fire("ebayListingProfile-delete");
        return true;
    }

    Integer deleteSelectedProfile(List id, String at1, String at2){
        Integer count = 0
        id.each {
            if( deleteProfile(it, at1, at2) )
                count++
        }
        return count
    }

    @Transactional
    Boolean copyProfile(Long id) {
        EbayListingProfile profile = EbayListingProfile.get(id);
        EbayListingProfile copyProfile = DomainUtil.clone(profile, ['pricing', 'safePaymentMethod', 'postage', 'returnPolicy', 'setting', 'updateSchedule', 'availablePaymentMethods', 'created', 'updated'], ["categories", "products"]);
        copyProfile.name = commonService.getCopyNameForDomain(profile);
        EbayPaymentMethod safePaymentMethod = new EbayPaymentMethod();
        safePaymentMethod.name = profile.safePaymentMethod.name;
        profile.safePaymentMethod.metaValues.each {
            EbayMetaValue metaValue = new EbayMetaValue();
            metaValue.name = it.name;
            metaValue.value = it.value
            metaValue.save();
            safePaymentMethod.addToMetaValues(metaValue);
        }
        copyProfile.safePaymentMethod = safePaymentMethod.save();
        EbayPricing ebayPricing = DomainUtil.clone(profile.pricing, ["buyNowPrice", "startingPrice"])
        if(profile.pricing.buyNowPrice) {
            EbayPricingProfile pricingProfile = DomainUtil.clone(profile.pricing.buyNowPrice)
            pricingProfile.save();
            ebayPricing.buyNowPrice = pricingProfile;
        }
        if(profile.pricing.startingPrice) {
            EbayPricingProfile pricingProfile = DomainUtil.clone(profile.pricing.startingPrice)
            pricingProfile.save();
            ebayPricing.startingPrice = pricingProfile;
        }
        copyProfile.pricing = ebayPricing.save();
        EbayPostage postage = DomainUtil.clone(profile.postage);
        copyProfile.postage = postage.save();
        EbayReturnPolicy returnPolicy = DomainUtil.clone(profile.returnPolicy)
        copyProfile.returnPolicy = returnPolicy.save();
        profile.availablePaymentMethods.each {
            EbayPaymentMethod paymentMethod = new EbayPaymentMethod();
            paymentMethod.name = it.name;
            it.metaValues.each {
                EbayMetaValue metaValue = new EbayMetaValue();
                metaValue.name = it.name;
                metaValue.value = it.value
                metaValue.save();
                paymentMethod.addToMetaValues(metaValue);
            }
            copyProfile.addToAvailablePaymentMethods(paymentMethod.save());
        }
        EbayListingProfileSetting setting = DomainUtil.clone(profile.setting);
        copyProfile.setting = setting.save();
        copyProfile.save()
        return true;
    }

    @Transactional
    Boolean mapProductProfile(Map params) {
        Product product = Product.get(params.productId);
        EbayListingProfile listingProfile = EbayListingProfile.get(params.profile)
        EbayProfileMapping mapping = EbayProfileMapping.findByProduct(product) ?: new EbayProfileMapping();
        mapping.product = product;
        mapping.listingProfile = listingProfile
        mapping.save()
        return !mapping.hasErrors()
    }

    @Transactional
    Boolean mapCategoryProfile(Map params) {
        Category category = Category.get(params.categoryId);
        EbayListingProfile listingProfile = EbayListingProfile.get(params.profile)
        EbayCategoryProfileMapping mapping = EbayCategoryProfileMapping.findByCategory(category) ?: new EbayCategoryProfileMapping();
        mapping.category = category;
        mapping.listingProfile = listingProfile
        mapping.save()
        return !mapping.hasErrors()
    }

    @Transactional
    Map listProductOnEbay(Product product, EbayListingProfile listingProfile) {
        Map response = EbayApiService.addItem(listingProfile, product);
        if(response.status == "success") {
            EbayItemMapping mapping = new EbayItemMapping();
            mapping.product = product;
            mapping.ebayItemId = response["itemId"];
            mapping.save();
            return response
        }
        return response
    }

    @Transactional
    Map listProductOnEbay(Map params) {
        Product product = Product.get(params.productId);
        EbayListingProfile listingProfile = EbayListingProfile.get(params.profileId);
        return listProductOnEbay(product, listingProfile)
    }

    def listCategoryOnEbay(Map params) {
        List<Product> products = productService.getProducts([parent: params.categoryId, lookup: "recursive", stock: "in"])
        EbayListingProfile listingProfile = EbayListingProfile.get(params.profileId);
        Integer success = 0;
        products.each {
            Map response = listProductOnEbay(it, listingProfile)
            if(response.status == "success") {
                success++
            }
        }
        return [success: success, total: products.size()]
    }

    def startScheduler() {
        JobDetail job = JobBuilder.newJob(SchedulerJob.class)
                .withIdentity("ebayUpdateScheduler", "ebay").build();
        Trigger trigger = TriggerBuilder
                .newTrigger()
                .withIdentity("ebayUpdateTrigger", "ebay")
                .withSchedule(
                SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInMinutes(4).repeatForever())
                .build();
        quartzScheduler.scheduleJob(job, trigger)
    }

    def synchronizeInventory() {
        Map config = AppUtil.getConfig("ebay_listing")
        ApiContext apiContext = EbayApiService.getApiContext(config);
        GetItemCall getItemCall = new GetItemCall(apiContext);
        List<EbayItemMapping> ebayItemMappings = EbayItemMapping.list();
        ebayItemMappings.each {
            try {
                Product product = it.product
                if(product.isInventoryEnabled) {
                    ItemType itemType = getItemCall.getItem(it.ebayItemId);
                    Integer prevSold = it.sold ?: 0;
                    Integer currentSold = itemType.sellingStatus.getQuantitySold();
                    if(prevSold < currentSold) {
                        productService.updateStock(product.id, (currentSold - prevSold), "After Sell Ebay Item# ${it.ebayItemId}")
                        it.sold = currentSold;
                        it.save();
                    }
                }

            } catch (Exception ex) {
                log.error(ex.message)
            }
        }
    }

}
