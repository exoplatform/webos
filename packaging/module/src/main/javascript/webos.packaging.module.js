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
  	new Project("org.exoplatform.webos", "exo.webos.web.webosResources", "war", module.version) ;
  
  module.web.extension = {};
  module.web.extension.war =
    new Project("org.exoplatform.webos", "exo.webos.web.extension.war", "war", module.version).
    addDependency(new Project("org.exoplatform.webos", "exo.webos.web.extension.jar", "jar", module.version)).
    addDependency(new Project("org.exoplatform.webos", "exo.webos.web.extension.config", "jar", module.version));
  module.web.extension.war.deployName = "webos-ext";

  module.webui = {};
  module.webui.webos =
    new Project("org.exoplatform.webos", "exo.webos.webui.webos", "jar", module.version);
  
  module.server = {};

	module.server.tomcat = {}
	module.server.tomcat.patch =
	new Project("org.exoplatform.webos", "exo.webos.server.tomcat.patch", "jar", module.version);

  return module;
}
