package ua.edu.onu.workflow;

import com.google.gson.JsonObject;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.jscript.ScriptNode;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.alfresco.repo.security.authentication.AuthenticationUtil;
import org.alfresco.service.cmr.repository.*;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.json.JSONObject;
import ua.edu.onu.google.GoogleAccessService;
import ua.edu.onu.google.GoogleCommonService;
import ua.edu.onu.google.GoogleDocService;
import org.springframework.web.context.ContextLoader;

import java.io.InputStream;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class CreateGoogleDocDelegate implements JavaDelegate {
    private static final String NS = "http://www.testalf.onu.edu.ua/model/workflow/1.0";
    private static final String NO_CREATE_GOOGLE_DOC = "(-------)";

    private static final QName TYPE_GOOGLE_FILE = QName.createQName(NS, "googleDriveFile");
    private NodeService nodeService;
    private ContentService contentService;
    private GoogleDocService googleDocService;
    private GoogleAccessService googleAccessService;

    public void setNodeService(NodeService nodeService) {
        this.nodeService = nodeService;
    }

    public void setContentService(ContentService contentService) {
        this.contentService = contentService;
    }
    @Override
    public void execute(DelegateExecution execution) throws Exception{
        if (googleDocService == null) {
            googleDocService = (GoogleDocService) ContextLoader
                    .getCurrentWebApplicationContext()
                    .getBean("googleDocService");
        }
        if (googleAccessService == null) {
            googleAccessService = (GoogleAccessService) ContextLoader
                    .getCurrentWebApplicationContext()
                    .getBean("googleAccessService");
        }
        if (nodeService == null) {
            nodeService = (NodeService) ContextLoader
                    .getCurrentWebApplicationContext()
                    .getBean("NodeService");
        }
        if (contentService == null) {
            contentService = (ContentService) ContextLoader
                    .getCurrentWebApplicationContext()
                    .getBean("ContentService");
        }

        String googleDocType = getGoogleDocType((String) execution.getVariable("tawf_googleDocType"));
        ScriptNode initiator = (ScriptNode) execution.getVariable("initiator");
        String initiatorEmail = (String) initiator.getProperties().get("cm:email");
        ScriptNode assignee = (ScriptNode) execution.getVariable("bpm_assignee");
        String assigneeEmail = (String) assignee.getProperties().get("cm:email");

        String folderRefStr = (String) execution.getVariable("tawf_googleDriveUploadFiles");
        List<FileData> googleDriveFiles = Collections.synchronizedList(new ArrayList<>());

        ExecutorService pool = Executors.newFixedThreadPool(5);
        List<Future<?>> futures = new ArrayList<>();
        JsonObject doc = new JsonObject();
        String accessToken = ((GoogleCommonService) ContextLoader
                .getCurrentWebApplicationContext()
                .getBean("googleCommonService"))
                .getAlfrescoAccessToken();

        if (!googleDocType.equals(NO_CREATE_GOOGLE_DOC)) {
            String googleDocName = addPrefix((String) execution.getVariable("bpm_workflowDescription"));
            futures.add(pool.submit(() -> {
                AuthenticationUtil.runAs(() -> {
                    String json = googleDocService.createDoc(googleDocType, googleDocName, accessToken);

                    JSONObject obj = new JSONObject(json);
                    String fileId = obj.getString("id");
                    String mimeType = obj.getString("mimeType");

                    grantUsers(fileId, "writer", accessToken, initiatorEmail, assigneeEmail);

//        execution.setVariable("tawf_description", googleDocURL);
                    String googleDocURL = getGoogleDocURL(mimeType, fileId);
                    doc.addProperty("name", googleDocName);
                    doc.addProperty("url", googleDocURL);
                    doc.addProperty("icon", "https://upload.wikimedia.org/wikipedia/commons/thumb/1/12/Google_Drive_icon_%282020%29.svg/3840px-Google_Drive_icon_%282020%29.svg.png");
                    return null;
                }, AuthenticationUtil.getSystemUserName());
            }));
        }

        NodeRef folderRef = null;
        if (folderRefStr != null && !folderRefStr.trim().isEmpty()) {
            folderRef = new NodeRef(folderRefStr);
            if (nodeService.exists(folderRef)) {
                List<ChildAssociationRef> children = nodeService.getChildAssocs(folderRef);
                for (ChildAssociationRef child : children) {
                    futures.add(pool.submit(() -> {
                        AuthenticationUtil.runAs(() -> {
                            try {
                                NodeRef fileNode = child.getChildRef();
                                ContentReader reader = contentService.getReader(fileNode, ContentModel.PROP_CONTENT);
                                try (InputStream is = reader.getContentInputStream()) {
                                    String fileName = addPrefix((String) nodeService.getProperty(fileNode, ContentModel.PROP_NAME));
                                    String fileMimeType = reader.getMimetype();

                                    String fileInfo = googleDocService.uploadFileToDrive(is, fileName, fileMimeType, accessToken);
                                    JSONObject obj = new JSONObject(fileInfo);
                                    String fileId = obj.getString("id");
                                    grantUsers(fileId, "writer", accessToken, initiatorEmail, assigneeEmail);
                                    String docUrl = obj.getString("webViewLink").split("\\?")[0];
                                    String fileMimeTypeGD = obj.getString("mimeType");
                                    googleDriveFiles.add(new FileData(fileName, docUrl, fileId, fileMimeTypeGD));
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            return null;
                        }, AuthenticationUtil.getSystemUserName());
                    }));
                }
                nodeService.deleteNode(folderRef);
            }
        }

        for (Future<?> f : futures) {
            try {
                f.get();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        pool.shutdown();

        StringBuilder formattedDataListSB = new StringBuilder();
        for (FileData fileData : googleDriveFiles) {
            formattedDataListSB = formatDataForWFField(formattedDataListSB, fileData);
        }
        if (!googleDocType.equals(NO_CREATE_GOOGLE_DOC)) execution.setVariable("tawf_googleDocLink", doc.toString());
        execution.setVariable("tawf_googleDriveFiles", formattedDataListSB.toString());
        if (folderRef != null && nodeService.exists(folderRef)) nodeService.deleteNode(folderRef);
    }

    private NodeRef createGoogleDriveMetaNode(
            NodeRef packageNode,
            String fileId,
            String fileName,
            String url,
            String imgSrcIcon,
            String mimeType,
            NodeService nodeService
    ) {

        String safeName = QName.createValidLocalName("gfile_" + fileId);

        QName assocQName = QName.createQName(
                NamespaceService.CONTENT_MODEL_1_0_URI,
                safeName
        );

//        Map<QName, Serializable> props = new HashMap<>();
//        props.put(QName.createQName(NS, "fileIdGD"), fileId);
//        props.put(QName.createQName(NS, "fileNameGD"), fileName);
//        props.put(QName.createQName(NS, "fileUrlGD"), url);
//        props.put(QName.createQName(NS, "imgSrcIconGD"), imgSrcIcon);
//        props.put(QName.createQName(NS, "mimeTypeGD"), mimeType);
//
//        ChildAssociationRef childRef = nodeService.createNode(
//                packageNode,
//                ContentModel.ASSOC_CONTAINS,
//                assocQName,
//                TYPE_GOOGLE_FILE,
//                props
//        );

//        return childRef.getChildRef();
        ChildAssociationRef childRef = nodeService.createNode(
                packageNode,
                ContentModel.ASSOC_CONTAINS,
                assocQName,
                TYPE_GOOGLE_FILE
        );

        NodeRef child = childRef.getChildRef();
        nodeService.setProperty(child, ContentModel.PROP_NAME, fileName);
        nodeService.setProperty(child, ContentModel.PROP_DESCRIPTION, url);
        nodeService.setProperty(child, QName.createQName(NS, "fileIdGD"), fileId);
        nodeService.setProperty(child, QName.createQName(NS, "fileNameGD"), fileName);
        nodeService.setProperty(child, QName.createQName(NS, "fileUrlGD"), url);
        nodeService.setProperty(child, QName.createQName(NS, "imgSrcIconGD"), imgSrcIcon);
        nodeService.setProperty(child, QName.createQName(NS, "mimeTypeGD"), mimeType);

//        ContentWriter writer = contentService.getWriter(child, ContentModel.PROP_CONTENT, true);
//
//        writer.setMimetype(mimeType != null ? mimeType : "application/octet-stream");
//        writer.setEncoding("UTF-8");
//
//        writer.putContent(" ");
//        return childRef.getChildRef();
        return child;
    }

    private void addUploadedFilesToWFField(NodeRef nodeRef, FileData fileData) {
        String formattedData;
        QName PROP = QName.createQName(NS, "googleDriveFiles");
        Serializable value = nodeService.getProperty(nodeRef, PROP);
        List<String> list;
        if (value == null) {
            list = new ArrayList<>();
        } else {
            list = new ArrayList<>((List<String>) value);
        }
        JsonObject fileGD = new JsonObject();
        fileGD.addProperty("name", fileData.getFileName());
        fileGD.addProperty("url", fileData.getDocUrl());
        fileGD.addProperty("icon", "https://images.icon-icons.com/2631/PNG/512/google_docs_new_logo_icon_159146.png");
        list.add(fileGD.toString());
        nodeService.setProperty(nodeRef, PROP, (Serializable) list);
    }

    private StringBuilder formatDataForWFField(StringBuilder sb, FileData fileData) {
        if (sb.length() > 0) {
            sb.append(";");
        }
        String fileName = fileData.getFileName().replace(";", ".,")
                .replace("|", "I")
                .trim();
        sb.append(fileName).append("|").append(fileData.getDocUrl());
        return sb;
    }

    private void grantUsers(String fileId, String role, String accessToken, String... emails) {
        Arrays.stream(emails)
                .parallel()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .forEach(email ->
                        googleAccessService.grantAccessToUser(email, fileId, role, accessToken)
                );
    }

    private String addPrefix(String fileName) {
        String PREFIX = "Alfresco";
        if (!fileName.startsWith(PREFIX)) {
            fileName = PREFIX + " " + fileName;
        }
        return fileName;
    }

    private String getGoogleDocType(String googleDocTypeConstraint) {
        Map<String, String> MIME_TYPES = new HashMap<>();
        MIME_TYPES.put(NO_CREATE_GOOGLE_DOC, NO_CREATE_GOOGLE_DOC);
        MIME_TYPES.put("Документ", "doc");
        MIME_TYPES.put("Таблиця", "sheet");
        MIME_TYPES.put("Презентація", "slide");
        MIME_TYPES.put("План розробки продукту", "14r4cPLaEcqoktRD2VxEdiXLhZapEPXJS886mNTOTd1w");
        MIME_TYPES.put("Чернетка електронного листа", "1Kd7dl0LqYrqxcBM5aMREJ-iJ8v5cHTLa-bfvBsNWK0w");
        MIME_TYPES.put("Менеджмент 2020-2024  4  курс", "1RVKR8NBCDgE4-w1G8JIOTXa5yOlJ1Z4BwH-PV1aOlzo");

        String type = MIME_TYPES.get(googleDocTypeConstraint);
        if (type == null) throw new RuntimeException("Wrong google doc type: " + googleDocTypeConstraint);
        return type;
    }

    private String getGoogleDocURL(String mimeType, String fileId) {
        Map<String, String> MIME_TYPES = new HashMap<>();
        MIME_TYPES.put("application/vnd.google-apps.document", "document");
        MIME_TYPES.put("application/vnd.google-apps.spreadsheet", "spreadsheets");
        MIME_TYPES.put("application/vnd.google-apps.presentation", "presentation");
        return "https://docs.google.com/" + MIME_TYPES.get(mimeType) + "/d/" + fileId;
    }

    private static class FileData {

        private String fileName;
        private String docUrl;
        private String fileId;
        private String fileMimeType;

        public FileData() {
        }

        public FileData(String fileName, String docUrl, String fileId, String fileMimeType) {
            this.fileName = fileName;
            this.docUrl = docUrl;
            this.fileId = fileId;
            this.fileMimeType = fileMimeType;
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public String getDocUrl() {
            return docUrl;
        }

        public void setDocUrl(String docUrl) {
            this.docUrl = docUrl;
        }

        public String getFileId() {
            return fileId;
        }

        public void setFileId(String fileId) {
            this.fileId = fileId;
        }

        public String getFileMimeType() {
            return fileMimeType;
        }

        public void setFileMimeType(String fileMimeType) {
            this.fileMimeType = fileMimeType;
        }

        @Override
        public String toString() {
            return "FileData{" +
                    "fileName='" + fileName + '\'' +
                    ", docUrl='" + docUrl + '\'' +
                    ", fileId='" + fileId + '\'' +
                    ", fileMimeType='" + fileMimeType + '\'' +
                    '}';
        }
    }
}

