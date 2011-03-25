function UIDockbar() {
  this.curve = 3 ;
  this.weight = 2.3 ;
  this.isFirstTime = true;
  this.showDesktop = false ;
	this.arrayImage = false ;
	
	this.itemStyleClass = "MenuItem" ;
  this.itemOverStyleClass = "MenuItemOver" ;
  this.containerStyleClass = "MenuItemContainer" ;
  
  //TODO: tan.pham: Require by JS. will remove when webos using Gatein 3.0.1 with perfect javascriptService
  eXo.core.Loader.register('eXo.webui.UIPopupMenu', '/eXoResources/javascript/eXo/webui/UIPopupMenu.js');
  eXo.core.Loader.init("eXo.webui.UIPopupMenu");
  this.superClass = eXo.webui.UIPopupMenu ;
};

UIDockbar.prototype.init = function() {
  var UIDockbar = eXo.desktop.UIDockbar ;
  var uiDockbar = document.getElementById("UIDockBar") ;
  if(!uiDockbar) return ;
  var imgObject = eXo.core.DOMUtil.findDescendantsByClass(uiDockbar, "img", "Icon") ;
	UIDockbar.arrayImage = imgObject;
  
  uiDockbar.defaultIconSize = 40 ;
  uiDockbar.originalBGDockbarHeight = 47 ;
  /*If this value is changed, need to synchronous with (.UIPageDesktop .UIDockBar .DockbarCenter) class*/
  
  if(imgObject.length > 0 && imgObject[0].onmousemove == undefined) this.isFirstTime = true ;
  
  if(this.isFirstTime == true) {
		setTimeout("eXo.desktop.UIDockbar.waitOnLoad(eXo.desktop.UIDockbar.arrayImage)", 0);
    this.isFirstTime = false ;
  }
  
  uiDockbar.originalDockbarHeight = uiDockbar.offsetHeight ;
  window.setTimeout(function() {uiDockbar.style.visibility = "visible"}, 50);
  
  var portletsViewer = document.getElementById("PortletsViewer") ;
  var gadgetsViewer = document.getElementById("GadgetsViewer") ;
  portletsViewer.onclick = this.viewPortlets ;
  gadgetsViewer.onclick = this.viewGadgets ;
} ;

UIDockbar.prototype.waitOnLoad = function(images) {
	var UIDockbar = eXo.desktop.UIDockbar;
  for (var i = 0; i < images.length; i++) {
    images[i].onmousemove = UIDockbar.animationEvt ;
    images[i].onmouseover = UIDockbar.iconOverEvt ;
    images[i].onmouseout = UIDockbar.iconOutEvt ;
  
    if(eXo.core.Browser.isIE6()) {
      images[i].runtimeStyle.filter = "progid:DXImageTransform.Microsoft.AlphaImageLoader(src='"+images[i].src+"', sizingMethod='scale')" ;
      images[i].src = "/eXoResources/skin/sharedImages/Blank.gif" ;

    }
  }
};

UIDockbar.prototype.startDockBarEvt = function(evt) {
	evt.cancelBubble = true ;
	document.oncontextmenu =  document.body.oncontextmenu = function() {return false} ;
	var uiPageDesktop = document.getElementById("UIPageDesktop") ;
	uiPageDesktop.onmouseover = eXo.desktop.UIDockbar.endDockBarEvt ;   
} ;

UIDockbar.prototype.endDockBarEvt = function() {
	this.onmouseover = null ;
	document.oncontextmenu = document.body.oncontextmenu = function() {return true} ;
	eXo.webui.UIRightClickPopupMenu.hideContextMenu("DockbarContextMenu") ;
	eXo.desktop.UIDockbar.reset() ;
} ;

UIDockbar.prototype.iconOverEvt = function() {
  var uiDockbar = document.getElementById("UIDockBar") ;
  var objectXInDockbar = eXo.core.Browser.findPosXInContainer(this, uiDockbar) ;
  var iconContainer = document.getElementById("IconContainer") ;
  var tooltip = this.nextSibling ;
  
	eXo.webui.UIRightClickPopupMenu.hideContextMenu("DockbarContextMenu") ;
  tooltip.style.display = "block" ;
  tooltip.style.top = (-tooltip.offsetHeight) + "px" ;
  tooltip.style.left = objectXInDockbar + "px" ;
}

UIDockbar.prototype.iconOutEvt = function() {
  var uiDockbar = document.getElementById("UIDockBar") ;
  var objectXInDockbar = eXo.core.Browser.findPosXInContainer(this, uiDockbar) ;
  var iconContainer = document.getElementById("IconContainer") ;
  var tooltipObjects = eXo.core.DOMUtil.findChildrenByClass(iconContainer, "span", "Tooltip") ;
  this.nextSibling.style.display = "none" ;
}

UIDockbar.prototype.viewPortlets = function() {
	var uiPageDesktop = document.getElementById("UIPageDesktop") ;
  var children = eXo.core.DOMUtil.findDescendantsByClass(uiPageDesktop, "div", "UIWindow") ; 
	var srcMonitoringImage = "/eXoResources/skin/sharedImages/Icon80x80/Hide"+this.id+".png" ;
  var srcPortletsViewerImage = "/eXoResources/skin/sharedImages/Icon80x80/Show"+this.id+".png" ;
	eXo.desktop.UIDockbar.showDesktop = false ;
	for(var i = 0; i < children.length; i++) {
	    if(eXo.core.DOMUtil.hasDescendantClass(children[i], "UIGadgetPortlet"))
	       continue;
		if (children[i].style.display == "block" ) {
			children[i].style.display = "none" ;
			children[i].isShowed = true ;
			eXo.desktop.UIDockbar.showDesktop = false ;
		} else {
			if (children[i].isShowed)	{
				children[i].style.display = "block" ;
				eXo.desktop.UIDockbar.showDesktop = true ;
			}
		}
	}
	
	if (eXo.desktop.UIDockbar.showDesktop) {
		if (eXo.core.Browser.isIE6()) {
			this.runtimeStyle.filter = "progid:DXImageTransform.Microsoft.AlphaImageLoader(src='" + srcMonitoringImage + "', sizingMethod='scale')" ;
		} else {
			this.src = srcMonitoringImage ;
		}
	} else {
		if (eXo.core.Browser.isIE6()) {
			this.runtimeStyle.filter = "progid:DXImageTransform.Microsoft.AlphaImageLoader(src='" + srcPortletsViewerImage + "', sizingMethod='scale')" ;
		} else {
			this.src = srcPortletsViewerImage ;
		}
	}
} ;


UIDockbar.prototype.viewGadgets = function() {
	var uiPageDesktop = document.getElementById("UIPageDesktop") ;
  var children = eXo.core.DOMUtil.findDescendantsByClass(uiPageDesktop, "div", "UIGadgetPortlet") ; 
	for(var i = 0; i < children.length; i++) {
	    var gadgetWindow = eXo.core.DOMUtil.findAncestorByClass(children[i], "UIWindow");
		if (gadgetWindow.style.display != "none" ) {
			gadgetWindow.style.display = "none" ;
		} else {
			gadgetWindow.style.display = "block" ;
		}
	}
} ;

UIDockbar.prototype.animationEvt = function(e) {
	 
  var UIDockbar = eXo.desktop.UIDockbar ;
  var curve = UIDockbar.curve ;
  var weight = UIDockbar.weight ;
  var fixBugImageElement = document.getElementById("FixBug") ;
  
  var uiPageDesktop = document.getElementById("UIPageDesktop") ;

  var selectedIconX = eXo.desktop.UIDesktop.findPosXInDesktop(this, eXo.core.I18n.isRT()) ;
  var middleIcon = selectedIconX + (this.offsetWidth / 2) ;
  var mouseX = eXo.core.Browser.findMouseRelativeX(uiPageDesktop, e) ;
  if(eXo.core.I18n.isRT()) mouseX = uiPageDesktop.offsetWidth - mouseX ;

  var distanceWeight =  (middleIcon - mouseX)/(2*curve*(middleIcon - selectedIconX)) ;
  
  var selectedIconIndex = UIDockbar.findIndex(this) ;
  var icons = eXo.core.DOMUtil.findChildrenByClass(this.parentNode, "img", "Icon") ;
  var uiDockbar = document.getElementById("UIDockBar") ;
  var dockbarCenter = document.getElementById("DockbarCenter") ;
  
  fixBugImageElement.style.height = uiDockbar.defaultIconSize + (uiDockbar.defaultIconSize*(weight - 1)) + "px" ;
  
  dockbarCenter.style.height = uiDockbar.originalBGDockbarHeight + (uiDockbar.defaultIconSize*(weight - 1)) + "px" ;
  for(var i = 0; i < icons.length; i++) {
    var deltaCurve = Math.abs(selectedIconIndex - i) ;
    var size = uiDockbar.defaultIconSize ;
    if(deltaCurve < curve) {
      if(i == selectedIconIndex) {
        size = Math.round(uiDockbar.defaultIconSize + 
               uiDockbar.defaultIconSize * (weight - 1) * ((curve - deltaCurve) / curve - Math.abs(distanceWeight))) ;
        distanceWeight *= -1 ;
      } else {
        size = Math.round(uiDockbar.defaultIconSize + 
        uiDockbar.defaultIconSize * (weight - 1) * ((curve - deltaCurve) / curve + distanceWeight)) ;
      }
    }
        
    icons[i].style.width = size + "px" ;
    icons[i].style.height = size + "px" ;
  }
  
  UIDockbar.resizeDockBar() ;

} ;

UIDockbar.prototype.findIndex = function(object) {
  var icons = eXo.core.DOMUtil.findChildrenByClass(object.parentNode, "img", "Icon") ;
  for(var i = 0; i < icons.length; i++) {
    if(icons[i] == object) return i ;
  }
} ;

UIDockbar.prototype.removeDockbarIcon = function(iconId) {
	var icon = document.getElementById(iconId);
	if (icon) {
		var toolTip = eXo.core.DOMUtil.findNextElementByTagName(icon, "span");
		if(icon.previousSibling.nodeType == 3) eXo.core.DOMUtil.removeElement(icon.previousSibling) ;
		eXo.core.DOMUtil.removeElement(icon);
		eXo.core.DOMUtil.removeElement(toolTip);
	}
};

UIDockbar.prototype.reset = function() {
  var UIDockbar = eXo.desktop.UIDockbar ;
  var uiDockbar = document.getElementById("UIDockBar") ;
  var dockbarCenter = document.getElementById("DockbarCenter") ;
  dockbarCenter.style.height = uiDockbar.originalBGDockbarHeight + "px" ;
  
  var iconContainer = document.getElementById("IconContainer") ;
  var icons = eXo.core.DOMUtil.findChildrenByClass(iconContainer, "img", "Icon") ;
  for(var i = 0; i < icons.length; i++) {
    icons[i].style.width = uiDockbar.defaultIconSize + "px" ;
    icons[i].style.height = uiDockbar.defaultIconSize + "px" ;
  }
  var fixBugImageElement = document.getElementById("FixBug") ;
  fixBugImageElement.style.height = uiDockbar.defaultIconSize + "px" ;
  
  UIDockbar.resizeDockBar() ;
} ;

UIDockbar.prototype.resizeDockBar = function() {
  var uiPageDesktop = document.getElementById("UIPageDesktop") ;
  var uiDockbar = document.getElementById("UIDockBar") ;
  var iconContainer = document.getElementById("IconContainer") ;
  
  var icons = eXo.core.DOMUtil.findChildrenByClass(iconContainer, "img", "Icon") ;
  var widthItemControl = 0 ;
  for(var i = 0; i < icons.length; i++) {
    var iconWidth = icons[i].offsetWidth ;
    if(uiDockbar.defaultIconSize && (iconWidth < uiDockbar.defaultIconSize)) iconWidth = uiDockbar.defaultIconSize ; 
    widthItemControl = (widthItemControl + iconWidth + 5) ;
  }
    
  var separators = eXo.core.DOMUtil.findChildrenByClass(iconContainer, "img", "Separator") ;
  var totalWidthSeparators = 0 ;
  for(var i = 0; i < separators.length; i++) {
    totalWidthSeparators = totalWidthSeparators + separators[i].offsetWidth + 10 ;
    /* 10 is the total of margin left and right of each separator*/
  }
  
  iconContainer.style.width = (widthItemControl + totalWidthSeparators + 10) + "px" ;
  
  if(!uiDockbar.totalPadding) {
	  var pr = iconContainer.parentNode ;
	  var totalPadding = 0 ;
	  while(pr) {
	  	var pad = parseInt(eXo.core.DOMUtil.getStyle(pr, "paddingRight")) ;
	  	if(!isNaN(pad)) totalPadding += pad ;
	  	pad = parseInt(eXo.core.DOMUtil.getStyle(pr, "paddingLeft")) ;
	  	if(!isNaN(pad)) totalPadding += pad ;
	  	if(pr.parentNode == uiDockbar) break;
	  	pr = pr.parentNode ;
	  }
	  uiDockbar.totalPadding = totalPadding ;
  }
  
  uiDockbar.style.width = (iconContainer.offsetWidth + uiDockbar.totalPadding) + "px" ;
  uiDockbar.style.left = ((uiPageDesktop.offsetWidth - uiDockbar.offsetWidth) / 2) + "px" ;
} ;

UIDockbar.prototype.resetDesktopShowedStatus = function(uiPageDesktop, uiDockBar) {
  var uiPageDesktopChildren = eXo.core.DOMUtil.getChildrenByTagName(uiPageDesktop, "div") ;
  for(var i = 0; i < uiPageDesktopChildren.length; i++) {
    if(uiPageDesktopChildren[i].isShowed == true && uiPageDesktopChildren[i].style.display == "none") {
      uiPageDesktopChildren[i].isShowed = false ;
    }
  }
  if(this.showDesktop) {
    var portletsViewer = eXo.core.DOMUtil.findDescendantById(uiDockBar, "PortletsViewer") ;
    var blankImage = portletsViewer.src ;
    var srcMonitoringImage = "/eXoResources/skin/sharedImages/Icon80x80/HidePortletsViewer.png" ;
    if(eXo.core.Browser.isIE6()) {
      portletsViewer.runtimeStyle.filter = "progid:DXImageTransform.Microsoft.AlphaImageLoader(src='" + srcMonitoringImage + "', sizingMethod='scale')" ;
      portletsViewer.src = blankImage ;
    } else {
      portletsViewer.src = srcMonitoringImage ;
    }
    this.showDesktop = false ;
  }
} ;

UIDockbar.prototype.onMenuItemOver = function(event) {
	this.className = eXo.desktop.UIDockbar.itemOverStyleClass ;
	if (this.menuItemContainer) {
		var menuItemContainer = this.menuItemContainer ;
		menuItemContainer.style.display = "block" ;
		menuItemContainer.style.visibility = "" ;
		var x = this.offsetWidth ;
		var posRight = eXo.core.Browser.getBrowserWidth() - eXo.core.Browser.findPosX(this) - this.offsetWidth ; 
	  var rootX = (eXo.core.I18n.isLT() ? eXo.core.Browser.findPosX(this) : posRight) ;
		if (x + menuItemContainer.offsetWidth + rootX > eXo.core.Browser.getBrowserWidth()) {
    	x -= (menuItemContainer.offsetWidth + this.offsetWidth) ;
	  }
	  
	  //padWidth is used for improvement PORTAL-2827
	 	if(eXo.core.I18n.isLT()) {
		  var padWidth = eXo.core.Browser.findPosX(this) - eXo.core.Browser.findPosX(menuItemContainer.offsetParent) ;
	 		menuItemContainer.style.left = x + padWidth + "px" ;
	 	}	else {
	 		var padWidth = (eXo.core.Browser.findPosX(menuItemContainer.offsetParent) + menuItemContainer.offsetParent.offsetWidth) 
	 		    - (eXo.core.Browser.findPosX(this) + this.offsetWidth) ;
	 		menuItemContainer.style.right = x + padWidth + "px" ;
	 	}
		eXo.desktop.UIDockbar.createSlide(this);
    eXo.desktop.UIDockbar.superClass.pushVisibleContainer(this.menuItemContainer.id) ;
    
    var y ;
	 	var browserHeight = eXo.core.Browser.getBrowserHeight() ;
	 	
		var parentMenu = eXo.core.DOMUtil.findAncestorByClass(this, "MenuItemContainer") ;
		var blockMenu = eXo.core.DOMUtil.findAncestorByClass(this, "BlockMenu") ;
 		var objTop = eXo.core.Browser.findPosY(this) ;
 		y = objTop - eXo.core.Browser.findPosY(parentMenu) - blockMenu.scrollTop ;
 		if(y + menuItemContainer.offsetHeight + 15 > browserHeight) {
 			y += (this.offsetHeight - menuItemContainer.offsetHeight) ;
 			if(y <= 0) y = 1 ;
 		}
		menuItemContainer.style.top = y + "px" ;
	}
};

UIDockbar.prototype.onMenuItemOut = function(event) {
	this.className = eXo.desktop.UIDockbar.itemStyleClass ;
	if (this.menuItemContainer) {
    eXo.desktop.UIDockbar.superClass.pushHiddenContainer(this.menuItemContainer.id) ;
    eXo.desktop.UIDockbar.superClass.popVisibleContainer() ;
    eXo.desktop.UIDockbar.superClass.setCloseTimeout() ;
	}
};

UIDockbar.prototype.createSlide = function(menuItem) {
	var menuItemContainer = menuItem.menuItemContainer ;
	var icon = eXo.core.DOMUtil.findFirstDescendantByClass(menuItem, "div", "Icon") ;
	// fix width for menuContainer, only IE.
	if (!menuItemContainer.resized) eXo.desktop.UIDockbar.setContainerSize(menuItemContainer);
	
 	var blockMenu = eXo.core.DOMUtil.findFirstDescendantByClass(menuItemContainer, "div", "BlockMenu") ;
	var parentMenu = blockMenu.parentNode;
	var topElement = eXo.core.DOMUtil.findFirstChildByClass(parentMenu, "div", "TopNavigator") ;
 	var bottomElement = eXo.core.DOMUtil.findFirstChildByClass(parentMenu, "div", "BottomNavigator") ;

	var menuContainer = eXo.core.DOMUtil.findFirstDescendantByClass(blockMenu, "div", "MenuContainer") ;
	
	if (!blockMenu.id) blockMenu.id = "eXo" + new Date().getTime() + Math.random().toString().substring(2) ;
	
	var browserHeight = eXo.core.Browser.getBrowserHeight() ;
	if (menuContainer.offsetHeight + 64 > browserHeight) {
		var curentHeight = browserHeight - 64;
		blockMenu.style.height = curentHeight + "px" ;
		blockMenu.style.overflowY = "hidden" ;
		topElement.style.display = "block" ;
		bottomElement.style.display = "block" ;

		if(!menuContainer.curentHeight || (menuContainer.curentHeight != curentHeight)) {
			eXo.desktop.UIDockbar.initSlide(menuContainer, curentHeight) ;
		}
		topElement.onmousedown = function(evt) {
			if(!evt) evt = window.event ;
      evt.cancelBubble = true;
			eXo.portal.VerticalScrollManager.scrollComponent(blockMenu.id, true, 15) ;
		};
		topElement.onmouseup = function(evt) {
			if(!evt) evt = window.event ;
      evt.cancelBubble = true;
      eXo.portal.VerticalScrollManager.cancelScroll() ;
		};
		topElement.onclick = function(event) {
			event = event || window.event ;
			event.cancelBubble = true ;
		};
		
		bottomElement.onmousedown = function(evt) {
			if(!evt) evt = window.event ;
			evt.cancelBubble = true;
			eXo.portal.VerticalScrollManager.scrollComponent(blockMenu.id, false, 15) ;
		};
		bottomElement.onmouseup = function(evt) {
			if(!evt) evt = window.event ;
			evt.cancelBubble = true;
      eXo.portal.VerticalScrollManager.cancelScroll() ;
		};			
		bottomElement.onclick = function(event) {
			event = event || window.event ;
			event.cancelBubble = true ;
		};
  } else {
  	blockMenu.scrollTop = 0 ;
		blockMenu.style.height = menuContainer.offsetHeight + "px" ;
		blockMenu.style.overflowY = "" ;
		menuContainer.curentHeight = null;
		topElement.style.display = "none" ;
		bottomElement.style.display = "none" ;
  }
};

UIDockbar.prototype.setContainerSize = function(menuItemContainer) {
  var menuCenter = eXo.core.DOMUtil.findFirstDescendantByClass(menuItemContainer, "div", "StartMenuML") ;
  var menuTop = eXo.core.DOMUtil.findFirstDescendantByClass(menuItemContainer, "div", "StartMenuTL") ;
  var decorator = eXo.core.DOMUtil.findFirstDescendantByClass(menuTop, "div", "StartMenuTR") ;
  var menuBottom = menuTop.nextSibling ;
  while (menuBottom.className != "StartMenuBL") menuBottom = menuBottom.nextSibling ;
  var w = menuCenter.offsetWidth - decorator.offsetLeft ;
  if(eXo.core.Browser.isIE7() && eXo.core.I18n.isRT()) {
  	w = menuCenter.offsetWidth ;
  }
  menuTop.style.width = w + "px" ;
  menuBottom.style.width = w + "px" ;
  menuCenter.style.width = w + "px" ;
  menuItemContainer.resized = true ;
};

UIDockbar.prototype.initSlide = function(menuContainer, clipBottom) {
	menuContainer.curentHeight = clipBottom ;
	menuContainer.style.top = 0 + "px" ;
};

eXo.desktop.UIDockbar = new UIDockbar() ;
