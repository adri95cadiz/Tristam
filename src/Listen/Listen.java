/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Listen;

import Communication.*;
import Parameters.*;
import be.tarsos.dsp.AudioDispatcher;
import be.tarsos.dsp.AudioEvent;
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory;
import be.tarsos.dsp.pitch.PitchDetectionHandler;
import be.tarsos.dsp.pitch.PitchDetectionResult;
import be.tarsos.dsp.pitch.PitchProcessor;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.LineUnavailableException;

/**
 *
 * @author Adri
 */
public class Listen implements Runnable {

    private final static int MIN_TEMPO_BPM = 60;
    private final static int MAX_TEMPO_BPM = 250;
    private final static int MAX_NOTES_KEY = 15;
    // Para el bpm total.
    private static double StartTime_ms = 0;
    private static double FinishTime_ms = 0;
    private static double CurrentTime_ms = 0;
    private static int NumberSamples = 0;
    private int NumParameter = 0;
    private static ArrayList<String> colaParametros = new ArrayList<String>() {};   
    
    
    @Override
    public void run() {        
        try {
            final int sampleRate = 44100;
            final int bufferSize = 4096;
            
            PitchDetectionHandler pitchhandler = new PitchDetectionHandler() {
                //Ultimos valores.
                private double lastProbability = 0;
                private double lastTimeStamp = 0;
                private double lastPitch = 0;
                private Parameters parameters = new Parameters();
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
                                            parameters.setBpm((((NumberSamples -1) * 1000 * 60) / FinishTime_ms - StartTime_ms) / 1000);
                                        }
                                        System.out.println("BPM Totales: " + parameters.getBpm() + " bpm");
                                        parameters.setBeat(4);
                                        if(timeBetween != timeStamp){
                                            parameters.setBpmAdapted(60/timeBetween);
                                            System.out.println("BPM Crudo: " + parameters.getBpmAdapted() + " bpm");
                                            while(parameters.getBpmAdapted() < MIN_TEMPO_BPM || (Math.abs((parameters.getBpmAdapted()*2) - parameters.getBpm())) < (Math.abs((parameters.getBpmAdapted()) - parameters.getBpm())) ){
                                                parameters.setBpmAdapted(parameters.getBpmAdapted()*2);
                                                parameters.setBeat(parameters.getBeat()/2);
                                            }
                                            while(parameters.getBpmAdapted() > MAX_TEMPO_BPM || (Math.abs((parameters.getBpmAdapted()/2) - parameters.getBpm())) < (Math.abs((parameters.getBpmAdapted()) - parameters.getBpm())) ){
                                                parameters.setBpmAdapted(parameters.getBpmAdapted()/2);
                                                parameters.setBeat(parameters.getBeat()*2);
                                            }
                                        }
                                        System.out.println("BPM Neto: " + parameters.getBpmAdapted() + " bpm");
                                        System.out.println("Duracion del Beat: 1/" + parameters.getBeat());
                                        int noteMIDI = (int) Math.floor(Math.log(pitch/440.0)/Math.log(2.0) * 12 + 69);
                                        System.out.println("Nota MIDI: " + noteMIDI);
                                        if(noteMIDI < 12) {
                                            parameters.setOctave(0);
                                            parameters.setNote(noteMIDI);
                                        } else if(noteMIDI < 24){
                                            parameters.setOctave(1);
                                            parameters.setNote(noteMIDI - 12);
                                        } else if(noteMIDI < 36){
                                            parameters.setOctave(2);
                                            parameters.setNote(noteMIDI - 24);
                                        } else if(noteMIDI < 48){
                                            parameters.setOctave(3);
                                            parameters.setNote(noteMIDI - 36);
                                        } else if(noteMIDI < 60){
                                            parameters.setOctave(4);
                                            parameters.setNote(noteMIDI - 48);
                                        } else if(noteMIDI < 72){
                                            parameters.setOctave(5);
                                            parameters.setNote(noteMIDI - 60);
                                        } else if(noteMIDI < 84){
                                            parameters.setOctave(6);
                                            parameters.setNote(noteMIDI - 72);
                                        } else if(noteMIDI < 96){
                                            parameters.setOctave(7);
                                            parameters.setNote(noteMIDI - 80);
                                        } else if(noteMIDI < 108){
                                            parameters.setOctave(8);
                                            parameters.setNote(noteMIDI - 96);
                                        } else if(noteMIDI < 120){
                                            parameters.setOctave(9);
                                            parameters.setNote(noteMIDI - 108);
                                        } else {
                                            parameters.setOctave(10);
                                            parameters.setNote(noteMIDI - 120);
                                        }
                                        switch(parameters.getNote()){
                                            case 0:
                                                System.out.println("La nota tocada es C o Do");
                                                noteArray.add("C"+parameters.getOctave());
                                                break;
                                            case 1:
                                                System.out.println("La nota tocada es C# o Do# o Db o Re bemol");
                                                noteArray.add("C#"+parameters.getOctave()+"_or_Db"+parameters.getOctave());
                                                break;
                                            case 2:
                                                System.out.println("La nota tocada es D o Re");
                                                noteArray.add("D"+parameters.getOctave());
                                                break;
                                            case 3:
                                                System.out.println("La nota tocada es D# o Re# o Eb o Mi bemol");
                                                noteArray.add("D#"+parameters.getOctave()+"_or_Eb"+parameters.getOctave());
                                                break;
                                            case 4:
                                                System.out.println("La nota tocada es E o Mi");
                                                noteArray.add("E"+parameters.getOctave());
                                                break;
                                            case 5:
                                                System.out.println("La nota tocada es F o Fa");
                                                noteArray.add("F"+parameters.getOctave());
                                                break;
                                            case 6:
                                                System.out.println("La nota tocada es F# o Fa# o Gb o Sol bemol");
                                                noteArray.add("F#"+parameters.getOctave()+"_or_Gb"+parameters.getOctave());
                                                break;
                                            case 7:
                                                System.out.println("La nota tocada es G o Sol");
                                                noteArray.add("G"+parameters.getOctave());
                                                break;
                                            case 8:
                                                System.out.println("La nota tocada es G# o Sol# o Ab o La bemol");
                                                noteArray.add("G#"+parameters.getOctave()+"_or_Ab"+parameters.getOctave());
                                                break;
                                            case 9:
                                                System.out.println("La nota tocada es A o La");
                                                noteArray.add("A"+parameters.getOctave());
                                                break;
                                            case 10:
                                                System.out.println("La nota tocada es A# o La# o Bb o Si bemol");
                                                noteArray.add("A#"+parameters.getOctave()+"_or_Bb"+parameters.getOctave());
                                                break;
                                            case 11:
                                                System.out.println("La nota tocada es B o Si");
                                                noteArray.add("B"+parameters.getOctave());
                                                break;
                                        }
                                        System.out.println("Estas tocando en la octava: " + parameters.getOctave());
                                        if(noteArray.size() > MAX_NOTES_KEY) {
                                            noteArray.remove(0);
                                        }
                                        //keyFinder.SetAccidental(noteArray, KeyFinder.getAccidentalVal());
                                        keyFinder.DetermineKey(noteArray);
                                        String keySig = keyFinder.getKEYval();
                                        System.out.println("Estas tocando en la clave: " + keySig);
                                        parameters.setNum(NumParameter);
                                        String parametros = JSON.listenParameters(parameters);
                                        colaParametros.add(parametros);
                                        NumParameter++;
                                    }
                                }
                            }
                        }
                        lastProbability = probability;
                    }
                }
            };
            
            AudioDispatcher adp = AudioDispatcherFactory.fromDefaultMicrophone(sampleRate, bufferSize, 0);
            adp.addAudioProcessor(new PitchProcessor(PitchProcessor.PitchEstimationAlgorithm.YIN, sampleRate, bufferSize, pitchhandler));
            adp.run();
        } catch (LineUnavailableException ex) {
            Logger.getLogger(Listen.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public ArrayList<String> getColaParametros(){
        return colaParametros;
    }
    
    public static void resetBPM(){
        NumberSamples = 0;
        StartTime_ms = CurrentTime_ms;
    }   
    
}
