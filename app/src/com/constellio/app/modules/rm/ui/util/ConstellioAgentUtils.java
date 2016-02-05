package com.constellio.app.modules.rm.ui.util;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.UserDocumentVO;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.model.conf.FoldersLocator;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.UserDocument;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.SchemaUtils;
import com.vaadin.server.Page;
import com.vaadin.server.VaadinServlet;
import com.vaadin.server.WebBrowser;

public class ConstellioAgentUtils {

	public static final String AGENT_DOWNLOAD_URL = "http://constellio.com/agent/";

	public static boolean isAgentSupported() {
		if (Page.getCurrent() == null || Page.getCurrent().getWebBrowser() == null
				|| Page.getCurrent().getWebBrowser().getAddress() == null) {
			return false;
		}
		String address = Page.getCurrent().getWebBrowser().getAddress();
		WebBrowser webBrowser = Page.getCurrent().getWebBrowser();
		return webBrowser.isWindows() || webBrowser.isMacOSX() || address.contains("127.0.0.1");
	}

	public static String getAgentBaseURL() {
		String agentBaseURL;
		// FIXME Should not obtain ConstellioFactories through singleton
		ModelLayerFactory modelLayerFactory = ConstellioFactories.getInstance().getModelLayerFactory();
		SystemConfigurationsManager systemConfigurationsManager = modelLayerFactory.getSystemConfigurationsManager();
		RMConfigs rmConfigs = new RMConfigs(systemConfigurationsManager);
		if (rmConfigs.isAgentEnabled()) {
			Page page = Page.getCurrent();
			URI location = page.getLocation();
			String contextPath = VaadinServlet.getCurrent().getServletContext().getContextPath();

			String schemeSpecificPart = location.getSchemeSpecificPart().substring(2);
			String schemeSpecificPartBeforeContextPath;
			if (StringUtils.isNotBlank(contextPath)) {
				if (schemeSpecificPart.indexOf(contextPath) != -1) {
					schemeSpecificPartBeforeContextPath = StringUtils.substringBeforeLast(schemeSpecificPart, contextPath);
				} else {
					contextPath = null;
					schemeSpecificPartBeforeContextPath = StringUtils.removeEnd(schemeSpecificPart, "/");
				}
			} else {
				schemeSpecificPartBeforeContextPath = StringUtils.removeEnd(schemeSpecificPart, "/");
			}

			StringBuffer sb = new StringBuffer();
			sb.append(location.getScheme());
			sb.append("://");
			sb.append(schemeSpecificPartBeforeContextPath);
			if (StringUtils.isNotBlank(contextPath)) {
				sb.append(contextPath);
			}
			sb.append("/agentPath");
			agentBaseURL = sb.toString();
		} else {
			agentBaseURL = null;
		}
		return agentBaseURL;
	}

	public static String getAgentInitURL() {
		String agentBaseURL = getAgentBaseURL();
		return addConstellioProtocol(agentBaseURL);
	}

	public static String getAgentURL(RecordVO recordVO, ContentVersionVO contentVersionVO) {
		String agentURL;
		if (recordVO != null && contentVersionVO != null && isAgentSupported()) {
			// FIXME Should not obtain ConstellioFactories through singleton
			ModelLayerFactory modelLayerFactory = ConstellioFactories.getInstance().getModelLayerFactory();
			SystemConfigurationsManager systemConfigurationsManager = modelLayerFactory.getSystemConfigurationsManager();
			RMConfigs rmConfigs = new RMConfigs(systemConfigurationsManager);
			if (rmConfigs.isAgentEnabled() && (!(recordVO instanceof UserDocumentVO) || rmConfigs.isAgentEditUserDocuments())) {
				String resourcePath = getResourcePath(recordVO, contentVersionVO);
				if (resourcePath != null) {
					String agentBaseURL = getAgentBaseURL();
					StringBuffer sb = new StringBuffer();
					sb.append(agentBaseURL);
					sb.append(resourcePath);
					agentURL = sb.toString();
				} else {
					agentURL = null;
				}
			} else {
				agentURL = null;
			}
		} else {
			agentURL = null;
		}
		return addConstellioProtocol(agentURL);
	}
	
	public static String getAgentSmbURL(String smbPath) {
		String agentSmbURL;
		
		String passthroughPath;
		WebBrowser webBrowser = Page.getCurrent().getWebBrowser();
		if (webBrowser.isWindows()) {
			passthroughPath = StringUtils.replace(smbPath, "/", "\\");
			passthroughPath = StringUtils.removeStart(passthroughPath, "smb:");
		} else {
			passthroughPath = smbPath;
		}

		String agentBaseURL = getAgentBaseURL();
		StringBuffer sb = new StringBuffer();
		sb.append(agentBaseURL);
		sb.append("/passthrough/");
		sb.append(passthroughPath);
		agentSmbURL = sb.toString();
		return addConstellioProtocol(agentSmbURL);
	}

	private static String getResourcePath(RecordVO recordVO, ContentVersionVO contentVersionVO) {
		String resourcePath;

		ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
		ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();
		RecordServices recordServices = modelLayerFactory.newRecordServices();

		Record record = recordServices.getDocumentById(recordVO.getId());
		String schemaCode = record.getSchemaCode();
		String schemaTypeCode = new SchemaUtils().getSchemaTypeCode(schemaCode);

		String collectionName = ConstellioUI.getCurrentSessionContext().getCurrentCollection();
		UserVO currentUserVO = ConstellioUI.getCurrentSessionContext().getCurrentUser();
		String currentUsername = currentUserVO.getUsername();
		String currentUserId = currentUserVO.getId();

		MetadataSchemaTypes types = types();

		if (UserDocument.SCHEMA_TYPE.equals(schemaTypeCode)) {
			UserDocument userDocument = new UserDocument(record, types);
			if (currentUserId.equals(userDocument.getUser())) {
				StringBuffer sb = new StringBuffer();
				sb.append("/");
				sb.append(currentUsername);
				sb.append("/");
				sb.append(collectionName);
				sb.append("/userDocuments");
				sb.append("/");
				sb.append(userDocument.getId());
				sb.append("/");
				sb.append(contentVersionVO.getFileName());
				resourcePath = sb.toString();
			} else {
				resourcePath = null;
			}
		} else if (Document.SCHEMA_TYPE.equals(schemaTypeCode)) {
			Document document = new Document(record, types);
			Content content = document.getContent();
			if (content == null) {
				resourcePath = null;
			} else if (currentUserId.equals(content.getCheckoutUserId())) {
				StringBuffer sb = new StringBuffer();
				sb.append("/");
				sb.append(currentUsername);
				sb.append("/");
				sb.append(collectionName);
				sb.append("/checkedOutDocuments");
				sb.append("/");
				sb.append(document.getId());
				sb.append("/");
				sb.append(contentVersionVO.getFileName());
				resourcePath = sb.toString();
			} else {
				StringBuffer sb = new StringBuffer();
				sb.append("/");
				sb.append(currentUsername);
				sb.append("/");
				sb.append(collectionName);
				sb.append("/notCheckedOutDocuments");
				sb.append("/");
				sb.append(document.getId());
				sb.append("/");
				sb.append(contentVersionVO.getFileName());
				resourcePath = sb.toString();
			}
		} else {
			resourcePath = null;
		}

		return resourcePath;
	}

	private static final MetadataSchemaTypes types() {
		String collectionName = ConstellioUI.getCurrentSessionContext().getCurrentCollection();
		ModelLayerFactory modelLayerFactory = ConstellioFactories.getInstance().getModelLayerFactory();
		MetadataSchemasManager metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
		return metadataSchemasManager.getSchemaTypes(collectionName);
	}

	public static String addConstellioProtocol(String url) {
		String agentURL;
		if (url != null) {
			String encoding;
			if (Page.getCurrent().getWebBrowser().isWindows()) {
				encoding = "cp1252";
			} else {
				// TODO Validate after implementing the agent for other OS.
				encoding = "UTF-8";
			}
			String encodedURL; 
			try {
				encodedURL = URLEncoder.encode(url, encoding);
			} catch (UnsupportedEncodingException e) {
				if ("cp1252".equals(encoding)) {
					try {
						encodedURL = URLEncoder.encode(url, "ISO-8859-1");
					} catch (UnsupportedEncodingException e2) {
						throw new RuntimeException(e2);
					}
				} else {
					throw new RuntimeException(e);
				}
			}
			agentURL = "constellio://" + encodedURL;
		} else {
			agentURL = null;
		}
		return agentURL;
	}
	
	public static String getAgentDownloadURL() {
		return AGENT_DOWNLOAD_URL;
	}

	public static String getAgentVersion() {
		FoldersLocator foldersLocator = new FoldersLocator();
		File resourcesFolder = foldersLocator.getModuleResourcesFolder("rm");
		File agentFolder = new File(resourcesFolder, "agent");
		File agentVersionFile = new File(agentFolder, "constellio-agent.version");
		String version;
		try {
			version = FileUtils.readFileToString(agentVersionFile);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return version;
	}

	public static void main(String[] args)
			throws Exception {
		URI location = new URI("http://constellio.doculibre.com/#!agentSetup");
		String contextPath = "/constellio";
		String schemeSpecificPart = location.getSchemeSpecificPart().substring(2);
		String schemeSpecificPartBeforeContextPath;
		if (StringUtils.isNotBlank(contextPath)) {
			if (schemeSpecificPart.indexOf(contextPath) != -1) {
				schemeSpecificPartBeforeContextPath = StringUtils.substringBeforeLast(schemeSpecificPart, contextPath);
			} else {
				contextPath = null;
				schemeSpecificPartBeforeContextPath = StringUtils.removeEnd(schemeSpecificPart, "/");
			}
		} else {
			schemeSpecificPartBeforeContextPath = StringUtils.removeEnd(schemeSpecificPart, "/");
		}

		StringBuffer sb = new StringBuffer();
		sb.append(location.getScheme());
		sb.append("://");
		sb.append(schemeSpecificPartBeforeContextPath);
		if (StringUtils.isNotBlank(contextPath)) {
			sb.append(contextPath);
		}
		sb.append("/agentPath");
		System.out.println(sb);
	}

}
