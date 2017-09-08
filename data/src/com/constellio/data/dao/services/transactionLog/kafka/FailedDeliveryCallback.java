package com.constellio.data.dao.services.transactionLog.kafka;

public interface FailedDeliveryCallback {
	public void onFailedDelivery(Throwable e);
}
