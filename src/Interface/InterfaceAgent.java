/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Interface;

import es.upv.dsic.gti_ia.core.ACLMessage;
import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.SingleAgent;

/**
 *
 * @author Adri
 * 
 * Este agente comunicará la interfaz con el resto del sistema.
 * 
 */
public class InterfaceAgent extends SingleAgent {
    
    boolean playerConnected = false;
    boolean listenConnected = false;
    boolean composerConnected = false;
    private boolean fin = false;

    public InterfaceAgent(AgentID aid) throws Exception {
        super(aid);
    }
        
    public void init() {     
        System.out.println("Agente "+this.getAid().name+" iniciado");
        while(!playerConnected || !listenConnected || !composerConnected){}
        System.out.println("Agente " + this.getAid().name + " conectado correctamente.");  
    }
     
    public void execute() {
        while(!fin){
        }
    }
     
    public void finalize() {
        System.out.println("Agente "+this.getAid().name+" finalizando");        
        super.finalize();        
    }   
    
    private void informOK(String receiver, String conversationID, String replyWith) {
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
    
    private void sendAgree(String receiver, String conversationID, String replyWith) {
        ACLMessage msjSalida = new ACLMessage(ACLMessage.AGREE);
        msjSalida.setSender(this.getAid());
        msjSalida.setReceiver(new AgentID(receiver));
        msjSalida.setConversationId(conversationID);
        msjSalida.setInReplyTo(replyWith);
        msjSalida.setReplyWith(replyWith);
        this.send(msjSalida);
        System.out.println(this.getAid().name + ": AGREE to request from " + receiver);
    }
    
    public void onMessage(ACLMessage msg) {
        if(msg.getPerformativeInt() == ACLMessage.REQUEST){           // REQUEST recibido.   
            if(msg.getSender().name.equals("Listen") || msg.getSender().name.equals("Player") || msg.getSender().name.equals("Director")){
                this.sendAgree(msg.getSender().name, msg.getConversationId(), msg.getReplyWith());
                switch(msg.getSender().name){
                    case "Listen":
                        if(msg.getContent().equals("connectionBegin") && !listenConnected){
                            System.out.println(this.getAid().name + ": Recibida conexión inicial desde " + msg.getSender().name);
                            listenConnected = true;
                            this.informOK(msg.getSender().name, msg.getConversationId(), msg.getReplyWith());           
                        } else if(!msg.getContent().equals("connectionBegin") && listenConnected){
                            Interface.setParametros(msg.getContent());
                            System.out.println(this.getAid().name + ": Parametros actualizados.");
                            this.informOK(msg.getSender().name, msg.getConversationId(), msg.getReplyWith());       
                        } else{                            
                            System.out.println(this.getAid().name + ": Recibida conexión inicial desde " + msg.getSender().name + " ya recibida anteriormente. FINALIZANDO");
                            fin = true;
                        } break;
                    case "Director":
                        if(msg.getContent().equals("connectionBegin") && !composerConnected){
                            System.out.println(this.getAid().name + ": Recibida conexión inicial desde " + msg.getSender().name);
                            composerConnected = true;
                            this.informOK(msg.getSender().name, msg.getConversationId(), msg.getReplyWith());       
                        } else if(!msg.getContent().equals("connectionBegin") && composerConnected) {
                            Interface.setDirectiva(msg.getContent());
                            System.out.println(this.getAid().name + ": Partitura actualizada.");
                            this.informOK(msg.getSender().name, msg.getConversationId(), msg.getReplyWith());       
                        } else {                                                 
                            System.out.println(this.getAid().name + ": Recibida conexión inicial desde " + msg.getSender().name + " ya recibida anteriormente. FINALIZANDO");
                            fin = true;
                        } break;
                    case "Player":
                        if(msg.getContent().equals("connectionBegin") && !playerConnected){
                            System.out.println(this.getAid().name + ": Recibida conexión inicial desde " + msg.getSender().name);
                            playerConnected = true;                            
                            this.informOK(msg.getSender().name, msg.getConversationId(), msg.getReplyWith());       
                        } else if(!msg.getContent().equals("connectionBegin") && playerConnected) {
                            Interface.setLoop(msg.getContent());
                            System.out.println(this.getAid().name + ": Secuencia actualizado.");
                            this.informOK(msg.getSender().name, msg.getConversationId(), msg.getReplyWith());       
                        } else{                     
                            System.out.println(this.getAid().name + ": Recibida conexión inicial desde " + msg.getSender().name + " ya recibida anteriormente. FINALIZANDO");
                            fin = true;                            
                        } break;             
                }         
            } else {                
                System.out.println(this.getAid().name + ": Este agente no debería recibir mensajes que vengan de otros destinatarios que Listen, Player o Director; recibido desde "+msg.getSender().name+". FINALIZANDO");
                fin = true;
            }
        } else {   // !REQUEST.
            System.out.println(this.getAid().name + ": Este agente no debería recibir mensajes que no sean de tipo REQUEST. FINALIZANDO");
            fin = true;
        }
    }
    
}
