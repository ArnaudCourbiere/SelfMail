package com.javatomic.drupal.test;

import junit.framework.TestCase;

public class TestDummy extends TestCase {
    protected int mValue1;
    protected int mValue2;

    protected void setUp() {
        mValue1 = 1;
        mValue2 = 2;
    }

    public void testAdd() {
        int result = mValue1 + mValue2;

        assertTrue(result == 3);
    }
}
