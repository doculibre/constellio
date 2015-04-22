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
package com.constellio.model.services.search;

import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.allConditions;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.containingText;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.equal;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.from;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.isNotEqual;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.where;
import static com.constellio.model.services.search.query.logical.LogicalSearchQueryOperators.whereAll;

import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchema;
import com.constellio.model.entities.schemas.MetadataSchemaType;
import com.constellio.model.services.search.query.logical.LogicalSearchValueCondition;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;

public class QueriesExemples {

	MetadataSchemaType categories;
	MetadataSchemaType folders;
	MetadataSchema customFolders;

	LogicalSearchCondition condition;
	Metadata folderTitle;
	Metadata folderDescription;
	Metadata folderRule, folderRuleCode;
	Metadata folderCategory;
	Metadata categoryRules, categoryIsEnabled;

	public void testA()
			throws Exception {

		condition = from(folders).whereAny(folderTitle, folderDescription).isContainingText("blabla")
				.and(containingText("francis")).andWhere(folderRule).isAny(equal("1"), equal("pomme"), isNotEqual("2"));

	}

	public void testB()
			throws Exception {

		LogicalSearchValueCondition containingFrancisAndBaril = containingText("francis").and(containingText("baril"));
		LogicalSearchValueCondition containingChuckAndNorris = containingText("chuck").and(containingText("norris"));
		condition = from(customFolders).whereAny(folderTitle, folderDescription).is(containingFrancisAndBaril)
				.or(containingChuckAndNorris).orWhere(folderRule).is("1");

		// q=((title_s:*francis* AND title_s:*baril*) OR (title_s:*chuck* AND title_s:*norris*) OR (description_s:*francis* AND
		// description_s:*baril*) OR (description_s:*chuck* AND description_s:*norris*)) OR (rule:1)

	}

	public void testC()
			throws Exception {

		LogicalSearchValueCondition containingFrancisAndBaril = containingText("francis").and(containingText("baril"));
		LogicalSearchValueCondition containingChuckAndNorris = containingText("chuck").and(containingText("norris"));
		LogicalSearchCondition containingFrancisOrChuck = whereAll(folderTitle, folderDescription).is(
				containingFrancisAndBaril).or(containingChuckAndNorris);

		LogicalSearchCondition enabledCategories = from(categories).where(categoryRules).isNotNull().andWhere(categoryIsEnabled)
				.isTrueOrNull();

		LogicalSearchCondition folderCategoryHasRule = where(folderRule).is(enabledCategories);
		LogicalSearchCondition folderCategoryCodeStartingWithA = where(folderRuleCode).isStartingWithText("A");

		condition = from(folders).whereAnyCondition(containingFrancisOrChuck,
				allConditions(folderCategoryHasRule, folderCategoryCodeStartingWithA));

	}
}
