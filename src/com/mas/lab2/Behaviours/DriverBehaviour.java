package com.mas.lab2.Behaviours;

import com.mas.lab2.Agents.Driver;
import com.mas.lab2.Util.Util;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import java.io.IOException;

import static jade.lang.acl.MessageTemplate.MatchPerformative;

public class DriverBehaviour extends CyclicBehaviour {

    private DRIVER_STATE state = DRIVER_STATE.LISTEN_REQUESTS;
    private MessageTemplate mt;
    private Long tripTime;

    private enum DRIVER_STATE {
        LISTEN_REQUESTS,
        WAITING_CONFIRMATION,
        DRIVING
    }

    private void log(String data) {
        System.out.println(myAgent.getAID().getName() + ": " + data);
    }

    @Override
    public void action() {
        switch (state) {
            case LISTEN_REQUESTS:
                log("waiting requests");
                mt = MatchPerformative(ACLMessage.REQUEST);
                ACLMessage request = myAgent.receive(mt);

                if (request != null) {
                    log("got request");
                    ACLMessage reply = request.createReply();
                    if (((Driver)myAgent).isBusy()) {
                        log("declined: im busy");
                        reply.setPerformative(ACLMessage.CANCEL);
                    } else {
                        log("accepted request");
                        reply.setPerformative(ACLMessage.AGREE);
                        try {
                            reply.setContentObject(((Driver)myAgent).getLocation());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        state = DRIVER_STATE.WAITING_CONFIRMATION;
                    }
                    myAgent.send(reply);
                } else {
                    block();
                }
                break;
            case WAITING_CONFIRMATION:
//                log("waiting confirmation");
                mt = MessageTemplate.or(MatchPerformative(ACLMessage.CONFIRM), MatchPerformative(ACLMessage.CANCEL));
                ACLMessage cm = myAgent.receive(mt);
                if (cm != null) {
                    if (cm.getPerformative() == ACLMessage.CONFIRM) {
                        log("got confirmation");
                        ((Driver)myAgent).setBusy(true);
                        tripTime = Long.valueOf(cm.getContent());
//                        state = DRIVER_STATE.DRIVING;
                        log("driving start(time): "+ tripTime);
                        Util.sleep(tripTime);
                        ((Driver)myAgent).setBusy(false);
                        log("driving done");
                    } else {
                        log("driving request was canceled: "+ cm.getPerformative());
                        state = DRIVER_STATE.LISTEN_REQUESTS;
                    }
                }
                break;
            case DRIVING:
                log("driving start");
                Util.sleep(tripTime);
                ((Driver)myAgent).setBusy(false);
                log("driving done");
                break;
        }
    }
}
