package io.github.carrknight.schedule;

/**
 * This is the basis for all the objects that want to interact with the main schedule. <br>
 * The idea is agents tell the schedule they want to act at a specific phase, so that the schedule will call (and wait for the result) at the appropriate time. <br>
 * Subcomponents of the agents can register their needs by adding themselves as tasks that will be executed when  the agents is called by the schedule. <br>
 * While actions are supposed to be performed concurrently, effects are supposed to take place sequentially.
 * Created by carrknight on 7/28/14.
 */
public interface Agent {


    /**
     * called by the schedule to initialize the agent just before the model formally starts
     * @param schedule the schedule
     */
    public void start(Schedule schedule);


    /**
     * called by the schedule to turn off the agent
     */
    public void turnOff();


}
