<% for(apiGroup in apiGroups){ %>

# ${apiGroup.name}<% for(api in apiGroup.apis){ %>
* [${api.name}](#${api.id})<%}%>
<%}%>