package com.constellio.app.ui.framework.components.fields.lookup;

/**
 * Created by constellios on 2017-07-19.
 */
public class LookupStringField extends LookupField<String> {
	public LookupStringField(TextInputDataProvider<String> suggestInputDataProvider,
							 LookupTreeDataProvider<String>[] lookupTreeDataProviders) {
		super(suggestInputDataProvider, lookupTreeDataProviders);
	}

	@Override
	public Class<? extends String> getType() {
		return String.class;
	}
}
