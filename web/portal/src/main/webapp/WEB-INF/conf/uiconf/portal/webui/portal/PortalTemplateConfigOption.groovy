import java.util.List;
import java.util.ArrayList;
import org.exoplatform.portal.webui.portal.PortalTemplateConfigOption ;
import org.exoplatform.webui.core.model.SelectItemCategory;

List options = new ArrayList();

  SelectItemCategory webos = new SelectItemCategory("WebOSPortal");
  webos.addSelectItemOption(
    new PortalTemplateConfigOption("", "webos", "WebOS Portal", "WebOSPortal").addGroup("/platform/guests")
  );
  options.add(webos);
  
return options ;
