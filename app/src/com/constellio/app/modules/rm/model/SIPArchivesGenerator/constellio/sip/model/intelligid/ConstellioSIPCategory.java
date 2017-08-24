package com.constellio.app.modules.rm.model.SIPArchivesGenerator.constellio.sip.model.intelligid;

import com.constellio.app.modules.rm.model.SIPArchivesGenerator.constellio.sip.model.SIPCategory;
import com.constellio.app.modules.rm.wrappers.Category;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConstellioSIPCategory implements SIPCategory {
	
	private String id;
	
	private String code;
	
	private String title;
	
	private String description;
	
	private SIPCategory parentCategory;

	private EntityRetriever entityRetriever;

	public ConstellioSIPCategory(Category processusActivite, EntityRetriever entityRetriever) {
		this.id = "" + processusActivite.getId();
		this.code = processusActivite.getCode();
		this.title = processusActivite.getTitle();
		this.description = processusActivite.getDescription();
		this.entityRetriever = entityRetriever;
		Category processusActiviteParent = entityRetriever.getCategoryById(processusActivite.getParent());
		if (processusActiviteParent != null) {
			parentCategory = new ConstellioSIPCategory(processusActiviteParent, entityRetriever);
		}
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getType() {
		return CATEGORY_TYPE;
	}

	@Override
	public List<String> getMetadataIds() {
		return Arrays.asList("id", "code", "title");
	}

	@Override
	public String getMetadataLabel(String metadataId) {
		String metadataLabel;
		if ("id".equals(metadataId)) {
			metadataLabel = "Identifiant";
		} else if ("code".equals(metadataId)) {
			metadataLabel = "Code";
		} else if ("title".equals(metadataId)) {
			metadataLabel = "Titre";
		} else {
			metadataLabel = null;
		}
		return metadataLabel;
	}

	@Override
	public String getMetadataValue(String metadataId) {
		String metadataValue;
		if ("id".equals(metadataId)) {
			metadataValue = id;
		} else if ("code".equals(metadataId)) {
			metadataValue = code;
		} else if ("title".equals(metadataId)) {
			metadataValue = title;
		} else {
			metadataValue = null;
		}
		return metadataValue;
	}

	@Override
	public List<String> getMetadataValues(String metadataId) {
		String metadataValue = getMetadataValue(metadataId);
		return metadataValue != null ? Arrays.asList(metadataValue) : new ArrayList<String>();
	}

	@Override
	public String getCode() {
		return code;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public SIPCategory getParentCategory() {
		return parentCategory;
	}

	@Override
	public String getZipPath() {
		StringBuffer sb = new StringBuffer();
		SIPCategory currentCategory = this;
		while (currentCategory != null) {
			String currentCategoryCode = currentCategory.getCode();
			if (sb.length() > 0) {
				sb.insert(0, "/");
			}
			sb.insert(0, currentCategoryCode);
			currentCategory = currentCategory.getParentCategory();
		}
		sb.insert(0, "/data/");
		return sb.toString();
	}

}
