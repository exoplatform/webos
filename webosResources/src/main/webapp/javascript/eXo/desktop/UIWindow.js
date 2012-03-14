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

    var uiWindow = xj(popup);
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
      if (!popup.maximized)
      {
        this.initDND(windowBar[0], uiWindow[0]);
      }
    }
    catch(err)
    {
      alert("Error In DND: " + err);
    }

    var minimizedIcon = windowBar.find("div.MinimizedIcon");
    minimizedIcon.bind("mouseup", this.minimizeWindowEvt);
    var maximizedIcon = windowBar.find("div.MaximizedIcon");
    maximizedIcon.bind("mouseup", this.maximizeWindowEvt);
    windowBar.bind("dblclick", function() {eXo.desktop.UIWindow.maximizeWindowEvt.call(maximizedIcon)});

    if (!popup.maximized)
    {
      var resizeArea = uiWindow.children("div.BottomDecoratorLeft").find("div.ResizeArea");
      resizeArea.mousedown(function(e)
      {
        eXo.desktop.UIWindow.startResizeProcess(e, uiWindow);
      });
    }

    popup.resizeCallback = new eXo.core.HashMap();
  },

  initDND : function(dragBar, appWindow)
  {
    eXo.core.DragDrop2.init(dragBar, appWindow);

    appWindow.onDrag = function(nx, ny, ex, ey, e)
    {
      var jqObj = xj(appWindow);
      var dragObjectY = jqObj.position().top;
      var browserHeight = xj(window).height();
      var browserWidth = xj(window).width();

      var desktopHeight = xj("#UIPageDesktop").height();
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
      xj(document).bind("mousemove", eXo.core.DragDrop2.end);
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

  maximizeWindowEvt : function(evt)
  {
    var jqObj = xj(this);
    var portletWindow = jqObj.closest(".UIResizeObject");

    var desktopPage = xj("#UIPageDesktop")[0];
    var desktopHeight = desktopPage.offsetHeight;
    var resizableBlocks = portletWindow.find("div.UIResizableBlock");
    if (portletWindow[0].maximized)
    {
      portletWindow[0].maximized = false;
      portletWindow.css("top", eXo.desktop.UIWindow.posY + "px");
      if (eXo.core.I18n.isLT())
      {
        portletWindow.css("left", eXo.desktop.UIWindow.posX + "px");
      }
      else
      {
        portletWindow.css("right", eXo.desktop.UIWindow.posX + "px");
      }
      portletWindow.css("width", eXo.desktop.UIWindow.originalWidth + "px");
      portletWindow.css("height", null);
      resizableBlocks.each(function()
      {
        var h = (this.originalHeight ? this.originalHeight : 400 ) + "px";
        xj(this).css("height", h);
      });
      jqObj.attr("class", "ControlIcon MaximizedIcon");

    }
    else
    {
      eXo.desktop.UIWindow.backupObjectProperties(portletWindow[0], resizableBlocks.get());
      portletWindow.css("top", "0px");
      if (eXo.core.I18n.isLT())
      {
        portletWindow.css("left", "0px");
      }
      else
      {
        portletWindow.css("right", "0px");
      }
      portletWindow.css("width", "100%");
      portletWindow.css("height", "auto");
      var delta = desktopHeight - portletWindow[0].clientHeight;
      resizableBlocks.each(function()
      {
        xj(this).css("height", (parseInt(this.clientHeight) + delta) + "px");
      });

      portletWindow.css("height", portletWindow[0].clientHeight + "px");
      portletWindow[0].maximized = true;
      jqObj.attr("class", "ControlIcon RestoreIcon");
    }
    eXo.desktop.UIWindow.saveWindowProperties(portletWindow[0]);
  },

  minimizeWindowEvt : function(evt)
  {
    var icon = xj(this);
    var popup = icon.closest(".UIDragObject");
    popup.parent().children("div").each(function(index)
    {
      if(this == popup[0])
      {
        eXo.desktop.UIDesktop.showHideWindow(popup[0], xj("#IconContainer").children("img.Icon")[index + 1]);
      }
    });
  },

  startResizeProcess : function(e, portletWindow)
  {
    var resizableBlock = portletWindow.find("div.UIResizableBlock");
    resizableBlock.css("overflow", "hidden");

    var originX = e.clientX;
    var originY = e.clientY;
    var originW = portletWindow[0].offsetWidth;
    var originH = resizableBlock[0].offsetHeight;

    var desktopPage = xj("#UIPageDesktop");
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

  backupObjectProperties : function(windowPortlet, resizableComponents)
  {
    var UIWindow = eXo.desktop.UIWindow;
    for (var i = 0; i < resizableComponents.length; i++)
    {
      resizableComponents[i].originalWidth = resizableComponents[i].offsetWidth;
      resizableComponents[i].originalHeight = resizableComponents[i].offsetHeight;
    }

    UIWindow.posX = eXo.desktop.UIDesktop.findPosXInDesktop(windowPortlet, eXo.core.I18n.isRT());
    UIWindow.posY = eXo.desktop.UIDesktop.findPosYInDesktop(windowPortlet);
    UIWindow.originalWidth = windowPortlet.offsetWidth;
    UIWindow.originalHeight = windowPortlet.offsetHeight;
  },

  saveWindowProperties : function(object, appStatus)
  {
    var jqObj = xj(object);
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

    var blockID = xj(".UIPage").attr("id").replace(/^UIPage-/, "");
    ajaxAsyncGetRequest(eXo.env.server.createPortalURL(blockID, "SaveWindowProperties", true, params), true);
  }
}