/*
 * The MIT License (MIT)
 *
 * Copyright (c)  2014 Ernesto Carrella
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package io.github.carrknight.schedule;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;

import java.util.*;
import java.util.concurrent.*;

/**
 * A simple implementation of an independently threaded schedule. The independent dispatch thread deals with registering
 * effects and actions one at a time to avoid using locks. It is, in a way, a crude reimplementation of channel-based
 * multithreading that is at the basis of go. A language I hope to port this code to at some point.
 * Created by carrknight on 7/30/14.
 */
public class ScheduleServer implements Schedule {

    /**
     * this thread keeps listening for new actions/effects/commands and deal with them one at a time
     */
    private final Thread dispatch;

    /**
     * here we receive and store the new tasks/messages/commands and wait for the dispatch thread to deal with them
     */
    private final BlockingQueue<Runnable> channel;

    /**
     * a map of all recursive actions to take
     */
    private final Multimap<DAY_PHASES,RecursiveAction> actions;

    /**
     * a map of all the effects each agent need to resolve
     */
    private final ListMultimap<Agent,Effect> pendingEffects;

    private int day = 0;

    private boolean active = true;

    /**
     * the workhorse of the schedule. Runs all the actions
     */
    private final ForkJoinPool threadPool;



    public ScheduleServer() {

        //create the channel holding the commands
        channel = new LinkedBlockingQueue<>();

        //create the thread unsafe lists
        actions = Multimaps.newListMultimap(new EnumMap<>(DAY_PHASES.class),
                LinkedList::new
        );

        //create map of effects
        pendingEffects = Multimaps.newListMultimap(new HashMap<>(),
                LinkedList::new
        );


        //now create and start the dispatch thread
        //create a simple thread
        dispatch = new Thread(() -> {
            //very simple runnable: keep grabbing commands from the channel and execute them
            try {
                while(active)
                {
                    channel.take().run();
                }
            } catch (InterruptedException e) {/*end of the game*/}
        });
        dispatch.setDaemon(true); //doesn't stop the program from quitting
        dispatch.start();

        //create the workhorse
        threadPool = new ForkJoinPool();

    }

    /**
     * Add a new "effect" to be resolved as soon as possible
     *
     * @param e the effect to resolve
     * @param a the agent whose effect we are dealing with
     */
    @Override
    public void registerEffect(Effect e, Agent a) {

        //tell the dispatch to add it to the masterlist
        channel.offer(() -> pendingEffects.put(a,e));

    }

    /**
     * Add a new recurring action to  be resolved at the same phase everyday
     *
     * @param phase  the phase at which point the action should be resolved
     * @param action the action to resolve
     */
    @Override
    public void registerRecurringAction(DAY_PHASES phase, RecursiveAction action) {
        //tell the dispatch to add it to the correct map
        channel.offer(() -> actions.put(phase,action) );

    }

    /**
     * Go through all the phases of a day and perform all its actions and effects
     */
    @Override
    public void completeADay() {
        //call each phase completion. This cannot be run by the dispatch thread obviously
        try {
            for (DAY_PHASES phase : DAY_PHASES.values())
                completeAnArbitraryPhase(phase);
        }
        catch (InterruptedException e) {
            e.printStackTrace();
            System.err.println("interrupted");
            System.exit(-1);
        }
        day++;
    }


    private void completeAllActions(DAY_PHASES phase){

        //make a copy of the actions needed so that we don't have to block the dispatch thread
        final Collection<RecursiveAction> todo =  actions.get(phase);
        if(todo.isEmpty())
            return;

        final RecursiveAction runPhase = new RecursiveAction() {
            //this recursive action just calls all the other recursive actions!
            @Override
            protected void compute() {
                invokeAll(todo);
            }
        };
        threadPool.invoke(runPhase); //invoke should join!




    }

    private void completeAllEffects() {

        if(pendingEffects.isEmpty())
            return;

        Collection<ForkJoinTask> receipts = new LinkedList<>();

        //submit all actions. For each agent they happen in sequence
        for(Map.Entry<Agent,Collection<Effect>> effects : pendingEffects.asMap().entrySet() )
        {
            List<Effect> todo =(List)effects.getValue(); //the cast is always correct because it's a MultiMapList
            final ForkJoinTask<?> receipt = threadPool.submit(() -> {
                Collections.sort(todo);
                for (Effect e : todo)
                    e.run();
            });
            receipts.add(receipt);
        }

        //now wait for all of them to complete
        try {
            for (ForkJoinTask receipt : receipts)
                receipt.get();
            pendingEffects.clear();

        }
        catch (Exception e){
            e.printStackTrace();
            System.err.println("interrupted");
            System.exit(-1);
        }
        //done



    }

    /**
     * Skip arbitrarily to this phase and complete it. Useful mostly for testing and debugging.
     * The thread stops until it's done
     *
     * @param phase phase to complete
     */
    @Override
    public void completeAnArbitraryPhase(DAY_PHASES phase) throws InterruptedException {
        //blocking till completion means this is not doable by dispatch thread


        final Semaphore waitForCompletion = new Semaphore(0);
        //two steps: do actions then do effects

        channel.offer(() -> {
            completeAllActions(phase); //notice that the dispatch thread will not update while it's completing actions
            waitForCompletion.release();
        });
        //wait for completion before telling it to do effects (so that if effects are created by the actions then they are recorded)
        waitForCompletion.acquire();
        waitForCompletion.drainPermits(); // no more permits
        //now do effects
        channel.offer(() -> {
            completeAllEffects();
            waitForCompletion.release();
        });
        //wait again
        waitForCompletion.acquire();
        //done!
    }

    /**
     * how many days have passed?
     *
     * @return day
     */
    @Override
    public int getDay() {
        return day;
    }
}
