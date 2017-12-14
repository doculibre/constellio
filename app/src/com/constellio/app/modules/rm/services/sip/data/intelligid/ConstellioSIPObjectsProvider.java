package com.constellio.app.modules.rm.services.sip.data.intelligid;

import com.constellio.app.entities.modules.ProgressInfo;
import com.constellio.app.modules.rm.model.CopyRetentionRule;
import com.constellio.app.modules.rm.services.sip.data.SIPObjectsProvider;
import com.constellio.app.modules.rm.services.sip.ead.EADArchdesc;
import com.constellio.app.modules.rm.services.sip.filter.SIPFilter;
import com.constellio.app.modules.rm.services.sip.model.SIPObject;
import com.constellio.app.modules.rm.services.sip.model.EntityRetriever;
import com.constellio.app.modules.rm.services.sip.model.SIPDocument;
import com.constellio.app.modules.rm.services.sip.model.SIPFolder;
import com.constellio.app.modules.rm.services.RMSchemasRecordsServices;
import com.constellio.app.modules.rm.wrappers.*;
import com.constellio.app.modules.rm.wrappers.type.DocumentType;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.data.dao.services.bigVault.SearchResponseIterator;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.schemas.MetadataListFilter;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.LocalDate;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

public class ConstellioSIPObjectsProvider implements SIPObjectsProvider {
    public static final String JOINT_FILES_KEY = "attachments";

    private List<Metadata> folderMetadatas;

    private List<Metadata> documentMetadatas;

    private String categoryId;

    private String administrativeUnitId;

    private List<Document> documents;

    private String collection;

    private AppLayerFactory factory;

    private RMSchemasRecordsServices rm;

    private SIPFilter filter;

    private MetadataSchemaTypes types;

    private int currentProgress;

    private ProgressInfo progressInfo;

    public ConstellioSIPObjectsProvider(String collection, AppLayerFactory factory, SIPFilter filter, ProgressInfo progressInfo) {
        this.collection = collection;
        this.factory = factory;
        this.filter = filter;
        this.rm = new RMSchemasRecordsServices(collection, factory);
        this.progressInfo = progressInfo;
        this.currentProgress = 0;
        init();
    }

    @Override
    public int getStartIndex() {
        return filter.getStartIndex();
    }

    @SuppressWarnings("unchecked")
    private void init() {
        if (filter.getAdministrativeUnit() != null) {
            administrativeUnitId = this.filter.getAdministrativeUnit().getId();
        }
        if (filter.getRubriqueCode() != null) {
            categoryId = filter.getRubriqueCode().getId();
        }

        System.out.println("Obtention de la liste des documents");
        documents = filter.getDocument();
        this.progressInfo.setEnd(documents.size());
        System.out.println("Liste de documents obtenue (" + documents.size() + ")");
    }

    @Override
    public List<String> getMetadataIds(SIPObject sipObject) {
        List<String> metadataIds = new ArrayList<>();
        if (sipObject instanceof SIPFolder) {
            metadataIds.add("numeroRegleConservation");
            metadataIds.add("regleConservation");
        } else if (sipObject instanceof SIPDocument) {
            SIPDocument sipDocument = (SIPDocument) sipObject;
            Document document = rm.wrapDocument(sipDocument.getFicheMetadonnees());
            boolean isCourriel = document.getSchema().getCode().equals(Email.SCHEMA);
            if (isCourriel) {
                metadataIds.add(Email.EMAIL_TO);
                metadataIds.add(Email.EMAIL_FROM);
                metadataIds.add(Email.EMAIL_IN_NAME_OF);
                metadataIds.add(Email.EMAIL_CC_TO);
                metadataIds.add(Email.EMAIL_BCC_TO);
                metadataIds.add(Email.EMAIL_OBJECT);
            } else {
                metadataIds.add("typeDocument");
            }
        }
        return metadataIds;
    }

    @Override
    public List<String> getMetadataValues(SIPObject sipObject, String metadataId) {
        List<String> metadataValues = new ArrayList<>();
        if (sipObject instanceof SIPFolder) {
            SIPFolder sipFolder = (SIPFolder) sipObject;
            Folder folder = rm.wrapFolder(sipFolder.getFicheMetadonnees());
            Folder currentFolder = folder;
            while (currentFolder.getParentFolder() != null) {
                currentFolder = rm.getFolder(currentFolder.getParentFolder());
            }
            RetentionRule retentionRule = rm.getRetentionRule(currentFolder.getRetentionRule());
            CopyRetentionRule copyRetentionRule = retentionRule.getCopyRetentionRules().get(0);
            if ("numeroRegleConservation".equals(metadataId)) {
                metadataValues.add(copyRetentionRule.getCode());
            } else if ("regleConservation".equals(metadataId)) {
                metadataValues.add(copyRetentionRule.getTitle());
            }
        } else if (sipObject instanceof SIPDocument) {
            SIPDocument sipDocument = (SIPDocument) sipObject;
            Document document = rm.wrapDocument(sipDocument.getFicheMetadonnees());
            boolean isEmail = document.getSchema().getCode().equals(Email.SCHEMA);
            if (isEmail) {
                String metadataName;
                if ("emailTo".equals(metadataId)) {
                    metadataName = Email.EMAIL_TO;
                } else if ("emailFrom".equals(metadataId)) {
                    metadataName = Email.EMAIL_FROM;
                } else if ("emailInNameOf".equals(metadataId)) {
                    metadataName = Email.EMAIL_IN_NAME_OF;
                } else if ("emailCCTo".equals(metadataId)) {
                    metadataName = Email.EMAIL_CC_TO;
                } else if ("emailBCCTo".equals(metadataId)) {
                    metadataName = Email.EMAIL_BCC_TO;
                } else if ("emailObject".equals(metadataId)) {
                    metadataName = Email.EMAIL_OBJECT;
                } else {
                    metadataName = null;
                }
                if (metadataName != null) {
                    Object metadataValue = document.get(metadataName);
                    if (metadataValue != null) {
                        String metadataValueAsString = metadataValue instanceof List ? String.join(", ", (List<String>) metadataValue) : metadataValue.toString();
                        if (StringUtils.isNotBlank(metadataValueAsString)) {
                            metadataValues.add(metadataValueAsString);
                        }
                    }
                }
            } else {
                if ("typeDocument".equals(metadataId)) {
                    DocumentType typeDocument = rm.getDocumentType(document.<String>get(Document.TYPE));
                    if (typeDocument != null) {
                        metadataValues.add(typeDocument.getCode());
                    }
                }
            }
        }
        return metadataValues;
    }

    @Override
    public List<SIPObject> list() {
        return new AbstractList<SIPObject>() {
            @Override
            public SIPObject get(int index) {
                System.out.println("Document " + (index + 1) + " de " + documents.size());
                Document document = documents.get(index);
//                progressInfo.setCurrentState(++currentProgress);
//                progressInfo.setProgressMessage("Document " + (index + 1) + " de " + documents.size());
                return new SIPDocument(document, document.getSchema().getMetadatas(), new EntityRetriever(collection, factory));
            }

            @Override
            public int size() {
                return documents.size();
            }
        };
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, byte[]> getExtraFiles(SIPObject sipObject) {
        Map<String, byte[]> result;
        if (sipObject instanceof SIPDocument) {
            SIPDocument sipDocument = (SIPDocument) sipObject;
            Document document = rm.wrapDocument(sipDocument.getFicheMetadonnees());
            boolean isEmail = document.getSchema().getCode().equals(Email.SCHEMA);
            if (isEmail && document.getContent() != null) {
                File sipDocumentFile = sipDocument.getFile();
                String filename = sipDocument.getFilename();
                Map<String, Object> parsedMessage;
                try {
                    if (sipDocumentFile != null) {
                        InputStream in = new FileInputStream(sipDocumentFile);
                        parsedMessage = rm.parseEmail(filename, in);
                        if (parsedMessage != null) {
                            result = new LinkedHashMap<>();
                            Map<String, InputStream> streamMap = (Map<String, InputStream>) parsedMessage.get(JOINT_FILES_KEY);
                            for (Entry<String, InputStream> entry : streamMap.entrySet()) {
                                InputStream fichierJointIn = entry.getValue();
                                byte[] joinFilesBytes = IOUtils.toByteArray(fichierJointIn);
                                result.put(entry.getKey(), joinFilesBytes);
                            }
                        } else {
                            result = null;
                        }
                    } else {
                        result = null;
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                    result = null;
                }
            } else {
                result = null;
            }
        } else {
            result = null;
        }
        return result;
    }

    @Override
    public String getCollection() {
        return this.collection;
    }

    @Override
    public AppLayerFactory getAppLayerCollection() {
        return this.factory;
    }

    private String formatDate(LocalDate date) {
        return date != null ? new SimpleDateFormat("yyyyMMdd").format(date.toDate()) : null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public EADArchdesc getEADArchdesc(SIPObject sipObject) {
        EADArchdesc archdesc;
        if (sipObject instanceof SIPDocument) {
            SIPDocument sipDocument = (SIPDocument) sipObject;
            Document document = rm.wrapDocument(sipDocument.getFicheMetadonnees());
            Folder folder = rm.getFolder(document.getFolder());

            archdesc = new EADArchdesc();

            String creationDate = formatDate(document.getCreatedOn().toLocalDate());
            if (creationDate != null) {
                archdesc.getDidUnitDates().put("creation", creationDate);
            }

            String publishDate = formatDate(document.getFolderActualDepositDate());
            if (publishDate != null) {
                archdesc.getDidUnitDates().put("publication", publishDate);
            }

            String summary = document.getDescription();
            if (StringUtils.isNotBlank(summary)) {
                archdesc.getDidAbstracts().add(summary);
            }

            List<String> keywords = document.getKeywords();
            if (keywords != null) {
                for (String keyword : keywords) {
                    archdesc.getControlAccessSubjects().add(keyword);
                }
            }

        } else if (sipObject instanceof SIPFolder) {
            SIPFolder sipFolder = (SIPFolder) sipObject;
            Folder folder = rm.wrapFolder(sipFolder.getFicheMetadonnees());

            archdesc = new EADArchdesc();

            Category processusActivite = rm.getCategory(folder.getCategory());
            archdesc.getFileplanPs().add(processusActivite.getTitle());

            String openingDate = formatDate(folder.getOpenDate());
            if (openingDate != null) {
                archdesc.getDidUnitDates().put("creation", openingDate);
            }

            String closingDate = formatDate(folder.getCloseDate());
            if (closingDate != null) {
                archdesc.getDidUnitDates().put("closure", closingDate);
            }

            String summary = folder.getDescription();
            if (StringUtils.isNotBlank(summary)) {
                archdesc.getDidAbstracts().add(summary);
            }

            AdministrativeUnit administrativeUnit = rm.getAdministrativeUnit(folder.<String>get(Folder.ADMINISTRATIVE_UNIT));
            String adminstrativeUnitParentId = administrativeUnit.get(AdministrativeUnit.PARENT);
            if (adminstrativeUnitParentId != null) {
                AdministrativeUnit parentAdministrativeUnit = rm.getAdministrativeUnit(adminstrativeUnitParentId);
                archdesc.setDidOriginationCorpname(parentAdministrativeUnit.getCode());
            }

            MetadataSchemasManager manager = factory.getModelLayerFactory().getMetadataSchemasManager();
            MetadataSchemaType documentSchemaType = manager.getSchemaTypes(collection).getSchemaType(Document.SCHEMA_TYPE);
            LogicalSearchCondition conditionDocument = LogicalSearchQueryOperators.from(documentSchemaType).where(documentSchemaType.getDefaultSchema().getMetadata(Document.FOLDER)).isEqualTo(folder.getId());
            SearchResponseIterator<Record> iteratorDocument = factory.getModelLayerFactory().newSearchServices().recordsIterator(new LogicalSearchQuery(conditionDocument));
            if (iteratorDocument != null) {
                while (iteratorDocument.hasNext()) {
                    Document documentLie = rm.wrapDocument(iteratorDocument.next());
                    List<String> relatedmaterialList = new ArrayList<>();
                    relatedmaterialList.add(documentLie.getId() + " " + documentLie.getTitle());
                    archdesc.getRelatedmaterialLists().add(relatedmaterialList);
                }
            }

            MetadataSchemaType folderSchemaType = manager.getSchemaTypes(collection).getSchemaType(Folder.SCHEMA_TYPE);
            LogicalSearchCondition conditionFolder = LogicalSearchQueryOperators.from(folderSchemaType).where(folderSchemaType.getDefaultSchema().getMetadata(Folder.PARENT_FOLDER)).isEqualTo(folder.getId());
            SearchResponseIterator<Record> iteratorFolder = factory.getModelLayerFactory().newSearchServices().recordsIterator(new LogicalSearchQuery(conditionFolder));
            if (iteratorFolder != null) {
                while (iteratorFolder.hasNext()) {
                    Folder dossierLie = rm.wrapFolder(iteratorFolder.next());
                    List<String> relatedmaterialList = new ArrayList<>();
                    relatedmaterialList.add(dossierLie.getId() + " " + dossierLie.getTitle());
                    archdesc.getRelatedmaterialLists().add(relatedmaterialList);
                }
            }


            //TODO
            List<String> keywords = folder.getKeywords();
            for (String keyword : keywords) {
                archdesc.getControlAccessSubjects().add(keyword);
            }
        } else {
            archdesc = null;
        }
        return archdesc;
    }

    private class SearchableMetadataListFilter implements MetadataListFilter {
        @Override
        public boolean isReturned(Metadata metadata) {
            return metadata.isSearchable();
        }
    }

}
