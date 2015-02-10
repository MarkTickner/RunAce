package com.mtickner.runningmotivator.tests;

import com.mtickner.runningmotivator.MiscHelper;

import junit.framework.TestCase;

public class MiscHelperTest extends TestCase {

    public void testConvertKilometresToMiles() {
        double result = MiscHelper.ConvertKilometresToMiles(1);

        assertEquals(0.621371192, result);
    }

}