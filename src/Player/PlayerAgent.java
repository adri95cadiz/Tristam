/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Player;

import Loop.*;
import java.util.Stack;
import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.SingleAgent;

/**
 *
 * @author Adri
 * 
 * Este agente se encargará de encolar y reproducir los loops que le van llegando para su reproducción.
 * 
 */
public class PlayerAgent extends SingleAgent {

    private static Stack<Loop> pilaLoops = new Stack<>() ;   
    
    public PlayerAgent(AgentID aid) throws Exception {
        super(aid);
    }
        
    public void init() {
        System.out.println("Agente "+this.getAid()+" iniciado");     
    }
     
    public void execute() {
         
    }
     
    public void finalize() {
        System.out.println("Agente "+this.getAid()+" finalizando");        
        super.finalize();        
    }    
    
}
