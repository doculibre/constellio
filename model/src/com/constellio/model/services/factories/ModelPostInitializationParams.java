package com.constellio.model.services.factories;

public class ModelPostInitializationParams {

	boolean rebuildCacheFromSolr;

	Runnable cacheLoadingFinishedCallback;

	public boolean isRebuildCacheFromSolr() {
		return rebuildCacheFromSolr;
	}

	public ModelPostInitializationParams setRebuildCacheFromSolr(boolean rebuildCacheFromSolr) {
		this.rebuildCacheFromSolr = rebuildCacheFromSolr;
		return this;
	}

	public Runnable getCacheLoadingFinishedCallback() {
		return cacheLoadingFinishedCallback;
	}

	public ModelPostInitializationParams setCacheLoadingFinishedCallback(Runnable cacheLoadingFinishedCallback) {
		this.cacheLoadingFinishedCallback = cacheLoadingFinishedCallback;
		return this;
	}
}
