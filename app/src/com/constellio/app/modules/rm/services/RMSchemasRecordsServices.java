package com.constellio.app.modules.rm.services;

import com.auxilii.msgparser.Message;
import com.auxilii.msgparser.MsgParser;
import com.auxilii.msgparser.attachment.Attachment;
import com.auxilii.msgparser.attachment.FileAttachment;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Cart;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Email;
import com.constellio.app.modules.rm.wrappers.FilingSpace;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.PrintableReport;
import com.constellio.app.modules.rm.wrappers.RMObject;
import com.constellio.app.modules.rm.wrappers.RMUserFolder;
import com.constellio.app.modules.rm.wrappers.StorageSpace;
import com.constellio.app.modules.rm.wrappers.UniformSubdivision;
import com.constellio.app.modules.rm.wrappers.type.ContainerRecordType;
import com.constellio.app.modules.rm.wrappers.type.DocumentType;
import com.constellio.app.modules.rm.wrappers.type.FolderType;
import com.constellio.app.modules.rm.wrappers.type.MediumType;
import com.constellio.app.modules.rm.wrappers.type.SchemaLinkingType;
import com.constellio.app.modules.rm.wrappers.type.StorageSpaceType;
import com.constellio.app.modules.rm.wrappers.type.VariableRetentionPeriod;
import com.constellio.app.modules.rm.wrappers.type.YearType;
import com.constellio.app.services.factories.AppLayerFactory;
import com.constellio.app.ui.pages.base.SessionContextProvider;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.data.utils.LazyIterator;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.DocumentListPDF;
import com.constellio.model.entities.records.wrappers.HierarchicalValueListItem;
import com.constellio.model.entities.records.wrappers.Report;
import com.constellio.model.entities.records.wrappers.SavedSearch;
import com.constellio.model.entities.records.wrappers.SearchEvent;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.records.wrappers.UserDocument;
import com.constellio.model.entities.records.wrappers.UserFolder;
import com.constellio.model.entities.records.wrappers.ValueListItem;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.entities.security.global.AuthorizationAddRequest;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.RecordServices;
import com.constellio.model.services.records.RecordServicesRuntimeException.NoSuchRecordWithId;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hsmf.datatypes.ByteChunk;
import org.apache.poi.hsmf.datatypes.Chunk;
import org.apache.poi.hsmf.datatypes.ChunkGroup;
import org.apache.poi.hsmf.datatypes.Chunks;
import org.apache.poi.hsmf.datatypes.MAPIProperty;
import org.apache.poi.hsmf.datatypes.StringChunk;
import org.apache.poi.hsmf.parsers.POIFSChunkParser;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.joda.time.LocalDateTime;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import static com.constellio.model.entities.schemas.Schemas.SCHEMA;
import static com.constellio.model.entities.security.global.AuthorizationAddRequest.authorizationInCollection;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;

public class RMSchemasRecordsServices extends RMGeneratedSchemaRecordsServices {

	private static final Logger LOGGER = Logger.getLogger(RMSchemasRecordsServices.class);

	public static final String EMAIL_MIME_TYPES = "mimeTypes";
	public static final String EMAIL_ATTACHMENTS = "attachments";
	private static final long NUMBER_OF_RECORDS_IN_CART_LIMIT = 1000;

	public RMSchemasRecordsServices(String collection, SessionContextProvider sessionContextProvider) {
		this(collection, sessionContextProvider.getConstellioFactories().getModelLayerFactory(),
				sessionContextProvider.getSessionContext().getCurrentLocale());
	}

	@Deprecated
	public RMSchemasRecordsServices(String collection, ModelLayerFactory modelLayerFactory) {
		super(collection, modelLayerFactory);
	}

	public RMSchemasRecordsServices(String collection, AppLayerFactory appLayerFactory) {
		super(collection, appLayerFactory.getModelLayerFactory());
	}

	@Deprecated
	public RMSchemasRecordsServices(String collection, ModelLayerFactory modelLayerFactory, Locale locale) {
		super(collection, modelLayerFactory, locale);
	}

	public RMSchemasRecordsServices(String collection, AppLayerFactory appLayerFactory, Locale locale) {
		super(collection, appLayerFactory.getModelLayerFactory(), locale);
	}

	//

	//Generic

	public MetadataSchema getLinkedSchema(MetadataSchemaType schemaType, SchemaLinkingType recordType) {
		if (recordType == null || recordType.getLinkedSchema() == null) {
			return schemaType.getDefaultSchema();
		} else {
			return schemaType.getSchema(recordType.getLinkedSchema());
		}
	}

	//

	//Container record type

	public MetadataSchema containerRecordTypeSchema() {
		return getTypes().getSchema(ContainerRecordType.DEFAULT_SCHEMA);
	}

	public MetadataSchemaType containerRecordTypeSchemaType() {
		return getTypes().getSchemaType(ContainerRecordType.SCHEMA_TYPE);
	}

	public ContainerRecordType wrapContainerRecordType(Record record) {
		return new ContainerRecordType(record, getTypes());
	}

	public List<ContainerRecordType> wrapContainerRecordTypes(List<Record> records) {
		List<ContainerRecordType> containerRecordTypes = new ArrayList<>();
		for (Record record : records) {
			containerRecordTypes.add(wrapContainerRecordType(record));
		}
		return containerRecordTypes;
	}

	public ContainerRecordType getContainerRecordTypeWithLegacyId(String id) {
		return new ContainerRecordType(getByLegacyId(ContainerRecordType.SCHEMA_TYPE, id), getTypes());
	}

	public ContainerRecordType getContainerRecordTypeWithCode(String id) {
		return new ContainerRecordType(getByCode(containerRecordTypeSchemaType(), id), getTypes());
	}

	public ContainerRecordType getContainerRecordType(String id) {
		try {
			return new ContainerRecordType(get(id), getTypes());
		} catch (NoSuchRecordWithId e) {
			return null;
		}
	}

	public ContainerRecordType newContainerRecordType() {
		return new ContainerRecordType(create(containerRecordTypeSchema()), getTypes());
	}

	public ContainerRecordType newContainerRecordTypeWithId(String id) {
		return new ContainerRecordType(create(containerRecordTypeSchema(), id), getTypes());
	}

	//Document

	public MetadataSchema defaultDocumentSchema() {
		return getTypes().getSchema(Document.DEFAULT_SCHEMA);
	}

	public Email wrapEmail(Record record) {
		return record == null ? null : new Email(record, getTypes());
	}

	public Email getEmail(String id) {
		return new Email(get(id), getTypes());
	}

	public MetadataSchema emailSchema() {
		return getTypes().getSchema(Email.SCHEMA);
	}

	public MetadataSchemaType documentSchemaType() {
		return getTypes().getSchemaType(Document.SCHEMA_TYPE);
	}

	public MetadataSchemaType searchEventSchemaType() {
		return getTypes().getSchemaType(SearchEvent.SCHEMA_TYPE);
	}

	public MetadataSchema documentSchemaFor(DocumentType type) {
		return getLinkedSchema(documentSchemaType(), type);
	}

	public MetadataSchema documentSchemaFor(String typeId) {
		return documentSchemaFor(getDocumentType(typeId));
	}

	public DocumentListPDF getDocumentListPDF(String id) {
		return wrapDocumentListPdf(get(id));
	}

	public DocumentListPDF wrapDocumentListPdf(Record record) {
		return record == null ? null : new DocumentListPDF(record, getTypes());
	}

	public Document wrapDocument(Record record) {
		return record == null ? null : new Document(record, getTypes());
	}

	public List<Document> wrapDocuments(List<Record> records) {
		List<Document> documents = new ArrayList<>();
		for (Record record : records) {
			documents.add(wrapDocument(record));
		}
		return documents;
	}

	public Document getDocumentByLegacyId(String id) {
		return wrapDocument(getByLegacyId(Document.SCHEMA_TYPE, id));
	}

	public Document getDocument(String id) {
		return new Document(get(id), getTypes());
	}

	public List<Document> getDocuments(List<String> recordIds) {
		List<Document> documents = new ArrayList<>();
		for (String recordId : recordIds) {
			documents.add(getDocument(recordId));
		}
		return documents;
	}

	public Document newDocument() {
		return new Document(create(defaultDocumentSchema()), getTypes());
	}

	public Email newEmail() {
		Email email = new Email(create(emailSchema()), getTypes());
		String emailSchemaTypeId = getRecordIdForEmailSchema();
		email.setType(emailSchemaTypeId);
		return email;
	}

	public Report newReport() {
		return new Report(create(reportSchema()), getTypes());
	}

	public MetadataSchemaType reportSchemaType() {
		return getTypes().getSchemaType(Report.SCHEMA_TYPE);
	}

	public MetadataSchema reportSchema() {
		return getTypes().getSchema(Report.DEFAULT_SCHEMA);
	}

	public MetadataSchema documentListPDFSchema() {
		return getTypes().getSchema(DocumentListPDF.FULL_SCHEMA);
	}

	public DocumentListPDF newDocumentListPDFWithId(String id) {
		return new DocumentListPDF(create(documentListPDFSchema(), id), getTypes());
	}

	public Document newDocumentWithId(String id) {
		return new Document(create(defaultDocumentSchema(), id), getTypes());
	}

	public Document newDocumentWithType(DocumentType type) {
		Record record = create(documentSchemaFor(type));
		return new Document(record, getTypes()).setType(type);
	}

	public Document newDocumentWithTypeAndId(DocumentType type, String id) {
		Record record = create(documentSchemaFor(type), id);
		return new Document(record, getTypes()).setType(type);
	}

	public Document newDocumentWithType(String typeId) {
		Record record = create(documentSchemaFor(typeId));
		return new Document(record, getTypes()).setType(typeId);
	}

	public Metadata documentContent() {
		return defaultDocumentSchema().getMetadata(Document.CONTENT);
	}

	public Metadata documentFolder() {
		return defaultDocumentSchema().getMetadata(Document.FOLDER);
	}

	public Metadata documentDocumentType() {
		return defaultDocumentSchema().getMetadata(Document.DOCUMENT_TYPE);
	}

	public Metadata documentAdministrativeUnit() {
		return defaultDocumentSchema().getMetadata(Document.FOLDER_ADMINISTRATIVE_UNIT);
	}

	public Metadata documentArchivisticStatus() {
		return defaultDocumentSchema().getMetadata(Document.FOLDER_ARCHIVISTIC_STATUS);
	}

	public Metadata documentPlanifiedTransferDate() {
		return defaultDocumentSchema().getMetadata(Document.FOLDER_EXPECTED_TRANSFER_DATE);
	}

	public Metadata documentPlanifiedDepositDate() {
		return defaultDocumentSchema().getMetadata(Document.FOLDER_EXPECTED_DEPOSIT_DATE);
	}

	public Metadata documentPlanifiedDestructionDate() {
		return defaultDocumentSchema().getMetadata(Document.FOLDER_EXPECTED_DESTRUCTION_DATE);
	}

	//

	//Document type


	public MetadataSchema documentTypeSchema() {
		return getTypes().getSchema(DocumentType.DEFAULT_SCHEMA);
	}

	public MetadataSchemaType documentTypeSchemaType() {
		return getTypes().getSchemaType(DocumentType.SCHEMA_TYPE);
	}

	public DocumentType wrapDocumentType(Record record) {
		return record == null ? null : new DocumentType(record, getTypes());
	}

	public List<DocumentType> wrapDocumentTypes(List<Record> records) {
		List<DocumentType> documentTypes = new ArrayList<>();
		for (Record record : records) {
			documentTypes.add(wrapDocumentType(record));
		}
		return documentTypes;
	}

	public DocumentType getDocumentType(String id) {
		return wrapDocumentType(id == null ? null : get(id));
	}

	public DocumentType getDocumentTypeByCode(String code) {
		return wrapDocumentType(getByCode(documentTypeSchemaType(), code));
	}

	public DocumentType newDocumentType() {
		return new DocumentType(create(documentTypeSchema()), getTypes());
	}

	public DocumentType newDocumentTypeWithId(String id) {
		return new DocumentType(create(documentTypeSchema(), id), getTypes());
	}

	public Metadata documentParentFolder() {
		return defaultDocumentSchema().getMetadata(Document.FOLDER);
	}

	//

	//Filing space

	public MetadataSchemaType filingSpaceSchemaType() {
		return getTypes().getSchemaType(FilingSpace.SCHEMA_TYPE);
	}

	//

	//Folder

	public MetadataSchema folderSchemaFor(String typeId) {
		return typeId == null ? defaultFolderSchema() : folderSchemaFor(getFolderType(typeId));
	}

	public Folder newFolderWithType(String typeId) {
		Record record = create(folderSchemaFor(typeId));
		return new Folder(record, getTypes()).setType(typeId);
	}

	public Folder newFolderWithTypeAndId(String typeId, String id) {
		Record record = create(folderSchemaFor(typeId), id);
		return new Folder(record, getTypes()).setType(typeId);
	}

	public MetadataSchema defaultFolderSchema() {
		return getTypes().getSchema(Folder.DEFAULT_SCHEMA);
	}

	public MetadataSchemaType folderSchemaType() {
		return getTypes().getSchemaType(Folder.SCHEMA_TYPE);
	}

	public MetadataSchema folderSchemaFor(FolderType type) {
		return type == null ? defaultFolderSchema() : getLinkedSchema(folderSchemaType(), type);
	}

	public Folder newFolderWithType(FolderType type) {
		Record record = create(folderSchemaFor(type));
		return new Folder(record, getTypes()).setType(type);
	}

	public Iterator<Folder> foldersIterator(LogicalSearchCondition condition) {
		MetadataSchemaType type = folder.schemaType();
		LogicalSearchQuery query = new LogicalSearchQuery(from(type).whereAllConditions(asList(condition)));
		return foldersIterator(query);
	}

	public Iterator<Folder> foldersIterator(LogicalSearchQuery query) {
		final Iterator<Record> recordIterator = modelLayerFactory.newSearchServices().recordsIterator(query, 2000);
		return new LazyIterator<Folder>() {
			@Override
			protected Folder getNextOrNull() {
				if (recordIterator.hasNext()) {
					return wrapFolder(recordIterator.next());
				} else {
					return null;
				}
			}
		};
	}

	//Folder type

    /*

    public MetadataSchemaType folderTypeSchemaType() {
        return getTypes().getSchemaType(FolderType.SCHEMA_TYPE);
    }

    public List<FolderType> wrapFolderTypes(List<Record> records) {
        List<FolderType> folderTypes = new ArrayList<>();
        for (Record record : records) {
            folderTypes.add(wrapFolderType(record));
        }
        return folderTypes;
    }

    public FolderType getFolderType(String id) {
        return new FolderType(get(id), getTypes());
    }

    public FolderType getFolderTypeByCode(String code) {
        return wrapFolderType(getByCode(folderTypeSchemaType(), code));
    }

    public MetadataSchema defaultFolderTypeSchema() {
        return getTypes().getSchema(FolderType.DEFAULT_SCHEMA);
    }

    public FolderType newFolderType() {
        return new FolderType(create(defaultFolderTypeSchema()), getTypes());
    }

    public FolderType newFolderTypeWithId(String id) {
        return new FolderType(create(defaultFolderTypeSchema(), id), getTypes());
    }*/

	public MetadataSchema folderTypeSchema() {
		return getTypes().getSchema(FolderType.DEFAULT_SCHEMA);
	}

	public MetadataSchemaType folderTypeSchemaType() {
		return getTypes().getSchemaType(FolderType.SCHEMA_TYPE);
	}

	//

	//Hierarchical value list item

	public HierarchicalValueListItem wrapHierarchicalValueListItem(Record record) {
		return new HierarchicalValueListItem(record, getTypes(), record.getSchemaCode());
	}

	public List<HierarchicalValueListItem> wrapHierarchicalValueListItems(List<Record> records) {
		List<HierarchicalValueListItem> hierarchicalValueListItems = new ArrayList<>();
		for (Record record : records) {
			hierarchicalValueListItems.add(wrapHierarchicalValueListItem(record));
		}
		return hierarchicalValueListItems;
	}

	public HierarchicalValueListItem getHierarchicalValueListItem(String id) {
		Record record = get(id);
		return new HierarchicalValueListItem(record, getTypes(), record.getSchemaCode());
	}

	public HierarchicalValueListItem newHierarchicalValueListItem(String schemaCode) {
		return new HierarchicalValueListItem(create(schema(schemaCode)), getTypes(), schemaCode);
	}

	public HierarchicalValueListItem newHierarchicalValueListItemWithId(String schemaCode, String id) {
		return new HierarchicalValueListItem(create(schema(schemaCode), id), getTypes(), schemaCode);
	}

	//Medium type

	public MetadataSchema mediumTypeSchema() {
		return getTypes().getSchema(MediumType.DEFAULT_SCHEMA);
	}

	public MetadataSchemaType mediumTypeSchemaType() {
		return getTypes().getSchemaType(MediumType.SCHEMA_TYPE);
	}

	public MediumType wrapMediumType(Record record) {
		return record == null ? null : new MediumType(record, getTypes());
	}

	public List<MediumType> wrapMediumTypes(List<Record> records) {
		List<MediumType> mediumTypes = new ArrayList<>();
		for (Record record : records) {
			mediumTypes.add(wrapMediumType(record));
		}
		return mediumTypes;
	}

	//KEEP
	@Deprecated
	public String PA() {
		MediumType mediumType = getMediumTypeByCode("PA");
		return mediumType == null ? null : mediumType.getId();
	}

	//KEEP
	@Deprecated
	public String FI() {
		MediumType mediumType = getMediumTypeByCode("FI");
		return mediumType == null ? null : mediumType.getId();
	}

	//KEEP
	@Deprecated
	public String DM() {
		MediumType frenchMediumType = getMediumTypeByCode("DM");
		if (frenchMediumType == null) {
			MediumType mediumType = getMediumTypeByCode("MD");
			return mediumType == null ? null : mediumType.getId();

		} else {
			return frenchMediumType.getId();
		}
	}

	public List<MediumType> getMediumTypes(List<String> ids) {
		return wrapMediumTypes(get(ids));
	}

	public MediumType getMediumType(String id) {
		return wrapMediumType(get(id));
	}

	public MediumType getMediumTypeByCode(String code) {
		return wrapMediumType(getByCode(mediumTypeSchemaType(), code));
	}

	public MediumType newMediumType() {
		return new MediumType(create(mediumTypeSchema()), getTypes());
	}

	public MediumType newMediumTypeWithId(String id) {
		return new MediumType(create(mediumTypeSchema(), id), getTypes());
	}

	//

	//Storage space type

	public MetadataSchema storageSpaceTypeSchema() {
		return getTypes().getSchema(StorageSpaceType.DEFAULT_SCHEMA);
	}

	public MetadataSchemaType storageSpaceTypeSchemaType() {
		return getTypes().getSchemaType(StorageSpaceType.SCHEMA_TYPE);
	}

	public StorageSpaceType wrapStorageSpaceType(Record record) {
		return new StorageSpaceType(record, getTypes());
	}

	public List<StorageSpaceType> wrapStorageSpaceTypes(List<Record> records) {
		List<StorageSpaceType> storageSpaceTypes = new ArrayList<>();
		for (Record record : records) {
			storageSpaceTypes.add(wrapStorageSpaceType(record));
		}
		return storageSpaceTypes;
	}

	public StorageSpaceType getStorageSpaceType(String id) {
		return new StorageSpaceType(get(id), getTypes());
	}

	public StorageSpaceType newStorageSpaceType() {
		return new StorageSpaceType(create(storageSpaceTypeSchema()), getTypes());
	}

	public StorageSpaceType newStorageSpaceTypeWithId(String id) {
		return new StorageSpaceType(create(storageSpaceTypeSchema(), id), getTypes());
	}

	//

	public MetadataSchemaType cartSchemaType() {
		return getTypes().getSchemaType(Cart.SCHEMA_TYPE);
	}

	public MetadataSchema cartSchema() {
		return getTypes().getSchema(Cart.DEFAULT_SCHEMA);
	}

	public Metadata cartOwner() {
		return cartSchema().getMetadata(Cart.OWNER);
	}

	public Metadata cartSharedWithUsers() {
		return cartSchema().getMetadata(Cart.SHARED_WITH_USERS);
	}

	//Cart
	public Cart getOrCreateUserCart(User user) {
		Record record = modelLayerFactory.newSearchServices().searchSingleResult(
				from(cartSchemaType()).where(cartOwner()).isEqualTo(user));
		if (record == null) {
			record = create(cartSchema());
		}
		return new Cart(record, getTypes()).setOwner(user);
	}

	public Cart getOrCreateCart(User user, String cartId) {
		Record record = modelLayerFactory.newSearchServices().searchSingleResult(
				from(cartSchemaType()).where(Schemas.IDENTIFIER).isEqualTo(cartId));
		if (record == null) {
			record = create(cartSchema()).set(cart.owner(), user.getId());
		}
		return new Cart(record, getTypes());
	}

	//User document

	public UserDocument getUserDocument(String id) {
		return new UserDocument(get(id), getTypes());
	}

	public MetadataSchemaType userDocumentSchemaType() {
		return getTypes().getSchemaType(UserDocument.SCHEMA_TYPE);
	}

	public MetadataSchema userDocumentSchema() {
		return getTypes().getSchema(UserDocument.DEFAULT_SCHEMA);
	}

	public Metadata userDocumentUser() {
		return userDocumentSchema().getMetadata(UserDocument.USER);
	}

	public UserDocument wrapUserDocument(Record record) {
		return record == null ? null : new UserDocument(record, getTypes());
	}

	public List<UserDocument> wrapUserDocuments(List<Record> records) {
		List<UserDocument> userDocuments = new ArrayList<>();
		for (Record record : records) {
			userDocuments.add(wrapUserDocument(record));
		}
		return userDocuments;
	}

	public UserDocument newUserDocument() {
		return new UserDocument(create(userDocumentSchema()), getTypes());
	}

	public UserDocument newUserDocumentWithId(String id) {
		return new UserDocument(create(userDocumentSchema(), id), getTypes());
	}

	//User Folder

	public RMUserFolder getUserFolder(String id) {
		return new RMUserFolder(get(id), getTypes());
	}

	public MetadataSchemaType userFolderSchemaType() {
		return getTypes().getSchemaType(UserFolder.SCHEMA_TYPE);
	}

	public MetadataSchema userFolderSchema() {
		return getTypes().getSchema(UserFolder.DEFAULT_SCHEMA);
	}

	public RMUserFolder wrapUserFolder(Record record) {
		return record == null ? null : new RMUserFolder(record, getTypes());
	}

	public List<UserFolder> wrapUserFolders(List<Record> records) {
		List<UserFolder> userFolders = new ArrayList<>();
		for (Record record : records) {
			userFolders.add(wrapUserFolder(record));
		}
		return userFolders;
	}

	public RMUserFolder newUserFolder() {
		return new RMUserFolder(create(userFolderSchema()), getTypes());
	}

	public RMUserFolder newUserFolderWithId(String id) {
		return new RMUserFolder(create(userFolderSchema(), id), getTypes());
	}

	//

	//

	//Value list item

	public ValueListItem getValueListItemByLegacyId(String valueListCode, String valueListItemCode) {
		return wrapValueListItem(getByLegacyId(getTypes().getSchemaType(valueListCode), valueListItemCode));
	}

	public ValueListItem wrapValueListItem(Record record) {
		return record == null ? null : new ValueListItem(record, getTypes(), record.getSchemaCode());
	}

	public List<ValueListItem> wrapValueListItems(List<Record> records) {
		List<ValueListItem> valueListItems = new ArrayList<>();
		for (Record record : records) {
			valueListItems.add(wrapValueListItem(record));
		}
		return valueListItems;
	}


	// Variable retention period

	public MetadataSchema variableRetentionPeriodSchema() {
		return getTypes().getSchema(VariableRetentionPeriod.DEFAULT_SCHEMA);
	}

	public MetadataSchemaType variableRetentionPeriodSchemaType() {
		return getTypes().getSchemaType(VariableRetentionPeriod.SCHEMA_TYPE);
	}

	public VariableRetentionPeriod wrapVariableRetentionPeriod(Record record) {
		return record == null ? null : new VariableRetentionPeriod(record, getTypes());
	}

	public List<VariableRetentionPeriod> wrapVariableRetentionPeriods(List<Record> records) {
		List<VariableRetentionPeriod> variableRetentionPeriods = new ArrayList<>();
		for (Record record : records) {
			variableRetentionPeriods.add(new VariableRetentionPeriod(record, getTypes()));
		}
		return variableRetentionPeriods;
	}

	public VariableRetentionPeriod getVariableRetentionPeriod(String id) {
		return wrapVariableRetentionPeriod(get(id));
	}

	public List<VariableRetentionPeriod> getVariableRetentionPeriods(List<String> stringList) {
		return wrapVariableRetentionPeriods(get(stringList));
	}

	public VariableRetentionPeriod getVariableRetentionPeriodWithCode(String code) {
		return wrapVariableRetentionPeriod(getByCode(variableRetentionPeriodSchemaType(), code));
	}

	public VariableRetentionPeriod newVariableRetentionPeriod() {
		return new VariableRetentionPeriod(create(variableRetentionPeriodSchema()), getTypes());
	}

	public VariableRetentionPeriod newVariableRetentionPeriodWithId(String id) {
		return new VariableRetentionPeriod(create(variableRetentionPeriodSchema(), id), getTypes());
	}

	public VariableRetentionPeriod PERIOD_888() {
		return getVariableRetentionPeriodWithCode("888");
	}

	public VariableRetentionPeriod PERIOD_999() {
		return getVariableRetentionPeriodWithCode("999");
	}

	//KEEP
	public RMObject wrapRMObject(Record record) {
		if (record == null) {
			return null;

		} else if (record.getSchemaCode().startsWith(Folder.SCHEMA_TYPE)) {
			return wrapFolder(record);

		} else if (record.getSchemaCode().startsWith(Document.SCHEMA_TYPE)) {
			return wrapDocument(record);

		} else {
			throw new ImpossibleRuntimeException(
					"Record '" + record.getIdTitle() + "' with schema '" + record.getSchemaCode() + "' is not an RMObject");
		}
	}

	//KEEP
	public String getSchemaCodeForDocumentTypeRecordId(String documentTypeRecordId) {
		ModelLayerFactory modelLayerFactory = getModelLayerFactory();
		RecordServices recordServices = modelLayerFactory.newRecordServices();
		Record schemaRecord = recordServices.getDocumentById(documentTypeRecordId);
		DocumentType documentType = new DocumentType(schemaRecord, getTypes());
		String linkedSchemaCode = documentType.getLinkedSchema();
		return linkedSchemaCode;
	}

	//KEEP
	public String getSchemaCodeForFolderTypeRecordId(String folderTypeRecordId) {
		ModelLayerFactory modelLayerFactory = getModelLayerFactory();
		RecordServices recordServices = modelLayerFactory.newRecordServices();
		Record schemaRecord = recordServices.getDocumentById(folderTypeRecordId);
		FolderType folderType = new FolderType(schemaRecord, getTypes());
		String linkedSchemaCode = folderType.getLinkedSchema();
		return linkedSchemaCode;
	}

	//KEEP
	public String getRecordIdForEmailSchema() {
		ModelLayerFactory modelLayerFactory = getModelLayerFactory();
		SearchServices searchServices = modelLayerFactory.newSearchServices();
		MetadataSchemaTypes types = getTypes();

		MetadataSchema documentTypeDefaultSchema = schema(DocumentType.DEFAULT_SCHEMA);
		Metadata linkedSchemaMetadata = documentTypeDefaultSchema.getMetadata(DocumentType.LINKED_SCHEMA);
		LogicalSearchCondition condition = from(documentTypeDefaultSchema).where(linkedSchemaMetadata)
				.isEqualTo(Email.SCHEMA);
		DocumentType emailDocumentType = new DocumentType(
				searchServices.search(new LogicalSearchQuery().setCondition(condition)).get(0), types);
		return emailDocumentType.getId();
	}

	//KEEP
	public boolean isEmail(String fileName) {
		String extension = StringUtils.lowerCase(FilenameUtils.getExtension(fileName));
		return extension.equals("eml") || extension.equals("msg");
	}

	//KEEP
	public Map<String, Object> parseEmail(String fileName, InputStream messageInputStream) {
		Map<String, Object> parsedMessage;
		String extension = StringUtils.lowerCase(FilenameUtils.getExtension(fileName));
		if ("eml".equals(extension)) {
			parsedMessage = parseEml(messageInputStream);
		} else if ("msg".equals(extension)) {
			parsedMessage = parseMsg(messageInputStream);
		} else {
			throw new IllegalArgumentException("Invalid file name : " + fileName);
		}
		return parsedMessage;
	}

	//KEEP
	@SuppressWarnings("unchecked")
	public Email newEmail(String fileName, InputStream messageInputStream) {
		Map<String, Object> parsedEmail = parseEmail(fileName, messageInputStream);

		Email email = newEmail();

		String subject = (String) parsedEmail.get(Email.SUBJECT);
		String object = (String) parsedEmail.get(Email.EMAIL_OBJECT);
		Date sentOn = (Date) parsedEmail.get(Email.EMAIL_SENT_ON);
		Date receivedOn = (Date) parsedEmail.get(Email.EMAIL_RECEIVED_ON);
		String from = (String) parsedEmail.get(Email.EMAIL_FROM);
		List<String> to = (List<String>) parsedEmail.get(Email.EMAIL_TO);
		List<String> ccTo = (List<String>) parsedEmail.get(Email.EMAIL_CC_TO);
		List<String> bccTo = (List<String>) parsedEmail.get(Email.EMAIL_BCC_TO);
		List<String> attachmentFileNames = (List<String>) parsedEmail.get(Email.EMAIL_ATTACHMENTS_LIST);

		LocalDateTime sentOnDateTime = sentOn != null ? new LocalDateTime(sentOn.getTime()) : null;
		LocalDateTime receivedOnDateTime = receivedOn != null ? new LocalDateTime(receivedOn.getTime()) : null;

		email.setSubject(subject);
		email.setEmailObject(object);
		email.setEmailSentOn(sentOnDateTime);
		email.setEmailReceivedOn(receivedOnDateTime);
		email.setEmailFrom(from);
		email.setEmailTo(to);
		email.setEmailCCTo(ccTo);
		email.setEmailBCCTo(bccTo);
		email.setEmailAttachmentsList(attachmentFileNames);

		return email;
	}

	//KEEP
	public Map<String, Object> parseEml(InputStream messageInputStream) {
		Map<String, Object> parsed = new HashMap<String, Object>();

		Properties props = System.getProperties();
		props.put("mail.host", "smtp.dummydomain.com");
		props.put("mail.transport.protocol", "smtp");

		Session mailSession = Session.getDefaultInstance(props, null);
		try {
			MimeMessage message = new MimeMessage(mailSession, messageInputStream);
			messageInputStream.close();
			String subject = message.getSubject();
			String object = subject;
			Date sentDate = message.getSentDate();
			Date receivedDate = message.getReceivedDate();

			Address from = message.getFrom()[0];
			Address[] to = message.getRecipients(RecipientType.TO);
			Address[] cc = message.getRecipients(RecipientType.CC);
			Address[] bcc = message.getRecipients(RecipientType.BCC);

			ByteArrayOutputStream contentOs = new ByteArrayOutputStream();
			message.writeTo(contentOs);
			contentOs.close();
			String content = contentOs.toString("UTF-8");

			parsed.put(Email.SUBJECT, subject);
			parsed.put(Email.EMAIL_OBJECT, object);
			parsed.put(Email.EMAIL_SENT_ON, sentDate);
			parsed.put(Email.EMAIL_RECEIVED_ON, receivedDate);
			parsed.put(Email.EMAIL_FROM, "" + from);
			parsed.put(Email.EMAIL_TO, addressesAsStringList(to));
			parsed.put(Email.EMAIL_CC_TO, addressesAsStringList(cc));
			parsed.put(Email.EMAIL_BCC_TO, addressesAsStringList(bcc));

			Map<String, InputStream> attachments = new HashMap<String, InputStream>();
			parsed.put(EMAIL_ATTACHMENTS, attachments);

			Map<String, String> mimeTypes = new HashMap<String, String>();
			parsed.put(EMAIL_MIME_TYPES, mimeTypes);

			List<String> attachmentFileNames = new ArrayList<>();
			parsed.put(Email.EMAIL_ATTACHMENTS_LIST, attachmentFileNames);

			Object messageContent = message.getContent();
			if (messageContent instanceof MimeMultipart) {
				MimeMultipart mimeMultipart = (MimeMultipart) messageContent;
				int partCount = mimeMultipart.getCount();
				for (int i = 0; i < partCount; i++) {
					try {
						BodyPart bodyPart = mimeMultipart.getBodyPart(i);
						String partFileName = bodyPart.getFileName();
						Object partContent = bodyPart.getContent();
						if (partContent instanceof InputStream) {
							partFileName = MimeUtility.decodeText(partFileName);
							InputStream inputAttachment = (InputStream) partContent;
							attachments.put(partFileName, inputAttachment);
							mimeTypes.put(partFileName, bodyPart.getContentType());
							attachmentFileNames.add(partFileName);
						}
					} catch (Throwable t) {
						LOGGER.warn("Error while parsing message content", t);
					}
				}
			}

		} catch (MessagingException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return parsed;
	}

	//KEEP
	private static List<String> addressesAsStringList(Address[] addresses) {
		List<String> addressesStr = new ArrayList<>();
		if (addresses != null) {
			for (Address address : addresses) {
				addressesStr.add(address.toString());
			}
		}
		return addressesStr;
	}

	//KEEP
	public Map<String, Object> parseMsg(InputStream messageInputStream) {
		Map<String, Object> parsed = new HashMap<String, Object>();
		try {
			byte[] messageBytes = IOUtils.toByteArray(messageInputStream);
			messageInputStream.close();

			Chunks CHUNKS = new Chunks();
			POIFSFileSystem filesystem = new POIFSFileSystem(new ByteArrayInputStream(messageBytes));
			ChunkGroup[] chunkGroups = POIFSChunkParser.parse(filesystem);
			for (ChunkGroup chunkGroup : chunkGroups) {
				for (Chunk chunk : chunkGroup.getChunks()) {
					Chunk recordChunk;
					int chunkId = chunk.getChunkId();
					if (chunkId == MAPIProperty.BODY.id) {
						if (chunk instanceof ByteChunk) {
							final ByteChunk byteChunk = (ByteChunk) chunk;
							recordChunk = new StringChunk(byteChunk.getChunkId(), byteChunk.getType()) {
								@Override
								public String get7BitEncoding() {
									return byteChunk.getAs7bitString();
								}

								@Override
								public void set7BitEncoding(String encoding) {
									super.set7BitEncoding(encoding);
								}

								@Override
								public void readValue(InputStream value)
										throws IOException {
									byteChunk.readValue(value);
								}

								@Override
								public void writeValue(OutputStream out)
										throws IOException {
									byteChunk.writeValue(out);
								}

								@Override
								public String getValue() {
									return new String(byteChunk.getValue());
								}

								@Override
								public byte[] getRawValue() {
									return byteChunk.getValue();
								}

								@Override
								public void setValue(String str) {
									byteChunk.setValue(str.getBytes());
								}

								@Override
								public String toString() {
									return byteChunk.toString();
								}

								@Override
								public String getEntryName() {
									return byteChunk.getEntryName();
								}
							};
						} else {
							recordChunk = chunk;
						}
					} else {
						recordChunk = chunk;
					}
					CHUNKS.record(recordChunk);
				}
			}

			String from = getValue(CHUNKS.getDisplayFromChunk());
			String subject = getValue(CHUNKS.getSubjectChunk());
			String to = getValue(CHUNKS.getDisplayToChunk());
			String cc = getValue(CHUNKS.getDisplayCCChunk());
			String bcc = getValue(CHUNKS.getDisplayBCCChunk());
			String content = getValue(CHUNKS.getTextBodyChunk());

			MsgParser msgp = new MsgParser();
			Message msg = msgp.parseMsg(new ByteArrayInputStream(messageBytes));
			Date sentDate = msg.getDate();
			Date receivedDate = msg.getDate();

			parsed.put(Email.SUBJECT, subject);
			parsed.put(Email.EMAIL_OBJECT, subject);
			parsed.put(Email.EMAIL_SENT_ON, sentDate);
			parsed.put(Email.EMAIL_RECEIVED_ON, receivedDate);
			parsed.put(Email.EMAIL_FROM, from);
			parsed.put(Email.EMAIL_CC_TO, splitAddresses(to));
			parsed.put(Email.EMAIL_CC_TO, splitAddresses(cc));
			parsed.put(Email.EMAIL_BCC_TO, splitAddresses(bcc));
			insertMsgAttachments(parsed, msg);

		} catch (UnsupportedOperationException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		return parsed;
	}

	//KEEP
	private String getValue(StringChunk chunk) {
		return chunk == null ? null : chunk.getValue();
	}

	//KEEP
	private static List<String> splitAddresses(String addresses) {
		return Arrays.asList(StringUtils.split(addresses, ";"));
	}

	//KEEP
	private static void insertMsgAttachments(Map<String, Object> parsed, Message msg) {
		Map<String, InputStream> attachments = new HashMap<String, InputStream>();
		parsed.put(EMAIL_ATTACHMENTS, attachments);

		List<String> attachmentFileNames = new ArrayList<>();
		parsed.put(Email.EMAIL_ATTACHMENTS_LIST, attachmentFileNames);

		Map<String, String> mimeTypes = new HashMap<String, String>();
		parsed.put(EMAIL_MIME_TYPES, mimeTypes);

		List<Attachment> atts = msg.getAttachments();
		for (Attachment att : atts) {
			if (att instanceof FileAttachment) {
				FileAttachment file = (FileAttachment) att;
				String fileName = file.getFilename();
				attachments.put(file.getLongFilename(), new ByteArrayInputStream(file.getData()));
				mimeTypes.put(fileName, file.getMimeTag());
				attachmentFileNames.add(fileName);
			}
		}
	}

	//KEEP
	public AuthorizationAddRequest newAuthorization() {
		return authorizationInCollection(getCollection());
	}

	//KEEP
	public List<MetadataSchemaType> valueListSchemaTypes() {
		List<MetadataSchemaType> returnedTypes = new ArrayList<>();

		for (MetadataSchemaType type : getTypes().getSchemaTypes()) {
			if (type.getCode().startsWith("ddv")) {
				returnedTypes.add(type);
			}
		}

		return returnedTypes;
	}

	//KEEP
	public DocumentType emailDocumentType() {
		return getDocumentTypeByCode(DocumentType.EMAIL_DOCUMENT_TYPE);
	}

	//KEEP
	public Folder setType(Folder folder, FolderType folderType) {
		setType(folder.getWrappedRecord(), folderType == null ? null : folderType.getWrappedRecord());
		return folder;
	}

	//KEEP
	public Document setType(Document document, FolderType documentType) {
		setType(document.getWrappedRecord(), documentType == null ? null : documentType.getWrappedRecord());
		return document;
	}

	public Report wrapReport(Record record) {
		return record == null ? null : new Report(record, getTypes());
	}

	public SavedSearch wrapSavedSearch(Record record) {
		return record == null ? null : new SavedSearch(record, getTypes());
	}

	public YearType getYearTypeWithCode(String code) {
		return wrapYearType(getByCode(ddvYearType.schemaType(), code));
	}

	public List<AdministrativeUnit> getAllAdministrativeUnits() {
		return wrapAdministrativeUnits(getModelLayerFactory().newSearchServices().getAllRecords(administrativeUnit.schemaType()));
	}


	public List<UniformSubdivision> getAllUniformSubdivisions() {
		return wrapUniformSubdivisions(getModelLayerFactory().newSearchServices().getAllRecords(uniformSubdivision.schemaType()));
	}

	public List<Category> getAllCategories() {
		return wrapCategorys(getModelLayerFactory().newSearchServices().getAllRecords(category.schemaType()));
	}

	public List<StorageSpace> getAllStorageSpaces() {
		return wrapStorageSpaces(getModelLayerFactory().newSearchServices().getAllRecords(storageSpace.schemaType()));
	}

	public List<AdministrativeUnit> getAllAdministrativeUnitsInUnmodifiableState() {
		return wrapAdministrativeUnits(
				getModelLayerFactory().newSearchServices().getAllRecordsInUnmodifiableState(administrativeUnit.schemaType()));
	}

	public List<PrintableReport> getAllPrintableReports() {
		return wrapPrintableReports(getModelLayerFactory().newSearchServices().cachedSearch(new LogicalSearchQuery(
				from(printable_report.schemaType()).where(SCHEMA).isEqualTo(PrintableReport.SCHEMA_NAME))));
	}

	public List<Folder> getFolderByUnicity(String unicity) {
		List<Folder> resultListFolder = wrapFolders(getModelLayerFactory().newSearchServices().search(new LogicalSearchQuery(
				from(folder.schemaType()).where(folder.uniqueKey()).isEqualTo(unicity))));

		return resultListFolder;
	}

	public boolean numberOfFoldersInFavoritesReachesLimit(String cartId, int size) {
		SearchServices searchServices = modelLayerFactory.newSearchServices();
		LogicalSearchQuery logicalSearchQuery = new LogicalSearchQuery(from(folder.schemaType()).where(folder.favorites()).isEqualTo(cartId));
		return searchServices.getResultsCount(logicalSearchQuery) + size > NUMBER_OF_RECORDS_IN_CART_LIMIT;
	}

	public boolean numberOfContainersInFavoritesReachesLimit(String cartId, int size) {
		SearchServices searchServices = modelLayerFactory.newSearchServices();
		LogicalSearchQuery logicalSearchQuery = new LogicalSearchQuery(from(containerRecord.schemaType()).where(containerRecord.favorites()).isEqualTo(cartId));
		return searchServices.getResultsCount(logicalSearchQuery) + size > NUMBER_OF_RECORDS_IN_CART_LIMIT;
	}

	public boolean numberOfDocumentsInFavoritesReachesLimit(String cartId, int size) {
		SearchServices searchServices = modelLayerFactory.newSearchServices();
		LogicalSearchQuery logicalSearchQuery = new LogicalSearchQuery(from(document.schemaType()).where(document.favorites()).isEqualTo(cartId));
		return searchServices.getResultsCount(logicalSearchQuery) + size > NUMBER_OF_RECORDS_IN_CART_LIMIT;
	}

}
