package com.constellio.app.modules.rm.services.cart;

public class CartEmailServiceRuntimeException extends RuntimeException {
	public CartEmailServiceRuntimeException(Exception e) {
		super(e);
	}

	public static class CartEmlServiceRuntimeException_InvalidRecordId extends CartEmailServiceRuntimeException {
		public CartEmlServiceRuntimeException_InvalidRecordId(
				Exception e) {
			super(e);
		}
	}
}
