import org.exoplatform.webui.core.model.SelectItemOption ;
import org.exoplatform.webui.core.model.SelectItemCategory ;
import java.util.List;
import java.util.ArrayList;

  List options = new ArrayList() ;
  
  SelectItemCategory itemDesktop  = new SelectItemCategory("Desktop");
  itemDesktop.addSelectItemOption(new SelectItemOption("Page template",
                                  "system:/groovy/portal/webui/page/UIPageDesktop.gtmpl",
                                  "Description", "Desktop", false));  
  options.add(itemDesktop);


return options;

