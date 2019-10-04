package com.constellio.app.ui.entities;

import com.constellio.app.ui.application.ConstellioUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@SuppressWarnings("serial")
public class MetadataSchemaVO implements Serializable {

	private static final Logger LOGGER = LoggerFactory.getLogger(MetadataSchemaVO.class);

	final String code;
	final String collection;
	final List<MetadataVO> metadatas = new ArrayList<>();
	final Map<Locale, String> labels;
	final List<String> formMetadataCodes;
	final List<String> hiddenFormMetadataCodes;
	final List<String> displayMetadataCodes;
	final List<String> searchMetadataCodes;
	final List<String> tableMetadataCodes;
	final String localCode;
	final CollectionInfoVO collectionInfoVO;

	public MetadataSchemaVO(String code, String collection, Map<Locale, String> labels,
							CollectionInfoVO collectionInfoVO) {
		this(code, collection, null, null, null, null, null, null, labels, collectionInfoVO);
	}

	public MetadataSchemaVO(String code, String collection, String localCode, Map<Locale, String> labels,
							CollectionInfoVO collectionInfoVO) {
		this(code, collection, localCode, null, null, null, null, null, labels, collectionInfoVO);
	}

	public MetadataSchemaVO(String code, String collection, String localCode, List<String> formMetadataCodes,
							List<String> hiddenFormMetadataCodes, List<String> displayMetadataCodes,
							List<String> tableMetadataCodes, List<String> searchMetadataCodes,
							Map<Locale, String> labels, CollectionInfoVO collectionInfoVO) {
		super();
		this.code = code;
		this.collection = collection;
		this.formMetadataCodes = formMetadataCodes;
		this.hiddenFormMetadataCodes = hiddenFormMetadataCodes;
		this.displayMetadataCodes = displayMetadataCodes;
		this.searchMetadataCodes = searchMetadataCodes;
		this.tableMetadataCodes = tableMetadataCodes;
		this.labels = labels;
		this.localCode = localCode;
		this.collectionInfoVO = collectionInfoVO;
	}

	public String getLocalCode() {
		return localCode;
	}

	public String getCode() {
		return code;
	}

	public String getTypeCode() {
		return code != null ? code.split("_")[0] : null;
	}

	public String getCollection() {
		return collection;
	}

	public final List<String> getFormMetadataCodes() {
		return formMetadataCodes;
	}

	public final List<String> getHiddenFormMetadataCodes() {
		return hiddenFormMetadataCodes;
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

	public CollectionInfoVO getCollectionInfoVO() {
		return collectionInfoVO;
	}

	public List<MetadataVO> getFormMetadatas() {
		List<MetadataVO> formMetadatas;
		List<String> formMetadataCodes = getFormMetadataCodes();
		if (formMetadataCodes == null) {
			formMetadatas = getMetadatas();
		} else {
			formMetadatas = new ArrayList<>();
			for (String formMetadataCode : formMetadataCodes) {
				List<MetadataVO> metadataVOs = getMetadatas(formMetadataCode);
				formMetadatas.addAll(metadataVOs);
			}
		}
		return formMetadatas;
	}

	public List<MetadataVO> getHiddenFormMetadatas() {
		List<MetadataVO> hiddenMetadatas;
		List<String> hiddenMetadataCodes = getHiddenFormMetadataCodes();
		if (hiddenMetadataCodes == null) {
			hiddenMetadatas = getMetadatas();
		} else {
			hiddenMetadatas = new ArrayList<>();
			for (String hiddenMetadataCode : hiddenMetadataCodes) {
				List<MetadataVO> metadataVOs = getMetadatas(hiddenMetadataCode);
				hiddenMetadatas.addAll(metadataVOs);
			}
		}
		return hiddenMetadatas;
	}

	public List<MetadataVO> getDisplayMetadatas() {
		List<MetadataVO> displayMetadatas;
		List<String> displayMetadataCodes = getDisplayMetadataCodes();
		if (displayMetadataCodes == null) {
			displayMetadatas = getMetadatas();
		} else {
			displayMetadatas = new ArrayList<>();
			for (String displayMetadataCode : displayMetadataCodes) {
				List<MetadataVO> metadataVOs = getMetadatas(displayMetadataCode);
				displayMetadatas.addAll(metadataVOs);
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
				List<MetadataVO> metadataVOs = getMetadatas(tableMetadataCode);
				tableMetadatas.addAll(metadataVOs);
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
				List<MetadataVO> metadataVOs = getMetadatas(searchMetadataCode);
				searchMetadatas.addAll(metadataVOs);
			}
		}
		return searchMetadatas;
	}

	public List<MetadataVO> getMetadatas(String metadataCode) {
		List<MetadataVO> matches = new ArrayList<>();
		String metadataCodeWithoutPrefix = MetadataVO.getCodeWithoutPrefix(metadataCode);
		for (MetadataVO schemaMetadata : metadatas) {
			String schemaMetadataCode = schemaMetadata.getCode();
			String schemaMetadataCodeWithoutPrefix = MetadataVO.getCodeWithoutPrefix(schemaMetadataCode);
			if (schemaMetadataCodeWithoutPrefix.equals(metadataCodeWithoutPrefix)) {
				matches.add(schemaMetadata);
			}
		}
		return matches;
	}

	public MetadataVO getMetadata(String metadataCode) {
		List<MetadataVO> matches = getMetadatas(metadataCode);
		return !matches.isEmpty() ? matches.get(0) : null;
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
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		MetadataSchemaVO other = (MetadataSchemaVO) obj;
		if (code == null) {
			if (other.code != null) {
				return false;
			}
		} else if (!code.equals(other.code)) {
			return false;
		}
		if (collection == null) {
			if (other.collection != null) {
				return false;
			}
		} else if (!collection.equals(other.collection)) {
			return false;
		}
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
