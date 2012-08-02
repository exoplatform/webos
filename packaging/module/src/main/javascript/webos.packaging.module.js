eXo.require("eXo.projects.Module") ;
eXo.require("eXo.projects.Product") ;

function getModule(params) {

  var kernel = params.kernel;
  var core = params.core;
  var jcr = params.eXoJcr;

  var module = new Module();

  module.version = "${project.version}" ;
  module.relativeMavenRepo =  "org/exoplatform/webos" ;
  module.relativeSRCRepo =  "webos" ;
  module.name =  "webos" ;
                   
  module.web = {}
  module.web.webosResources = 
  	new Project("org.exoplatform.webos", "webos-resources", "war", module.version) ;
  module.web.webosResources.deployName="webosResources";
  
  module.web.extension = {};
  module.web.extension.war =
    new Project("org.exoplatform.webos", "webos-extension-war", "war", module.version).
    addDependency(new Project("org.exoplatform.webos", "webos-extension-config", "jar", module.version));
  module.web.extension.war.deployName = "webos-ext";

  module.component = {};
  module.component.web = new Project("org.exoplatform.webos", "webos-component-web", "jar", module.version);

  module.webui = {};
  module.webui.webos =
    new Project("org.exoplatform.webos", "webos-webui", "jar", module.version);
  
  module.server = {};

	module.server.tomcat = {}
	module.server.tomcat.patch =
	new Project("org.exoplatform.webos", "webos-server-tomcat-patch", "jar", module.version);
   module.portlet = new Project("org.exoplatform.webos", "webos-portlet-webosadmin", "war", module.version);
   module.portlet.deployName="webosadmin";
  return module;
}
