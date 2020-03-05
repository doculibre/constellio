package com.constellio.app.ui.util;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.model.entities.records.LocalisedRecordMetadataRetrieval;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesRuntimeException.NoSuchRecordWithId;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.SchemaUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Locale;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.app.ui.i18n.i18n.isRightToLeft;
import static com.constellio.model.entities.records.LocalisedRecordMetadataRetrieval.PREFERRING;

public class SchemaCaptionUtils implements Serializable {

	private static final Logger LOGGER = LoggerFactory.getLogger(SchemaCaptionUtils.class);

	private static final String EXPRESSION_START = "{";
	private static final String EXPRESSION_END = "}";

	public static String getCaptionForRecordId(String recordId) {
		return getCaptionForRecordId(recordId, ConstellioUI.getCurrentSessionContext().getCurrentLocale());
	}

	public static String getCaptionForRecordId(String recordId, Locale locale) {
		String caption;
		if (StringUtils.isNotBlank(recordId)) {
			ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
			ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();
			RecordServices recordServices = modelLayerFactory.newRecordServices();
			try {
				Record record = recordServices.getDocumentById(recordId);
				String schemaCode = record.getSchemaCode();
				String schemaTypeCode = SchemaUtils.getSchemaTypeCode(schemaCode);
				String captionForSchemaTypeCode = getCaptionForSchemaTypeCode(schemaTypeCode, locale);

				String captionFormatKey = "caption." + schemaTypeCode + ".record";
				String captionFormat = $(captionFormatKey);
				if (captionFormatKey.equals(captionFormat)) {
					captionFormatKey = "caption.allTypes.record";
					captionFormat = $(captionFormatKey);
				}

				caption = applyPattern(captionFormat, record, locale);

				if (StringUtils.isNotBlank(captionForSchemaTypeCode)) {
					if (isRightToLeft()) {
						caption = caption + " " + captionForSchemaTypeCode;
					} else {
						caption = captionForSchemaTypeCode + " " + caption;
					}
				}
			} catch (NoSuchRecordWithId e) {
				caption = "";
				LOGGER.warn(e.getMessage(), e);
			}
		} else {
			caption = "";
		}
		return caption;
	}

	public static String getNiceTitleForRecordId(String recordId, Locale locale) {
		String niceTitle;
		if (StringUtils.isNotBlank(recordId)) {
			ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
			ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();
			RecordServices recordServices = modelLayerFactory.newRecordServices();
			MetadataSchemasManager schemas = modelLayerFactory.getMetadataSchemasManager();
			try {
				Record record = recordServices.getDocumentById(recordId);
				MetadataSchema recordSchema = schemas.getSchemaOf(record);
				String descriptionLocalCode = Schemas.DESCRIPTION_STRING.getLocalCode();
				if (recordSchema.hasMetadataWithCode(descriptionLocalCode)) {
					niceTitle = record.get(recordSchema.getMetadata(descriptionLocalCode));
				} else {
					niceTitle = "";
				}
			} catch (NoSuchRecordWithId e) {
				niceTitle = "";
				LOGGER.warn(e.getMessage(), e);
			}
		} else {
			niceTitle = "";
		}
		return niceTitle;
	}


	public static String getCaptionForRecord(Record record, Locale locale) {
		String caption;
		if (record != null) {
			try {
				String schemaCode = record.getSchemaCode();
				String schemaTypeCode = new SchemaUtils().getSchemaTypeCode(schemaCode);
				String captionForSchemaTypeCode = getCaptionForSchemaTypeCode(schemaTypeCode, locale);

				String captionFormatKey = "caption." + schemaTypeCode + ".record";
				String captionFormat = $(captionFormatKey, locale);
				if (captionFormatKey.equals(captionFormat)) {
					captionFormatKey = "caption.allTypes.record";
					captionFormat = $(captionFormatKey, locale);
				}

				caption = applyPattern(captionFormat, record, locale);
				if (StringUtils.isNotBlank(captionForSchemaTypeCode)) {
					//					if (isRightToLeft()) {
					//						caption = caption + " " + captionForSchemaTypeCode;
					//					} else {
					//						caption = captionForSchemaTypeCode + " " + caption;
					//					}
					caption = captionForSchemaTypeCode + " " + caption;
				}
			} catch (NoSuchRecordWithId e) {
				caption = "";
				LOGGER.warn(e.getMessage(), e);
			}
		} else {
			caption = "";
		}
		return caption;
	}

	public static String getShortCaptionForRecord(Record record, Locale locale) {
		String caption;
		if (record != null) {
			try {
				String schemaCode = record.getSchemaCode();
				String schemaTypeCode = new SchemaUtils().getSchemaTypeCode(schemaCode);
				String captionForSchemaTypeCode = getCaptionForSchemaTypeCode(schemaTypeCode, locale);

				String captionFormatKey = "caption." + schemaTypeCode + ".record.short";
				String captionFormat = $(captionFormatKey);
				if (captionFormatKey.equals(captionFormat)) {
					captionFormatKey = "caption." + schemaTypeCode + ".record";
					captionFormat = $(captionFormatKey);
					if (captionFormatKey.equals(captionFormat)) {
						captionFormatKey = "caption.allTypes.record";
						captionFormat = $(captionFormatKey);
					}
				}

				caption = applyPattern(captionFormat, record, locale);
				if (StringUtils.isNotBlank(captionForSchemaTypeCode)) {
					if (isRightToLeft()) {
						caption = caption + " " + captionForSchemaTypeCode;
					} else {
						caption = captionForSchemaTypeCode + " " + caption;
					}
				}
			} catch (NoSuchRecordWithId e) {
				caption = "";
				LOGGER.warn(e.getMessage(), e);
			}
		} else {
			caption = "";
		}
		return caption;
	}

	public static String getCaptionForRecordVO(RecordVO recordVO, Locale locale) {
		String caption;
		if (recordVO != null) {
			try {
				String schemaCode = recordVO.getSchema().getCode();
				String schemaTypeCode = new SchemaUtils().getSchemaTypeCode(schemaCode);
				String captionForSchemaTypeCode = getCaptionForSchemaTypeCode(schemaTypeCode, locale);

				String captionFormatKey = "caption." + schemaTypeCode + ".record";
				String captionFormat = $(captionFormatKey);
				if (captionFormatKey.equals(captionFormat)) {
					captionFormatKey = "caption.allTypes.record";
					captionFormat = $(captionFormatKey);
				}

				caption = applyPattern(captionFormat, recordVO, locale, PREFERRING);
				if (StringUtils.isNotBlank(captionForSchemaTypeCode)) {
					if (isRightToLeft()) {
						caption = caption + " " + captionForSchemaTypeCode;
					} else {
						caption = captionForSchemaTypeCode + " " + caption;
					}
				}
			} catch (NoSuchRecordWithId e) {
				caption = "";
				LOGGER.warn(e.getMessage(), e);
			}
		} else {
			caption = "";
		}
		return caption;
	}

	public static String getCaptionForSchema(String schemaCode, Locale locale) {
		String schemaTypeCode = new SchemaUtils().getSchemaTypeCode(schemaCode);
		return getCaptionForSchemaTypeCode(schemaTypeCode, locale);
	}

	public static String getCaptionForSchemaTypeCode(String schemaTypeCode, Locale locale) {
		String key = "caption." + schemaTypeCode;
		String caption = $(key, locale);
		if (key.equals(caption)) {
			key = "caption.allTypes";
			caption = $(key, locale);
		}
		return caption;
	}


	private static String applyPattern(String pattern, Record record, Locale locale) {
		String collection = record.getCollection();
		ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
		ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();
		MetadataSchemasManager metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
		MetadataSchemaTypes metadataSchemaTypes = metadataSchemasManager.getSchemaTypes(collection);

		String schemaCode = record.getSchemaCode();

		StringBuffer sb = new StringBuffer(pattern);
		int start = pattern.length();
		while ((start = pattern.lastIndexOf(EXPRESSION_START, start - 1)) != -1) {
			int end = pattern.indexOf(EXPRESSION_END, start);
			String metadataCode = pattern.substring(start + EXPRESSION_START.length(), end);
			String value = null;
			try {
				if ("id".equals(metadataCode)) {
					value = record.getId();
					try {
						value = "" + Long.parseLong(value);
					} catch (Exception e) {
						// Ignore, not a number
					}
				} else {
					Metadata metadata = metadataSchemaTypes.getMetadata(schemaCode + "_" + metadataCode);
					value = record.get(metadata, locale);
				}
			} catch (Exception e) {
				LOGGER.warn("Could not compute caption", e);
				value = null;
			}
			if (value == null) {
				value = "";
			}
			sb.replace(start, end + EXPRESSION_END.length(), value);
		}
		return sb.toString();
	}

	private static String applyPattern(String pattern, RecordVO recordVO, Locale locale,
									   LocalisedRecordMetadataRetrieval mode) {
		StringBuffer sb = new StringBuffer(pattern);
		int start = pattern.length();
		while ((start = pattern.lastIndexOf(EXPRESSION_START, start - 1)) != -1) {
			int end = pattern.indexOf(EXPRESSION_END, start);
			String metadataCode = pattern.substring(start + EXPRESSION_START.length(), end);
			String value = null;
			try {
				if ("id".equals(metadataCode)) {
					value = recordVO.getId();
					try {
						value = "" + Long.parseLong(value);
					} catch (Exception e) {
						// Ignore, not a number
					}
				} else {
					MetadataVO metadata = recordVO.getMetadata(metadataCode);
					value = recordVO.get(metadata, locale, mode);
				}
			} catch (Exception e) {
				value = null;
			}
			if (value == null) {
				value = "";
			}
			sb.replace(start, end + EXPRESSION_END.length(), value);
		}
		return sb.toString();
	}
}
