package com.mas.lab2;

import com.mas.lab2.Agents.Driver;
import com.mas.lab2.Agents.Passenger;
import com.mas.lab2.Util.Config;
import jade.core.Profile;
import jade.core.Runtime;
import jade.core.ProfileImpl;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;

public class Main {
    public static AgentContainer mainContainer;

    public static void main(String[] args) {
        Runtime runtime = Runtime.instance();
        Profile profile = new ProfileImpl("localhost", 1200, "MyPlatform");
        mainContainer = runtime.createMainContainer(profile);

        try {

            for (int i = 0; i < Config.passengersAmount; i++) {
                AgentController passengerController = mainContainer.acceptNewAgent("passenger"+i, new Passenger());
                passengerController.start();
            }

            for (int i = 0; i < Config.taxiDriversAmount; i++) {
                AgentController driverController = mainContainer.acceptNewAgent("driver"+i, new Driver());
                driverController.start();
            }


        } catch (Exception e) {
            e.printStackTrace();
        }


    }

}
