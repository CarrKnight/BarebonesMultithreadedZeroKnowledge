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

import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

import static org.mockito.Mockito.mock;

public class AgentServerTest {



    //effects are cleared test


    @Test
    public void effectsAreCleared() throws Exception {


        final boolean[] effectsHappened = new boolean[]{false,false,false};
        //create the server!
        AgentServer server = new AgentServer();
        server.start(mock(Schedule.class));
        //register the effects!
        server.registerEffect(new Effect(0) {
            @Override
            public void run() {
                effectsHappened[0] = true;
            }
        });
        server.registerEffect(new Effect(1) {
            @Override
            public void run() {
                effectsHappened[1] = true;
            }
        });
        server.registerEffect(new Effect(2) {
            @Override
            public void run() {
                effectsHappened[2] = true;
            }
        });

        //should run now!
        ForkJoinPool pool = new ForkJoinPool();
        server.resolveEffects(pool);
        Assert.assertTrue(effectsHappened[0]);
        Assert.assertTrue(effectsHappened[1]);
        Assert.assertTrue(effectsHappened[2]);
    }



    @Test
    public void actionsAreCleared() throws Exception{
        final boolean[] actionsHappened = new boolean[]{false,false,false};
        //create the server!
        AgentServer server = new AgentServer();
        server.start(mock(Schedule.class));
        //register the effects!
        server.registerSubTask(DAY_PHASES.PRODUCTION, new RecursiveAction() {
            @Override
            public void compute() {
                actionsHappened[0] = true;
            }
        });
        server.registerSubTask(DAY_PHASES.TRADE, new RecursiveAction() {
            @Override
            public void compute() {
                actionsHappened[0] = false;
                new RecursiveAction() {

                    @Override
                    protected void compute() {
                        actionsHappened[1]=true;
                        actionsHappened[2]=true;
                    }
                }.invoke();

            }
        });

        //should run now!
        ForkJoinPool pool = new ForkJoinPool();
        server.resolveActions(DAY_PHASES.PRODUCTION,pool); //only the first switch to true
        Assert.assertTrue(actionsHappened[0]);
        Assert.assertTrue(!actionsHappened[1]);
        Assert.assertTrue(!actionsHappened[2]);
        server.resolveActions(DAY_PHASES.PLACE_ORDERS, pool); //nothing happens
        Assert.assertTrue(actionsHappened[0]);
        Assert.assertTrue(!actionsHappened[1]);
        Assert.assertTrue(!actionsHappened[2]);
        server.resolveActions(DAY_PHASES.TRADE, pool); //flips
        Assert.assertTrue(!actionsHappened[0]);
        Assert.assertTrue(actionsHappened[1]);
        Assert.assertTrue(actionsHappened[2]);


    }
}