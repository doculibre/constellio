package com.constellio.model.services.background;

import java.util.Date;

public class DownloadLastServerStateBackgroundAction implements Runnable {
	@Override
	public void run() {
		System.out.println("DownloadLastServerStateBackgroundAction @ " + new Date());
	}
}
