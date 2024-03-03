package com.idan.serverSide.entities;

public class SqlEntity {
    private String sqlQuery;
    private BaseEntity entity;

    public SqlEntity(BaseEntity entity, String sqlQuery) {
        this.entity = entity;
        this.sqlQuery = sqlQuery;
    }

    public String getSqlQuery() {
        return sqlQuery;
    }

    public void setSqlQuery(String sqlQuery) {
        this.sqlQuery = sqlQuery;
    }

    public BaseEntity getEntity() {
        return entity;
    }

    public void setEntity(BaseEntity entity) {
        this.entity = entity;
    }
}
