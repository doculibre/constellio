package com.constellio.app.ui.pages.management.updates;

public class LinuxOperation {

	String Behavior;
	String Command;


	public LinuxOperation(String commande, String behavior){

		this.Command=commande;
		this.Behavior=behavior;
	}


	public String getOperationBehavior() {

		return Behavior;

	}
	public String getOperationCommand() {

		return Command;

	}





}
