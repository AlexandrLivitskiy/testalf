var username = person.properties.userName;

var personNode = people.getPerson(username);

var homeFolder = personNode.properties.homeFolder;

model.nodeRef = homeFolder.nodeRef.toString();;