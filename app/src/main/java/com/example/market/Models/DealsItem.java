package com.example.market.Models;

import java.util.ArrayList;

public class DealsItem {

    private ArrayList<GridItem> itemsList;

    public DealsItem(){

    }
    public DealsItem(ArrayList<GridItem> itemsList) {
        this.itemsList = itemsList;
    }

    public ArrayList<GridItem> getItemsList() {
        return itemsList;
    }

    public void setItemsList(ArrayList<GridItem> itemsList) {
        this.itemsList = itemsList;
    }
}
