package com.constellio.data.dao.services.solr.serverFactories;

@SuppressWarnings("serial")
public class ServerReloadException extends RuntimeException{

	public ServerReloadException(Exception e) {
		super(e);
	}

}
