package com.constellio.app.extensions.impl;

import com.constellio.app.extensions.api.scripts.ScriptWithLogOutput;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.services.contents.ContentDao;
import com.constellio.data.dao.services.contents.FileSystemContentDao;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.ContentVersion;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;

import java.util.*;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;

/**
 * Script that reports missing files by comparing differences between Constellio's physical files and ones registered in database.
 */
public class VaultVerificationScript extends ScriptWithLogOutput {

    private static final int BATCH_SIZE = 5000;

    private static final String ELEMENT_SEPARATOR = "\n\n\n\n";
    private static final String TITLE_LINE = "==========";
    private static final String MSG_NO_MISSING_ELEMENT = "No missing elements.";
    private static final String REPORT_HEADER = TITLE_LINE + "BEGIN REPORT" + TITLE_LINE;
    private static final String REPORT_FOOTER = TITLE_LINE + "END REPORT" + TITLE_LINE;

    private AppLayerFactory appLayerFactory;
    ContentDao vaultDao;
    List<String> allCollectionNames;

    Map<Record, Set<String>> currentVersionsWithMissingFile;
    Map<Record, Set<String>> historicalVersionsWithMissingFile;

    public VaultVerificationScript(AppLayerFactory appLayerFactory) {

        super(appLayerFactory, "Voûte", "Trouver les fichiers manquants", true);

        this.appLayerFactory = appLayerFactory;
        vaultDao = appLayerFactory.getModelLayerFactory().getDataLayerFactory().getContentsDao();

        currentVersionsWithMissingFile = new TreeMap<>(
                new Comparator<Record>() {
                    @Override
                    public int compare(Record o1, Record o2) {
                        return o1.getId().compareTo(o2.getId());
                    }
                }
        );
        historicalVersionsWithMissingFile = new TreeMap<>(
                new Comparator<Record>() {
                    @Override
                    public int compare(Record o1, Record o2) {
                        return o1.getId().compareTo(o2.getId());
                    }
                }
        );
        allCollectionNames = modelLayerFactory.getCollectionsListManager().getCollectionsExcludingSystem();
    }

    @Override
    protected void execute() {
        generateReport();
        outputLogger.appendToFileWithoutLogging(getReport());
    }

    private void generateReport() {

        for (String collectionName : allCollectionNames) {

            LogicalSearchQuery query = new LogicalSearchQuery().setCondition(fromAllSchemasIn(collectionName).returnAll());
            Iterator<Record> records = appLayerFactory.getModelLayerFactory().newSearchServices().recordsIterator(query, BATCH_SIZE);

            while (records.hasNext()) {

                Record record = records.next();
                MetadataSchema metadataSchema = modelLayerFactory.getMetadataSchemasManager().getSchemaOf(record);
                List<Metadata> metadataList = metadataSchema.getMetadatas(); // it is possible that a record has multiple content metadatas

                for (Metadata metadata : metadataList) {
                    if (metadata.getType() == MetadataValueType.CONTENT) {
                        for (Content content : record.<Content>getValues(metadata)) { // handles multiple values per content
                            // current
                            verifyIfExistsAndLog(record, content.getCurrentVersion().getHash(), currentVersionsWithMissingFile);

                            // historical
                            List<ContentVersion> historicalFileHashes = content.getHistoryVersions();
                            for (ContentVersion contentVersion : historicalFileHashes) {
                                verifyIfExistsAndLog(record, contentVersion.getHash(), historicalVersionsWithMissingFile);
                            }
                        }
                    }
                }
            }
        }
    }

    private void verifyIfExistsAndLog(Record record, String fileHash, Map<Record, Set<String>> recordsWithMissingFiles) {

        if (!vaultDao.isDocumentExisting(fileHash)) {

            String fileHashInfos = fileHash + " located in "+((FileSystemContentDao)vaultDao).getFileOf(fileHash).getAbsolutePath();

            if (recordsWithMissingFiles.containsKey(record)) {
                recordsWithMissingFiles.get(record).add(fileHashInfos);
            } else {
                recordsWithMissingFiles.put(record, new HashSet<String>());
                recordsWithMissingFiles.get(record).add(fileHashInfos);
            }
        }
    }

    private String getReport() {

        String report = "";

        report += "\n" + REPORT_HEADER + ELEMENT_SEPARATOR;

        report += "\nCurrent documents with missing files :\n" + getMissingFileReport(currentVersionsWithMissingFile);
        report += ELEMENT_SEPARATOR + "Historical documents with missing files :\n" + getMissingFileReport(historicalVersionsWithMissingFile);

        report += ELEMENT_SEPARATOR + REPORT_FOOTER + "\n\n";

        return report;
    }

    private String getMissingFileReport(Map<Record, Set<String>> missingFiles) {
        String report = "";

        if (!missingFiles.isEmpty()) {

            for (Map.Entry<Record, Set<String>> entry : missingFiles.entrySet()) {

                Record record = entry.getKey();
                Set<String> hashes = entry.getValue();

                report += "\nid : " + record.getId() + ", title : " + record.getTitle() + ", file hash(es) : ";

                for (String hash : hashes) {
                    report += hash + "; ";
                }
            }
        } else {
            report += MSG_NO_MISSING_ELEMENT;
        }

        return report;
    }
}
