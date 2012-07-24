eXo.desktop = {};

eXo.desktop.UIDesktop = {

  init : function()
  {
    var desktopPage = gj("#UIPageDesktop");
    if (desktopPage.length > 0)
    {
      eXo.desktop.UIDesktop.fixDesktop();
      eXo.desktop.UIDockbar.init();
      desktopPage.children("div.UIWindow").each(function()
      {
        eXo.desktop.UIDesktop.backupWindowProperties(this);
      });

      desktopPage[0].onmousedown = eXo.desktop.UIDesktop.showContextMenu;
    }
  },

  showContextMenu : function(evt) {
      if (!evt) evt = window.event;
      var targetID = (evt.target || evt.srcElement).id;
      
      if ("UIPageDesktop" !== targetID)
         return;
      eXo.webui.UIRightClickPopupMenu.clickRightMouse(evt, this, "UIDesktopContextMenu", "", null, 5);
   },

  closeAll: function()
  {
    gj("#UIPageDesktop").find("div.UIWindow").each(function()
    {
      var appWindow = gj(this);
      if (appWindow.css("display") == "block")
      {
        eXo.desktop.UIDesktop.removeWindowContent(appWindow.attr("id").replace("UIWindow-", ""));
        appWindow.css("display", "none");
      }
    });
  },

  fixDesktop : function()
  {
    var desktopPage = gj("#UIPageDesktop");
    var h = gj(window).height() - desktopPage.offset().top + "px";
    desktopPage.css("height", h);

    window.scroll(0, 0);
    setTimeout("eXo.desktop.UIDockbar.resizeDockBar()", 0);
  },

  resetZIndex : function(windowObject)
  {
    var jqObj = gj(windowObject);
    var hasPopup;
    jqObj.parent().find("div.UIPopupWindow").each(function()
    {
      if (gj(this).css("display") == "block")
      {
        hasPopup = true;
        return false;
      }
    });

    if (hasPopup)
    {
      return;
    }

    var maxZIndex = parseInt(jqObj.css("z-index"));
    gj("#UIPageDesktop").find("div.UIWindow").each(function()
    {
      var appWindow = gj(this);
      var z = parseInt(appWindow.css("z-index"));
      if (z < 0)
      {
        appWindow.css("z-index", 1);
        z = 1;
      }
      if (maxZIndex < z)
      {
        maxZIndex = z;
      }
    });
    eXo.desktop.UIWindow.maxIndex = maxZIndex + 1;
    jqObj.css("z-index", eXo.desktop.UIWindow.maxIndex);
  },

  isMaxZIndex : function(object)
  {
    var ret = true;
    var z = parseInt(gj(object).css("z-index"));
    gj("#UIPageDesktop").find("div.UIWindow").each(function()
    {
      var app = gj(this);
      if (app[0] != object && app.css("display") == "block" && z <= parseInt(app.css("z-index")))
      {
        ret = false;
        return false;
      }
    });
    return ret;
  },

  showWindow : function(popupWindow, dockIcon)
  {
    var desktopPage = gj("#UIPageDesktop");

    if(!eXo.desktop.UIDesktop.isMaxZIndex(popupWindow))
    {
      eXo.desktop.UIDesktop.resetZIndex(popupWindow);
    }

    if (gj(popupWindow).find("div.PORTLET-FRAGMENT").children("div").length == 0)
    {
      var blockID = desktopPage.closest(".UIPage").attr("id").replace(/^UIPage-/, "");
      var params = [
        {name : "objectId", value: popupWindow.id.replace(/^UIWindow-/, "")}
      ];
      ajaxGet(eXo.env.server.createPortalURL(blockID, "ShowPortlet", true, params));
    }

    eXo.animation.ImplodeExplode.explode(popupWindow, dockIcon, desktopPage, 10);
    eXo.desktop.UIWindow.saveWindowProperties(popupWindow, "SHOW");
    popupWindow.isShowed = true;

    gj(dockIcon).addClass("ShowIcon");
  },

  hideWindow : function(popupWindow, dockIcon)
  {
    eXo.animation.ImplodeExplode.implode(popupWindow, dockIcon, gj("#UIPageDesktop"), 10);
    eXo.desktop.UIWindow.saveWindowProperties(popupWindow, "HIDE");
    gj(dockIcon).addClass("ShowIcon");
  },

  quitWindow : function(popupWindow, dockIcon)
  {
    if (gj(popupWindow).css("display") == "block")
    {
      eXo.animation.ImplodeExplode.implode(popupWindow, dockIcon, gj("#UIPageDesktop"), 10);
    }
    eXo.desktop.UIWindow.saveWindowProperties(popupWindow, "QUIT");
    popupWindow.isShowed = false;

    gj(dockIcon).removeClass("ShowIcon");
  },

  showHideWindow : function(windowID, dockIcon)
  {
    var popupWindow = gj("#" + windowID);

    if (popupWindow.css("display") == "block")
    {
      eXo.desktop.UIDesktop.hideWindow(popupWindow[0], dockIcon);
    }
    else
    {
      eXo.desktop.UIDesktop.showWindow(popupWindow[0], dockIcon);
    }
  },

  findPosXInDesktop : function(object, isRTL)
  {
    var uiPageDesktop = gj(object).closest(".UIPageDesktop")[0];
    return eXo.core.Browser.findPosXInContainer(object, uiPageDesktop, isRTL);
  },

  findPosYInDesktop : function(object)
  {
    var jqObj = gj(object);
    return jqObj.offset().top - jqObj.closest(".UIPageDesktop").offset().top;
  },

  backupWindowProperties : function(uiWindow)
  {
    uiWindow.originalX = eXo.desktop.UIDesktop.findPosXInDesktop(uiWindow, eXo.core.I18n.isRT());
    uiWindow.originalY = eXo.desktop.UIDesktop.findPosYInDesktop(uiWindow);
    uiWindow.originalW = uiWindow.offsetWidth;
    uiWindow.originalH = uiWindow.offsetHeight;
    uiWindow.style.visibility = "visible";
    if (uiWindow.style.display == "")
    {
      uiWindow.style.display = "none";
    }

    var portletID = uiWindow.id.replace(/^UIWindow-/, "");
    if (gj("#DockItem" + portletID).hasClass("ShowIcon"))
    {
      uiWindow.isShowed = true;
    }
    //uiWindow.isFirstTime = false;
  },

  removeApp : function(uri)
  {
    var result = ajaxAsyncGetRequest(uri, false);
    if (result == "OK")
    {
      var appId = uri.substr(uri.lastIndexOf("=") + 1);
      eXo.desktop.UIDesktop.removeWindow("UIWindow-" + appId);
      eXo.desktop.UIDockbar.removeDockbarIcon("DockItem" + appId);
    }
  },

  removeWindow : function (idWindow)
  {
    gj("#" + idWindow).remove();
  },

  removeWindowContent : function (evt, elemt)
  {
    var idWindow = evt;
    if (elemt)
    {
      //TODO: Optimize this if branch with a nicer solution
      var contextMenu = gj(elemt).closest(".UIRightClickPopupMenu")[0];
      if (!evt)
      {
        evt = window.event;
      }
      evt.cancelBubble = true;
      idWindow = contextMenu.objId;
    }

    var uiWindow = gj("#UIWindow-" + idWindow).eq(0);
    if (uiWindow)
    {
      var portletFrag = uiWindow.find("div.PORTLET-FRAGMENT");
      portletFrag.children().remove();
      portletFrag.html("<span></span>");
      eXo.desktop.UIDesktop.quitWindow(uiWindow[0], gj("#DockItem" + idWindow)[0]);
    }
  },

  setDesktopBackground : function (imageURL)
  {
    var pageDesktop = document.getElementById("UIPageDesktop");
    if (!pageDesktop)
    {
      return;
    }

    if (imageURL)
    {
      imageURL = "url('" + imageURL + "') no-repeat center center";
    } else if (navigator.userAgent.indexOf("MSIE") >= 0)
    {
      pageDesktop.style.backgroundAttachment = "";
      pageDesktop.style.backgroundImage = "";
      pageDesktop.style.backgroundRepeat = "";
      pageDesktop.style.backgroundPositionX = "";
      pageDesktop.style.backgroundPositionY = "";
      pageDesktop.style.backgroundColor = "";
      return;
    }

    pageDesktop.style.background = imageURL;
  }
}
_module.UIDesktop = eXo.desktop.UIDesktop;
