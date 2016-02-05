package com.constellio.app.ui.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.constellio.app.ui.entities.RecordVORuntimeException.RecordVORuntimeException_NoSuchMetadata;
import com.constellio.data.utils.LangUtils;
import com.constellio.model.entities.schemas.Schemas;

@SuppressWarnings("serial")
public class RecordVO implements Serializable {

	public enum VIEW_MODE {
		FORM, DISPLAY, TABLE, SEARCH
	}

	String id;

	private String resourceKey;

	private String extension;

	private String niceTitle;

	final List<MetadataValueVO> metadataValues;

	VIEW_MODE viewMode;

	public RecordVO(String id, List<MetadataValueVO> metadataValues, VIEW_MODE viewMode) {
		this.id = id;
		LangUtils.ensureNoNullItems(metadataValues);
		this.metadataValues = metadataValues;
		this.viewMode = viewMode;
	}

	protected boolean getBooleanWithDefaultValue(String param, boolean defaultValue) {
		Boolean value = get(param);
		return value == null ? defaultValue : value;
	}

	public String getId() {
		return id;
	}

	public String getTitle() {
		return get(Schemas.TITLE.getLocalCode());
	}

	public void setTitle(String title) {
		set(Schemas.TITLE.getLocalCode(), title);
	}

	public final VIEW_MODE getViewMode() {
		return viewMode;
	}

	public MetadataSchemaVO getSchema() {
		MetadataSchemaVO schema = null;
		if (!metadataValues.isEmpty()) {
			schema = metadataValues.get(0).getMetadata().getSchema();
		}
		return schema;
	}

	public List<MetadataValueVO> getMetadataValues() {
		return metadataValues;
	}

	public List<MetadataValueVO> getFormMetadataValues() {
		List<MetadataValueVO> formMetadataValues = new ArrayList<>();
		MetadataSchemaVO schemaVO = getSchema();
		List<String> formMetadataCodes = schemaVO.getFormMetadataCodes();
		if (formMetadataCodes == null) {
			formMetadataCodes = getMetadataCodes();
		}
		for (String formMetadataCode : formMetadataCodes) {
			MetadataVO metadataVO = getMetadata(formMetadataCode);
			MetadataValueVO metadataValueVO = getMetadataValue(metadataVO);
			if (metadataValueVO != null) {
				formMetadataValues.add(metadataValueVO);
			}
		}
		return formMetadataValues;
	}

	public List<MetadataValueVO> getDisplayMetadataValues() {
		List<MetadataValueVO> displayMetadataValues = new ArrayList<>();
		MetadataSchemaVO schemaVO = getSchema();
		List<String> displayMetadataCodes = schemaVO.getDisplayMetadataCodes();
		if (displayMetadataCodes == null) {
			displayMetadataCodes = getMetadataCodes();
		}
		for (String displayMetadataCode : displayMetadataCodes) {
			try {
				MetadataVO metadataVO = getMetadata(displayMetadataCode);
				MetadataValueVO metadataValueVO = getMetadataValue(metadataVO);
				if (metadataValueVO != null) {
					displayMetadataValues.add(metadataValueVO);
				}
			} catch (RecordVORuntimeException_NoSuchMetadata e) {

			}
		}
		return displayMetadataValues;
	}

	public List<MetadataValueVO> getTableMetadataValues() {
		List<MetadataValueVO> tableMetadataValues = new ArrayList<>();
		MetadataSchemaVO schemaVO = getSchema();
		List<String> tableMetadataCodes = schemaVO.getTableMetadataCodes();
		if (tableMetadataCodes == null) {
			tableMetadataCodes = getMetadataCodes();
		}
		for (String tableMetadataCode : tableMetadataCodes) {
			MetadataVO metadataVO = getMetadata(tableMetadataCode);
			MetadataValueVO metadataValueVO = getMetadataValue(metadataVO);
			if (metadataValueVO != null) {
				tableMetadataValues.add(metadataValueVO);
			}
		}
		return tableMetadataValues;
	}

	public List<MetadataValueVO> getSearchMetadataValues() {
		List<MetadataValueVO> searchMetadataValues = new ArrayList<>();
		MetadataSchemaVO schemaVO = getSchema();
		List<String> searchMetadataCodes = schemaVO.getSearchMetadataCodes();
		if (searchMetadataCodes == null) {
			searchMetadataCodes = getMetadataCodes();
		}
		for (String tableMetadataCode : searchMetadataCodes) {
			MetadataVO metadataVO = getMetadata(tableMetadataCode);
			MetadataValueVO metadataValueVO = getMetadataValue(metadataVO);
			if (metadataValueVO != null) {
				searchMetadataValues.add(metadataValueVO);
			}
		}
		return searchMetadataValues;
	}

	public MetadataValueVO getMetadataValue(MetadataVO metadata) {
		MetadataValueVO match = null;
		for (MetadataValueVO metadataValue : metadataValues) {
			if (metadata.equals(metadataValue.getMetadata())) {
				match = metadataValue;
				break;
			}
		}
		return match;
	}

	public List<MetadataVO> getMetadatas() {
		List<MetadataVO> metadatas = new ArrayList<MetadataVO>();
		for (MetadataValueVO metadataValue : metadataValues) {
			metadatas.add(metadataValue.getMetadata());
		}
		return metadatas;
	}

	public List<String> getMetadataCodes() {
		List<String> metadataCodes = new ArrayList<>();
		List<MetadataVO> metadatas = getMetadatas();
		for (MetadataVO metadataVO : metadatas) {
			metadataCodes.add(metadataVO.getCode());
		}
		return metadataCodes;
	}

	public List<MetadataVO> getFormMetadatas() {
		return getSchema().getFormMetadatas();
	}

	public List<MetadataVO> getDisplayMetadatas() {
		return getSchema().getDisplayMetadatas();
	}

	public List<MetadataVO> getTableMetadatas() {
		return getSchema().getTableMetadatas();
	}

	public List<MetadataVO> getSearchMetadatas() {
		return getSchema().getSearchMetadatas();
	}

	public MetadataVO getMetadataOrNull(String code) {
		String codeWithoutPrefix = MetadataVO.getCodeWithoutPrefix(code);
		for (MetadataValueVO metadataValue : metadataValues) {
			MetadataVO metadata = metadataValue.getMetadata();
			String metadataCode = metadata.getCode();
			String metadataCodeWithoutPrefix = MetadataVO.getCodeWithoutPrefix(metadataCode);
			if (code.equals(metadataCode) || codeWithoutPrefix.equals(metadataCodeWithoutPrefix)) {
				return metadataValue.getMetadata();
			}
		}
		return null;
	}

	public MetadataVO getMetadata(String code) {
		MetadataVO metadataVO = getMetadataOrNull(code);
		if (metadataVO == null) {
			throw new RecordVORuntimeException_NoSuchMetadata(code);
		} else {
			return metadataVO;
		}
	}

	public <T extends Object> T get(MetadataVO metadata) {
		T value = null;
		if (metadata != null) {
			for (MetadataValueVO metadataValue : metadataValues) {
				if (metadata.equals(metadataValue.getMetadata())) {
					value = metadataValue.getValue();
					break;
				}
			}
		}
		return value;
	}

	public void set(MetadataVO metadata, Object value) {
		for (MetadataValueVO metadataValue : metadataValues) {
			if (metadata.equals(metadataValue.getMetadata())) {
				metadataValue.setValue(value);
				break;
			}
		}
	}

	public <T extends Object> T get(String metadataCode) {
		MetadataVO metadata = getMetadata(metadataCode);
		return get(metadata);
	}

	public void set(String metadataCode, Object value) {
		MetadataVO metadata = getMetadata(metadataCode);
		set(metadata, value);
	}

	public <T extends Object> List<T> getList(MetadataVO metadata) {
		return get(metadata);
	}

	public <T extends Object> List<T> getList(String metadataCode) {
		return get(metadataCode);
	}

	public String getResourceKey() {
		return resourceKey;
	}

	public void setResourceKey(String resourceKey) {
		this.resourceKey = resourceKey;
	}

	public String getExtension() {
		return extension;
	}

	public void setExtension(String extension) {
		this.extension = extension;
	}

	public String getNiceTitle() {
		return niceTitle;
	}

	public void setNiceTitle(String niceTitle) {
		this.niceTitle = niceTitle;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj == null) {
			return false;
		} else if (!(obj instanceof RecordVO)) {
			return false;
		} else {
			RecordVO other = (RecordVO) obj;
			if (id == null) {
				if (other.id != null) {
					return false;
				}
			} else if (!id.equals(other.id)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public String toString() {
		return id;
	}

}
