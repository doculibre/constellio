package com.constellio.app.services.importExport.records.writers;

import com.constellio.app.services.schemas.bulkImport.data.ImportDataOptions;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.data.utils.LangUtils;
import com.constellio.model.services.records.ContentImportVersion;
import com.constellio.model.services.records.ImportContent;
import com.constellio.model.services.records.SimpleImportContent;
import com.constellio.model.services.records.StructureImportContent;
import com.sun.xml.txw2.output.IndentingXMLStreamWriter;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Map;

public class ImportRecordOfSameTypeWriter {

	IndentingXMLStreamWriter writer;

	OutputStream outputStream;

	boolean tooLateToWriteOptions;

	public ImportRecordOfSameTypeWriter(File outputFile) {

		try {
			outputFile.delete();
			XMLOutputFactory factory = XMLOutputFactory.newInstance();
			outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));
			XMLStreamWriter defaultWriter = factory.createXMLStreamWriter(outputStream, "UTF-8");
			writer = new IndentingXMLStreamWriter(defaultWriter);
			writer.setIndentStep("  ");
			writer.writeStartDocument("UTF-8", "1.0");
			writer.writeStartElement("records");
			writer.writeAttribute("datePattern", "yyyy-MM-dd");
			writer.writeAttribute("datetimePattern", "yyyy-MM-dd HH:mm:ss");

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void write(ModifiableImportRecord importRecord) {
		tooLateToWriteOptions = true;
		try {
			writer.writeStartElement("record");
			writer.writeAttribute("id", importRecord.getPreviousSystemId());
			if (StringUtils.isNotBlank(importRecord.getSchema())) {
				writer.writeAttribute("schema", importRecord.getSchema());
			}
			for (Map.Entry<String, Object> importRecordMetadata : importRecord.getFields().entrySet()) {
				Object value = importRecordMetadata.getValue();
				if (value != null) {

					if (value instanceof Collection) {
						Collection collectionValue = LangUtils.withoutNulls((Collection) value);
						if (!collectionValue.isEmpty()) {
							writer.writeStartElement(importRecordMetadata.getKey());
							writer.writeAttribute("multivalue", "true");
							String type = null;
							for (Object item : collectionValue) {
								if (item instanceof Map) {
									type = "structure";

								} else if (item instanceof LocalDate) {
									type = "date";

								} else if (item instanceof LocalDateTime) {
									type = "datetime";

								} else if (item instanceof ImportContent) {
									type = "content";
								}
							}

							if (type != null) {
								writer.writeAttribute("type", type);
							}

							for (Object item : collectionValue) {
								if (item != null) {
									writer.writeStartElement("item");
									if ("date".equals(type)) {
										writeValue(((LocalDate) item).toString("yyyy-MM-dd"));

									} else if ("datetime".equals(type)) {
										writeValue(((LocalDateTime) item).toString("yyyy-MM-dd HH:mm:ss"));

									} else {
										writeValue(item);
									}
									writer.writeEndElement();
								}

							}
							writer.writeEndElement();
						}
					} else if (value instanceof LocalDate) {
						writer.writeStartElement(importRecordMetadata.getKey());
						writer.writeAttribute("type", "date");
						writer.writeCharacters(value.toString());
						writer.writeEndElement();

					} else if (value instanceof LocalDateTime) {
						writer.writeStartElement(importRecordMetadata.getKey());
						writer.writeAttribute("type", "datetime");
						writer.writeCharacters(((LocalDateTime) value).toString("yyyy-MM-dd HH:mm:ss"));
						writer.writeEndElement();

					} else if (value instanceof StructureImportContent) {
						writer.writeStartElement("structureContent");
						writer.writeAttribute("type", "structureContent");
						writer.writeAttribute("structure", ((StructureImportContent) value).getSerializedStructure());
						writer.writeAttribute("key", importRecordMetadata.getKey());
						writer.writeEndElement();
					} else if (value instanceof ImportContent) {
						writer.writeStartElement(importRecordMetadata.getKey());
						writer.writeAttribute("type", "content");
						writeValue(value);
						writer.writeEndElement();
					} else if (value instanceof Map) {
						writer.writeStartElement(importRecordMetadata.getKey());
						writer.writeAttribute("type", "structure");
						writeValue(value);
						writer.writeEndElement();
					} else {
						writer.writeStartElement(importRecordMetadata.getKey());
						writeValue(value);
						writer.writeEndElement();
					}
				}

			}
			writer.writeEndElement();

		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	private void writeValue(Object value)
			throws XMLStreamException {
		if (value instanceof String) {
			writer.writeCharacters((String) value);
		}
		if (value instanceof Map) {
			Map<String, Object> mapValue = (Map<String, Object>) value;
			for (Map.Entry<String, Object> entry : mapValue.entrySet()) {
				if (entry.getValue() != null && entry.getValue() instanceof String) {
					writer.writeAttribute(entry.getKey(), (String) entry.getValue());
				} else if (entry.getValue() != null && entry.getValue() instanceof Collection) {
					String valueToWrite = "";
					for (Object collectionElement : (Collection) entry.getValue()) {
						valueToWrite += collectionElement + ",";
					}
					if (StringUtils.isNotBlank(valueToWrite)) {
						writer.writeAttribute(entry.getKey(), valueToWrite.substring(0, valueToWrite.length() - 1));
					}
				}
			}
		}
		if (value instanceof SimpleImportContent) {
			for (ContentImportVersion version : ((SimpleImportContent) value).getVersions()) {
				writer.writeStartElement("contentVersion");
				writer.writeAttribute("url", version.getUrl());
				writer.writeAttribute("filename", version.getFileName());
				writer.writeAttribute("major", String.valueOf(version.isMajor()));
				writer.writeEndElement();
			}
		}
	}

	public void close() {
		try {

			writer.writeEndElement();
			writer.writeEndDocument();
			writer.flush();
			writer.close();
			outputStream.close();

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void write(ImportDataOptions options) {
		if (tooLateToWriteOptions) {
			throw new ImpossibleRuntimeException("Too late to write options");
		}
		try {
			if (!options.isImportAsLegacyId()) {
				writer.writeAttribute("importAsLegacyId", "false");
			}
			if (options.isMergeExistingRecordWithSameUniqueMetadata()) {
				writer.writeAttribute("mergeExistingRecordWithSameUniqueMetadata", "true");
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

}
