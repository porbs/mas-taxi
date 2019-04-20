package com.mas.lab2.Util;

import jade.wrapper.AgentContainer;

public class Config {

    public static int cityWidth = 29000;
    public static int cityHeight = 30000;
    public static int passengersAmount = 4;
    public static int taxiDriversAmount = 1;
    public static double driverSpeed = 15.0;
    public static int visitingTimeLowerBoundry = 1800;
    public static int visitingTimeUpperBoundry = 10800;

    public static double carPrice = 15000;
    public static double oneKmCost = 2.5;
    public static double initResources = 150000;
    public static double passengerSalary = 50;

    public double customerSatisfactionIndex(double waitingTime, double tripCost) {
        return (waitingTime / 60) + (tripCost / 1000);
    }
}
