package com.constellio.app.services.sip.zip;

import java.io.File;

public interface SIPFileNameProvider {

	File newSIPFile(int index);

	String newSIPName(int index);

}
