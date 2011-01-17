/**
 * 
 */
package org.exoplatform.webos.services.dockbar;

import java.util.List;

/**
 * @author Minh Hoang TO - hoang281283@gmail.com
 *
 * Nov 4, 2010
 */
public interface DockbarService {

	public List<DockbarIcon> getCommonIcons();
	
	public List<DockbarIcon> getUserIcons();
	
	public List<DockbarIcon> getUtilIcons();
	
	public DockbarIcon getIcon(String iconName);
	
	public boolean hasPermission(String remoteUser, DockbarIcon icon);
}
