package com.constellio.app.ui.framework.components.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.UserVO;
import com.constellio.app.ui.pages.base.VaadinSessionContext;
import com.constellio.app.ui.params.ParamUtils;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.ContentVersion;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.contents.ContentManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.users.UserServices;
import com.vaadin.server.ExternalResource;
import com.vaadin.server.RequestHandler;
import com.vaadin.server.Resource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinResponse;
import com.vaadin.server.VaadinServletRequest;
import com.vaadin.server.VaadinSession;
import com.vaadin.util.FileTypeResolver;

public class ConstellioResourceHandler implements RequestHandler {

    private static final long serialVersionUID = 1L;
    private static final String PATH = UUID.randomUUID().toString();

    @Override
    public boolean handleRequest(VaadinSession session, VaadinRequest request, VaadinResponse response)
            throws IOException {
    	String sep = "/" + PATH + "/";
    	String requestURI = ((VaadinServletRequest) request).getRequestURI();
        if (requestURI.indexOf(sep) != -1) {
            String params = StringUtils.substringAfter(requestURI, sep);

    		Map<String, String> paramsMap = ParamUtils.getParamsMap(params);
    		String recordId = paramsMap.get("recordId");
    		String metadataCode = paramsMap.get("metadataCode");
    		String version = paramsMap.get("version");
    		String preview = paramsMap.get("preview");
    		String filePath = paramsMap.get("file");
    		
    		String filename;
    		InputStream in = null;
			if (recordId != null) {
	    		ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
	    		ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();
	    		UserServices userServices = modelLayerFactory.newUserServices();
	    		RecordServices recordServices = modelLayerFactory.newRecordServices();
	    		ContentManager contentManager = modelLayerFactory.getContentManager();
	    		MetadataSchemasManager metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
	    		
	    		VaadinSession vaadinSession = VaadinSession.getCurrent();
	    		UserVO userVO = (UserVO) vaadinSession.getSession().getAttribute(VaadinSessionContext.CURRENT_USER_ATTRIBUTE);
	    		String collection = (String) vaadinSession.getSession().getAttribute(VaadinSessionContext.CURRENT_COLLECTION_ATTRIBUTE);
	    		
	    		MetadataSchemaTypes types = metadataSchemasManager.getSchemaTypes(collection);
	    		User user = userServices.getUserInCollection(userVO.getUsername(), collection);
	    		Record record = recordServices.getDocumentById(recordId);
	    		
	    		if (user.hasReadAccess().on(record)) {
	    			String schemaCode = record.getSchemaCode();
	    			Metadata metadata = types.getMetadata(schemaCode + "_" + metadataCode);
	    			Content content = (Content) record.get(metadata);
	    			ContentVersion contentVersion = content.getVersion(version);
	    			
	    			String hash = contentVersion.getHash();
	    			filename = contentVersion.getFilename();
	    			if ("true".equals(preview)) {
	    				if (contentManager.hasContentPreview(hash)) {
			    			in = contentManager.getContentPreviewInputStream(hash, getClass().getSimpleName() + ".handleRequest");
	    				} else {
	    					in = null;
	    				}
	    			} else {
		    			in = contentManager.getContentInputStream(hash, getClass().getSimpleName() + ".handleRequest");
	    			}
	    		} else {
	    			filename = null;
	    			in = null;
	    		}
    		} else if (filePath != null) {
    			File file = new File(filePath);
    			filename = file.getName();
    			in = new FileInputStream(file);
    		} else {
    			filename = null;
    			in = null;
    		}
    		
    		if (in != null) {
    			String mimeType = FileTypeResolver.getMIMEType(filename);
    			response.setContentType(mimeType);
    			
    			OutputStream out = null;
    			try {
        			out = response.getOutputStream();
        			IOUtils.copy(in, out);
    			} finally {
    				IOUtils.closeQuietly(in);
    				IOUtils.closeQuietly(out);
    			}
    		}
            return true;
        }
        return false;
    }

    public static Resource createResource(String recordId, String metadataCode, String version, String filename) {
    	return createResource(recordId, metadataCode, version, filename, false);
    }

    public static Resource createResource(String recordId, String metadataCode, String version, String filename, boolean preview) {
    	Map<String, String> params = new LinkedHashMap<>();
    	params.put("recordId", recordId);
    	params.put("metadataCode", metadataCode);
    	params.put("preview", "" + preview);
    	params.put("version", version);
    	params.put("z-filename", filename);
    	String resourcePath = ParamUtils.addParams(PATH , params);
        return new ExternalResource(resourcePath);
    }
    
    public static Resource createResource(File file) {
    	Map<String, String> params = new LinkedHashMap<>();
    	params.put("file", file.getAbsolutePath());
    	String resourcePath = ParamUtils.addParams(PATH, params);
        return new ExternalResource(resourcePath);
    }

}