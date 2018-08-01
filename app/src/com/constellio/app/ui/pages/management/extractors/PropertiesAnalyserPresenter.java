package com.constellio.app.ui.pages.management.extractors;

import com.constellio.app.ui.pages.base.BasePresenter;
import com.constellio.model.entities.CorePermissions;
import com.constellio.model.entities.records.ParsedContent;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.services.parser.FileParser;
import org.apache.tika.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
		if (file == null) {
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
