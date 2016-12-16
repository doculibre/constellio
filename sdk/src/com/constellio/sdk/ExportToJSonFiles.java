package com.constellio.sdk;

import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.entities.ContentVersionVO;
import com.constellio.app.ui.framework.builders.ContentVersionToVOBuilder;
import com.constellio.app.ui.framework.components.converters.RecordIdToCaptionConverter;
import com.constellio.model.entities.records.Content;
import com.constellio.model.entities.records.ContentVersion;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataSchemasRuntimeException;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.extractions.RecordPopulateServices;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SPEQueryResponse;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.users.UserServices;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;

/**
 * Created by Majid on 2016-07-07.
 */
public class ExportToJSonFiles {
    final int BATCH_SIZE = 1000;

    private final AppLayerFactory appLayerFactory;
    private final ModelLayerFactory modelLayerFactory;
    private final UserServices userServices;
    private final RecordIdToCaptionConverter recordCaptionConverter = new RecordIdToCaptionConverter();;
    private RMSchemasRecordsServices rmSchemasRecordsServices ;

    private final Gson gson;
    private final File contentDir;

    //stats
    private int nullContentDocCnt;
    private int multiVersionDocs;
    private int totalDocs;
    private int fileNotFound;


    private List<Map<String, Object>> docsMetadatas = new ArrayList<>();

    private static Logger LOGGER = LoggerFactory.getLogger(ExportToJSonFiles.class);

    public ExportToJSonFiles(File contentDir) {
        RecordPopulateServices.LOG_CONTENT_MISSING = true;
        appLayerFactory = SDKScriptUtils.startApplicationWithBatchProcesses();
        modelLayerFactory = appLayerFactory.getModelLayerFactory();
        userServices = new UserServices(appLayerFactory.getModelLayerFactory());
        gson = new GsonBuilder().setPrettyPrinting().create();
        this.contentDir = contentDir;
    }

    public void exportTo(File outputDir, boolean toMove) throws FileNotFoundException {
        if (!outputDir.exists())
            outputDir.mkdir();

        nullContentDocCnt = 0;
        multiVersionDocs = 0;
        totalDocs = 0;
        fileNotFound = 0;
        docsMetadatas.clear();

        for (String collection: modelLayerFactory.getCollectionsListManager().getCollections()) {
            System.out.println(String.format("Exporting document of the collection '%s'", collection));

            final List<User> allUsersInCollection = userServices.getAllUsersInCollection(collection);
            if (allUsersInCollection.size() == 0) {
                System.out.println(String.format("Ignore the collection '%s'. No user has been found in the collection.", collection));
                continue;
            }

            rmSchemasRecordsServices = new RMSchemasRecordsServices(collection, appLayerFactory);
            MetadataSchemaType documentSchemaType;
            try {
                documentSchemaType = rmSchemasRecordsServices.documentSchemaType();
            } catch (MetadataSchemasRuntimeException e) {
                System.out.println(String.format("Ignore the collection '%s'. The Document schema has not been found.", collection));
                continue;
            }

            final SearchServices searchServices = modelLayerFactory.newSearchServices();

            MetadataSchemasManager metadataSchemasManager = modelLayerFactory.getMetadataSchemasManager();
            MetadataSchemaTypes types = metadataSchemasManager.getSchemaTypes(collection);
//            final SessionContext sessionContext = FakeSessionContext.forRealUserIncollection(allUsersInCollection.get(0));

            LogicalSearchQuery query = new LogicalSearchQuery(from(documentSchemaType).returnAll());
            exportDocuments(outputDir, searchServices, types, query);
        }

        System.out.println(String.format("Total Documents=%d\tDocuments with multiple versions=%d" +
                "\tDocument with null content=%d\tFile not found=%d", totalDocs, multiVersionDocs, nullContentDocCnt, fileNotFound));

        File multiVersionDocsInfoFile = new File(outputDir, "multiVersionDocs.json");
        PrintStream multiVersionDocsInfoStream = new PrintStream(new BufferedOutputStream(new FileOutputStream(multiVersionDocsInfoFile)));
        gson.toJson(docsMetadatas, multiVersionDocsInfoStream);
        multiVersionDocsInfoStream.close();

        File statFile = new File(outputDir, "stat.json");
        Map<String, Integer> stat = new TreeMap<>();
        stat.put("Total docs", totalDocs);
        stat.put("Multi version docs", multiVersionDocs);
        stat.put("Null content docs", nullContentDocCnt);
        stat.put("Not found docs", fileNotFound);
        PrintStream statStream = new PrintStream(new BufferedOutputStream(new FileOutputStream(statFile)));
        gson.toJson(stat, statStream);
        statStream.close();
    }

    protected void exportDocuments(final File outputDir, final SearchServices searchServices,
                                   final MetadataSchemaTypes types, final LogicalSearchQuery query) throws FileNotFoundException {
//        final DocumentToVOBuilder builder = new DocumentToVOBuilder(appLayerFactory.getModelLayerFactory());

        query.setStartRow(0);
        query.setNumberOfRows(0);
        final SPEQueryResponse response = searchServices.query(query);
        final long numFound = response.getNumFound();

        query.setNumberOfRows(BATCH_SIZE);

        for (int start = 0; start < numFound; start += BATCH_SIZE) {
            System.out.println(String.format("%d / %d", start, numFound));
            query.setStartRow(start);
            final List<Record> documents = searchServices.search(query);
            int idx = totalDocs + start;
            for (Record record : documents) {

//                final DocumentVO documentVO = builder.build(record, RecordVO.VIEW_MODE.DISPLAY, sessionContext);
                File documentDir = new File(outputDir, "" + idx);
                documentDir.mkdir();
                final Map<String, Object> docMetadata = extractMetatdatas(types, record, documentDir);
                if (docMetadata != null) {
                    docMetadata.put("dir", "" + idx);
                    File outputFile = new File(documentDir, "metadata.json");
                    PrintStream output = new PrintStream(new BufferedOutputStream(new FileOutputStream(outputFile)));
                    gson.toJson(docMetadata, output);
                    output.close();

                    if (((List<Map<String, String>>) docMetadata.get("versions")).size() > 1)
                        docsMetadatas.add(docMetadata);
                    ++idx;
                } else {
                    documentDir.delete();
                }
            }
            totalDocs = idx;
        }

    }

    private Map<String, Object> extractMetatdatas(MetadataSchemaTypes types, Record record, File documentDir) {
        Document document = new Document(record, types);
        Content content = document.getContent();
        if (content != null) {
            Map<String, Object> documentMetadatas = extractMetadatas(document, documentDir);
            return documentMetadatas;
        } else {
            String type = "null";
            if (document.getType() != null)
                type = rmSchemasRecordsServices.getDocumentType(document.getType()).getTitle();
            ++nullContentDocCnt;
            System.err.println(String.format("%d- Document with no content! Document type = '%s'", nullContentDocCnt, type));
            return null;
        }

    }

    private Map<String, Object> extractMetadatas(Document document, File documentDir) {
        Map<String, Object> documentMetadatas = new TreeMap<>();
        documentMetadatas.put("id", document.getId());
        documentMetadatas.put("title", document.getTitle());
        documentMetadatas.put("author", document.getAuthor());
        final String folderID = document.getFolder();
        documentMetadatas.put("folder", rmSchemasRecordsServices.getFolder(folderID).getTitle());
        documentMetadatas.put("folderId", document.getFolder());

        List<Map<String, String>> versions = new ArrayList<>();
        int versionIdx = 0;
        versions.add(getContentMetadata(document.getContent(), document.getContent().getCurrentVersion(),
                new File(documentDir, "" + versionIdx), versionIdx));

        for (ContentVersion version : document.getContent().getHistoryVersions()) {
            versionIdx++;
            versions.add(getContentMetadata(document.getContent(), version,
                    new File(documentDir, "" + versionIdx), versionIdx));
        }

        documentMetadatas.put("versions", versions);
        if (versions.size() > 1)
            multiVersionDocs++;

        return documentMetadatas;
    }

    private Map<String, String> getContentMetadata(Content content, ContentVersion contentVersion, File versionFile, int versionIdx){
        ContentVersionToVOBuilder builder = new ContentVersionToVOBuilder(modelLayerFactory);
        ContentVersionVO contentVersionVO = builder.build(content, contentVersion);

        Map<String, String> contentMetadata = new TreeMap<>();
        contentMetadata.put("fileIndex", "" + versionIdx);
        contentMetadata.put("fileName", contentVersionVO.getFileName());
        contentMetadata.put("mimeType", contentVersionVO.getMimeType());
        contentMetadata.put("id", contentVersionVO.getHash());
        contentMetadata.put("modificationBy", contentVersionVO.getLastModifiedBy());
        contentMetadata.put("modificationByCaption", recordCaptionConverter.convertToPresentation(
                contentVersionVO.getLastModifiedBy(), String.class, Locale.ENGLISH));

        contentMetadata.put("lastModificationDate", contentVersionVO.getLastModificationDateTime().toString());

        archive(versionFile, contentVersionVO);

        return contentMetadata;
    }

    private void archive(File versionFile, ContentVersionVO contentVersionVO) {
        final File toMove = getFileOf(contentVersionVO.getHash());
        if (toMove.exists()) {
            toMove.renameTo(versionFile);
            for (String ext: new String[]{"__parsed", ".preview"}){
                new File(toMove.getAbsolutePath() + ext).renameTo(new File(versionFile.getAbsolutePath() + ext));
            }
        } else {
            ++fileNotFound;
            System.err.println(String.format("%d- File <%s> is not found.", fileNotFound, toMove.getAbsolutePath()));
        }
    }


    private File getFileOf(String contentId) {
        if (contentId.contains("/")) {
            return new File(contentDir, contentId.replace("/", File.separator));

        } else {
            String folderName = contentId.substring(0, 2);
            File folder = new File(contentDir, folderName);
            return new File(folder, contentId);
        }

    }


    public static void main(String[] args) throws FileNotFoundException {
        final File zeFolder = new File("/Volumes/encrypted/constellio/zeFolder");
        final File contentDir = new File(zeFolder, "contents");
        final File outputDir = new File(zeFolder, "json");

        final ExportToJSonFiles exportToJSonFiles = new ExportToJSonFiles(contentDir);

        exportToJSonFiles.exportTo(outputDir, true);
    }

}
