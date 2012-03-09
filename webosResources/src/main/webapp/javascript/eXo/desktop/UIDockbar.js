eXo.desktop.UIDockbar = {

  curve : 3,

  weight : 2.3,

  isFirstTime : true,

  showDesktop : false,

  arrayImage : false,

  itemStyleClass : "MenuItem",

  itemOverStyleClass : "MenuItemOver",

  containerStyleClass : "MenuItemContainer",

  superClass : eXo.webui.UIPopupMenu,

  init : function()
  {
    var dockbar = xj("#UIDockBar");
    if (dockbar.length == 0)
    {
      return;
    }
    var imgObject = dockbar.find("img.Icon");
    eXo.desktop.UIDockbar.arrayImage = imgObject.get();

    dockbar[0].defaultIconSize = 40;
    dockbar[0].originalBGDockbarHeight = 47;

    if (imgObject.length > 0 && imgObject[0].onmousemove == undefined)
    {
      this.isFirstTime = true;
    }

    if (this.isFirstTime == true)
    {
      setTimeout("eXo.desktop.UIDockbar.waitOnLoad(eXo.desktop.UIDockbar.arrayImage)", 0);
      this.isFirstTime = false;
    }

    dockbar[0].originalDockbarHeight = dockbar[0].offsetHeight;
    window.setTimeout(function() {dockbar.css("visibility", "visible")}, 50);

    xj("#PortletsViewer")[0].onclick = this.viewPortlets;
  },

  waitOnLoad : function(images)
  {
    for (var i = 0; i < images.length; i++)
    {
      images[i].onmousemove = eXo.desktop.UIDockbar.animationEvt;
      images[i].onmouseover = eXo.desktop.UIDockbar.iconOverEvt;
      images[i].onmouseout = eXo.desktop.UIDockbar.iconOutEvt;

      if (eXo.core.Browser.isIE6())
      {
        images[i].runtimeStyle.filter = "progid:DXImageTransform.Microsoft.AlphaImageLoader(src='" + images[i].src + "', sizingMethod='scale')";
        images[i].src = "/eXoResources/skin/sharedImages/Blank.gif";
      }
    }
  },

  startDockBarEvt : function(evt)
  {
    evt.cancelBubble = true;
    document.oncontextmenu = document.body.oncontextmenu = function() {return false};
    var uiPageDesktop = document.getElementById("UIPageDesktop");
    uiPageDesktop.onmouseover = eXo.desktop.UIDockbar.endDockBarEvt;
  },

  endDockBarEvt : function()
  {
    this.onmouseover = null;
    document.oncontextmenu = document.body.oncontextmenu = function() {return true};
    eXo.webui.UIRightClickPopupMenu.hideContextMenu("DockbarContextMenu");
    eXo.desktop.UIDockbar.reset();
  },

  iconOverEvt : function()
  {
    var dockbar = xj("#UIDockBar")[0];
    var objectXInDockbar = eXo.core.Browser.findPosXInContainer(this, dockbar);
    eXo.webui.UIRightClickPopupMenu.hideContextMenu("DockbarContextMenu");

    var tooltip = xj(this).next();
    tooltip.css({"display" : "block", "top" : -tooltip[0].offsetHeight + "px", "left" : objectXInDockbar + "px"});
  },

  iconOutEvt : function()
  {
    this.nextSibling.style.display = "none";
  },

  viewPortlets : function()
  {
    xj("#UIPageDesktop").find("div.UIWindow").each(function()
    {
      var appWindow = xj(this);
      if (eXo.desktop.UIDockbar.showDesktop)
      {
        if (this.isShowed)
        {
          appWindow.css("display", "block");
        }
      } else if (appWindow.css("display") == "block")
      {
        appWindow.css("display", "none");
      }
    });

    var srcMonitoringImage = "/eXoResources/skin/sharedImages/Icon80x80/Hide" + this.id + ".png";
    var srcPortletsViewerImage = "/eXoResources/skin/sharedImages/Icon80x80/Show" + this.id + ".png";

    if (eXo.desktop.UIDockbar.showDesktop)
    {
      if (eXo.core.Browser.isIE6())
      {
        this.runtimeStyle.filter = "progid:DXImageTransform.Microsoft.AlphaImageLoader(src='" + srcMonitoringImage + "', sizingMethod='scale')";
      }
      else
      {
        this.src = srcMonitoringImage;
      }
      eXo.desktop.UIDockbar.showDesktop = false;
    }
    else
    {
      if (eXo.core.Browser.isIE6())
      {
        this.runtimeStyle.filter = "progid:DXImageTransform.Microsoft.AlphaImageLoader(src='" + srcPortletsViewerImage + "', sizingMethod='scale')";
      }
      else
      {
        this.src = srcPortletsViewerImage;
      }
      eXo.desktop.UIDockbar.showDesktop = true;
    }
  },

  animationEvt : function(e)
  {
    var curve = eXo.desktop.UIDockbar.curve;
    var weight = eXo.desktop.UIDockbar.weight;

    var fixBugImageElement = document.getElementById("FixBug");
    var desktopPage = document.getElementById("UIPageDesktop");

    var selectedIconX = eXo.desktop.UIDesktop.findPosXInDesktop(this, eXo.core.I18n.isRT());
    var middleIcon = selectedIconX + (this.offsetWidth / 2);
    var mouseX = eXo.core.Browser.findMouseRelativeX(desktopPage, e);
    if (eXo.core.I18n.isRT())
    {
      mouseX = desktopPage.offsetWidth - mouseX;
    }

    var distanceWeight = (middleIcon - mouseX) / (2 * curve * (middleIcon - selectedIconX));

    var selectedIconIndex = eXo.desktop.UIDockbar.findIndex(this);
    var dockbar = document.getElementById("UIDockBar");
    var dockbarCenter = document.getElementById("DockbarCenter");

    fixBugImageElement.style.height = dockbar.defaultIconSize + (dockbar.defaultIconSize * (weight - 1)) + "px";

    dockbarCenter.style.height = dockbar.originalBGDockbarHeight + (dockbar.defaultIconSize * (weight - 1)) + "px";

    var defaultIconSize = dockbar.defaultIconSize;

    xj(this).parent().children("img.Icon").each(function(index)
    {
      var deltaCurve = Math.abs(selectedIconIndex - index);
      var size = defaultIconSize;
      if (deltaCurve < curve)
      {
        if (index == selectedIconIndex)
        {
          size = Math.round(defaultIconSize + defaultIconSize * (weight - 1) * ((curve - deltaCurve) / curve - Math.abs(distanceWeight)));
          distanceWeight = -distanceWeight;
        }
        else
        {
          size = Math.round(defaultIconSize + defaultIconSize * (weight - 1) * ((curve - deltaCurve) / curve + distanceWeight));
        }

        xj(this).css({"width" : size + "px", "height" : size + "px"});
      }
    });

    eXo.desktop.UIDockbar.resizeDockBar();
  },

  findIndex : function(object)
  {
    var ret;
    xj(object).parent().children("img.Icon").each(function(index)
    {
      if (object == this)
      {
        ret = index;
        return false;
      }
    });

    return ret;
  },

  removeDockbarIcon : function(iconId)
  {
    var icon = xj("#" + iconId);
    if (icon.length > 0)
    {
      var prev = icon.prev();
      if (prev[0].nodeType == 3)
      {
        prev.remove();
      }
      icon.remove();
      //Remove tooltip
      icon.next("span").remove();
    }
  },

  reset : function()
  {
    var dockbar = xj("#UIDockBar")[0];
    xj("#DockbarCenter").css("height", dockbar.originalBGDockbarHeight + "px");
    var iconSize = dockbar.defaultIconSize + "px";
    xj("#IconContainer").children("img.Icon").css({"width" : iconSize, "height" : iconSize});
    xj("#FixBug").css("height", iconSize);
    eXo.desktop.UIDockbar.resizeDockBar();
  },

  resizeDockBar : function()
  {
    var desktopPage = xj("#UIPageDesktop")[0];
    var dockbar = xj("#UIDockBar")[0];
    var iconContainer = xj("#IconContainer");

    var widthItemControl = 0;
    var defaultIconSize = dockbar.defaultIconSize ? dockbar.defaultIconSize : 0;
    iconContainer.children("img.Icon").each(function()
    {
      widthItemControl += Math.max(this.offsetWidth, defaultIconSize) + 5;
    });

    var totalWidthSeparators = 0;
    iconContainer.children("img.Separator").each(function()
    {
      totalWidthSeparators += this.offsetWidth + 10;
    });

    iconContainer.css("width", (widthItemControl + totalWidthSeparators + 10) + "px");

    if (!dockbar.totalPadding)
    {
      var totalPadding = 0;
      iconContainer.parentsUntil(dockbar.parentNode).each(function()
      {
        var p = xj(this);
        var rp = parseInt(p.css("paddingRight"));
        var lp = parseInt(p.css("paddingLeft"));
        if (!isNaN(rp))
        {
          totalPadding += rp;
        }
        if (!isNaN(lp))
        {
          totalPadding += lp;
        }
      });

      dockbar.totalPadding = totalPadding;
    }

    dockbar.style.width = (iconContainer.offsetWidth + dockbar.totalPadding) + "px";
    dockbar.style.left = ((desktopPage.offsetWidth - dockbar.offsetWidth) / 2) + "px";
  },

  resetDesktopShowedStatus : function(uiPageDesktop, uiDockBar)
  {
    if (this.showDesktop)
    {
      var portletsViewer = xj(uiDockBar).find("#PortletsViewer")[0];
      var blankImage = portletsViewer.src;
      var srcMonitoringImage = "/eXoResources/skin/sharedImages/Icon80x80/HidePortletsViewer.png";
      if (eXo.core.Browser.isIE6())
      {
        portletsViewer.runtimeStyle.filter = "progid:DXImageTransform.Microsoft.AlphaImageLoader(src='" + srcMonitoringImage + "', sizingMethod='scale')";
        portletsViewer.src = blankImage;
      }
      else
      {
        portletsViewer.src = srcMonitoringImage;
      }
      this.showDesktop = false;
    }
  },

  onMenuItemOver : function(event)
  {
    this.className = eXo.desktop.UIDockbar.itemOverStyleClass;
    if (this.menuItemContainer)
    {
      var menuItemContainer = this.menuItemContainer;
      menuItemContainer.style.display = "block";
      menuItemContainer.style.visibility = "";
      var x = this.offsetWidth;
      var browserWidth = xj(window).width();
      var posRight = browserWidth - eXo.core.Browser.findPosX(this) - this.offsetWidth;
      var rootX = (eXo.core.I18n.isLT() ? eXo.core.Browser.findPosX(this) : posRight);
      if (x + menuItemContainer.offsetWidth + rootX > browserWidth)
      {
        x -= (menuItemContainer.offsetWidth + this.offsetWidth);
      }

      //padWidth is used for improvement PORTAL-2827
      if (eXo.core.I18n.isLT())
      {
        var padWidth = eXo.core.Browser.findPosX(this) - eXo.core.Browser.findPosX(menuItemContainer.offsetParent);
        menuItemContainer.style.left = x + padWidth + "px";
      }
      else
      {
        var padWidth = (eXo.core.Browser.findPosX(menuItemContainer.offsetParent) + menuItemContainer.offsetParent.offsetWidth)
          - (eXo.core.Browser.findPosX(this) + this.offsetWidth);
        menuItemContainer.style.right = x + padWidth + "px";
      }
      eXo.desktop.UIDockbar.createSlide(this);
      eXo.desktop.UIDockbar.superClass.pushVisibleContainer(this.menuItemContainer.id);

      var browserHeight = xj(window).height();

      var jqObj = xj(this);
      var parentMenu = jqObj.closest(".MenuItemContainer");
      var blockMenu = jqObj.closest(".BlockMenu");
      var y = jqObj.offset().top - parentMenu.offset().top - blockMenu.scrollTop();
      if (y + menuItemContainer.offsetHeight + 15 > browserHeight)
      {
        y += (this.offsetHeight - menuItemContainer.offsetHeight);
        if (y <= 0)
        {
          y = 1;
        }
      }
      menuItemContainer.style.top = y + "px";
    }
  },

  onMenuItemOut : function(event)
  {
    this.className = eXo.desktop.UIDockbar.itemStyleClass;
    if (this.menuItemContainer)
    {
      eXo.desktop.UIDockbar.superClass.pushHiddenContainer(this.menuItemContainer.id);
      eXo.desktop.UIDockbar.superClass.popVisibleContainer();
      eXo.desktop.UIDockbar.superClass.setCloseTimeout();
    }
  },

  createSlide : function(menuItem)
  {
    var menuItemContainer = menuItem.menuItemContainer;
    if (!menuItemContainer.resized)
    {
      eXo.desktop.UIDockbar.setContainerSize(menuItemContainer);
    }

    var jqObj = xj(menuItemContainer);
    var blockMenu = jqObj.find("div.BlockMenu").eq(0);
    var topElement = blockMenu.parent().children("div.TopNavigator").eq(0);
    var bottomElement = blockMenu.parent().children("div.BottomNavigator").eq(0);
    var menuContainer = blockMenu.find("div.MenuContainer").eq(0);

    if (!blockMenu[0].id)
    {
      blockMenu.attr("id", "eXo" + new Date().getTime() + Math.random().toString().substring(2));
    }

    var browserHeight = xj(window).height();
    if (menuContainer[0].offsetHeight + 64 > browserHeight)
    {
      var curentHeight = browserHeight - 64;
      blockMenu.css({"height" : curentHeight + "px", "overflowY" : "hidden"});
      topElement.css("display", "block");
      bottomElement.css("display", "block");

      if (!menuContainer[0].curentHeight || (menuContainer[0].curentHeight != curentHeight))
      {
        eXo.desktop.UIDockbar.initSlide(menuContainer[0], curentHeight);
      }

      topElement.mousedown(function(e)
      {
        e.stopPropagation();
        eXo.portal.VerticalScrollManager.scrollComponent(blockMenu.attr("id"), true, 15);
      });

      topElement.mouseup(function(e)
      {
        e.stopPropagation();
        eXo.portal.VerticalScrollManager.cancelScroll();
      });

      topElement.click(function(e)
      {
        e.stopPropagation();
      });

      bottomElement.mousedown(function(e)
      {
        e.stopPropagation();
        eXo.portal.VerticalScrollManager.scrollComponent(blockMenu.attr("id"), false, 15);
      });

      bottomElement.mouseup(function(e)
      {
        e.stopPropagation();
        eXo.portal.VerticalScrollManager.cancelScroll();
      });

      bottomElement.click(function(e)
      {
        e.stopPropagation();
      });
    }
    else
    {
      blockMenu.scrollTop(0);
      blockMenu.css({"height" : menuContainer[0].offsetHeight + "px", "overflowY" : ""});
      menuContainer[0].curentHeight = null;
      topElement.css("display", "none");
      bottomElement.css("display", "none");
    }
  },

  setContainerSize : function(menuItemContainer)
  {
    var jqObj = xj(menuItemContainer);
    var menuCenter = jqObj.find("div.StartMenuML").eq(0);
    var menuTop = jqObj.find("div.StartMenuTL").eq(0);
    var menuBottom = menuTop.nextAll(".StartMenuBL").eq(0);
    var w = (menuCenter[0].offsetWidth - menuTop.find("div.StartMenuTR")[0].offsetLeft) + "px";
    if (eXo.core.Browser.isIE7() && eXo.core.I18n.isRT())
    {
      w = menuCenter[0].offsetWidth + "px";
    }
    menuTop.css("width", w);
    menuBottom.css("width", w);
    menuCenter.css("width", w);
    menuItemContainer.resized = true;
  },

  initSlide : function(menuContainer, clipBottom)
  {
    menuContainer.curentHeight = clipBottom;
    menuContainer.style.top = 0 + "px";
  }
}
