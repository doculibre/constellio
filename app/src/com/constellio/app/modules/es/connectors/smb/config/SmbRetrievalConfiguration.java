package com.constellio.app.modules.es.connectors.smb.config;

import java.util.List;

public class SmbRetrievalConfiguration {
	private List<String> seeds;
	private List<String> inclusions;
	private List<String> exclusions;
	private boolean skipSharePermissions;
	private boolean skipContentAndAcl;

	public SmbRetrievalConfiguration(List<String> seeds, List<String> inclusions, List<String> exclusions,
									 boolean skipSharePermissions, boolean skipContentAndACL) {
		this.seeds = seeds;
		this.inclusions = inclusions;
		this.exclusions = exclusions;
		this.skipSharePermissions = skipSharePermissions;
		this.skipContentAndAcl = skipContentAndACL;
	}

	public List<String> getSeeds() {
		return seeds;
	}

	public SmbRetrievalConfiguration setSeeds(List<String> seeds) {
		this.seeds = seeds;
		return this;
	}

	public List<String> getInclusions() {
		return inclusions;
	}

	public SmbRetrievalConfiguration setInclusions(List<String> inclusions) {
		this.inclusions = inclusions;
		return this;
	}

	public List<String> getExclusions() {
		return exclusions;
	}

	public SmbRetrievalConfiguration setExclusions(List<String> exclusions) {
		this.exclusions = exclusions;
		return this;
	}

	public boolean isSkipSharePermissions() {
		return skipSharePermissions;
	}

	public boolean isSkipContentAndAcl() {
		return skipContentAndAcl;
	}
}