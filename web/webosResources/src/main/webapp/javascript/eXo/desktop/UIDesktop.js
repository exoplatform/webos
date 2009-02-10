function UIDesktop() {
};

UIDesktop.prototype.init = function() {
	var pageDesktop = document.getElementById("UIPageDesktop") ;
	if(pageDesktop) {
		eXo.desktop.UIDesktop.fixDesktop() ;
		eXo.desktop.UIDockbar.init() ;
	  var uiWindows = eXo.core.DOMUtil.findChildrenByClass(pageDesktop, "div", "UIWindow") ;
	  for(var i = 0; i < uiWindows.length; i++) {
	  	if(uiWindows[i].isFirstTime == false)	continue ;
			//fix display scroll in first time
//	  	var blockResizes = eXo.core.DOMUtil.findDescendantsByClass(uiWindows[i], "div", "UIResizableBlock");
//			if (blockResizes.length > 1) blockResizes[0].style.overflow = "hidden" ;
			eXo.desktop.UIDesktop.backupWindowProperties(uiWindows[i]);
	  }
	}
};

UIDesktop.prototype.fixDesktop = function() {
  var pageDesktop = document.getElementById("UIPageDesktop") ;
  var browserHeight = eXo.core.Browser.getBrowserHeight() ;
  if(pageDesktop) pageDesktop.style.height = browserHeight + "px" ;
  window.scroll(0,0);
  setTimeout("eXo.desktop.UIDockbar.resizeDockBar()", 0) ; 
};

//TODO DungHM
UIDesktop.prototype.resetZIndex = function(windowObject) {
  var windowsInDesktop = eXo.core.DOMUtil.getChildrenByTagName(windowObject.parentNode, "div") ;
  var maxZIndex = windowObject.style.zIndex ;
 
  var uiPopupWindow = eXo.core.DOMUtil.findDescendantsByClass(windowObject.parentNode,'div','UIPopupWindow') ;
  for (var i = 0; i < uiPopupWindow.length; i ++) {
 		if (uiPopupWindow[i].style.display == "block") return ;
  }
  
  for(var i = 0; i < windowsInDesktop.length; i++) {
  	if((windowsInDesktop[i].className.indexOf("UIWindow") >= 0) || (windowsInDesktop[i].className.indexOf("UIWidget") >= 0)) {
  		
	    if(parseInt(maxZIndex) < parseInt(windowsInDesktop[i].style.zIndex)) {
	      maxZIndex = windowsInDesktop[i].style.zIndex ;
	    }
	    //TODO: tan.pham: test for fix bug WEBOS-119: 2 portlets may have same zIndex when reload page
//	    if(parseInt(windowsInDesktop[i].style.zIndex) >= parseInt(windowObject.style.zIndex)) {
//	      windowsInDesktop[i].style.zIndex = parseInt(windowsInDesktop[i].style.zIndex) - 1 ;
//	      
//	    }
  	}
  	if (windowsInDesktop[i].style.zIndex < 0) windowsInDesktop[i].style.zIndex = 1 ;
  }
  windowObject.style.zIndex = parseInt(maxZIndex) + 1 ;
};

UIDesktop.prototype.isMaxZIndex = function(object) {
	var isMax = false ;
	var DOMUtil = eXo.core.DOMUtil ;
	var uiPageDesktop = document.getElementById("UIPageDesktop") ;
	//var uiDockbar = document.getElementById("UIDockBar") ;
	var desktopApps = DOMUtil.getChildrenByTagName(uiPageDesktop, "div") ;
	
	var maxZIndex = parseInt(object.style.zIndex) ;
	for(var i = 0; i < desktopApps.length; i++) {
		if((desktopApps[i].className.indexOf("UIWindow") >= 0) || (desktopApps[i].className.indexOf("UIWidget") >= 0)) {
			if(parseInt(desktopApps[i].style.zIndex) > maxZIndex) maxZIndex = desktopApps[i].style.zIndex ;
		}
	}
	
	if(object.style.zIndex == maxZIndex) isMax = true ;
	return isMax ;
};

UIDesktop.prototype.showHideWindow = function(uiWindow, clickedElement, mode) {
	var DOMUtil = eXo.core.DOMUtil ;
  if(typeof(uiWindow) == "string") this.object = document.getElementById(uiWindow) ;
  else this.object = uiWindow ;
  
  var portletId = (this.object ? this.object.id : uiWindow).replace(/^UIWindow-/, "") ;
  var portletFrag = DOMUtil.findFirstDescendantByClass(this.object, "div", "PORTLET-FRAGMENT") ;
  
  var dockIcon = document.getElementById("DockItem"+portletId) ;
  var isMaxZIndex = eXo.desktop.UIDesktop.isMaxZIndex(this.object) ;
  if(!isMaxZIndex) eXo.desktop.UIDesktop.resetZIndex(this.object) ;
  var numberOfFrame = 10 ; 
  if(mode == "QUIT") {
  	if(this.object.style.display == "block") {
	    eXo.animation.ImplodeExplode.implode(this.object, clickedElement, "UIPageDesktop", numberOfFrame, false) ;
	    this.object.isShowed = false ;
  	}
    eXo.desktop.UIWindow.saveWindowProperties(this.object, "QUIT");
  	if(dockIcon) DOMUtil.removeClass(dockIcon, "ShowIcon") ;
  	return ;
  }
  if(this.object.style.display == "block") {
    eXo.animation.ImplodeExplode.implode(this.object, clickedElement, "UIPageDesktop", numberOfFrame, false) ;
    eXo.desktop.UIWindow.saveWindowProperties(this.object, "HIDE");
    this.object.isShowed = false ;
    if(dockIcon) DOMUtil.addClass(dockIcon, "ShowIcon") ;
  } else {
	  if(DOMUtil.getChildrenByTagName(portletFrag, "div").length < 1) {
	  	var uiPage = eXo.core.DOMUtil.findAncestorByClass(this.object, "UIPage") ;
			var uiPageIdNode = eXo.core.DOMUtil.findFirstDescendantByClass(uiPage, "div", "id") ;
			containerBlockId = uiPageIdNode.innerHTML ;
			var params = [{name : "objectId", value: portletId}] ;
			ajaxGet(eXo.env.server.createPortalURL(containerBlockId, "ShowPortlet", true, params)) ;
	  }
  	
    var uiDockBar = document.getElementById("UIDockBar") ;
		var uiPageDesktop	= document.getElementById("UIPageDesktop") ;
    eXo.desktop.UIDockbar.resetDesktopShowedStatus(uiPageDesktop, uiDockBar) ;
    eXo.animation.ImplodeExplode.explode(this.object, clickedElement, "UIPageDesktop", numberOfFrame, false) ;
    eXo.desktop.UIWindow.saveWindowProperties(this.object, "SHOW");
	//	document.getElementById(uiWindow).style.visibility = "visible";
		this.object.isShowed = true ;
		if(dockIcon) DOMUtil.addClass(dockIcon, "ShowIcon") ;  
  	//TODO MinhJS: fix bug for don't apply style css in IE6 in first time.
  	if(eXo.core.Browser.isIE6()) {
  		this.object.style.filter =  "" ;
  	}
		//fix display scroll in first time.
//		var blockResize = eXo.core.DOMUtil.findFirstDescendantByClass(this.object, "div", "UIResizableBlock");
//		if (blockResize) blockResize.style.overflow = "hidden" ;
  }
//  eXo.desktop.UIDockbar.containerMouseOver() ;
};

UIDesktop.prototype.findPosXInDesktop = function(object, isRTL) {
  var uiPageDesktop = eXo.core.DOMUtil.findAncestorByClass(object, "UIPageDesktop") ;
  return eXo.core.Browser.findPosXInContainer(object, uiPageDesktop, isRTL) ;
} ;

UIDesktop.prototype.findPosYInDesktop = function(object) {
  var uiPageDesktop = eXo.core.DOMUtil.findAncestorByClass(object, "UIPageDesktop") ;
  var posYUIPageDesktop = eXo.core.Browser.findPosY(uiPageDesktop) ;
  var posYObject = eXo.core.Browser.findPosY(object) ;
  return (posYObject - posYUIPageDesktop) ;
} ;

UIDesktop.prototype.backupWindowProperties = function(uiWindow) {
  uiWindow.originalX = eXo.desktop.UIDesktop.findPosXInDesktop(uiWindow, eXo.core.I18n.isRT()) ;
  uiWindow.originalY = eXo.desktop.UIDesktop.findPosYInDesktop(uiWindow) ;
  uiWindow.originalW = uiWindow.offsetWidth ;
  uiWindow.originalH = uiWindow.offsetHeight ;
  uiWindow.style.visibility = "visible" ;
  if(uiWindow.style.display == "") uiWindow.style.display = "none" ;
  
  uiWindow.isShowed = false ;
  uiWindow.isFirstTime = false ;
} ;

UIDesktop.prototype.removeApp = function(uri) {
	var result = ajaxAsyncGetRequest(uri, false) ;
	if(result == "OK") {
		var appId = uri.substr(uri.lastIndexOf("=") + 1) ;
		eXo.desktop.UIDesktop.removeWindow("UIWindow-" + appId) ;
		eXo.desktop.UIDockbar.removeDockbarIcon("DockItem" + appId) ;
	}
};

UIDesktop.prototype.removeWindow = function (idWindow) {
	var uiWindow = document.getElementById(idWindow); 
	if(uiWindow) eXo.core.DOMUtil.removeElement(uiWindow);
};

UIDesktop.prototype.removeWindowContent = function (idWindow) {
	var uiWindow = document.getElementById("UIWindow-" + idWindow) ;
	if(uiWindow) {
		var portletFrag = eXo.core.DOMUtil.findFirstDescendantByClass(uiWindow, "div", "PORTLET-FRAGMENT") ;
		for(var i = 0; i < portletFrag.childNodes.length; i++) {
			portletFrag.removeChild(portletFrag.childNodes[i]) ;
		}
		portletFrag.innerHTML = "<span></span>" ;
		eXo.desktop.UIDesktop.showHideWindow(uiWindow, document.getElementById("DockItem"+idWindow), "QUIT") ;
	}
};

eXo.desktop.UIDesktop = new UIDesktop() ;
