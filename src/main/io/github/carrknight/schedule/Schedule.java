package io.github.carrknight.schedule;

/**
 * Created by carrknight on 7/28/14.
 */
public interface Schedule {



    public void registerAgentToResolveEffects(Agent a);

    public void registerAgentToPerformActions(Agent a, DAY_PHASES phases);
}
