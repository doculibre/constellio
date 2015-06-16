/*Constellio Enterprise Information Management

Copyright (c) 2015 "Constellio inc."

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program. If not, see <http://www.gnu.org/licenses/>.
*/
package com.constellio.sdk.dev.tools;

import static java.util.Arrays.asList;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.constellio.model.entities.records.wrappers.User;
import com.constellio.model.entities.security.global.UserCredential;
import com.constellio.model.entities.security.global.UserCredentialStatus;
import com.constellio.model.services.records.bulkImport.BulkImportProgressionListener;
import com.constellio.model.services.records.bulkImport.LoggerBulkImportProgressionListener;
import com.constellio.model.services.records.bulkImport.RecordsImportServices;
import com.constellio.model.services.records.bulkImport.UserImportServices;
import com.constellio.model.services.records.bulkImport.data.ImportDataProvider;
import com.constellio.model.services.records.bulkImport.data.xml.XMLImportDataProvider;
import com.constellio.model.services.users.UserServices;
import com.constellio.sdk.tests.ConstellioTest;
import com.constellio.sdk.tests.annotations.InDevelopmentTest;
import com.constellio.sdk.tests.annotations.UiTest;

@UiTest
public class StartApplicationWithStateAcceptanceTest extends ConstellioTest {

	BulkImportProgressionListener progressionListener = new LoggerBulkImportProgressionListener();

	@InDevelopmentTest
	@Test
	public void testName()
			throws Exception {

		File userFile = new File("/Users/francisbaril/Workspaces/Projets/BAnQ/BAnQ-data2/userCredential.xml");

		File stateFile = new File("/Users/francisbaril/Workspaces/Projets/BAnQ/banq-before-import.zip");

		givenTransactionLogIsEnabled();
		getCurrentTestSession().getFactoriesTestFeatures().givenSystemInState(stateFile).withPasswordsReset();

		User user = getModelLayerFactory().newUserServices().getUserInCollection("admin", "banq");

		RecordsImportServices recordsImportServices = new RecordsImportServices(getModelLayerFactory());
		UserImportServices userImportServices = new UserImportServices(getModelLayerFactory());
		ImportDataProvider importDataProvider = XMLImportDataProvider.forSingleXMLFile(getModelLayerFactory(), userFile);
		userImportServices.bulkImport(importDataProvider, progressionListener, user, Arrays.asList("banq"));

		//		addUsers(asList("abdelkader.lamouchi", "adesbiens", "agagnon", "al.razafiarimanana", "amelie.doyon", "andre.leblanc",
		//				"andre.ruest", "anne.montambault", "anne.ouellet", "annie.bigaouette", "annie.bisson", "annie.choquette",
		//				"annie.dube", "annie.labrecque", "annie.leclerc", "aouatef.cherif", "audrey.bouchard", "audrey.gagne",
		//				"audrey.stjean", "beatrice.forget", "benoit.ferland", "bertrand.aubin", "brainville", "brigitte.banville",
		//				"brigitte.damours", "bruno.lemay", "carl.demers", "carole.gagne", "carole.langelier", "carole.melancon",
		//				"carole.payen", "carole.ritchot", "caroline.sauvageau", "catherine.catta", "cdavid", "cecile.tremblay",
		//				"celine.morin", "cendrine.metayer", "chantal.ahki", "chantale.bessette", "christian.bolduc", "christian.drolet",
		//				"christiane.roy", "claude.rocheleau", "claudette.chevalier", "colette.boudreau", "colombe.dallaire",
		//				"conversion.intelliGID", "corriolan.claude", "daniel.ducharme", "daniel.filion", "daniel.lavoie",
		//				"danielle.bureau", "danielle.poirier", "danielle.saucier", "danny.boulanger", "dany.david", "david.rajotte",
		//				"dchagnon", "denis.boudreau", "denyse.bchampagne", "diane.aubry", "diane.gagnon", "diane.guay", "diane.jolicoeur",
		//				"diane.sarrazin", "dominique.bergeron", "dominique.hetu", "dominique.marleau", "dominique.roelandts",
		//				"edwige.beaudin", "elena.fracas", "eric.cyrenne", "eric.turcotte", "erick.jeanty", "estelle.brisson",
		//				"etienne.carpentier", "f.ouellet", "fabienne.benoist", "fethi.guerdelli", "florian.daveau", "france.monty",
		//				"francine.bussiere", "francine.toulouse", "francois.david", "francois.montreuil", "francois.rivard",
		//				"francois.veillette", "frederic.aloi", "frederic.giuliano", "g.vaillancourt", "gchauvin", "genevieve.marin",
		//				"gerald.plourde", "ginette.robert", "guillaume.hebert", "helene.auger", "helene.cadieux", "helene.charbonneau",
		//				"helene.chartrand", "helene.cossette", "helene.fortier", "helene.roussel", "hyacinthe.munger", "idenisova",
		//				"igid.rgd01", "igid.rgd02", "igid.rgd03", "igid.rgd04", "isabelle.crevier", "isabelle.lafrance", "ja.charland",
		//				"jb.giard", "jean-f.gauvin", "jerome.lemonnier", "jf.lecaude", "jm.demers", "joanne.poirier", "jocelyne.langlois",
		//				"johane.stamand", "johanne.montredon", "josee.alarie", "jp.dagenais", "jp.pare", "julie.bernard",
		//				"julie.fontaine", "julie.fregault", "julie.goulet", "julie.roy", "karen.weck", "karim.mansouri",
		//				"laureanne.langlois", "leila.hamdani", "leslie.lavigne", "liette.bernard", "lina.brouillette", "linda.khauv",
		//				"lisa.miniaci", "lise.viel", "louellet", "louise.boutin", "louise.champagne", "louise.lavallee", "luc.charlebois",
		//				"lucie.brouillette", "lynda.corcoran", "lysandre.parent", "m.desgroseilliers", "ma.sabourin", "magali.neilson",
		//				"manon.girard", "manon.lavallee", "marc.stjacques", "marco.babin", "marie-chantal.anctil", "marie.jolicoeur",
		//				"marielle.dufour", "marielle.lavertu", "mariloue.ste-marie", "marjolaine.lapierre", "marjolaine.thibeault",
		//				"marthe.leger", "martin.lavoie", "maryse.dompierre", "mathieu.ayoub", "maude.doyon", "maurice.houde",
		//				"me.pelletier", "me.poulin", "melanie.aracena", "melanie.plouffe", "mf.mignault", "michel.simard",
		//				"mireille.laforce", "mireille.lebeau", "mlapointe", "mlessard", "monique.lord", "mp.lamarre", "mp.nault",
		//				"mylene.robichaud", "myriam.cardinal", "n.vaillancourt", "nancy.belanger", "natalie.bouchard", "nathalie.boucher",
		//				"nathalie.gelinas", "nathalie.lussier", "nbelanger", "nicholas.boucher", "nicolas.dion", "nicole.vallieres",
		//				"nina.stpierre", "nj.bilodeau", "normand.charbonneau", "pa.leclerc", "pascal.laforce", "pascale.messier",
		//				"pascale.ryan", "patrice.levesque", "patrick.dossantos", "paul.christolin", "paulo.leduc", "pierre.beaulieu",
		//				"pierre.liboiron", "pierre.rainville", "rachel.massicotte", "reina.ouimet", "renald.lessard", "renee.malo",
		//				"rita.barrette", "robert.lacroix", "sarah.trudel", "sebastien.gouin", "sebastien.tessier", "silvye.lamirande",
		//				"simon.barabe", "simon.lair", "sj.lefebvre", "solange.manegre", "sonia.lachance", "sophie.cote",
		//				"sophie.montreuil", "sophie.morel", "stephany.simba", "suzanne.armstrong", "suzanne.provost", "suzanne.tessier",
		//				"sylvain.dechamplain", "sylvain.lesage", "sylvie.bedard", "sylvie.desroches", "sylvie.forcier", "tristan.muller",
		//				"valerie.damour", "vana.ke", "veronique.gagnon", "ya.lapointe", "yde.bouchard", "yhenneron", "yolaine.audet",
		//				"youcef.talbi", "yvan.carette"));

		File importFile = new File(
				"/Users/francisbaril/Workspaces/Projets/BAnQ/BAnQ-data2/import-all-except-folder-document.zip");
		File folderImportFile = new File("/Users/francisbaril/Workspaces/Projets/BAnQ/BAnQ-data2/folder.xml");
		File documentImportFile = new File("/Users/francisbaril/Workspaces/Projets/BAnQ/BAnQ-data2/document.xml");

		importDataProvider = XMLImportDataProvider.forZipFile(getModelLayerFactory(), importFile);
		recordsImportServices.bulkImport(importDataProvider, progressionListener, user);

		importDataProvider = XMLImportDataProvider.forSingleXMLFile(getModelLayerFactory(), folderImportFile);
		recordsImportServices.bulkImport(importDataProvider, progressionListener, user);

		importDataProvider = XMLImportDataProvider.forSingleXMLFile(getModelLayerFactory(), documentImportFile);
		recordsImportServices.bulkImport(importDataProvider, progressionListener, user);

	}

	private void addUsers(List<String> usernames) {
		UserServices userServices = getModelLayerFactory().newUserServices();
		for (String username : usernames) {
			List<String> groups = new ArrayList<>();
			List<String> collections = asList("banq");
			UserCredential userCredential = new UserCredential(username, username, username, username + "@ze.com", groups,
					collections,
					UserCredentialStatus.ACTIVE);
			userServices.addUpdateUserCredential(userCredential);
		}

	}
}
