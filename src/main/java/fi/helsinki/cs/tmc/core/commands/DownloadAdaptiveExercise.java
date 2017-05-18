/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.helsinki.cs.tmc.core.commands;

import fi.helsinki.cs.tmc.core.domain.Exercise;
import fi.helsinki.cs.tmc.core.domain.Progress;
import fi.helsinki.cs.tmc.core.domain.ProgressObserver;
import fi.helsinki.cs.tmc.core.exceptions.ExerciseDownloadFailedException;
import fi.helsinki.cs.tmc.core.exceptions.ExtractingExericeFailedException;
import fi.helsinki.cs.tmc.core.exceptions.TmcInterruptionException;
import fi.helsinki.cs.tmc.core.holders.TmcLangsHolder;
import fi.helsinki.cs.tmc.core.holders.TmcSettingsHolder;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Level;

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
        
        //extractProject(zipb, exercise, progress);

        Path exerciseDirectory = TmcSettingsHolder.get().getTmcProjectDirectory();      // menee exercise directory (mihin unzippattu)
        
        Path unzippedFolderTemp = extractToTempFolder(zipb, exercise, progress);
        // säättää kurssi nimet puratun polun perusteella
        // unzip temp pois
        cleanExtractedExerciseWithTempNames(exercise, unzippedFolderTemp, exerciseDirectory);
        
        return exercise;
    }
    
    private Path extractToTempFolder (byte[] zipb, Exercise exercise, Progress progress) 
        throws ExtractingExericeFailedException, ExerciseDownloadFailedException, TmcInterruptionException{
        logger.info("Extracting exercise");
        Path zipTemp; 
        logger.debug("Writing zip to temporary location");
        try {
            zipTemp = Files.createTempFile("tmc-exercise-", ".zip");
            Files.write(zipTemp, zipb);
            logger.debug("Successfully write temporary exercise zip to dist");
        } catch (IOException ex) {
            logger.warn("Failed to write downloaded zip to disk", ex);
            throw new ExerciseDownloadFailedException(exercise.getDownloadUrl(), ex);
        }
        checkInterrupt();
        logger.debug("Zip file successfully written");
        
        
        informObserver(progress.incrementAndGet(), "Extracting exercise from " + exercise.getDownloadUrl());
        Path unzipFolderTemp = null;
        try {
            unzipFolderTemp = Files.createTempDirectory("tmc-exercise-unzipped-"+System.nanoTime());
            try{
                TmcLangsHolder.get().extractProject(zipTemp, unzipFolderTemp);
                logger.info("Successfully extracted exercise");
            } catch (Exception ex) {
                logger.warn(
                        "Failed to extract project from "
                            + zipTemp
                            + " to "
                            + unzipFolderTemp,
                        ex);
                throw new ExtractingExericeFailedException(exercise.getDownloadUrl(), ex);
            }
        }
        catch (IOException ex) {
            logger.warn("Failed to create temporary folder tmc-exercise-unzipped");
            throw new ExtractingExericeFailedException(exercise.getDownloadUrl(), ex);
        }
        
        try {
            Files.deleteIfExists(zipTemp);
            logger.debug("Cleaned up temporary zip files");
        } catch (IOException ex) {
            logger.warn("Failed to delete temporary exercise zip from " + zipTemp, ex);
        }
        
        return unzipFolderTemp;
    }
    
    private void cleanExtractedExerciseWithTempNames(Exercise exercise, Path unzipFolderTemp, Path exerciseDirectory) {
        //Path unzipTempConameNanotime = exerciseDirectory.resolve(exercise.getCourseName()); // menee kansioon "coname " + nanotime
        //Path unzipTempExnameNanotime = unzipTempConameNanotime.resolve(exercise.getName()); // menee kansioon "exname " + nanotime
        //File unzipTempFolder = unzipTempExnameNanotime.toFile();
        //File superFolderWithNameOsaa = unzipTempFolder.listFiles()[0]; // menee ainoaan kansioon, esim. "osaa01"
        File superFolderWithNameOsaa = unzipFolderTemp.toFile().listFiles()[0]; // menee ainoaan kansioon, esim. "osaa01"
        //exercise.setCourseName(superFolderWithNameOsaa.getName());  // set course name "osaa01" //tai???
        File exerciseFolder = superFolderWithNameOsaa.listFiles()[0]; // menee ainoaan tehtävän kansioon, esim. "Osaa01_01.AdaLovelace"
        exercise.setName(exerciseFolder.getName()); // exercise.setName(exer.getName().split(".")[1]); // set exercise name "AdaLovelace" //tai??
        try {
            // siirtää purattu tiedosto exercise directoryyn
            // mv exercise_directory/"coname "+ nanotime / "exname " + nanotime / "osaa01" exercise_directocty
            logger.info("start moving temporary unzip to final location");
            Files.move(superFolderWithNameOsaa.toPath(), exerciseDirectory);
            logger.debug("successfully moved temporary unzip to final location");
        }
        catch (IOException ex) {
            logger.warn("Failed to move temporary unzip exercise to final location" + unzipFolderTemp, ex);
            // throw jtn
        }
        try {
            // cleanup temp
            // rmdir "coname " + nanotime
            //Files.deleteIfExists(unzipTempConameNanotime);
            Files.deleteIfExists(unzipFolderTemp);
            logger.debug("successfully deleted temporary unzip folder");
        }
        catch (IOException ex) {
            logger.warn("Failed to delete temporary unzip folder");
        }
    }
}
