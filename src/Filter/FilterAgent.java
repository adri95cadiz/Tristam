/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Filter;

import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.SingleAgent;

/**
 *
 * @author Adri
 * 
 * Este agente controla los filtros que se utilizarán para seleccionar los loops que se corresponden con ciertos parámetros.
 * 
 */
public abstract class FilterAgent extends SingleAgent {
    
    public FilterAgent(AgentID aid) throws Exception {
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
