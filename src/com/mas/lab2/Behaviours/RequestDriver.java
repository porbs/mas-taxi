package com.mas.lab2.Behaviours;

import com.mas.lab2.Agents.Driver;
import com.mas.lab2.Agents.Passenger;
import com.mas.lab2.Main;
import com.mas.lab2.Util.Config;
import com.mas.lab2.Util.Point;
import com.mas.lab2.Util.Util;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.lang.acl.UnreadableException;
import jade.wrapper.AgentController;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RequestDriver extends Behaviour {
    private ArrayList<AID> passengers;
    DFAgentDescription[] drivers;
    private ArrayList<AID> residentCandidates = new ArrayList<>();

    private AID resident = null;
    private Double tripTime;
    private AID driver = null;

    private int residentReplies = 0;

    private MessageTemplate mt;
    private PASSENGER_STATE state = PASSENGER_STATE.SEND_NOTIFICATION;

    private enum PASSENGER_STATE {
        SEND_NOTIFICATION,
        FIND_RESIDENT,
        NOTIFY_RESIDENT,
        REQUEST_DRIVERS,
        FIND_DRIVER,
        DO_THE_TRIP,
        PARTY_TIME,
        DONE
    }

    public RequestDriver(ArrayList<AID> passengers) {
        this.passengers = passengers;
    }

    private void log(String data) {
        System.out.println(myAgent.getAID().getName() + ": " + data);
    }

    @Override
    public void action() {
        switch (state) {
            case SEND_NOTIFICATION:
                log("broadcasting residents");
                ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
                for (AID passenger : passengers) {
                    request.addReceiver(passenger);
                }

                request.setConversationId("visit-request");
                request.setReplyWith("availability-status"+System.currentTimeMillis());

                myAgent.send(request);

                mt = MessageTemplate.and(MessageTemplate.MatchConversationId("visit-request"),
                        MessageTemplate.MatchInReplyTo(request.getReplyWith()));
                state = PASSENGER_STATE.FIND_RESIDENT;
                break;
            case FIND_RESIDENT:
                ACLMessage reply = myAgent.receive(mt);

                if (reply != null) {
                    residentReplies++;
                    log("got reply: " + reply.getPerformative());
                    if (reply.getPerformative() == ACLMessage.AGREE) {
                        log("potential resident found");
                        residentCandidates.add(reply.getSender());
                    }

                    if (residentReplies == passengers.size()) {

                        if (!residentCandidates.isEmpty()) {
                            resident = residentCandidates.get(0);

                            Point<Integer> from = ((Passenger)myAgent).getLocation();
                            double distance = 0.0;
                            try {
                                Point<Integer> to = (Point<Integer>)reply.getContentObject();
                                distance = Util.distance(from, to);
                                tripTime = distance / Config.driverSpeed;
                            } catch (UnreadableException e) {
                                e.printStackTrace();
                            }

                            ACLMessage cancellationMessage =  new ACLMessage(ACLMessage.CANCEL);
                            for (int i = 1; i < residentCandidates.size(); i++) {
                                cancellationMessage.addReceiver(residentCandidates.get(i));
                            }
                            cancellationMessage.setConversationId("visit-request");
                            cancellationMessage.setReplyWith("cancel" + System.currentTimeMillis());
                            myAgent.send(cancellationMessage);
                            residentCandidates.clear();

                            log("residents were notified");
                            state = PASSENGER_STATE.NOTIFY_RESIDENT;
                        } else {
                            log("no residents found");
                            state = PASSENGER_STATE.SEND_NOTIFICATION;
                        }
                    }

                } else {
                    block();
                }
                break;
            case NOTIFY_RESIDENT:
                System.out.println("Sending notify to resident");
                ((Passenger) myAgent).setBusy(true);
                ACLMessage notify = new ACLMessage(ACLMessage.CONFIRM);
                notify.addReceiver(resident);
                notify.setConversationId("visit-request");
                notify.setReplyWith("confirm" + System.currentTimeMillis());
                myAgent.send(notify);
                state = PASSENGER_STATE.REQUEST_DRIVERS;

                break;
            case REQUEST_DRIVERS:
                drivers = Util.findAgents(myAgent, null, "Taxi");
                log("requesting driver");
                if (drivers != null) {
                    ACLMessage requestDriver = new ACLMessage(ACLMessage.REQUEST);
                    requestDriver.setConversationId("request-driver");
                    requestDriver.setReplyWith("request-driver-answer" + System.currentTimeMillis());

                    for (DFAgentDescription driver: drivers) {
                        requestDriver.addReceiver(driver.getName());
                    }
                    myAgent.send(requestDriver);
                    mt = MessageTemplate.and(MessageTemplate.MatchConversationId("request-driver"),
                            MessageTemplate.MatchInReplyTo(requestDriver.getReplyWith()));
                    state = PASSENGER_STATE.FIND_DRIVER;
                } else {
                    block();
                }
                break;
            case FIND_DRIVER:
                HashMap<AID, Double> potentialDrivers = new HashMap<>();
                int driversRepliesCounter = 0;
                ACLMessage replyDriver = myAgent.receive(mt);
                drivers = Util.findAgents(myAgent, null, "Taxi");

                if (replyDriver != null) {
                    driversRepliesCounter++;
                    log("found potential driver: " + driversRepliesCounter);
                    if (replyDriver.getPerformative() == ACLMessage.AGREE) {
                        Double distanceToDriver = 0.0;
                        try {
                            distanceToDriver = Util.distance(((Passenger)myAgent).getLocation(),
                                    (Point<Integer>) replyDriver.getContentObject());
                        } catch (UnreadableException e) {
                            e.printStackTrace();
                        }
                        potentialDrivers.put(replyDriver.getSender(), distanceToDriver);
                    }

                    if (driversRepliesCounter == drivers.length) {
                        Map.Entry<AID, Double> optimum = null;
                        for (Map.Entry<AID, Double> entry : potentialDrivers.entrySet()) {
                            AID key = entry.getKey();
                            Double value = entry.getValue();

                            if (optimum == null || optimum.getValue() < value) {
                                optimum = entry;
                                log("Found new driver optimum: " + optimum.getValue());
                            }
                        }

                        assert optimum != null;
                        driver = optimum.getKey();
                        tripTime += (optimum.getValue() / Config.driverSpeed);

                        ACLMessage cm = new ACLMessage(ACLMessage.CANCEL);
                        cm.setReplyWith("driver-cancel" + System.currentTimeMillis());
                        cm.setConversationId("request-driver");
                        for (DFAgentDescription d : drivers) {
                            if (d.getName() != driver) {
                                cm.addReceiver(d.getName());
                            }
                        }
                        myAgent.send(cm);
                        state = PASSENGER_STATE.DO_THE_TRIP;
                    }
                } else {
                    block();
                }

                break;
            case DO_THE_TRIP:
                if (driver != null) {
                    log("on my way");
                    ACLMessage notifyDriver = new ACLMessage(ACLMessage.CONFIRM);
                    notifyDriver.addReceiver(driver);
                    notifyDriver.setConversationId("request-driver");
                    notifyDriver.setReplyWith("drive in progress" + System.currentTimeMillis());
                    notifyDriver.setContent(tripTime.toString());
                    Util.sleep(tripTime.longValue());
                    state = PASSENGER_STATE.PARTY_TIME;
                } else {
                    state = PASSENGER_STATE.REQUEST_DRIVERS;
                }
                break;
            case PARTY_TIME:
                log("party");
                ACLMessage inform = new ACLMessage(ACLMessage.INFORM);
                inform.addReceiver(resident);
                int partyTime = Util.randInt(Config.visitingTimeLowerBoundry, Config.visitingTimeUpperBoundry);
                inform.setContent(((Integer)partyTime).toString());
                myAgent.send(inform);
                Util.sleep(partyTime);
                ((Passenger)myAgent).setBusy(false);
                state = PASSENGER_STATE.DONE;
                break;
            case DONE:
                break;
        }
    }

    @Override
    public boolean done() {
        return (state == PASSENGER_STATE.DONE);
    }
}
