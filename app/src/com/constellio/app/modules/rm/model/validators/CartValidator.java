package com.constellio.app.modules.rm.model.validators;

import com.constellio.app.modules.rm.RMConfigs;
import com.constellio.app.modules.rm.wrappers.Cart;
import com.constellio.app.modules.rm.wrappers.ContainerRecord;
import com.constellio.app.modules.rm.wrappers.StorageSpace;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.entities.schemas.validation.RecordValidator;
import com.constellio.model.services.records.RecordProvider;
import com.constellio.model.services.records.RecordValidatorParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartValidator implements RecordValidator {

	private static final Logger LOGGER = LoggerFactory.getLogger(CartValidator.class);
	public static final String CART_CANNOT_CONTAIN_MORE_THAN_A_THOUSAND_FOLDERS = "cartCannotContainMoreThanAThousandFolders";
	public static final String CART_CANNOT_CONTAIN_MORE_THAN_A_THOUSAND_DOCUMENTS = "cartCannotContainMoreThanAThousandDocuments";
	public static final String CART_CANNOT_CONTAIN_MORE_THAN_A_THOUSAND_CONTAINERS = "cartCannotContainMoreThanAThousandContainers";
	public static final String NUMBER_OF_RECORDS = "numberOfRecords";

	@Override
	public void validate(RecordValidatorParams params) {
		Cart cart = new Cart(params.getValidatedRecord(), params.getTypes());
		validate(cart, params);
	}

	private void validate(Cart cart, RecordValidatorParams params) {
		List<String> folders = cart.getFolders();
		List<String> documents = cart.getDocuments();
		List<String> containers = cart.getContainers();

		if(folders != null && folders.size() > 1000) {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put(NUMBER_OF_RECORDS, formatToParameter(folders.size()));
			params.getValidationErrors().add(CartValidator.class, CART_CANNOT_CONTAIN_MORE_THAN_A_THOUSAND_FOLDERS, parameters);
		}

		if(documents != null && documents.size() > 1000) {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put(NUMBER_OF_RECORDS, formatToParameter(documents.size()));
			params.getValidationErrors().add(CartValidator.class, CART_CANNOT_CONTAIN_MORE_THAN_A_THOUSAND_DOCUMENTS, parameters);
		}

		if(containers != null && containers.size() > 1000) {
			Map<String, Object> parameters = new HashMap<>();
			parameters.put(NUMBER_OF_RECORDS, formatToParameter(containers.size()));
			params.getValidationErrors().add(CartValidator.class, CART_CANNOT_CONTAIN_MORE_THAN_A_THOUSAND_CONTAINERS, parameters);
		}
	}

	private String formatToParameter(Object parameter) {
		if(parameter == null) {
			return "";
		}
		return parameter.toString();
	}
}
