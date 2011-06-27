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
			eXo.desktop.UIDesktop.backupWindowProperties(uiWindows[i]);
	  }

	  pageDesktop.onmousedown = eXo.desktop.UIDesktop.showContextMenu;
	}
};

UIDesktop.prototype.showContextMenu = function(evt) {
	if (!evt) evt = window.event;
	var targetID = (evt.target || evt.srcElement).id;

  if ("UIPageDesktop" !== targetID)
		return;
	eXo.webui.UIRightClickPopupMenu.clickRightMouse(evt, this, "UIDesktopContextMenu", "", null, 5);
};

UIDesktop.prototype.closeAll= function() {
	var pageDesktop = document.getElementById("UIPageDesktop") ;
  var windowList = eXo.core.DOMUtil.findDescendantsByClass(pageDesktop, "DIV", "UIWindow");
	var blockList = [];
	for (var i = 0; i < windowList.length; i++) {
		if (eXo.core.DOMUtil.getStyle(windowList[i], "display") == "block") {
			eXo.desktop.UIDesktop.removeWindowContent(windowList[i].id.replace("UIWindow-", ""));
			blockList.push(windowList[i]);
		}
	}
	//ImplodeExplode.js can only run with 1 window each time
	for (i = 1; i < blockList.length; i++) {
		if (eXo.core.DOMUtil.getStyle(blockList[i], "display") == "block") {
			blockList[i].style.display = "none";
		}
	}
};

UIDesktop.prototype.fixDesktop = function() {
  var pageDesktop = document.getElementById("UIPageDesktop") ;
  var browserHeight = eXo.core.Browser.getBrowserHeight() ;
  if(pageDesktop) pageDesktop.style.height = browserHeight - eXo.core.Browser.findPosY(pageDesktop) + "px" ;
  window.scroll(0,0);
  setTimeout("eXo.desktop.UIDockbar.resizeDockBar()", 0) ; 
};

//TODO DungHM
UIDesktop.prototype.resetZIndex = function(windowObject) {
   var uiPageDesktop = document.getElementById("UIPageDesktop");
   var windowsInDesktop = eXo.core.DOMUtil.findDescendantsByClass(uiPageDesktop, "div", "UIWindow");
   var maxZIndex = windowObject.style.zIndex ;
 
   var uiPopupWindow = eXo.core.DOMUtil.findDescendantsByClass(windowObject.parentNode,'div','UIPopupWindow') ;
   for (var i = 0; i < uiPopupWindow.length; i ++) {
 		if (uiPopupWindow[i].style.display == "block") return ;
   }
  
   for(var i = 0; i < windowsInDesktop.length; i++) {
      if (windowsInDesktop[i].style.zIndex < 0) windowsInDesktop[i].style.zIndex = 1;

      if(parseInt(maxZIndex) < parseInt(windowsInDesktop[i].style.zIndex)) {
         maxZIndex = windowsInDesktop[i].style.zIndex ;
      }
      //TODO: tan.pham: test for fix bug WEBOS-119: 2 portlets may have same zIndex when reload page
      //	    if(parseInt(windowsInDesktop[i].style.zIndex) >= parseInt(windowObject.style.zIndex)) {
      //	      windowsInDesktop[i].style.zIndex = parseInt(windowsInDesktop[i].style.zIndex) - 1 ;
      //
      //}
   }
   eXo.desktop.UIWindow.maxIndex = parseInt(maxZIndex) + 1;
   windowObject.style.zIndex = eXo.desktop.UIWindow.maxIndex;
};

UIDesktop.prototype.isMaxZIndex = function(object) {
	var DOMUtil = eXo.core.DOMUtil ;
	var uiPageDesktop = document.getElementById("UIPageDesktop");
	var desktopApps = DOMUtil.findDescendantsByClass(uiPageDesktop, "div", "UIWindow") ;

   var maxZIndex = parseInt(object.style.zIndex) ;
   for (var i = 0; i < desktopApps.length; i++) {
      if(desktopApps[i] != object && desktopApps[i].style.display == "block") {
         if (maxZIndex <= parseInt(desktopApps[i].style.zIndex)) {
            return false;
         }
      }
   }

   return true;
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
  	}
  	this.object.isShowed = false ;
    eXo.desktop.UIWindow.saveWindowProperties(this.object, "QUIT");
  	if(dockIcon) DOMUtil.removeClass(dockIcon, "ShowIcon") ;
  	return ;
  }
  if(this.object.style.display == "block") {
    eXo.animation.ImplodeExplode.implode(this.object, clickedElement, "UIPageDesktop", numberOfFrame, false) ;
    eXo.desktop.UIWindow.saveWindowProperties(this.object, "HIDE");
    if(dockIcon) DOMUtil.addClass(dockIcon, "ShowIcon") ;
  } else {
	  if(DOMUtil.getChildrenByTagName(portletFrag, "div").length < 1) {
	  	var uiPage = eXo.core.DOMUtil.findAncestorByClass(this.object, "UIPage") ;
			containerBlockId = uiPage.id.replace(/^UIPage-/,"") ;
			var params = [{name : "objectId", value: portletId}] ;
			ajaxGet(eXo.env.server.createPortalURL(containerBlockId, "ShowPortlet", true, params)) ;
	  }
  	
    var uiDockBar = document.getElementById("UIDockBar") ;
		var uiPageDesktop	= document.getElementById("UIPageDesktop") ;
    eXo.desktop.UIDockbar.resetDesktopShowedStatus(uiPageDesktop, uiDockBar) ;
    eXo.animation.ImplodeExplode.explode(this.object, clickedElement, "UIPageDesktop", numberOfFrame, false) ;
    eXo.desktop.UIWindow.saveWindowProperties(this.object, "SHOW");
		this.object.isShowed = true ;
		if(dockIcon) DOMUtil.addClass(dockIcon, "ShowIcon") ;  
  	//TODO MinhJS: fix bug for don't apply style css in IE6 in first time.
  	if(eXo.core.Browser.isIE6()) {
  		this.object.style.filter =  "" ;
  	}
  }
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
  
  var portletId = uiWindow.id.replace(/^UIWindow-/, "");
  var dockIcon = document.getElementById("DockItem"+portletId);
  if(dockIcon) {
  	if(eXo.core.DOMUtil.hasClass(dockIcon, "ShowIcon"))
  		uiWindow.isShowed = true;
  }
  
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

UIDesktop.prototype.setDesktopBackground = function (imageURL) {
	var pageDesktop = document.getElementById("UIPageDesktop") ;
	if (!pageDesktop) return;

	if (imageURL) {
		imageURL = "url('" + imageURL + "') no-repeat center center";
	} else if (navigator.userAgent.indexOf("MSIE") >= 0) {
		pageDesktop.style.backgroundAttachment = "";
		pageDesktop.style.backgroundImage = "";
		pageDesktop.style.backgroundRepeat = "";
		pageDesktop.style.backgroundPositionX = "";
		pageDesktop.style.backgroundPositionY = "";
		pageDesktop.style.backgroundColor = "";
		return;
	}

	pageDesktop.style.background = imageURL;
};

eXo.desktop.UIDesktop = new UIDesktop() ;
