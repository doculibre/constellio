package com.constellio.model.services.records.populators;

import com.constellio.data.utils.KeyListMap;
import com.constellio.data.conf.FoldersLocator;
import com.constellio.data.conf.FoldersLocatorMode;
import com.constellio.model.entities.CollectionInfo;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.ContentVersion;
import com.constellio.model.entities.records.ParsedContent;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.extensions.events.schemas.SearchFieldPopulatorParams;
import com.constellio.model.services.contents.ContentManagerRuntimeException.ContentManagerRuntimeException_NoSuchContent;
import com.constellio.model.services.contents.ParsedContentProvider;
import com.constellio.model.services.extensions.ModelLayerExtensions;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.records.FieldsPopulator;
import com.constellio.model.services.records.RecordUtils;
import org.apache.commons.io.FilenameUtils;
import org.joda.time.LocalDate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static com.constellio.data.dao.services.bigVault.BigVaultRecordDao.DATE_SEARCH_FIELD;

public class SearchFieldsPopulator extends SeparatedFieldsPopulator implements FieldsPopulator {

	private static final Logger LOGGER = LoggerFactory.getLogger(SearchFieldsPopulator.class);

	//LanguageDetectionManager languageDectionServices;

	ParsedContentProvider parsedContentProvider;

	CollectionInfo collectionInfo;

	ConstellioEIMConfigs systemConf;

	ModelLayerExtensions extensions;

	Set<String> fileExtensionsExcludedFromParsing;

	public SearchFieldsPopulator(MetadataSchemaTypes types, boolean fullRewrite,
								 ParsedContentProvider parsedContentProvider, CollectionInfo collectionInfo,
								 ConstellioEIMConfigs systemConf,
								 ModelLayerExtensions extensions) {
		super(types, fullRewrite);
		//	this.languageDectionServices = languageDectionServices;
		this.parsedContentProvider = parsedContentProvider;
		this.collectionInfo = collectionInfo;
		this.systemConf = systemConf;
		this.extensions = extensions;
		this.fileExtensionsExcludedFromParsing = systemConf.getFileExtensionsExcludedFromParsing();
	}

	@Override
	public Map<String, Object> populateCopyfields(MetadataSchema schema, Record record) {
		Map<String, Object> fields = super.populateCopyfields(schema, record);

		Metadata idMetadata = schema.getMetadata("id");
		if (idMetadata.isSearchable()) {
			String id = record.getId();
			String idWithoutZeros = RecordUtils.removeZerosInId(id);

			List<String> searchableValues = new ArrayList<>();
			searchableValues.add(id);
			if (!idWithoutZeros.equals(id)) {
				searchableValues.add(idWithoutZeros);
			}

			for (String collectionLanguage : collectionInfo.getCollectionLanguesCodes()) {
				fields.put("id_txt_" + collectionLanguage, searchableValues);
			}
		}

		return fields;
	}

	private String getSearchFieldFor(Metadata metadata) {
		String copiedMetadataCodePrefix;
		String dataStoreCode = metadata.getDataStoreCode();

		if (dataStoreCode.endsWith("_txt")) {
			copiedMetadataCodePrefix = dataStoreCode.replace("_txt", "_txt_");
		} else {
			copiedMetadataCodePrefix = dataStoreCode.replace("_t", "_t_").replace("_ss", "_txt_").replace("_s", "_t_");
		}

		return copiedMetadataCodePrefix;
	}

	@Override
	public Map<String, Object> populateCopyfields(Metadata metadata, Object value, Locale locale) {

		if (metadata.isSearchable() && !"id".equals(metadata.getLocalCode())) {

			if (metadata.isMultivalue() && metadata.getType().isStringOrText()) {
				List<String> values = asNotNullList(value);
				return populateCopyFieldsOfMultivalueSearchableTextMetadata(values, getSearchFieldFor(metadata), locale);

			} else if (metadata.isMultivalue() && metadata.getType().equals(MetadataValueType.CONTENT)) {
				List<Content> values = asNotNullList(value);
				return populateCopyFieldsOfMultivalueSearchableContentMetadata(values, metadata.getLocalCode());

			} else if (metadata.isMultivalue() && metadata.getType().equals(MetadataValueType.DATE)) {
				List<String> values = asNotNullList(value);
				return populateCopyFieldsOfMultivalueSearchableDateMetadata(values, metadata.getLocalCode());

			} else if (metadata.isMultivalue() && metadata.getType().isIntegerOrFloatingPoint()) {
				List<String> values = asNotNullList(value);
				return populateCopyFieldsOfMultivalueSearchableNumberMetadata(values, metadata.getLocalCode());

			} else if (!metadata.isMultivalue() && metadata.getType().isStringOrText()) {
				return populateCopyFieldsOfSinglevalueSearchableTextMetadata((String) value, metadata, locale);

			} else if (!metadata.isMultivalue() && metadata.getType().equals(MetadataValueType.DATE)) {
				return populateCopyFieldsOfSinglevalueSearchableDateMetadata(value, getSearchFieldFor(metadata));

			} else if (!metadata.isMultivalue() && metadata.getType().isIntegerOrFloatingPoint()) {
				return populateCopyFieldsOfSinglevalueSearchableNumberMetadata("" + value, getSearchFieldFor(metadata));

			} else if (!metadata.isMultivalue() && metadata.getType().equals(MetadataValueType.CONTENT)) {
				return populateCopyFieldsOfSinglevalueSearchableContentMetadata((Content) value, metadata.getLocalCode());

			} else {
				return Collections.emptyMap();
			}

		} else {
			return Collections.emptyMap();
		}

	}

	private void addFilenameAndParsedContent(ContentVersion currentVersion, KeyListMap<String, Object> keyListMap,
											 String code) {
		try {
			ParsedContent parsedContent = parsedContentProvider.getParsedContentIfAlreadyParsed(currentVersion.getHash());

			String contentLanguage = null;
			if (collectionInfo.isMonoLingual()) {
				contentLanguage = collectionInfo.getMainSystemLanguage().getCode();

			} else if (parsedContent != null && collectionInfo.getCollectionLanguesCodes().contains(parsedContent.getLanguage())) {
				contentLanguage = parsedContent.getLanguage();

			} else {
				contentLanguage = collectionInfo.getMainSystemLanguage().getCode();
			}

			if (parsedContent == null || contentLanguage == null) {
				for (String collectionLanguage : collectionInfo.getCollectionLanguesCodes()) {
					keyListMap.add(code + "_" + collectionLanguage + "_ss", currentVersion.getFilename());
				}
			} else {
				keyListMap.add(code + "_" + contentLanguage + "_ss", currentVersion.getFilename());
			}

			String currentExtension = FilenameUtils.getExtension(currentVersion.getFilename());
			currentExtension = currentExtension == null ? null : currentExtension.toLowerCase();
			if (parsedContent != null && contentLanguage != null &&
				!fileExtensionsExcludedFromParsing.contains(currentExtension)) {
				keyListMap.add(code + "_txt_" + contentLanguage, parsedContent.getParsedContent());
			}
		} catch (ContentManagerRuntimeException_NoSuchContent e) {
			if (new FoldersLocator().getFoldersLocatorMode() != FoldersLocatorMode.PROJECT) {
				LOGGER.warn("Parsed content of '" + currentVersion.getHash() + "' was not found in vault");
			}
		}
	}

	private Map<String, Object> populateCopyFieldsOfSinglevalueSearchableContentMetadata(Content value,
																						 String copiedMetadataCode) {

		KeyListMap<String, Object> keyListMap = new KeyListMap<>();
		if (value != null) {
			ContentVersion currentVersion = value.getCurrentVersion();
			addFilenameAndParsedContent(currentVersion, keyListMap, copiedMetadataCode);

		}
		addEmptyValuesToOtherFields(copiedMetadataCode, keyListMap);
		return (Map) keyListMap.getNestedMap();
	}

	private void addEmptyValuesToOtherFields(String copiedMetadataCode, KeyListMap<String, Object> keyListMap) {
		for (String collectionLanguage : collectionInfo.getCollectionLanguesCodes()) {
			String fieldCode = copiedMetadataCode + "_txt_" + collectionLanguage;
			if (!keyListMap.getNestedMap().containsKey(fieldCode)) {
				keyListMap.add(fieldCode, "");
			}

			fieldCode = copiedMetadataCode + "_" + collectionLanguage + "_ss";
			if (!keyListMap.getNestedMap().containsKey(fieldCode)) {
				keyListMap.add(fieldCode, "");
			}
		}
	}

	private Map<String, Object> populateCopyFieldsOfSinglevalueSearchableTextMetadata(String value, Metadata metadata,
																					  Locale locale) {

		SearchFieldPopulatorParams extensionParam = new SearchFieldPopulatorParams(metadata, value, locale);
		Object finalValue = extensions.forCollection(metadata.getCollection()).populateSearchField(extensionParam);

		Map<String, Object> copyfields = new HashMap<>();
		String fieldCode = getSearchFieldFor(metadata) + locale.getLanguage();
		copyfields.put(fieldCode, finalValue);

		return copyfields;
	}

	private Map<String, Object> populateCopyFieldsOfSinglevalueSearchableNumberMetadata(String value,
																						String copiedMetadataCodePrefix) {

		String prefix = copiedMetadataCodePrefix;
		String valueLanguage = collectionInfo.getMainSystemLanguage().getCode();
		if (!prefix.endsWith("_")) {
			prefix += "_";
		}
		int index = prefix.lastIndexOf("d_");
		if (prefix.endsWith("d_")) {
			prefix = new StringBuilder(prefix).replace(index, index + 2, "t_").toString();
		}

		Map<String, Object> copyfields = new HashMap<>();
		for (String collectionLanguage : collectionInfo.getCollectionLanguesCodes()) {
			String fieldCode = prefix + collectionLanguage;
			if (collectionLanguage.equals(valueLanguage) && value != null && !"null".equals(value)) {
				copyfields.put(fieldCode, Double.parseDouble(value));
			} else {
				copyfields.put(fieldCode, "");
			}
		}

		return copyfields;
	}

	private Map<String, Object> populateCopyFieldsOfSinglevalueSearchableDateMetadata(Object value,
																					  String copiedMetadataCodePrefix) {
		String dateFormat = systemConf.getDateFormat();

		String fieldCode = copiedMetadataCodePrefix;
		if (!fieldCode.endsWith("_")) {
			fieldCode += "_";
		}
		int index = fieldCode.lastIndexOf("_da_");
		if (fieldCode.endsWith("_da_")) {
			fieldCode = new StringBuilder(fieldCode).replace(index, index + 4, DATE_SEARCH_FIELD).toString();
		}

		KeyListMap<String, Object> keyListMap = new KeyListMap<>();
		if (value != null && value instanceof LocalDate) {
			try {
				keyListMap.add(fieldCode, ((LocalDate) value).toString(dateFormat));
			} catch (Exception e) {
				keyListMap.add(fieldCode, ((LocalDate) value).toString());
			}
			keyListMap.add(fieldCode, String.valueOf(((LocalDate) value).getYear()));
		} else {
			keyListMap.add(fieldCode, "");
		}

		return (Map) keyListMap.getNestedMap();
	}

	private Map<String, Object> populateCopyFieldsOfMultivalueSearchableDateMetadata(List<String> values,
																					 String copiedMetadataCodePrefix) {
		String dateFormat = systemConf.getDateFormat();

		String prefix = copiedMetadataCodePrefix;
		if (!prefix.contains("_")) {
			prefix += "_das_";
		}
		if (!prefix.endsWith("_")) {
			prefix += "_";
		}
		int index = prefix.lastIndexOf("_das_");
		if (prefix.endsWith("_das_")) {
			prefix = new StringBuilder(prefix).replace(index, index + 5, DATE_SEARCH_FIELD).toString();
		}

		KeyListMap<String, Object> keyListMap = new KeyListMap<>();
		for (Object value : values) {
			String fieldCode = prefix;
			if (value != null && value instanceof LocalDate) {
				try {
					keyListMap.add(fieldCode, ((LocalDate) value).toString(dateFormat));
				} catch (Exception e) {
					keyListMap.add(fieldCode, ((LocalDate) value).toString());
				}
				keyListMap.add(fieldCode, String.valueOf(((LocalDate) value).getYear()));
			} else {
				keyListMap.add(fieldCode, "");
			}
		}

		String fieldCode = prefix;
		if (!keyListMap.getNestedMap().containsKey(fieldCode)) {
			keyListMap.add(fieldCode, "");
		}
		return (Map) keyListMap.getNestedMap();
	}

	private Map<String, Object> populateCopyFieldsOfMultivalueSearchableNumberMetadata(List<String> values,
																					   String copiedMetadataCodePrefix) {

		String prefix = copiedMetadataCodePrefix;
		if (!prefix.contains("_")) {
			prefix += "_txt_";
		}
		if (!prefix.endsWith("_")) {
			prefix += "_";
		}
		int index = prefix.lastIndexOf("d_");
		if (prefix.endsWith("d_")) {
			prefix = new StringBuilder(prefix).replace(index, index + 2, "txt_").toString();
		}

		KeyListMap<String, Object> keyListMap = new KeyListMap<>();
		for (Object value : values) {
			String language = collectionInfo.getMainSystemLanguage().getCode();
			if (language != null && collectionInfo.getCollectionLanguesCodes().contains(language)) {
				String fieldCode = prefix + language;
				keyListMap.add(fieldCode, Double.parseDouble(String.valueOf(value)));
			}
		}

		for (String collectionLanguage : collectionInfo.getCollectionLanguesCodes()) {
			String fieldCode = prefix + collectionLanguage;
			if (!keyListMap.getNestedMap().containsKey(fieldCode)) {
				keyListMap.add(fieldCode, "");
			}
		}
		return (Map) keyListMap.getNestedMap();
	}

	private Map<String, Object> populateCopyFieldsOfMultivalueSearchableTextMetadata(List<String> values,
																					 String copiedMetadataCodePrefix,
																					 Locale locale) {
		String fieldCode = copiedMetadataCodePrefix + locale.getLanguage();
		KeyListMap<String, Object> keyListMap = new KeyListMap<>();
		for (String value : values) {
			keyListMap.add(fieldCode, value);
		}
		if (!keyListMap.getNestedMap().containsKey(fieldCode)) {
			keyListMap.add(fieldCode, "");
		}
		return (Map) keyListMap.getNestedMap();
	}

	private Map<String, Object> populateCopyFieldsOfMultivalueSearchableContentMetadata(List<Content> values,
																						String copiedMetadataCode) {

		KeyListMap<String, Object> keyListMap = new KeyListMap<>();
		for (Content value : values) {
			ContentVersion currentVersion = value.getCurrentVersion();
			try {
				ParsedContent parsedContent = parsedContentProvider.getParsedContentIfAlreadyParsed(currentVersion.getHash());

				String contentLanguage = null;
				if (collectionInfo.isMonoLingual()) {
					contentLanguage = collectionInfo.getMainSystemLanguage().getCode();
				} else if (parsedContent != null && collectionInfo.getCollectionLanguesCodes().contains(parsedContent.getLanguage())) {
					contentLanguage = parsedContent.getLanguage();
				} else {
					contentLanguage = collectionInfo.getMainSystemLanguage().getCode();
				}

				if (parsedContent == null || contentLanguage == null) {
					for (String collectionLanguage : collectionInfo.getCollectionLanguesCodes()) {
						keyListMap.add(copiedMetadataCode + "_" + collectionLanguage + "_ss", currentVersion.getFilename());
					}
				} else {
					keyListMap.add(copiedMetadataCode + "_" + contentLanguage + "_ss", currentVersion.getFilename());
				}
				if (parsedContent != null && contentLanguage != null &&
					!fileExtensionsExcludedFromParsing.contains(FilenameUtils.getExtension(currentVersion.getFilename()))) {
					keyListMap.add(copiedMetadataCode + "_txt_" + contentLanguage, parsedContent.getParsedContent());
				}
			} catch (ContentManagerRuntimeException_NoSuchContent e) {
				if (new FoldersLocator().getFoldersLocatorMode() != FoldersLocatorMode.PROJECT) {
					LOGGER.warn("Parsed content of '" + currentVersion.getHash() + "' was not found in vault");
				}
			}
		}

		addEmptyValuesToOtherFields(copiedMetadataCode, keyListMap);
		return (Map) keyListMap.getNestedMap();
	}

	private <T> List<T> asNotNullList(Object value) {
		if (value == null) {
			return Collections.emptyList();
		} else {
			return (List) value;
		}
	}
}
