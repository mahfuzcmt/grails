package com.webcommander.task

import com.webcommander.config.SiteConfig
import com.webcommander.constants.DomainConstants
import com.webcommander.data.ESResultData
import com.webcommander.search.ESSearchHelper
import com.webcommander.search.SearchParams
import com.webcommander.search.SearchResult
import com.webcommander.util.AppUtil
import com.webcommander.util.ConfigurationReader
import com.webcommander.util.HttpUtil
import grails.converters.JSON
import grails.util.Holders


class ElasticSearchService {

    public static def getTypes(){
        def map = [:];
        map.blog = [offset:0,max:500]
        map.product = [offset:0,max:500]
        map.category = [offset:0,max:500]
        map.page = [offset:0,max:500]
        return  map
    }

    private  String concatURL(String first, String second){
        if (first.endsWith("/")){
            return first + second;
        }else {
            return first + "/" + second;
        }
    }

    private def callToESServer(String urlExtension) {
        String url = SiteConfig.findByTypeAndConfigKey(DomainConstants.SITE_CONFIG_TYPES.ELASTIC_SEARCH, 'url').value
        String index = SiteConfig.findByTypeAndConfigKey(DomainConstants.SITE_CONFIG_TYPES.ELASTIC_SEARCH, 'index').value
        url = concatURL(url, index)
        try {
            return HttpUtil.doGetRequest(concatURL(url,urlExtension));
        } catch (IOException io) {
            return null
        }
    }

    SearchResult elasticSearch(String urlExtension) {
        String url = SiteConfig.findByTypeAndConfigKey(DomainConstants.SITE_CONFIG_TYPES.ELASTIC_SEARCH, 'url').value
        String index = SiteConfig.findByTypeAndConfigKey(DomainConstants.SITE_CONFIG_TYPES.ELASTIC_SEARCH, 'index').value
        ESSearchHelper esSearchHelper = new ESSearchHelper(url , index)
        println("elasticSearch: ${urlExtension}")
        return  esSearchHelper.search(SearchParams.getInstance(urlExtension))
    }

    public def search(String urlExtension){
        def result = [:]
        def blog = []
        def product = []
        def category = []
        def page = []
        result.isSuccess = false;
        String isAliveES = callToESServer("");
        if (isAliveES != null){
            String pullDataFromServer = callToESServer(urlExtension);
            if (pullDataFromServer != null){
                def map = JSON.parse(pullDataFromServer)
                result.total = map?.hits?.total?:0
                def hits = map?.hits?.hits
                hits.each{ hit ->
                    if ( hit._type.equals("blog") ) {
                        if (hit._source){
                            blog.add(hit._source)
                        }
                    } else if ( hit._type.equals("page") ) {
                        if (hit._source) {
                            page.add(hit._source)
                        }
                    } else if ( hit._type.equals("product") ) {
                        if (hit._source) {
                            product.add(hit._source)
                        }
                    } else if ( hit._type.equals("category") ) {
                        if (hit._source) {
                            category.add(hit._source)
                        }
                    }
                }
                result.isSuccess = true;
            }
        }
        result.blog = blog
        result.product = product
        result.category = category
        result.page = page
        return result;
    }

    private def searchParamGenerator(String query, Integer from = 0, Integer size = 500){
        String params = "_search/?from=" + from + "&size=" + size + "&q='" + URLEncoder.encode(query, "UTF-8") + "'";
        return params;
    }


    def globalSearch(String query, Integer from = 0, Integer size = 500){
        def map = search(searchParamGenerator(query,from,size))
        return map;
    }


    def typeBaseSearch(String typeName, String query, Integer from = 0, Integer size = 500){
        def map = search(typeName + "/" + searchParamGenerator(query,from,size));
        return map;
    }


}
