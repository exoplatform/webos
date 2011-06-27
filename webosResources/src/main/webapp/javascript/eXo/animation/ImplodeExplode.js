ImplodeExplode = function() {
} ;

/*TODO: it has a confusion posX and posY */
ImplodeExplode.prototype.doInit = function(uiWindow, clickedElement, containerId, numberOfFrame) {
	
	var container = document.getElementById(containerId) ;
	this.object = uiWindow ;
	this.object.loop = numberOfFrame ;
	this.object.iconY = eXo.core.Browser.findPosYInContainer(clickedElement, container) ;
	this.object.iconX = eXo.core.Browser.findPosXInContainer(clickedElement, container, eXo.core.I18n.isRT()) ;
	this.object.iconW = clickedElement.offsetWidth ;
	this.object.iconH = clickedElement.offsetHeight ;

	if(this.object.animation == null) {		
		this.object.animation = document.createElement("div") ;		
		container.appendChild(this.object.animation) ;	
		this.object.animation.style.display = "block" ;
		this.object.animation.style.background = "#ffffff" ;
		this.object.animation.style.position = "absolute" ;	
		eXo.core.Browser.setOpacity(this.object.animation, 50) ;
		this.object.animation.style.zIndex = this.object.style.zIndex ;
	}
} ;

ImplodeExplode.prototype.doCenterInit = function(uiWindow, clickedElement, containerId, numberOfFrame) {
	var container = document.getElementById(containerId) ;

	this.object = uiWindow ;
	this.object.loop = numberOfFrame ;
	if(this.object.style.display == "none") {
		this.object.iconY = this.object.originalY + this.object.originalH/2 ;
		this.object.iconX = this.object.originalX + this.object.originalW/2 ;
	} else {
		this.object.iconY = eXo.core.Browser.findPosYInContainer(this.object, container) + this.object.offsetHeight/2 ;
		this.object.iconX = eXo.core.Browser.findPosXInContainer(this.object, container, eXo.core.I18n.isRT()) + this.object.offsetWidth/2 ;
	}
	this.object.iconW = 1 ;
	this.object.iconH = 1 ;
	if(this.object.animation == null) {
		this.object.animation = document.createElement("div") ;
		container.appendChild(this.object.animation) ;
		this.object.animation.style.display = "block" ;
		this.object.animation.style.background = "white" ;
		this.object.animation.style.position = "absolute" ;
		eXo.core.Browser.setOpacity(this.object.animation, 40) ;
		this.object.animation.style.zIndex = this.object.style.zIndex ;
	}
	uiWindow = this.object;
} ;

/*
 * minh.js.exo
 * fix bug speed click in dockbar.
 * reference -> variable this.busy in method ...doExplode ...doImplode
 */
ImplodeExplode.prototype.explode = function(uiWindow, clickedElement, containerId, numberOfFrame, type) {
	if (!this.busy) {
		if(type) {
			eXo.animation.ImplodeExplode.doCenterInit(uiWindow, clickedElement, containerId, numberOfFrame) ;
		} else {
			eXo.animation.ImplodeExplode.doInit(uiWindow, clickedElement, containerId, numberOfFrame) ;
		}
		this.object.step = numberOfFrame - 1 ;
		this.object.isShowed = true ;
		eXo.animation.ImplodeExplode.doExplode(containerId) ;
	}
} ;

ImplodeExplode.prototype.implode = function(uiWindow, clickedElement, containerId, numberOfFrame, type) {
	if (!this.busy) {	
		if(type) {
			eXo.animation.ImplodeExplode.doCenterInit(uiWindow, clickedElement, containerId, numberOfFrame) ;
		} else {
			eXo.animation.ImplodeExplode.doInit(uiWindow, clickedElement, containerId, numberOfFrame) ;
		}
		this.object.originalY = this.object.offsetTop ;
		if(eXo.core.I18n.isLT()) this.object.originalX = this.object.offsetLeft ;
		else this.object.originalX = eXo.core.Browser.findPosXInContainer(this.object, this.object.offsetParent, true) ;
		this.object.originalW = this.object.offsetWidth ;
		this.object.originalH = this.object.offsetHeight ;
		this.object.step = 1 ;
		if(this.object.style.display == "block") {
			this.object.style.display = "none" ;
		}
	
		eXo.animation.ImplodeExplode.doImplode(containerId) ;
	} 
} ;

ImplodeExplode.prototype.doImplode = function(containerId) {
	this.busy = true ;
	var container = document.getElementById(containerId) ;
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
		setTimeout("eXo.animation.ImplodeExplode.doImplode('" + containerId + "');", 0) ;
	}	else {
		container.removeChild(win.animation) ;
		win.animation = null ;
		this.busy = false ;
	}

} ;

ImplodeExplode.prototype.doExplode = function(containerId ) {
			this.busy = true ;
			var container = document.getElementById(containerId) ;
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
				setTimeout("eXo.animation.ImplodeExplode.doExplode('" + containerId + "');", 0) ;
			} else {
				win.style.top = Y0 + "px" ;
				if(eXo.core.I18n.isLT()) win.style.left = X0 + "px" ;
				else win.style.right = X0 + "px" ;
				win.style.width = (!win.maximized) ? W0 + "px" : win.style.width ;
				win.style.height = H0 + "px" ;
				win.style.display = "block" ;
				if(win.maximized) {
					var pageDesktop = eXo.core.DOMUtil.findAncestorByClass(win, "UIPageDesktop");
					win.style.height = "100%";
					var resizeBlock = eXo.core.DOMUtil.findFirstDescendantByClass(win, "div", "UIResizableBlock");
					var topEle = eXo.core.DOMUtil.findFirstChildByClass(win, "div", "WindowBarLeft");
					var bottomEle = eXo.core.DOMUtil.findFirstChildByClass(win, "div", "BottomDecoratorLeft");
					if(resizeBlock) resizeBlock.style.height = win.clientHeight - topEle.offsetHeight - bottomEle.offsetHeight +"px";
				}
				container.removeChild(win.animation) ;
				win.animation = null ;
				this.busy = false ;
			}
			
} ;

eXo.animation.ImplodeExplode = new ImplodeExplode() ;