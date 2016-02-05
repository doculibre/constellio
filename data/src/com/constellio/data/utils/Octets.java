package com.constellio.data.utils;

public class Octets {

	private final long octets;

	private Octets(long octets) {
		this.octets = octets;
	}

	public static Octets octets(long octets) {
		return new Octets(octets);
	}

	public static Octets kilooctets(long kilooctets) {
		return octets(kilooctets * 1024);
	}

	public static Octets megaoctets(long megaoctet) {
		return kilooctets(megaoctet * 1024);
	}

	public static Octets gigaoctets(long gigaoctet) {
		return megaoctets(gigaoctet * 1024);
	}

	public long getOctets() {
		return octets;
	}
}
