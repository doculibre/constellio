package com.constellio.app.ui.pages.management.updates;

public class LinuxOperation {

	String Behavior;
	String Command;


	LinuxOperation(String commande,String behavior){

		this.Command=commande;
		this.Behavior=behavior;
	}


	String getOperationBehavior() {

		return Behavior;

	}
	String getOperationCommand() {

		return Command;

	}





}
