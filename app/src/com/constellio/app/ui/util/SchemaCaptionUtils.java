/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.app.ui.util;

import static com.constellio.app.ui.i18n.i18n.$;

import java.io.Serializable;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.constellio.app.services.factories.ConstellioFactories;
import com.constellio.app.ui.entities.MetadataVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesRuntimeException.NoSuchRecordWithId;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.SchemaUtils;

public class SchemaCaptionUtils implements Serializable {

	private static final Logger LOGGER = LoggerFactory.getLogger(SchemaCaptionUtils.class);

	private static final String EXPRESSION_START = "{";
	private static final String EXPRESSION_END = "}";

	public static String getCaptionForRecordId(String recordId) {
		String caption;
		if (StringUtils.isNotBlank(recordId)) {
			ConstellioFactories constellioFactories = ConstellioFactories.getInstance();
			ModelLayerFactory modelLayerFactory = constellioFactories.getModelLayerFactory();
			RecordServices recordServices = modelLayerFactory.newRecordServices();
			try {
				Record record = recordServices.getDocumentById(recordId);
				String schemaCode = record.getSchemaCode();
				String schemaTypeCode = new SchemaUtils().getSchemaTypeCode(schemaCode);
				String captionForSchemaTypeCode = getCaptionForSchemaTypeCode(schemaTypeCode);

				String captionFormatKey = "caption." + schemaTypeCode + ".record";
				String captionFormat = $(captionFormatKey);
				if (captionFormatKey.equals(captionFormat)) {
					captionFormatKey = "caption.allTypes.record";
					captionFormat = $(captionFormatKey);
				}

				caption = applyPattern(captionFormat, record);
				if (StringUtils.isNotBlank(captionForSchemaTypeCode)) {
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

	public static String getCaptionForRecord(Record record) {
		String caption;
		if (record != null) {
			try {
				String schemaCode = record.getSchemaCode();
				String schemaTypeCode = new SchemaUtils().getSchemaTypeCode(schemaCode);
				String captionForSchemaTypeCode = getCaptionForSchemaTypeCode(schemaTypeCode);

				String captionFormatKey = "caption." + schemaTypeCode + ".record";
				String captionFormat = $(captionFormatKey);
				if (captionFormatKey.equals(captionFormat)) {
					captionFormatKey = "caption.allTypes.record";
					captionFormat = $(captionFormatKey);
				}

				caption = applyPattern(captionFormat, record);
				if (StringUtils.isNotBlank(captionForSchemaTypeCode)) {
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

	public static String getCaptionForRecordVO(RecordVO recordVO) {
		String caption;
		if (recordVO != null) {
			try {
				String schemaCode = recordVO.getSchema().getCode();
				String schemaTypeCode = new SchemaUtils().getSchemaTypeCode(schemaCode);
				String captionForSchemaTypeCode = getCaptionForSchemaTypeCode(schemaTypeCode);

				String captionFormatKey = "caption." + schemaTypeCode + ".record";
				String captionFormat = $(captionFormatKey);
				if (captionFormatKey.equals(captionFormat)) {
					captionFormatKey = "caption.allTypes.record";
					captionFormat = $(captionFormatKey);
				}

				caption = applyPattern(captionFormat, recordVO);
				if (StringUtils.isNotBlank(captionForSchemaTypeCode)) {
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

	public static String getCaptionForSchema(String schemaCode) {
		String schemaTypeCode = new SchemaUtils().getSchemaTypeCode(schemaCode);
		return getCaptionForSchemaTypeCode(schemaTypeCode);
	}

	public static String getCaptionForSchemaTypeCode(String schemaTypeCode) {
		String key = "caption." + schemaTypeCode;
		String caption = $(key);
		if (key.equals(caption)) {
			key = "caption.allTypes";
			caption = $(key);
		}
		return caption;
	}

	private static String applyPattern(String pattern, Record record) {
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
					value = record.get(metadata);
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

	private static String applyPattern(String pattern, RecordVO recordVO) {
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
					value = recordVO.get(metadata);
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
