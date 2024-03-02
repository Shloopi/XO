package com.idan.serverSide.entities;

public abstract class BaseEntity {
    protected int id;

    public BaseEntity(int id) {
        this.id = id;
    }
    public BaseEntity() {
        this.id = -1;
    }
    public int getID() {
        return this.id;
    }
    public void setID(int id) {
        if (this.id != -1) {
            this.id = id;
        }
    } 
}
