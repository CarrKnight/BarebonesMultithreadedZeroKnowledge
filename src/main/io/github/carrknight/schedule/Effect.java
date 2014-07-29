package io.github.carrknight.schedule;

/**
 * A simple Runnable with an additional number representing "priority" which is a way
 * to define which effect happens first
 * Created by carrknight on 7/28/14.
 */
public abstract class Effect implements Runnable,Comparable<Effect>{

    /**
     * a simple way to choose which effect goes first is by having a fixed random priority assigned at creation
     */
    private final int priority;


    protected Effect(int priority) {
        this.priority = priority;
    }


    @Override
    public int compareTo(Effect o) {
        return Integer.compare(this.priority,o.priority);
    }
}
