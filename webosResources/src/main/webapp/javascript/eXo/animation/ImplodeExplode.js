//TODO: Refactor this shitty ImplodeExplode.js
ImplodeExplode = function() {
} ;

/*TODO: it has a confusion posX and posY */
ImplodeExplode.prototype.doInit = function(uiWindow, clickedElement, desktopPage, numberOfFrame) {
	
	this.object = uiWindow ;
	this.object.loop = numberOfFrame ;
	this.object.iconY = xj(clickedElement).offset().top - desktopPage.offset().top;
	this.object.iconX = eXo.core.Browser.findPosXInContainer(clickedElement, desktopPage[0], eXo.core.I18n.isRT()) ;
	this.object.iconW = clickedElement.offsetWidth ;
	this.object.iconH = clickedElement.offsetHeight ;

	if(this.object.animation == null) {		
		this.object.animation = document.createElement("div") ;
    desktopPage.append(this.object.animation);
		this.object.animation.style.display = "block" ;
		this.object.animation.style.background = "#ffffff" ;
		this.object.animation.style.position = "absolute" ;
    xj(this.object.animation).fadeTo("fast", 50);
		this.object.animation.style.zIndex = this.object.style.zIndex ;
	}
} ;

/*
 * minh.js.exo
 * fix bug speed click in dockbar.
 * reference -> variable this.busy in method ...doExplode ...doImplode
 */
ImplodeExplode.prototype.explode = function(uiWindow, clickedElement, desktopPage, numberOfFrame) {
  if (!this.busy)
  {
    eXo.animation.ImplodeExplode.doInit(uiWindow, clickedElement, desktopPage, numberOfFrame);
    this.object.step = numberOfFrame - 1;
    this.object.isShowed = true;
    eXo.animation.ImplodeExplode.doExplode(desktopPage);
  }
} ;

ImplodeExplode.prototype.implode = function(uiWindow, clickedElement, desktopPage, numberOfFrame) {
  if (!this.busy)
  {
    eXo.animation.ImplodeExplode.doInit(uiWindow, clickedElement, desktopPage, numberOfFrame);
    this.object.originalY = this.object.offsetTop;
    if (eXo.core.I18n.isLT())
    {
      this.object.originalX = this.object.offsetLeft;
    }
    else
    {
      this.object.originalX = eXo.core.Browser.findPosXInContainer(this.object, this.object.offsetParent, true);
    }
    this.object.originalW = this.object.offsetWidth;
    this.object.originalH = this.object.offsetHeight;
    this.object.step = 1;
    if (this.object.style.display == "block")
    {
      this.object.style.display = "none";
    }

    eXo.animation.ImplodeExplode.doImplode(desktopPage) ;
	} 
} ;

ImplodeExplode.prototype.doImplode = function(desktopPage) {
	this.busy = true ;
	var win = this.object ;
	var Y0 = win.originalY + (win.step*(win.iconY - win.originalY))/win.loop ;
	var X0 = win.originalX + ((Y0 - win.originalY)*(win.iconX - win.originalX))/(win.iconY - win.originalY) ;
	var W0 = ((win.originalW - win.iconW)*(win.loop - win.step))/win.loop + win.iconW ;
	var H0 = ((win.originalH - win.iconH)*(win.loop - win.step))/win.loop + win.iconH ;

	win.animation.style.top = Y0 + "px" ;
	if(eXo.core.I18n.isLT()) win.animation.style.left = X0 + "px" ;
	else win.animation.style.right = X0 + "px" ;
	win.animation.style.width = W0 + "px" ;
	win.animation.style.height = H0 + "px" ;

	win.step++ ;
	if(W0 > win.iconW) {
    setTimeout(function(){ eXo.animation.ImplodeExplode.doImplode(desktopPage);}, 0);
	}	else {
    xj(win.animation).remove();
		win.animation = null ;
		this.busy = false ;
	}

} ;

ImplodeExplode.prototype.doExplode = function(desktopPage) {
			this.busy = true ;
			var win = this.object ;
		
			var Y0 = win.originalY + (win.step*(win.iconY - win.originalY))/win.loop ;
			var X0 = win.originalX + ((Y0 - win.originalY)*(win.iconX - win.originalX))/(win.iconY - win.originalY) ;
			var W0 = ((win.originalW - win.iconW)*(win.loop - win.step))/win.loop + win.iconW ;
			var H0 = ((win.originalH - win.iconH)*(win.loop - win.step))/win.loop + win.iconH ;
			
			win.animation.style.top = Y0 + "px" ;
			if(eXo.core.I18n.isLT()) win.animation.style.left = X0 + "px" ;
			else win.animation.style.right = X0 + "px" ;
			win.animation.style.width = W0 + "px" ;
			win.animation.style.height = H0 + "px" ;
			
			win.step-- ;
			
			if(W0 < win.originalW) {
        setTimeout(function(){ eXo.animation.ImplodeExplode.doExplode(desktopPage);}, 0);
			} else {
				win.style.top = Y0 + "px" ;
				if(eXo.core.I18n.isLT()) win.style.left = X0 + "px" ;
				else win.style.right = X0 + "px" ;
				win.style.width = (!win.maximized) ? W0 + "px" : win.style.width ;
				win.style.display = "block" ;
				if(win.maximized) {
          var jqObj = xj(win);
          jqObj.css("height", "100%");
					var resizeBlock = jqObj.find("div.UIResizableBlock")[0];
					var topEle = jqObj.children("div.WindowBarLeft")[0];
					var bottomEle = jqObj.children("div.BottomDecoratorLeft")[0];
					if(resizeBlock) resizeBlock.style.height = win.clientHeight - topEle.offsetHeight - bottomEle.offsetHeight +"px";
				}
				xj(win.animation).remove();
				win.animation = null ;
				this.busy = false ;
			}
			
} ;

eXo.animation = {};
eXo.animation.ImplodeExplode = new ImplodeExplode() ;