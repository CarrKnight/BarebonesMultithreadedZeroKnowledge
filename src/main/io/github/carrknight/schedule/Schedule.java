package io.github.carrknight.schedule;

import java.util.concurrent.RecursiveAction;

/**
 * A simple interface defining what a schedule does. Simply receive effects and actions and then resolving them!
 * Created by carrknight on 7/28/14.
 */
public interface Schedule {


    /**
     * Add a new "effect" to be resolved as soon as possible
     * @param e the effect to resolve
     * @param a the agent whose effect we are dealing with
     */
    public void registerEffect(Effect e,Agent a);

    /**
     * Add a new recurring action to  be resolved at the same phase everyday
     * @param phase the phase at which point the action should be resolved
     * @param action the action to resolve
     */
    public void registerRecurringAction(DAY_PHASES phase, RecursiveAction action);

    /**
     * Go through all the phases of a day and perform all its actions and effects. This method waits until the tasks are complete
     */
    public void completeADay();

    /**
     * Skip arbitrarily to this phase and complete it. Useful mostly for testing and debugging.
     * @param phase phase to complete
     */
    public void completeAnArbitraryPhase(DAY_PHASES phase) throws InterruptedException;

    /**
     * how many days have passed?
     * @return day
     */
    public int getDay();

}
