/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Listen;

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
    private static int NumParameter = 0;
    private static HashMap<String, ACLMessage> hashmapMensajes = new HashMap<String, ACLMessage>() {};
    private static boolean fin = false;
    private final static int MAX_MESSAGES = 20;
    private Listen escucha;
      
    public ListenAgent(AgentID aid) throws Exception {
        super(aid);
        escucha = new Listen();
    }
    
    public void init() {
        Thread thread = new Thread(escucha);
        thread.start();
        System.out.println("Agente "+this.getAid()+" iniciado");
        /* Envio mensaje conexión inicial a Filter. */
        this.sendParameters("Filter", "connectionBegin");   
        /* Envio mensaje conexión inicial a Director. */
        this.sendParameters("Director", "connectionBegin");   
        /* Envio mensaje conexión inicial a Interface. */
        this.sendParameters("Interface", "connectionBegin");   
    }
     
    public void execute() {
        while(!fin){
            if(!escucha.getColaParametros().isEmpty()) {                         // Hay parámetros analizados esperando a ser enviados.
                String parameters = escucha.getColaParametros().get(0);
                escucha.getColaParametros().remove(0);
                /* Envio cadena parámetros a Filter. */
                this.sendParameters("Filter", parameters);            
                /* Envio cadena parámetros a Director. */
                this.sendParameters("Director", parameters);
                /* Envio cadena parámetros a Interface. */
                this.sendParameters("Interface", parameters);
                NumParameter++;
            } else if (hashmapMensajes.size() > MAX_MESSAGES) {     // Si hay demasiados mensajes en espera se finaliza.                
                System.out.println(this.getAid() + ": Demasiados mensajes en espera: " + hashmapMensajes.size() + ". FINALIZANDO");
                fin = true;
            }    
        }  
    }
     
    public void finalize() {
        System.out.println("Agente "+this.getAid()+" finalizando");
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
        msjSalida.setConversationId(this.getAid()+" to "+receiver);
        msjSalida.setReplyWith("parameters"+NumParameter+"to"+receiver);
        this.send(msjSalida);
        hashmapMensajes.put(msjSalida.getReplyWith(), msjSalida);
        System.out.println(this.getAid() + ": Parámetros enviados a " + receiver);
    }    
    
    public void onMessage(ACLMessage msg) {
        if(msg.getPerformativeInt() == ACLMessage.AGREE){           // AGREE recibido.     
            if( hashmapMensajes.containsKey(msg.getReplyWith()) ) {               
                System.out.println(this.getAid() + ": Parámetros recibidos por " + msg.getSender() + ". ID mensaje: " + msg.getReplyWith());
            } else {
                System.out.println(this.getAid() + ": Agree recibido desde " + msg.getSender() + " no registrado. ID mensaje: " + msg.getReplyWith());
            }
        } else if(msg.getPerformativeInt() == ACLMessage.INFORM){   // INFORM recibido.
            if(msg.getContent().contains("OK")){                    // INFORM<OK>.          
                if(hashmapMensajes.containsKey(msg.getReplyWith()) ) {
                    System.out.println(this.getAid() + ": OK recibido por " + msg.getSender() + ". ID mensaje: " + msg.getReplyWith());    
                    hashmapMensajes.remove(msg.getReplyWith());
                } else {                                            
                    System.out.println(this.getAid() + ": OK recibido desde " + msg.getSender() + " no registrado. ID mensaje: " + msg.getReplyWith());
                }
            } else {                                                // INFORM<!OK>. 
                System.out.println(this.getAid() + ": Este agente no debería recibir mensajes de tipo INFORM que no sean OK. FINALIZANDO");
                fin = true;
            }
        } else {                                                    // !INFORM && !AGREE
            System.out.println(this.getAid() + ": Este agente no debería recibir mensajes que no sean de tipo AGREE o INFORM. FINALIZANDO");
            fin = true;
        }
    }
}
