package com.constellio.app.modules.rm.model.SIPArchivedGenerator.constellio.model.base;

import com.constellio.app.modules.rm.model.SIPArchivedGenerator.constellio.sip.model.SIPCategory;

import java.util.List;
import java.util.Map;

public abstract class BaseSIPCategory extends BaseSIPObject implements SIPCategory {
	
	private String code;
	
	private String description;
	
	private SIPCategory parentCategory;

	public BaseSIPCategory(String id, List<String> metadataIds,
			Map<String, String> metadataLabels,
			Map<String, List<String>> metadataValues, String code, String title, String description, SIPCategory parentCategory) {
		super(id, CATEGORY_TYPE, title, metadataIds, metadataLabels, metadataValues);
		this.code = code;
		this.description = description;
		this.parentCategory = parentCategory;
	}

	@Override
	public String getCode() {
		return code;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public SIPCategory getParentCategory() {
		return parentCategory;
	}

}
