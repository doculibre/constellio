package com.constellio.app.services.records;

import static com.constellio.app.ui.i18n.i18n.$;
import static com.constellio.model.entities.Language.withCode;
import static com.constellio.model.entities.schemas.MetadataValueType.REFERENCE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.constellio.app.extensions.AppLayerExtensions;
import com.constellio.app.extensions.sequence.AvailableSequence;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.utils.KeySetMap;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.entries.SequenceDataEntry;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.SchemaUtils;

public class AvailableSequencesServices {

	AppLayerFactory appLayerFactory;
	AppLayerExtensions extensions;
	MetadataSchemasManager metadataSchemasManager;
	CollectionsListManager collectionsListManager;

	public AvailableSequencesServices(AppLayerFactory appLayerFactory) {
		this.appLayerFactory = appLayerFactory;
		extensions = this.appLayerFactory.getExtensions();
		metadataSchemasManager = appLayerFactory.getModelLayerFactory().getMetadataSchemasManager();
		collectionsListManager = appLayerFactory.getModelLayerFactory().getCollectionsListManager();
	}

	public List<AvailableSequence> getAvailableSequencesForRecord(Record record) {

		List<AvailableSequence> availableSequences = new ArrayList<>();
		availableSequences.addAll(appLayerFactory.getExtensions().forCollectionOf(record).getAvailableSequencesForRecord(record));
		availableSequences.addAll(getAvailableSequencesUsingSchemaTypeOn(record));

		return withoutDuplicates(availableSequences);
	}

	public List<AvailableSequence> getAvailableGlobalSequences() {
		List<AvailableSequence> availableSequences = new ArrayList<>();
		availableSequences.addAll(extensions.getSystemWideExtensions().getAvailableSequences());
		availableSequences.addAll(getAvailableSequencesUsingCollectionsSchemaTypes());
		return withoutDuplicates(availableSequences);
	}

	private List<AvailableSequence> getAvailableSequencesUsingSchemaTypeOn(Record record) {
		List<AvailableSequence> availableSequences = new ArrayList<>();
		MetadataSchemaTypes types = metadataSchemasManager.getSchemaTypes(record.getCollection());
		Set<String> typesWithSequenceFeedingRecord = new HashSet<>();

		for (MetadataSchemaType schemaType : types.getSchemaTypes()) {
			for (Metadata metadata : schemaType.getAllMetadatas().onlySequence()) {
				SequenceDataEntry dataEntry = (SequenceDataEntry) metadata.getDataEntry();
				if (dataEntry.getMetadataProvidingSequenceCode() != null) {
					MetadataSchema schema = types.getSchema(metadata.getSchemaCode());
					Metadata metadataProvidingSequence = schema.getMetadata(dataEntry.getMetadataProvidingSequenceCode());
					if (metadataProvidingSequence.getType() == REFERENCE) {
						if (record.isOfSchemaType(metadataProvidingSequence.getAllowedReferences().getTypeWithAllowedSchemas())) {
							typesWithSequenceFeedingRecord.add(new SchemaUtils().getSchemaTypeCode(schema.getCode()));
						}
					}
				}
			}
		}

		if (!typesWithSequenceFeedingRecord.isEmpty()) {
			AvailableSequence seq = new AvailableSequence(record.getId(),
					getAvailableSequenceLabelForTypes(types, typesWithSequenceFeedingRecord));
			availableSequences.add(seq);
		}
		return availableSequences;
	}

	private Map<Language, String> getAvailableSequenceLabelForTypes(MetadataSchemaTypes types,
			Set<String> typesWithSequenceFeedingRecord) {
		Map<Language, String> labels = new HashMap<>();
		for (Language language : types.getLanguages()) {
			StringBuilder stringBuilder = new StringBuilder();
			if (typesWithSequenceFeedingRecord.size() > 1) {
				stringBuilder.append($("AvailableSequencesServices.sequenceForTypes") + " ");
			} else {
				stringBuilder.append($("AvailableSequencesServices.sequenceForType") + " ");
			}
			List<String> typeLabels = new ArrayList<>();
			for (Iterator<String> i = typesWithSequenceFeedingRecord.iterator(); i.hasNext(); ) {
				String typeWithSequenceFeedingRecord = i.next();
				MetadataSchemaType type = types.getSchemaType(typeWithSequenceFeedingRecord);
				typeLabels.add(type.getLabel(language));
			}
			Collections.sort(typeLabels);
			stringBuilder.append(StringUtils.join(typeLabels, ", "));

			labels.put(language, stringBuilder.toString());
		}
		return labels;
	}

	private List<AvailableSequence> getAvailableSequencesUsingCollectionsSchemaTypes() {
		Language mainLanguage = withCode(appLayerFactory.getModelLayerFactory().getConfiguration().getMainDataLanguage());
		List<AvailableSequence> availableSequences = new ArrayList<>();
		KeySetMap<String, String> globalSequencesAndTypesUsingIt = new KeySetMap<>();

		for (String collection : collectionsListManager.getCollectionsExcludingSystem()) {
			MetadataSchemaTypes types = metadataSchemasManager.getSchemaTypes(collection);
			for (MetadataSchemaType type : types.getSchemaTypes()) {
				for (Metadata metadata : type.getAllMetadatas().onlySequence()) {
					SequenceDataEntry dataEntry = (SequenceDataEntry) metadata.getDataEntry();
					if (dataEntry.getFixedSequenceCode() != null) {

						globalSequencesAndTypesUsingIt.add(dataEntry.getFixedSequenceCode(), type.getLabel(mainLanguage));
					}
				}
			}
		}

		for (Map.Entry<String, Set<String>> entry : globalSequencesAndTypesUsingIt.getMapEntries()) {

			StringBuilder labelBuilder = new StringBuilder();
			if (entry.getValue().size() > 1) {
				labelBuilder.append($("AvailableSequencesServices.sequenceForTypes") + " ");
			} else {
				labelBuilder.append($("AvailableSequencesServices.sequenceForType") + " ");
			}

			List<String> typeLabels = new ArrayList<>(entry.getValue());

			Collections.sort(typeLabels);
			labelBuilder.append(StringUtils.join(typeLabels, ", "));

			Map<Language, String> languageStringMap = new HashMap<>();
			languageStringMap.put(mainLanguage, labelBuilder.toString());
			availableSequences.add(new AvailableSequence(entry.getKey(), languageStringMap));
		}

		return availableSequences;
	}

	private List<AvailableSequence> withoutDuplicates(List<AvailableSequence> sequences) {

		Set<String> codes = new HashSet<>();
		List<AvailableSequence> availableSequences = new ArrayList<>();

		for (AvailableSequence sequence : sequences) {
			if (!codes.contains(sequence.getCode())) {
				codes.add(sequence.getCode());
				availableSequences.add(sequence);
			}
		}

		return availableSequences;

	}
}
