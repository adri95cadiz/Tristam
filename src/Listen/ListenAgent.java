/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Listen;

import Communication.JSON;
import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.SingleAgent;
import es.upv.dsic.gti_ia.core.ACLMessage;
import java.util.HashMap;
/*import be.tarsos.dsp.onsets.ComplexOnsetDetector;
import be.tarsos.dsp.AudioProcessor;
import be.hogent.tarsos.dsp.util.FFT;*/

/**
 *
 * @author Adri
 * 
 * Este agente se encarga de escuchar el instrumento de entrada y parametrizar la reproducción de los loops.
 * 
 */
public class ListenAgent extends SingleAgent {

    // Para el funcionamiento del agente.
    private HashMap<String, ACLMessage> hashmapMensajes = new HashMap<String, ACLMessage>() {};
    private boolean fin = false;
    private final static int MAX_MESSAGES = 20;
    private Listen escucha;
      
    public ListenAgent(AgentID aid) throws Exception {
        super(aid);
        escucha = new Listen();
    }
    
    public void init() {
        System.out.println("Agente "+this.getAid().name+" iniciado");
        /* Envio mensaje conexión inicial a Filter. */
        this.sendParameters("Filter", "connectionBegin");   
        /* Envio mensaje conexión inicial a Director. */
        this.sendParameters("Director", "connectionBegin");   
        /* Envio mensaje conexión inicial a Interface. */
        this.sendParameters("Interface", "connectionBegin");  
        /* Esperando recepción de los mensajes */
        while(!hashmapMensajes.isEmpty()){}        
        System.out.println("Agente "+this.getAid().name+" conectado correctamente.");
    }
     
    public void execute() {        
        Thread thread = new Thread(escucha);
        thread.start();
        while(!fin){
            if(!escucha.getColaParametros().isEmpty()) {                         // Hay parámetros analizados esperando a ser enviados.
                /* Tomando el primer parámetro de la cola */
                String parameters = escucha.getColaParametros().get(0);
                escucha.getColaParametros().remove(0);
                /* Envio cadena parámetros a Filter. */
                this.sendParameters("Filter", parameters);            
                /* Envio cadena parámetros a Director. */
                this.sendParameters("Director", parameters);
                /* Envio cadena parámetros a Interface. */
                this.sendParameters("Interface", parameters);
            } else if(hashmapMensajes.size() > MAX_MESSAGES) {     // Si hay demasiados mensajes en espera se finaliza.                
                System.out.println(this.getAid().name + ": Demasiados mensajes en espera: " + hashmapMensajes.size() + ". FINALIZANDO");
                fin = true;
            }    
        }  
    }
     
    public void finalize() {
        System.out.println("Agente "+this.getAid().name+" finalizando");
        super.finalize();        
    }        
    
    /**
     * 
     * @param receiver Receptor de los parámetros.
     * @param content Cadena JSON con los parámetros.
     */    
    private void sendParameters(String receiver, String content) {
        ACLMessage msjSalida = new ACLMessage(ACLMessage.REQUEST);
        msjSalida.setSender(this.getAid());
        msjSalida.setReceiver(new AgentID(receiver));
        msjSalida.setContent(content);
        int num = 0;
        if(!content.equals("connectionBegin")) num = JSON.parameterNumber(content);
        msjSalida.setConversationId("parameters" + num + "from" + this.getAid().name + "to" + receiver);
        msjSalida.setReplyWith("parameters" + num + "from" + this.getAid().name + "to" + receiver);
        this.send(msjSalida);
        hashmapMensajes.put(msjSalida.getReplyWith(), msjSalida);
        System.out.println(this.getAid().name + ": Parámetros enviados a " + receiver);
    }    
    
    public void onMessage(ACLMessage msg) {
        if(msg.getPerformativeInt() == ACLMessage.AGREE){           // AGREE recibido.     
            if( hashmapMensajes.containsKey(msg.getReplyWith()) ) {               
                System.out.println(this.getAid().name + ": Agree recibido desde " + msg.getSender().name + ". ID mensaje: " + msg.getReplyWith());
            } else {
                System.out.println(this.getAid().name + ": Agree recibido desde " + msg.getSender().name + " no registrado. ID mensaje: " + msg.getReplyWith() + ".");
            }
        } else if(msg.getPerformativeInt() == ACLMessage.INFORM){   // INFORM recibido.
            if(msg.getContent().contains("OK")){                    // INFORM<OK>.          
                if(hashmapMensajes.containsKey(msg.getReplyWith()) ) {
                    System.out.println(this.getAid().name + ": OK recibido desde " + msg.getSender().name + ". ID mensaje: " + msg.getReplyWith());    
                    hashmapMensajes.remove(msg.getReplyWith());
                } else {                                            
                    System.out.println(this.getAid().name + ": OK recibido desde " + msg.getSender().name + " no registrado. ID mensaje: " + msg.getReplyWith() + ". FINALIZANDO");
                    fin = true;
                }
            } else {                                                // INFORM<!OK>. 
                System.out.println(this.getAid().name + ": Este agente no debería recibir mensajes de tipo INFORM que no sean OK. FINALIZANDO");
                fin = true;
            }
        } else {                                                    // !INFORM && !AGREE
            System.out.println(this.getAid().name + ": Este agente no debería recibir mensajes que no sean de tipo AGREE o INFORM. FINALIZANDO");
            fin = true;
        }
    }
}
