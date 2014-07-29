package io.github.carrknight.schedule;

import java.util.Collections;
import java.util.LinkedList;
import java.util.concurrent.*;

/**
 * So ideally this object is delegated in other agents to deal with the scheduling. It is a single independent thread that keeps
 * listening for new actions/events one at a time. <br>
 *     The reason I use this way to dispatch new actions and events is because it is very similar to go channels and channel-based
 *     multithreading. The idea is to port this to go at some future time.
 * Created by carrknight on 7/28/14.
 */
public class AgentServer implements Agent {

    /**
     * this thread keeps listening for new actions/effects/commands and deal with them one at a time
     */
    private final Thread dispatch;

    /**
     * here we receive and store the new tasks/messages/commands and wait for the dispatch thread to deal with them
     */
    private final BlockingQueue<Runnable> channel;

    /**
     * an array of lists of actions to take each day. Imagine these being recurring actions.
     * Notice that it isn't thread safe and that's okay because only the dispatch thread can access it
     */
    private final LinkedList<RecursiveAction>[] actions;

    /**
     * store here all the effects that have to be cleared
     */
    private final LinkedList<Effect> pendingEffects;

    /**
     * link to the schedule. It is valid only after the start is called!
     */
    private Schedule schedule;

    /**
     * whether the agent server is active
     */
    private boolean active = false;

    public AgentServer() {

        channel = new LinkedBlockingQueue<>();
        //create the thread unsafe lists
        actions = new LinkedList[DAY_PHASES.values().length];
        for(int i=0; i<actions.length; i++)
            actions[i]=new LinkedList<>();
        pendingEffects = new LinkedList<>();

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
        //notice that it doesn't start yet
    }

    /**
     *     starts the dispatch thread
     */
    @Override
    public void start(Schedule schedule) {
        active = true;
        this.schedule =schedule;
        dispatch.start();

    }


    @Override
    public void registerEffect(final Effect effect) {

        //tell the channel to tell dispatch to add the effect to the list of effects
        channel.offer(() -> {
            boolean wasEmpty = pendingEffects.isEmpty();
            pendingEffects.add(effect);
            if (wasEmpty) //if this was the first "effect", then register with the schedule
                schedule.registerAgentToResolveEffects(this);
        });
    }


    @Override
    public void registerSubTask(DAY_PHASES phase, RecursiveAction subtask) {
        //tell the channel to tell dispatch to add the recurring task to the list of effects
        channel.offer(() -> {
            boolean wasEmpty =actions[phase.ordinal()].isEmpty();
            actions[phase.ordinal()].add(subtask);
            if (wasEmpty) //if this was the first "effect", then register with the schedule
                schedule.registerAgentToPerformActions(this,phase);
        });
    }


    @Override
    public void resolveActions(DAY_PHASES phase, ForkJoinPool executor) {
        //this method will be called as part of the executor, we are going to pause it until all the tasks have been accomplished
        final Semaphore semaphore = new Semaphore(0);
        channel.offer(() -> {
            //invoke all the tasks
            for(RecursiveAction action : actions[phase.ordinal()])
                executor.invoke(action);
            //wait for them to complete
            for(RecursiveAction action : actions[phase.ordinal()])
                action.join();
            //done!
            semaphore.release();
        });
        //wait here for all the actions to finish
        try {
            semaphore.acquire();
        } catch (InterruptedException ignored) {}

    }

    @Override
    public void resolveEffects(ForkJoinPool executor) {
        final Semaphore semaphore = new Semaphore(0);
        channel.offer(new Runnable() {
            @Override
            public void run() {
                //dispatch makes a copy, clears original list and gives the executor the job of working through each job sequentially
                final LinkedList<Effect> currentEffects = new LinkedList<Effect>(pendingEffects);
                pendingEffects.clear();
                executor.execute(() -> {
                    Collections.sort(currentEffects);
                    for(Effect e : currentEffects)
                        e.run();
                    semaphore.release();
                });
               //the dispatch itself is done and can wait for more
            }
        });
        //this thread (probably a schedule thread within the executor) now waits for the tasks to be completed
        try {
            semaphore.acquire();
        } catch (InterruptedException ignored) {}


    }
}
