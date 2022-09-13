package com.rohit.metrics.counters;

import com.codahale.metrics.Counter;

/**
 * class SingleValueCounter
 *
 * Implements a JMX counter interface to simply keep track of a current value.
 *
 * The set( long ) interface takes a number, and simply sets it.  It then returns
 *     that value on the getCount().  To reset, simply call with a value of 0.
 *
 *
 */

public class SingleValueCounter extends Counter {
    private volatile long currentValue;

    public SingleValueCounter() {
        super();
        currentValue = 0;
    }

    public void set( long value ) { currentValue = value; }

    @Override
    public long getCount() { return currentValue; }
}
