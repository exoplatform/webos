function UIDockbar() {
  this.curve = 3 ;
  this.weight = 2.3 ;
  this.isFirstTime = true;
  this.showDesktop = false ;
	this.arrayImage = false ;
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
	eXo.desktop.UIDockbar.resizeDockBar() ;
  
  var portletsViewer = document.getElementById("PortletsViewer") ;
  var widgetsViewer = document.getElementById("WidgetsViewer") ;
  portletsViewer.onclick = this.viewPortlets ;
  widgetsViewer.onclick = this.viewWidgets ;
} ;

UIDockbar.prototype.waitOnLoad = function(images) {
	var UIDockbar = eXo.desktop.UIDockbar;
  for (var i = 0; i < images.length; i++) {
    images[i].onmousemove = UIDockbar.animationEvt ;
    images[i].onmouseover = UIDockbar.iconOverEvt ;
    images[i].onmouseout = UIDockbar.iconOutEvt ;
  
    if(eXo.core.Browser.isIE6() && (images[i].getAttribute("altText") != "")) {
      images[i].runtimeStyle.filter = "progid:DXImageTransform.Microsoft.AlphaImageLoader(src='"+images[i].src+"', sizingMethod='scale')" ;
      images[i].src = images[i].getAttribute("altText") ;
//      images[i].alt = "" ;
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
	eXo.desktop.UIDockbar.hideNavigation() ;
	eXo.desktop.UIDockbar.reset() ;
} ;

UIDockbar.prototype.iconOverEvt = function() {
  var uiDockbar = document.getElementById("UIDockBar") ;
  var objectXInDockbar = eXo.core.Browser.findPosXInContainer(this, uiDockbar) ;
  var iconContainer = document.getElementById("IconContainer") ;
  var tooltip = this.nextSibling ;
  
	eXo.webui.UIRightClickPopupMenu.hideContextMenu("DockbarContextMenu") ;
	eXo.desktop.UIDockbar.hideNavigation() ;
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


UIDockbar.prototype.viewWidgets = function() {
	var uiPageDesktop = document.getElementById("UIPageDesktop") ;
  var children = eXo.core.DOMUtil.findChildrenByClass(uiPageDesktop, "div", "UIGadget") ; 
	for(var i = 0; i < children.length; i++) {
		if (children[i].style.display != "none" ) {
			children[i].style.display = "none" ;
		} else {
			children[i].style.display = "block" ;
		}
	}
} ;

//UIDockbar.prototype.showTooltip = function(e) {
//	window.status = (new Date()).getTime() ;
//  var UIDockbar = eXo.desktop.UIDockbar ;
//  var object = this ;
//  var uiDockbar = document.getElementById("UIDockBar") ;
//  var objectXInDockbar = eXo.core.Browser.findPosXInContainer(object, uiDockbar) ;
//  var iconContainer = document.getElementById("IconContainer") ;
//  var tooltipObjects = eXo.core.DOMUtil.findChildrenByClass(iconContainer, "span", "Tooltip") ;
//  var selectedIconIndex = UIDockbar.findIndex(object) ;
//  
//  if(UIDockbar.displayTooltip) {
//		eXo.webui.UIRightClickPopupMenu.hideContextMenu("DockbarContextMenu") ;
//    tooltipObjects[selectedIconIndex].style.display = "block" ;
//    tooltipObjects[selectedIconIndex].style.top = (-tooltipObjects[selectedIconIndex].offsetHeight) + "px" ;
//    tooltipObjects[selectedIconIndex].style.left = objectXInDockbar + "px" ;
//    UIDockbar.displayTooltip = false ;
//  } else {
//    tooltipObjects[selectedIconIndex].style.display = "none" ;
//    UIDockbar.displayTooltip = true ;
//  }
//} ;

UIDockbar.prototype.animationEvt = function(e) {
	 
  var UIDockbar = eXo.desktop.UIDockbar ;
  var curve = UIDockbar.curve ;
  var weight = UIDockbar.weight ;
  var fixBugImageElement = document.getElementById("FixBug") ;
  
  var uiPageDesktop = document.getElementById("UIPageDesktop") ;

  var selectedIconX = eXo.desktop.UIDesktop.findPosXInDesktop(this) ;
  var middleIcon = selectedIconX + (this.offsetWidth / 2) ;
  var mouseX = eXo.core.Browser.findMouseRelativeX(uiPageDesktop, e) ;

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

UIDockbar.prototype.removeDockbarIcon = function(idIcon) {
	var icon = document.getElementById(idIcon);
	if (icon) {
		var portlet = document.getElementById("UIWindow-" + idIcon.replace(/[a-zA-Z]*/, ""));
		if (portlet) portlet.style.display = "none";
		var toolTip = eXo.core.DOMUtil.findNextElementByTagName(icon, "span");
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

UIDockbar.prototype.createApplicationIcon = function(iconUrl, iconId) {
  var appIcon = document.createElement("img") ;
  appIcon.className = "Icon" ;
  appIcon.src = iconUrl ;
  appIcon.id = iconId ;
  appIcon.style.marginRight = "4px" ;
  
  if(eXo.core.Browser.isIE6()) {
    appIcon.runtimeStyle.filter = "progid:DXImageTransform.Microsoft.AlphaImageLoader(src='"+iconUrl+"', sizingMethod='scale')" ;
    appIcon.src = "/eXoResources/skin/sharedImages/Blank.gif" ;
//    appIcon.alt = "" ;
  }
  
  appIcon.onmousemove = eXo.desktop.UIDockbar.animationEvt ;
  appIcon.onmouseover = eXo.desktop.UIDockbar.showTooltip ;
  appIcon.onmouseout = eXo.desktop.UIDockbar.showTooltip ;
  
  return appIcon ;
} ;

UIDockbar.prototype.createApplicationTooltip = function(tooltip) {
  var appTooltip = document.createElement("span") ;
  appTooltip.className = "Tooltip" ;
  appTooltip.style.display = "none" ;
  appTooltip.innerHTML = tooltip ;
  
  return appTooltip ;
};

UIDockbar.prototype.initNav = function() {
	var nav = document.getElementById("DockNavigation") ;
	eXo.portal.UIExoStartMenu.buildMenu(nav) ;
};

UIDockbar.prototype.showNavigation = function(event) {
	event = event || window.event ;
  event.cancelBubble = true ;

  var uiDockbar = document.getElementById("UIDockBar") ;
	var dockNavigation = document.getElementById("DockNavigation") ;
	dockNavigation.style.display = "block" ;
	var menuItemContainer = eXo.core.DOMUtil.findFirstDescendantByClass(dockNavigation, "div", "MenuItemContainer") ;
	dockNavigation.menuItemContainer = menuItemContainer ;
	eXo.portal.UIExoStartMenu.createSlide(dockNavigation) ;
	
	eXo.core.Mouse.update(event) ;

	var fixWidthForIE7 = 0 ;
	var uiWorkingWS = document.getElementById("UIWorkingWorkspace") ;
	if(eXo.core.I18n.isLT()) {
		if (eXo.core.Browser.isIE7() && document.getElementById("UIDockBar")) {
			fixWidthForIE7 = uiWorkingWS.offsetLeft ;
		}
	} else if(eXo.core.Browser.getBrowserType() == "ie") fixWidthForIE7 = 16;
	
	menuItemContainer.style.top = -(menuItemContainer.offsetHeight) + "px" ;
	
	//TODO: add transparent border in blockMenu to fix bug blockMenu out of menuItemContainer in Vista desktopPage 
	if(eXo.core.Browser.isIE6()) {
		var blockMenu = eXo.core.DOMUtil.findFirstDescendantByClass(menuItemContainer, "div", "BlockMenu") ;
		blockMenu.style.borderBottom = "solid 1px white" ;
		blockMenu.style.filter = "chroma(color=white)"; 
	}
	
	var dockLeft = eXo.core.Browser.findPosX(dockNavigation) ;
	var dockRight = dockLeft + dockNavigation.offsetWidth ;
	var menuLeft = eXo.core.Browser.findPosX(menuItemContainer) ;
	var menuRight = menuLeft + menuItemContainer.offsetWidth ;

	if(eXo.core.I18n.isLT()) {
		var intLeft = dockLeft - menuLeft ;
		intLeft += eXo.core.Mouse.mousexInPage - eXo.core.Browser.findPosX(uiDockbar) + fixWidthForIE7 ;
		dockNavigation.style.left = intLeft + "px" ;
	} else {
		var intRight = menuRight - dockRight ;
		intRight += (eXo.core.Browser.findPosX(uiDockbar) + uiDockbar.offsetWidth - eXo.core.Mouse.mousexInPage) + fixWidthForIE7 ;
		dockNavigation.style.right = intRight + "px" ;
	}
	
	var intTop = eXo.core.Mouse.mouseyInPage - (eXo.core.Browser.findPosY(dockNavigation) - dockNavigation.offsetTop) ;
	var browserHeight = eXo.core.Browser.getBrowserHeight() ;
	if (eXo.core.Mouse.mouseyInPage - menuItemContainer.offsetHeight < 0) {
		intTop = uiDockbar.offsetHeight - ((browserHeight - menuItemContainer.offsetHeight)/2) ;
	}
	dockNavigation.style.top = intTop + "px";
	
};

UIDockbar.prototype.hideNavigation = function(event) {
	var nav = document.getElementById("DockNavigation") ;
	nav.style.display = "none" ;
};

eXo.desktop.UIDockbar = new UIDockbar() ;