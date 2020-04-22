package br.marraware.reflectiondatabase.queries;

import android.database.Cursor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import br.marraware.reflectiondatabase.exception.ColumnNotFoundException;
import br.marraware.reflectiondatabase.exception.QueryException;
import br.marraware.reflectiondatabase.model.ColumnModel;
import br.marraware.reflectiondatabase.model.DaoModel;
import br.marraware.reflectiondatabase.model.ORDER_BY;
import br.marraware.reflectiondatabase.model.WHERE_COMPARATION;
import br.marraware.reflectiondatabase.utils.AsyncQueryCallback;
import br.marraware.reflectiondatabase.utils.TransactionTask;

/**
 * Created by joao_gabriel on 10/05/17.
 */

public class SelectDistinctQueryTransaction<T extends DaoModel> extends QueryTransactionWhere {

    private String orderBy;
    private int limit;
    private int offset;

    public SelectDistinctQueryTransaction(Class<T> T, SelectDistinct type) {
        super(T, type);
        orderBy = null;
        limit = -1;
        offset = -1;
    }

    @Override
    public SelectDistinctQueryTransaction where(String column, Object value, WHERE_COMPARATION comparation) throws ColumnNotFoundException {
        super.where(column, value, comparation);
        return this;
    }

    @Override
    public SelectDistinctQueryTransaction whereAnd(Object[]... columnValueComparation) throws ColumnNotFoundException {
        super.whereAnd(columnValueComparation);
        return this;
    }

    @Override
    public SelectDistinctQueryTransaction whereOr(Object[]... columnValueComparation) throws ColumnNotFoundException {
        super.whereOr(columnValueComparation);
        return this;
    }

    public SelectDistinctQueryTransaction orderBy(String column, ORDER_BY order) throws ColumnNotFoundException {
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

    public SelectDistinctQueryTransaction offset(int offset) {
        if(type instanceof Insert || type instanceof Update || type instanceof InsertMany)
            return this;

        this.offset = offset;

        return this;
    }

    public SelectDistinctQueryTransaction limit(int limit) {
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
    public ArrayList<ColumnModel> execute() throws QueryException {
        Cursor cursor = preExecute();

        ArrayList<ColumnModel> models = null;
        if(cursor != null) {
            try {
                ColumnModel model;
                if (cursor.moveToFirst()) {
                    models = new ArrayList<>();
                    while (!cursor.isAfterLast()) {
                        model = new ColumnModel(modelClass, cursor);
                        models.add(model);
                        cursor.moveToNext();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return models;
    }

    @Override
    public ColumnModel executeForFirst() throws QueryException {
        ArrayList<ColumnModel> models = execute();
        if(models != null && models.size() > 0)
            return models.get(0);
        return null;
    }

}