/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package AgentLauncher;

import MidiProcessor.MidiProcessorAgent;
import Composer.*;
import es.upv.dsic.gti_ia.core.AgentID;
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
        MidiProcessorAgent Combination = null;
        ComposerAgent Director = null;
        InstrumentAgent Instrument = null;
        InterfaceAgent Interface = null;
        ListenAgent Listen = null;
        PlayerAgent Player = null;
        
        AgentsConnection.connect("localhost", 5672, "test", "guest", "guest", false);
        
        try {
            Combination = new MidiProcessorAgent(new AgentID("Combination"));
            Director = new ComposerAgent(new AgentID("Director"));
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
        Instrument.start();
        Interface.start();
        Listen.start();
        Player.start();
    }
}
