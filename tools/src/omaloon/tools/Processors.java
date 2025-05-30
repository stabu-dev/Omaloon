package omaloon.tools;

import arc.util.*;

import omaloon.tools.proc.*;

import java.util.concurrent.*;

/**
 * Static class containing all processors. Call {@link #process()} to initiate asset processing.
 * @author GlennFolker
 */
public final class Processors{
    private static final Processor[] processes = {
            new OutlineRegionProcessor(),
            new UnitProcessor(),
            new StatusEffectProcessor()
    };

    private Processors(){}

    public static void process(){
        Log.info("Starting asset processing...");
        for(var process : processes){
            String processName = process.getClass().getSimpleName();
            Log.info("Running processor: @", processName);
            Time.mark();

            ExecutorService exec = Executors.newCachedThreadPool();

            try {
                process.process(exec);
                Threads.await(exec);

                process.finish();
                Log.info("@ executed successfully in @ms", processName, Time.elapsed());
            } catch (Exception e) {
                Log.err("Processor @ failed.", processName);
                Log.err(e);
                Threads.await(exec);
            }
        }
        Log.info("Asset processing finished.");
    }
}