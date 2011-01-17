package org.exoplatform.webos.services.dockbar;

import java.util.ArrayList;
import java.util.List;

import org.exoplatform.container.xml.ComponentPlugin;
import org.picocontainer.Startable;

/**
 * @author Minh Hoang TO - hoang281283@gmail.com
 *
 * Nov 4, 2010
 */
public class BaseDockbarService implements DockbarService, Startable {

	private final List<DockbarServicePlugin> dockbarServicePlugins;
	
	protected final List<DockbarIcon> commonIcons;
	
	protected final List<DockbarIcon> userIcons;
	
	protected final List<DockbarIcon> utilIcons;
	
	public BaseDockbarService() throws Exception
	{
		dockbarServicePlugins = new ArrayList<DockbarServicePlugin>();
		commonIcons = new ArrayList<DockbarIcon>();
		userIcons = new ArrayList<DockbarIcon>();
		utilIcons = new ArrayList<DockbarIcon>();
	}
	
	public void addPlugin(ComponentPlugin plugin)
	{
		if(plugin instanceof DockbarServicePlugin)
		{
			dockbarServicePlugins.add((DockbarServicePlugin)plugin);
		}
	}
	
	
	public void start() {
		for(DockbarServicePlugin plugin : dockbarServicePlugins)
		{
			this.commonIcons.addAll(plugin.getCommonIcons());
			this.userIcons.addAll(plugin.getUserIcons());
			this.utilIcons.addAll(plugin.getUtilIcons());
		}
	}

	@Override
	public List<DockbarIcon> getCommonIcons() {
		return commonIcons;
	}
	
	@Override
	public List<DockbarIcon> getUserIcons() {
		return userIcons;
	}
	
	@Override
	public List<DockbarIcon> getUtilIcons() {
		return utilIcons;
	}
	
	@Override
	public DockbarIcon getIcon(String iconName) {
		for(DockbarIcon icon : utilIcons)
		{
			if(iconName.equals(icon.getIconName()))
			{
				return icon;
			}
		}
		
		return null;
	}
	
	@Override
	public boolean hasPermission(String remoteUser, DockbarIcon icon) {
		return "Everyone".equals(icon.getAccessPermission());
	}
	
	public void stop() {

	}

}
