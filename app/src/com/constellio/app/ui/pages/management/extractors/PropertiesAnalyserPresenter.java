package com.constellio.app.ui.pages.management.extractors;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.MetadataExtractorVO;
import com.constellio.app.ui.entities.MetadataSchemaTypeVO;
import com.constellio.app.ui.entities.MetadataSchemaVO;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.MetadataSchemaToVOBuilder;
import com.constellio.app.ui.framework.builders.MetadataSchemaTypeToVOBuilder;
import com.constellio.app.ui.framework.builders.MetadataToVOBuilder;
import com.constellio.app.ui.framework.components.fields.upload.TempFileUpload;
import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.app.ui.pages.management.extractors.entities.RegexConfigVO;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.ParsedContent;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.*;
import com.constellio.model.services.parser.FileParser;
import com.constellio.model.services.schemas.MetadataListFilter;
import com.constellio.model.services.schemas.MetadataSchemaTypesAlteration;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.ui.Label;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.regex.Pattern;

import static com.constellio.model.entities.schemas.MetadataValueType.*;

public class PropertiesAnalyserPresenter extends BasePresenter<PropertiesAnalyserViewImpl> {
	
	Map<String, Object> properties = new HashMap<>(); 
	Map<String, List<String>> styles = new HashMap<>(); 

	public PropertiesAnalyserPresenter(PropertiesAnalyserViewImpl view) {
		super(view);
	}

	@Override
	protected boolean hasPageAccess(String params, User user) {
		return user.has(CorePermissions.MANAGE_METADATAEXTRACTOR).globally();
	}

	public void backButtonClicked() {
		view.navigate().to().listMetadataExtractors();
	}

	public void calculatePropertiesAndStyles(File file) {
		if(file == null) {
			properties = new HashMap<>();
			styles = new HashMap<>();
		} else {
			FileParser fileParser = modelLayerFactory.newFileParser();
			InputStream in = null;

			try {
				in = new BufferedInputStream(new FileInputStream(file));
				ParsedContent parsedContent = fileParser.parse(in, false);

				properties = parsedContent.getProperties();
				styles = parsedContent.getStyles();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				IOUtils.closeQuietly(in);
			}
		}	
	}

	public Map<String, Object> getPropertiesContainer() {
		return properties;
	}

	public Map<String, Object> getStylesContainer() {
		return (Map) styles;
	}
}
