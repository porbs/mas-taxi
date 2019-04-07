package com.mas.lab2.Behaviours;

import com.mas.lab2.Agents.Passenger;
import com.mas.lab2.Util.Util;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class AcceptVisitor extends CyclicBehaviour {

    private MessageTemplate mt;
    private PASSENGER_STATE state = PASSENGER_STATE.RECEIVE_REQUEST;

    private enum PASSENGER_STATE{
        RECEIVE_REQUEST,
        RECEIVE_NOTIFICATION,
        PARTY_TIME
    }

    private void log(String data) {
        System.out.println(myAgent.getAID().getName() + ": " + data);
    }

    @Override
    public void action() {
        switch (state) {
            case RECEIVE_REQUEST:
                log("waiting visitors");
                mt = MessageTemplate.MatchPerformative(ACLMessage.REQUEST);
                ACLMessage request = myAgent.receive(mt);

                if (request != null) {
                    ACLMessage reply = request.createReply();
                    if (((Passenger)myAgent).isBusy()) {
                        reply.setPerformative(ACLMessage.CANCEL);
                    } else {
                        reply.setPerformative(ACLMessage.AGREE);
                        state = PASSENGER_STATE.RECEIVE_NOTIFICATION;
                    }
                    myAgent.send(reply);
                } else {
                    block();
                }
                break;
            case RECEIVE_NOTIFICATION:
                log("got visitor request");
                mt = MessageTemplate.or(MessageTemplate.MatchPerformative(ACLMessage.CONFIRM),
                        MessageTemplate.MatchPerformative(ACLMessage.CANCEL));

                ACLMessage notification = myAgent.receive(mt);

                if (notification != null) {
                    if (notification.getPerformative() == ACLMessage.CONFIRM) {
                        state = PASSENGER_STATE.PARTY_TIME;
                    } else {
                        state = PASSENGER_STATE.RECEIVE_REQUEST;
                    }
                } else {
                    block();
                }
                break;
            case PARTY_TIME:
                log("acquiring visitor");
                mt = MessageTemplate.MatchPerformative(ACLMessage.INFORM);

                ACLMessage partyDurationInfo = myAgent.receive(mt);

                if (partyDurationInfo != null) {
                    String partyTime = partyDurationInfo.getContent();
                    Util.sleep(Long.parseLong(partyTime));
                    state = PASSENGER_STATE.RECEIVE_REQUEST;
                } else {
                    block();
                }

                break;
        }
    }
}
