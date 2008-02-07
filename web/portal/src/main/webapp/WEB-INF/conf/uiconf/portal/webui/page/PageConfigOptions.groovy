import org.exoplatform.webui.core.model.SelectItemOption;
import org.exoplatform.webui.core.model.SelectItemCategory;

List categories = new ArrayList(); 
String config = null ;

SelectItemCategory normalPageConfigs = new SelectItemCategory("normalPageConfigs") ;
categories.add(normalPageConfigs);

config = "<page>" +         
         "  <owner-type></owner-type>" +
         "  <owner-id></owner-id>" +
         "  <name>UIPage</name>" +
         "  <factory-id>Desktop</factory-id>" +
         "</page>" ;

normalPageConfigs.addSelectItemOption(new SelectItemOption("Desktop Layout", config, "DesktopImage"));

return categories;