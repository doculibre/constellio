package com.constellio.app.modules.rm.model.validators;

import com.constellio.app.modules.rm.wrappers.Cart;
import com.constellio.model.entities.schemas.validation.RecordValidator;
import com.constellio.model.services.records.RecordValidatorParams;

public class CartValidator implements RecordValidator {

	@Override
	public void validate(RecordValidatorParams params) {
		Cart cart = new Cart(params.getValidatedRecord(), params.getTypes());
		validate(cart, params);
	}

	private void validate(Cart cart, RecordValidatorParams params) {
	}
}
