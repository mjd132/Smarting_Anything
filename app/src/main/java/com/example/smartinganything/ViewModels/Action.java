package com.example.smartinganything.ViewModels;


public class Action {

    public int id;
    public String name = "Default Action";
    public String btRMSG = "";
    public String btOnMSG = "";
    public String btOffMSG = "";

    public Action() {
    }

    public Action(String btRMSG, String btOnMSG, String btOffMSG) {
        this.btRMSG = btRMSG;
        this.btOnMSG = btOnMSG;
        this.btOffMSG = btOffMSG;
    }

    public Action(int id, String name, String btRMSG, String btOnMSG, String btOffMSG) {
        this.id = id;
        this.name = name;
        this.btRMSG = btRMSG;
        this.btOnMSG = btOnMSG;
        this.btOffMSG = btOffMSG;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBtRMSG() {
        return btRMSG;
    }

    public void setBtRMSG(String btRMSG) {
        this.btRMSG = btRMSG;
    }

    public String getBtOnMSG() {
        return btOnMSG;
    }

    public void setBtOnMSG(String btOnMSG) {
        this.btOnMSG = btOnMSG;
    }

    public String getBtOffMSG() {
        return btOffMSG;
    }

    public void setBtOffMSG(String btOffMSG) {
        this.btOffMSG = btOffMSG;
    }
}
