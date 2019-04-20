package com.mas.lab2.Util;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;

import java.util.Random;

public class Util {
    private static Random rand = new Random();

    public static int randInt(int from, int to) {
        return rand.nextInt(to - from) + from;
    }

    public static Double distance(Point a, Point b) {
//        Double res = 0.0;
//        try {
//            res = Math.sqrt((b.x.doubleValue() - a.x.doubleValue()) * (b.x.doubleValue() - a.x.doubleValue()) +
//                    (b.y.doubleValue() - b.y.doubleValue()) * (b.y.doubleValue() - b.y.doubleValue()));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return res;

        return 1488.0;
    }

    public static void sleep(long timeInMillis){
        try {
            Thread.sleep(timeInMillis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void registerAgentInYellowPages(Agent agent, String serviceName, String serviceType){
        DFAgentDescription agentDescription = new DFAgentDescription();
        agentDescription.setName(agent.getAID());
        ServiceDescription serviceDescription = new ServiceDescription();
        serviceDescription.setType(serviceType);
        serviceDescription.setName(serviceName);
        agentDescription.addServices(serviceDescription);
        try {
            DFService.register(agent, agentDescription);
        } catch (FIPAException e){
            e.printStackTrace();
        }
    }

    public static DFAgentDescription[] findAgents(Agent searchPerformer, String serviceName, String serviceType){
        DFAgentDescription template = new DFAgentDescription();
        ServiceDescription serviceDescription = new ServiceDescription();
        if(serviceName != null){
            serviceDescription.setName(serviceName);
        }
        if(serviceType != null) {
            serviceDescription.setType(serviceType);

        }
        template.addServices(serviceDescription);
        try {
            return DFService.searchUntilFound(searchPerformer, searchPerformer.getDefaultDF(),template, null, 60000);
        } catch (FIPAException e){
            e.printStackTrace();
        }
        return null;
    }

}
