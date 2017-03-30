/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Filter;

import es.upv.dsic.gti_ia.core.AgentID;

/**
 *
 * @author Adri
 * 
 * Filtro de tempo.
 * 
 */
public class TimeSigFilter extends FilterAgent {

    public TimeSigFilter(AgentID aid) throws Exception {
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
