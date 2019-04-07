package com.mas.lab2.Agents;

import com.mas.lab2.Behaviours.DriverBehaviour;
import com.mas.lab2.Util.Util;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;

public class Driver extends Agent {

    private boolean isBusy = false;

    @Override
    protected void setup(){
        Util.registerAgentInYellowPages(this, "driver", "Taxi");
        addBehaviour(new DriverBehaviour());
    }

    @Override
    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (FIPAException e){
            e.printStackTrace();
        }
    }

    public boolean isBusy() {
        return isBusy;
    }

    public void setBusy(boolean busy) {
        isBusy = busy;
    }
}
