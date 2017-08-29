package com.constellio.app.modules.rm.services.sip;

import com.constellio.app.modules.rm.RMTestRecords;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.services.sip.data.intelligid.ConstellioSIPObjectsProvider;
import com.constellio.app.modules.rm.services.sip.filter.SIPFilter;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.data.io.services.facades.IOServices;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.records.RecordServicesRuntimeException;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.sdk.tests.ConstellioTest;
import org.apache.commons.io.IOUtils;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.assertj.core.api.Assertions.assertThat;

public class SIPArchivesAcceptanceTest extends ConstellioTest {
    RMTestRecords records = new RMTestRecords(zeCollection);
    RMSchemasRecordsServices rm;
    File outFile;
    IOServices ioServices;
    @Before
    public void setUp() throws Exception {

        prepareSystem(
                withZeCollection().withConstellioRMModule().withRMTest(records)
                        .withFoldersAndContainersOfEveryStatus()
        );

        InputStream bagInfoIn = new FileInputStream(getTestResourceFile("baginfo.txt"));
        List<String> bagInfoLines = IOUtils.readLines(bagInfoIn);
        ioServices = getModelLayerFactory().getIOServicesFactory().newIOServices();
        outFile = ioServices.newTemporaryFile("temporarySIPFile.zip");
        bagInfoIn.close();
        rm = new RMSchemasRecordsServices(zeCollection, getAppLayerFactory());

        SIPFilter filter = new SIPFilter(zeCollection, getAppLayerFactory()).withIncludeFolderIds(Collections.singletonList(records.getFolder_A01().getId()));
        ConstellioSIPObjectsProvider metsObjectsProvider = new ConstellioSIPObjectsProvider(zeCollection, getAppLayerFactory(), filter);
        if (!metsObjectsProvider.list().isEmpty()) {
            ConstellioSIP constellioSIP = new ConstellioSIP(metsObjectsProvider, bagInfoLines, false, getAppLayerFactory().newApplicationService().getWarVersion());
            constellioSIP.build(outFile);
        }

    }

    @Test
    public void checkIfZipContainsTheCorrectFoldersForCategoryTest() throws Exception {
        FileInputStream fis = new FileInputStream(outFile);
        ZipInputStream zis = new ZipInputStream(fis);
        Stack<String> categoryStack = new Stack<>();
        Category category = rm.getCategory(records.getFolder_A01().getCategory());
        while(category != null) {
            try{
                categoryStack.push(category.getCode());
                category = rm.getCategory(category.getParent());
            }catch (RecordServicesRuntimeException.NoSuchRecordWithId e) {
                category = null;
            }
        }
        categoryStack.push(records.getFolder_A01().getId());
        ZipEntry ze;
        ze = zis.getNextEntry();
        while (ze != null) {
            String[] path = ze.getName().split("/");
            for(String currentPath : path) {
                //Check if it's the data folder or the file itself
                if(!"data".equals(currentPath) && !currentPath.matches("([A-Za-z0-9\\-])+(\\.)(\\w+)")){
                    assertThat(currentPath).isIn(categoryStack);
                }
            }
            ze = zis.getNextEntry();
        }
        ioServices.closeQuietly(fis);
        ioServices.closeQuietly(zis);
        ioServices.deleteQuietly(outFile);
    }

    @Test
    public void checkForRequiredMetadataInTheXml() throws Exception{
        FileInputStream fis = new FileInputStream(outFile);
        nonConventionalClosingZipInputStream zis = new nonConventionalClosingZipInputStream(fis);
        ZipEntry ze;
        ze = zis.getNextEntry();
        SAXBuilder builder = new SAXBuilder();
        while (ze != null) {
            List<String> documentIds = this.getDocumentForFolder(records.getFolder_A01().getId());
            //Check if it's the actual xml file.
            if(ze.getName().matches("([A-Za-z0-9\\-\\/\\\\\\(\\)\\ ]+)(\\.xml)") && ze.getName().contains("/")) {
                Document doc = builder.build(zis);
                Element rootElement = (Element) doc.getContent().get(0);
                Element eadheaderElement = rootElement.getChild("eadheader", Namespace.getNamespace("urn:isbn:1-931666-22-9"));
                Element eadid = eadheaderElement.getChild("eadid", Namespace.getNamespace("urn:isbn:1-931666-22-9"));
                assertThat(eadid.getText()).isIn(documentIds);
            }
            ze = zis.getNextEntry();
        }
        ioServices.closeQuietly(zis);
        ioServices.deleteQuietly(outFile);
        zis.doClose();
    }

    private List<String> getDocumentForFolder(String folderId) {
        SearchServices searchServices = getModelLayerFactory().newSearchServices();
        MetadataSchemasManager metadataSchemasManager = getModelLayerFactory().getMetadataSchemasManager();
        MetadataSchemaType type = metadataSchemasManager.getSchemaTypes(zeCollection).getSchemaType(com.constellio.app.modules.rm.wrappers.Document.SCHEMA_TYPE);
        LogicalSearchCondition condition = LogicalSearchQueryOperators.from(type).where(type.getDefaultSchema().getMetadata(com.constellio.app.modules.rm.wrappers.Document.FOLDER)).isEqualTo(folderId);
        List<com.constellio.app.modules.rm.wrappers.Document> documents = rm.wrapDocuments(searchServices.search(new LogicalSearchQuery(condition)));
        List<String> documentsIds = new ArrayList<>();
        for(com.constellio.app.modules.rm.wrappers.Document document : documents) {
            documentsIds.add(document.getId());
        }
        Folder parentFolder = rm.getFolder(folderId);
        while(parentFolder != null) {
            documentsIds.add(parentFolder.getId());
            try{
                parentFolder = rm.getFolder(parentFolder.getParentFolder());
            }catch (RecordServicesRuntimeException.NoSuchRecordWithId e) {
                parentFolder = null;
            }
        }
        return documentsIds;
    }

    private class nonConventionalClosingZipInputStream extends ZipInputStream {

        public nonConventionalClosingZipInputStream(InputStream in) {
            super(in);
        }

        @Override
        public void close() throws IOException {
            //nothing
        }

        public void doClose() throws IOException{
            super.close();
        }

    }
}
