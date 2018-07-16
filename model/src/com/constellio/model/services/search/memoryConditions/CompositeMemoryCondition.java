package com.constellio.model.services.search.memoryConditions;

import com.constellio.data.utils.LangUtils;
import com.constellio.model.entities.records.Record;
import com.constellio.model.entities.schemas.DataStoreField;
import com.constellio.model.entities.schemas.Metadata;
import com.constellio.model.entities.schemas.MetadataSchemaTypes;
import com.constellio.model.services.search.query.logical.LogicalOperator;
import com.constellio.model.services.search.query.logical.LogicalSearchValueCondition;
import com.constellio.model.services.search.query.logical.condition.CompositeLogicalSearchCondition;
import com.constellio.model.services.search.query.logical.condition.DataStoreFieldLogicalSearchCondition;
import com.constellio.model.services.search.query.logical.condition.LogicalSearchCondition;
import com.constellio.model.services.search.query.logical.criteria.IsEqualCriterion;
import org.antlr.v4.runtime.atn.SemanticContext;

import java.util.ArrayList;
import java.util.List;

import static com.constellio.model.services.search.query.logical.LogicalOperator.AND;

public class CompositeMemoryCondition  implements InMemoryCondition {

    LogicalOperator operator;

    List<InMemoryCondition> conditions;

    public CompositeMemoryCondition(LogicalOperator operator) {
        this.operator = operator;
        conditions=new ArrayList<InMemoryCondition>();
    }
    public List<InMemoryCondition> getconditions(){

        return conditions;
    }
    public void addconditions(InMemoryCondition condition){
       conditions.add(condition);
    }



    private boolean withOr(Record record){

            boolean result = false;
            for(InMemoryCondition condition : conditions){
                if(condition.isReturnable(record)){
                        result=true;
                        break;
                    }
                }

        return result;
    }

    private boolean withAnd(Record record){
        boolean result = true;
        for(InMemoryCondition condition : conditions){
            if(!(condition.isReturnable(record))){
                result=false;
                break;
            }
        }

        return result;
    }

    private boolean withNot(Record record){

        boolean result = false;
        for(InMemoryCondition condition : conditions){
            if(!(condition.isReturnable(record))){
                result=true;



                break;
            }
        }

        return result;
    }


    @Override
    public boolean isReturnable(Record record) {
        boolean result;
      if(operator==AND){
          result=withAnd(record);
      }else{
          result=withOr(record);
      }
      return result;
    }
}