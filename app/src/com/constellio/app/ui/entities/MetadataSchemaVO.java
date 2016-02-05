package com.constellio.app.ui.entities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.ui.application.ConstellioUI;

@SuppressWarnings("serial")
public class MetadataSchemaVO implements Serializable {

	private static final Logger LOGGER = LoggerFactory.getLogger(MetadataSchemaVO.class);

	final String code;
	final String collection;
	final List<MetadataVO> metadatas = new ArrayList<>();
	final Map<Locale, String> labels;
	final List<String> formMetadataCodes;
	final List<String> displayMetadataCodes;
	final List<String> searchMetadataCodes;
	final List<String> tableMetadataCodes;

	public MetadataSchemaVO(String code, String collection, Map<Locale, String> labels) {
		this(code, collection, null, null, null, null, labels);
	}

	public MetadataSchemaVO(String code, String collection, List<String> formMetadataCodes, List<String> displayMetadataCodes,
			List<String> tableMetadataCodes, List<String> searchMetadataCodes, Map<Locale, String> labels) {
		super();
		this.code = code;
		this.collection = collection;
		this.formMetadataCodes = formMetadataCodes;
		this.displayMetadataCodes = displayMetadataCodes;
		this.searchMetadataCodes = searchMetadataCodes;
		this.tableMetadataCodes = tableMetadataCodes;
		this.labels = labels;
	}

	public String getCode() {
		return code;
	}

	public String getTypeCode() {
		return code.split("_")[0];
	}

	public String getCollection() {
		return collection;
	}

	public final List<String> getFormMetadataCodes() {
		return formMetadataCodes;
	}

	public final List<String> getDisplayMetadataCodes() {
		return displayMetadataCodes;
	}

	public final List<String> getTableMetadataCodes() {
		return tableMetadataCodes;
	}

	public final List<String> getSearchMetadataCodes() {
		return searchMetadataCodes;
	}

	public List<MetadataVO> getMetadatas() {
		return metadatas;
	}

	public List<MetadataVO> getFormMetadatas() {
		List<MetadataVO> formMetadatas;
		List<String> formMetadataCodes = getFormMetadataCodes();
		if (formMetadataCodes == null) {
			formMetadatas = getMetadatas();
		} else {
			formMetadatas = new ArrayList<>();
			for (String formMetadataCode : formMetadataCodes) {
				MetadataVO metadataVO = getMetadata(formMetadataCode);
				formMetadatas.add(metadataVO);
			}
		}
		return formMetadatas;
	}

	public List<MetadataVO> getDisplayMetadatas() {
		List<MetadataVO> displayMetadatas;
		List<String> displayMetadataCodes = getDisplayMetadataCodes();
		if (displayMetadataCodes == null) {
			displayMetadatas = getMetadatas();
		} else {
			displayMetadatas = new ArrayList<>();
			for (String displayMetadataCode : displayMetadataCodes) {
				MetadataVO metadataVO = getMetadata(displayMetadataCode);
				displayMetadatas.add(metadataVO);
			}
		}
		return displayMetadatas;
	}

	public List<MetadataVO> getTableMetadatas() {
		List<MetadataVO> tableMetadatas;
		List<String> tableMetadataCodes = getTableMetadataCodes();
		if (tableMetadataCodes == null) {
			tableMetadatas = getMetadatas();
		} else {
			tableMetadatas = new ArrayList<>();
			for (String tableMetadataCode : tableMetadataCodes) {
				MetadataVO metadataVO = getMetadata(tableMetadataCode);
				if (metadataVO == null) {
					LOGGER.warn("No such metadata '" + tableMetadataCode + "'");
				} else {
					tableMetadatas.add(metadataVO);
				}
			}
		}
		return tableMetadatas;
	}

	public List<MetadataVO> getSearchMetadatas() {
		List<MetadataVO> searchMetadatas;
		List<String> searchMetadataCodes = getSearchMetadataCodes();
		if (searchMetadataCodes == null) {
			searchMetadatas = getMetadatas();
		} else {
			searchMetadatas = new ArrayList<>();
			for (String searchMetadataCode : searchMetadataCodes) {
				MetadataVO metadataVO = getMetadata(searchMetadataCode);
				if (metadataVO == null) {
					LOGGER.warn("No such metadata '" + searchMetadataCode + "'");
				} else {
					searchMetadatas.add(metadataVO);
				}
			}
		}
		return searchMetadatas;
	}

	public MetadataVO getMetadata(String metadataCode) {
		MetadataVO match = null;
		String metadataCodeWithoutPrefix = MetadataVO.getCodeWithoutPrefix(metadataCode);
		for (MetadataVO schemaMetadata : metadatas) {
			String schemaMetadataCode = schemaMetadata.getCode();
			String schemaMetadataCodeWithoutPrefix = MetadataVO.getCodeWithoutPrefix(schemaMetadataCode);
			if (schemaMetadataCodeWithoutPrefix.equals(metadataCodeWithoutPrefix)) {
				match = schemaMetadata;
				break;
			}
		}
		return match;
	}

	public Map<Locale, String> getLabels() {
		return labels;
	}

	public String getLabel(Locale locale) {
		return labels.get(locale);
	}

	public String getLabel() {
		return labels.get(ConstellioUI.getCurrentSessionContext().getCurrentLocale());
	}

	public void setLabel(Locale locale, String label) {
		labels.put(locale, label);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((code == null) ? 0 : code.hashCode());
		result = prime * result + ((collection == null) ? 0 : collection.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MetadataSchemaVO other = (MetadataSchemaVO) obj;
		if (code == null) {
			if (other.code != null)
				return false;
		} else if (!code.equals(other.code))
			return false;
		if (collection == null) {
			if (other.collection != null)
				return false;
		} else if (!collection.equals(other.collection))
			return false;
		return true;
	}

	/**
	 * Used by Vaadin to populate the header of the column in a table (since we use MetadataVO objects as property ids).
	 *
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String toString;
		try {
			toString = getLabel(ConstellioUI.getCurrentSessionContext().getCurrentLocale());
		} catch (RuntimeException e) {
			toString = code;
		}
		return toString;
	}

}
