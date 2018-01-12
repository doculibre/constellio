package com.constellio.app.modules.rm.services.sip.model;

import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.constellio.app.modules.rm.services.sip.model.SIPMetadataObject.CATEGORY_TYPE;

public class SIPCategory implements SIPObject {
	
	private String id;
	
	private String code;
	
	private String title;
	
	private String description;
	
	private SIPCategory parentCategory;

	private EntityRetriever entityRetriever;

	private Category category;

	public SIPCategory(Category category, EntityRetriever entityRetriever) {
		this.category = category;
		this.id = "" + category.getId();
		this.code = category.getCode();
		this.title = category.getTitle();
		this.description = category.getDescription();
		this.entityRetriever = entityRetriever;
		Category constellioCategory = entityRetriever.getCategoryById(category.getParent());
		if (constellioCategory != null) {
			parentCategory = new SIPCategory(constellioCategory, entityRetriever);
		}
	}

	public String getId() {
		return id;
	}

	public String getType() {
		return CATEGORY_TYPE;
	}

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

	public List<String> getMetadataValues(String metadataId) {
		String metadataValue = getMetadataValue(metadataId);
		return metadataValue != null ? Arrays.asList(metadataValue) : new ArrayList<String>();
	}

	public String getCode() {
		return code;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public SIPCategory getParentCategory() {
		return parentCategory;
	}

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

	@Override
	public List<Metadata> getMetadataList() {
		return this.category.getSchema().getMetadatas();
	}

	@Override
	public Record getRecord() {
		return this.category.getWrappedRecord();
	}

}
