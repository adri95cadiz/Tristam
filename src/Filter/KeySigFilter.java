/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Filter;

import Communication.*;
import static Filter.FilterAgent.MAX_MESSAGES;
import es.upv.dsic.gti_ia.core.ACLMessage;
import es.upv.dsic.gti_ia.core.AgentID;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Adri
 * 
 * Filtro de clave.
 * 
 */
public class KeySigFilter extends FilterAgent {
    
    private boolean filterConnected = false;
    private boolean masterConnected = false;
    private boolean fin = false;
    private ArrayList<String> colaParametros = new ArrayList<>();
    private HashMap<Integer, String> colaLoops = new HashMap<Integer, String>() {};
    private AgentID anterior = null;

    public KeySigFilter(AgentID aid, AgentID next) throws Exception {
        super(aid, next);
    }
    
    public void init() {
        System.out.println("Agente " + this.getAid().name + " iniciado");  
        /* Enviando conexión inicial al siguiente filtro */
        this.sendParameters(this.getNext().name, "connectionBegin");
        while(!filterConnected || !masterConnected){}
        System.out.println("Agente " + this.getAid().name + " conectado correctamente.");  
    }
     
    public void execute() {
        while(!fin){
            if(!colaParametros.isEmpty()){                
                /* Tomando el primer parámetro de la cola */
                String parameters = colaParametros.get(0);
                colaParametros.remove(0);
                /* AQUÍ SE FILTRARÁN LOS LOOPS RECIBIDOS DE LA BASE DE DATOS */   
                int num = JSON.parameterNumber(parameters);             
                String loop = colaLoops.get(num);
                colaLoops.remove(num);   
                /* --------------------------------------------- */
                /* Envio loops al siguiente filtro. */
                this.sendLoops(this.getNext().name, loop);
                this.informOK("Filter", "parameters" + num + "fromFilterto" + this.getName(), "parameters" + num + "fromFilterto" + this.getName());
                this.informOK(anterior.name, "loops" + num + "from" + anterior.name + "to" + this.getName(), "loops" + num + "from" + anterior.name + "to" + this.getName());
            } else if(hashmapMensajes.size() > MAX_MESSAGES) {     // Si hay demasiados mensajes en espera se finaliza.               
                System.out.println(this.getAid().name + ": Demasiados mensajes en espera: " + hashmapMensajes.size() + ". FINALIZANDO");
                fin = true;
            }  
        }
    }
     
    public void finalize() {
        System.out.println("Agente " + this.getAid().name + " finalizando");
        super.finalize();        
    } 
  
    public void onMessage(ACLMessage msg) {
        if(msg.getPerformativeInt() == ACLMessage.REQUEST){             // REQUEST recibido.   
            if(msg.getSender().name.contains("Filter")){
                this.sendAgree(msg.getSender().name, msg.getConversationId(), msg.getReplyWith());
                if(msg.getContent().equals("connectionBegin") && (!filterConnected || !masterConnected)){
                    System.out.println(this.getAid().name + ": Recibida conexión inicial desde " + msg.getSender().name);
                    if(!msg.getSender().name.equals("Filter")){
                        this.sendParameters(this.getNext().name, "connectionBegin");
                        anterior = msg.getSender();
                        if(filterConnected){                                                                       
                            System.out.println(this.getAid().name + ": Recibida conexión inicial desde " + msg.getSender().name + " ya recibida anteriormente. FINALIZANDO");
                            fin = true; 
                        } else filterConnected = true;
                    } else {
                        if(masterConnected){                                                                       
                            System.out.println(this.getAid().name + ": Recibida conexión inicial desde " + msg.getSender().name + " ya recibida anteriormente. FINALIZANDO");
                            fin = true; 
                        } else masterConnected = true;
                    }
                    this.informOK(msg.getSender().name, msg.getConversationId(), msg.getReplyWith());                          
                } else if(msg.getContent().equals("connectionBegin") && filterConnected && masterConnected) {                                             
                    System.out.println(this.getAid().name + ": Recibida conexión inicial desde " + msg.getSender().name + " ya recibida anteriormente. FINALIZANDO");
                    fin = true; 
                } else if(msg.getContent().contains("loop")){
                    System.out.println(this.getAid().name + ": Recibidos loops desde " + msg.getSender().name);
                    colaLoops.put(JSON.loopNumber(msg.getContent()), msg.getContent());
                } else if(msg.getContent().contains("bpm")) {   
                    System.out.println(this.getAid().name + ": Recibidos parámetros desde " + msg.getSender().name);
                    colaParametros.add(msg.getContent());  
                } else {            
                    System.out.println(this.getAid().name + ": Este agente no debería algo que no sean loops o parámetros. FINALIZANDO");
                    fin = true;                    
                }
            } else {                      
                System.out.println(this.getAid().name + ": Este agente no debería recibir REQUESTs que vengan de otros destinatarios que un Filter; recibido desde "+msg.getSender().name+". FINALIZANDO");
                fin = true;
            }
        } else if(msg.getPerformativeInt() == ACLMessage.AGREE) {       // AGREE recibido.
            if( hashmapMensajes.containsKey(msg.getReplyWith()) ) {               
                System.out.println(this.getAid().name + ": Agree recibido desde " + msg.getSender().name + ". ID mensaje: " + msg.getReplyWith());
            } else {
                System.out.println(this.getAid().name + ": Agree recibido desde " + msg.getSender().name + " no registrado. ID mensaje: " + msg.getReplyWith() + ".");
            }            
        } else if(msg.getPerformativeInt() == ACLMessage.INFORM) {      // INFORM recibido.            
            if(msg.getContent().contains("OK")){                        // INFORM<OK>.          
                if(hashmapMensajes.containsKey(msg.getReplyWith()) ) {
                    System.out.println(this.getAid().name + ": OK recibido por " + msg.getSender().name + ". ID mensaje: " + msg.getReplyWith());    
                    hashmapMensajes.remove(msg.getReplyWith());
                } else {                                            
                    System.out.println(this.getAid().name + ": OK recibido desde " + msg.getSender().name + " no registrado. ID mensaje: " + msg.getReplyWith() + ". FINALIZANDO");
                    fin = true;
                }
            } else {                                                    // INFORM<!OK>. 
                System.out.println(this.getAid().name + ": Este agente no debería recibir mensajes de tipo INFORM que no sean OK. FINALIZANDO");
                fin = true;
            }
        } else {                                                        // !REQUEST && !AGREE && !INFORM            
            System.out.println(this.getAid().name + ": Este agente no debería recibir mensajes que no sean de tipo REQUEST, AGREE O INFORM. FINALIZANDO");
            fin = true;
        }
    } 
    
}
