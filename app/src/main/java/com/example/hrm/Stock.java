package com.example.hrm;

import java.io.Serializable;

public class Stock implements Serializable {
    private int id;
    private String itemName;
    private int quantity;

    public Stock(int id, String itemName, int quantity) {
        this.id = id;
        this.itemName = itemName;
        this.quantity = quantity;
    }

    // Getters
    public int getId() { return id; }
    public String getItemName() { return itemName; }
    public int getQuantity() { return quantity; }
}
