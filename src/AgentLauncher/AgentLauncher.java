/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package AgentLauncher;

import es.upv.dsic.gti_ia.core.AgentID;
import Combination.*;
import Director.*;
import Filter.*;
import Instrument.*;
import Interface.*;
import Listen.*;
import Player.*;
import es.upv.dsic.gti_ia.core.AgentsConnection;

/**
 * Clase principal que lanza todos los agentes en un solo ordenador.
 * 
 * @author Adri
 */
public class AgentLauncher {
    
    public static void main(String[] args) throws Exception {
        CombinationAgent Combination = null;
        DirectorAgent Director = null;
        FilterAgent Filter = null;
        InstrumentAgent Instrument = null;
        InterfaceAgent Interface = null;
        ListenAgent Listen = null;
        PlayerAgent Player = null;
        
        AgentsConnection.connect("localhost", 5672, "test", "guest", "guest", false);
        
        try {
            Combination = new CombinationAgent(new AgentID("Combination"));
            Director = new DirectorAgent(new AgentID("Director"));
            Filter = new MasterFilter(new AgentID("Filter"));
            Instrument = new InstrumentAgent(new AgentID("Instrument"));
            Interface = new InterfaceAgent(new AgentID("Interface"));
            Listen = new ListenAgent(new AgentID("Listen"));
            Player = new PlayerAgent(new AgentID("Player"));
        } catch (Exception e) { 
            System.err.println("Error al crear los agentes");
            System.err.println(e);
            System.exit(1);
        }
        Combination.start();
        Director.start();
        Filter.start();
        Instrument.start();
        Interface.start();
        Listen.start();
        Player.start();
    }
}
