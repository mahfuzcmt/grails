import com.webcommander.util.PluginDestroyUtil

class PluginDestroyer {

    PluginDestroyUtil destroyUtil = PluginDestroyUtil.getInstance();
    public void destroy() {
        try {
            destroyUtil.removeWidget('liveChat')
                    .removeSiteConfig('live_chat')
                    .removeEmailTemplates('live-chat-offline-email', 'send-chat-to-mail')
                    .removeSiteMessage('chat.with.us', 'need.help', 'send.us.message')
                    .dropTable('chat_message_notification_args', 'chat_chat_tag', 'chat_message', 'chat', 'chat_agent', 'chat_tag')
        }catch (Exception e) {
            e.printStackTrace()
        }
        finally {
            destroyUtil.closeConnection();
        }
    }
}