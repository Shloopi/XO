package com.idan.serverSide.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import com.idan.serverSide.entities.BaseEntity;
import com.idan.serverSide.entities.SqlEntity;

public abstract class DB {
    protected Connection connection;

     protected abstract BaseEntity createModel(BaseEntity entity, ResultSet res) throws SQLException;


    protected ArrayList<SqlEntity> inserted = new ArrayList<>();
    protected ArrayList<SqlEntity> updated = new ArrayList<>();
    protected ArrayList<SqlEntity> deleted = new ArrayList<>();

    public abstract String insertQuery(BaseEntity entity);
    protected abstract BaseEntity newEntity();
    public abstract String updateQuery(BaseEntity entity);
    public abstract String deleteQuery(BaseEntity entity);
    
    public DB() {
        this.connection = null;
        this.connectSql();
    }

    private void connectSql() {
        String url = "jdbc:mysql://localhost:3306/xoFinal?serverTimezone=Asia/Jerusalem";
        String username = "root";
        String password = "Shloopi210405";
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(url, username, password);
        } catch(Exception e) {
            System.out.println(e);
        }
    }

    public void disconnectSql() {
        try {
            if (this.connection != null && !this.connection.isClosed()) {
                this.connection.close();
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public int saveChanges() {
        int rows = 0;
        String sqlStr = "";
        ResultSet result;
        try {
            Statement statement = connection.createStatement();
            for (SqlEntity item : this.inserted) {
                sqlStr = item.getSqlQuery();
                rows += statement.executeUpdate(sqlStr, Statement.RETURN_GENERATED_KEYS);

                result = statement.getGeneratedKeys();
                if (result.next()) {
                    item.getEntity().setID(result.getInt(1));
                }
            }
            for (SqlEntity item : updated) {
                sqlStr = item.getSqlQuery();
                rows += statement.executeUpdate(sqlStr);
            }
            for (SqlEntity item : deleted) {
                sqlStr = item.getSqlQuery();
                rows += statement.executeUpdate(sqlStr);
            }
        } catch (SQLException e) {
        } finally {
            inserted.clear();
            updated.clear();
            deleted.clear();
        }
        return rows;
    }

    public ArrayList<BaseEntity> select(String sqlStr) {
        ArrayList<BaseEntity> list = new ArrayList<BaseEntity>();
        Statement statement;
        ResultSet res;
        try {
            // create the satement.
            statement = this.connection.createStatement();
            res = statement.executeQuery(sqlStr);

            // run as long as there is more information from the select.
            while (res.next()) {
                // create the entity.
                BaseEntity entity = newEntity();

                // create the model with the res.
                this.createModel(entity, res);

                // add the entity to the list.
                list.add(entity);
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return list;
    }
    public void insert(BaseEntity entity) {
        BaseEntity reqEntity = this.newEntity();
        String sqlQuery = "";

        if (entity != null && entity.getClass() == reqEntity.getClass()) {
            sqlQuery = this.insertQuery(entity);
        }
        SqlEntity query = new SqlEntity(entity, sqlQuery);
        inserted.add(query);
    }

    public void update(BaseEntity entity) {
        BaseEntity reqEntity = this.newEntity();
        String sqlQuery = "";

        if (entity != null && entity.getClass() == reqEntity.getClass()) {
            sqlQuery = this.updateQuery(entity);
        }
        SqlEntity query = new SqlEntity(entity, sqlQuery);
        updated.add(query);
    }

    public void delete(BaseEntity entity) {
        BaseEntity reqEntity = this.newEntity();
        String sqlQuery = "";

        if (entity != null && entity.getClass() == reqEntity.getClass()) {
            sqlQuery = this.deleteQuery(entity);
        }
        SqlEntity query = new SqlEntity(entity, sqlQuery);
        deleted.add(query);
    }
    }
