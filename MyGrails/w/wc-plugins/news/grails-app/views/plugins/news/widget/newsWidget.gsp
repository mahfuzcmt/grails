
<g:applyLayout name="_widget">

    <script src="${app.systemResourceBaseUrl()}plugins/news/js/ui-widgets/newsticker.js"></script>
    <%
        def indexes = [] as Set;
        Random rand = new Random();
        int max = newses.size();
        if (config.transition_direction == "random") {
            while(indexes.size() <= max - 1 ){
                indexes.add(rand.nextInt(max));
            }
        } else {
            (0..max-1).each {
                indexes.add(it);
            }
        }
    %>
    <g:set var="newsPerPhase" value="${config.news_per_phase? config.news_per_phase.toInteger() :2 }"/>
    <g:set var="newsPerPhase" value="${config.news_transition == "no_transition" ? newses.size() : newsPerPhase }"/>
    <g:set var="transition" value="${config.news_transition?config.news_transition:"horizontal_scroll"}"/>
    <g:set var="speed" value="${config.transition_speed?config.transition_speed+"000":"8000"}" />
    <g:set var="noOfChar" value="${config.noOfChar ? config.noOfChar.toInteger() : 150}"/>
    <g:set var="n" value="${newses?.size()? newses.size() : 0 }"/>
    <div class="news-list" speed="${speed}" transition="${transition}" direction="" height="${config.height}" style="overflow: hidden; ${config.news_transition != "no_transition" ? 'height: ' +
            config.height + 'px' : ''}">
       <g:set var="i" value="${0}"/>
       <g:while test="${i<n}">
           <g:set var="j" value="${0}" />
           <div class="item" style="overflow: auto;">
                <g:while test="${j < newsPerPhase && i < n}">
                    <div>
                        <div class="news-title" >${newses[indexes[i]]?.title}</div>
                        <div class="news-description">
                            <g:if test="${newses[indexes[i]].summary}" >
                                ${newses[indexes[i]]?.summary?.length() > noOfChar ? newses[indexes[i]]?.summary?.substring(0,noOfChar): newses[indexes[i]]?.summary }
                                <span class="read-more"><a href="${app.relativeBaseUrl()}article/${newses[indexes[i]]?.article?.url}"><g:message code="read.more"/></a></span>
                            </g:if>
                            <g:elseif test="${newses[indexes[i]].article?.summary}">
                                ${newses[indexes[i]]?.article?.summary?.replaceAll("<(.|\n)*?>", '').length() > noOfChar ?
                                        newses[indexes[i]]?.article?.summary?.replaceAll("<(.|\n)*?>", '').substring(0, noOfChar-1) : newses[indexes[i]]?.article?.summary}
                                <span class="read-more"><a href="${app.relativeBaseUrl()}article/${newses[indexes[i]]?.article?.url}"><g:message code="read.more"/></a></span>
                            </g:elseif>
                            <g:else >
                                ${newses[indexes[i]]?.article?.content?.replaceAll("<(.|\n)*?>", '').length() > noOfChar ?
                                        newses[indexes[i]]?.article?.content?.replaceAll("<(.|\n)*?>", '').substring(0, noOfChar-1) : newses[indexes[i]]?.article?.content}
                                <span class="read-more"><a href="${app.relativeBaseUrl()}article/${newses[indexes[i]]?.article?.url}"><g:message code="read.more"/></a></span>
                            </g:else>
                        </div>
                    </div>
                    <g:set var="j" value="${j+1}"/>
                    <g:set var="i" value="${i+1}"/>
                </g:while>
           </div>
       </g:while>
    </div>
</g:applyLayout>