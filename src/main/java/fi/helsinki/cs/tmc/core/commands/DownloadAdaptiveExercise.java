/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.domain.Progress;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author sakuolin
 */
public class DownloadAdaptiveExercise extends ExerciseDownloadingCommand<Exercise> {
    
    private static final Logger logger = LoggerFactory.getLogger(SendFeedback.class);

    public DownloadAdaptiveExercise(ProgressObserver observer) {
        super(observer);
    }

    @Override
    public Exercise call() throws Exception {
        Progress progress = new Progress(3);
        logger.info("Checking adaptive exercises availability");
        //informObserver
        Exercise exercise = 
            tmcServerCommunicationTaskFactory.getAdaptiveExercise().call();

        //long nanotime = System.nanoTime();          
        //exercise.setName("exname " + nanotime);     // set temp exercise name
        //exercise.setCourseName("coname " + nanotime);   // set temp course name
        //Tallennuspolku riippuu edellämainituista nimistä (TMCroot)
        
        byte[] zipb = tmcServerCommunicationTaskFactory.getDownloadingExerciseZipTask(exercise).call();
        //checkInterrupt(); // jos tarvi???
        
        extractProject(zipb, exercise, progress);

        // säättää kurssi nimet puratun polun perusteella
        //Path exerciseDirectory = TmcSettingsHolder.get().getTmcProjectDirectory();      // menee exercise directory (mihin unzippattu)
        //Path unzipTempConameNanotime = exerciseDirectory.resolve(exercise.getCourseName()); // menee kansioon "coname " + nanotime
        //Path unzipTempExnameNanotime = unzipTempConameNanotime.resolve(exercise.getName()); // menee kansioon "exname " + nanotime
        //File unzipTempFolder = unzipTempExnameNanotime.toFile();
        //File superFolderWithNameOsaa = unzipTempFolder.listFiles()[0]; // menee ainoaan kansioon, esim. "osaa01"
        //exercise.setCourseName(superFolderWithNameOsaa.getName());  // set course name "osaa01" //tai???
        //File exerciseFolder = superFolderWithNameOsaa.listFiles()[0]; // menee ainoaan tehtävän kansioon, esim. "Osaa01_01.AdaLovelace"
        //exercise.setName(exerciseFolder.getName()); // exercise.setName(exer.getName().split(".")[1]); // set exercise name "AdaLovelace" //tai??
        
        // siirtää purattu tiedosto exercise directoryyn
        // mv exercise_directory/"coname "+ nanotime / "exname " + nanotime / "osaa01" exercise_directocty 
        //Files.move(superFolderWithNameOsaa.toPath(), exerciseDirectory);
        // cleanup temp
        // rmdir "coname " + nanotime
        //Files.deleteIfExists(unzipTempConameNanotime);    
        
        return exercise;
    }
}
