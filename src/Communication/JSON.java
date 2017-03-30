/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Communication;

/**
 *
 * @author Adri
 */

import com.eclipsesource.json.*;
import es.upv.dsic.gti_ia.core.ACLMessage;
import es.upv.dsic.gti_ia.core.AgentID;

/**
 *
 * @author Adri
 * 
 * Esta clase encapsula la comunicación entre los agentes.
 * 
 */
public class JSON {
    
    /**
     * Leemos los parámetros y creamos la cadena JSON que los contiene. 
     * @author Adri
     * @param totalBPM BPM totales desde el inicio la improvisación.
     * @param netoBPM BPM netos adaptados a los últimos momentos de la improvisación.
     * @param beat duración de un beat: 1/beat.
     * @param note nota tocada de la escala.
     * @param octave octava de la nota tocada.
     * @param keySig clave en la que se está tocando.
     * 
     * @return Cadena JSON de parámetros
     */
    public static String listenParameters(double totalBPM, double netoBPM, int beat, int note, int octave, String keySig) {
        JsonObject objeto = new JsonObject();
        objeto.add("bpmTotal",totalBPM);
        objeto.add("bpmAdapted",netoBPM);
        objeto.add("beat",beat);
        objeto.add("note",note);
        objeto.add("octave",octave);
        objeto.add("keySig",keySig);
        return objeto.toString();     
    }
    
}