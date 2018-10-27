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

import Sequencer.Sequencer;
import Parameters.*;
import com.eclipsesource.json.*;
import java.util.ArrayList;

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
     * @param  parametros parametros de la reproducción para pasar a JSONObject.    
     * @return Cadena JSON de parámetros
     */
    public static String listenParameters(Parameters parametros) {
        JsonObject objeto = new JsonObject();
        objeto.add("bpm",parametros.getBpm());
        objeto.add("bpmAdapted",parametros.getBpmAdapted());
        objeto.add("beat",parametros.getBeat());
        objeto.add("note",parametros.getNote());
        objeto.add("octave",parametros.getOctave());
        objeto.add("key",parametros.getKey());
        objeto.add("num",parametros.getNum());
        return objeto.toString();     
    }    
    
    public static Parameters listenParameters(String parametros) {        
        JsonObject objeto = Json.parse(parametros).asObject();
        Parameters parameters = new Parameters();
        parameters.setBpm(Double.parseDouble(objeto.getString("bpm", null)));
        parameters.setBpmAdapted(Double.parseDouble(objeto.getString("bpmAdapted", null)));
        parameters.setBeat(Integer.parseInt(objeto.getString("beat", null)));
        parameters.setNote(Integer.parseInt(objeto.getString("note", null)));
        parameters.setOctave(Integer.parseInt(objeto.getString("octave", null)));
        parameters.setKey(objeto.getString("beat", null));
        parameters.setNum(Integer.parseInt(objeto.getString("num", null)));
        return parameters;     
    }    
    
    public static int parameterNumber(String parametros) {        
        JsonObject objeto = Json.parse(parametros).asObject();
        int num = Integer.parseInt(objeto.getString("num", null));
        return num;     
    } 
    
    public static String loops(ArrayList<Sequencer> loops, int num){    
        JsonObject objeto = new JsonObject();
        for(int i = 0; i<loops.size(); i++){
            objeto.add("loop"+i, JSON.loop(loops.get(i)));
        }
        objeto.add("num", num);
        return objeto.toString();
    }
    
    public static ArrayList<Sequencer> loops (String jsonLoops){                  
        JsonObject objeto = Json.parse(jsonLoops).asObject();
        ArrayList<Sequencer> loops = new ArrayList<>();
        for(int i = 0; objeto.getString("loop"+i, null) != null ; i++){
            loops.add(JSON.loop(objeto.getString("loop"+i, null)));
        }
        return loops;
    }
    
    public static int loopNumber (String jsonLoops){                  
        JsonObject objeto = Json.parse(jsonLoops).asObject();
        int num = Integer.parseInt(objeto.getString("num", null));        
        return num;
    }
    
    public static String loop(Sequencer loop){    
        JsonObject objeto = new JsonObject();
        objeto.add("loop", 1);        
        return objeto.toString();
    }
    
    public static Sequencer loop(String thisLoop){         
        JsonObject objeto = Json.parse(thisLoop).asObject();
        Sequencer loop = new Sequencer();        
        objeto.getString("loop", null);
        return loop;
    }
}