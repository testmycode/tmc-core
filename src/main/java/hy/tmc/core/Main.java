
package hy.tmc.core;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import fi.helsinki.cs.tmc.langs.domain.RunResult;
import hy.tmc.core.exceptions.TmcCoreException;
import java.io.IOException;

public class Main {
    
    public static void main(String[] args) throws IOException, TmcCoreException{
        TmcCore c = new TmcCore();
        ClientTmcSettings settings = new ClientTmcSettings();
        settings.setServerAddress("https://tmc.mooc.fi/staging");
        settings.setPassword("1234");
        settings.setUsername("test");
        ListenableFuture<RunResult> result = c.test("/home/xtoxtox/NetBeansProjects/kesa2015-wepa/WK1/W1E03.Papukaija", settings);
        Futures.addCallback(result, new FutureCallback<RunResult>(){

            @Override
            public void onSuccess(RunResult v) {
                System.out.println("Result: " + v.testResults.get(0).toString());
            }

            @Override
            public void onFailure(Throwable thrwbl) {
                thrwbl.printStackTrace();
            }
            
        });
    }
    
}
