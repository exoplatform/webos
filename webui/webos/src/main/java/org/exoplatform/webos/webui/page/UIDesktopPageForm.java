/**
 * 
 */
package org.exoplatform.webos.webui.page;

import org.exoplatform.portal.application.PortalRequestContext;
import org.exoplatform.portal.config.DataStorage;
import org.exoplatform.portal.config.model.Page;
import org.exoplatform.portal.webui.page.UIPage;
import org.exoplatform.portal.webui.util.PortalDataMapper;
import org.exoplatform.portal.webui.util.Util;
import org.exoplatform.portal.webui.workspace.UIMaskWorkspace;
import org.exoplatform.portal.webui.workspace.UIPortalApplication;
import org.exoplatform.portal.webui.workspace.UIWorkingWorkspace;
import org.exoplatform.webui.event.Event;
import org.exoplatform.webui.event.EventListener;
import org.exoplatform.webui.event.Event.Phase;
import org.exoplatform.webui.form.UIFormInputSet;
import org.exoplatform.webui.form.UIFormStringInput;
import org.exoplatform.webui.form.UIFormTabPane;
import org.exoplatform.webui.config.annotation.ComponentConfig;
import org.exoplatform.webui.config.annotation.ComponentConfigs;
import org.exoplatform.webui.config.annotation.EventConfig;
import org.exoplatform.webui.core.lifecycle.UIFormLifecycle;

/**
 * Created by The eXo Platform SAS
 * @author Kien Nguyen
 *		   nguyenanhkien2a@gmail.com
 *
 *  Jan 3, 2011
 */
@ComponentConfigs({
	   @ComponentConfig(lifecycle = UIFormLifecycle.class, template = "system:/groovy/webui/form/UIFormTabPane.gtmpl", events = {
	      @EventConfig(listeners = UIDesktopPageForm.SaveActionListener.class),
	      @EventConfig(listeners = UIMaskWorkspace.CloseActionListener.class, phase = Phase.DECODE)})
	      })
public class UIDesktopPageForm extends UIFormTabPane {
	private UIDesktopPage uiDesktopPage_;
	
	public UIDesktopPageForm() throws Exception {
		super("UIDesktopPageForm");
		UIFormInputSet uiSettingSet = new UIFormInputSet("DesktopPageSetting");
		uiSettingSet.addUIFormInput(new UIFormStringInput("title", "title", null));
		this.addUIFormInput(uiSettingSet);
		setSelectedTab(uiSettingSet.getId());
		setActions(new String[]{"Save", "Close"});
	}
	
	public UIDesktopPage getUIDesktopPage() {
		return uiDesktopPage_;
	}
	
	public void setValues(UIDesktopPage uiPage) throws Exception
	{
	   uiDesktopPage_ = uiPage;
	   Page page = (Page) PortalDataMapper.buildModelObject(uiDesktopPage_);
	   invokeGetBindingBean(page);
	   page.setShowMaxWindow(true);
       getUIStringInput("title").setValue(uiPage.getTitle());
	}
    
	static public class SaveActionListener extends EventListener<UIDesktopPageForm>
    {
       public void execute(Event<UIDesktopPageForm> event) throws Exception
       {
    	  UIDesktopPageForm uiDesktopPageForm = event.getSource();
    	  PortalRequestContext pcontext = Util.getPortalRequestContext();

    	  //Hide pop up
          UIMaskWorkspace uiMaskWorkspace = uiDesktopPageForm.getAncestorOfType(UIMaskWorkspace.class);
          uiMaskWorkspace.setUIComponent(null);
          uiMaskWorkspace.setShow(false);
          pcontext.addUIComponentToUpdateByAjax(uiMaskWorkspace);
          
          UIPage uiPage = uiDesktopPageForm.getUIDesktopPage();
          if (uiPage == null)
             return;
          
    	  Page page = new Page();
          page.setPageId(uiPage.getPageId());
          uiDesktopPageForm.invokeSetBindingBean(page);
          page.setOwnerType(uiPage.getOwnerType());
          page.setFactoryId(uiPage.getFactoryId());
          
          DataStorage dataService = uiDesktopPageForm.getApplicationComponent(DataStorage.class);
          dataService.save(page);       
          
          //Update page
          uiPage.setTitle(page.getTitle());
          pcontext.setFullRender(true);
          pcontext.addUIComponentToUpdateByAjax(Util.getUIPortalApplication().getChild(UIWorkingWorkspace.class));
       }
    }
}
