function UIWindow() {
	this.maxIndex = 0;
}

UIWindow.prototype.init = function(popup, isShow, posX, posY) {
	//TODO: tan.pham: Require by JS. will remove when webos using Gatein 3.0.1 with perfect javascriptService
	eXo.core.Loader.register('eXo.webui.UIPopup', '/eXoResources/javascript/eXo/webui/UIPopup.js');
  eXo.core.Loader.init("eXo.webui.UIPopup");
	this.superClass = eXo.webui.UIPopup ;
	if(typeof(popup) == "string") popup = document.getElementById(popup) ;
	if(popup == null) return ;

	var DOMUtil = eXo.core.DOMUtil ;
	var uiApplication = DOMUtil.findFirstDescendantByClass(popup, "div", "UIApplication") ;
	if(!uiApplication) return ;

	if(popup.style.zIndex == "") popup.style.zIndex = ++eXo.webui.UIPopup.zIndex ;
	
	popup.onmousedown = this.mousedownOnPopup ;

	var windowPortletInfo = DOMUtil.findFirstDescendantByClass(popup, "div", "WindowPortletInfo") ;
	this.superClass.setPosition(popup, posX, posY, eXo.core.I18n.isRT()) ;
	try {
		windowPortletInfo.onmousedown = this.initDND ;
	} catch(err) {
		alert("Error In DND: " + err) ;
	}

	var minimizedIcon = DOMUtil.findFirstDescendantByClass(popup, "div", "MinimizedIcon") ;
	minimizedIcon.onmouseup = this.minimizeWindowEvt ; 
	var maximizedIcon = DOMUtil.findFirstDescendantByClass(popup, "div", "MaximizedIcon") ;
	maximizedIcon.onmouseup = this.maximizeWindowEvt ;
	windowPortletInfo.ondblclick = function() {eXo.desktop.UIWindow.maximizeWindowEvt.call(maximizedIcon)};
	var resizeArea = DOMUtil.findFirstDescendantByClass(popup, "div", "ResizeArea") ;
	resizeArea.onmousedown = this.startResizeWindowEvt ;
 	/*
	 * minh.js.exo
	 * check maximize portlet in first time;
	 * posX == posY == 0;
	 */
	if (posX == posY && posX == 0) {
 		popup.style.width = "100%";
 		popup.maximized = true;
 		this.posX = 15;
 		this.posY = 15;
 		this.originalWidth = 800;
 		this.originalHeight = 400;
 		maximizedIcon.className = "ControlIcon RestoreIcon";
 		maximizedIcon.title = maximizedIcon.getAttribute("modeTitle");
		setTimeout(eXo.desktop.UIWindow.toForcus, 1000);
  }
  popup.resizeCallback = new eXo.core.HashMap() ;
} ;

UIWindow.prototype.fixHeight = function(portletId) {
	var portlet = document.getElementById(portletId) ;
	var delta = portlet.parentNode.offsetHeight - portlet.offsetHeight ;
	var resizeObj = eXo.core.DOMUtil.findDescendantsByClass(portlet, 'div', 'UIResizableBlock') ;
	for(var i = 0; i < resizeObj.length; i++) {
		var nHeight = parseInt(resizeObj[i].offsetHeight) + delta ;
		if (nHeight < 0 ) nHeight = "0px" ;
		resizeObj[i].style.height = nHeight + 'px' ;
	}
} ;

UIWindow.prototype.toForcus = function() {
	//reset zIndex all widget when in case them over to maximize portlet.
	var DOMUtil = eXo.core.DOMUtil ;
	var uiPageDesktop = document.getElementById("UIPageDesktop") ;
	var uiWidgets =  DOMUtil.findDescendantsByClass(uiPageDesktop, "div", "UIGadget");
	if (uiWidgets.length) {
		for (var i = 0; i < uiWidgets.length; i ++ ) {
			uiWidgets[i].style.zIndex = 1;
		}
	}
} ;

UIWindow.prototype.mousedownOnPopup = function(evt) {
	var isMaxZIndex = eXo.desktop.UIDesktop.isMaxZIndex(this) ;
	if(!isMaxZIndex)	eXo.desktop.UIDesktop.resetZIndex(this) ;
} ;

UIWindow.prototype.maximizeWindowEvt = function(evt) {
	var DOMUtil = eXo.core.DOMUtil ;
	var portletWindow = DOMUtil.findAncestorByClass(this, "UIResizeObject") ;
	
	var uiWindow = eXo.desktop.UIWindow ;
	var uiPageDesktop = document.getElementById("UIPageDesktop") ;
  var desktopHeight = uiPageDesktop.offsetHeight  ;
  var uiResizableBlock = DOMUtil.findDescendantsByClass(portletWindow, "div", "UIResizableBlock") ;
  if(portletWindow.maximized) {
    portletWindow.maximized = false ;
    portletWindow.style.top = uiWindow.posY + "px" ;
    if(eXo.core.I18n.isLT()) portletWindow.style.left = uiWindow.posX + "px" ;
    else portletWindow.style.right = uiWindow.posX + "px" ;
    portletWindow.style.width = uiWindow.originalWidth + "px" ;
		for(var i = 0; i < uiResizableBlock.length; i++) {
  	 if (uiResizableBlock[i].originalHeight) {
      uiResizableBlock[i].style.height = uiResizableBlock[i].originalHeight + "px" ;
  	 } else	{
  	 		uiResizableBlock[i].style.height = 400 + "px" ;
  	 }
    }
    this.className = "ControlIcon MaximizedIcon" ;
    
  } else {
    uiWindow.backupObjectProperties(portletWindow, uiResizableBlock) ;
    portletWindow.style.top = "0px" ;
    if(eXo.core.I18n.isLT()) portletWindow.style.left = "0px" ;
    else portletWindow.style.right = "0px" ;
    portletWindow.style.width = "100%" ;
		portletWindow.style.height = "auto" ;
    var delta = desktopHeight - portletWindow.clientHeight ;
    for(var i = 0; i < uiResizableBlock.length; i++) {
			uiResizableBlock[i].style.height =  (parseInt(uiResizableBlock[i].clientHeight) + delta) + "px" ;
    }
    portletWindow.style.height = portletWindow.clientHeight + "px" ;
    portletWindow.maximized = true ;
    this.className = "ControlIcon RestoreIcon" ;
  }
	eXo.desktop.UIWindow.saveWindowProperties(portletWindow) ;
  // Re initializes the scroll tabs managers on the page
	eXo.portal.UIPortalControl.initAllManagers() ;
} ;

UIWindow.prototype.minimizeWindowEvt =	function(evt) {
	var DOMUtil = eXo.core.DOMUtil ;
	var popup = DOMUtil.findAncestorByClass(this, "UIDragObject") ;
	var windows = DOMUtil.getChildrenByTagName(popup.parentNode, "div") ;
	var index = 0 ;
	for(var j = 0; j < windows.length; j++) {
		if(popup == windows[j]) {
			index = j ;
			break ;
		}
	}
	var iconContainer = document.getElementById("IconContainer") ;
	var children = DOMUtil.findChildrenByClass(iconContainer, "img", "Icon") ;
	eXo.desktop.UIDesktop.showHideWindow(popup, children[index + 1]) ;
} ;
	
UIWindow.prototype.startResizeWindowEvt = function(evt) {
	var portletWindow = eXo.core.DOMUtil.findAncestorByClass(this, "UIResizeObject") ;
	var uiWindow = eXo.desktop.UIWindow ;
	if(portletWindow.maximized || uiWindow.portletWindow) return ;

	if(!evt) evt = window.event ;
	var uiPageDesktop = document.getElementById("UIPageDesktop") ;
	var uiApplication = eXo.core.DOMUtil.findFirstDescendantByClass(portletWindow, "div", "UIApplication") ;
	var hasResizableClass = eXo.core.DOMUtil.hasDescendantClass(uiApplication, "UIResizableBlock")	;
	if(hasResizableClass) uiApplication.style.overflow = "hidden" ;

	var portlet = eXo.core.DOMUtil.getChildrenByTagName(uiApplication, "div")[0] ;
	uiWindow.resizableObject = eXo.core.DOMUtil.findDescendantsByClass(portletWindow, "div", "UIResizableBlock") ;
	uiWindow.initMouseX = evt.clientX ;
	uiWindow.initMouseY = evt.clientY ;
	uiWindow.backupObjectProperties(portletWindow, uiWindow.resizableObject) ;
	uiWindow.portletWindow = portletWindow ;
	uiPageDesktop.onmousemove = uiWindow.resizeWindowEvt ;
	uiPageDesktop.onmouseup = uiWindow.endResizeWindowEvt ;
} ;

UIWindow.prototype.resizeWindowEvt = function(evt) {
	if(!evt) evt = window.event ;
	var uiWindow = eXo.desktop.UIWindow;
	var uiPageDesktop = document.getElementById("UIPageDesktop") ;
	if(eXo.core.I18n.isLT()) {
		var deltaX = evt.clientX - uiWindow.initMouseX ;
		var deltaY = evt.clientY - uiWindow.initMouseY ;
	} else {
		var deltaX = uiWindow.initMouseX - evt.clientX ;
		var deltaY = evt.clientY - uiWindow.initMouseY ;
	}
	var uiApplication = eXo.core.DOMUtil.findFirstDescendantByClass(uiWindow.portletWindow, "div", "UIApplication") ;
	uiWindow.portletWindow.style.width = Math.max(200, (uiWindow.originalWidth + deltaX)) + "px" ;
	for(var i = 0; i < uiWindow.resizableObject.length; i++) {
		uiWindow.resizableObject[i].style.height = Math.max(10,(uiWindow.resizableObject[i].originalHeight + deltaY)) + "px" ;
	}
} ;

UIWindow.prototype.endResizeWindowEvt = function(evt) {
	var uiWindow = eXo.desktop.UIWindow.portletWindow ;
	for(var name in uiWindow.resizeCallback.properties) {
 		var method = uiWindow.resizeCallback.get(name) ;
		if (typeof(method) == "function") method(evt) ;
	}
  eXo.desktop.UIWindow.saveWindowProperties(uiWindow);
	// Re initializes the scroll tabs managers on the page
	eXo.portal.UIPortalControl.initAllManagers() ;
	eXo.desktop.UIWindow.portletWindow = null ;
	eXo.desktop.UIWindow.resizableObject = null ;
	this.onmousemove = null ;
	this.onmouseup = null ;
	
} ;  

UIWindow.prototype.backupObjectProperties = function(windowPortlet, resizableComponents) {
	var UIWindow = eXo.desktop.UIWindow ;
	for(var i = 0; i < resizableComponents.length; i++) {
		resizableComponents[i].originalWidth = resizableComponents[i].offsetWidth ;
		resizableComponents[i].originalHeight = resizableComponents[i].offsetHeight ;
	}
  
  UIWindow.posX = eXo.desktop.UIDesktop.findPosXInDesktop(windowPortlet, eXo.core.I18n.isRT()) ;
	UIWindow.posY = eXo.desktop.UIDesktop.findPosYInDesktop(windowPortlet) ;
	UIWindow.originalWidth = windowPortlet.offsetWidth ;
	UIWindow.originalHeight = windowPortlet.offsetHeight ;
} ;

UIWindow.prototype.initDND = function(e) {
	
	var DOMUtil = eXo.core.DOMUtil ;
	var DragDrop = eXo.core.DragDrop ;
	var clickBlock = this ;
	var dragBlock = DOMUtil.findAncestorByClass(this, "UIDragObject") ;
	var uiPageDeskTop = document.getElementById("UIPageDesktop") ;
	var maxIndex = eXo.desktop.UIWindow.maxIndex ;
	//fix zIndex for refesh
	var dragObjects = DOMUtil.findDescendantsByClass(uiPageDeskTop, "div", "UIDragObject") ;
	if (dragObjects.length > 0) {
      var isMaxZIndex = eXo.desktop.UIDesktop.isMaxZIndex(this) ;
	   if(!isMaxZIndex)	eXo.desktop.UIDesktop.resetZIndex(this) ;
	}

	
	// Can drag n drop only when the window is NOT maximized
  if(!dragBlock.maximized) {
	  
		var uiApplication = DOMUtil.findFirstDescendantByClass(dragBlock, "div", "UIApplication") ;
		var hiddenElements = new Array() ;
		
		DragDrop.initCallback = function(dndEvent) {
	  	// A workaround to make the window go under the workspace panel during drag
	  	if (eXo.core.Browser.getBrowserType() == "mozilla" && DOMUtil.getStyle(uiApplication, "overflow") == "auto") {
	  		hiddenElements.push(uiApplication) ;
	  		uiApplication.style.overflow = "hidden" ;
	  	}
	  	uiAppDescendants = DOMUtil.findDescendantsByTagName(uiApplication, "div") ;
	  	for (var i = 0; i < uiAppDescendants.length; i++) {
	  		if (DOMUtil.getStyle(uiAppDescendants[i], "overflow") == "auto") {
	  			hiddenElements.push(uiAppDescendants[i]) ;
	  			uiAppDescendants[i].style.overflow = "hidden" ;
	  		}
	  	}
	  } ;
	
	  DragDrop.dragCallback = function(dndEvent) {
	    var dragObject = dndEvent.dragObject ;
	    var dragObjectY = eXo.core.Browser.findPosY(dragObject) ;
	    var browserHeight = eXo.core.Browser.getBrowserHeight() ;
	    var browserWidth = eXo.core.Browser.getBrowserWidth() ;
	    var mouseX = eXo.core.Browser.findMouseXInPage(dndEvent.backupMouseEvent) ;
	    	    
	    if(dragObjectY < 0) {
	      dragObject.style.top = "0px" ;
	      document.onmousemove = DragDrop.onDrop ; /*Fix Bug On IE6*/
	    }
	    
	    if(dragObjectY > (browserHeight - 25)) {
			//WEBOS-362 dragObjectY is not the same with dragObject.style.top
	      dragObject.style.top = (browserHeight - 25 - dragObjectY + parseInt(dragObject.style.top)) + "px" ;
	      document.onmousemove = DragDrop.onDrop ; /*Fix Bug On IE6*/
	    }
	    
		  var uiPageDesktop = document.getElementById("UIPageDesktop") ;
		  var uiPageDesktopX = eXo.core.Browser.findPosX(uiPageDesktop) ;
		  
		  /*Fix Bug On IE7, It's always double the value returned*/
		  if(eXo.core.Browser.isIE7()) {
		  	uiPageDesktopX = uiPageDesktopX / 2 ;
		  }
		  
	    if((mouseX < uiPageDesktopX) || (mouseX > browserWidth)) {
	      document.onmousemove = DragDrop.onDrop ;
	    }
	    
	  } ;
	
	  DragDrop.dropCallback = function(dndEvent) {
	  	var dragObject = dndEvent.dragObject ;
	  	
		  //TODO Lambkin: Save properties of window
		  eXo.desktop.UIWindow.saveWindowProperties(dragBlock) ;
	  	for (var i = 0; i < hiddenElements.length; i++) {
	  		hiddenElements[i].style.overflow = "auto" ;
	  	}
	  } ;
	  DragDrop.init(null, clickBlock, dragBlock, e) ;
	}
} ;

UIWindow.prototype.saveWindowProperties = function(object, appStatus) {
	var DOMUtil = eXo.core.DOMUtil ;
	var uiPage = DOMUtil.findAncestorByClass(object, "UIPage") ;
	containerBlockId = uiPage.id.replace(/^UIPage-/,"") ;
	var uiResizableBlock = DOMUtil.findFirstDescendantByClass(object, "div", "UIResizableBlock") ;
	
	var params ;
	if(!appStatus) {
	  params = [
	  	{name : "objectId", value : object.id.replace(/^UIWindow-/, "")},
	  	{name : "posX", value : (eXo.core.I18n.isLT() ? parseInt(object.style.left) : parseInt(object.style.right)) },
	  	{name : "posY", value : parseInt(object.style.top)},
	  	{name : "zIndex", value : object.style.zIndex},
	  	{name : "windowWidth", value : object.offsetWidth},
		  {name : "windowHeight", value : uiResizableBlock.offsetHeight}
	  ] ;
	} else {
		params = [
	  	{name : "objectId", value : object.id.replace(/^UIWindow-/, "")},
		  {name : "appStatus", value : appStatus}
	  ] ;
	}

	ajaxAsyncGetRequest(eXo.env.server.createPortalURL(containerBlockId, "SaveWindowProperties", true, params), true) ;
} ;

eXo.desktop.UIWindow = new UIWindow() ;
