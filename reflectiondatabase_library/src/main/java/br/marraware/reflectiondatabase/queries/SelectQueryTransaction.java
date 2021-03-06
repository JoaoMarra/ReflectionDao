package br.marraware.reflectiondatabase.queries;

import android.database.Cursor;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import br.marraware.reflectiondatabase.exception.ColumnNotFoundException;
import br.marraware.reflectiondatabase.exception.QueryException;
import br.marraware.reflectiondatabase.model.DaoModel;
import br.marraware.reflectiondatabase.model.ORDER_BY;
import br.marraware.reflectiondatabase.model.WHERE_COMPARATION;

/**
 * Created by joao_gabriel on 10/05/17.
 */

public class SelectQueryTransaction<T extends DaoModel> extends QueryTransactionWhere<T> {

    private String orderBy;
    private int limit;
    private int offset;

    public SelectQueryTransaction(Class<T> T, Select type) {
        super(T, type);
        orderBy = null;
        limit = -1;
        offset = -1;
    }

    @Override
    public SelectQueryTransaction<T> where(String column, Object value, WHERE_COMPARATION comparation) throws ColumnNotFoundException {
        super.where(column, value, comparation);
        return this;
    }

    @Override
    public SelectQueryTransaction<T> whereAnd(Object[]... columnValueComparation) throws ColumnNotFoundException {
        super.whereAnd(columnValueComparation);
        return this;
    }

    @Override
    public SelectQueryTransaction<T> whereOr(Object[]... columnValueComparation) throws ColumnNotFoundException {
        super.whereOr(columnValueComparation);
        return this;
    }

    @Override
    public SelectQueryTransaction<T> whereIn(String column, Object... values) throws ColumnNotFoundException {
        super.whereIn(column, values);
        return this;
    }

    @Override
    public SelectQueryTransaction<T> whereNotIn(String column, Object... values) throws ColumnNotFoundException {
        super.whereNotIn(column, values);
        return this;
    }

    @Override
    public SelectQueryTransaction<T> whereBetween(String column, Object value1, Object value2) throws ColumnNotFoundException {
        super.whereBetween(column, value1, value2);
        return this;
    }

    @Override
    public SelectQueryTransaction<T> whereRaw(String query) throws ColumnNotFoundException {
        super.whereRaw(query);
        return this;
    }

    @Override
    public SelectQueryTransaction<T> whereJSONObject(String column, String key, Object value) throws ColumnNotFoundException {
        super.whereJSONObject(column, key, value);
        return this;
    }

    @Override
    public SelectQueryTransaction<T> whereJSONArray(String column, Object value) throws ColumnNotFoundException {
        super.whereJSONArray(column, value);
        return this;
    }

    public SelectQueryTransaction<T> orderBy(String column, ORDER_BY order) throws ColumnNotFoundException {
        if(DaoModel.checkColumn(modelClass, column)) {
            String newOrderBy = " "+column+" "+order;
            if (orderBy == null) {
                orderBy = newOrderBy;
            } else {
                orderBy = orderBy+" , "+newOrderBy;
            }
        }

        return this;
    }

    public SelectQueryTransaction<T> offset(int offset) {
        if(type instanceof Insert || type instanceof Update || type instanceof InsertMany)
            return this;

        this.offset = offset;

        return this;
    }

    public SelectQueryTransaction<T> limit(int limit) {
        if(type instanceof Insert || type instanceof Update || type instanceof InsertMany)
            return this;

        this.limit = limit;

        return this;
    }

    @Override
    protected Cursor preExecute() throws QueryException {
        String newOrderBy = null;
        if(orderBy != null) {
            newOrderBy = " order by "+orderBy;
        }

        return type.execute(modelClass,newOrderBy,limit,offset);
    }

    @Override
    public void postExecute(ArrayList<T> models) {
        if(models.size() > 0) {
            T exampleModel = models.get(0);
            HashMap<Class<? extends DaoModel>,String> dependecies = exampleModel.getDepedencyTables();
            ArrayList<Field> fields = exampleModel.getDependecyValues();
            Class type;
            if(dependecies.size() > 0) {
                String fieldString;
                for(T model : models) {
                    for(Field field : fields) {

                        try {
                            if (DaoModel.class.isAssignableFrom(field.getType())) {
                                fieldString = dependecies.get(field.getType());
                                field.set(model,Select.from((Class<? extends DaoModel>)field.getType())
                                        .where(fieldString,model.identifierValue(),WHERE_COMPARATION.EQUAL)
                                        .executeForFirst());
                            } else if (List.class.isAssignableFrom(field.getType())) {
                                type = (Class) ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0];
                                fieldString = dependecies.get(type);
                                field.set(model,Select
                                        .from((Class<? extends DaoModel>)type)
                                        .where(fieldString,model.identifierValue(),WHERE_COMPARATION.EQUAL)
                                        .execute());
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
    }

    public SelectQueryTransaction<T> setConflictType(int conflictType) {
        type.setConflictType(conflictType);
        return this;
    }
}
