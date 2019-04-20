package com.mas.lab2.Behaviours;

import com.mas.lab2.Agents.Passenger;
import com.mas.lab2.Util.Util;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.FIPAAgentManagement.DFAgentDescription;

import java.util.ArrayList;
import java.util.Arrays;

public class TickWrapper extends TickerBehaviour {
    public TickWrapper(Agent a, long period) {
        super(a, period);
    }

    @Override
    protected void onTick() {
        DFAgentDescription[] passengers = Util.findAgents(myAgent, "passenger", "Visit");

        if (passengers != null) {
            ArrayList<AID> passengersAIDList = new ArrayList<>(Arrays
                    .asList(Arrays.stream(passengers)
                    .map(DFAgentDescription::getName)
                    .filter(x -> x != myAgent.getAID()).toArray(AID[]::new)));

            myAgent.addBehaviour(new RequestDriver(passengersAIDList));
        }
    }
}
