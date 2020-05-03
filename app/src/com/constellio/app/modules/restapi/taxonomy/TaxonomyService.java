package com.constellio.app.modules.restapi.taxonomy;

import com.constellio.app.modules.restapi.core.dao.BaseDao;
import com.constellio.app.modules.restapi.core.service.BaseService;
import com.constellio.app.modules.restapi.taxonomy.dao.TaxonomyDao;
import com.constellio.app.modules.restapi.taxonomy.dto.TaxonomyDto;
import com.constellio.app.modules.restapi.taxonomy.dto.TaxonomyNodeDto;
import com.constellio.app.modules.restapi.validation.ValidationService;
import com.constellio.app.modules.restapi.validation.exception.UnauthorizedAccessException;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemasRuntimeException;
import com.constellio.model.services.taxonomies.TaxonomySearchRecord;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class TaxonomyService extends BaseService {

	@Inject
	private TaxonomyDao taxonomyDao;

	@Inject
	private ValidationService validationService;

	@Override
	protected BaseDao getDao() {
		return taxonomyDao;
	}

	public List<TaxonomyDto> getTaxonomies(String host, String token, String serviceKey, String collection,
										   String schemaTypeCode, String username) {
		validateCacheIsLoaded();
		validationService.validateHost(host);
		validationService.validateToken(token, serviceKey);
		validationService.validateCollection(collection);
		validateSchemaTypeCode(collection, schemaTypeCode);

		User user = username != null ? getUserByUsername(username, collection) : getUserByServiceKey(serviceKey, collection);
		if (!userHasCollectionReadAccess(username, serviceKey, collection)) {
			throw new UnauthorizedAccessException();
		}

		List<Taxonomy> taxonomies = new ArrayList<>();
		// FIXME better way?
		if (schemaTypeCode != null && schemaTypeCode.startsWith("taxo")) {
			taxonomies.add(taxonomyDao.getTaxonomyBySchemaTypeCode(collection, schemaTypeCode));
		} else {
			taxonomies.addAll(taxonomyDao.getTaxonomies(user, schemaTypeCode));
		}

		return taxonomies.stream().map(taxonomy -> TaxonomyDto.builder()
				.code(taxonomy.getCode())
				.schemaTypes(taxonomy.getSchemaTypes())
				.titles(taxonomy.getTitle().entrySet().stream()
						.collect(Collectors.toMap(entry -> entry.getKey().getCode(), Map.Entry::getValue)))
				.build())
				.collect(Collectors.toList());
	}

	public List<TaxonomyNodeDto> getTaxonomyNodes(String host, String token, String serviceKey, String id,
												  String collection, String schemaTypeCode, String username,
												  Set<String> metadatas, Integer rowsStart, Integer rowsLimit,
												  Boolean requireWriteAccess) {
		validateCacheIsLoaded();
		validationService.validateHost(host);
		validationService.validateToken(token, serviceKey);
		validationService.validateCollection(collection);
		validateSchemaTypeCode(collection, schemaTypeCode);

		User user = username != null ? getUserByUsername(username, collection) : getUserByServiceKey(serviceKey, collection);
		if (!userHasCollectionReadAccess(username, serviceKey, collection)) {
			throw new UnauthorizedAccessException();
		}

		Record parent = null;
		Taxonomy taxonomy = taxonomyDao.getTaxonomyByCode(collection, id);
		if (taxonomy == null) {
			parent = getRecord(id, true);
		}

		MetadataSchemaType schemaType = schemaTypeCode != null ?
										taxonomyDao.getMetadataSchemaType(collection, schemaTypeCode) :
										null;

		List<TaxonomySearchRecord> taxonomyNodes = taxonomyDao.getTaxonomyNodes(taxonomy, parent, user, schemaType,
				rowsStart, rowsLimit, requireWriteAccess);
		return taxonomyNodes.stream()
				.map(node -> TaxonomyNodeDto.builder()
						.id(node.getId())
						.schemaType(node.getRecord().getSchemaCode().substring(0, node.getRecord().getSchemaCode().indexOf("_")))
						.hasChildren(node.hasChildren())
						.linkable(node.isLinkable())
						.metadatas(getMetadatas(node, metadatas))
						.build())
				.collect(Collectors.toList());
	}

	private void validateSchemaTypeCode(String collection, String schemaTypeCode) {
		if (schemaTypeCode != null) {
			taxonomyDao.getMetadataSchemaType(collection, schemaTypeCode);
		}
	}

	private void validateCacheIsLoaded() {
		if (!taxonomyDao.areSummaryCacheLoaded()) {
			throw new RuntimeException("Summary caches are not initialized");
		}
	}

	private Map<String, String> getMetadatas(TaxonomySearchRecord taxonomyNode, Set<String> metadatas) {
		Map<String, String> metadataValues = new HashMap<>();

		MetadataSchema schema = getDao().getMetadataSchema(taxonomyNode.getRecord());
		metadatas.forEach(metadata -> {
			try {
				Metadata currentMetadata = getDao().getMetadata(schema, metadata);
				metadataValues.put(metadata, taxonomyNode.getRecord().get(currentMetadata));
			} catch (MetadataSchemasRuntimeException.NoSuchMetadata ignored) {
			}
		});
		return metadataValues;
	}

	private boolean userHasCollectionReadAccess(String username, String serviceKey, String collection) {
		if (username != null) {
			User authentifiedUser = getUserByServiceKey(serviceKey, collection);
			if (!authentifiedUser.getUsername().equals(username)) {
				return authentifiedUser.hasCollectionReadAccess();
			}
		}
		return true;
	}
}
