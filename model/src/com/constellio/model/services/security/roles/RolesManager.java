package com.constellio.model.services.security.roles;

import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.dao.managers.config.ConfigManager;
import com.constellio.data.dao.managers.config.DocumentAlteration;
import com.constellio.data.dao.services.cache.ConstellioCache;
import com.constellio.data.dao.services.cache.ConstellioCacheManager;
import com.constellio.model.entities.security.Role;
import com.constellio.model.frameworks.validation.ValidationErrors;
import com.constellio.model.services.collections.CollectionsListManager;
import com.constellio.model.services.factories.ModelLayerFactory;
import com.constellio.model.services.records.SchemasRecordsServices;
import com.constellio.model.services.security.RoleValidator;
import com.constellio.model.services.security.roles.RolesManagerRuntimeException.RolesManagerRuntimeException_Validation;
import com.constellio.model.utils.OneXMLConfigPerCollectionManager;
import com.constellio.model.utils.OneXMLConfigPerCollectionManagerListener;
import com.constellio.model.utils.XMLConfigReader;
import org.jdom2.Document;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RolesManager implements StatefulService, OneXMLConfigPerCollectionManagerListener<List<Role>> {

	public static String ROLES_CONFIG = "/roles.xml";
	private OneXMLConfigPerCollectionManager<List<Role>> oneXMLConfigPerCollectionManager;
	private ConfigManager configManager;
	private CollectionsListManager collectionsListManager;
	private ModelLayerFactory modelLayerFactory;
	private ConstellioCacheManager cacheManager;
	private Map<String, SchemasRecordsServices> schemasRecordsServicesPerCollection = new HashMap<>();

	public RolesManager(ModelLayerFactory modelLayerFactory) {
		this.configManager = modelLayerFactory.getDataLayerFactory().getConfigManager();
		this.modelLayerFactory = modelLayerFactory;
		this.collectionsListManager = modelLayerFactory.getCollectionsListManager();
		this.cacheManager = modelLayerFactory.getDataLayerFactory().getLocalCacheManager();
	}

	@Override
	public void initialize() {
		ConstellioCache cache = cacheManager.getCache(RolesManager.class.getName());
		this.oneXMLConfigPerCollectionManager = new OneXMLConfigPerCollectionManager<>(configManager, collectionsListManager,
				ROLES_CONFIG, xmlConfigReader(), this, cache);
	}

	public void createCollectionRole(String collection) {
		DocumentAlteration createConfigAlteration = new DocumentAlteration() {
			@Override
			public void alter(Document document) {
				RolesManagerWriter writer = newRoleWriter(document);
				writer.createEmptyRoles();
			}
		};
		oneXMLConfigPerCollectionManager.createCollectionFile(collection, createConfigAlteration);
	}

	public Role addRole(final Role role) {
		validate(role.getCollection(), false, role);
		DocumentAlteration alteration = new DocumentAlteration() {
			@Override
			public void alter(Document document) {
				RolesManagerWriter writer = newRoleWriter(document);
				writer.addRole(role);
			}
		};
		String collection = role.getCollection();
		oneXMLConfigPerCollectionManager.updateXML(collection, alteration);
		return role;
	}

	public void deleteRole(final Role role)
			throws RolesManagerRuntimeException {
		validate(role.getCollection(), true, role);
		DocumentAlteration alteration = new DocumentAlteration() {
			@Override
			public void alter(Document document) {
				RolesManagerWriter writer = newRoleWriter(document);
				writer.deleteRole(role);
			}
		};
		String collection = role.getCollection();
		oneXMLConfigPerCollectionManager.updateXML(collection, alteration);
	}

	public void updateRole(final Role role)
			throws RolesManagerRuntimeException {
		validate(role.getCollection(), true, role.getCode());
		DocumentAlteration alteration = new DocumentAlteration() {
			@Override
			public void alter(Document document) {
				RolesManagerWriter writer = newRoleWriter(document);
				writer.updateRole(role);
			}
		};
		oneXMLConfigPerCollectionManager.updateXML(role.getCollection(), alteration);
	}

	public List<Role> getAllRoles(String collection) {
		return oneXMLConfigPerCollectionManager.get(collection);
	}

	public Roles getCollectionRoles(String collection) {
		return getCollectionRoles(collection, modelLayerFactory);
	}

	public Roles getCollectionRoles(String collection, ModelLayerFactory modelLayerFactory) {

		SchemasRecordsServices schemasRecordsServices = schemasRecordsServicesPerCollection.get(collection);
		if (schemasRecordsServices == null) {
			schemasRecordsServices = new SchemasRecordsServices(collection, modelLayerFactory);
			schemasRecordsServicesPerCollection.put(collection, schemasRecordsServices);
		}

		return new Roles(getAllRoles(collection), schemasRecordsServices);
	}

	public Role getRole(String collection, String code)
			throws RolesManagerRuntimeException {

		// TODO quick fix for roles
		if (Role.READ.equals(code)) {
			return Role.READ_ROLE;
		}
		if (Role.WRITE.equals(code)) {
			return Role.WRITE_ROLE;
		}
		if (Role.DELETE.equals(code)) {
			return Role.DELETE_ROLE;
		}

		validate(collection, true, code);

		for (Role role : getAllRoles(collection)) {
			if (role.getCode().equals(code)) {
				return role;
			}
		}
		return null;
	}

	private void validate(String collection, boolean updateValidation, String code)
			throws RolesManagerRuntimeException {
		ValidationErrors validationErrors = new ValidationErrors();
		new RoleValidator(getAllRoles(collection), true).validate(code, validationErrors);

		if (!validationErrors.getValidationErrors().isEmpty()) {
			throw new RolesManagerRuntimeException_Validation(validationErrors);
		}
	}

	private void validate(String collection, boolean updateValidation, Role role)
			throws RolesManagerRuntimeException_Validation {
		ValidationErrors validationErrors = new ValidationErrors();
		new RoleValidator(getAllRoles(collection), updateValidation).validate(role, validationErrors);
		if (!validationErrors.getValidationErrors().isEmpty()) {
			throw new RolesManagerRuntimeException_Validation(validationErrors);
		}
	}

	private RolesManagerWriter newRoleWriter(Document document) {
		return new RolesManagerWriter(document);
	}

	private RolesManagerReader newRoleReader(Document document) {
		return new RolesManagerReader(document);
	}

	private XMLConfigReader<List<Role>> xmlConfigReader() {
		return new XMLConfigReader<List<Role>>() {
			@Override
			public List<Role> read(String collection, Document document) {
				return newRoleReader(document).getAllRoles();
			}
		};
	}

	public boolean hasPermission(String collection, String roleCode, String operationPermission) {
		try {
			Role role = getRole(collection, roleCode);
			return role.hasOperationPermission(operationPermission);
		} catch (RolesManagerRuntimeException rme) {
			return false;
		}
	}

	@Override
	public void onValueModified(String collection, List<Role> newValue) {

	}

	@Override
	public void close() {

	}

}
