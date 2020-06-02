package com.constellio.model.services.records.utils;

import com.constellio.data.dao.dto.records.RecordDTO;
import com.constellio.data.dao.dto.records.TransactionDTO;
import com.constellio.data.dao.services.bigVault.BigVaultRecordDao;
import com.constellio.data.dao.services.bigVault.solr.BigVaultServerTransaction;
import com.constellio.data.dao.services.transactionLog.writer1.TransactionWriterV1;
import com.constellio.data.extensions.DataLayerSystemExtensions;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.FieldsPopulator;
import com.constellio.model.services.records.RecordImpl;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import org.apache.commons.io.IOUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.constellio.data.dao.dto.records.RecordsFlushing.NOW;

public class SavestateFileWriter {

	TransactionWriterV1 transactionWriter = new TransactionWriterV1(false, new DataLayerSystemExtensions());
	BufferedWriter writer;
	List<FieldsPopulator> populators = new ArrayList<>();
	MetadataSchemasManager schemasManager;
	BigVaultRecordDao recordDao;

	public SavestateFileWriter(ModelLayerFactory modelLayerFactory, File file) {
		this.schemasManager = modelLayerFactory.getMetadataSchemasManager();
		this.recordDao = (BigVaultRecordDao) modelLayerFactory.getDataLayerFactory().newRecordDao();
		try {
			writer = new BufferedWriter(new FileWriter(file));

		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public List<FieldsPopulator> getPopulators() {
		return populators;
	}

	public SavestateFileWriter setPopulators(
			List<FieldsPopulator> populators) {
		this.populators = populators;
		return this;
	}

	public void write(List<Record> records) {

		List<RecordDTO> recordDTOs = new ArrayList<>();

		for (Record record : records) {
			MetadataSchema schema = schemasManager.getSchemaTypes(record.getCollection()).getSchema(record.getSchemaCode());
			recordDTOs.add(((RecordImpl) record).toDocumentDTO(schema, populators));
		}

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
