/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Director;

import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.SingleAgent;

/**
 *
 * @author Adri
 */
public class DirectorAgent extends SingleAgent {

    public DirectorAgent(AgentID aid) throws Exception {
        super(aid);
    }
        
    public void init() {     
        System.out.println("Agente "+this.getAid().name+" iniciado");
    }
     
    public void execute() {
        
    }
     
    public void finalize() {
        System.out.println("Agente "+this.getAid().name+" finalizando");
        super.finalize();        
    }    
    
}
