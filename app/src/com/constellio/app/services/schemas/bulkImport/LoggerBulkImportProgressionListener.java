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
package com.constellio.app.services.schemas.bulkImport;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggerBulkImportProgressionListener implements BulkImportProgressionListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(LoggerBulkImportProgressionListener.class);

    int totalProgression;

    int currentTotal;

    int currentStepTotal;

    String currentStepName;

    @Override
    public void updateTotal(int newTotal) {
        this.currentTotal = newTotal;
    }

    @Override
    public void updateProgression(int stepProgression, int totalProgression) {
        this.totalProgression = totalProgression;
        LOGGER.info("Import progression : " + getPercentage() + "% [" + currentStepName + " " + stepProgression + "/"
                + currentStepTotal + "]");
    }

    @Override
    public void updateCurrentStepTotal(int newTotal) {
        this.currentStepTotal = newTotal;
    }

    @Override
    public void updateCurrentStepName(String stepName) {
        this.currentStepName = stepName;
        LOGGER.info("Import progression : " + getPercentage() + "% [" + currentStepName + "]");
    }

    private double getPercentage() {
        if (currentTotal == 0) {
            return 0;
        }
        int pct10 = 1000 * totalProgression / currentTotal;
        return pct10 / 10.0;
    }
}
