package com.webcommander.plugin.live_chat.manager

import com.webcommander.plugin.live_chat.ChatAgent
import com.webcommander.util.AppUtil

class AgentManager {
    private static List<Map> AGENT_HOLDER = Collections.synchronizedList(new ArrayList<Map>());

    public static getAgentCount() {
        return AGENT_HOLDER.size();
    }

    public static pushAgent(Long agentId) {
        Map agent = [
            id: agentId,
            loginTime: new Date().gmt(),
            sessionId: AppUtil.session.id
        ];
        AGENT_HOLDER.add(agent);
    }

    public static Boolean removeAgent(Long id, String sessionId) {
       return AGENT_HOLDER.removeAll {
            it.id == id && it.sessionId == sessionId
        };
    }

    public static Boolean removeAgent(Long id) {
       return AGENT_HOLDER.removeAll {
            it.id == id
        };
    }

    public static List getAgents(Map params) {
        int max = params.max.toInteger();
        int fromIndex = params.offset.toInteger();
        int toIndex = (max == -1 || (max + fromIndex) >= AGENT_HOLDER.size()) ? AGENT_HOLDER.size() : (fromIndex + max);
        return AGENT_HOLDER.subList(fromIndex, toIndex);
    }

    public static List getAgentIds() {
        return AGENT_HOLDER.id;
    }

    public static Integer getLoginCountByAgent(Long agentId) {
        return AGENT_HOLDER.findAll {
            it.id == agentId
        }.size();
    }
}
