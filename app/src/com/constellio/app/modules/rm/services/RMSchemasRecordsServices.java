package com.constellio.app.modules.rm.services;

import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Cart;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Email;
import com.constellio.app.modules.rm.wrappers.FilingSpace;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.PrintableReport;
import com.constellio.app.modules.rm.wrappers.RMObject;
import com.constellio.app.modules.rm.wrappers.RMUserFolder;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.modules.rm.wrappers.UniformSubdivision;
import com.constellio.app.modules.rm.wrappers.UserFunction;
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
import com.constellio.data.dao.dto.records.FacetValue;
import com.constellio.data.utils.ImpossibleRuntimeException;
import com.constellio.data.utils.LazyIterator;
import com.constellio.data.utils.Provider;
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
import com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.users.UserServices;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.ss.formula.functions.T;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static com.constellio.model.entities.schemas.Schemas.SCHEMA;
import static com.constellio.model.entities.schemas.Schemas.VISIBLE_IN_TREES;
import static com.constellio.model.entities.security.global.AuthorizationAddRequest.authorizationInCollection;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.fromAllSchemasIn;
import static java.util.Arrays.asList;

public class RMSchemasRecordsServices extends RMGeneratedSchemaRecordsServices {

	private static final Logger LOGGER = Logger.getLogger(RMSchemasRecordsServices.class);

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

	public Folder getFolderSummary(String id) {
		return wrapFolder(getSummary(folder.schemaType(), id));
	}

	public Document getDocumentSummary(String id) {
		return wrapDocument(getSummary(document.schemaType(), id));
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

	public MetadataSchemaType containerRecordSchemaType() {
		return getTypes().getSchemaType(ContainerRecord.SCHEMA_TYPE);
	}

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

	@Deprecated
	/**
	 * User LogicalSearchQuery instead
	 */
	public List<AdministrativeUnit> getAllAdministrativeUnits() {
		return wrapAdministrativeUnits(getModelLayerFactory().newSearchServices().getAllRecords(administrativeUnit.schemaType()));
	}

	@Deprecated
	/**
	 * User LogicalSearchQuery instead
	 */
	public List<RetentionRule> getAllRetentionRules() {
		return wrapRetentionRules(getModelLayerFactory().newSearchServices().getAllRecords(retentionRule.schemaType()));
	}

	@Deprecated
	/**
	 * User LogicalSearchQuery instead
	 */

	public List<UniformSubdivision> getAllUniformSubdivisions() {
		return wrapUniformSubdivisions(getModelLayerFactory().newSearchServices().getAllRecords(uniformSubdivision.schemaType()));
	}

	public List<Category> getAllCategories() {
		return wrapCategorys(getModelLayerFactory().newSearchServices().getAllRecords(category.schemaType()));
	}

	@Deprecated
	/**
	 * User LogicalSearchQuery instead
	 */
	public List<AdministrativeUnit> getAllAdministrativeUnitsInUnmodifiableState() {
		return wrapAdministrativeUnits(
				getModelLayerFactory().newSearchServices().getAllRecordsInUnmodifiableState(administrativeUnit.schemaType()));
	}

	/**
	 * Use logical search query instead
	 *
	 * @return
	 */
	@Deprecated
	public List<PrintableReport> getAllPrintableReports() {
		return wrapPrintableReports(getModelLayerFactory().newSearchServices().cachedSearch(new LogicalSearchQuery(
				from(printable_report.schemaType()).where(SCHEMA).isEqualTo(PrintableReport.SCHEMA_NAME))));
	}


	public List<Folder> getFolderByUnicity(String uniqueKey) {
		if (uniqueKey == null) {
			return Collections.emptyList();
		}
		List<Folder> resultListFolder = wrapFolders(getModelLayerFactory().newSearchServices().search(new LogicalSearchQuery(
				from(folder.schemaType()).where(folder.uniqueKey()).isEqualTo(uniqueKey))));

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

	public UserFunctionChecker getUsersWithFunction(UserFunction userFunction) {
		return new UserFunctionChecker(this, userFunction);
	}

	/**
	 * Use logical search query instead
	 *
	 * @return
	 */
	@Deprecated
	public List<AdministrativeUnit> getAdministrativeUnitsUsingFilter(
			final Provider<AdministrativeUnit, Boolean> provider) {

		List<AdministrativeUnit> administrativeUnits = new ArrayList<>();

		for (AdministrativeUnit au : getAllAdministrativeUnitsInUnmodifiableState()) {
			if (provider.get(au)) {
				administrativeUnits.add(au);
			}
		}

		return administrativeUnits;

	}

	public void preloadCategoryTaxonomyCache() {
		UserServices userServices = modelLayerFactory.newUserServices();
		SearchServices searchServices = modelLayerFactory.newSearchServices();
		List<User> allUsersInCollection = userServices.getAllUsersInCollection(getCollection());
		for (int i = 0; i < allUsersInCollection.size(); i++) {
			LOGGER.info("Loading taxonomy cache of user " + i + " / " + allUsersInCollection.size());
			User user = allUsersInCollection.get(i);

			LogicalSearchQuery query = new LogicalSearchQuery(fromAllSchemasIn(this.getCollection())
					.where(Schemas.SCHEMA).<T>isNot(LogicalSearchQueryOperators.<T>startingWithText(Category.SCHEMA_TYPE + "_"))
					.andWhere(VISIBLE_IN_TREES).isTrueOrNull());

			query.filteredWithUser(user);
			query.setFieldFacetLimit(100_000);
			query.addFieldFacet(folder.category().getDataStoreCode());
			query.setNumberOfRows(0);

			Set<String> visibleIds = new HashSet<>();
			for (FacetValue facetValue : searchServices.query(query)
					.getFieldFacetValues(folder.category().getDataStoreCode())) {
				if (facetValue.getQuantity() > 0) {

					String id = facetValue.getValue();
					while (id != null) {
						visibleIds.add(id);
						id = getCategory(id).getParent();
					}
				}
			}

			for (Category category : getAllCategories()) {
				modelLayerFactory.getTaxonomiesSearchServicesCache()
						.insert(user.getUsername(), category.getId(), "visible", visibleIds.contains(category.getId()));
			}

		}
	}
}
