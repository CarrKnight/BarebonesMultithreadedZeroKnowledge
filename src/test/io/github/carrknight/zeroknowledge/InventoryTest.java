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

import org.junit.Assert;
import org.junit.Test;

public class InventoryTest {


    @Test
    public void iKnowHowToCount() throws Exception {

        Inventory inventory = new Inventory();

        inventory.consume(GoodType.CASH,100.5f);
        inventory.receiveOrProduce(GoodType.CASH, 10);
        inventory.receiveOrProduce(GoodType.PEOPLE, 10);
        Assert.assertEquals(-90.5,inventory.hasHowMany(GoodType.CASH),.0001);
        Assert.assertEquals(10,inventory.hasHowMany(GoodType.PEOPLE),.0001);
        inventory.resetTo0(GoodType.PEOPLE);
        Assert.assertEquals(0,inventory.hasHowMany(GoodType.PEOPLE),.0001);

    }
}