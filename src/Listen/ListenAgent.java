/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Listen;

import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;
import be.tarsos.dsp.pitch.PitchProcessor.PitchEstimationAlgorithm;
import es.upv.dsic.gti_ia.core.AgentID;
import es.upv.dsic.gti_ia.core.SingleAgent;
import java.util.ArrayList;
import java.util.Stack;
import javax.sound.sampled.LineUnavailableException;
import Communication.*;
import es.upv.dsic.gti_ia.core.ACLMessage;
import java.util.HashMap;
/*import be.tarsos.dsp.onsets.ComplexOnsetDetector;
import be.tarsos.dsp.AudioProcessor;
import be.hogent.tarsos.dsp.util.FFT;*/

/**
 *
 * @author Adri
 * 
 * Este agente se encarga de escuchar el instrumento de entrada y parametrizar la reproducción de los loops.
 * 
 */
public class ListenAgent extends SingleAgent {

    private final static int MIN_TEMPO_BPM = 60;
    private final static int MAX_TEMPO_BPM = 250;
    private final static int MAX_NOTES_KEY = 15;
    //Para el bpm total.
    private static double StartTime_ms = 0;
    private static double FinishTime_ms = 0;
    private static double CurrentTime_ms = 0;
    private static int NumberSamples = 0;
    //Para el funcionamiento del agente.
    private static int NumParameter = 0;
    private static Stack<String> pilaParametros = new Stack<>();   
    private static HashMap<String, ACLMessage> hashmapMensajes = new HashMap<String, ACLMessage>() {};
    private static boolean fin = false;
    private final static int MAX_MESSAGES = 20;
      
    public ListenAgent(AgentID aid) throws Exception {
        super(aid);
    }
    
    public void init() {
        System.out.println("Agente "+this.getAid()+" iniciado");
        /* Envio mensaje conexión inicial a Filter. */
        this.sendParameters("Filter", "connectionBegin");   
        /* Envio mensaje conexión inicial a Director. */
        this.sendParameters("Director", "connectionBegin");   
        /* Envio mensaje conexión inicial a Interface. */
        this.sendParameters("Interface", "connectionBegin");   
    }
     
    public void execute() {
        while(!fin){
            if(!pilaParametros.isEmpty()) {                         // Hay parámetros analizados esperando a ser enviados.
                String parameters = pilaParametros.pop();
                /* Envio cadena parámetros a Filter. */
                this.sendParameters("Filter", parameters);            
                /* Envio cadena parámetros a Director. */
                this.sendParameters("Director", parameters);
                /* Envio cadena parámetros a Interface. */
                this.sendParameters("Interface", parameters);
                NumParameter++;
            } else if (hashmapMensajes.size() > MAX_MESSAGES) {     // Si hay demasiados mensajes en espera se finaliza.                
                System.out.println(this.getAid() + ": Demasiados mensajes en espera: " + hashmapMensajes.size() + ". FINALIZANDO");
                fin = true;
            }    
        }  
    }
     
    public void finalize() {
        System.out.println("Agente "+this.getAid()+" finalizando");
        super.finalize();        
    }        
    
    /**
     * 
     * @param receiver Receptor de los parámetros.
     * @param content Cadena JSON con los parámetros.
     */    
    private void sendParameters(String receiver, String content) {
        ACLMessage msjSalida = new ACLMessage(ACLMessage.REQUEST);
        msjSalida.setSender(this.getAid());
        msjSalida.setReceiver(new AgentID(receiver));
        msjSalida.setContent(content);
        msjSalida.setConversationId(this.getAid()+" to "+receiver);
        msjSalida.setReplyWith("parameters"+NumParameter+"to"+receiver);
        this.send(msjSalida);
        hashmapMensajes.put(msjSalida.getReplyWith(), msjSalida);
        System.out.println(this.getAid() + ": Parámetros enviados a " + receiver);
    }    
    
    public void onMessage(ACLMessage msg) {
        if(msg.getPerformativeInt() == ACLMessage.AGREE){           // AGREE recibido.     
            if( hashmapMensajes.containsKey(msg.getReplyWith()) ) {               
                System.out.println(this.getAid() + ": Parámetros recibidos por " + msg.getSender() + ". ID mensaje: " + msg.getReplyWith());
            } else {
                System.out.println(this.getAid() + ": Agree recibido desde " + msg.getSender() + " no registrado. ID mensaje: " + msg.getReplyWith());
            }
        } else if(msg.getPerformativeInt() == ACLMessage.INFORM){   // INFORM recibido.
            if(msg.getContent().contains("OK")){                    // INFORM<OK>.          
                if(hashmapMensajes.containsKey(msg.getReplyWith()) ) {
                    System.out.println(this.getAid() + ": OK recibido por " + msg.getSender() + ". ID mensaje: " + msg.getReplyWith());    
                    hashmapMensajes.remove(msg.getReplyWith());
                } else {                                            
                    System.out.println(this.getAid() + ": OK recibido desde " + msg.getSender() + " no registrado. ID mensaje: " + msg.getReplyWith());
                }
            } else {                                                // INFORM<!OK>. 
                System.out.println(this.getAid() + ": Este agente no debería recibir mensajes de tipo INFORM que no sean OK. FINALIZANDO");
                fin = true;
            }
        } else {                                                    // !INFORM && !AGREE
            System.out.println(this.getAid() + ": Este agente no debería recibir mensajes que no sean de tipo AGREE o INFORM. FINALIZANDO");
            fin = true;
        }
    }
    
    /**
     * @param args the command line arguments
     * @throws javax.sound.sampled.LineUnavailableException
     */  
    public static void main(String[] args) throws LineUnavailableException {
        
        final int sampleRate = 44100;        
        final int bufferSize = 4096;
        
        PitchDetectionHandler pitchhandler = new PitchDetectionHandler() {
            //Ultimos valores.
            private double lastProbability = 0;          
            private double lastTimeStamp = 0;         
            private double lastPitch = 0;               
            private double bpm = 0;
            private double bpmBeat = 0;
            private int beat = 4;
            private int octava;
            private int nota;
            //Key Finder
            private KeyFinder keyFinder = new KeyFinder("Unknown", "Unknown");
            private ArrayList<String> noteArray = new ArrayList<>();
            @Override
            public void handlePitch(PitchDetectionResult pitchDetectionResult, AudioEvent audioEvent) {
                if(pitchDetectionResult.getPitch() != -1){
                    double timeStamp = audioEvent.getTimeStamp();
                    float pitch = pitchDetectionResult.getPitch();
                    float probability = pitchDetectionResult.getProbability();
                    double rms = audioEvent.getRMS() * 100;
                    double difPitch = (pitch - lastPitch);
                    double timeBetween = (timeStamp - lastTimeStamp);
                    if(timeStamp > 0.5 && (difPitch > 5 || difPitch < -5 || timeBetween > 0.1)){ 
                        if(rms > 0.2){
                            if(difPitch > 0.5 || difPitch < -0.5){
                                if (difPitch > 2 || difPitch < -2 || probability > lastProbability + 0.1 || (lastProbability > 0.95 && probability > lastProbability + 0.005)) {
                                    
                                    System.out.println("\nBeat detectado en: " + timeStamp + " s con frecuencia de: " + pitch +" Hz");                                
                                    System.out.println("Media cuadratica de: " + rms + " y probabilidad de: " + probability);
                                    System.out.println("Tiempo desde el anterior beat: " + timeBetween + " s");
                                    lastTimeStamp = timeStamp;
                                    lastPitch = pitch;
                                    CurrentTime_ms = timeStamp;
                                    if (NumberSamples == 0) {
                                        StartTime_ms = CurrentTime_ms;
                                    }
                                    NumberSamples++;
                                    FinishTime_ms = CurrentTime_ms;   
                                    if (NumberSamples >= 2 && (FinishTime_ms - StartTime_ms) != 0) { 
                                        bpm = (((NumberSamples -1) * 1000 * 60) / FinishTime_ms - StartTime_ms) / 1000;
                                    }                                    
                                    System.out.println("BPM Totales: " + bpm + " bpm");
                                    beat = 4;
                                    if(timeBetween != timeStamp){
                                        bpmBeat = 60/timeBetween;
                                        System.out.println("BPM Crudo: " + bpmBeat + " bpm");
                                        while(bpmBeat < MIN_TEMPO_BPM || (Math.abs((bpmBeat*2) - bpm)) < (Math.abs((bpmBeat) - bpm)) ){
                                            bpmBeat*=2;  
                                            beat/=2;
                                        }
                                        while(bpmBeat > MAX_TEMPO_BPM || (Math.abs((bpmBeat/2) - bpm)) < (Math.abs((bpmBeat) - bpm)) ){
                                            bpmBeat/=2;
                                            beat*=2;
                                        }
                                    }
                                    System.out.println("BPM Neto: " + bpmBeat + " bpm");
                                    System.out.println("Duracion del Beat: 1/" + beat);
                                    int noteMIDI = (int) Math.floor(Math.log(pitch/440.0)/Math.log(2.0) * 12 + 69);
                                    System.out.println("Nota MIDI: " + noteMIDI);
                                    if(noteMIDI < 12) {
                                        octava = 0;
                                        nota = noteMIDI;
                                    } else if(noteMIDI < 24){                                    
                                        octava = 1;
                                        nota = noteMIDI - 12;
                                    } else if(noteMIDI < 36){
                                        octava = 2;
                                        nota = noteMIDI - 24;
                                    } else if(noteMIDI < 48){
                                        octava = 3;
                                        nota = noteMIDI - 36;
                                    } else if(noteMIDI < 60){
                                        octava = 4;
                                        nota = noteMIDI - 48;
                                    } else if(noteMIDI < 72){  
                                        octava = 5;  
                                        nota = noteMIDI - 60;                                    
                                    } else if(noteMIDI < 84){
                                        octava = 6;
                                        nota = noteMIDI - 72;
                                    } else if(noteMIDI < 96){
                                        octava = 7;
                                        nota = noteMIDI - 80;
                                    } else if(noteMIDI < 108){
                                        octava = 8;
                                        nota = noteMIDI - 96;
                                    } else if(noteMIDI < 120){
                                        octava = 9;
                                        nota = noteMIDI - 108;
                                    } else {
                                        octava = 10;
                                        nota = noteMIDI - 120;
                                    }
                                    switch(nota){
                                        case 0: 
                                            System.out.println("La nota tocada es C o Do");
                                            noteArray.add("C"+octava);
                                            break;
                                        case 1:
                                            System.out.println("La nota tocada es C# o Do# o Db o Re bemol");
                                            noteArray.add("C#"+octava+"_or_Db"+octava);
                                            break;
                                        case 2:
                                            System.out.println("La nota tocada es D o Re");
                                            noteArray.add("D"+octava);
                                            break;
                                        case 3:
                                            System.out.println("La nota tocada es D# o Re# o Eb o Mi bemol");
                                            noteArray.add("D#"+octava+"_or_Eb"+octava);
                                            break;
                                        case 4: 
                                            System.out.println("La nota tocada es E o Mi");
                                            noteArray.add("E"+octava);
                                            break;
                                        case 5:
                                            System.out.println("La nota tocada es F o Fa"); 
                                            noteArray.add("F"+octava);
                                            break;
                                        case 6:
                                            System.out.println("La nota tocada es F# o Fa# o Gb o Sol bemol");
                                            noteArray.add("F#"+octava+"_or_Gb"+octava);
                                            break;
                                        case 7:
                                            System.out.println("La nota tocada es G o Sol");
                                            noteArray.add("G"+octava);
                                            break;
                                        case 8: 
                                            System.out.println("La nota tocada es G# o Sol# o Ab o La bemol");
                                            noteArray.add("G#"+octava+"_or_Ab"+octava);
                                            break;
                                        case 9:
                                            System.out.println("La nota tocada es A o La"); 
                                            noteArray.add("A"+octava);
                                            break;
                                        case 10:
                                            System.out.println("La nota tocada es A# o La# o Bb o Si bemol");
                                            noteArray.add("A#"+octava+"_or_Bb"+octava);
                                            break;
                                        case 11: 
                                            System.out.println("La nota tocada es B o Si");
                                            noteArray.add("B"+octava);
                                            break;                                      
                                    }
                                    System.out.println("Estas tocando en la octava: " + octava);
                                    if(noteArray.size() > MAX_NOTES_KEY) {                                        
                                        noteArray.remove(0);
                                    }
                                    //keyFinder.SetAccidental(noteArray, KeyFinder.getAccidentalVal());
                                    keyFinder.DetermineKey(noteArray);
                                    String keySig = keyFinder.getKEYval();
                                    System.out.println("Estas tocando en la clave: " + keySig);
                                    String parameters = JSON.listenParameters(bpm, bpmBeat, beat, nota, octava, keySig);
                                    pilaParametros.push(parameters);
                                }
                            }
                        }
                    }
                    lastProbability = probability;
                }
            }              
        };      
        
        AudioDispatcher adp = AudioDispatcherFactory.fromDefaultMicrophone(sampleRate, bufferSize, 0);
        adp.addAudioProcessor(new PitchProcessor(PitchEstimationAlgorithm.YIN, sampleRate, bufferSize, pitchhandler));
        adp.run();
    }
    
    public static void resetBPM(){
        NumberSamples = 0;
        StartTime_ms = CurrentTime_ms;
    }
}
