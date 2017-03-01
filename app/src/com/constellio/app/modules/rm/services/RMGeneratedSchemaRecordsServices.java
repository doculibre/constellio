package com.constellio.app.modules.rm.services;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;

import com.constellio.app.modules.reports.wrapper.Printable;
import com.constellio.app.modules.rm.wrappers.*;
import com.constellio.app.modules.rm.wrappers.type.DocumentType;
import com.constellio.app.modules.rm.wrappers.type.FolderType;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

public class RMGeneratedSchemaRecordsServices extends SchemasRecordsServices {

	ModelLayerFactory appLayerFactory;

	public RMGeneratedSchemaRecordsServices(String collection,
			ModelLayerFactory modelLayerFactory) {
		super(collection, modelLayerFactory);
	}

	/** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** **/
	// Auto-generated methods by GenerateHelperClassAcceptTest -- start

	/** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** **/

	public AdministrativeUnit wrapAdministrativeUnit(Record record) {
		return record == null ? null : new AdministrativeUnit(record, getTypes());
	}

	public List<AdministrativeUnit> wrapAdministrativeUnits(List<Record> records) {
		List<AdministrativeUnit> wrapped = new ArrayList<>();
		for (Record record : records) {
			wrapped.add(new AdministrativeUnit(record, getTypes()));
		}

		return wrapped;
	}

	public List<AdministrativeUnit> searchAdministrativeUnits(LogicalSearchQuery query) {
		return wrapAdministrativeUnits(modelLayerFactory.newSearchServices().search(query));
	}

	public List<AdministrativeUnit> searchAdministrativeUnits(LogicalSearchCondition condition) {
		MetadataSchemaType type = administrativeUnit.schemaType();
		LogicalSearchQuery query = new LogicalSearchQuery(from(type).whereAllConditions(asList(condition)));
		return wrapAdministrativeUnits(modelLayerFactory.newSearchServices().search(query));
	}

	public AdministrativeUnit getAdministrativeUnit(String id) {
		return wrapAdministrativeUnit(get(id));
	}

	public List<AdministrativeUnit> getAdministrativeUnits(List<String> ids) {
		return wrapAdministrativeUnits(get(ids));
	}

	public AdministrativeUnit getAdministrativeUnitWithCode(String code) {
		return wrapAdministrativeUnit(getByCode(administrativeUnit.schemaType(), code));
	}

	public AdministrativeUnit getAdministrativeUnitWithLegacyId(String legacyId) {
		return wrapAdministrativeUnit(getByLegacyId(administrativeUnit.schemaType(), legacyId));
	}

	public AdministrativeUnit newAdministrativeUnit() {
		return wrapAdministrativeUnit(create(administrativeUnit.schema()));
	}

	public AdministrativeUnit newAdministrativeUnitWithId(String id) {
		return wrapAdministrativeUnit(create(administrativeUnit.schema(), id));
	}

	public final SchemaTypeShortcuts_administrativeUnit_default administrativeUnit
			= new SchemaTypeShortcuts_administrativeUnit_default("administrativeUnit_default");

	public class SchemaTypeShortcuts_administrativeUnit_default extends SchemaTypeShortcuts {
		protected SchemaTypeShortcuts_administrativeUnit_default(String schemaCode) {
			super(schemaCode);
		}

		public Metadata adress() {
			return metadata("adress");
		}

		public Metadata code() {
			return metadata("code");
		}

		public Metadata decommissioningMonth() {
			return metadata("decommissioningMonth");
		}

		public Metadata description() {
			return metadata("description");
		}

		public Metadata filingSpaces() {
			return metadata("filingSpaces");
		}

		public Metadata filingSpacesAdmins() {
			return metadata("filingSpacesAdmins");
		}

		public Metadata filingSpacesUsers() {
			return metadata("filingSpacesUsers");
		}

		public Metadata parent() {
			return metadata("parent");
		}

		public Metadata unitAncestors() {
			return metadata("unitAncestors");
		}
	}

	public Cart wrapCart(Record record) {
		return record == null ? null : new Cart(record, getTypes());
	}

	public List<Cart> wrapCarts(List<Record> records) {
		List<Cart> wrapped = new ArrayList<>();
		for (Record record : records) {
			wrapped.add(new Cart(record, getTypes()));
		}

		return wrapped;
	}

	public List<Cart> searchCarts(LogicalSearchQuery query) {
		return wrapCarts(modelLayerFactory.newSearchServices().search(query));
	}

	public List<Cart> searchCarts(LogicalSearchCondition condition) {
		MetadataSchemaType type = cart.schemaType();
		LogicalSearchQuery query = new LogicalSearchQuery(from(type).whereAllConditions(asList(condition)));
		return wrapCarts(modelLayerFactory.newSearchServices().search(query));
	}

	public Cart getCart(String id) {
		return wrapCart(get(id));
	}

	public List<Cart> getCarts(List<String> ids) {
		return wrapCarts(get(ids));
	}

	public Cart getCartWithLegacyId(String legacyId) {
		return wrapCart(getByLegacyId(cart.schemaType(), legacyId));
	}

	public Cart newCart() {
		return wrapCart(create(cart.schema()));
	}

	public Cart newCartWithId(String id) {
		return wrapCart(create(cart.schema(), id));
	}

	public final SchemaTypeShortcuts_cart_default cart
			= new SchemaTypeShortcuts_cart_default("cart_default");

	public class SchemaTypeShortcuts_cart_default extends SchemaTypeShortcuts {
		protected SchemaTypeShortcuts_cart_default(String schemaCode) {
			super(schemaCode);
		}

		public Metadata containers() {
			return metadata("containers");
		}

		public Metadata documents() {
			return metadata("documents");
		}

		public Metadata folders() {
			return metadata("folders");
		}

		public Metadata owner() {
			return metadata("owner");
		}

		public Metadata sharedWithUsers() {
			return metadata("sharedWithUsers");
		}
	}

	public Category wrapCategory(Record record) {
		return record == null ? null : new Category(record, getTypes());
	}

	public List<Category> wrapCategorys(List<Record> records) {
		List<Category> wrapped = new ArrayList<>();
		for (Record record : records) {
			wrapped.add(new Category(record, getTypes()));
		}

		return wrapped;
	}

	public List<Category> searchCategorys(LogicalSearchQuery query) {
		return wrapCategorys(modelLayerFactory.newSearchServices().search(query));
	}

	public List<Category> searchCategorys(LogicalSearchCondition condition) {
		MetadataSchemaType type = category.schemaType();
		LogicalSearchQuery query = new LogicalSearchQuery(from(type).whereAllConditions(asList(condition)));
		return wrapCategorys(modelLayerFactory.newSearchServices().search(query));
	}

	public Category getCategory(String id) {
		return wrapCategory(get(id));
	}

	public List<Category> getCategorys(List<String> ids) {
		return wrapCategorys(get(ids));
	}

	public Category getCategoryWithCode(String code) {
		return wrapCategory(getByCode(category.schemaType(), code));
	}

	public Category getCategoryWithLegacyId(String legacyId) {
		return wrapCategory(getByLegacyId(category.schemaType(), legacyId));
	}

	public Category newCategory() {
		return wrapCategory(create(category.schema()));
	}

	public Category newCategoryWithId(String id) {
		return wrapCategory(create(category.schema(), id));
	}

	public final SchemaTypeShortcuts_category_default category
			= new SchemaTypeShortcuts_category_default("category_default");

	public class SchemaTypeShortcuts_category_default extends SchemaTypeShortcuts {
		protected SchemaTypeShortcuts_category_default(String schemaCode) {
			super(schemaCode);
		}

		public Metadata code() {
			return metadata("code");
		}

		public Metadata comments() {
			return metadata("comments");
		}

		public Metadata copyRetentionRulesOnDocumentTypes() {
			return metadata("copyRetentionRulesOnDocumentTypes");
		}

		public Metadata description() {
			return metadata("description");
		}

		public Metadata keywords() {
			return metadata("keywords");
		}

		public Metadata level() {
			return metadata("level");
		}

		public Metadata parent() {
			return metadata("parent");
		}

		public Metadata retentionRules() {
			return metadata("retentionRules");
		}
	}

	public ContainerRecord wrapContainerRecord(Record record) {
		return record == null ? null : new ContainerRecord(record, getTypes());
	}

	public List<ContainerRecord> wrapContainerRecords(List<Record> records) {
		List<ContainerRecord> wrapped = new ArrayList<>();
		for (Record record : records) {
			wrapped.add(new ContainerRecord(record, getTypes()));
		}

		return wrapped;
	}

	public List<ContainerRecord> searchContainerRecords(LogicalSearchQuery query) {
		return wrapContainerRecords(modelLayerFactory.newSearchServices().search(query));
	}

	public List<ContainerRecord> searchContainerRecords(LogicalSearchCondition condition) {
		MetadataSchemaType type = containerRecord.schemaType();
		LogicalSearchQuery query = new LogicalSearchQuery(from(type).whereAllConditions(asList(condition)));
		return wrapContainerRecords(modelLayerFactory.newSearchServices().search(query));
	}

	public ContainerRecord getContainerRecord(String id) {
		return wrapContainerRecord(get(id));
	}

	public List<ContainerRecord> getContainerRecords(List<String> ids) {
		return wrapContainerRecords(get(ids));
	}

	public ContainerRecord getContainerRecordWithLegacyId(String legacyId) {
		return wrapContainerRecord(getByLegacyId(containerRecord.schemaType(), legacyId));
	}

	public ContainerRecord newContainerRecord() {
		return wrapContainerRecord(create(containerRecord.schema()));
	}

	public ContainerRecord newContainerRecordWithId(String id) {
		return wrapContainerRecord(create(containerRecord.schema(), id));
	}

	public final SchemaTypeShortcuts_containerRecord_default containerRecord
			= new SchemaTypeShortcuts_containerRecord_default("containerRecord_default");

	public class SchemaTypeShortcuts_containerRecord_default extends SchemaTypeShortcuts {
		protected SchemaTypeShortcuts_containerRecord_default(String schemaCode) {
			super(schemaCode);
		}

		public Metadata administrativeUnit() {
			return metadata("administrativeUnit");
		}

		public Metadata borrowDate() {
			return metadata("borrowDate");
		}

		public Metadata borrowed() {
			return metadata("borrowed");
		}

		public Metadata borrower() {
			return metadata("borrower");
		}

		public Metadata capacity() {
			return metadata("capacity");
		}

		public Metadata comments() {
			return metadata("comments");
		}

		public Metadata completionDate() {
			return metadata("completionDate");
		}

		public Metadata decommissioningType() {
			return metadata("decommissioningType");
		}

		public Metadata description() {
			return metadata("description");
		}

		public Metadata filingSpace() {
			return metadata("filingSpace");
		}

		public Metadata fillRatioEntered() {
			return metadata("fillRatioEntered");
		}

		public Metadata full() {
			return metadata("full");
		}

		public Metadata identifier() {
			return metadata("identifier");
		}

		public Metadata planifiedReturnDate() {
			return metadata("planifiedReturnDate");
		}

		public Metadata position() {
			return metadata("position");
		}

		public Metadata realDepositDate() {
			return metadata("realDepositDate");
		}

		public Metadata realReturnDate() {
			return metadata("realReturnDate");
		}

		public Metadata realTransferDate() {
			return metadata("realTransferDate");
		}

		public Metadata storageSpace() {
			return metadata("storageSpace");
		}

		public Metadata temporaryIdentifier() {
			return metadata("temporaryIdentifier");
		}

		public Metadata type() {
			return metadata("type");
		}
	}

	public DocumentType wrapDocumentType(Record record) {
		return record == null ? null : new DocumentType(record, getTypes());
	}

	public List<DocumentType> wrapDocumentTypes(List<Record> records) {
		List<DocumentType> wrapped = new ArrayList<>();
		for (Record record : records) {
			wrapped.add(new DocumentType(record, getTypes()));
		}

		return wrapped;
	}

	public List<DocumentType> searchDocumentTypes(LogicalSearchQuery query) {
		return wrapDocumentTypes(modelLayerFactory.newSearchServices().search(query));
	}

	public List<DocumentType> searchDocumentTypes(LogicalSearchCondition condition) {
		MetadataSchemaType type = ddvDocumentType.schemaType();
		LogicalSearchQuery query = new LogicalSearchQuery(from(type).whereAllConditions(asList(condition)));
		return wrapDocumentTypes(modelLayerFactory.newSearchServices().search(query));
	}

	public DocumentType getDocumentType(String id) {
		return wrapDocumentType(get(id));
	}

	public List<DocumentType> getDocumentTypes(List<String> ids) {
		return wrapDocumentTypes(get(ids));
	}

	public DocumentType getDocumentTypeWithCode(String code) {
		return wrapDocumentType(getByCode(ddvDocumentType.schemaType(), code));
	}

	public DocumentType getDocumentTypeWithLegacyId(String legacyId) {
		return wrapDocumentType(getByLegacyId(ddvDocumentType.schemaType(), legacyId));
	}

	public DocumentType newDocumentType() {
		return wrapDocumentType(create(ddvDocumentType.schema()));
	}

	public DocumentType newDocumentTypeWithId(String id) {
		return wrapDocumentType(create(ddvDocumentType.schema(), id));
	}

	public final SchemaTypeShortcuts_ddvDocumentType_default ddvDocumentType
			= new SchemaTypeShortcuts_ddvDocumentType_default("ddvDocumentType_default");

	public class SchemaTypeShortcuts_ddvDocumentType_default extends SchemaTypeShortcuts {
		protected SchemaTypeShortcuts_ddvDocumentType_default(String schemaCode) {
			super(schemaCode);
		}

		public Metadata linkedSchema() {
			return metadata("linkedSchema");
		}

		public Metadata templates() {
			return metadata("templates");
		}
	}

	public FolderType wrapFolderType(Record record) {
		return record == null ? null : new FolderType(record, getTypes());
	}

	public List<FolderType> wrapFolderTypes(List<Record> records) {
		List<FolderType> wrapped = new ArrayList<>();
		for (Record record : records) {
			wrapped.add(new FolderType(record, getTypes()));
		}

		return wrapped;
	}

	public List<FolderType> searchFolderTypes(LogicalSearchQuery query) {
		return wrapFolderTypes(modelLayerFactory.newSearchServices().search(query));
	}

	public List<FolderType> searchFolderTypes(LogicalSearchCondition condition) {
		MetadataSchemaType type = ddvFolderType.schemaType();
		LogicalSearchQuery query = new LogicalSearchQuery(from(type).whereAllConditions(asList(condition)));
		return wrapFolderTypes(modelLayerFactory.newSearchServices().search(query));
	}

	public FolderType getFolderType(String id) {
		return wrapFolderType(get(id));
	}

	public List<FolderType> getFolderTypes(List<String> ids) {
		return wrapFolderTypes(get(ids));
	}

	public FolderType getFolderTypeWithCode(String code) {
		return wrapFolderType(getByCode(ddvFolderType.schemaType(), code));
	}

	public FolderType getFolderTypeWithLegacyId(String legacyId) {
		return wrapFolderType(getByLegacyId(ddvFolderType.schemaType(), legacyId));
	}

	public FolderType newFolderType() {
		return wrapFolderType(create(ddvFolderType.schema()));
	}

	public FolderType newFolderTypeWithId(String id) {
		return wrapFolderType(create(ddvFolderType.schema(), id));
	}

	public final SchemaTypeShortcuts_ddvFolderType_default ddvFolderType
			= new SchemaTypeShortcuts_ddvFolderType_default("ddvFolderType_default");

	public class SchemaTypeShortcuts_ddvFolderType_default extends SchemaTypeShortcuts {
		protected SchemaTypeShortcuts_ddvFolderType_default(String schemaCode) {
			super(schemaCode);
		}

		public Metadata linkedSchema() {
			return metadata("linkedSchema");
		}
	}

	public DecommissioningList wrapDecommissioningList(Record record) {
		return record == null ? null : new DecommissioningList(record, getTypes());
	}

	public List<DecommissioningList> wrapDecommissioningLists(List<Record> records) {
		List<DecommissioningList> wrapped = new ArrayList<>();
		for (Record record : records) {
			wrapped.add(new DecommissioningList(record, getTypes()));
		}

		return wrapped;
	}

	public List<DecommissioningList> searchDecommissioningLists(LogicalSearchQuery query) {
		return wrapDecommissioningLists(modelLayerFactory.newSearchServices().search(query));
	}

	public List<DecommissioningList> searchDecommissioningLists(LogicalSearchCondition condition) {
		MetadataSchemaType type = decommissioningList.schemaType();
		LogicalSearchQuery query = new LogicalSearchQuery(from(type).whereAllConditions(asList(condition)));
		return wrapDecommissioningLists(modelLayerFactory.newSearchServices().search(query));
	}

	public DecommissioningList getDecommissioningList(String id) {
		return wrapDecommissioningList(get(id));
	}

	public List<DecommissioningList> getDecommissioningLists(List<String> ids) {
		return wrapDecommissioningLists(get(ids));
	}

	public DecommissioningList getDecommissioningListWithLegacyId(String legacyId) {
		return wrapDecommissioningList(getByLegacyId(decommissioningList.schemaType(), legacyId));
	}

	public DecommissioningList newDecommissioningList() {
		return wrapDecommissioningList(create(decommissioningList.schema()));
	}

	public DecommissioningList newDecommissioningListWithId(String id) {
		return wrapDecommissioningList(create(decommissioningList.schema(), id));
	}

	public final SchemaTypeShortcuts_decommissioningList_default decommissioningList
			= new SchemaTypeShortcuts_decommissioningList_default("decommissioningList_default");

	public class SchemaTypeShortcuts_decommissioningList_default extends SchemaTypeShortcuts {
		protected SchemaTypeShortcuts_decommissioningList_default(String schemaCode) {
			super(schemaCode);
		}

		public Metadata administrativeUnit() {
			return metadata("administrativeUnit");
		}

		public Metadata analogicalMedium() {
			return metadata("analogicalMedium");
		}

		public Metadata approvalDate() {
			return metadata("approvalDate");
		}

		public Metadata approvalRequest() {
			return metadata("approvalRequest");
		}

		public Metadata approvalRequestDate() {
			return metadata("approvalRequestDate");
		}

		public Metadata approvalUser() {
			return metadata("approvalUser");
		}

		public Metadata comments() {
			return metadata("comments");
		}

		public Metadata containerDetails() {
			return metadata("containerDetails");
		}

		public Metadata containers() {
			return metadata("containers");
		}

		public Metadata description() {
			return metadata("description");
		}

		public Metadata documents() {
			return metadata("documents");
		}

		public Metadata documentsReportContent() {
			return metadata("documentsReportContent");
		}

		public Metadata electronicMedium() {
			return metadata("electronicMedium");
		}

		public Metadata filingSpace() {
			return metadata("filingSpace");
		}

		public Metadata folderDetails() {
			return metadata("folderDetails");
		}

		public Metadata folders() {
			return metadata("folders");
		}

		public Metadata foldersMediaTypes() {
			return metadata("foldersMediaTypes");
		}

		public Metadata foldersReportContent() {
			return metadata("foldersReportContent");
		}

		public Metadata originArchivisticStatus() {
			return metadata("originArchivisticStatus");
		}

		public Metadata pendingValidations() {
			return metadata("pendingValidations");
		}

		public Metadata processingDate() {
			return metadata("processingDate");
		}

		public Metadata processingUser() {
			return metadata("processingUser");
		}

		public Metadata status() {
			return metadata("status");
		}

		public Metadata type() {
			return metadata("type");
		}

		public Metadata uniform() {
			return metadata("uniform");
		}

		public Metadata uniformCategory() {
			return metadata("uniformCategory");
		}

		public Metadata uniformCopyRule() {
			return metadata("uniformCopyRule");
		}

		public Metadata uniformCopyType() {
			return metadata("uniformCopyType");
		}

		public Metadata uniformRule() {
			return metadata("uniformRule");
		}

		public Metadata validationDate() {
			return metadata("validationDate");
		}

		public Metadata validationUser() {
			return metadata("validationUser");
		}

		public Metadata validations() {
			return metadata("validations");
		}
	}

	public Document wrapDocument(Record record) {
		return record == null ? null : new Document(record, getTypes());
	}

	public List<Document> wrapDocuments(List<Record> records) {
		List<Document> wrapped = new ArrayList<>();
		for (Record record : records) {
			wrapped.add(new Document(record, getTypes()));
		}

		return wrapped;
	}

	public List<Document> searchDocuments(LogicalSearchQuery query) {
		return wrapDocuments(modelLayerFactory.newSearchServices().search(query));
	}

	public List<Document> searchDocuments(LogicalSearchCondition condition) {
		MetadataSchemaType type = document.schemaType();
		LogicalSearchQuery query = new LogicalSearchQuery(from(type).whereAllConditions(asList(condition)));
		return wrapDocuments(modelLayerFactory.newSearchServices().search(query));
	}

	public Document getDocument(String id) {
		return wrapDocument(get(id));
	}

	public List<Document> getDocuments(List<String> ids) {
		return wrapDocuments(get(ids));
	}

	public Document getDocumentWithLegacyId(String legacyId) {
		return wrapDocument(getByLegacyId(document.schemaType(), legacyId));
	}

	public Document newDocument() {
		return wrapDocument(create(document.schema()));
	}

	public Document newDocumentWithId(String id) {
		return wrapDocument(create(document.schema(), id));
	}

	public final SchemaTypeShortcuts_document_default document
			= new SchemaTypeShortcuts_document_default("document_default");

	public class SchemaTypeShortcuts_document_default extends SchemaTypeShortcuts {
		protected SchemaTypeShortcuts_document_default(String schemaCode) {
			super(schemaCode);
		}

		public Metadata actualDepositDate() {
			return metadata("actualDepositDate");
		}

		public Metadata actualDepositDateEntered() {
			return metadata("actualDepositDateEntered");
		}

		public Metadata actualDestructionDate() {
			return metadata("actualDestructionDate");
		}

		public Metadata actualDestructionDateEntered() {
			return metadata("actualDestructionDateEntered");
		}

		public Metadata actualTransferDate() {
			return metadata("actualTransferDate");
		}

		public Metadata actualTransferDateEntered() {
			return metadata("actualTransferDateEntered");
		}

		public Metadata administrativeUnit() {
			return metadata("administrativeUnit");
		}

		public Metadata alertUsersWhenAvailable() {
			return metadata("alertUsersWhenAvailable");
		}

		public Metadata applicableCopyRule() {
			return metadata("applicableCopyRule");
		}

		public Metadata archivisticStatus() {
			return metadata("archivisticStatus");
		}

		public Metadata author() {
			return metadata("author");
		}

		public Metadata borrowed() {
			return metadata("borrowed");
		}

		public Metadata calendarYear() {
			return metadata("calendarYear");
		}

		public Metadata calendarYearEntered() {
			return metadata("calendarYearEntered");
		}

		public Metadata category() {
			return metadata("category");
		}

		public Metadata closingDate() {
			return metadata("closingDate");
		}

		public Metadata comments() {
			return metadata("comments");
		}

		public Metadata company() {
			return metadata("company");
		}

		public Metadata content() {
			return metadata("content");
		}

		public Metadata copyStatus() {
			return metadata("copyStatus");
		}

		public Metadata description() {
			return metadata("description");
		}

		public Metadata documentType() {
			return metadata("documentType");
		}

		public Metadata expectedDepositDate() {
			return metadata("expectedDepositDate");
		}

		public Metadata expectedDestructionDate() {
			return metadata("expectedDestructionDate");
		}

		public Metadata expectedTransferDate() {
			return metadata("expectedTransferDate");
		}

		public Metadata filingSpace() {
			return metadata("filingSpace");
		}

		public Metadata folder() {
			return metadata("folder");
		}

		public Metadata inheritedRetentionRule() {
			return metadata("inheritedRetentionRule");
		}

		public Metadata keywords() {
			return metadata("keywords");
		}

		public Metadata mainCopyRule() {
			return metadata("mainCopyRule");
		}

		public Metadata mainCopyRuleIdEntered() {
			return metadata("mainCopyRuleIdEntered");
		}

		public Metadata openingDate() {
			return metadata("openingDate");
		}

		public Metadata published() {
			return metadata("published");
		}

		public Metadata retentionRule() {
			return metadata("retentionRule");
		}

		public Metadata sameInactiveFateAsFolder() {
			return metadata("sameInactiveFateAsFolder");
		}

		public Metadata sameSemiActiveFateAsFolder() {
			return metadata("sameSemiActiveFateAsFolder");
		}

		public Metadata subject() {
			return metadata("subject");
		}

		public Metadata type() {
			return metadata("type");
		}

		public Metadata version() {
			return metadata("version");
		}
	}

	public Email wrapEmail(Record record) {
		return record == null ? null : new Email(record, getTypes());
	}

	public List<Email> wrapEmails(List<Record> records) {
		List<Email> wrapped = new ArrayList<>();
		for (Record record : records) {
			wrapped.add(new Email(record, getTypes()));
		}

		return wrapped;
	}

	public List<Email> searchEmails(LogicalSearchQuery query) {
		return wrapEmails(modelLayerFactory.newSearchServices().search(query));
	}

	public List<Email> searchEmails(LogicalSearchCondition condition) {
		MetadataSchemaType type = document.schemaType();
		LogicalSearchQuery query = new LogicalSearchQuery(from(type).whereAllConditions(asList(condition)));
		return wrapEmails(modelLayerFactory.newSearchServices().search(query));
	}

	public Email getEmail(String id) {
		return wrapEmail(get(id));
	}

	public List<Email> getEmails(List<String> ids) {
		return wrapEmails(get(ids));
	}

	public Email getEmailWithLegacyId(String legacyId) {
		return wrapEmail(getByLegacyId(document.schemaType(), legacyId));
	}

	public Email newEmail() {
		return wrapEmail(create(document_email.schema()));
	}

	public Email newEmailWithId(String id) {
		return wrapEmail(create(document_email.schema(), id));
	}

	public final SchemaTypeShortcuts_document_email document_email
			= new SchemaTypeShortcuts_document_email("document_email");

	public class SchemaTypeShortcuts_document_email extends SchemaTypeShortcuts_document_default {
		protected SchemaTypeShortcuts_document_email(String schemaCode) {
			super(schemaCode);
		}

		public Metadata emailAttachmentsList() {
			return metadata("emailAttachmentsList");
		}

		public Metadata emailBCCTo() {
			return metadata("emailBCCTo");
		}

		public Metadata emailCCTo() {
			return metadata("emailCCTo");
		}

		public Metadata emailCompany() {
			return metadata("emailCompany");
		}

		public Metadata emailContent() {
			return metadata("emailContent");
		}

		public Metadata emailFrom() {
			return metadata("emailFrom");
		}

		public Metadata emailInNameOf() {
			return metadata("emailInNameOf");
		}

		public Metadata emailObject() {
			return metadata("emailObject");
		}

		public Metadata emailReceivedOn() {
			return metadata("emailReceivedOn");
		}

		public Metadata emailSentOn() {
			return metadata("emailSentOn");
		}

		public Metadata emailTo() {
			return metadata("emailTo");
		}

		public Metadata subjectToBroadcastRule() {
			return metadata("subjectToBroadcastRule");
		}
	}

	public Printable wrapReportConfig(Record record) {
		return record == null ? null : new Printable(record, getTypes());
	}

	public List<Printable> wrapReportConfigs(List<Record> records) {
		List<Printable> wrapped = new ArrayList<>();
		for (Record record : records) {
			wrapped.add(new Printable(record, getTypes()));
		}

		return wrapped;
	}

	public List<Printable> searchReportConfigs(LogicalSearchQuery query) {
		return wrapReportConfigs(modelLayerFactory.newSearchServices().search(query));
	}

	public List<Printable> searchReportConfigs(LogicalSearchCondition condition) {
		MetadataSchemaType type = reportsrecords.schemaType();
		LogicalSearchQuery query = new LogicalSearchQuery(from(type).whereAllConditions(asList(condition)));
		return wrapReportConfigs(modelLayerFactory.newSearchServices().search(query));
	}

	public Printable getReportConfig(String id) {
		return wrapReportConfig(get(id));
	}

	public List<Printable> getReportConfigs(List<String> ids) {
		return wrapReportConfigs(get(ids));
	}

	public Printable getReportConfigWithLegacyId(String legacyId) {
		return wrapReportConfig(getByLegacyId(reportsrecords.schemaType(), legacyId));
	}

	public Printable newReportConfig() {
		return wrapReportConfig(create(reportsrecords.schema()));
	}

	public Printable newReportConfigWithId(String id) {
		return wrapReportConfig(create(reportsrecords.schema(), id));
	}

	public final SchemaTypeShortcuts_reportsrecords_default reportsrecords
			= new SchemaTypeShortcuts_reportsrecords_default("reportsrecords_default");

	public class SchemaTypeShortcuts_reportsrecords_default extends SchemaTypeShortcuts {
		protected SchemaTypeShortcuts_reportsrecords_default(String schemaCode) {
			super(schemaCode);
		}

		public Metadata NULL() {
			return metadata("NULL");
		}

		public Metadata Height() {
			return metadata("192");
		}

		public Metadata Width() {
			return metadata("384");
		}
	}

	public Folder wrapFolder(Record record) {
		return record == null ? null : new Folder(record, getTypes());
	}

	public List<Folder> wrapFolders(List<Record> records) {
		List<Folder> wrapped = new ArrayList<>();
		for (Record record : records) {
			wrapped.add(new Folder(record, getTypes()));
		}

		return wrapped;
	}

	public List<Folder> searchFolders(LogicalSearchQuery query) {
		return wrapFolders(modelLayerFactory.newSearchServices().search(query));
	}

	public List<Folder> searchFolders(LogicalSearchCondition condition) {
		MetadataSchemaType type = folder.schemaType();
		LogicalSearchQuery query = new LogicalSearchQuery(from(type).whereAllConditions(asList(condition)));
		return wrapFolders(modelLayerFactory.newSearchServices().search(query));
	}

	public Folder getFolder(String id) {
		return wrapFolder(get(id));
	}

	public List<Folder> getFolders(List<String> ids) {
		return wrapFolders(get(ids));
	}

	public Folder getFolderWithLegacyId(String legacyId) {
		return wrapFolder(getByLegacyId(folder.schemaType(), legacyId));
	}

	public Folder newFolder() {
		return wrapFolder(create(folder.schema()));
	}

	public Folder newFolderWithId(String id) {
		return wrapFolder(create(folder.schema(), id));
	}

	public final SchemaTypeShortcuts_folder_default folder
			= new SchemaTypeShortcuts_folder_default("folder_default");

	public class SchemaTypeShortcuts_folder_default extends SchemaTypeShortcuts {
		protected SchemaTypeShortcuts_folder_default(String schemaCode) {
			super(schemaCode);
		}

		public Metadata activeRetentionPeriodCode() {
			return metadata("activeRetentionPeriodCode");
		}

		public Metadata activeRetentionType() {
			return metadata("activeRetentionType");
		}

		public Metadata actualDepositDate() {
			return metadata("actualDepositDate");
		}

		public Metadata actualDestructionDate() {
			return metadata("actualDestructionDate");
		}

		public Metadata actualTransferDate() {
			return metadata("actualTransferDate");
		}

		public Metadata administrativeUnit() {
			return metadata("administrativeUnit");
		}

		public Metadata administrativeUnitAncestors() {
			return metadata("administrativeUnitAncestors");
		}

		public Metadata administrativeUnitEntered() {
			return metadata("administrativeUnitEntered");
		}

		public Metadata alertUsersWhenAvailable() {
			return metadata("alertUsersWhenAvailable");
		}

		public Metadata applicableCopyRule() {
			return metadata("applicableCopyRule");
		}

		public Metadata archivisticStatus() {
			return metadata("archivisticStatus");
		}

		public Metadata borrowDate() {
			return metadata("borrowDate");
		}

		public Metadata borrowPreviewReturnDate() {
			return metadata("borrowPreviewReturnDate");
		}

		public Metadata borrowReturnDate() {
			return metadata("borrowReturnDate");
		}

		public Metadata borrowUser() {
			return metadata("borrowUser");
		}

		public Metadata borrowUserEntered() {
			return metadata("borrowUserEntered");
		}

		public Metadata borrowed() {
			return metadata("borrowed");
		}

		public Metadata borrowingType() {
			return metadata("borrowingType");
		}

		public Metadata calendarYear() {
			return metadata("calendarYear");
		}

		public Metadata calendarYearEntered() {
			return metadata("calendarYearEntered");
		}

		public Metadata category() {
			return metadata("category");
		}

		public Metadata categoryCode() {
			return metadata("categoryCode");
		}

		public Metadata categoryEntered() {
			return metadata("categoryEntered");
		}

		public Metadata closingDate() {
			return metadata("closingDate");
		}

		public Metadata comments() {
			return metadata("comments");
		}

		public Metadata container() {
			return metadata("container");
		}

		public Metadata copyRulesExpectedDepositDates() {
			return metadata("copyRulesExpectedDepositDates");
		}

		public Metadata copyRulesExpectedDestructionDates() {
			return metadata("copyRulesExpectedDestructionDates");
		}

		public Metadata copyRulesExpectedTransferDates() {
			return metadata("copyRulesExpectedTransferDates");
		}

		public Metadata copyStatus() {
			return metadata("copyStatus");
		}

		public Metadata copyStatusEntered() {
			return metadata("copyStatusEntered");
		}

		public Metadata decommissioningDate() {
			return metadata("decommissioningDate");
		}

		public Metadata description() {
			return metadata("description");
		}

		public Metadata enteredClosingDate() {
			return metadata("enteredClosingDate");
		}

		public Metadata manualExpectedDepositDate() {
			return metadata(Folder.MANUAL_EXPECTED_DEPOSIT_DATE);
		}

		public Metadata manualExpectedDestructionDate() {
			return metadata(Folder.MANUAL_EXPECTED_DESTRUCTION_DATE);
		}

		public Metadata manualExpectedTransferDate() {
			return metadata(Folder.MANUAL_EXPECTED_TRANSFER_DATE);
		}

		public Metadata expectedDepositDate() {
			return metadata("expectedDepositDate");
		}

		public Metadata expectedDestructionDate() {
			return metadata("expectedDestructionDate");
		}

		public Metadata expectedTransferDate() {
			return metadata("expectedTransferDate");
		}

		public Metadata filingSpace() {
			return metadata("filingSpace");
		}

		public Metadata filingSpaceCode() {
			return metadata("filingSpaceCode");
		}

		public Metadata filingSpaceEntered() {
			return metadata("filingSpaceEntered");
		}

		public Metadata folderType() {
			return metadata("folderType");
		}

		public Metadata inactiveDisposalType() {
			return metadata("inactiveDisposalType");
		}

		public Metadata keywords() {
			return metadata("keywords");
		}

		public Metadata linearSize() {
			return metadata("linearSize");
		}

		public Metadata mainCopyRule() {
			return metadata("mainCopyRule");
		}

		public Metadata mainCopyRuleIdEntered() {
			return metadata("mainCopyRuleIdEntered");
		}

		public Metadata mediaType() {
			return metadata("mediaType");
		}

		public Metadata mediumTypes() {
			return metadata("mediumTypes");
		}

		public Metadata openingDate() {
			return metadata("openingDate");
		}

		public Metadata parentFolder() {
			return metadata("parentFolder");
		}

		public Metadata permissionStatus() {
			return metadata("permissionStatus");
		}

		public Metadata retentionRule() {
			return metadata("retentionRule");
		}

		public Metadata retentionRuleEntered() {
			return metadata("retentionRuleEntered");
		}

		public Metadata ruleAdminUnit() {
			return metadata("ruleAdminUnit");
		}

		public Metadata semiactiveRetentionPeriodCode() {
			return metadata("semiactiveRetentionPeriodCode");
		}

		public Metadata semiactiveRetentionType() {
			return metadata("semiactiveRetentionType");
		}

		public Metadata type() {
			return metadata("type");
		}

		public Metadata uniformSubdivision() {
			return metadata("uniformSubdivision");
		}

		public Metadata uniformSubdivisionEntered() {
			return metadata("uniformSubdivisionEntered");
		}
	}

	public RetentionRule wrapRetentionRule(Record record) {
		return record == null ? null : new RetentionRule(record, getTypes());
	}

	public List<RetentionRule> wrapRetentionRules(List<Record> records) {
		List<RetentionRule> wrapped = new ArrayList<>();
		for (Record record : records) {
			wrapped.add(new RetentionRule(record, getTypes()));
		}

		return wrapped;
	}

	public List<RetentionRule> searchRetentionRules(LogicalSearchQuery query) {
		return wrapRetentionRules(modelLayerFactory.newSearchServices().search(query));
	}

	public List<RetentionRule> searchRetentionRules(LogicalSearchCondition condition) {
		MetadataSchemaType type = retentionRule.schemaType();
		LogicalSearchQuery query = new LogicalSearchQuery(from(type).whereAllConditions(asList(condition)));
		return wrapRetentionRules(modelLayerFactory.newSearchServices().search(query));
	}

	public RetentionRule getRetentionRule(String id) {
		return wrapRetentionRule(get(id));
	}

	public List<RetentionRule> getRetentionRules(List<String> ids) {
		return wrapRetentionRules(get(ids));
	}

	public RetentionRule getRetentionRuleWithCode(String code) {
		return wrapRetentionRule(getByCode(retentionRule.schemaType(), code));
	}

	public RetentionRule getRetentionRuleWithLegacyId(String legacyId) {
		return wrapRetentionRule(getByLegacyId(retentionRule.schemaType(), legacyId));
	}

	public RetentionRule newRetentionRule() {
		return wrapRetentionRule(create(retentionRule.schema()));
	}

	public RetentionRule newRetentionRuleWithId(String id) {
		return wrapRetentionRule(create(retentionRule.schema(), id));
	}

	public final SchemaTypeShortcuts_retentionRule_default retentionRule
			= new SchemaTypeShortcuts_retentionRule_default("retentionRule_default");

	public class SchemaTypeShortcuts_retentionRule_default extends SchemaTypeShortcuts {
		protected SchemaTypeShortcuts_retentionRule_default(String schemaCode) {
			super(schemaCode);
		}

		public Metadata administrativeUnits() {
			return metadata("administrativeUnits");
		}

		public Metadata approvalDate() {
			return metadata("approvalDate");
		}

		public Metadata approved() {
			return metadata("approved");
		}

		public Metadata code() {
			return metadata("code");
		}

		public Metadata confidentialDocuments() {
			return metadata("confidentialDocuments");
		}

		public Metadata copyRetentionRules() {
			return metadata("copyRetentionRules");
		}

		public Metadata copyRulesComment() {
			return metadata("copyRulesComment");
		}

		public Metadata corpus() {
			return metadata("corpus");
		}

		public Metadata corpusRuleNumber() {
			return metadata("corpusRuleNumber");
		}

		public Metadata description() {
			return metadata("description");
		}

		public Metadata documentCopyRetentionRules() {
			return metadata("documentCopyRetentionRules");
		}

		public Metadata documentTypes() {
			return metadata("documentTypes");
		}

		public Metadata documentTypesDetails() {
			return metadata("documentTypesDetails");
		}

		public Metadata essentialDocuments() {
			return metadata("essentialDocuments");
		}

		public Metadata folderTypes() {
			return metadata("folderTypes");
		}

		public Metadata generalComment() {
			return metadata("generalComment");
		}

		public Metadata history() {
			return metadata("history");
		}

		public Metadata juridicReference() {
			return metadata("juridicReference");
		}

		public Metadata keywords() {
			return metadata("keywords");
		}

		public Metadata principalDefaultDocumentCopyRetentionRule() {
			return metadata("principalDefaultDocumentCopyRetentionRule");
		}

		public Metadata responsibleAdministrativeUnits() {
			return metadata("responsibleAdministrativeUnits");
		}

		public Metadata scope() {
			return metadata("scope");
		}

		public Metadata secondaryDefaultDocumentCopyRetentionRule() {
			return metadata("secondaryDefaultDocumentCopyRetentionRule");
		}
	}

	public StorageSpace wrapStorageSpace(Record record) {
		return record == null ? null : new StorageSpace(record, getTypes());
	}

	public List<StorageSpace> wrapStorageSpaces(List<Record> records) {
		List<StorageSpace> wrapped = new ArrayList<>();
		for (Record record : records) {
			wrapped.add(new StorageSpace(record, getTypes()));
		}

		return wrapped;
	}

	public List<StorageSpace> searchStorageSpaces(LogicalSearchQuery query) {
		return wrapStorageSpaces(modelLayerFactory.newSearchServices().search(query));
	}

	public List<StorageSpace> searchStorageSpaces(LogicalSearchCondition condition) {
		MetadataSchemaType type = storageSpace.schemaType();
		LogicalSearchQuery query = new LogicalSearchQuery(from(type).whereAllConditions(asList(condition)));
		return wrapStorageSpaces(modelLayerFactory.newSearchServices().search(query));
	}

	public StorageSpace getStorageSpace(String id) {
		return wrapStorageSpace(get(id));
	}

	public List<StorageSpace> getStorageSpaces(List<String> ids) {
		return wrapStorageSpaces(get(ids));
	}

	public StorageSpace getStorageSpaceWithCode(String code) {
		return wrapStorageSpace(getByCode(storageSpace.schemaType(), code));
	}

	public StorageSpace getStorageSpaceWithLegacyId(String legacyId) {
		return wrapStorageSpace(getByLegacyId(storageSpace.schemaType(), legacyId));
	}

	public StorageSpace newStorageSpace() {
		return wrapStorageSpace(create(storageSpace.schema()));
	}

	public StorageSpace newStorageSpaceWithId(String id) {
		return wrapStorageSpace(create(storageSpace.schema(), id));
	}

	public final SchemaTypeShortcuts_storageSpace_default storageSpace
			= new SchemaTypeShortcuts_storageSpace_default("storageSpace_default");

	public class SchemaTypeShortcuts_storageSpace_default extends SchemaTypeShortcuts {
		protected SchemaTypeShortcuts_storageSpace_default(String schemaCode) {
			super(schemaCode);
		}

		public Metadata capacity() {
			return metadata("capacity");
		}

		public Metadata code() {
			return metadata("code");
		}

		public Metadata comments() {
			return metadata("comments");
		}

		public Metadata decommissioningType() {
			return metadata("decommissioningType");
		}

		public Metadata description() {
			return metadata("description");
		}

		public Metadata parentStorageSpace() {
			return metadata("parentStorageSpace");
		}

		public Metadata type() {
			return metadata("type");
		}
	}

	public UniformSubdivision wrapUniformSubdivision(Record record) {
		return record == null ? null : new UniformSubdivision(record, getTypes());
	}

	public List<UniformSubdivision> wrapUniformSubdivisions(List<Record> records) {
		List<UniformSubdivision> wrapped = new ArrayList<>();
		for (Record record : records) {
			wrapped.add(new UniformSubdivision(record, getTypes()));
		}

		return wrapped;
	}

	public List<UniformSubdivision> searchUniformSubdivisions(LogicalSearchQuery query) {
		return wrapUniformSubdivisions(modelLayerFactory.newSearchServices().search(query));
	}

	public List<UniformSubdivision> searchUniformSubdivisions(LogicalSearchCondition condition) {
		MetadataSchemaType type = uniformSubdivision.schemaType();
		LogicalSearchQuery query = new LogicalSearchQuery(from(type).whereAllConditions(asList(condition)));
		return wrapUniformSubdivisions(modelLayerFactory.newSearchServices().search(query));
	}

	public UniformSubdivision getUniformSubdivision(String id) {
		return wrapUniformSubdivision(get(id));
	}

	public List<UniformSubdivision> getUniformSubdivisions(List<String> ids) {
		return wrapUniformSubdivisions(get(ids));
	}

	public UniformSubdivision getUniformSubdivisionWithCode(String code) {
		return wrapUniformSubdivision(getByCode(uniformSubdivision.schemaType(), code));
	}

	public UniformSubdivision getUniformSubdivisionWithLegacyId(String legacyId) {
		return wrapUniformSubdivision(getByLegacyId(uniformSubdivision.schemaType(), legacyId));
	}

	public UniformSubdivision newUniformSubdivision() {
		return wrapUniformSubdivision(create(uniformSubdivision.schema()));
	}

	public UniformSubdivision newUniformSubdivisionWithId(String id) {
		return wrapUniformSubdivision(create(uniformSubdivision.schema(), id));
	}

	public final SchemaTypeShortcuts_uniformSubdivision_default uniformSubdivision
			= new SchemaTypeShortcuts_uniformSubdivision_default("uniformSubdivision_default");

	public class SchemaTypeShortcuts_uniformSubdivision_default extends SchemaTypeShortcuts {
		protected SchemaTypeShortcuts_uniformSubdivision_default(String schemaCode) {
			super(schemaCode);
		}

		public Metadata code() {
			return metadata("code");
		}

		public Metadata comments() {
			return metadata("comments");
		}

		public Metadata description() {
			return metadata("description");
		}

		public Metadata retentionRule() {
			return metadata("retentionRule");
		}
	}

	public PrintableLabel wrapRMReport(Record record) {
		return record == null ? null : new PrintableLabel(record, getTypes());
	}

	public List<PrintableLabel> wrapRMReports(List<Record> records) {
		List<PrintableLabel> wrapped = new ArrayList<>();
		for (Record record : records) {
			wrapped.add(new PrintableLabel(record, getTypes()));
		}

		return wrapped;
	}

	public List<PrintableLabel> searchRMReports(LogicalSearchQuery query) {
		return wrapRMReports(modelLayerFactory.newSearchServices().search(query));
	}

	public List<PrintableLabel> searchRMReports(LogicalSearchCondition condition) {
		MetadataSchemaType type = reportsrecords.schemaType();
		LogicalSearchQuery query = new LogicalSearchQuery(from(type).whereAllConditions(asList(condition)));
		return wrapRMReports(modelLayerFactory.newSearchServices().search(query));
	}

	public PrintableLabel getRMReport(String id) {
		return wrapRMReport(get(id));
	}

	public List<PrintableLabel> getRMReports(List<String> ids) {
		return wrapRMReports(get(ids));
	}

	public PrintableLabel getRMReportWithLegacyId(String legacyId) {
		return wrapRMReport(getByLegacyId(reportsrecords.schemaType(), legacyId));
	}

	public PrintableLabel newRMReport() {
		return wrapRMReport(create(reportsrecords_label.schema()));
	}

	public PrintableLabel newRMReportWithId(String id) {
		return wrapRMReport(create(reportsrecords_label.schema(), id));
	}

	public final SchemaTypeShortcuts_reportsrecords_label reportsrecords_label
			= new SchemaTypeShortcuts_reportsrecords_label(PrintableLabel.SCHEMA_NAME);

	public class SchemaTypeShortcuts_reportsrecords_label extends SchemaTypeShortcuts_reportsrecords_default {
		protected SchemaTypeShortcuts_reportsrecords_label(String schemaCode) {
			super(schemaCode);
		}

		public Metadata colonne() {
			return metadata("colonne");
		}

		public Metadata height() {
			return metadata("height");
		}

		public Metadata ligne() {
			return metadata("ligne");
		}

		public Metadata typelabel() {
			return metadata("typelabel");
		}

		public Metadata width() {
			return metadata("width");
		}
	}
/** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** **/
	// Auto-generated methods by GenerateHelperClassAcceptTest -- end
	/** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** **/

}
