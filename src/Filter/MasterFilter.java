/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Filter;

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
      
    private ArrayList<FilterAgent> filtros;
    
    public MasterFilter(AgentID aid) throws Exception {
        super(aid);
        filtros.add(new BPMFilter(new AgentID("BPMFilter")));
        filtros.add(new KeySigFilter(new AgentID("KeySigFilter")));
        filtros.add(new OctaveFilter(new AgentID("OctaveFilter")));
        filtros.add(new TimeSigFilter(new AgentID("TimeSigFilter")));
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
