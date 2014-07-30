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

public class ScheduleServerTest {


    @Test
    public void effectsAreCleared() throws Exception {


        for(int i=0; i<100;i++) {
            final boolean[] effectsHappened = new boolean[]{false, false, false};
            //create the server!
            Schedule server = new ScheduleServer();
            //register the effects!
            server.registerEffect(new Effect(0) {
                @Override
                public void run() {
                    effectsHappened[0] = true;
                }
            }, mock(Agent.class));
            server.registerEffect(new Effect(1) {
                @Override
                public void run() {
                    effectsHappened[1] = true;
                }
            }, mock(Agent.class));
            server.registerEffect(new Effect(2) {
                @Override
                public void run() {
                    effectsHappened[2] = true;
                }
            }, mock(Agent.class));

            //should run now!
            server.completeAnArbitraryPhase(DAY_PHASES.PRODUCTION);
            Assert.assertTrue(effectsHappened[0]);
            Assert.assertTrue(effectsHappened[1]);
            Assert.assertTrue(effectsHappened[2]);
            //you should have scheduled yourself once
        }
    }



    @Test
    public void actionsAreCleared() throws Exception{
        for(int i=0; i<100;i++) {

            final boolean[] actionsHappened = new boolean[]{false, false, false};
            //create the server!
            Schedule server = new ScheduleServer();
            //register the effects!
            server.registerRecurringAction(DAY_PHASES.PRODUCTION, new RecursiveAction() {
                @Override
                public void compute() {
                    actionsHappened[0] = true;
                }
            });
            server.registerRecurringAction(DAY_PHASES.TRADE, new RecursiveAction() {
                @Override
                public void compute() {
                    actionsHappened[0] = false;
                    new RecursiveAction() {

                        @Override
                        protected void compute() {
                            actionsHappened[1] = true;
                            actionsHappened[2] = true;
                        }
                    }.invoke();

                }
            });

            //should run now!
            ForkJoinPool pool = new ForkJoinPool();
            server.completeAnArbitraryPhase(DAY_PHASES.PRODUCTION); //only the first switch to true
            Assert.assertTrue(actionsHappened[0]);
            Assert.assertTrue(!actionsHappened[1]);
            Assert.assertTrue(!actionsHappened[2]);
            server.completeAnArbitraryPhase(DAY_PHASES.PLACE_ORDERS); //nothing happens
            Assert.assertTrue(actionsHappened[0]);
            Assert.assertTrue(!actionsHappened[1]);
            Assert.assertTrue(!actionsHappened[2]);
            server.completeAnArbitraryPhase(DAY_PHASES.TRADE); //flips
            Assert.assertTrue(!actionsHappened[0]);
            Assert.assertTrue(actionsHappened[1]);
            Assert.assertTrue(actionsHappened[2]);

        }

    }


    @Test
    public void actionsCreateEvents() throws Exception {
        for(int i=0; i<100;i++) {

            final boolean[] actionsHappened = new boolean[]{false, false, false};
            //create the server!
            Schedule server = new ScheduleServer();
            server.registerRecurringAction(DAY_PHASES.PRODUCTION, new RecursiveAction() {
                @Override
                public void compute() {
                    server.registerEffect(new Effect(1) {
                        @Override
                        public void run() {
                            actionsHappened[0] = true;
                            actionsHappened[1] = true;
                            actionsHappened[2] = true;
                        }
                    }, mock(Agent.class));
                }
            });

            server.completeAnArbitraryPhase(DAY_PHASES.PRODUCTION);
            Assert.assertTrue(actionsHappened[0]);
            Assert.assertTrue(actionsHappened[1]);
            Assert.assertTrue(actionsHappened[2]);
        }
    }

    @Test
    public void actionsCreateActions() throws Exception {
        for(int i=0; i<100;i++) {

            final boolean[] actionsHappened = new boolean[]{false, false, false};
            //create the server!
            Schedule server = new ScheduleServer();
            server.registerRecurringAction(DAY_PHASES.PRODUCTION, new RecursiveAction() {
                @Override
                public void compute() {
                    server.registerRecurringAction(DAY_PHASES.TRADE, new RecursiveAction() {
                        @Override
                        protected void compute() {
                            server.registerEffect(new Effect(1) {
                                @Override
                                public void run() {
                                    actionsHappened[0] = true;
                                    actionsHappened[1] = true;
                                    actionsHappened[2] = true;
                                }
                            }, mock(Agent.class));
                        }
                    });

                }
            });

            server.completeADay();
            Assert.assertTrue(actionsHappened[0]);
            Assert.assertTrue(actionsHappened[1]);
            Assert.assertTrue(actionsHappened[2]);
        }
    }


    @Test
    public void canRunA1000Days() throws Exception {
        Schedule server = new ScheduleServer();

        while(server.getDay() < 1000)
            server.completeADay();


    }
}