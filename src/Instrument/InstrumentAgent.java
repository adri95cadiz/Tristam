/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Instrument;

import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.SingleAgent;

/**
 *
 * @author Adri
 * 
 * Este agente actúa como instrumento del sistema, seleccionando de los loops de su instrumento los más prometedores.
 * 
 */
public class InstrumentAgent extends SingleAgent {

    public InstrumentAgent(AgentID aid) throws Exception {
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
