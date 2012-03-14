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
    var resizeArea = uiWindow.children("div.BottomDecoratorLeft").find("div.ResizeArea");
    resizeArea.bind("mousedown", this.startResizeWindowEvt);

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
    // Re initializes the scroll tabs managers on the page
    //eXo.portal.UIPortalControl.initAllManagers();
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

  startResizeWindowEvt : function(evt)
  {
    var icon = xj(this);
    var portletWindow = icon.closest(".UIResizeObject");
    var uiWindow = eXo.desktop.UIWindow;
    if (portletWindow[0].maximized || uiWindow.portletWindow)
    {
      return;
    }

    if (!evt)
    {
      evt = window.event;
    }
    var uiPageDesktop = document.getElementById("UIPageDesktop");
    var uiApplication = portletWindow.find("div.UIApplication").eq(0);
    if (uiApplication.find(".UIResizableBlock"))
    {
      uiApplication.css("overflow", "hidden");
    }

    uiWindow.resizableObject = portletWindow.find("div.UIResizableBlock")[0];
    uiWindow.initMouseX = evt.clientX;
    uiWindow.initMouseY = evt.clientY;
    uiWindow.backupObjectProperties(portletWindow[0], uiWindow.resizableObject);
    uiWindow.portletWindow = portletWindow[0];
    uiPageDesktop.onmousemove = uiWindow.resizeWindowEvt;
    uiPageDesktop.onmouseup = uiWindow.endResizeWindowEvt;
  },

  resizeWindowEvt : function(evt)
  {
    if (!evt)
    {
      evt = window.event;
    }
    var uiWindow = eXo.desktop.UIWindow;
    if (eXo.core.I18n.isLT())
    {
      var deltaX = evt.clientX - uiWindow.initMouseX;
      var deltaY = evt.clientY - uiWindow.initMouseY;
    }
    else
    {
      var deltaX = uiWindow.initMouseX - evt.clientX;
      var deltaY = evt.clientY - uiWindow.initMouseY;
    }
    uiWindow.portletWindow.style.width = Math.max(200, (uiWindow.originalWidth + deltaX)) + "px";
    for (var i = 0; i < uiWindow.resizableObject.length; i++)
    {
      uiWindow.resizableObject[i].style.height = Math.max(10, (uiWindow.resizableObject[i].originalHeight + deltaY)) + "px";
    }
  },

  endResizeWindowEvt : function(evt)
  {
    if (!evt)
    {
      evt = window.event;
    }
    if (evt.stopPropagation)
    {
      evt.stopPropagation();
    }
    evt.cancelBubble = true;

    var uiWindow = eXo.desktop.UIWindow.portletWindow;
    for (var name in uiWindow.resizeCallback.properties)
    {
      var method = uiWindow.resizeCallback.get(name);
      if (typeof(method) == "function")
      {
        method(evt);
      }
    }
    eXo.desktop.UIWindow.saveWindowProperties(uiWindow);
    // Re initializes the scroll tabs managers on the page
    eXo.portal.UIPortalControl.initAllManagers();
    eXo.desktop.UIWindow.portletWindow = null;
    eXo.desktop.UIWindow.resizableObject = null;
    this.onmousemove = null;
    this.onmouseup = null;

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