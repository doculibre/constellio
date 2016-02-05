package com.constellio.data.dao.services.bigVault;

import java.util.Iterator;

public interface SearchResponseIterator<T> extends Iterator<T> {

	long getNumFound();

}
