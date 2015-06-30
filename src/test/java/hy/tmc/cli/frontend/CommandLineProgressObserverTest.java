/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hy.tmc.cli.frontend;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class CommandLineProgressObserverTest {
    
    
    
    private CommandLineProgressObserver observer;
    private DataOutputStream stream;
    
    @Before
    public void setUp() throws IOException {
        stream = mock(DataOutputStream.class);
        observer = new CommandLineProgressObserver(stream);
    }
    

    /**
     * Test of progress method, of class CommandLineProgressObserver.
     */
    @Test
    public void testProgress() throws IOException {
        observer.progress(88.7, "hello world");
        verify(stream).write(eq("hello world (88.7% done)\n".getBytes()));
    }
    
    @Test
    public void testProgressManyTimes() throws IOException {
        observer.progress(88.7, "hello world");
        verify(stream).write(eq("hello world (88.7% done)\n".getBytes()));
        observer.progress(57.33138958, "ducktales");
        verify(stream).write(eq("ducktales (57.3% done)\n".getBytes()));
        observer.progress(1.023408, "hello world");
        verify(stream).write(eq("hello world (1.0% done)\n".getBytes()));
    }
    
}
