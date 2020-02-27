package com.constellio.model.services.taxonomies;

import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.managers.config.DocumentAlteration;
import com.constellio.data.dao.managers.config.FileSystemConfigManager;
import com.constellio.data.dao.services.cache.ConstellioCache;
import com.constellio.data.dao.services.cache.ConstellioCacheManager;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.services.batch.manager.BatchProcessesManager;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.migrations.ConstellioEIMConfigs;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.search.SearchServices;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.taxonomies.TaxonomiesManager.TaxonomiesManagerCache;
import com.constellio.model.services.taxonomies.TaxonomiesManagerRuntimeException.*;
import com.constellio.model.utils.OneXMLConfigPerCollectionManager;
import com.constellio.model.utils.OneXMLConfigPerCollectionManagerListener;
import com.constellio.model.utils.XMLConfigReader;
import org.jdom2.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;

public class TaxonomiesManager implements StatefulService, OneXMLConfigPerCollectionManagerListener<TaxonomiesManagerCache> {

	public static final String TAXONOMIES_CONFIG = "/taxonomies.xml";
	private static final Logger LOGGER = LoggerFactory.getLogger(FileSystemConfigManager.class);

	private final SearchServices searchServices;
	private final ConfigManager configManager;
	private final CollectionsListManager collectionsListManager;
	private ConstellioCacheManager cacheManager;
	private final BatchProcessesManager batchProcessesManager;
	private OneXMLConfigPerCollectionManager<TaxonomiesManagerCache> oneXMLConfigPerCollectionManager;
	private ConstellioEIMConfigs eimConfigs;

	public TaxonomiesManager(ConfigManager configManager, SearchServices searchServices,
							 BatchProcessesManager batchProcessesManager, CollectionsListManager collectionsListManager,
							 ConstellioCacheManager cacheManager,
							 ConstellioEIMConfigs eimConfigs) {
		this.searchServices = searchServices;
		this.configManager = configManager;
		this.collectionsListManager = collectionsListManager;
		this.batchProcessesManager = batchProcessesManager;
		this.cacheManager = cacheManager;
		this.eimConfigs = eimConfigs;
	}

	@Override
	public void initialize() {
		this.oneXMLConfigPerCollectionManager = newOneXMLConfigPerCollectionManager();
	}

	public OneXMLConfigPerCollectionManager<TaxonomiesManagerCache> newOneXMLConfigPerCollectionManager() {
		ConstellioCache cache = cacheManager.getCache(TaxonomiesManager.class.getName());
		return new OneXMLConfigPerCollectionManager<>(configManager, collectionsListManager,
				TAXONOMIES_CONFIG, xmlConfigReader(), this, cache);
	}

	private XMLConfigReader<TaxonomiesManagerCache> xmlConfigReader() {

		return new XMLConfigReader<TaxonomiesManagerCache>() {

			@Override
			public TaxonomiesManagerCache read(String collection, Document document) {
				TaxonomiesReader reader = newTaxonomyReader(document, collectionsListManager.getCollectionLanguages(collection));
				List<Taxonomy> enableTaxonomies = Collections.unmodifiableList(reader.readEnables());
				List<Taxonomy> disableTaxonomies = Collections.unmodifiableList(reader.readDisables());

				String principalTaxonomyCode = reader.readPrincipalCode();

				Taxonomy principalTaxonomy = null;
				for (Taxonomy taxonomy : enableTaxonomies) {
					if (taxonomy.getCode().equals(principalTaxonomyCode)) {
						principalTaxonomy = taxonomy;
						break;
					}
				}

				return new TaxonomiesManagerCache(principalTaxonomy, enableTaxonomies, disableTaxonomies);
			}
		};
	}

	public void addTaxonomy(Taxonomy taxonomy, MetadataSchemasManager schemasManager) {
		canCreateTaxonomy(taxonomy, schemasManager);
		String collection = taxonomy.getCollection();
		oneXMLConfigPerCollectionManager.updateXML(collection, newAddTaxonomyDocumentAlteration(taxonomy));

	}


	public void editTaxonomy(Taxonomy taxonomy) {
		String collection = taxonomy.getCollection();
		oneXMLConfigPerCollectionManager.updateXML(collection, newEditTaxonomyDocumentAlteration(taxonomy));
	}

	public void deleteWithoutValidations(Taxonomy taxonomy) {
		String collection = taxonomy.getCollection();
		oneXMLConfigPerCollectionManager.updateXML(collection, newDeleteTaxonomyDocumentAlteration(taxonomy));
	}

	public void enable(Taxonomy taxonomy, MetadataSchemasManager schemasManager) {
		verifyRecordsWithTaxonomiesSchemaTypes(taxonomy, schemasManager);
		String collection = taxonomy.getCollection();
		oneXMLConfigPerCollectionManager.updateXML(collection, newEnableTaxonomyDocumentAlteration(taxonomy.getCode()));
	}

	public void disable(Taxonomy taxonomy, MetadataSchemasManager schemasManager) {
		verifyRecordsWithTaxonomiesSchemaTypes(taxonomy, schemasManager);
		String collection = taxonomy.getCollection();
		TaxonomiesManagerCache cache = oneXMLConfigPerCollectionManager.get(collection);
		if (cache.principalTaxonomy != null && taxonomy.getCode().equals(cache.principalTaxonomy.getCode())) {
			throw new PrincipalTaxonomyCannotBeDisabled();
		}

		oneXMLConfigPerCollectionManager.updateXML(collection, newDisableTaxonomyDocumentAlteration(taxonomy.getCode()));
	}

	public void setPrincipalTaxonomy(Taxonomy taxonomy, MetadataSchemasManager schemasManager) {
		validateCanBePrincipalTaxonomy(taxonomy, schemasManager);
		String collection = taxonomy.getCollection();
		oneXMLConfigPerCollectionManager.updateXML(collection, newSetPrincipalTaxonomy(taxonomy));

	}

	private void validateCanBePrincipalTaxonomy(Taxonomy taxonomy, MetadataSchemasManager schemasManager) {
		String collection = taxonomy.getCollection();
		TaxonomiesManagerCache cache = oneXMLConfigPerCollectionManager.get(collection);
		if (cache.principalTaxonomy != null) {
			throw new PrincipalTaxonomyIsAlreadyDefined();
		}
		if (getDisabledTaxonomies(taxonomy.getCollection()).contains(taxonomy)) {
			throw new PrincipalTaxonomyCannotBeDisabled();
		}
		if (!getEnabledTaxonomies(taxonomy.getCollection()).contains(taxonomy)) {
			throw new TaxonomyMustBeAddedBeforeSettingItHasPrincipal();
		}

	}

	public List<String> getSecondaryTaxonomySchemaTypes(String collection) {
		List<String> secondaryTaxonomySchemaTypes = new ArrayList<>();
		Taxonomy principalTaxonomy = getPrincipalTaxonomy(collection);
		for (Taxonomy taxonomy : getEnabledTaxonomies(collection)) {
			if (principalTaxonomy == null || !principalTaxonomy.getCode().equals(taxonomy.getCode())) {
				secondaryTaxonomySchemaTypes.addAll(taxonomy.getSchemaTypes());
			}
		}
		return secondaryTaxonomySchemaTypes;
	}

	public Taxonomy getPrincipalTaxonomy(String collection) {
		return oneXMLConfigPerCollectionManager.get(collection).principalTaxonomy;
	}

	public List<Taxonomy> getEnabledTaxonomies(String collection) {
		return oneXMLConfigPerCollectionManager.get(collection).enableTaxonomies;
	}

	public List<Taxonomy> getDisabledTaxonomies(String collection) {
		return oneXMLConfigPerCollectionManager.get(collection).disableTaxonomies;
	}

	public Taxonomy getEnabledTaxonomyWithCode(String collection, String code) {
		for (Taxonomy taxonomy : getEnabledTaxonomies(collection)) {
			if (taxonomy.getCode().equals(code)) {
				return taxonomy;
			}
		}
		throw new TaxonomiesManagerRuntimeException_EnableTaxonomyNotFound(code, collection);
	}

	public Taxonomy getTaxonomyOf(Record record) {
		return getTaxonomyFor(record.getCollection(), new SchemaUtils().getSchemaTypeCode(record.getSchemaCode()));

	}

	public Taxonomy getTaxonomyFor(String collection, String schemaTypeCode) {
		List<Taxonomy> enableTaxonomies = oneXMLConfigPerCollectionManager.get(collection).enableTaxonomies;
		if (enableTaxonomies != null) {
			for (Taxonomy taxonomy : enableTaxonomies) {
				if (taxonomy.getSchemaTypes().contains(schemaTypeCode)) {
					return taxonomy;
				}
			}
		}
		return null;
	}

	TaxonomiesWriter newTaxonomyWriter(Document document) {
		return new TaxonomiesWriter(document);
	}

	TaxonomiesReader newTaxonomyReader(Document document, List<String> collectionLanguageList) {
		return new TaxonomiesReader(document, collectionLanguageList);
	}

	DocumentAlteration newAddTaxonomyDocumentAlteration(final Taxonomy taxonomy) {
		return new DocumentAlteration() {
			@Override
			public void alter(Document document) {
				newTaxonomyWriter(document).addTaxonmy(taxonomy);
			}
		};
	}

	DocumentAlteration newEditTaxonomyDocumentAlteration(final Taxonomy taxonomy) {
		return new DocumentAlteration() {
			@Override
			public void alter(Document document) {
				newTaxonomyWriter(document).editTaxonomy(taxonomy);
			}
		};
	}

	DocumentAlteration newDeleteTaxonomyDocumentAlteration(final Taxonomy taxonomy) {
		return new DocumentAlteration() {
			@Override
			public void alter(Document document) {
				newTaxonomyWriter(document).deleteTaxonomy(taxonomy);
			}
		};
	}

	DocumentAlteration newDisableTaxonomyDocumentAlteration(final String taxonomyCode) {
		return new DocumentAlteration() {
			@Override
			public void alter(Document document) {
				newTaxonomyWriter(document).disable(taxonomyCode);
			}
		};
	}

	DocumentAlteration newEnableTaxonomyDocumentAlteration(final String taxonomyCode) {
		return new DocumentAlteration() {
			@Override
			public void alter(Document document) {
				newTaxonomyWriter(document).enable(taxonomyCode);
			}
		};
	}

	DocumentAlteration newSetPrincipalTaxonomy(final Taxonomy taxonomy) {
		return new DocumentAlteration() {
			@Override
			public void alter(Document document) {
				newTaxonomyWriter(document).setPrincipalCode(taxonomy.getCode());
			}
		};
	}

	void canCreateTaxonomy(Taxonomy taxonomy, MetadataSchemasManager schemasManager) {
		verifyIfExists(taxonomy);
		verifyRecordsWithTaxonomiesSchemaTypes(taxonomy, schemasManager);
	}

	void verifyRecordsWithTaxonomiesSchemaTypes(Taxonomy taxonomy, MetadataSchemasManager schemasManager) {
		List<String> taxonomiesTypes = new ArrayList<>();
		taxonomiesTypes.addAll(taxonomy.getSchemaTypes());
		MetadataSchemaTypes schemaTypes = schemasManager.getSchemaTypes(taxonomy.getCollection());
		for (String taxonomieType : taxonomiesTypes) {
			MetadataSchemaType schemaType = schemaTypes.getSchemaType(taxonomieType);
			LogicalSearchCondition condition = from(schemaType).returnAll();
			if (searchServices.hasResults(condition)) {
				throw new TaxonomySchemaTypesHaveRecords(schemaType.getCode());
			}
		}
	}

	void verifyIfExists(Taxonomy taxonomy) {
		List<Taxonomy> enableTaxonomies = oneXMLConfigPerCollectionManager.get(taxonomy.getCollection()).enableTaxonomies;
		if (enableTaxonomies != null) {
			for (Taxonomy enableTaxonomy : enableTaxonomies) {
				if (enableTaxonomy.getCode().equals(taxonomy.getCode())) {
					throw new TaxonomiesManagerRuntimeException.TaxonomyAlreadyExists(taxonomy.getCode());
				}
				verifyTaxonomiesSchemaTypes(taxonomy);
			}
		}
	}

	void verifyTaxonomiesSchemaTypes(Taxonomy taxonomy) {
		for (String schemaType : taxonomy.getSchemaTypes()) {
			if (getTaxonomyFor(taxonomy.getCollection(), schemaType) != null) {
				throw new TaxonomiesManagerRuntimeException.TaxonomyAlreadyExists(taxonomy.getCode());
			}
		}
	}

	public void createCollectionTaxonomies(String collection) {
		DocumentAlteration createConfigAlteration = new DocumentAlteration() {
			@Override
			public void alter(Document document) {
				TaxonomiesWriter writer = newTaxonomyWriter(document);
				writer.createEmptyTaxonomy();
			}
		};
		oneXMLConfigPerCollectionManager.createCollectionFile(collection, createConfigAlteration);
	}

	@Override
	public void onValueModified(String collection, TaxonomiesManagerCache newValue) {

	}

	public List<Taxonomy> getAvailableTaxonomiesInHomePage(User user) {
		List<Taxonomy> taxonomies = new ArrayList<>();

		for (Taxonomy taxonomy : getEnabledTaxonomies(user.getCollection())) {
			if (taxonomy.isVisibleInHomePage()) {
				boolean visibleByUser;
				if (taxonomy.getUserIds().isEmpty() && taxonomy.getGroupIds().isEmpty()) {
					visibleByUser = true;
				} else {
					boolean userInList = taxonomy.getUserIds().contains(user.getId());
					boolean groupInList = false;
					for (String aUserGroupId : user.getUserGroups()) {
						groupInList |= taxonomy.getGroupIds().contains(aUserGroupId);
					}
					visibleByUser = userInList || groupInList;
				}
				if (visibleByUser) {
					taxonomies.add(taxonomy);
				}
			}
		}

		return sortTaxonomies(taxonomies, user);
	}

	private List<Taxonomy> sortTaxonomies(List<Taxonomy> taxonomies, User user) {
		final List<String> taxonomiesInOrder = new ArrayList<>();

		String globalConfigOrder = eimConfigs.getTaxonomyOrderInHomeView();
		List<String> userTaxonomyDisplayOrder = user.getTaxonomyDisplayOrder();
		if (userTaxonomyDisplayOrder != null && !userTaxonomyDisplayOrder.isEmpty()) {
			taxonomiesInOrder.addAll(userTaxonomyDisplayOrder);
		} else if (globalConfigOrder != null) {
			taxonomiesInOrder.addAll(asList(globalConfigOrder.replaceAll("\\s", "").split(",")));
		}

		Collections.sort(taxonomies, new Comparator<Taxonomy>() {
			@Override
			public int compare(Taxonomy o1, Taxonomy o2) {
				int index1 = taxonomiesInOrder.indexOf(o1.getCode());
				int index2 = taxonomiesInOrder.indexOf(o2.getCode());

				if (index1 == -1) {
					return 1;
				} else if (index2 == -1) {
					return -1;
				} else {
					return index1 - index2;
				}
			}
		});
		return taxonomies;
	}

	public List<Taxonomy> getAvailableTaxonomiesForSchema(String schemaCode, User user,
														  MetadataSchemasManager metadataSchemasManager) {

		Set<Taxonomy> taxonomies = new HashSet<>();

		SchemaUtils schemaUtils = new SchemaUtils();

		String schemaType = schemaUtils.getSchemaTypeCode(schemaCode);

		List<Taxonomy> schemaTaxonomies = getAvailableTaxonomiesForSelectionOfType(schemaType, user, metadataSchemasManager);
		taxonomies.addAll(schemaTaxonomies);

		return sortTaxonomies(new ArrayList<>(taxonomies), user);
	}

	public List<Taxonomy> getAvailableTaxonomiesForSelectionOfType(String schemaType, User user,
																   MetadataSchemasManager metadataSchemasManager) {

		MetadataSchemaTypes types = metadataSchemasManager.getSchemaTypes(user.getCollection());

		Taxonomy taxonomyWithType = getTaxonomyFor(user.getCollection(), schemaType);
		if (taxonomyWithType != null) {
			return sortTaxonomies(asList(taxonomyWithType), user);
		} else {

			MetadataSchemaType type = types.getSchemaType(schemaType);

			List<Taxonomy> taxonomies = new ArrayList<>();
			Set<String> taxonomyCodes = new HashSet<>();

			for (Metadata metadata : type.getAllMetadatas()) {
				if (metadata.getType() == MetadataValueType.REFERENCE && metadata.isTaxonomyRelationship()) {
					String referenceTypeCode = metadata.getAllowedReferences().getTypeWithAllowedSchemas();
					Taxonomy taxonomy = getTaxonomyFor(user.getCollection(), referenceTypeCode);
					if (taxonomy != null && hasCurrentUserRightsOnTaxonomy(taxonomy, user)) {
						if (taxonomyCodes.add(taxonomy.getCode())) {
							taxonomies.add(taxonomy);
						}
					}
				}
			}

			for (Metadata metadata : type.getAllMetadatas()) {
				if (metadata.getType() == MetadataValueType.REFERENCE && metadata.isChildOfRelationship()) {
					String referenceTypeCode = metadata.getAllowedReferences().getTypeWithAllowedSchemas();
					if (!referenceTypeCode.equals(type.getCode())) {
						for (Taxonomy taxonomy : getAvailableTaxonomiesForSelectionOfType(referenceTypeCode, user, metadataSchemasManager)) {
							if (taxonomyCodes.add(taxonomy.getCode())) {
								taxonomies.add(taxonomy);
							}
						}
					}
				}
			}

			return sortTaxonomies(new ArrayList<>(taxonomies), user);
		}

	}

	private boolean hasCurrentUserRightsOnTaxonomy(Taxonomy taxonomy, User currentUser) {
		String userid = currentUser.getId();

		if (taxonomy != null) {
			List<String> taxonomyGroupIds = taxonomy.getGroupIds();
			List<String> taxonomyUserIds = taxonomy.getUserIds();
			List<String> userGroups = currentUser.getUserGroups();
			for (String group : taxonomyGroupIds) {
				for (String userGroup : userGroups) {
					if (userGroup.equals(group)) {
						return true;
					}
				}
			}
			return (taxonomyGroupIds.isEmpty() && taxonomyUserIds.isEmpty()) || taxonomyUserIds.contains(userid);
		} else {
			return true;
		}
	}

	public boolean isTypeInPrincipalTaxonomy(MetadataSchemaType type) {
		return isTypeInPrincipalTaxonomy(type.getCollection(), type.getCode());
	}

	public boolean isTypeInPrincipalTaxonomy(String collection, String typeCode) {
		Taxonomy typeTaxonomy = getTaxonomyFor(collection, typeCode);
		if (typeTaxonomy == null) {
			return false;
		} else {
			Taxonomy principalTaxonomy = getPrincipalTaxonomy(collection);
			return principalTaxonomy != null && principalTaxonomy.getCode().equals(typeTaxonomy.getCode());
		}
	}

	public static class TaxonomiesManagerCache implements Serializable {
		final Taxonomy principalTaxonomy;
		final List<Taxonomy> enableTaxonomies;
		final List<Taxonomy> disableTaxonomies;

		TaxonomiesManagerCache(Taxonomy principalTaxonomy, List<Taxonomy> enableTaxonomies,
							   List<Taxonomy> disableTaxonomies) {
			this.principalTaxonomy = principalTaxonomy;
			this.enableTaxonomies = enableTaxonomies;
			this.disableTaxonomies = disableTaxonomies;
		}
	}

	@Override
	public void close() {

	}
}
