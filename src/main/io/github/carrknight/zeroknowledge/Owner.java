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
 * An interface describing that this object can own goods
 * Created by carrknight on 7/30/14.
 */
public interface Owner {

    /**
     * add this much to the list of goods
     * @param type what kind of good
     * @param amount how much was bought/produced
     */
    public void receiveOrProduce(GoodType type, float amount);

    /**
     * consume this much to the list of goods
     * @param type what kind of good
     * @param amount how much was consumed
     */
    public void consume(GoodType type, float amount);
    /**
     * check inventory levels of this good
     * @param type how much of this good is currently owned
     */
    public float hasHowMany(GoodType type);

    /**
     * set the inventory back to 0
     * @param type goodType
     */
    public void resetTo0(GoodType type);
}
