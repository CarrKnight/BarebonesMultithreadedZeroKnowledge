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

package io.github.carrknight.zeroknowledge;

/**
 * A simple facade for an array. This is very thread-unsafe, which means that these methods should only be dealt with
 * as an effect and only by the agent owning the inventory. <br>
 *     Negatives are allowed, no problems
 * Created by carrknight on 7/29/14.
 */
public class Inventory implements Owner {

    private final float[] goodsOwned;


    public Inventory() {
        goodsOwned = new float[GoodType.values().length];
    }


    public void receiveOrProduce(GoodType type, float amount){
        goodsOwned[type.ordinal()]+=amount;
    }

    public void consume(GoodType type, float amount){
        goodsOwned[type.ordinal()]-=amount;
    }

    public float hasHowMany(GoodType type){
        return goodsOwned[type.ordinal()];
    }

    public void resetTo0(GoodType type){
        goodsOwned[type.ordinal()]=0;
    }
}
