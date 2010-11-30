/**
 * 
 */
package org.exoplatform.webos.services.dockbar;

import java.util.List;

import org.exoplatform.container.xml.ComponentPlugin;

/**
 * @author Minh Hoang TO - hoang281283@gmail.com
 *
 * Nov 4, 2010
 */
public abstract class DockbarServicePlugin extends ComponentPlugin {

	abstract public List<DockbarIcon> getCommonIcons();
	
	abstract public List<DockbarIcon> getUserIcons();
	
	abstract public List<DockbarIcon> getUtilIcons();
}
