eXo.animation = {};

eXo.animation.ImplodeExplode =
{

  animWindow : null,

  doInit : function(uiWindow, clickedElement, desktopPage)
  {
    this.animWindow = xj("<div>").css({"display" : "block", "background-color" : "#ffffff", "position" : "absolute", "z-index" : uiWindow.style.zIndex}).fadeTo("fast", 50);
    desktopPage.append(this.animWindow);
  },

  explode : function(uiWindow, clickedElement, desktopPage, numberOfFrame)
  {
    if (!eXo.animation.ImplodeExplode.animWindow)
    {
      eXo.animation.ImplodeExplode.doInit(uiWindow, clickedElement, desktopPage);

      var animWindow = eXo.animation.ImplodeExplode.animWindow;
      animWindow.iconX = eXo.core.Browser.findPosXInContainer(clickedElement, desktopPage[0], eXo.core.I18n.isRT());
      animWindow.iconY = xj(clickedElement).offset().top - desktopPage.offset().top;
      animWindow.iconW = clickedElement.offsetWidth
      animWindow.iconH = clickedElement.offsetHeight;
      animWindow.originX = uiWindow.originalX;
      animWindow.originY = uiWindow.originalY;
      animWindow.originW = uiWindow.originalW;
      animWindow.originH = uiWindow.originalH;

      eXo.animation.ImplodeExplode.doExplode(numberOfFrame, numberOfFrame - 1, animWindow, xj(uiWindow));
    }
  },

  implode : function(uiWindow, clickedElement, desktopPage, numberOfFrame)
  {
    if (!eXo.animation.ImplodeExplode.animWindow)
    {
      eXo.animation.ImplodeExplode.doInit(uiWindow, clickedElement, desktopPage);

      var animWindow = eXo.animation.ImplodeExplode.animWindow;
      animWindow.iconX = eXo.core.Browser.findPosXInContainer(clickedElement, desktopPage[0], eXo.core.I18n.isRT());
      animWindow.iconY = xj(clickedElement).offset().top - desktopPage.offset().top;
      animWindow.iconW = clickedElement.offsetWidth
      animWindow.iconH = clickedElement.offsetHeight;
      animWindow.originY = uiWindow.offsetTop;
      animWindow.originX = eXo.core.I18n.isLT() ? uiWindow.offsetLeft : eXo.core.Browser.findPosXInContainer(uiWindow, uiWindow.offsetParent, true);
      animWindow.originW = uiWindow.offsetWidth;
      animWindow.originH = uiWindow.offsetHeight;

      if (uiWindow.style.display == "block")
      {
        uiWindow.style.display = "none";
      }

      eXo.animation.ImplodeExplode.doImplode(numberOfFrame, 1, animWindow);
    }
  },

  doImplode : function(numberOfFrame, index, animWindow)
  {
    eXo.animation.ImplodeExplode.adjustAnimWindow(numberOfFrame, index, animWindow);

    var newIndex = index + 1;
    if (newIndex < numberOfFrame)
    {
      setTimeout(function() { eXo.animation.ImplodeExplode.doImplode(numberOfFrame, newIndex, animWindow)}, 0);
    }
    else
    {
      animWindow.remove();
      eXo.animation.ImplodeExplode.animWindow = null;
    }
  },

  doExplode : function(numberOfFrame, index, animWindow, appWindow)
  {
    eXo.animation.ImplodeExplode.adjustAnimWindow(numberOfFrame, index, animWindow);

    var newIndex = index - 1;
    if (newIndex > 0)
    {
      setTimeout(function() { eXo.animation.ImplodeExplode.doExplode(numberOfFrame, newIndex, animWindow, appWindow)}, 0);
    }
    else
    {
      appWindow.css("top", animWindow.originY);
      if (eXo.core.I18n.isLT())
      {
        appWindow.css("left", animWindow.originX);
      }
      else
      {
        appWindow.css("right", animWindow.originX);
      }
      appWindow.css("width", (!appWindow[0].maximized) ? animWindow.originW : appWindow.css("width"));
      appWindow.css("display", "block");
      if (appWindow[0].maximized)
      {
        appWindow.css("height", "100%");
        var resizeBlock = appWindow.find("div.UIResizableBlock")[0];
        var topEle = appWindow.children("div.WindowBarLeft")[0];
        var bottomEle = appWindow.children("div.BottomDecoratorLeft")[0];
        if (resizeBlock)
        {
          resizeBlock.style.height = appWindow[0].clientHeight - topEle.offsetHeight - bottomEle.offsetHeight + "px";
        }
      }

      animWindow.remove();
      eXo.animation.ImplodeExplode.animWindow = null;
    }
  },

  adjustAnimWindow : function(numberOfFrame, index, animWindow)
  {
    var Y = animWindow.originY + index * (animWindow.iconY - animWindow.originY) / numberOfFrame;
    var X = animWindow.originX + (Y - animWindow.originY) * (animWindow.iconX - animWindow.originX) / (animWindow.iconY - animWindow.originY);
    var W = (animWindow.originW - animWindow.iconW) * (numberOfFrame - index) / numberOfFrame + animWindow.iconW;
    var H = (animWindow.originH - animWindow.iconH) * (numberOfFrame - index) / numberOfFrame + animWindow.iconH;

    animWindow.css({"top" : Y, "width" : W, "height" : H});
    if (eXo.core.I18n.isLT())
    {
      animWindow.css("left", X);
    }
    else
    {
      animWindow.css("right", X);
    }
  }
}
