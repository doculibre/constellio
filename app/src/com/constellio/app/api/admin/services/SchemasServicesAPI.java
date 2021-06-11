package com.constellio.app.api.admin.services;

import com.constellio.app.client.entities.MetadataResource;
import com.constellio.app.ui.application.ConstellioUI;
import com.constellio.model.entities.Language;
import com.constellio.model.entities.Taxonomy;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.MetadataValueType;
import com.constellio.model.entities.schemas.entries.CalculatedDataEntry;
import com.constellio.model.entities.schemas.entries.DataEntryType;
import com.constellio.model.entities.schemas.validation.RecordMetadataValidator;
import com.constellio.model.entities.schemas.validation.RecordValidator;
import com.constellio.model.services.schemas.MetadataSchemasManager;
import com.constellio.model.services.schemas.MetadataSchemasManagerException.OptimisticLocking;
import com.constellio.model.services.schemas.SchemaUtils;
import com.constellio.model.services.schemas.builders.MetadataBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaBuilderRuntimeException.NoSuchMetadata;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypeBuilder;
import com.constellio.model.services.schemas.builders.MetadataSchemaTypesBuilder;
import com.constellio.model.services.taxonomies.TaxonomiesManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Path("schemas")
@Consumes("application/xml")
@Produces("application/xml")
public class SchemasServicesAPI {

	private static Logger LOGGER = LoggerFactory.getLogger(SchemasServicesAPI.class);

	@GET
	@Path("getSchemaTypes")
	@Produces(MediaType.APPLICATION_JSON)
	public List<String> getSchemaTypes(@QueryParam("collection") String collection) {
		logServiceCall("getSchemaTypes", collection);
		List<String> schemaTypes = new ArrayList<>();
		for (MetadataSchemaType type : types(collection).getSchemaTypes()) {
			schemaTypes.add(type.getCode());
		}
		return schemaTypes;
	}

	@GET
	@Path("getCustomSchemas")
	@Produces(MediaType.APPLICATION_JSON)
	public List<String> getSchemas(@QueryParam("collection") String collection,
								   @QueryParam("schemaType") String schemaType) {
		logServiceCall("getCustomSchemas", collection, schemaType);
		List<String> schemas = new ArrayList<>();
		for (MetadataSchema schema : types(collection).getSchemaType(schemaType).getAllSchemas()) {
			schemas.add(schema.getCode());
		}
		return schemas;
	}

	@GET
	@Path("getSchemaMetadataCodes")
	@Produces(MediaType.APPLICATION_JSON)
	public List<String> getSchemaMetadataCodes(@QueryParam("collection") String collection,
											   @QueryParam("schemaCode") String schemaCode) {
		logServiceCall("getSchemaMetadataCodes", collection, schemaCode);
		List<String> metadataCodes = new ArrayList<>();
		for (Metadata metadata : types(collection).getSchema(schemaCode).getMetadatas()) {
			metadataCodes.add(metadata.getCode());
		}
		return metadataCodes;
	}

	@GET
	@Path("getSchemaValidators")
	@Produces(MediaType.APPLICATION_JSON)
	public List<String> getSchemaValidators(@QueryParam("collection") String collection,
											@QueryParam("schemaCode") String schemaCode) {
		logServiceCall("getSchemaValidators", collection, schemaCode);
		List<String> validators = new ArrayList<>();
		for (RecordValidator validator : types(collection).getSchema(schemaCode).getValidators()) {
			validators.add(validator.getClass().getCanonicalName());
		}
		return validators;
	}

	@GET
	@Path("getDataStoreFields")
	@Produces(MediaType.APPLICATION_JSON)
	public MetadataResource getMetadata(@QueryParam("collection") String collection, @QueryParam("code") String code) {
		logServiceCall("getDataStoreFields", collection, code);
		Metadata metadata = types(collection).getMetadata(code);
		return toMetadataResource(metadata);
	}

	@GET
	@Path("getTaxonomies")
	@Produces(MediaType.APPLICATION_JSON)
	public List<String> getTaxonomies(@QueryParam("collection") String collection) {
		logServiceCall("getTaxonomies", collection);
		List<String> taxonomyCodes = new ArrayList<>();
		for (Taxonomy taxonomy : taxonomyManager().getEnabledTaxonomies(collection)) {
			taxonomyCodes.add(taxonomy.getCode());
		}
		return taxonomyCodes;
	}

	@GET
	@Path("getPrincipalTaxonomy")
	@Produces(MediaType.TEXT_PLAIN)
	public String getPrincipalTaxonomy(@QueryParam("collection") String collection) {
		logServiceCall("getPrincipalTaxonomy", collection);
		Taxonomy principalTaxonomy = taxonomyManager().getPrincipalTaxonomy(collection);
		return principalTaxonomy == null ? null : principalTaxonomy.getCode();
	}

	@GET
	@Path("getTaxonomySchemaTypes")
	@Produces(MediaType.APPLICATION_JSON)
	public List<String> getTaxonomySchemaTypes(@QueryParam("collection") String collection,
											   @QueryParam("taxonomyCode") String taxonomyCode) {
		logServiceCall("getTaxonomySchemaTypes", collection, taxonomyCode);
		Taxonomy taxonomy = taxonomyManager().getEnabledTaxonomyWithCode(collection, taxonomyCode);
		return taxonomy.getSchemaTypes();
	}

	@POST
	@Path("createSchemaType")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.TEXT_PLAIN)
	public String createSchemaType(@QueryParam("collection") String collection,
								   @QueryParam("schemaTypeCode") String schemaTypeCode) {
		logServiceCall("createSchemaType", collection, schemaTypeCode);
		MetadataSchemaTypesBuilder typesBuilder = typesBuilder(collection);
		typesBuilder.createNewSchemaTypeWithSecurity(schemaTypeCode);
		save(typesBuilder);
		return "OK";
	}

	@POST
	@Path("createCustomSchema")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.TEXT_PLAIN)
	public String createCustomSchema(@QueryParam("collection") String collection,
									 @QueryParam("schemaCode") String schemaCode) {
		logServiceCall("createCustomSchema", collection, schemaCode);
		String schemaTypeCode = schemaUtils().getSchemaTypeCode(schemaCode);
		String schemaLocalCode = schemaUtils().getSchemaLocalCode(schemaCode);

		MetadataSchemaTypesBuilder typesBuilder = typesBuilder(collection);
		typesBuilder.getSchemaType(schemaTypeCode).createCustomSchema(schemaLocalCode);
		save(typesBuilder);
		return "OK";
	}

	@POST
	@Path("addSchemaValidator")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.TEXT_PLAIN)
	public String addSchemaValidator(@QueryParam("collection") String collection,
									 @QueryParam("schemaCode") String schemaCode,
									 @QueryParam("schemaValidator") String schemaValidator) {
		logServiceCall("getSchemaTypes", collection, schemaCode);
		MetadataSchemaTypesBuilder typesBuilder = typesBuilder(collection);
		typesBuilder.getSchema(schemaCode).defineValidators().add(schemaValidator);
		save(typesBuilder);
		return "OK";
	}

	@POST
	@Path("removeSchemaValidator")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.TEXT_PLAIN)
	public String removeSchemaValidator(@QueryParam("collection") String collection,
										@QueryParam("schemaCode") String schemaCode,
										@QueryParam("schemaValidator") String schemaValidator) {
		logServiceCall("removeSchemaValidator", collection, schemaCode);
		MetadataSchemaTypesBuilder typesBuilder = typesBuilder(collection);
		typesBuilder.getSchema(schemaCode).defineValidators().remove(schemaValidator);
		save(typesBuilder);
		return "OK";
	}

	@POST
	@Path("addUpdateMetadata")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public String addUpdateMetadata(@QueryParam("collection") String collection, MetadataResource metadataResource) {
		logServiceCall("addUpdateMetadata", collection, metadataResource);
		MetadataSchemaTypesBuilder typesBuilder = typesBuilder(collection);
		String schemaCode = schemaUtils().getSchemaCode(metadataResource.getCode());
		MetadataSchemaBuilder schemaBuilder = typesBuilder.getSchema(schemaCode);

		String metadataCode = schemaUtils().getLocalCode(metadataResource.getCode(), schemaCode);
		try {
			MetadataBuilder metadata = schemaBuilder.getMetadata(metadataCode);
			configureExitentMetadata(metadata, metadataResource);
		} catch (NoSuchMetadata e) {
			LOGGER.info("No metadata with code {}, creating one", metadataCode, e);
			MetadataBuilder metadata = schemaBuilder.create(metadataCode);
			configureNewMetadata(metadata, metadataResource, typesBuilder);
		}

		save(typesBuilder);
		return "OK";
	}

	private void configureNewMetadata(MetadataBuilder metadata, MetadataResource metadataResource,
									  MetadataSchemaTypesBuilder typesBuilder) {
		configureExitentMetadata(metadata, metadataResource);

		metadata.setType(notNull("type", MetadataValueType.valueOf(metadataResource.getType())));
		if (metadataResource.getCalculator() != null) {
			metadata.defineDataEntry().asCalculated(metadataResource.getCalculator());
		}

		if (metadataResource.getAllowedReference() != null) {
			MetadataSchemaTypeBuilder type = typesBuilder.getSchemaType(metadataResource.getAllowedReference());
			metadata.defineReferencesTo(type);
		}

		if (metadataResource.getChildOfRelationship() != null) {
			metadata.setChildOfRelationship(metadataResource.getChildOfRelationship());
		}
		if (metadataResource.getMultivalue() != null) {
			metadata.setMultivalue(metadataResource.getMultivalue());
		}
		if (metadataResource.getUniqueValue() != null) {
			metadata.setUniqueValue(metadataResource.getUniqueValue());
		}
		if (metadataResource.getSearchable() != null) {
			metadata.setSearchable(metadataResource.getSearchable());
		}

		configureModifiableMetadataAttributes(metadata, metadataResource);
	}

	private void configureExitentMetadata(MetadataBuilder metadata, MetadataResource metadataResource) {
		configureModifiableMetadataAttributes(metadata, metadataResource);
	}

	private void configureModifiableMetadataAttributes(MetadataBuilder metadata, MetadataResource metadataResource) {
		if (metadataResource.getDefaultRequirement() != null) {
			metadata.setDefaultRequirement(metadataResource.getDefaultRequirement());
		}
		if (metadataResource.getEnabled() != null) {
			metadata.setEnabled(metadataResource.getEnabled());
		}
		if (metadataResource.getLabel() != null) {
			Language language = Language.withCode(ConstellioUI.getCurrentSessionContext().getCurrentLocale().getLanguage());
			metadata.addLabel(language, metadataResource.getLabel());
		}

		metadata.defineValidators().set(metadataResource.getValidators());
	}

	@POST
	@Path("disableMetadata")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.TEXT_PLAIN)
	public String disableMetadata(@QueryParam("collection") String collection,
								  @QueryParam("metadataCode") String metadataCode) {
		logServiceCall("disableMetadata", collection, metadataCode);
		MetadataSchemaTypesBuilder typesBuilder = typesBuilder(collection);
		typesBuilder.getMetadata(metadataCode).setEnabled(false);
		save(typesBuilder);
		return "OK";
	}

	@POST
	@Path("createTaxonomy")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.TEXT_PLAIN)
	public String createTaxonomy(@QueryParam("collection") String collection, @QueryParam("code") String code,
								 List<String> schemaTypes) {
		logServiceCall("createTaxonomy", collection, code, schemaTypes);

		Map<Language, String> mapLang = new HashMap<>();
		mapLang.put(Language.French, code);
		Taxonomy taxonomy = Taxonomy.createPublic(code, mapLang, collection, schemaTypes);
		taxonomyManager().addTaxonomy(taxonomy, metadataSchemasManager());

		Map<String, String> queryParams = new HashMap<>();
		queryParams.put("code", code);
		return "OK";
	}

	@POST
	@Path("setAsPrincipalTaxonomy")
	@Consumes(MediaType.TEXT_PLAIN)
	@Produces(MediaType.TEXT_PLAIN)
	public String setAsPrincipalTaxonomy(@QueryParam("collection") String collection, @QueryParam("code") String code) {
		logServiceCall("setAsPrincipalTaxonomy", collection, code);
		Taxonomy taxonomy = taxonomyManager().getEnabledTaxonomyWithCode(collection, code);
		taxonomyManager().setPrincipalTaxonomy(taxonomy, metadataSchemasManager());
		return "OK";
	}

	TaxonomiesManager taxonomyManager() {
		return AdminServicesUtils.modelServicesFactory().getTaxonomiesManager();
	}

	MetadataSchemasManager metadataSchemasManager() {
		return AdminServicesUtils.modelServicesFactory().getMetadataSchemasManager();
	}

	MetadataSchemaTypes types(String collection) {
		return metadataSchemasManager().getSchemaTypes(collection);
	}

	MetadataSchemaTypesBuilder typesBuilder(String collection) {
		return metadataSchemasManager().modify(collection);
	}

	void save(MetadataSchemaTypesBuilder typesBuilder) {
		try {
			metadataSchemasManager().saveUpdateSchemaTypes(typesBuilder);
		} catch (OptimisticLocking optimistickLocking) {
			throw new RuntimeException(optimistickLocking);
		}
	}

	SchemaUtils schemaUtils() {
		return new SchemaUtils();
	}

	private MetadataResource toMetadataResource(Metadata metadata) {
		MetadataResource resource = new MetadataResource();
		resource.setCode(metadata.getCode());
		resource.setDataStoreCode(metadata.getDataStoreCode());
		Language language = Language.withCode(ConstellioUI.getCurrentSessionContext().getCurrentLocale().getLanguage());
		resource.setLabel(metadata.getLabel(language));
		resource.setType(metadata.getType().name());
		resource.setChildOfRelationship(metadata.isChildOfRelationship());
		if (metadata.getAllowedReferences() != null) {
			resource.setAllowedReference(metadata.getAllowedReferences().getAllowedSchemaType());
		}
		resource.setSearchable(metadata.isSearchable());
		resource.setEnabled(metadata.isEnabled());
		resource.setMultivalue(metadata.isMultivalue());
		if (metadata.getDataEntry().getType() == DataEntryType.CALCULATED) {
			CalculatedDataEntry entry = (CalculatedDataEntry) metadata.getDataEntry();
			resource.setCalculator(entry.getCalculator().getClass().getCanonicalName());
		}
		resource.setDefaultRequirement(metadata.isDefaultRequirement());
		resource.setUniqueValue(metadata.isUniqueValue());

		List<String> validators = new ArrayList<>();
		for (RecordMetadataValidator validator : metadata.getValidators()) {
			validators.add(validator.getClass().getCanonicalName());
		}

		resource.setValidators(validators);

		return resource;
	}

	private <T> T notNull(String name, T value) {
		if (value == null) {
			throw new RuntimeException("Value is required or invalid for '" + name + "'");
		}
		return value;
	}

	private void logServiceCall(String serviceName, Object... params) {
		StringBuilder serviceCall = new StringBuilder();
		serviceCall.append(serviceName + " called with {");

		boolean first = true;
		for (Object object : params) {
			if (!first) {
				serviceCall.append(", ");
			}
			serviceCall.append(object);
			first = false;
		}

		serviceCall.append("}");
		LOGGER.info(serviceCall.toString());
	}
}
