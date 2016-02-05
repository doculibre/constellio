package com.constellio.app.ui.tools;

import java.util.concurrent.atomic.AtomicReference;

public class ServerThrowableContext {

	public static AtomicReference<Throwable> LAST_THROWABLE = new AtomicReference<Throwable>();

}
