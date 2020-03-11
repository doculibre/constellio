package com.constellio.app.modules.rm.services;

import com.constellio.app.modules.rm.model.ExternalLinkType;
import com.constellio.app.modules.rm.wrappers.AdministrativeUnit;
import com.constellio.app.modules.rm.wrappers.Cart;
import com.constellio.app.modules.rm.wrappers.Category;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.DecommissioningList;
import com.constellio.app.modules.rm.wrappers.Document;
import com.constellio.app.modules.rm.wrappers.Email;
import com.constellio.app.modules.rm.wrappers.ExternalLink;
import com.constellio.app.modules.rm.wrappers.Folder;
import com.constellio.app.modules.rm.wrappers.PrintableLabel;
import com.constellio.app.modules.rm.wrappers.PrintableReport;
import com.constellio.app.modules.rm.wrappers.RMTask;
import com.constellio.app.modules.rm.wrappers.RMUserFolder;
import com.constellio.app.modules.rm.wrappers.RetentionRule;
import com.constellio.app.modules.rm.wrappers.SIParchive;
import com.constellio.app.modules.rm.wrappers.StorageSpace;
import com.constellio.app.modules.rm.wrappers.UniformSubdivision;
import com.constellio.app.modules.rm.wrappers.UserFunction;
import com.constellio.app.modules.rm.wrappers.triggers.Trigger;
import com.constellio.app.modules.rm.wrappers.triggers.TriggerActionType;
import com.constellio.app.modules.rm.wrappers.triggers.TriggerType;
import com.constellio.app.modules.rm.wrappers.triggers.actions.MoveInFolderTriggerAction;
import com.constellio.app.modules.rm.wrappers.type.DocumentType;
import com.constellio.app.modules.rm.wrappers.type.FolderType;
import com.constellio.app.modules.rm.wrappers.type.StorageSpaceType;
import com.constellio.app.modules.rm.wrappers.type.YearType;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.search.query.logical.LogicalSearchQuery;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;

public class RMGeneratedSchemaRecordsServices extends SchemasRecordsServices {

	public RMGeneratedSchemaRecordsServices(String collection,
											ModelLayerFactory modelLayerFactory) {
		super(collection, modelLayerFactory);
	}

	public RMGeneratedSchemaRecordsServices(String collection,
											ModelLayerFactory modelLayerFactory, Locale locale) {
		super(collection, modelLayerFactory, locale);
	}

	/** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** **/
	// Auto-generated methods by GenerateHelperClassAcceptTest -- start
	/** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** **/



	public AdministrativeUnit wrapAdministrativeUnit(Record record) {
		return record == null ? null : new AdministrativeUnit(record, getTypes(), locale);
	}

	public List<AdministrativeUnit> wrapAdministrativeUnits(List<Record> records) {
		List<AdministrativeUnit> wrapped = new ArrayList<>();
		for (Record record : records) {
			wrapped.add(new AdministrativeUnit(record, getTypes(), locale));
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

	public Iterator<AdministrativeUnit> administrativeUnitIterator() {
		return iterateFromCache(administrativeUnit.schemaType(), this::wrapAdministrativeUnit);
	}

	public Stream<AdministrativeUnit> administrativeUnitStream() {
		return streamFromCache(administrativeUnit.schemaType(), this::wrapAdministrativeUnit);
	}

	public AdministrativeUnit getAdministrativeUnit(String id) {
		return wrapAdministrativeUnit(get(administrativeUnit.schemaType(), id));
	}

	public List<AdministrativeUnit> getAdministrativeUnits(List<String> ids) {
		return wrapAdministrativeUnits(get(administrativeUnit.schemaType(), ids));
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

		public Metadata abbreviation() {
			return metadata("abbreviation");
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

		public Metadata functionUsers() {
			return metadata("functionUsers");
		}

		public Metadata functions() {
			return metadata("functions");
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

	public Iterator<Cart> cartIterator() {
		return iterateFromCache(cart.schemaType(), this::wrapCart);
	}

	public Stream<Cart> cartStream() {
		return streamFromCache(cart.schemaType(), this::wrapCart);
	}

	public Cart getCart(String id) {
		return wrapCart(get(cart.schemaType(), id));
	}

	public List<Cart> getCarts(List<String> ids) {
		return wrapCarts(get(cart.schemaType(), ids));
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

		public Metadata owner() {
			return metadata("owner");
		}

		public Metadata sharedWithUsers() {
			return metadata("sharedWithUsers");
		}
	}
	public Category wrapCategory(Record record) {
		return record == null ? null : new Category(record, getTypes(), locale);
	}

	public List<Category> wrapCategorys(List<Record> records) {
		List<Category> wrapped = new ArrayList<>();
		for (Record record : records) {
			wrapped.add(new Category(record, getTypes(), locale));
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

	public Iterator<Category> categoryIterator() {
		return iterateFromCache(category.schemaType(), this::wrapCategory);
	}

	public Stream<Category> categoryStream() {
		return streamFromCache(category.schemaType(), this::wrapCategory);
	}

	public Category getCategory(String id) {
		return wrapCategory(get(category.schemaType(), id));
	}

	public List<Category> getCategorys(List<String> ids) {
		return wrapCategorys(get(category.schemaType(), ids));
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

		public Metadata abbreviation() {
			return metadata("abbreviation");
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

		public Metadata deactivate() {
			return metadata("deactivate");
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

	public Iterator<ContainerRecord> containerRecordIterator() {
		return iterateFromCache(containerRecord.schemaType(), this::wrapContainerRecord);
	}

	public Stream<ContainerRecord> containerRecordStream() {
		return streamFromCache(containerRecord.schemaType(), this::wrapContainerRecord);
	}

	public ContainerRecord getContainerRecord(String id) {
		return wrapContainerRecord(get(containerRecord.schemaType(), id));
	}

	public List<ContainerRecord> getContainerRecords(List<String> ids) {
		return wrapContainerRecords(get(containerRecord.schemaType(), ids));
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

		public Metadata administrativeUnits() {
			return metadata("administrativeUnits");
		}

		public Metadata availableSize() {
			return metadata("availableSize");
		}

		public Metadata borrowDate() {
			return metadata("borrowDate");
		}

		public Metadata borrowReturnDate() {
			return metadata("borrowReturnDate");
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

		public Metadata documentResponsible() {
			return metadata("documentResponsible");
		}

		public Metadata favorites() {
			return metadata("favorites");
		}

		public Metadata filingSpace() {
			return metadata("filingSpace");
		}

		public Metadata fillRatioEntered() {
			return metadata("fillRatioEntered");
		}

		public Metadata firstDepositReportDate() {
			return metadata("firstDepositReportDate");
		}

		public Metadata firstTransferReportDate() {
			return metadata("firstTransferReportDate");
		}

		public Metadata full() {
			return metadata("full");
		}

		public Metadata identifier() {
			return metadata("identifier");
		}

		public Metadata linearSize() {
			return metadata("linearSize");
		}

		public Metadata linearSizeEntered() {
			return metadata("linearSizeEntered");
		}

		public Metadata linearSizeSum() {
			return metadata("linearSizeSum");
		}

		public Metadata localization() {
			return metadata("localization");
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

	public Iterator<DocumentType> documentTypeIterator() {
		return iterateFromCache(ddvDocumentType.schemaType(), this::wrapDocumentType);
	}

	public Stream<DocumentType> documentTypeStream() {
		return streamFromCache(ddvDocumentType.schemaType(), this::wrapDocumentType);
	}

	public DocumentType getDocumentType(String id) {
		return wrapDocumentType(get(ddvDocumentType.schemaType(), id));
	}

	public List<DocumentType> getDocumentTypes(List<String> ids) {
		return wrapDocumentTypes(get(ddvDocumentType.schemaType(), ids));
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

	public Iterator<FolderType> folderTypeIterator() {
		return iterateFromCache(ddvFolderType.schemaType(), this::wrapFolderType);
	}

	public Stream<FolderType> folderTypeStream() {
		return streamFromCache(ddvFolderType.schemaType(), this::wrapFolderType);
	}

	public FolderType getFolderType(String id) {
		return wrapFolderType(get(ddvFolderType.schemaType(), id));
	}

	public List<FolderType> getFolderTypes(List<String> ids) {
		return wrapFolderTypes(get(ddvFolderType.schemaType(), ids));
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
	public StorageSpaceType wrapStorageSpaceType(Record record) {
		return record == null ? null : new StorageSpaceType(record, getTypes());
	}

	public List<StorageSpaceType> wrapStorageSpaceTypes(List<Record> records) {
		List<StorageSpaceType> wrapped = new ArrayList<>();
		for (Record record : records) {
			wrapped.add(new StorageSpaceType(record, getTypes()));
		}

		return wrapped;
	}

	public List<StorageSpaceType> searchStorageSpaceTypes(LogicalSearchQuery query) {
		return wrapStorageSpaceTypes(modelLayerFactory.newSearchServices().search(query));
	}

	public List<StorageSpaceType> searchStorageSpaceTypes(LogicalSearchCondition condition) {
		MetadataSchemaType type = ddvStorageSpaceType.schemaType();
		LogicalSearchQuery query = new LogicalSearchQuery(from(type).whereAllConditions(asList(condition)));
		return wrapStorageSpaceTypes(modelLayerFactory.newSearchServices().search(query));
	}

	public Iterator<StorageSpaceType> storageSpaceTypeIterator() {
		return iterateFromCache(ddvStorageSpaceType.schemaType(), this::wrapStorageSpaceType);
	}

	public Stream<StorageSpaceType> storageSpaceTypeStream() {
		return streamFromCache(ddvStorageSpaceType.schemaType(), this::wrapStorageSpaceType);
	}

	public StorageSpaceType getStorageSpaceType(String id) {
		return wrapStorageSpaceType(get(ddvStorageSpaceType.schemaType(), id));
	}

	public List<StorageSpaceType> getStorageSpaceTypes(List<String> ids) {
		return wrapStorageSpaceTypes(get(ddvStorageSpaceType.schemaType(), ids));
	}

	public StorageSpaceType getStorageSpaceTypeWithCode(String code) {
		return wrapStorageSpaceType(getByCode(ddvStorageSpaceType.schemaType(), code));
	}

	public StorageSpaceType getStorageSpaceTypeWithLegacyId(String legacyId) {
		return wrapStorageSpaceType(getByLegacyId(ddvStorageSpaceType.schemaType(), legacyId));
	}

	public StorageSpaceType newStorageSpaceType() {
		return wrapStorageSpaceType(create(ddvStorageSpaceType.schema()));
	}

	public StorageSpaceType newStorageSpaceTypeWithId(String id) {
		return wrapStorageSpaceType(create(ddvStorageSpaceType.schema(), id));
	}

	public final SchemaTypeShortcuts_ddvStorageSpaceType_default ddvStorageSpaceType
			= new SchemaTypeShortcuts_ddvStorageSpaceType_default("ddvStorageSpaceType_default");
	public class SchemaTypeShortcuts_ddvStorageSpaceType_default extends SchemaTypeShortcuts {
		protected SchemaTypeShortcuts_ddvStorageSpaceType_default(String schemaCode) {
			super(schemaCode);
		}

		public Metadata linkedSchema() {
			return metadata("linkedSchema");
		}
	}
	public UserFunction wrapUserFunction(Record record) {
		return record == null ? null : new UserFunction(record, getTypes());
	}

	public List<UserFunction> wrapUserFunctions(List<Record> records) {
		List<UserFunction> wrapped = new ArrayList<>();
		for (Record record : records) {
			wrapped.add(new UserFunction(record, getTypes()));
		}

		return wrapped;
	}

	public List<UserFunction> searchUserFunctions(LogicalSearchQuery query) {
		return wrapUserFunctions(modelLayerFactory.newSearchServices().search(query));
	}

	public List<UserFunction> searchUserFunctions(LogicalSearchCondition condition) {
		MetadataSchemaType type = ddvUserFunction.schemaType();
		LogicalSearchQuery query = new LogicalSearchQuery(from(type).whereAllConditions(asList(condition)));
		return wrapUserFunctions(modelLayerFactory.newSearchServices().search(query));
	}

	public Iterator<UserFunction> userFunctionIterator() {
		return iterateFromCache(ddvUserFunction.schemaType(), this::wrapUserFunction);
	}

	public Stream<UserFunction> userFunctionStream() {
		return streamFromCache(ddvUserFunction.schemaType(), this::wrapUserFunction);
	}

	public UserFunction getUserFunction(String id) {
		return wrapUserFunction(get(ddvUserFunction.schemaType(), id));
	}

	public List<UserFunction> getUserFunctions(List<String> ids) {
		return wrapUserFunctions(get(ddvUserFunction.schemaType(), ids));
	}

	public UserFunction getUserFunctionWithCode(String code) {
		return wrapUserFunction(getByCode(ddvUserFunction.schemaType(), code));
	}

	public UserFunction getUserFunctionWithLegacyId(String legacyId) {
		return wrapUserFunction(getByLegacyId(ddvUserFunction.schemaType(), legacyId));
	}

	public UserFunction newUserFunction() {
		return wrapUserFunction(create(ddvUserFunction.schema()));
	}

	public UserFunction newUserFunctionWithId(String id) {
		return wrapUserFunction(create(ddvUserFunction.schema(), id));
	}

	public final SchemaTypeShortcuts_ddvUserFunction_default ddvUserFunction
			= new SchemaTypeShortcuts_ddvUserFunction_default("ddvUserFunction_default");
	public class SchemaTypeShortcuts_ddvUserFunction_default extends SchemaTypeShortcuts {
		protected SchemaTypeShortcuts_ddvUserFunction_default(String schemaCode) {
			super(schemaCode);
		}
	}
	public YearType wrapYearType(Record record) {
		return record == null ? null : new YearType(record, getTypes());
	}

	public List<YearType> wrapYearTypes(List<Record> records) {
		List<YearType> wrapped = new ArrayList<>();
		for (Record record : records) {
			wrapped.add(new YearType(record, getTypes()));
		}

		return wrapped;
	}

	public List<YearType> searchYearTypes(LogicalSearchQuery query) {
		return wrapYearTypes(modelLayerFactory.newSearchServices().search(query));
	}

	public List<YearType> searchYearTypes(LogicalSearchCondition condition) {
		MetadataSchemaType type = ddvYearType.schemaType();
		LogicalSearchQuery query = new LogicalSearchQuery(from(type).whereAllConditions(asList(condition)));
		return wrapYearTypes(modelLayerFactory.newSearchServices().search(query));
	}

	public Iterator<YearType> yearTypeIterator() {
		return iterateFromCache(ddvYearType.schemaType(), this::wrapYearType);
	}

	public Stream<YearType> yearTypeStream() {
		return streamFromCache(ddvYearType.schemaType(), this::wrapYearType);
	}

	public YearType getYearType(String id) {
		return wrapYearType(get(ddvYearType.schemaType(), id));
	}

	public List<YearType> getYearTypes(List<String> ids) {
		return wrapYearTypes(get(ddvYearType.schemaType(), ids));
	}

	public YearType getYearTypeWithCode(String code) {
		return wrapYearType(getByCode(ddvYearType.schemaType(), code));
	}

	public YearType getYearTypeWithLegacyId(String legacyId) {
		return wrapYearType(getByLegacyId(ddvYearType.schemaType(), legacyId));
	}

	public YearType newYearType() {
		return wrapYearType(create(ddvYearType.schema()));
	}

	public YearType newYearTypeWithId(String id) {
		return wrapYearType(create(ddvYearType.schema(), id));
	}

	public final SchemaTypeShortcuts_ddvYearType_default ddvYearType
			= new SchemaTypeShortcuts_ddvYearType_default("ddvYearType_default");
	public class SchemaTypeShortcuts_ddvYearType_default extends SchemaTypeShortcuts {
		protected SchemaTypeShortcuts_ddvYearType_default(String schemaCode) {
			super(schemaCode);
		}

		public Metadata yearEnd() {
			return metadata("yearEnd");
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

	public Iterator<DecommissioningList> decommissioningListIterator() {
		return iterateFromCache(decommissioningList.schemaType(), this::wrapDecommissioningList);
	}

	public Stream<DecommissioningList> decommissioningListStream() {
		return streamFromCache(decommissioningList.schemaType(), this::wrapDecommissioningList);
	}

	public DecommissioningList getDecommissioningList(String id) {
		return wrapDecommissioningList(get(decommissioningList.schemaType(), id));
	}

	public List<DecommissioningList> getDecommissioningLists(List<String> ids) {
		return wrapDecommissioningLists(get(decommissioningList.schemaType(), ids));
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

		public Metadata contents() {
			return metadata("contents");
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

	public Iterator<Document> documentIterator(LogicalSearchCondition condition) {
		return searchIterator(from(document.schemaType()).whereAllConditions(asList(condition)), this::wrapDocument);
	}

	public Stream<Document> documentStream(LogicalSearchCondition condition) {
		return searchIterator(from(document.schemaType()).whereAllConditions(asList(condition)), this::wrapDocument).stream();
	}

	public Iterator<Document> documentIterator(LogicalSearchQuery query) {
		return searchIterator(query, this::wrapDocument);
	}

	public Stream<Document> documentStream(LogicalSearchQuery query) {
		return searchIterator(query, this::wrapDocument).stream();
	}

	public Document getDocument(String id) {
		return wrapDocument(get(document.schemaType(), id));
	}

	public List<Document> getDocuments(List<String> ids) {
		return wrapDocuments(get(document.schemaType(), ids));
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

		public Metadata category() {
			return metadata("category");
		}

		public Metadata categoryCode() {
			return metadata("categoryCode");
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

		public Metadata confidential() {
			return metadata("confidential");
		}

		public Metadata content() {
			return metadata("content");
		}

		public Metadata contentCheckedOutBy() {
			return metadata("contentCheckedOutBy");
		}

		public Metadata contentCheckedOutDate() {
			return metadata("contentCheckedOutDate");
		}

		public Metadata contentHashes() {
			return metadata("contentHashes");
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

		public Metadata essential() {
			return metadata("essential");
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

		public Metadata favorites() {
			return metadata("favorites");
		}

		public Metadata filingSpace() {
			return metadata("filingSpace");
		}

		public Metadata folder() {
			return metadata("folder");
		}

		public Metadata hasContent() {
			return metadata("hasContent");
		}

		public Metadata inheritedRetentionRule() {
			return metadata("inheritedRetentionRule");
		}

		public Metadata isModel() {
			return metadata("isModel");
		}

		public Metadata keywords() {
			return metadata("keywords");
		}

		public Metadata linkedTo() {
			return metadata("linkedTo");
		}

		public Metadata mainCopyRule() {
			return metadata("mainCopyRule");
		}

		public Metadata mainCopyRuleIdEntered() {
			return metadata("mainCopyRuleIdEntered");
		}

		public Metadata mimetype() {
			return metadata("mimetype");
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

	public Iterator<Email> emailIterator(LogicalSearchCondition condition) {
		return searchIterator(from(document.schemaType()).whereAllConditions(asList(condition)), this::wrapEmail);
	}

	public Stream<Email> emailStream(LogicalSearchCondition condition) {
		return searchIterator(from(document.schemaType()).whereAllConditions(asList(condition)), this::wrapEmail).stream();
	}

	public Iterator<Email> emailIterator(LogicalSearchQuery query) {
		return searchIterator(query, this::wrapEmail);
	}

	public Stream<Email> emailStream(LogicalSearchQuery query) {
		return searchIterator(query, this::wrapEmail).stream();
	}

	public Email getEmail(String id) {
		return wrapEmail(get(document.schemaType(), id));
	}

	public List<Email> getEmails(List<String> ids) {
		return wrapEmails(get(document.schemaType(), ids));
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

	public ExternalLink wrapExternalLink(Record record) {
		return record == null ? null : new ExternalLink(record, getTypes());
	}

	public List<ExternalLink> wrapExternalLinks(List<Record> records) {
		List<ExternalLink> wrapped = new ArrayList<>();
		for (Record record : records) {
			wrapped.add(new ExternalLink(record, getTypes()));
		}

		return wrapped;
	}

	public List<ExternalLink> searchExternalLinks(LogicalSearchQuery query) {
		return wrapExternalLinks(modelLayerFactory.newSearchServices().search(query));
	}

	public List<ExternalLink> searchExternalLinks(LogicalSearchCondition condition) {
		MetadataSchemaType type = externalLink.schemaType();
		LogicalSearchQuery query = new LogicalSearchQuery(from(type).whereAllConditions(asList(condition)));
		return wrapExternalLinks(modelLayerFactory.newSearchServices().search(query));
	}

	public Iterator<ExternalLink> externalLinkIterator() {
		return iterateFromCache(externalLink.schemaType(), this::wrapExternalLink);
	}

	public Stream<ExternalLink> externalLinkStream() {
		return streamFromCache(externalLink.schemaType(), this::wrapExternalLink);
	}

	public ExternalLink getExternalLink(String id) {
		return wrapExternalLink(get(externalLink.schemaType(), id));
	}

	public List<ExternalLink> getExternalLinks(List<String> ids) {
		return wrapExternalLinks(get(externalLink.schemaType(), ids));
	}

	public ExternalLink getExternalLinkWithLegacyId(String legacyId) {
		return wrapExternalLink(getByLegacyId(externalLink.schemaType(), legacyId));
	}

	public ExternalLink newExternalLink() {
		return wrapExternalLink(create(externalLink.schema()));
	}

	public ExternalLink newExternalLinkWithId(String id) {
		return wrapExternalLink(create(externalLink.schema(), id));
	}

	public final SchemaTypeShortcuts_externalLink_default externalLink
			= new SchemaTypeShortcuts_externalLink_default("externalLink_default");

	public class SchemaTypeShortcuts_externalLink_default extends SchemaTypeShortcuts {
		protected SchemaTypeShortcuts_externalLink_default(String schemaCode) {
			super(schemaCode);
		}

		public Metadata type() {
			return metadata("type");
		}
	}

	public ExternalLinkType wrapExternalLinkType(Record record) {
		return record == null ? null : new ExternalLinkType(record, getTypes());
	}

	public List<ExternalLinkType> wrapExternalLinkTypes(List<Record> records) {
		List<ExternalLinkType> wrapped = new ArrayList<>();
		for (Record record : records) {
			wrapped.add(new ExternalLinkType(record, getTypes()));
		}

		return wrapped;
	}

	public List<ExternalLinkType> searchExternalLinkTypes(LogicalSearchQuery query) {
		return wrapExternalLinkTypes(modelLayerFactory.newSearchServices().search(query));
	}

	public List<ExternalLinkType> searchExternalLinkTypes(LogicalSearchCondition condition) {
		MetadataSchemaType type = externalLinkType.schemaType();
		LogicalSearchQuery query = new LogicalSearchQuery(from(type).whereAllConditions(asList(condition)));
		return wrapExternalLinkTypes(modelLayerFactory.newSearchServices().search(query));
	}

	public Iterator<ExternalLinkType> externalLinkTypeIterator() {
		return iterateFromCache(externalLinkType.schemaType(), this::wrapExternalLinkType);
	}

	public Stream<ExternalLinkType> externalLinkTypeStream() {
		return streamFromCache(externalLinkType.schemaType(), this::wrapExternalLinkType);
	}

	public ExternalLinkType getExternalLinkType(String id) {
		return wrapExternalLinkType(get(externalLinkType.schemaType(), id));
	}

	public List<ExternalLinkType> getExternalLinkTypes(List<String> ids) {
		return wrapExternalLinkTypes(get(externalLinkType.schemaType(), ids));
	}

	public ExternalLinkType getExternalLinkTypeWithCode(String code) {
		return wrapExternalLinkType(getByCode(externalLinkType.schemaType(), code));
	}

	public ExternalLinkType getExternalLinkTypeWithLegacyId(String legacyId) {
		return wrapExternalLinkType(getByLegacyId(externalLinkType.schemaType(), legacyId));
	}

	public ExternalLinkType newExternalLinkType() {
		return wrapExternalLinkType(create(externalLinkType.schema()));
	}

	public ExternalLinkType newExternalLinkTypeWithId(String id) {
		return wrapExternalLinkType(create(externalLinkType.schema(), id));
	}

	public final SchemaTypeShortcuts_externalLinkType_default externalLinkType
			= new SchemaTypeShortcuts_externalLinkType_default("externalLinkType_default");

	public class SchemaTypeShortcuts_externalLinkType_default extends SchemaTypeShortcuts {
		protected SchemaTypeShortcuts_externalLinkType_default(String schemaCode) {
			super(schemaCode);
		}

		public Metadata code() {
			return metadata("code");
		}

		public Metadata linkedSchema() {
			return metadata("linkedSchema");
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

	public Iterator<Folder> folderIterator(LogicalSearchCondition condition) {
		return searchIterator(from(folder.schemaType()).whereAllConditions(asList(condition)), this::wrapFolder);
	}

	public Stream<Folder> folderStream(LogicalSearchCondition condition) {
		return searchIterator(from(folder.schemaType()).whereAllConditions(asList(condition)), this::wrapFolder).stream();
	}

	public Iterator<Folder> folderIterator(LogicalSearchQuery query) {
		return searchIterator(query, this::wrapFolder);
	}

	public Stream<Folder> folderStream(LogicalSearchQuery query) {
		return searchIterator(query, this::wrapFolder).stream();
	}

	public Folder getFolder(String id) {
		return wrapFolder(get(folder.schemaType(), id));
	}

	public List<Folder> getFolders(List<String> ids) {
		return wrapFolders(get(folder.schemaType(), ids));
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

		public Metadata administrativeUnitCode() {
			return metadata("administrativeUnitCode");
		}

		public Metadata administrativeUnitEntered() {
			return metadata("administrativeUnitEntered");
		}

		public Metadata alertUsersWhenAvailable() {
			return metadata("alertUsersWhenAvailable");
		}

		public Metadata allowedDocumentTypes() {
			return metadata("allowedDocumentTypes");
		}

		public Metadata allowedFolderTypes() {
			return metadata("allowedFolderTypes");
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

		public Metadata confidential() {
			return metadata("confidential");
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

		public Metadata description() {
			return metadata("description");
		}

		public Metadata documentsTokens() {
			return metadata("documentsTokens");
		}

		public Metadata enteredClosingDate() {
			return metadata("enteredClosingDate");
		}

		public Metadata essential() {
			return metadata("essential");
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

		public Metadata favorites() {
			return metadata("favorites");
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

		public Metadata hasContent() {
			return metadata("hasContent");
		}

		public Metadata inactiveDisposalType() {
			return metadata("inactiveDisposalType");
		}

		public Metadata isModel() {
			return metadata("isModel");
		}

		public Metadata isRestrictedAccess() {
			return metadata("isRestrictedAccess");
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

		public Metadata mainCopyRuleCode() {
			return metadata("mainCopyRuleCode");
		}

		public Metadata mainCopyRuleIdEntered() {
			return metadata("mainCopyRuleIdEntered");
		}

		public Metadata manualArchivisticStatus() {
			return metadata("manualArchivisticStatus");
		}

		public Metadata manualDisposalType() {
			return metadata("manualDisposalType");
		}

		public Metadata manualExpectedDepositDate() {
			return metadata("manualExpectedDepositDate");
		}

		public Metadata manualExpectedDesctructionDate() {
			return metadata("manualExpectedDesctructionDate");
		}

		public Metadata manualExpectedTransferDate() {
			return metadata("manualExpectedTransferDate");
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

		public Metadata previousDepositDates() {
			return metadata("previousDepositDates");
		}

		public Metadata previousTransferDates() {
			return metadata("previousTransferDates");
		}

		public Metadata reactivationDates() {
			return metadata("reactivationDates");
		}

		public Metadata reactivationDecommissioningDate() {
			return metadata("reactivationDecommissioningDate");
		}

		public Metadata reactivationUsers() {
			return metadata("reactivationUsers");
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

		public Metadata subFoldersTokens() {
			return metadata("subFoldersTokens");
		}

		public Metadata summary() {
			return metadata("summary");
		}

		public Metadata timerange() {
			return metadata("timerange");
		}

		public Metadata title() {
			return metadata("title");
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

		public Metadata uniqueKey() {
			return metadata("uniqueKey");
		}
	}
	public PrintableLabel wrapPrintableLabel(Record record) {
		return record == null ? null : new PrintableLabel(record, getTypes());
	}

	public List<PrintableLabel> wrapPrintableLabels(List<Record> records) {
		List<PrintableLabel> wrapped = new ArrayList<>();
		for (Record record : records) {
			wrapped.add(new PrintableLabel(record, getTypes()));
		}

		return wrapped;
	}

	public List<PrintableLabel> searchPrintableLabels(LogicalSearchQuery query) {
		return wrapPrintableLabels(modelLayerFactory.newSearchServices().search(query));
	}

	public List<PrintableLabel> searchPrintableLabels(LogicalSearchCondition condition) {
		MetadataSchemaType type = printable.schemaType();
		LogicalSearchQuery query = new LogicalSearchQuery(from(type).whereAllConditions(asList(condition)));
		return wrapPrintableLabels(modelLayerFactory.newSearchServices().search(query));
	}

	public Iterator<PrintableLabel> printableLabelIterator() {
		return iterateFromCache(printable.schemaType(), this::wrapPrintableLabel);
	}

	public Stream<PrintableLabel> printableLabelStream() {
		return streamFromCache(printable.schemaType(), this::wrapPrintableLabel);
	}

	public PrintableLabel getPrintableLabel(String id) {
		return wrapPrintableLabel(get(printable.schemaType(), id));
	}

	public List<PrintableLabel> getPrintableLabels(List<String> ids) {
		return wrapPrintableLabels(get(printable.schemaType(), ids));
	}

	public PrintableLabel getPrintableLabelWithLegacyId(String legacyId) {
		return wrapPrintableLabel(getByLegacyId(printable.schemaType(), legacyId));
	}

	public PrintableLabel newPrintableLabel() {
		return wrapPrintableLabel(create(printable_label.schema()));
	}

	public PrintableLabel newPrintableLabelWithId(String id) {
		return wrapPrintableLabel(create(printable_label.schema(), id));
	}

	public final SchemaTypeShortcuts_printable_label printable_label
			= new SchemaTypeShortcuts_printable_label("printable_label");
	public class SchemaTypeShortcuts_printable_label extends SchemaTypeShortcuts_printable_default {
		protected SchemaTypeShortcuts_printable_label(String schemaCode) {
			super(schemaCode);
		}

		public Metadata colonne() {
			return metadata("colonne");
		}

		public Metadata ligne() {
			return metadata("ligne");
		}

		public Metadata typelabel() {
			return metadata("typelabel");
		}
	}
	public PrintableReport wrapPrintableReport(Record record) {
		return record == null ? null : new PrintableReport(record, getTypes());
	}

	public List<PrintableReport> wrapPrintableReports(List<Record> records) {
		List<PrintableReport> wrapped = new ArrayList<>();
		for (Record record : records) {
			wrapped.add(new PrintableReport(record, getTypes()));
		}

		return wrapped;
	}

	public List<PrintableReport> searchPrintableReports(LogicalSearchQuery query) {
		return wrapPrintableReports(modelLayerFactory.newSearchServices().search(query));
	}

	public List<PrintableReport> searchPrintableReports(LogicalSearchCondition condition) {
		MetadataSchemaType type = printable.schemaType();
		LogicalSearchQuery query = new LogicalSearchQuery(from(type).whereAllConditions(asList(condition)));
		return wrapPrintableReports(modelLayerFactory.newSearchServices().search(query));
	}

	public Iterator<PrintableReport> printableReportIterator() {
		return iterateFromCache(printable.schemaType(), this::wrapPrintableReport);
	}

	public Stream<PrintableReport> printableReportStream() {
		return streamFromCache(printable.schemaType(), this::wrapPrintableReport);
	}

	public PrintableReport getPrintableReport(String id) {
		return wrapPrintableReport(get(printable.schemaType(), id));
	}

	public List<PrintableReport> getPrintableReports(List<String> ids) {
		return wrapPrintableReports(get(printable.schemaType(), ids));
	}

	public PrintableReport getPrintableReportWithLegacyId(String legacyId) {
		return wrapPrintableReport(getByLegacyId(printable.schemaType(), legacyId));
	}

	public PrintableReport newPrintableReport() {
		return wrapPrintableReport(create(printable_report.schema()));
	}

	public PrintableReport newPrintableReportWithId(String id) {
		return wrapPrintableReport(create(printable_report.schema(), id));
	}

	public final SchemaTypeShortcuts_printable_report printable_report
			= new SchemaTypeShortcuts_printable_report("printable_report");
	public class SchemaTypeShortcuts_printable_report extends SchemaTypeShortcuts_printable_default {
		protected SchemaTypeShortcuts_printable_report(String schemaCode) {
			super(schemaCode);
		}

		public Metadata recordSchema() {
			return metadata("recordSchema");
		}

		public Metadata recordType() {
			return metadata("recordType");
		}
	}
	public RetentionRule wrapRetentionRule(Record record) {
		return record == null ? null : new RetentionRule(record, getTypes(), locale);
	}

	public List<RetentionRule> wrapRetentionRules(List<Record> records) {
		List<RetentionRule> wrapped = new ArrayList<>();
		for (Record record : records) {
			wrapped.add(new RetentionRule(record, getTypes(), locale));
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

	public Iterator<RetentionRule> retentionRuleIterator() {
		return iterateFromCache(retentionRule.schemaType(), this::wrapRetentionRule);
	}

	public Stream<RetentionRule> retentionRuleStream() {
		return streamFromCache(retentionRule.schemaType(), this::wrapRetentionRule);
	}

	public RetentionRule getRetentionRule(String id) {
		return wrapRetentionRule(get(retentionRule.schemaType(), id));
	}

	public List<RetentionRule> getRetentionRules(List<String> ids) {
		return wrapRetentionRules(get(retentionRule.schemaType(), ids));
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

		public Metadata yearTypes() {
			return metadata("yearTypes");
		}

		public Metadata yearTypesYearEnd() {
			return metadata("yearTypesYearEnd");
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

	public Iterator<StorageSpace> storageSpaceIterator() {
		return iterateFromCache(storageSpace.schemaType(), this::wrapStorageSpace);
	}

	public Stream<StorageSpace> storageSpaceStream() {
		return streamFromCache(storageSpace.schemaType(), this::wrapStorageSpace);
	}

	public StorageSpace getStorageSpace(String id) {
		return wrapStorageSpace(get(storageSpace.schemaType(), id));
	}

	public List<StorageSpace> getStorageSpaces(List<String> ids) {
		return wrapStorageSpaces(get(storageSpace.schemaType(), ids));
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

		public Metadata availableSize() {
			return metadata("availableSize");
		}

		public Metadata capacity() {
			return metadata("capacity");
		}

		public Metadata childLinearSizeSum() {
			return metadata("childLinearSizeSum");
		}

		public Metadata code() {
			return metadata("code");
		}

		public Metadata comments() {
			return metadata("comments");
		}

		public Metadata containerType() {
			return metadata("containerType");
		}

		public Metadata decommissioningType() {
			return metadata("decommissioningType");
		}

		public Metadata description() {
			return metadata("description");
		}

		public Metadata linearSize() {
			return metadata("linearSize");
		}

		public Metadata linearSizeEntered() {
			return metadata("linearSizeEntered");
		}

		public Metadata linearSizeSum() {
			return metadata("linearSizeSum");
		}

		public Metadata numberOfChild() {
			return metadata("numberOfChild");
		}

		public Metadata numberOfContainers() {
			return metadata("numberOfContainers");
		}

		public Metadata parentStorageSpace() {
			return metadata("parentStorageSpace");
		}

		public Metadata type() {
			return metadata("type");
		}
	}
	public SIParchive wrapSIParchive(Record record) {
		return record == null ? null : new SIParchive(record, getTypes());
	}

	public List<SIParchive> wrapSIParchives(List<Record> records) {
		List<SIParchive> wrapped = new ArrayList<>();
		for (Record record : records) {
			wrapped.add(new SIParchive(record, getTypes()));
		}

		return wrapped;
	}

	public List<SIParchive> searchSIParchives(LogicalSearchQuery query) {
		return wrapSIParchives(modelLayerFactory.newSearchServices().search(query));
	}

	public List<SIParchive> searchSIParchives(LogicalSearchCondition condition) {
		MetadataSchemaType type = temporaryRecord.schemaType();
		LogicalSearchQuery query = new LogicalSearchQuery(from(type).whereAllConditions(asList(condition)));
		return wrapSIParchives(modelLayerFactory.newSearchServices().search(query));
	}

	public Iterator<SIParchive> sIParchiveIterator() {
		return iterateFromCache(temporaryRecord.schemaType(), this::wrapSIParchive);
	}

	public Stream<SIParchive> sIParchiveStream() {
		return streamFromCache(temporaryRecord.schemaType(), this::wrapSIParchive);
	}

	public SIParchive getSIParchive(String id) {
		return wrapSIParchive(get(temporaryRecord.schemaType(), id));
	}

	public List<SIParchive> getSIParchives(List<String> ids) {
		return wrapSIParchives(get(temporaryRecord.schemaType(), ids));
	}

	public SIParchive getSIParchiveWithLegacyId(String legacyId) {
		return wrapSIParchive(getByLegacyId(temporaryRecord.schemaType(), legacyId));
	}

	public SIParchive newSIParchive() {
		return wrapSIParchive(create(temporaryRecord_sipArchive.schema()));
	}

	public SIParchive newSIParchiveWithId(String id) {
		return wrapSIParchive(create(temporaryRecord_sipArchive.schema(), id));
	}

	public final SchemaTypeShortcuts_temporaryRecord_sipArchive temporaryRecord_sipArchive
			= new SchemaTypeShortcuts_temporaryRecord_sipArchive("temporaryRecord_sipArchive");
	public class SchemaTypeShortcuts_temporaryRecord_sipArchive extends SchemaTypeShortcuts_temporaryRecord_default {
		protected SchemaTypeShortcuts_temporaryRecord_sipArchive(String schemaCode) {
			super(schemaCode);
		}

		public Metadata creationDate() {
			return metadata("creationDate");
		}

		public Metadata name() {
			return metadata("name");
		}

		public Metadata user() {
			return metadata("user");
		}
	}

	public Trigger wrapTrigger(Record record) {
		return record == null ? null : new Trigger(record, getTypes());
	}

	public List<Trigger> wrapTriggers(List<Record> records) {
		List<Trigger> wrapped = new ArrayList<>();
		for (Record record : records) {
			wrapped.add(new Trigger(record, getTypes()));
		}

		return wrapped;
	}

	public List<Trigger> searchTriggers(LogicalSearchQuery query) {
		return wrapTriggers(modelLayerFactory.newSearchServices().search(query));
	}

	public List<Trigger> searchTriggers(LogicalSearchCondition condition) {
		MetadataSchemaType type = trigger.schemaType();
		LogicalSearchQuery query = new LogicalSearchQuery(from(type).whereAllConditions(asList(condition)));
		return wrapTriggers(modelLayerFactory.newSearchServices().search(query));
	}

	public Iterator<Trigger> triggerIterator() {
		return iterateFromCache(trigger.schemaType(), this::wrapTrigger);
	}

	public Stream<Trigger> triggerStream() {
		return streamFromCache(trigger.schemaType(), this::wrapTrigger);
	}

	public Trigger getTrigger(String id) {
		return wrapTrigger(get(trigger.schemaType(), id));
	}

	public List<Trigger> getTriggers(List<String> ids) {
		return wrapTriggers(get(trigger.schemaType(), ids));
	}

	public Trigger getTriggerWithLegacyId(String legacyId) {
		return wrapTrigger(getByLegacyId(trigger.schemaType(), legacyId));
	}

	public Trigger newTrigger() {
		return wrapTrigger(create(trigger.schema()));
	}

	public Trigger newTriggerWithId(String id) {
		return wrapTrigger(create(trigger.schema(), id));
	}

	public final SchemaTypeShortcuts_trigger_default trigger
			= new SchemaTypeShortcuts_trigger_default("trigger_default");

	public class SchemaTypeShortcuts_trigger_default extends SchemaTypeShortcuts {
		protected SchemaTypeShortcuts_trigger_default(String schemaCode) {
			super(schemaCode);
		}

		public Metadata actions() {
			return metadata("actions");
		}

		public Metadata criteria() {
			return metadata("criteria");
		}

		public Metadata type() {
			return metadata("type");
		}
	}

	public MoveInFolderTriggerAction wrapMoveInFolderTriggerAction(Record record) {
		return record == null ? null : new MoveInFolderTriggerAction(record, getTypes());
	}

	public List<MoveInFolderTriggerAction> wrapMoveInFolderTriggerActions(List<Record> records) {
		List<MoveInFolderTriggerAction> wrapped = new ArrayList<>();
		for (Record record : records) {
			wrapped.add(new MoveInFolderTriggerAction(record, getTypes()));
		}

		return wrapped;
	}

	public List<MoveInFolderTriggerAction> searchMoveInFolderTriggerActions(LogicalSearchQuery query) {
		return wrapMoveInFolderTriggerActions(modelLayerFactory.newSearchServices().search(query));
	}

	public List<MoveInFolderTriggerAction> searchMoveInFolderTriggerActions(LogicalSearchCondition condition) {
		MetadataSchemaType type = triggerAction.schemaType();
		LogicalSearchQuery query = new LogicalSearchQuery(from(type).whereAllConditions(asList(condition)));
		return wrapMoveInFolderTriggerActions(modelLayerFactory.newSearchServices().search(query));
	}

	public Iterator<MoveInFolderTriggerAction> moveInFolderTriggerActionIterator() {
		return iterateFromCache(triggerAction.schemaType(), this::wrapMoveInFolderTriggerAction);
	}

	public Stream<MoveInFolderTriggerAction> moveInFolderTriggerActionStream() {
		return streamFromCache(triggerAction.schemaType(), this::wrapMoveInFolderTriggerAction);
	}

	public MoveInFolderTriggerAction getMoveInFolderTriggerAction(String id) {
		return wrapMoveInFolderTriggerAction(get(triggerAction.schemaType(), id));
	}

	public List<MoveInFolderTriggerAction> getMoveInFolderTriggerActions(List<String> ids) {
		return wrapMoveInFolderTriggerActions(get(triggerAction.schemaType(), ids));
	}

	public MoveInFolderTriggerAction getMoveInFolderTriggerActionWithLegacyId(String legacyId) {
		return wrapMoveInFolderTriggerAction(getByLegacyId(triggerAction.schemaType(), legacyId));
	}

	public MoveInFolderTriggerAction newMoveInFolderTriggerAction() {
		return wrapMoveInFolderTriggerAction(create(triggerAction.schema()));
	}

	public MoveInFolderTriggerAction newMoveInFolderTriggerActionWithId(String id) {
		return wrapMoveInFolderTriggerAction(create(triggerAction.schema(), id));
	}

	public final SchemaTypeShortcuts_triggerAction_default triggerAction
			= new SchemaTypeShortcuts_triggerAction_default("triggerAction_default");

	public class SchemaTypeShortcuts_triggerAction_default extends SchemaTypeShortcuts {
		protected SchemaTypeShortcuts_triggerAction_default(String schemaCode) {
			super(schemaCode);
		}
	}

	public TriggerActionType wrapTriggerActionType(Record record) {
		return record == null ? null : new TriggerActionType(record, getTypes());
	}

	public List<TriggerActionType> wrapTriggerActionTypes(List<Record> records) {
		List<TriggerActionType> wrapped = new ArrayList<>();
		for (Record record : records) {
			wrapped.add(new TriggerActionType(record, getTypes()));
		}

		return wrapped;
	}

	public List<TriggerActionType> searchTriggerActionTypes(LogicalSearchQuery query) {
		return wrapTriggerActionTypes(modelLayerFactory.newSearchServices().search(query));
	}

	public List<TriggerActionType> searchTriggerActionTypes(LogicalSearchCondition condition) {
		MetadataSchemaType type = triggerActionType.schemaType();
		LogicalSearchQuery query = new LogicalSearchQuery(from(type).whereAllConditions(asList(condition)));
		return wrapTriggerActionTypes(modelLayerFactory.newSearchServices().search(query));
	}

	public Iterator<TriggerActionType> triggerActionTypeIterator() {
		return iterateFromCache(triggerActionType.schemaType(), this::wrapTriggerActionType);
	}

	public Stream<TriggerActionType> triggerActionTypeStream() {
		return streamFromCache(triggerActionType.schemaType(), this::wrapTriggerActionType);
	}

	public TriggerActionType getTriggerActionType(String id) {
		return wrapTriggerActionType(get(triggerActionType.schemaType(), id));
	}

	public List<TriggerActionType> getTriggerActionTypes(List<String> ids) {
		return wrapTriggerActionTypes(get(triggerActionType.schemaType(), ids));
	}

	public TriggerActionType getTriggerActionTypeWithCode(String code) {
		return wrapTriggerActionType(getByCode(triggerActionType.schemaType(), code));
	}

	public TriggerActionType getTriggerActionTypeWithLegacyId(String legacyId) {
		return wrapTriggerActionType(getByLegacyId(triggerActionType.schemaType(), legacyId));
	}

	public TriggerActionType newTriggerActionType() {
		return wrapTriggerActionType(create(triggerActionType.schema()));
	}

	public TriggerActionType newTriggerActionTypeWithId(String id) {
		return wrapTriggerActionType(create(triggerActionType.schema(), id));
	}

	public final SchemaTypeShortcuts_triggerActionType_default triggerActionType
			= new SchemaTypeShortcuts_triggerActionType_default("triggerActionType_default");

	public class SchemaTypeShortcuts_triggerActionType_default extends SchemaTypeShortcuts {
		protected SchemaTypeShortcuts_triggerActionType_default(String schemaCode) {
			super(schemaCode);
		}

		public Metadata code() {
			return metadata("code");
		}

		public Metadata linkedSchema() {
			return metadata("linkedSchema");
		}

		public Metadata title() {
			return metadata("title");
		}
	}

	public TriggerType wrapTriggerType(Record record) {
		return record == null ? null : new TriggerType(record, getTypes());
	}

	public List<TriggerType> wrapTriggerTypes(List<Record> records) {
		List<TriggerType> wrapped = new ArrayList<>();
		for (Record record : records) {
			wrapped.add(new TriggerType(record, getTypes()));
		}

		return wrapped;
	}

	public List<TriggerType> searchTriggerTypes(LogicalSearchQuery query) {
		return wrapTriggerTypes(modelLayerFactory.newSearchServices().search(query));
	}

	public List<TriggerType> searchTriggerTypes(LogicalSearchCondition condition) {
		MetadataSchemaType type = triggerType.schemaType();
		LogicalSearchQuery query = new LogicalSearchQuery(from(type).whereAllConditions(asList(condition)));
		return wrapTriggerTypes(modelLayerFactory.newSearchServices().search(query));
	}

	public Iterator<TriggerType> triggerTypeIterator() {
		return iterateFromCache(triggerType.schemaType(), this::wrapTriggerType);
	}

	public Stream<TriggerType> triggerTypeStream() {
		return streamFromCache(triggerType.schemaType(), this::wrapTriggerType);
	}

	public TriggerType getTriggerType(String id) {
		return wrapTriggerType(get(triggerType.schemaType(), id));
	}

	public List<TriggerType> getTriggerTypes(List<String> ids) {
		return wrapTriggerTypes(get(triggerType.schemaType(), ids));
	}

	public TriggerType getTriggerTypeWithCode(String code) {
		return wrapTriggerType(getByCode(triggerType.schemaType(), code));
	}

	public TriggerType getTriggerTypeWithLegacyId(String legacyId) {
		return wrapTriggerType(getByLegacyId(triggerType.schemaType(), legacyId));
	}

	public TriggerType newTriggerType() {
		return wrapTriggerType(create(triggerType.schema()));
	}

	public TriggerType newTriggerTypeWithId(String id) {
		return wrapTriggerType(create(triggerType.schema(), id));
	}

	public final SchemaTypeShortcuts_triggerType_default triggerType
			= new SchemaTypeShortcuts_triggerType_default("triggerType_default");

	public class SchemaTypeShortcuts_triggerType_default extends SchemaTypeShortcuts {
		protected SchemaTypeShortcuts_triggerType_default(String schemaCode) {
			super(schemaCode);
		}

		public Metadata code() {
			return metadata("code");
		}

		public Metadata title() {
			return metadata("title");
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

	public Iterator<UniformSubdivision> uniformSubdivisionIterator() {
		return iterateFromCache(uniformSubdivision.schemaType(), this::wrapUniformSubdivision);
	}

	public Stream<UniformSubdivision> uniformSubdivisionStream() {
		return streamFromCache(uniformSubdivision.schemaType(), this::wrapUniformSubdivision);
	}

	public UniformSubdivision getUniformSubdivision(String id) {
		return wrapUniformSubdivision(get(uniformSubdivision.schemaType(), id));
	}

	public List<UniformSubdivision> getUniformSubdivisions(List<String> ids) {
		return wrapUniformSubdivisions(get(uniformSubdivision.schemaType(), ids));
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
	public RMUserFolder wrapRMUserFolder(Record record) {
		return record == null ? null : new RMUserFolder(record, getTypes());
	}

	public List<RMUserFolder> wrapRMUserFolders(List<Record> records) {
		List<RMUserFolder> wrapped = new ArrayList<>();
		for (Record record : records) {
			wrapped.add(new RMUserFolder(record, getTypes()));
		}

		return wrapped;
	}

	public List<RMUserFolder> searchRMUserFolders(LogicalSearchQuery query) {
		return wrapRMUserFolders(modelLayerFactory.newSearchServices().search(query));
	}

	public List<RMUserFolder> searchRMUserFolders(LogicalSearchCondition condition) {
		MetadataSchemaType type = userFolder.schemaType();
		LogicalSearchQuery query = new LogicalSearchQuery(from(type).whereAllConditions(asList(condition)));
		return wrapRMUserFolders(modelLayerFactory.newSearchServices().search(query));
	}

	public Iterator<RMUserFolder> rMUserFolderIterator(LogicalSearchCondition condition) {
		return searchIterator(from(userFolder.schemaType()).whereAllConditions(asList(condition)), this::wrapRMUserFolder);
	}

	public Stream<RMUserFolder> rMUserFolderStream(LogicalSearchCondition condition) {
		return searchIterator(from(userFolder.schemaType()).whereAllConditions(asList(condition)), this::wrapRMUserFolder).stream();
	}

	public Iterator<RMUserFolder> rMUserFolderIterator(LogicalSearchQuery query) {
		return searchIterator(query, this::wrapRMUserFolder);
	}

	public Stream<RMUserFolder> rMUserFolderStream(LogicalSearchQuery query) {
		return searchIterator(query, this::wrapRMUserFolder).stream();
	}

	public RMUserFolder getRMUserFolder(String id) {
		return wrapRMUserFolder(get(userFolder.schemaType(), id));
	}

	public List<RMUserFolder> getRMUserFolders(List<String> ids) {
		return wrapRMUserFolders(get(userFolder.schemaType(), ids));
	}

	public RMUserFolder getRMUserFolderWithLegacyId(String legacyId) {
		return wrapRMUserFolder(getByLegacyId(userFolder.schemaType(), legacyId));
	}

	public RMUserFolder newRMUserFolder() {
		return wrapRMUserFolder(create(userFolder.schema()));
	}

	public RMUserFolder newRMUserFolderWithId(String id) {
		return wrapRMUserFolder(create(userFolder.schema(), id));
	}

	public final SchemaTypeShortcuts_userFolder_default userFolder
			= new SchemaTypeShortcuts_userFolder_default("userFolder_default");
	public class SchemaTypeShortcuts_userFolder_default extends SchemaTypeShortcuts {
		protected SchemaTypeShortcuts_userFolder_default(String schemaCode) {
			super(schemaCode);
		}

		public Metadata administrativeUnit() {
			return metadata("administrativeUnit");
		}

		public Metadata category() {
			return metadata("category");
		}

		public Metadata parentFolder() {
			return metadata("parentFolder");
		}

		public Metadata retentionRule() {
			return metadata("retentionRule");
		}
	}
	public RMTask wrapRMTask(Record record) {
		return record == null ? null : new RMTask(record, getTypes());
	}

	public List<RMTask> wrapRMTasks(List<Record> records) {
		List<RMTask> wrapped = new ArrayList<>();
		for (Record record : records) {
			wrapped.add(new RMTask(record, getTypes()));
		}

		return wrapped;
	}

	public List<RMTask> searchRMTasks(LogicalSearchQuery query) {
		return wrapRMTasks(modelLayerFactory.newSearchServices().search(query));
	}

	public List<RMTask> searchRMTasks(LogicalSearchCondition condition) {
		MetadataSchemaType type = userTask.schemaType();
		LogicalSearchQuery query = new LogicalSearchQuery(from(type).whereAllConditions(asList(condition)));
		return wrapRMTasks(modelLayerFactory.newSearchServices().search(query));
	}

	public Iterator<RMTask> rMTaskIterator() {
		return iterateFromCache(userTask.schemaType(), this::wrapRMTask);
	}

	public Stream<RMTask> rMTaskStream() {
		return streamFromCache(userTask.schemaType(), this::wrapRMTask);
	}

	public RMTask getRMTask(String id) {
		return wrapRMTask(get(userTask.schemaType(), id));
	}

	public List<RMTask> getRMTasks(List<String> ids) {
		return wrapRMTasks(get(userTask.schemaType(), ids));
	}

	public RMTask getRMTaskWithLegacyId(String legacyId) {
		return wrapRMTask(getByLegacyId(userTask.schemaType(), legacyId));
	}

	public RMTask newRMTask() {
		return wrapRMTask(create(userTask.schema()));
	}

	public RMTask newRMTaskWithId(String id) {
		return wrapRMTask(create(userTask.schema(), id));
	}

	public final SchemaTypeShortcuts_userTask_default userTask
			= new SchemaTypeShortcuts_userTask_default("userTask_default");
	public class SchemaTypeShortcuts_userTask_default extends SchemaTypeShortcuts {
		protected SchemaTypeShortcuts_userTask_default(String schemaCode) {
			super(schemaCode);
		}

		public Metadata administrativeUnit() {
			return metadata("administrativeUnit");
		}

		public Metadata createdAuthorizations() {
			return metadata("createdAuthorizations");
		}

		public Metadata linkedContainers() {
			return metadata("linkedContainers");
		}

		public Metadata linkedDocuments() {
			return metadata("linkedDocuments");
		}

		public Metadata linkedFolders() {
			return metadata("linkedFolders");
		}
	}
	/** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** **/
	// Auto-generated methods by GenerateHelperClassAcceptTest -- end
	/** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** ** **/

}
