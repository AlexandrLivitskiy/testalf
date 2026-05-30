<import resource="classpath:/alfresco/site-webscripts/org/alfresco/share/imports/share-header.lib.js">

var headerModel = getHeaderModel();
if (!user.isAdmin)
{
    headerModel[0].config.widgets[0].config.widgets =
        headerModel[0].config.widgets[0].config.widgets.filter(function(widget)
        {
            return widget.id != "HEADER_HOME" &&
                   widget.id != "HEADER_MY_FILES" &&
                   widget.id != "HEADER_SHARED_FILES" &&
                   widget.id != "HEADER_SITES_MENU" &&
                   widget.id != "HEADER_REPOSITORY" &&
                   widget.id != "HEADER_PEOPLE";
        });
}
model.jsonModel = {
   rootNodeId: "share-header",
   services: getHeaderServices(),
   widgets: [
      {
         id: "SHARE_VERTICAL_LAYOUT",
         name: "alfresco/layout/VerticalWidgets",
         config: 
         {
            widgets: headerModel
         }
      }
   ]
};
