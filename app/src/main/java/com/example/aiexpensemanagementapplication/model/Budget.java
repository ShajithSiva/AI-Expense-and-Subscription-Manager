package com.example.aiexpensemanagementapplication.model;

public class Budget {

    private double monthlyBudget;

    private double foodBudget;
    private double transportBudget;
    private double shoppingBudget;
    private double billsBudget;
    private double healthBudget;
    private double educationBudget;
    private double entertainmentBudget;
    private double othersBudget;

    public Budget() {
    }

    public Budget(double monthlyBudget,
                  double foodBudget,
                  double transportBudget,
                  double shoppingBudget,
                  double billsBudget,
                  double healthBudget,
                  double educationBudget,
                  double entertainmentBudget,
                  double othersBudget) {

        this.monthlyBudget = monthlyBudget;
        this.foodBudget = foodBudget;
        this.transportBudget = transportBudget;
        this.shoppingBudget = shoppingBudget;
        this.billsBudget = billsBudget;
        this.healthBudget = healthBudget;
        this.educationBudget = educationBudget;
        this.entertainmentBudget = entertainmentBudget;
        this.othersBudget = othersBudget;
    }

    public double getMonthlyBudget() {
        return monthlyBudget;
    }

    public void setMonthlyBudget(double monthlyBudget) {
        this.monthlyBudget = monthlyBudget;
    }

    public double getFoodBudget() {
        return foodBudget;
    }

    public void setFoodBudget(double foodBudget) {
        this.foodBudget = foodBudget;
    }

    public double getTransportBudget() {
        return transportBudget;
    }

    public void setTransportBudget(double transportBudget) {
        this.transportBudget = transportBudget;
    }

    public double getShoppingBudget() {
        return shoppingBudget;
    }

    public void setShoppingBudget(double shoppingBudget) {
        this.shoppingBudget = shoppingBudget;
    }

    public double getBillsBudget() {
        return billsBudget;
    }

    public void setBillsBudget(double billsBudget) {
        this.billsBudget = billsBudget;
    }

    public double getHealthBudget() {
        return healthBudget;
    }

    public void setHealthBudget(double healthBudget) {
        this.healthBudget = healthBudget;
    }

    public double getEducationBudget() {
        return educationBudget;
    }

    public void setEducationBudget(double educationBudget) {
        this.educationBudget = educationBudget;
    }

    public double getEntertainmentBudget() {
        return entertainmentBudget;
    }

    public void setEntertainmentBudget(double entertainmentBudget) {
        this.entertainmentBudget = entertainmentBudget;
    }

    public double getOthersBudget() {
        return othersBudget;
    }

    public void setOthersBudget(double othersBudget) {
        this.othersBudget = othersBudget;
    }
}