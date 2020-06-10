package com.constellio.model.services.records.utils;

import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.dao.dto.records.TransactionDTO;
import com.constellio.data.dao.services.bigVault.BigVaultRecordDao;
import com.constellio.data.dao.services.bigVault.solr.BigVaultServerTransaction;
import com.constellio.data.dao.services.transactionLog.writer1.TransactionWriterV1;
import com.constellio.data.extensions.DataLayerSystemExtensions;
import com.constellio.model.entities.CollectionInfo;
import com.constellio.model.entities.records.ParsedContent;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.contents.ParsedContentProvider;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.FieldsPopulator;
import com.constellio.model.services.records.RecordImpl;
import com.constellio.model.services.records.RecordProvider;
import com.constellio.model.services.records.populators.SearchFieldsPopulator;
import com.constellio.model.services.records.populators.SortFieldsPopulator;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import org.apache.commons.io.IOUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.constellio.data.dao.dto.records.RecordsFlushing.NOW;

public class SavestateFileWriter {

	TransactionWriterV1 transactionWriter = new TransactionWriterV1(false, new DataLayerSystemExtensions());
	BufferedWriter writer;
	MetadataSchemasManager schemasManager;
	BigVaultRecordDao recordDao;
	ModelLayerFactory modelLayerFactory;

	boolean includeParsedContent = false;


	public SavestateFileWriter(ModelLayerFactory modelLayerFactory, File file, boolean includeParsedContent) {
		this.schemasManager = modelLayerFactory.getMetadataSchemasManager();
		this.recordDao = (BigVaultRecordDao) modelLayerFactory.getDataLayerFactory().newRecordDao();
		this.includeParsedContent = includeParsedContent;
		try {
			writer = new BufferedWriter(new FileWriter(file));

		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		this.modelLayerFactory = modelLayerFactory;
	}

	private List<FieldsPopulator> newFieldPopulatorsFor(Record record) {


		ParsedContentProvider parsedContentProvider;

		if (includeParsedContent) {
			parsedContentProvider = new ParsedContentProvider(modelLayerFactory.getContentManager());
		} else {
			parsedContentProvider = new ParsedContentProvider(modelLayerFactory.getContentManager()) {
				@Override
				public ParsedContent getParsedContentIfAlreadyParsed(String hash) {
					ParsedContent parsedContent = super.getParsedContentIfAlreadyParsed(hash);
					if (parsedContent != null) {
						new ParsedContent("", parsedContent.getLanguage(), parsedContent.getMimeType(),
								parsedContent.getLength(), new HashMap<>(), new HashMap<>());
					}
					return parsedContent;
				}

				@Override
				public ParsedContent getParsedContentParsingIfNotYetDone(String hash) {
					ParsedContent parsedContent = super.getParsedContentParsingIfNotYetDone(hash);
					if (parsedContent != null) {
						return new ParsedContent("", parsedContent.getLanguage(), parsedContent.getMimeType(),
								parsedContent.getLength(), new HashMap<>(), new HashMap<>());
					}
					return parsedContent;
				}
			};
		}

		CollectionInfo collectionInfo = record.getCollectionInfo();
		MetadataSchemaTypes types = schemasManager.getSchemaTypes(collectionInfo.getCollectionId());

		List<FieldsPopulator> populators = new ArrayList<>();

		populators.add(new SearchFieldsPopulator(types, true, parsedContentProvider, collectionInfo,
				modelLayerFactory.getSystemConfigs(), modelLayerFactory.getExtensions()));

		populators.add(new SortFieldsPopulator(types, true, modelLayerFactory,
				new RecordProvider(modelLayerFactory.newRecordServices())));

		return populators;
	}

	public void write(List<Record> records) {

		List<RecordDTO> recordDTOs = new ArrayList<>();

		for (Record record : records) {
			MetadataSchema schema = schemasManager.getSchemaTypes(record.getCollection()).getSchema(record.getSchemaCode());
			//recordDTOs.add(record.getRecordDTO());
			recordDTOs.add(((RecordImpl) record).toDocumentDTO(schema, newFieldPopulatorsFor(record)));
		}

		writeDTOs(recordDTOs);

	}

	public void writeDTOs(List<RecordDTO> recordDTOs) {
		BigVaultServerTransaction bigVaultServerTransaction = recordDao.prepare(new TransactionDTO(NOW())
				.withFullRewrite(true).withNewRecords(recordDTOs));

		try {
			writer.append(transactionWriter.toLogEntry(bigVaultServerTransaction));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void close() {
		IOUtils.closeQuietly(writer);
	}
}
