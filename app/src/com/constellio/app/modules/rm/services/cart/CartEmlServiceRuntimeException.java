package com.constellio.app.modules.rm.services.cart;

public class CartEmlServiceRuntimeException extends RuntimeException {
	public CartEmlServiceRuntimeException(Exception e) {
		super(e);
	}

	public static class CartEmlServiceRuntimeException_InvalidRecordId extends CartEmlServiceRuntimeException {
		public CartEmlServiceRuntimeException_InvalidRecordId(
				Exception e) {
			super(e);
		}
	}
}
