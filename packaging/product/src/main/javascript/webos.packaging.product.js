eXo.require("eXo.projects.Module") ;
eXo.require("eXo.projects.Product") ;

function getProduct(version) {
  var product = new Product();
  
  product.name = "eXoWebOS" ;
  product.portalwar = "portal.war" ;
  product.codeRepo = "webos" ;
  product.useWorkflow = false;
  product.version = "${project.version}" ;
  product.serverPluginVersion = "${org.exoplatform.portal.version}" ;
    
  var kernel = Module.GetModule("kernel") ;
  var core = Module.GetModule("core") ;
  var ws = Module.GetModule("ws");
  var eXoJcr = Module.GetModule("jcr", {kernel : kernel, core : core, ws : ws}) ;
  var portal = Module.GetModule("portal", {kernel : kernel, ws:ws, core : core,eXoJcr : eXoJcr});
  var webos = Module.GetModule("webos", {kernel : kernel, core : core, eXoJcr : eXoJcr });
  
  product.addDependencies(portal.web.rest) ;
  product.addDependencies(portal.portlet.exoadmin) ;
  product.addDependencies(portal.portlet.web) ;
  product.addDependencies(portal.portlet.dashboard) ;
	product.addDependencies(portal.eXoGadgetServer) ;
	product.addDependencies(portal.eXoGadgets) ;
  product.addDependencies(portal.webui.portal);
  
	product.addDependencies(portal.web.eXoResources);
	product.addDependencies(webos.web.webosResources);
	product.addDependencies(webos.webui.webos);
        product.addDependencies(portal.web.portal) ;
 
  portal.starter = new Project("org.exoplatform.portal", "exo.portal.starter.war", "war", portal.version);
  portal.starter.deployName = "starter";
  
  product.addDependencies(portal.starter);
  product.addDependencies(webos.web.extension.war);

  product.addDependencies(portal.ibm.jdk.support);
	
 /* product.addDependencies(webos.web.webosportal) ; */
  
  product.addServerPatch("tomcat", webos.server.tomcat.patch) ;
  //product.addServerPatch("jboss",  portal.server.jboss.patch) ;
  //product.addServerPatch("jbossear",  portal.server.jbossear.patch) ;
  
  /* cleanup duplicated lib */
  product.removeDependency(new Project("commons-httpclient", "commons-httpclient", "jar", "3.0"));
  product.removeDependency(new Project("commons-collections", "commons-collections", "jar", "3.1"));
  product.removeDependency(new Project("commons-collections", "commons-collections", "jar", "3.2"));
  
  product.module = webos ;
  product.dependencyModule = [kernel, core, ws, eXoJcr, portal];
    
  return product ;
}
