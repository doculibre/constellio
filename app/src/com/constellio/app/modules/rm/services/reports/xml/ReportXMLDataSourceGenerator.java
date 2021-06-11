package com.constellio.app.modules.rm.services.reports.xml;

import com.constellio.app.api.extensions.XmlDataSourceExtension.XmlDataSourceExtensionExtraMetadataInformationParams;
import com.constellio.app.api.extensions.XmlDataSourceExtension.XmlDataSourceExtensionExtraReferencesParams;
import com.constellio.app.extensions.AppLayerCollectionExtensions;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.services.collections.CollectionsManager;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.app.ui.entities.MetadataValueVO;
import com.constellio.app.ui.entities.RecordVO;
import com.constellio.app.ui.entities.RecordVO.VIEW_MODE;
import com.constellio.app.ui.framework.builders.RecordToVOBuilder;
import com.constellio.app.ui.i18n.i18n;
import com.constellio.app.ui.pages.base.SessionContext;
import com.constellio.model.entities.EnumWithSmallCode;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.ModifiableStructure;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.StatusFilter;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.groupingBy;

class ReportXMLDataSourceGenerator implements XMLDataSourceGenerator {

	private final RecordServices recordServices;
	private final RMSchemasRecordsServices rm;
	private final MetadataSchemasManager metadataSchemasManager;
	private final MetadataSchemaTypes metadataSchemaTypes;
	private final ObjectMapper objectMapper;
	private final SearchServices searchServices;
	private final AppLayerCollectionExtensions appLayerExtensions;
	private final CollectionsManager collectionsManager;

	private final static int DEFAULT_REF_DEPTH = 2;

	public ReportXMLDataSourceGenerator(String collection, AppLayerFactory appLayerFactory) {
		recordServices = appLayerFactory.getModelLayerFactory().newRecordServices();
		rm = new RMSchemasRecordsServices(collection, appLayerFactory);
		metadataSchemasManager = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager();
		metadataSchemaTypes = metadataSchemasManager.getSchemaTypes(collection);
		searchServices = appLayerFactory.getModelLayerFactory().newSearchServices();
		collectionsManager = appLayerFactory.getCollectionsManager();

		appLayerExtensions = appLayerFactory.getExtensions().forCollection(collection);

		objectMapper = new ObjectMapper();
		objectMapper.setVisibility(objectMapper.getSerializationConfig().getDefaultVisibilityChecker()
				.withFieldVisibility(JsonAutoDetect.Visibility.ANY)
				.withGetterVisibility(JsonAutoDetect.Visibility.NONE)
				.withSetterVisibility(JsonAutoDetect.Visibility.NONE)
				.withCreatorVisibility(JsonAutoDetect.Visibility.NONE));
		objectMapper.registerModule(new JodaModule());
		objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
	}

	@Override
	public InputStream generate(XMLDataSourceGeneratorParams params) {
		AtomicInteger currentDepth = new AtomicInteger();
		boolean forCreation = params.isXmlForTest();
		int maxDepth = params.getDepth() != null ? params.getDepth() : DEFAULT_REF_DEPTH;

		org.dom4j.Document xmlDocument = DocumentHelper.createDocument();
		Element root = xmlDocument.addElement("Constellio");

		MetadataSchemaType schemaType = metadataSchemaTypes.getSchemaType(params.getSchemaType());
		List<Record> records = searchServices.search(new LogicalSearchQuery(from(schemaType)
				.where(Schemas.IDENTIFIER).isIn(params.getRecordIds()))
				.sortAsc(Schemas.TITLE).sortAsc(Schemas.CODE)
				.filteredByStatus(StatusFilter.ACTIVES));
		RecordXMLInfos infos = addRecordsToXmlDocument(root, records, params.getLocale(), true, forCreation,
				currentDepth, maxDepth, params.getRequiredMetadataCodes());

		if (!params.isIgnoreReferences()) {
			Element referencesElement = root.addElement("references");
			Map<String, List<Record>> referenceRecordsByType = rm.get(new ArrayList<>(infos.referenceIds)).stream()
					.collect(groupingBy(Record::getTypeCode));
			referenceRecordsByType.values().forEach(referenceRecords ->
					addRecordsToXmlDocument(referencesElement, referenceRecords, params.getLocale(), true,
							forCreation, currentDepth, maxDepth, params.getRequiredMetadataCodes()));

			addStructuresToXmlDocument(referencesElement, infos.getStructuresByRecordId(), forCreation);
		}

		try {
			return new ByteArrayInputStream(xmlDocumentToBytes(xmlDocument));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void addStructuresToXmlDocument(Element element, List<ModifiableStructure> structures, String recordId,
											boolean forCreation) {
		HashMap<String, List<ModifiableStructure>> structuresByRecordId = new HashMap<>();
		structuresByRecordId.put(recordId, structures);
		addStructuresToXmlDocument(element, structuresByRecordId, forCreation);
	}

	private void addStructuresToXmlDocument(Element element,
											Map<String, List<ModifiableStructure>> structuresByRecordId,
											boolean forCreation) {
		try {
			structuresByRecordId.forEach((recordId, structures) -> {
				structures.forEach(structure -> {
					Element structureElement = element.addElement(structure.getClass().getSimpleName());
					Map<String, Object> structureMap = objectMapper.convertValue(structure, Map.class);
					addStructureMapValuesToXmlElement(structureElement, structureMap, structure, recordId, forCreation);
				});
			});
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void addStructureMapValuesToXmlElement(Element element, Map<String, Object> map,
												   ModifiableStructure structure,
												   String recordId, boolean forCreation) {
		map.forEach((key, value) -> {
			if (forCreation || value != null) {
				if (value instanceof ArrayList) {
					element.addElement(key).addText(((ArrayList<Object>) value).stream().map(Object::toString)
							.collect(Collectors.joining(";")));
				} else {
					element.addElement(key).addText(value != null ? value.toString() : getNullValue(forCreation));
				}
				if (!(value instanceof Map)) {
					addStructureTitleCodesToXmlElement(element, key, value);
				}
			}
		});
		element.addElement("toString").addText(structure.toString());
		element.addElement("parentRecordId").addText(recordId);
	}

	private void addStructureTitleCodesToXmlElement(Element element, String key, Object value) {
		if (value == null) {
			return;
		}
		List<String> recordIds = new ArrayList<>();
		if (value instanceof ArrayList) {
			recordIds = ((ArrayList<Object>) value).stream().map(Object::toString).collect(Collectors.toList());
		} else {
			recordIds.add(value.toString());
		}

		List<Record> records = recordIds.stream().map(recordId -> recordServices.getRecordsCaches().getRecord(recordId))
				.filter(Objects::nonNull).collect(Collectors.toList());

		if (!records.isEmpty()) {
			String titles = records.stream().map(record -> ((String) record.get(Schemas.TITLE)))
					.collect(Collectors.joining(";"));
			element.addElement(key + "_title").addText(titles);
			String codes = records.stream().map(record -> ((String) record.get(Schemas.CODE)))
					.collect(Collectors.joining(";"));
			if (StringUtils.isNotBlank(codes)) {
				element.addElement(key + "_code").addText(codes);
			}
		}
	}

	private void addStructureToStringToXmlElement(Element element, String name, Object value, boolean forCreation,
												  Set<String> requiredMetadataCodes) {
		if (!forCreation && value == null) {
			return;
		}
		String elementName = name + "_toString";
		if (!CollectionUtils.isEmpty(requiredMetadataCodes) && !requiredMetadataCodes.contains(elementName)) {
			return;
		}
		String toString = null;
		if (value != null) {
			if (value instanceof ArrayList) {
				toString = ((ArrayList<Object>) value).stream().map(Object::toString).collect(Collectors.joining(";"));
			} else {
				toString = value.toString();
			}
		}
		element.addElement(elementName).addText(toString != null ? toString : getNullValue(true));
	}

	private RecordXMLInfos addRecordsToXmlDocument(Element root, List<Record> records, Locale locale,
												   boolean processReferences, boolean forCreation,
												   AtomicInteger currentDepth, int maxDepth,
												   Set<String> requiredMetadataCodes) {
		if (records.isEmpty()) {
			return new RecordXMLInfos(Collections.emptySet(), Collections.emptyMap());
		}
		currentDepth.incrementAndGet();

		Set<String> referenceIds = new HashSet<>();
		Map<String, List<ModifiableStructure>> structuresByRecordId = new HashMap<>();

		records.forEach(record -> {
			List<ModifiableStructure> structures = new ArrayList<>();

			Element recordElement = root.addElement(StringUtils.capitalize(record.getTypeCode()));
			recordElement.addAttribute("schemaCode", record.getSchemaCode());

			MetadataSchema schema = metadataSchemasManager.getSchemaOf(record);
			addCustomRecordElement(recordElement, "collection_code", $("Collection.code"),
					record.getCollection(), forCreation, requiredMetadataCodes);
			addCustomRecordElement(recordElement, "collection_title", $("Collection.title"),
					collectionsManager.getCollection(record.getCollection()).getName(), forCreation, requiredMetadataCodes);

			schema.getMetadatas().forEach(metadata -> {
				addMetadataToXmlElement(recordElement, record, metadata, locale, structures, processReferences,
						forCreation, currentDepth, maxDepth, requiredMetadataCodes);
			});

			if (processReferences && currentDepth.get() <= maxDepth) {
				SessionContext sessionContext = ConstellioUI.getCurrentSessionContext();
				if (sessionContext != null) {
					RecordToVOBuilder recordToVOBuilder = new RecordToVOBuilder();
					RecordVO recordVO = recordToVOBuilder.build(record, VIEW_MODE.DISPLAY, sessionContext);
					recordVO.getMetadataValues().stream()
							.filter(metadataValueVO -> metadataValueVO.getMetadata().isSynthetic())
							.forEach(metadataValueVO -> {
								addSyntheticMetadataToXmlElement(recordElement, metadataValueVO, locale, forCreation,
										currentDepth, maxDepth, requiredMetadataCodes);
							});
				}
			}

			if (!structures.isEmpty()) {
				structuresByRecordId.put(record.getId(), structures);
			}

			XmlDataSourceExtensionExtraReferencesParams params = new XmlDataSourceExtensionExtraReferencesParams(record);
			appLayerExtensions.xmlDataSourceExtensions.forEach(extension -> {
				extension.getExtraReferences(params).forEach((key, value) -> {
					Element extraReferencesElement = recordElement.addElement(key);
					addRecordsToXmlDocument(extraReferencesElement, value, locale, false, forCreation,
							currentDepth, maxDepth, requiredMetadataCodes);
				});
			});

			if (processReferences) {
				schema.getAllReferences().forEach(metadata -> {
					if (record.get(metadata) != null) {
						referenceIds.addAll(record.getValues(metadata));
					}
				});
			}
		});
		currentDepth.decrementAndGet();
		return new RecordXMLInfos(referenceIds, structuresByRecordId);
	}

	private void addCustomRecordElement(Element recordElement, String code, String label, String text,
										boolean forCreation, Set<String> requiredMetadataCodes) {
		if (CollectionUtils.isEmpty(requiredMetadataCodes) || requiredMetadataCodes.contains(code)) {
			Element customElement = recordElement.addElement(code).addText(text);
			if (forCreation) {
				customElement.addAttribute("label", label).addAttribute("code", code);
			}
		}
	}

	private void addSyntheticMetadataToXmlElement(Element element, MetadataValueVO metadataValueVO, Locale locale,
												  boolean forCreation, AtomicInteger currentDepth, int maxDepth,
												  Set<String> requiredMetadataCodes) {
		Element metadataElement = element.addElement(metadataValueVO.getMetadata().getLocalCode());
		if (forCreation) {
			metadataElement.addAttribute("label", metadataValueVO.getMetadata().getLabel(locale))
					.addAttribute("code", metadataValueVO.getMetadata().getCode());
		}

		List<String> referenceIds = metadataValueVO.getMetadata().isMultivalue() ? metadataValueVO.getValue() :
									singletonList(metadataValueVO.getValue());
		List<Record> referenceRecords = rm.get(new ArrayList<>(referenceIds));
		addRecordsToXmlDocument(metadataElement, referenceRecords, locale, false, forCreation,
				currentDepth, maxDepth, requiredMetadataCodes);

		addGlobalMetadataToXmlElement(element, metadataValueVO, Schemas.TITLE, locale, forCreation);
		addGlobalMetadataToXmlElement(element, metadataValueVO, Schemas.CODE, locale, forCreation);
	}

	private void addMetadataToXmlElement(Element element, Record record, Metadata metadata, Locale locale,
										 List<ModifiableStructure> allStructures, boolean processReferences,
										 boolean forCreation, AtomicInteger currentDepth, int maxDepth,
										 Set<String> requiredMetadataCodes) {
		if (!forCreation && CollectionUtils.isEmpty(record.getValues(metadata))) {
			return;
		}

		if (CollectionUtils.isEmpty(requiredMetadataCodes) || requiredMetadataCodes.contains(metadata.getLocalCode())) {
			Element metadataElement = element.addElement(metadata.getLocalCode());
			if (forCreation) {
				metadataElement.addAttribute("label", metadata.getLabel(Language.withLocale(locale)))
						.addAttribute("code", metadata.getCode());
			}

			if (metadata.getType() == MetadataValueType.REFERENCE && metadataIsNotEmpty(record, metadata) && processReferences) {
				List<String> referenceIds = metadata.isMultivalue() ? record.get(metadata) : singletonList(record.get(metadata));
				List<Record> referenceRecords = rm.get(new ArrayList<>(referenceIds));
				addRecordsToXmlDocument(metadataElement, referenceRecords, locale, true, forCreation,
						currentDepth, maxDepth, requiredMetadataCodes);
			} else if (metadata.getType() == MetadataValueType.STRUCTURE && metadataIsNotEmpty(record, metadata)) {
				List<ModifiableStructure> structures = metadata.isMultivalue() ? record.get(metadata) : singletonList(record.get(metadata));
				addStructuresToXmlDocument(metadataElement, structures, record.getId(), forCreation);
				allStructures.addAll(structures);
			} else {
				metadataElement.addText(getRecordValue(record, metadata, forCreation));
			}

			XmlDataSourceExtensionExtraMetadataInformationParams params =
					new XmlDataSourceExtensionExtraMetadataInformationParams(element, metadata, record, getNullValue(forCreation));
			appLayerExtensions.xmlDataSourceExtensions.forEach(extension -> extension.addExtraMetadataInformation(params));
		}

		if (metadata.getType() == MetadataValueType.REFERENCE) {
			addGlobalMetadataToXmlElement(element, record, metadata, Schemas.TITLE, locale, forCreation, requiredMetadataCodes);
			addGlobalMetadataToXmlElement(element, record, metadata, Schemas.CODE, locale, forCreation, requiredMetadataCodes);
		} else if (metadata.getType() == MetadataValueType.ENUM) {
			addEnumMetadatasToXmlElement(element, record, metadata, locale, forCreation, requiredMetadataCodes);
		} else if (metadata.getType() == MetadataValueType.STRUCTURE) {
			addStructureToStringToXmlElement(element, metadata.getLocalCode(), record.get(metadata), forCreation, requiredMetadataCodes);
		}

	}

	private boolean metadataIsNotEmpty(Record record, Metadata metadata) {
		if (metadata.isMultivalue() && record.get(metadata) != null) {
			return !record.<List<Object>>get(metadata).isEmpty();
		}
		return record.get(metadata) != null;
	}

	private void addEnumMetadatasToXmlElement(Element element, Record record, Metadata enumMetadata, Locale locale,
											  boolean forCreation, Set<String> requiredMetadataCodes) {
		String code = getNullValue(forCreation);
		String title = getNullValue(forCreation);
		if (record.get(enumMetadata) != null) {
			if (enumMetadata.isMultivalue()) {
				List<EnumWithSmallCode> enumValues = record.get(enumMetadata);
				code = enumValues.stream().map(EnumWithSmallCode::getCode).collect(Collectors.joining(";"));
				title = enumValues.stream().map(i18n::$).collect(Collectors.joining(";"));
			} else {
				EnumWithSmallCode enumValue = record.get(enumMetadata);
				code = enumValue.getCode();
				title = $(enumValue);
			}
		}
		String enumTitleElementCode = enumMetadata.getLocalCode() + "_" + Schemas.TITLE.getLocalCode();
		if (CollectionUtils.isEmpty(requiredMetadataCodes) || requiredMetadataCodes.contains(enumTitleElementCode)) {
			Element enumTitleElement = element.addElement(enumTitleElementCode).addText(title);
			if (forCreation) {
				enumTitleElement.addAttribute("label", enumMetadata.getLabel(Language.withLocale(locale)))
						.addAttribute("code", enumMetadata.getCode());
			}
		}
		String enumCodeElementCode = enumMetadata.getLocalCode() + "_" + Schemas.CODE.getLocalCode();
		if (CollectionUtils.isEmpty(requiredMetadataCodes) || requiredMetadataCodes.contains(enumTitleElementCode)) {
			Element enumCodeElement = element.addElement(enumCodeElementCode).addText(code);
			if (forCreation) {
				enumCodeElement.addAttribute("label", enumMetadata.getLabel(Language.withLocale(locale)))
						.addAttribute("code", enumMetadata.getCode());
			}
		}
	}

	private String getNullValue(boolean forCreation) {
		return forCreation ? "null" : "";
	}

	private void addGlobalMetadataToXmlElement(Element element, MetadataValueVO metadataValueVO,
											   Metadata globalMetadata, Locale locale, boolean forCreation) {
		MetadataSchemaType referencedSchemaType = metadataSchemasManager.getSchemaTypes(metadataValueVO.getMetadata().getCollection())
				.getSchemaType(metadataValueVO.getMetadata().getSchemaTypeCode());
		if (!referencedSchemaType.hasMetadataWithCode(globalMetadata.getCode())) {
			return;
		}

		List<String> values = new ArrayList<>();
		if (metadataValueVO.getValue() != null) {
			List<String> referenceIds = new ArrayList<>(metadataValueVO.getMetadata().isMultivalue() ?
														metadataValueVO.getValue() : singletonList(metadataValueVO.getValue()));
			referenceIds.forEach(referenceId -> {
				Record referencedRecord = rm.get(referenceId);
				String value = referencedRecord.get(globalMetadata);
				if (value != null) {
					values.add(value);
				}
			});
		}
		Element globalElement = element.addElement(metadataValueVO.getMetadata().getLocalCode() + "_" + globalMetadata.getLocalCode())
				.addText(values.isEmpty() ? getNullValue(forCreation) : values.size() == 1 ? values.get(0) : String.join(";", values));
		if (forCreation) {
			globalElement.addAttribute("label", metadataValueVO.getMetadata().getLabel(locale))
					.addAttribute("code", referencedSchemaType.getMetadata(globalMetadata.getCode()).getCode());

		}
	}

	private void addGlobalMetadataToXmlElement(Element element, Record record, Metadata referenceMetadata,
											   Metadata globalMetadata, Locale locale, boolean forCreation,
											   Set<String> requiredMetadataCodes) {
		String code = referenceMetadata.getLocalCode() + "_" + globalMetadata.getLocalCode();
		if (!CollectionUtils.isEmpty(requiredMetadataCodes) && !requiredMetadataCodes.contains(code)) {
			return;
		}
		MetadataSchemaType referencedSchemaType = metadataSchemasManager.getSchemaTypes(record.getCollection())
				.getSchemaType(referenceMetadata.getReferencedSchemaTypeCode());
		if (!referencedSchemaType.hasMetadataWithCode(globalMetadata.getCode())) {
			return;
		}

		List<String> values = new ArrayList<>();
		List<String> referenceIds = new ArrayList<>();
		if (record.get(referenceMetadata) != null) {
			if (referenceMetadata.isMultivalue()) {
				referenceIds.addAll(record.get(referenceMetadata));
			} else {
				referenceIds.add(record.get(referenceMetadata));
			}
			referenceIds.forEach(referenceId -> {
				Record referencedRecord = rm.get(referenceId);
				String value = referencedRecord.get(globalMetadata);
				if (value != null) {
					values.add(value);
				}
			});
		}
		Element globalElement = element.addElement(code).addText(values.isEmpty() ? getNullValue(forCreation) :
																 values.size() == 1 ? values.get(0) : String.join(";", values));
		if (forCreation) {
			globalElement.addAttribute("label", referenceMetadata.getLabel(Language.withLocale(locale)))
					.addAttribute("code", referencedSchemaType.getMetadata(globalMetadata.getCode()).getCode());

		}
	}

	private String getRecordValue(Record record, Metadata metadata, boolean forCreation) {
		if (record.get(metadata) == null) {
			return getNullValue(forCreation);
		}
		if (metadata.getType() == MetadataValueType.STRUCTURE) {
			if (metadata.isMultivalue()) {
				List<ModifiableStructure> structures = record.getValues(metadata);
				String values = structures.stream().filter(Objects::nonNull).map(Object::toString)
						.collect(Collectors.joining(";"));
				return !values.equals("") ? values : getNullValue(forCreation);
			}
			ModifiableStructure structure = record.get(metadata);
			return structure != null ? structure.toString() : getNullValue(forCreation);
		} else {
			if (metadata.isMultivalue()) {
				String values = record.getValues(metadata).stream().filter(Objects::nonNull)
						.map(Object::toString).collect(Collectors.joining(";"));
				return !values.equals("") ? values : getNullValue(forCreation);
			}
			Object value = record.get(metadata);
			return value.toString();
		}
	}

	public static byte[] xmlDocumentToBytes(org.dom4j.Document xmlDocument) throws IOException {
		try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
			XMLWriter xmlWriter = new XMLWriter(outputStream, OutputFormat.createPrettyPrint());
			xmlWriter.write(xmlDocument);
			xmlWriter.close();
			return outputStream.toByteArray();
		}
	}

	@Data
	@AllArgsConstructor
	private static class RecordXMLInfos {
		Set<String> referenceIds;
		Map<String, List<ModifiableStructure>> structuresByRecordId;
	}
}
