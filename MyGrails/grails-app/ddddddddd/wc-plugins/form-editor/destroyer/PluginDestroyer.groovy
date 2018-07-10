import com.webcommander.util.PluginDestroyUtil

class PluginDestroyer {

    PluginDestroyUtil destroyUtil = PluginDestroyUtil.getInstance();
    public void destroy() {
        try {
            destroyUtil.removeWidget('form')
                        .removeEmailTemplates('form-submit-to-email')
                        .dropTable('form_form_field', 'form_field_form_field', 'form_submission_data',
                        'form_submission', 'form_field_form_extra_prop', 'form_extra_prop', 'form', 'field_condition', 'form_field')
        }catch (Exception e) {
            e.printStackTrace()
        }
        finally {
            destroyUtil.closeConnection()
        }
    }
}