package com.constellio.model.services.taxonomies;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jdom2.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.constellio.model.entities.schemas.Schemas;
import com.constellio.model.services.batch.manager.BatchProcessesManager;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.records.cache.CacheConfig;
import com.constellio.model.services.records.cache.RecordsCaches;
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

import java.util.*;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static java.util.Arrays.asList;

public class TaxonomiesManager implements StatefulService, OneXMLConfigPerCollectionManagerListener<TaxonomiesManagerCache> {

	public static final String TAXONOMIES_CONFIG = "/taxonomies.xml";
	private static final Logger LOGGER = LoggerFactory.getLogger(FileSystemConfigManager.class);

	private final RecordsCaches recordsCaches;
	private final SearchServices searchServices;
	private final ConfigManager configManager;
	private final CollectionsListManager collectionsListManager;
	private ConstellioCacheManager cacheManager;
	private final BatchProcessesManager batchProcessesManager;
	private OneXMLConfigPerCollectionManager<TaxonomiesManagerCache> oneXMLConfigPerCollectionManager;

	public TaxonomiesManager(ConfigManager configManager, SearchServices searchServices,
			BatchProcessesManager batchProcessesManager, CollectionsListManager collectionsListManager,
			RecordsCaches recordsCaches, ConstellioCacheManager cacheManager) {
		this.searchServices = searchServices;
		this.configManager = configManager;
		this.collectionsListManager = collectionsListManager;
		this.batchProcessesManager = batchProcessesManager;
		this.recordsCaches = recordsCaches;
		this.cacheManager = cacheManager;
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
				TaxonomiesReader reader = newTaxonomyReader(document);
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

		createCacheForTaxonomyTypes(taxonomy, schemasManager, collection);
	}

	void createCacheForTaxonomyTypes(Taxonomy taxonomy, MetadataSchemasManager schemasManager, String collection) {
		for (String schemaType : taxonomy.getSchemaTypes()) {
			MetadataSchemaType type = schemasManager.getSchemaTypes(collection).getSchemaType(schemaType);
			recordsCaches.getCache(collection).configureCache(CacheConfig.permanentCache(type));
		}
	}

	public void editTaxonomy(Taxonomy taxonomy) {
		String collection = taxonomy.getCollection();
		oneXMLConfigPerCollectionManager.updateXML(collection, newEditTaxonomyDocumentAlteration(taxonomy));
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

		List<Metadata> metadatas = asList(Schemas.PRINCIPAL_PATH);
		List<MetadataSchemaType> types = schemasManager.getSchemaTypes(taxonomy.getCollection())
				.getSchemaTypesWithCode(taxonomy.getSchemaTypes());

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

		MetadataSchemaTypes types = schemasManager.getSchemaTypes(taxonomy.getCollection());
		for (MetadataSchemaType type : types.getSchemaTypes()) {
			for (Metadata metadata : type.getAllReferencesToTaxonomySchemas(asList(taxonomy))) {
				if (metadata.isMultivalue()) {
					throw new TaxonomySchemaIsReferencedInMultivalueReference();
				}
			}
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

	TaxonomiesReader newTaxonomyReader(Document document) {
		return new TaxonomiesReader(document);
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
				newTaxonomyWriter(document).editTaxonmy(taxonomy);
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

		return taxonomies;
	}

	public List<Taxonomy> getAvailableTaxonomiesForSchema(String schemaCode, User user,
			MetadataSchemasManager metadataSchemasManager) {

		Set<Taxonomy> taxonomies = new HashSet<>();

		SchemaUtils schemaUtils = new SchemaUtils();

		String schemaType = schemaUtils.getSchemaTypeCode(schemaCode);

		List<Taxonomy> schemaTaxonomies = getAvailableTaxonomiesForSelectionOfType(schemaType, user, metadataSchemasManager);
		taxonomies.addAll(schemaTaxonomies);

		return new ArrayList<>(taxonomies);
	}

	public List<Taxonomy> getAvailableTaxonomiesForSelectionOfType(String schemaType, User user,
			MetadataSchemasManager metadataSchemasManager) {

		MetadataSchemaTypes types = metadataSchemasManager.getSchemaTypes(user.getCollection());

		Taxonomy taxonomyWithType = getTaxonomyFor(user.getCollection(), schemaType);
		if (taxonomyWithType != null) {
			return asList(taxonomyWithType);
		} else {

			MetadataSchemaType type = types.getSchemaType(schemaType);

			Set<Taxonomy> taxonomies = new HashSet<>();

			for (Metadata metadatas : type.getAllMetadatas().onlyTaxonomyReferences()) {
				String referenceTypeCode = metadatas.getAllowedReferences().getTypeWithAllowedSchemas();
				Taxonomy taxonomy = getTaxonomyFor(user.getCollection(), referenceTypeCode);
				if(hasCurrentUserRightsOnTaxonomy(taxonomy, user)) {
					taxonomies.add(taxonomy);
				}
			}

			for (Metadata metadatas : type.getAllMetadatas().onlyParentReferences()) {
				String referenceTypeCode = metadatas.getAllowedReferences().getTypeWithAllowedSchemas();
				if (!referenceTypeCode.equals(type.getCode())) {
					taxonomies.addAll(getAvailableTaxonomiesForSelectionOfType(referenceTypeCode, user, metadataSchemasManager));
				}
			}

			return new ArrayList<>(taxonomies);
		}

	}

	private boolean hasCurrentUserRightsOnTaxonomy(Taxonomy taxonomy, User currentUser) {
		String userid = currentUser.getId();

		if(taxonomy != null) {
			List<String> taxonomyGroupIds = taxonomy.getGroupIds();
			List<String> taxonomyUserIds = taxonomy.getUserIds();
			List<String> userGroups = currentUser.getUserGroups();
			for(String group: taxonomyGroupIds) {
				for(String userGroup: userGroups) {
					if(userGroup.equals(group)) {
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

		TaxonomiesManagerCache(Taxonomy principalTaxonomy, List<Taxonomy> enableTaxonomies, List<Taxonomy> disableTaxonomies) {
			this.principalTaxonomy = principalTaxonomy;
			this.enableTaxonomies = enableTaxonomies;
			this.disableTaxonomies = disableTaxonomies;
		}
	}

	@Override
	public void close() {

	}
}
