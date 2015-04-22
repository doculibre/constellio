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
package com.constellio.data.dao.services.factories;

import java.util.ArrayList;
import java.util.List;

import com.constellio.data.dao.managers.StatefulService;
import com.constellio.data.dao.managers.StatefullServiceDecorator;

public class LayerFactory {

	private LayerFactory bottomLayerFactory;

	private StatefullServiceDecorator statefullServiceDecorator;

	private List<StatefulService> statefulServices = new ArrayList<>();

	public LayerFactory(StatefullServiceDecorator statefullServiceDecorator) {
		this.statefullServiceDecorator = statefullServiceDecorator;
	}

	public LayerFactory(LayerFactory bottomLayerFactory, StatefullServiceDecorator statefullServiceDecorator) {
		this.bottomLayerFactory = bottomLayerFactory;
		this.statefullServiceDecorator = statefullServiceDecorator;
	}

	public <T extends StatefulService> T add(T statefullService) {
		T decoratedService = statefullServiceDecorator.decorate(statefullService);
		statefulServices.add(decoratedService);
		return decoratedService;
	}

	public void initialize() {
		if (bottomLayerFactory != null) {
			bottomLayerFactory.initialize();
		}
		for (StatefulService statefulService : statefulServices) {
			statefulService.initialize();
		}
	}

	public void close() {
		for (int i = statefulServices.size() - 1; i >= 0; i--) {
			statefulServices.get(i).close();
		}
		if (bottomLayerFactory != null) {
			bottomLayerFactory.close();
		}
	}

}
