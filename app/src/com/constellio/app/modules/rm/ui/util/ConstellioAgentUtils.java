package com.constellio.app.modules.rm.ui.util;

import com.constellio.app.modules.rm.ConstellioRMModule;
import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.extensions.api.AgentExtension.AgentUrlExtension;
import com.constellio.app.modules.rm.extensions.api.AgentExtension.AgentUrlExtension.GetAgentUrlParams;
import com.constellio.app.modules.rm.extensions.api.RMModuleExtensions;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.UserDocumentVO;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.data.conf.FoldersLocator;
import com.constellio.data.utils.UnicodeUtils;
import com.constellio.data.utils.dev.Toggle;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.records.wrappers.UserDocument;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.security.global.AgentStatus;
import com.constellio.model.services.configs.SystemConfigurationsManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.users.SystemWideUserInfos;
import com.constellio.model.services.users.UserServices;
import com.vaadin.server.VaadinServletService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;

import static com.constellio.app.utils.HttpRequestUtils.isLocalhost;
import static com.constellio.app.utils.HttpRequestUtils.isMacOsX;
import static com.constellio.app.utils.HttpRequestUtils.isWindows;

public class ConstellioAgentUtils {

	public static final String URL_SEP = "agentURLSep";

	public static final String AGENT_DOWNLOAD_URL = "http://constellio.com/agent/";
	
	public static boolean isAgentSupported() {
		return isAgentSupported(null);
	}

	public static boolean isAgentSupported(HttpServletRequest request) {
		if (request == null) {
			request = VaadinServletService.getCurrentServletRequest();
		}
		return request == null || (isWindows(request) || isMacOsX(request) || isLocalhost(request));
	}

	public static String getAgentBaseURL() {
		return getAgentBaseURL(null);
	}

	public static String getAgentBaseURL(HttpServletRequest request) {
		return getAgentBaseURL(request, null);
	}

	public static String getAgentBaseURL(HttpServletRequest request, AppLayerFactory appLayerFactory) {
		String agentBaseURL;
		// FIXME Should not obtain ConstellioFactories through singleton
		if (appLayerFactory == null) {
			appLayerFactory = ConstellioFactories.getInstance().getAppLayerFactory();
		}
		ModelLayerFactory modelLayerFactory = appLayerFactory.getModelLayerFactory();
		SystemConfigurationsManager systemConfigurationsManager = modelLayerFactory.getSystemConfigurationsManager();
		RMConfigs rmConfigs = new RMConfigs(systemConfigurationsManager);
		if (rmConfigs.isAgentEnabled()) {
			ConstellioEIMConfigs eimConfigs = new ConstellioEIMConfigs(systemConfigurationsManager);
			String baseURL = eimConfigs.getConstellioUrl();
			if (!baseURL.endsWith("/")) {
				baseURL += "/";
			}
			
			StringBuffer sb = new StringBuffer();
			sb.append(baseURL);
			sb.append("agentPath");
			agentBaseURL = sb.toString();
		} else {
			agentBaseURL = null;
		}
		return agentBaseURL;
	}

	public static String getAgentInitURL() {
		return getAgentInitURL(null);
	}

	public static String getAgentInitURL(HttpServletRequest request) {
		String agentBaseURL = getAgentBaseURL(request);
		return addConstellioProtocol(agentBaseURL, request);
	}

	public static String getAgentURL(RecordVO recordVO, ContentVersionVO contentVersionVO) {
		SessionContext sessionContext = ConstellioUI.getCurrentSessionContext();
		return getAgentURL(recordVO, contentVersionVO, null, sessionContext);
	}

	public static String getAgentURL(RecordVO recordVO, ContentVersionVO contentVersionVO,
									 SessionContext sessionContext) {
		return getAgentURL(recordVO, contentVersionVO, null, sessionContext);
	}

	public static String getAgentURL(RecordVO recordVO, ContentVersionVO contentVersionVO, HttpServletRequest request,
									 SessionContext sessionContext) {
		return getAgentURL(recordVO, contentVersionVO, request, sessionContext, null);
	}

	public static String getAgentURL(RecordVO recordVO, ContentVersionVO contentVersionVO, HttpServletRequest request,
									 SessionContext sessionContext, AppLayerFactory appLayerFactory) {
		String agentURL;
		request = ensureRequest(request);
		if (Toggle.AGENT_PLUGIN_INSTALLED.isEnabled() && recordVO != null && contentVersionVO != null && isAgentSupported(request)) {
			// FIXME Should not obtain ConstellioFactories through singleton
			if (appLayerFactory == null) {
				appLayerFactory = ConstellioFactories.getInstance().getAppLayerFactory();
			}
			ModelLayerFactory modelLayerFactory = appLayerFactory.getModelLayerFactory();
			SystemConfigurationsManager systemConfigurationsManager = modelLayerFactory.getSystemConfigurationsManager();
			RMConfigs rmConfigs = new RMConfigs(systemConfigurationsManager);
			UserVO userVO = sessionContext.getCurrentUser();
			UserServices userServices = modelLayerFactory.newUserServices();
			SystemWideUserInfos userCredentials = userServices.getUserInfos(userVO.getUsername());
			AgentStatus agentStatus = userCredentials.getAgentStatus();
			if (agentStatus == AgentStatus.DISABLED && !rmConfigs.isAgentDisabledUntilFirstConnection()) {
				agentStatus = AgentStatus.ENABLED;
			}
			if (rmConfigs.isAgentEnabled() && agentStatus == AgentStatus.ENABLED && (!(recordVO instanceof UserDocumentVO) || rmConfigs.isAgentEditUserDocuments())) {
				RMModuleExtensions rmModuleExtensions = appLayerFactory.getExtensions()
						.forCollection(recordVO.getRecord().getCollection()).forModule(ConstellioRMModule.ID);
				if (rmModuleExtensions.getAgentExtensions().getAgentUrlExtension().getValue() != null) {
					User user = userServices.getUserInCollection(userVO.getUsername(), recordVO.getRecord().getCollection());
					String url = rmModuleExtensions.getAgentExtensions().getAgentUrlExtension().getValue()
							.getAgentUrl(new GetAgentUrlParams(user, recordVO.getId(), request));
					if (AgentUrlExtension.FORCE_NULL_VALUE.equals(url)) {
						return null;
					} else if (url != null) {
						return url;
					}
				}
				String resourcePath = getResourcePath(recordVO, contentVersionVO, sessionContext);
				if (resourcePath != null) {
					String agentBaseURL = getAgentBaseURL(request, appLayerFactory);
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
		return addConstellioProtocol(agentURL, request);
	}

	public static String appendAgentURL(String agentURL, String appendedAgentURL) {
		StringBuilder sb = new StringBuilder();
		if (StringUtils.isNotBlank(agentURL)) {
			sb.append(agentURL);
			sb.append(URL_SEP);
			if (appendedAgentURL.startsWith("constellio://")) {
				appendedAgentURL = StringUtils.removeStart(appendedAgentURL, "constellio://");
			}
			try {
				appendedAgentURL = URLDecoder.decode(appendedAgentURL, "UTF-8");
				appendedAgentURL = StringUtils.substringAfter(appendedAgentURL, "/agentPath");
				appendedAgentURL = URLEncoder.encode(appendedAgentURL, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
			}
		}
		sb.append(appendedAgentURL);
		return sb.toString();
	}

	public static String getAgentSmbURL(RecordVO recordVO, MetadataVO metadataVO) {
		return getAgentSmbURL(recordVO, metadataVO, null);
	}
	
	private static HttpServletRequest ensureRequest(HttpServletRequest request) {
		if (request == null) {
			request = VaadinServletService.getCurrentServletRequest();
		}
//		if (request == null) {
//			request = ConstellioVaadinServlet.getCurrentHttpServletRequest();
//		}
		return request;
	}

	public static String getAgentSmbURL(RecordVO recordVO, MetadataVO metadataVO, HttpServletRequest request) {
		String agentSmbURL;

		request = ensureRequest(request);
		//		String passthroughPath;
		//		if (isWindows(request)) {
		//			passthroughPath = StringUtils.replace(smbPath, "/", "\\");
		//			passthroughPath = StringUtils.removeStart(passthroughPath, "smb:");
		//		} else {
		//			passthroughPath = smbPath;
		//		}
		//		passthroughPath = UnicodeUtils.unicodeEscape(passthroughPath);
		String passthroughPath = recordVO.getId() + "/" + metadataVO.getCode();

		String agentBaseURL = getAgentBaseURL();
		StringBuffer sb = new StringBuffer();
		sb.append(agentBaseURL);
		sb.append("/passthrough/");
		sb.append(passthroughPath);
		agentSmbURL = sb.toString();

		return addConstellioProtocol(agentSmbURL, request);
	}

	public static String getAgentURLForWebAuthenticationCompleted(HttpServletRequest request, String username,
																  String token) {
		return addConstellioProtocol(
				new StringBuilder(getAgentBaseURL(request))
						.append("/").append(username)
						.append("/").append(token)
						.append("/").append("agentLogin").toString());
	}


	private static String getResourcePath(RecordVO recordVO, ContentVersionVO contentVersionVO,
										  SessionContext sessionContext) {
		String resourcePath;

		Record record = recordVO.getRecord();
		String schemaCode = record.getSchemaCode();
		String schemaTypeCode = SchemaUtils.getSchemaTypeCode(schemaCode);

		String collectionName = record.getCollection();
		UserVO currentUserVO = sessionContext.getCurrentUser();
		String currentUsername = currentUserVO.getUsername();
		String currentUserId = currentUserVO.getId();
		String filename = contentVersionVO.getFileName();
		filename = UnicodeUtils.unicodeEscape(filename);

		MetadataSchemaTypes types = types(sessionContext);
		if (UserDocument.SCHEMA_TYPE.equals(schemaTypeCode)) {
			UserDocument userDocument = new UserDocument(record, types);
			if (currentUserId.equals(userDocument.getUser()) && userDocument.getUserFolder() == null) {
				StringBuffer sb = new StringBuffer();
				sb.append("/");
				sb.append(currentUsername);
				sb.append("/");
				sb.append(collectionName);
				sb.append("/userDocuments");
				sb.append("/");
				sb.append(userDocument.getId());
				//				sb.append("/");
				//				sb.append(filename);
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
				//				sb.append("/");
				//				sb.append(filename);
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
				//				sb.append("/");
				//				sb.append(filename);
				resourcePath = sb.toString();
			}
		} else {
			resourcePath = null;
		}

		return resourcePath;
	}


	private static final MetadataSchemaTypes types(SessionContext sessionContext) {
		String collectionName = sessionContext.getCurrentCollection();
		ModelLayerFactory modelLayerFactory = ConstellioFactories.getInstance().getModelLayerFactory();
		MetadataSchemasManager metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
		return metadataSchemasManager.getSchemaTypes(collectionName);
	}

	public static String addConstellioProtocol(String url) {
		return addConstellioProtocol(url, null);
	}

	public static String addConstellioProtocol(String url, HttpServletRequest request) {
		String agentURL;
		if (url != null) {
			if (request == null) {
				request = VaadinServletService.getCurrentServletRequest();
			}
			String encoding = "UTF-8";
			String encodedURL;
			try {
				encodedURL = URLEncoder.encode(url, encoding);
			} catch (UnsupportedEncodingException e) {
				throw new RuntimeException(e);
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
			version = FileUtils.readFileToString(agentVersionFile, "UTF-8");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return version;
	}

	public static boolean isAdvancedFeaturesEnabled() {
		return true;
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
