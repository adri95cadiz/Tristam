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
import java.util.ArrayList;

/**
 *
 * @author Adri
 * 
 * Este agente es el filtro que controla 
 * 
 */
public class MasterFilter extends FilterAgent {
      
    private FilterAgent filtros[] = new FilterAgent[4];
    private boolean fin = false;
    private ArrayList<String> colaParametros = new ArrayList<>();
    private boolean listenConnected = false;
    private boolean directorConnected = false;
    private boolean filtersConnected = false;
    
    public MasterFilter(AgentID aid, AgentID next) throws Exception {
        super(aid, next);
        filtros[3] = new TimeSigFilter(new AgentID("TimeSigFilter"), this.getAid());
        filtros[2] = new OctaveFilter(new AgentID("KeySigFilter"), filtros[3].getAid());
        filtros[1] = new KeySigFilter(new AgentID("OctaveFilter"), filtros[2].getAid());
        filtros[0] = new BPMFilter(new AgentID("BPMFilter"), filtros[1].getAid());
    }
        
    public void init() {
        System.out.println("Agente " + this.getAid().name + " iniciado");  
        
        for ( FilterAgent filtro : filtros ) {
            filtro.start();
        }
        /* Enviando conexión inicial al primer filtro */
        this.sendParameters(filtros[0].getAid().name, "connectionBegin");
        while(!listenConnected || !directorConnected || !filtersConnected){}
        System.out.println("Agente " + this.getAid().name + " conectado correctamente.");  
    }
     
    public void execute() {
        while(!fin){
            if(!colaParametros.isEmpty()){                
                /* Tomando el primer parámetro de la cola */
                String parameters = colaParametros.get(0);
                colaParametros.remove(0);
                /* AQUÍ SE TOMARÁN LOS LOOPS DE LA BASE DE DATOS */
                loops = new ArrayList<>();    
                /* --------------------------------------------- */
                /* Envio loops al primer filtro. */
                this.sendLoops(filtros[0].getName(), JSON.loops(loops, JSON.parameterNumber(parameters)));
                /* Envio parámetros al resto de filtros. */
                for ( FilterAgent filtro : filtros ) {
                    this.sendParameters(filtro.getName(), parameters);    
                }
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
            if(msg.getSender().name.equals("Listen") || msg.getSender().name.equals("Director") || msg.getSender().name.contains("Filter")){
                this.sendAgree(msg.getSender().name, msg.getConversationId(), msg.getReplyWith());
                if(msg.getSender().name.equals("Listen") || msg.getSender().name.equals("Director")) {
                    if(msg.getContent().equals("connectionBegin")){
                        if(msg.getSender().name.equals("Listen") && !listenConnected) {
                            System.out.println(this.getAid().name + ": Recibida conexión inicial desde " + msg.getSender().name);
                            this.informOK(msg.getSender().name, msg.getConversationId(), msg.getReplyWith());
                            listenConnected = true;
                        } else if(listenConnected){                   
                            System.out.println(this.getAid().name + ": Recibida conexión inicial desde " + msg.getSender().name + " ya recibida anteriormente. FINALIZANDO");
                            fin = true;                            
                        } else if(msg.getSender().name.equals("Director") && !directorConnected) {
                            System.out.println(this.getAid().name + ": Recibida conexión inicial desde " + msg.getSender().name);
                            this.informOK(msg.getSender().name, msg.getConversationId(), msg.getReplyWith());
                            directorConnected = true;
                        } else if(directorConnected){                   
                            System.out.println(this.getAid().name + ": Recibida conexión inicial desde " + msg.getSender().name + " ya recibida anteriormente. FINALIZANDO");
                            fin = true;                            
                        }
                    } else {                          
                        System.out.println(this.getAid().name + ": Recibidos parámetros desde " + msg.getSender().name);
                        colaParametros.add(msg.getContent()); 
                    }
                } else if(msg.getSender().name.contains("Filter")){
                    if(msg.getSender().name.equals(filtros[filtros.length-1].getName()) && msg.getReplyWith().contains("loops")){                        
                        if(msg.getContent().equals("connectionBegin") && !filtersConnected){
                            System.out.println(this.getAid().name + ": Recibida conexión inicial desde " + msg.getSender().name);
                            filtersConnected = true;                            
                        } else if(msg.getContent().equals("connectionBegin") && filtersConnected) {                                             
                            /*System.out.println(this.getAid().name + ": Recibida conexión inicial desde " + msg.getSender().name + " ya recibida anteriormente. FINALIZANDO");
                            fin = true; */       
                        } else if(msg.getContent().contains("loop")){
                            this.sendLoops(this.getNext().name, msg.getContent()); 
                            int num = JSON.loopNumber(msg.getContent());
                            this.informOK("Listen", "parameters" + num + "fromListento"+this.getName(), "parameters" + num + "fromListento"+this.getName());
                            this.informOK("Director", "parameters" + num + "fromDirectorto"+this.getName(), "parameters" + num + "fromDirectorto"+this.getName());                            
                        } else {         
                            System.out.println(this.getAid().name + ": Este agente no debería algo que no sean loops desde un Filter. FINALIZANDO");
                            fin = true;                            
                        }
                    } else if (msg.getContent().equals("connectionBegin")){                        
                        //System.out.println(this.getAid().name + ": Agente " + msg.getSender().name + "conectado correctamente a MasterFilter.");
                    } else {                                  
                        System.out.println(this.getAid().name + ": Este agente no debería recibir REQUESTs que vengan de un filtro que no sea el último; recibido desde "+msg.getSender().name+". FINALIZANDO");
                        fin = true;
                    }                        
                }
            } else {                
                System.out.println(this.getAid().name + ": Este agente no debería recibir REQUESTs que vengan de otros destinatarios que Listen, Director o un Filter; recibido desde "+msg.getSender().name+". FINALIZANDO");
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