/**
 * 
 */
package org.exoplatform.webos.services.dockbar;

import org.exoplatform.portal.config.model.Page;

/**
 * @author Minh Hoang TO - hoang281283@gmail.com
 *
 * Nov 4, 2010
 */
public interface DockbarService {

   /**
    * Inject applications (configured to appear on desktop page) into model of a desktop page.
    *
    * This method is invoked as there is page creation event. Call to this method has only effect
    * on desktop page
    *
    * @param page
    * @throws Exception
    */
	public void injectDockbarApps(Page page) throws Exception;
}
