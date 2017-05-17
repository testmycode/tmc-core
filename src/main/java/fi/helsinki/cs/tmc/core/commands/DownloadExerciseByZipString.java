/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.communication.http.HttpTasks;
import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;
import java.net.URI;
import java.util.concurrent.Callable;

/**
 *
 * @author lai
 */
public class DownloadExerciseByZipString extends Command<Exercise>{
    private String zip;
    public DownloadExerciseByZipString(ProgressObserver observer,String zip) {
        super(observer);
        this.zip=zip;
    }
    
    private String ToUrlByZipAndServerPath(String zip, String serverPath) {
        return serverPath + zip;
    }
    
    private Exercise ExtractExerciseByBytes(byte[] input){
        return null; //ExerciseDownloadingCommand.java
    }
    
    @Override
    public Exercise call() throws Exception {
        Callable<byte[]> download = new HttpTasks().getForBinary(URI.create(ToUrlByZipAndServerPath(zip, ""))); //mooc.helsin
        return ExtractExerciseByBytes(download.call());
    }
    
}
