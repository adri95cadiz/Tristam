/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Filter;

import Communication.JSON;
import Loop.*;
import es.upv.dsic.gti_ia.core.ACLMessage;
import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.SingleAgent;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Adri
 * 
 * Este agente controla los filtros que se utilizarán para seleccionar los loops que se corresponden con ciertos parámetros.
 * 
 */
public abstract class FilterAgent extends SingleAgent {
    
    private AgentID next;
    protected HashMap<String, ACLMessage> hashmapMensajes = new HashMap<String, ACLMessage>() {};
    protected ArrayList<Loop> loops = new ArrayList<>();
    protected final static int MAX_MESSAGES = 20;
    
    public FilterAgent(AgentID aid, AgentID next) throws Exception {
        super(aid);
        this.next = next;
    }
        
    public void init() {     
    }
     
    public void execute() {
         
    }
     
    public void finalize() {
        super.finalize();        
    }    
    
    public AgentID getNext(){
        return this.next;
    }    
    
    /**
     * 
     * @param receiver Receptor de los parámetros.
     * @param content Cadena JSON con los parámetros.
     */    
    protected void sendParameters(String receiver, String content) {
        ACLMessage msjSalida = new ACLMessage(ACLMessage.REQUEST);
        msjSalida.setSender(this.getAid());
        msjSalida.setReceiver(new AgentID(receiver));
        msjSalida.setContent(content);
        int num = 0;
        if(!content.equals("connectionBegin")) num = JSON.parameterNumber(content);
        msjSalida.setConversationId("parameters" + num + "from" +this.getAid().name + "to" + receiver);
        msjSalida.setReplyWith("parameters" + num + "from" +this.getAid().name + "to" + receiver);
        this.send(msjSalida);
        hashmapMensajes.put(msjSalida.getReplyWith(), msjSalida);
        System.out.println(this.getAid().name + ": Parámetros enviados a " + receiver);
    } 
    
    /**
     * 
     * @param receiver Receptor de los parámetros.
     * @param content Cadena JSON con los loops.
     */    
    protected void sendLoops(String receiver, String content) {
        ACLMessage msjSalida = new ACLMessage(ACLMessage.REQUEST);
        msjSalida.setSender(this.getAid());
        msjSalida.setReceiver(new AgentID(receiver));
        msjSalida.setContent(content);
        int num = 0;
        if(!content.equals("connectionBegin")) num = JSON.parameterNumber(content);
        msjSalida.setConversationId("loops" + num + "from" +this.getAid().name + "to" + receiver);
        msjSalida.setReplyWith("loops" + num + "from" +this.getAid().name + "to" + receiver);
        this.send(msjSalida);
        hashmapMensajes.put(msjSalida.getReplyWith(), msjSalida);
        System.out.println(this.getAid().name + ": Loops enviados a " + receiver);
    } 
    
    protected void informOK(String receiver, String conversationID, String replyWith) {
        ACLMessage msjSalida = new ACLMessage(ACLMessage.INFORM);
        msjSalida.setSender(this.getAid());
        msjSalida.setReceiver(new AgentID(receiver));
        msjSalida.setContent("OK");
        msjSalida.setConversationId(conversationID);
        msjSalida.setInReplyTo(replyWith);
        msjSalida.setReplyWith(replyWith);
        this.send(msjSalida);
        System.out.println(this.getAid().name + ": OK to request from " + receiver);
    }
    
    protected void sendAgree(String receiver, String conversationID, String replyWith) {
        ACLMessage msjSalida = new ACLMessage(ACLMessage.AGREE);
        msjSalida.setSender(this.getAid());
        msjSalida.setReceiver(new AgentID(receiver));
        msjSalida.setConversationId(conversationID);
        msjSalida.setInReplyTo(replyWith);
        msjSalida.setReplyWith(replyWith);
        this.send(msjSalida);
        System.out.println(this.getAid().name + ": AGREE to request from " + receiver);
    }  
    
}
