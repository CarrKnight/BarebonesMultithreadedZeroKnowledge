package io.github.carrknight.schedule;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

/**
 * This is the basis for all the objects that want to interact with the main schedule. <br>
 * The idea is agents tell the schedule they want to act at a specific phase, so that the schedule will call (and wait for the result) at the appropriate time. <br>
 * Subcomponents of the agents can register their needs by adding themselves as tasks that will be executed when  the agents is called by the schedule. <br>
 * While actions are supposed to be performed concurrently, effects are supposed to take place sequentially.
 * Created by carrknight on 7/28/14.
 */
public interface Agent {


    /**
     * called by the schedule to iniziale the agent just before the model formally starts
     * @param scheduleLink the schedule
     */
    public void start(Schedule schedule);

    /**
     * called by the schedule. Perform all the actions required for this phase. Should return ONLY when all the actions are complete
     * @param phase the time of the day
     * @param executor the executor needed to perform the actions
     */
    public void resolveActions(DAY_PHASES phase, ForkJoinPool executor);

    /**
     * tasked to resolve all the outstanding effects, supposedly done sequentially. Should return ONLY when all the effects are complete
     * @return true when all effects have been accomplished with no exception thrown
     */
    public void resolveEffects(ForkJoinPool executor);

    /**
     * this is to be called by the components of the agent to tell it a new task ought to be performed
     */
    public void registerSubTask(DAY_PHASES phase,RecursiveAction subtask);

    /**
     * this is to be called by the components of the agent to tell it a new task ought to be performed
     */
    public void registerEffect(Effect effect);

}
