package com.constellio.data.conf;

public enum HashingEncoding {

	//Encoding of constellio instances created with version 6.4 and less
	BASE64,

	//Encoding of constellio instances created with version 6.5 and more
	BASE64_URL_ENCODED,

	//Encoding of constellio instances created on case insensitive drives
	BASE32;

}
