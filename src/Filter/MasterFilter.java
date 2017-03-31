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
 * Este agente es el filtro que controla 
 * 
 */
public class MasterFilter extends FilterAgent {
      
    private FilterAgent filtros[] = new FilterAgent[4];
    
    public MasterFilter(AgentID aid) throws Exception {
        super(aid);
        filtros[0] = new BPMFilter(new AgentID("BPMFilter"));
        filtros[1] = new KeySigFilter(new AgentID("KeySigFilter"));
        filtros[2] = new OctaveFilter(new AgentID("OctaveFilter"));
        filtros[3] = new TimeSigFilter(new AgentID("TimeSigFilter"));
    }
        
    public void init() {
        System.out.println("Agente "+this.getAid()+" iniciado");  
        for ( FilterAgent filtro : filtros ) {
            filtro.start();
        }
    }
     
    public void execute() {
         
    }
     
    public void finalize() {
        System.out.println("Agente "+this.getAid()+" finalizando");
        super.finalize();        
    }    
}
