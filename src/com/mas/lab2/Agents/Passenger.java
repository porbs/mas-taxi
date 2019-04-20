package com.mas.lab2.Agents;

import com.mas.lab2.Behaviours.AcceptVisitor;
import com.mas.lab2.Behaviours.TickWrapper;
import com.mas.lab2.Util.Config;
import com.mas.lab2.Util.Point;
import com.mas.lab2.Util.Util;
import jade.core.Agent;

public class Passenger extends Agent {
    private Point<Integer> location;
    private boolean isBusy;

    public Point<Integer> getLocation() {
        return location;
    }

    public boolean isBusy() {
        return isBusy;
    }

    public void setBusy(boolean busy) {
        isBusy = busy;
    }

    protected void setup() {
        isBusy = false;
        location = new Point<>(Util.randInt(0, Config.cityWidth), Util.randInt(0, Config.cityHeight));

        Util.registerAgentInYellowPages(this, "passenger", "Visit");

        addBehaviour(new TickWrapper(this, Util.randInt(1000, 2000)));
        addBehaviour(new AcceptVisitor());

    }

}
