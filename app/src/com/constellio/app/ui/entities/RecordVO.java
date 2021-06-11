package com.constellio.app.ui.entities;

import com.constellio.app.ui.entities.RecordVORuntimeException.RecordVORuntimeException_NoSuchMetadata;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.data.utils.LangUtils;
import com.constellio.model.entities.records.LocalisedRecordMetadataRetrieval;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.Schemas;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@SuppressWarnings("serial")
public class RecordVO implements Serializable {

	public enum VIEW_MODE {
		FORM, DISPLAY, TABLE, SEARCH
	}

	String id;

	private String resourceKey;

	private String extension;

	private String niceTitle;

	private boolean saved;

	private List<String> extraSearchDisplayCode = new ArrayList<>();

	private List<String> excludedMetadataCodeList;
	private Set<String> excludedFormMetadataCodes;

	final List<MetadataValueVO> metadataValues;
	private List<MetadataValueVO> fakeMetadataToDisplay = new ArrayList();

	VIEW_MODE viewMode;

	private Record record;

	public RecordVO(String id, List<MetadataValueVO> metadataValues, VIEW_MODE viewMode) {
		this(id, metadataValues, viewMode, new ArrayList<String>());

	}

	public RecordVO(String id, List<MetadataValueVO> metadataValues, VIEW_MODE viewMode,
					List<String> excludedMetadataCodeList) {
		this.excludedMetadataCodeList = excludedMetadataCodeList;
		this.id = id;
		LangUtils.ensureNoNullItems(metadataValues);
		this.metadataValues = metadataValues;
		this.viewMode = viewMode;
	}

	public void addExtraSearchDisplayCode(String extraDisplayMetadata) {
		if (getMetadataOrNull(extraDisplayMetadata) != null) {
			extraSearchDisplayCode.add(extraDisplayMetadata);
		} else {
			throw new ImpossibleRuntimeException("metadata code invalid");
		}
	}

	public List<String> getExtraSearchDisplayCode() {
		return Collections.unmodifiableList(extraSearchDisplayCode);
	}

	public Record getRecord() {
		return record;
	}

	public List<String> getExcludedMetadataCodeList() {
		return excludedMetadataCodeList;
	}

	public Set<String> getExcludedFormMetadataCodes() {
		return excludedFormMetadataCodes;
	}

	public void setExcludedMetadataCodeList(List<String> excludedMetadataCodeList) {
		this.excludedMetadataCodeList = excludedMetadataCodeList;
	}

	public void setExcludedFormMetadataCodes(Set<String> excludedFormMetadataCodes) {
		this.excludedFormMetadataCodes = excludedFormMetadataCodes;
	}

	public void setRecord(Record record) {
		this.record = record;
	}

	protected boolean getBooleanWithDefaultValue(String param, boolean defaultValue) {
		Boolean value = get(param);
		return value == null ? defaultValue : value;
	}

	public String getId() {
		return id;
	}

	public String getTitle() {
		return get(Schemas.TITLE);
	}

	public void setTitle(String title) {
		set(Schemas.TITLE, title);
	}

	public boolean isSaved() {
		return saved;
	}

	public void setSaved(boolean saved) {
		this.saved = saved;
	}

	public final VIEW_MODE getViewMode() {
		return viewMode;
	}

	public String getSchemaCode() {
		String schema = null;
		if (record != null) {
			schema = record.getSchemaCode();
		}
		return schema;
	}

	public MetadataSchemaVO getSchema() {
		MetadataSchemaVO schema = null;
		if (!metadataValues.isEmpty()) {
			schema = metadataValues.get(0).getMetadata().getSchema();
		}
		return schema;
	}

	public void addFakeMetadataToDisplay(MetadataValueVO metadataValueVO) {
		fakeMetadataToDisplay.add(metadataValueVO);
		metadataValues.add(metadataValueVO);
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
			if (excludedMetadataCodeList.contains(formMetadataCode)) {
				continue;
			}
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
			if (excludedMetadataCodeList.contains(displayMetadataCode)) {
				continue;
			}

			try {
				MetadataVO metadataVO = getMetadata(displayMetadataCode);
				MetadataValueVO metadataValueVO = getMetadataValue(metadataVO);
				if (metadataValueVO != null) {
					displayMetadataValues.add(metadataValueVO);
				}
			} catch (RecordVORuntimeException_NoSuchMetadata e) {

			}
		}
		displayMetadataValues.addAll(fakeMetadataToDisplay);

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
			if (excludedMetadataCodeList.contains(tableMetadataCode)) {
				continue;
			}

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

		for (String extraSearchDisplayCode : extraSearchDisplayCode) {
			if (searchMetadataCodes.contains(extraSearchDisplayCode)) {
				searchMetadataCodes.remove(extraSearchDisplayCode);
			}

			searchMetadataCodes.add(0, extraSearchDisplayCode);
		}

		for (String tableMetadataCode : searchMetadataCodes) {
			if (excludedMetadataCodeList.contains(tableMetadataCode)) {
				continue;
			}
			MetadataVO metadataVO = getMetadataOrNull(tableMetadataCode);
			if (metadataVO != null) {
				MetadataValueVO metadataValueVO = getMetadataValue(metadataVO);
				if (metadataValueVO != null) {
					searchMetadataValues.add(metadataValueVO);
				}
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
		List<MetadataVO> formMetadatas = getFormShownMetadatas();
		formMetadatas.addAll(getFormHiddenMetadatas());
		return formMetadatas;
	}

	public List<MetadataVO> getFormShownMetadatas() {
		return filterFormMetadatas(getSchema().getFormMetadatas());
	}

	public List<MetadataVO> getFormHiddenMetadatas() {
		return filterFormMetadatas(getSchema().getHiddenFormMetadatas());
	}

	private List<MetadataVO> filterFormMetadatas(List<MetadataVO> formMetadatas) {
		List<MetadataVO> filteredFormMetadatas = new ArrayList<>();
		for (MetadataVO formMetadata : formMetadatas) {
			if (excludedFormMetadataCodes == null || !excludedFormMetadataCodes.contains(formMetadata.code)) {
				filteredFormMetadatas.add(formMetadata);
			}
		}
		return filteredFormMetadatas;
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
		String searchedLocalCode = MetadataVO.getCodeWithoutPrefix(code);
		for (MetadataValueVO metadataValue : metadataValues) {
			MetadataVO metadata = metadataValue.getMetadata();
			String metadataCode = metadata.getCode();
			String localCode = metadata.getLocalCode();
			if (localCode == null) {
				localCode = MetadataVO.getCodeWithoutPrefix(metadataCode);
			}
			if (searchedLocalCode.equals(localCode)) {
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

	public <T extends Object> T get(String metadataCode) {
		MetadataVO metadata = getMetadata(metadataCode);
		return get(metadata);
	}

	public <T extends Object> T get(Metadata metadata) {
		return get(metadata.getLocalCode());
	}

	public <T extends Object> T get(Metadata metadata, Locale locale, LocalisedRecordMetadataRetrieval mode) {
		return get(metadata);
	}

	public <T extends Object> T get(MetadataVO metadata, Locale locale, LocalisedRecordMetadataRetrieval mode) {
		T value = null;
		return get(metadata);
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

	public void set(Metadata metadata, Object value) {
		set(metadata.getLocalCode(), value);
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
