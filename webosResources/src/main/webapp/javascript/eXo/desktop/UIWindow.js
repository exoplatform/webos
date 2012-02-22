eXo.desktop.UIWindow = {
   maxIndex : 0,
   init : function(popup, posX, posY) {
      this.superClass = eXo.webui.UIPopup ;
      if(typeof(popup) == "string") popup = document.getElementById(popup) ;
      if(popup == null) return ;

      var uiWindow = xj(popup);
      var uiApplication = uiWindow.find('div.PORTLET-FRAGMENT');
      if(!uiApplication) return ;
      
      if (uiWindow.css("z-index") == "auto") uiWindow.css("z-index", ++eXo.webui.UIPopup.zIndex);
      uiWindow.bind("mousedown", this.mousedownOnPopup);
      
      var windowBar = uiWindow.children("div.WindowBarLeft");
      this.superClass.setPosition(popup, posX, posY, eXo.core.I18n.isRT()) ;
      try {
         if (!popup.maximized) {
            this.initDND(windowBar, uiWindow);
         }
      }
      catch(err) {
         alert("Error In DND: " + err);
      }
      
      var minimizedIcon = windowBar.find("div.MinimizedIcon");
      minimizedIcon.bind("mouseup", this.minimizeWindowEvt);
      var maximizedIcon = windowBar.find("div.MaximizedIcon");
      maximizedIcon.bind("mouseup", this.maximizeWindowEvt);
      windowBar.bind("dblclick", function() {eXo.desktop.UIWindow.maximizeWindowEvt.call(maximizedIcon)});
      var resizeArea = uiWindow.children("div.BottomDecoratorLeft").find("div.ResizeArea");
      resizeArea.bind("mousedown", this.startResizeWindowEvt);
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
         setTimeout(eXo.desktop.UIWindow.toFocus, 1000);
      }
      popup.resizeCallback = new eXo.core.HashMap();
      // TODO REMOVED
      $(".UIToolbarContainer").append('<div id="debug">DEBUG</div>');
   },

   initDND : function(windowBar, window) {
      eXo.core.DragDrop2.init(windowBar[0], window);

      var DOMUtil = eXo.core.DOMUtil;
      var uiApplication = window.find("div.UIApplication");// DOMUtil.findFirstDescendantByClass(window, "div", "UIApplication") ;
      var hiddenElements = new Array() ;
      window.onDrag = function(nx, ny, ex, ey, e) {
         var dragObjectPosition = window.position();
         var dragObjectY = dragObjectPosition.top;
         var browserHeight = eXo.core.Browser.getBrowserHeight() ;
         var browserWidth = eXo.core.Browser.getBrowserWidth() ;
         
         var uiPageDesktopHeight = $("#UIPageDesktop").height();
         if (dragObjectY < 0) {
            window.css("top", "0px");
            if (ey < 1)
            {
               this.endDND();
            }
         }

         if(dragObjectY > (uiPageDesktopHeight - 33)) {
            window.css("top", uiPageDesktopHeight - 33);
            if (ey > browserHeight) {
               this.endDND();
            }
         }
         
         if((ex < 0) || (ex > browserWidth)) {
            this.endDND();
         }
         var msg = 'dragObjectY = ' + dragObjectY + ' browserHeight=' + (browserHeight) + " ey = " + ey;
         $("#debug").text(msg);
      };

      window.onDragEnd = function(x, y, clientX, clientY) {
         eXo.desktop.UIWindow.saveWindowProperties(window) ;
         for (var i = 0; i < hiddenElements.length; i++) {
            hiddenElements[i].style.overflow = "auto" ;
         }
      };
      
      window.endDND = function() {
         xj(document).bind("mousemove", eXo.core.DragDrop2.end);
      };
   },
   
   toFocus : function() {
      //reset zIndex all widget when in case them over to maximize portlet.
      var DOMUtil = eXo.core.DOMUtil ;
      var uiPageDesktop = document.getElementById("UIPageDesktop") ;
      var uiWidgets =  DOMUtil.findDescendantsByClass(uiPageDesktop, "div", "UIGadget");
      if (uiWidgets.length) {
         for (var i = 0; i < uiWidgets.length; i ++ ) {
            uiWidgets[i].style.zIndex = 1;
         }
      }
   },
   
   mousedownOnPopup : function(evt) {
      var isMaxZIndex = eXo.desktop.UIDesktop.isMaxZIndex(this) ;
      if(!isMaxZIndex) {
         eXo.desktop.UIDesktop.resetZIndex(this) ;
         eXo.desktop.UIWindow.saveWindowProperties(this) ;
      }
   },
   
   maximizeWindowEvt : function(evt) {
      var DOMUtil = eXo.core.DOMUtil;
      var portletWindow = DOMUtil.findAncestorByClass(this, "UIResizeObject");
      
      var uiWindow = eXo.desktop.UIWindow;
      var uiPageDesktop = document.getElementById("UIPageDesktop");
      var desktopHeight = uiPageDesktop.offsetHeight;
      var uiResizableBlock = DOMUtil.findDescendantsByClass(portletWindow, "div", "UIResizableBlock");
      if (portletWindow.maximized) {
         portletWindow.maximized = false;
         portletWindow.style.top = uiWindow.posY + "px";
         if (eXo.core.I18n.isLT())
            portletWindow.style.left = uiWindow.posX + "px";
         else
            portletWindow.style.right = uiWindow.posX + "px";
         portletWindow.style.width = uiWindow.originalWidth + "px";
         portletWindow.style.height = null;
         for ( var i = 0; i < uiResizableBlock.length; i++) {
            if (uiResizableBlock[i].originalHeight) {
               uiResizableBlock[i].style.height = uiResizableBlock[i].originalHeight + "px";
            } else {
               uiResizableBlock[i].style.height = 400 + "px";
            }
         }
         this.className = "ControlIcon MaximizedIcon";
         
      } else {
         uiWindow.backupObjectProperties(portletWindow, uiResizableBlock);
         portletWindow.style.top = "0px";
         if (eXo.core.I18n.isLT())
            portletWindow.style.left = "0px";
         else
            portletWindow.style.right = "0px";
         portletWindow.style.width = "100%";
         portletWindow.style.height = "auto";
         var delta = desktopHeight - portletWindow.clientHeight;
         for ( var i = 0; i < uiResizableBlock.length; i++) {
            uiResizableBlock[i].style.height = (parseInt(uiResizableBlock[i].clientHeight) + delta) + "px";
         }
         
         portletWindow.style.height = portletWindow.clientHeight + "px";
         portletWindow.maximized = true;
         this.className = "ControlIcon RestoreIcon";
      }
      eXo.desktop.UIWindow.saveWindowProperties(portletWindow);
      // Re initializes the scroll tabs managers on the page
      eXo.portal.UIPortalControl.initAllManagers();
   },
   
   minimizeWindowEvt : function(evt) {
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
   },
   
   startResizeWindowEvt : function(evt) {
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
   },
   
   resizeWindowEvt : function(evt) {
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
   },
   
   endResizeWindowEvt : function(evt) {
      if (!evt) evt = window.event;
      if (evt.stopPropagation) evt.stopPropagation();
      evt.cancelBubble = true;	
      
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
      
   },  
   
   backupObjectProperties : function(windowPortlet, resizableComponents) {
      var UIWindow = eXo.desktop.UIWindow ;
      for(var i = 0; i < resizableComponents.length; i++) {
         resizableComponents[i].originalWidth = resizableComponents[i].offsetWidth ;
         resizableComponents[i].originalHeight = resizableComponents[i].offsetHeight ;
      }
      
      UIWindow.posX = eXo.desktop.UIDesktop.findPosXInDesktop(windowPortlet, eXo.core.I18n.isRT()) ;
      UIWindow.posY = eXo.desktop.UIDesktop.findPosYInDesktop(windowPortlet) ;
      UIWindow.originalWidth = windowPortlet.offsetWidth ;
      UIWindow.originalHeight = windowPortlet.offsetHeight ;
   },
   
   
   
   saveWindowProperties : function(object, appStatus) {
      var DOMUtil = eXo.core.DOMUtil ;
      var uiPage = $(".UIPage");
      containerBlockId = uiPage.attr("id").replace(/^UIPage-/,"");
      var uiResizableBlock = object.find("div.UIResizableBlock");
      
      var params ;
      if(!appStatus) {
         params = [
                   {name : "objectId", value : object.attr("id").replace(/^UIWindow-/, "")},
                   {name : "posX", value : (eXo.core.I18n.isLT() ? parseInt(object.css("left")) : parseInt(object.css("right"))) },
                   {name : "posY", value : parseInt(object.css("top"))},
                   {name : "zIndex", value : object.css("zIndex")},
                   {name : "windowWidth", value : object.offsetWidth},
                   {name : "windowHeight", value : uiResizableBlock.offsetHeight}
                   ] ;
      } else {
         params = [
                   {name : "objectId", value : object.id.replace(/^UIWindow-/, "")},
                   {name : "appStatus", value : appStatus}
                   ] ;
         if (appStatus == "SHOW") {
            params.push({name : "zIndex", value : object.style.zIndex});
         }
      }
      
      ajaxAsyncGetRequest(eXo.env.server.createPortalURL(containerBlockId, "SaveWindowProperties", true, params), true) ;
   }
}