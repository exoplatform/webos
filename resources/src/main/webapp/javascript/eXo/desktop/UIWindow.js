eXo.desktop.UIWindow = {

  maxIndex : 0,

  defaultWidth : 800,

  defaultHeight : 400,

  superClass : eXo.webui.UIPopup,

  init : function(popup, posX, posY)
  {
    if (typeof(popup) == "string")
    {
      popup = document.getElementById(popup);
    }
    if (popup == null)
    {
      return;
    }

    var uiWindow = gj(popup);
    var uiApplication = uiWindow.find('div.PORTLET-FRAGMENT');
    if (!uiApplication)
    {
      return;
    }

    if (uiWindow.css("z-index") == "auto")
    {
      uiWindow.css("z-index", ++eXo.webui.UIPopup.zIndex);
    }
    uiWindow.bind("mousedown", this.mousedownOnPopup);

    var windowBar = uiWindow.children("div.WindowBarLeft");
    this.superClass.setPosition(popup, posX, posY, eXo.core.I18n.isRT());
    try
    {
      this.initDND(windowBar[0], uiWindow[0]);
    }
    catch(err)
    {
      alert("Error In DND: " + err);
    }

    windowBar.find("div.MinimizedIcon").mousedown(function()
    {
      eXo.desktop.UIWindow.minimizeWindow(uiWindow);
      return false;
    });

    var toggleIcon = windowBar.find("div.MaximizedIcon, div.RestoreIcon");
    toggleIcon.mousedown(function()
    {
      eXo.desktop.UIWindow.toggleMaximize(uiWindow[0], toggleIcon);
      return false;
    });

    windowBar.dblclick(function()
    {
      eXo.desktop.UIWindow.toggleMaximize(uiWindow[0], toggleIcon);
      return false;
    });

    var resizeArea = uiWindow.children("div.BottomDecoratorLeft").find("div.ResizeArea");
    resizeArea.mousedown(function(e)
    {
	    if (!popup.maximized)
	    {
		    eXo.desktop.UIWindow.startResizeProcess(e, uiWindow);
	    }
    });

    popup.resizeCallback = new eXo.core.HashMap();
  },

  initDND : function(dragBar, appWindow)
  {
    eXo.core.DragDrop.init(dragBar, appWindow);

    appWindow.onDragStart = function() {
    	//trigger mousedownOnPopup
    	gj(appWindow).trigger("mousedown");
    }
    
    appWindow.onDrag = function(nx, ny, ex, ey, e)
    {
      var jqObj = gj(appWindow);
      var dragObjectY = jqObj.position().top;
      var browserHeight = gj(window).height();
      var browserWidth = gj(window).width();

      var desktopHeight = gj("#UIPageDesktop").height();
      if (dragObjectY < 0)
      {
        jqObj.css("top", "0px");
        if (ey < 1)
        {
          this.endDND();
        }
      }

      if (dragObjectY > (desktopHeight - 33))
      {
        jqObj.css("top", desktopHeight - 33);
        if (ey > browserHeight)
        {
          this.endDND();
        }
      }

      if ((ex < 0) || (ex > browserWidth))
      {
        this.endDND();
      }
    };

    appWindow.onDragEnd = function(x, y, clientX, clientY)
    {
      eXo.desktop.UIWindow.saveWindowProperties(appWindow);
    };

    appWindow.endDND = function()
    {
      gj(document).bind("mousemove", eXo.core.DragDrop.end);
    };
  },

  mousedownOnPopup : function(evt)
  {
    var isMaxZIndex = eXo.desktop.UIDesktop.isMaxZIndex(this);
    if (!isMaxZIndex)
    {
      eXo.desktop.UIDesktop.resetZIndex(this);
      eXo.desktop.UIWindow.saveWindowProperties(this);
    }
  },

  toggleMaximize : function(uiWindow, icon)
  {
  	var windowBar = gj(uiWindow).children(".WindowBarLeft");
  	
  	eXo.desktop.UIDesktop.resetZIndex(uiWindow);
  	if (icon.hasClass("MaximizedIcon")) {
  		eXo.desktop.UIWindow.maximizeWindow(uiWindow, icon);    		
  		windowBar.off("mousedown");
  	} else {
  		eXo.desktop.UIWindow.restoreWindow(uiWindow, icon);
  		eXo.desktop.UIWindow.initDND(windowBar[0], uiWindow);
  	} 
  },
  
  restoreWindow : function(popupWindow, restoreIcon)
  {
    popupWindow = gj(popupWindow);
    var originX = popupWindow.data("restoreX");
    var originY = popupWindow.data("restoreY");
    var originW = popupWindow.data("restoreW");
    var originH = popupWindow.data("restoreH");
    
    if(eXo.core.I18n.isLT())
    {
      popupWindow.css("left", originX);
    }
    else
    {
      popupWindow.css("right", originX);
    }
    popupWindow.css("top", originY).css("width", originW).css("height", "auto");
    popupWindow.find("div.UIResizableBlock").css("height", originH);
    restoreIcon.attr("class", "ControlIcon MaximizedIcon");
    eXo.desktop.UIWindow.saveWindowProperties(popupWindow[0]);
    popupWindow[0].maximized = false;
  },

  maximizeWindow : function(popupWindow, maximIcon)
  {
    popupWindow = gj(popupWindow);
    var resizableBlock = popupWindow.find("div.UIResizableBlock");
    popupWindow.data("restoreX",  eXo.desktop.UIDesktop.findPosXInDesktop(popupWindow[0], eXo.core.I18n.isRT()));
    popupWindow.data("restoreY", eXo.desktop.UIDesktop.findPosYInDesktop(popupWindow[0]));
    popupWindow.data("restoreW", popupWindow.width());
    popupWindow.data("restoreH", resizableBlock.height());    
    
    if(eXo.core.I18n.isLT())
    {
      popupWindow.css("left", 0);
    }
    else
    {
      popupWindow.css("right", 0);
    }
    popupWindow.css("top", 0).css("width", "100%").css("height", "auto");
    maximIcon.attr("class", "ControlIcon RestoreIcon");
    var h = resizableBlock[0].clientHeight + gj("#UIPageDesktop")[0].offsetHeight - popupWindow[0].clientHeight;
    resizableBlock.css("height", h);

    eXo.desktop.UIWindow.saveWindowProperties(popupWindow[0]);
    popupWindow[0].maximized = true;
  },

  minimizeWindow : function(popupWindow)
  {
    var id = popupWindow.attr("id").replace(/^UIWindow-/, "");
    var dockItem = gj("#DockItem" + id);
    eXo.animation.ImplodeExplode.implode(popupWindow[0], dockItem[0], gj("#UIPageDesktop"), 10);
    gj("#DockItem" + id).addClass("ShowIcon");
    eXo.desktop.UIWindow.saveWindowProperties(popupWindow[0], "HIDE");
  },

  startResizeProcess : function(e, portletWindow)
  {
    var resizableBlock = portletWindow.find("div.UIResizableBlock");
    resizableBlock.css("overflow", "hidden");

    var originX = e.clientX;
    var originY = e.clientY;
    var originW = portletWindow[0].offsetWidth;
    var originH = resizableBlock[0].offsetHeight;

    var desktopPage = gj("#UIPageDesktop");
    desktopPage.mousemove(function(event)
    {
      eXo.desktop.UIWindow.resizeWindow(event, portletWindow, originX, originY, originW, originH);
    });

    desktopPage.mouseup(function(event)
    {
      desktopPage.unbind("mousemove").unbind("mouseup");
      eXo.desktop.UIWindow.endResizeProcess(event, portletWindow);
    });
  },

  resizeWindow : function(e, popupWindow, originX, originY, originW, originH)
  {
    var deltaX = eXo.core.I18n.isLT() ? (e.clientX - originX) : (originX - e.clientX);
    var deltaY = e.clientY - originY;

    popupWindow.css("width", Math.max(200, originW + deltaX));
    popupWindow.find("div.UIResizableBlock").css("height", Math.max(10, originH + deltaY));
  },

  endResizeProcess : function(e, popupWindow)
  {
    e.stopPropagation();
    eXo.desktop.UIWindow.saveWindowProperties(popupWindow[0]);
  },

  saveWindowProperties : function(object, appStatus)
  {
    var jqObj = gj(object);
    var objID = jqObj.attr("id").replace(/^UIWindow-/, "");

    var params;
    if (!appStatus)
    {
      var uiResizableBlock = jqObj.find("div.UIResizableBlock");
      params = [
        {name : "objectId", value : objID},
        {name : "posX", value : (eXo.core.I18n.isLT() ? parseInt(jqObj.css("left")) : parseInt(jqObj.css("right"))) },
        {name : "posY", value : parseInt(jqObj.css("top"))},
        {name : "zIndex", value : jqObj.css("z-index")},
        {name : "windowWidth", value : object.offsetWidth},
        {name : "windowHeight", value : uiResizableBlock[0].offsetHeight}
      ];
    }
    else
    {
      params = [
        {name : "objectId", value : objID},
        {name : "appStatus", value : appStatus}
      ];
      if (appStatus == "SHOW")
      {
        params.push({name : "zIndex", value : jqObj.css("z-index")});
      }
    }

    var blockID = gj(".UIPage").attr("id").replace(/^UIPage-/, "");
    ajaxAsyncGetRequest(eXo.env.server.createPortalURL(blockID, "SaveWindowProperties", true, params), true);
  }
}
