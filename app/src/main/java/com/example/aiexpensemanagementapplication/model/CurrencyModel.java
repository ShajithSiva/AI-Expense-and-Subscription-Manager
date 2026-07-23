package com.example.aiexpensemanagementapplication.model;

import com.google.gson.annotations.SerializedName;

public class CurrencyModel {

    @SerializedName("iso_code")
    private String isoCode;

    @SerializedName("name")
    private String name;

    @SerializedName("symbol")
    private String symbol;

    private boolean selected;

    public CurrencyModel() {
    }

    public CurrencyModel(String isoCode, String name, String symbol) {
        this.isoCode = isoCode;
        this.name = name;
        this.symbol = symbol;
        this.selected = false;
    }

    public String getIsoCode() {
        return isoCode;
    }

    public void setIsoCode(String isoCode) {
        this.isoCode = isoCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSymbol() {
        if (symbol == null || symbol.trim().isEmpty()) {
            return isoCode;
        }

        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public String getDisplayName() {
        return name + " (" + isoCode + ")";
    }
}