/**
 * Copyright (C) 2010-14 pvmanager developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.pvmanager.formula;

import static org.epics.vtype.ValueFactory.*;

import java.util.Arrays;
import static org.epics.pvmanager.formula.BaseTestForFormula.testFunction;

import org.epics.util.array.ArrayDouble;
import org.epics.util.array.ArrayInt;
import org.epics.util.array.ListDouble;
import org.epics.util.array.ListInt;
import org.epics.util.time.Timestamp;
import org.epics.vtype.Alarm;
import org.epics.vtype.AlarmSeverity;
import org.epics.vtype.Time;
import org.epics.vtype.VNumber;
import org.epics.vtype.VNumberArray;
import org.epics.vtype.VString;
import org.epics.vtype.VStringArray;
import org.epics.vtype.ValueFactory;
import org.epics.vtype.table.ListNumberProvider;
import org.epics.vtype.table.VTableFactory;
import org.junit.Test;

/**
 * @author shroffk
 * 
 */
public class ArrayFunctionSetTest extends BaseTestForFormula {

    private FormulaFunctionSet set = new ArrayFunctionSet();

    @Test
    public void arrayOfString() {
	VString[] data = { newVString("x", alarmNone(), timeNow()),
		newVString("y", alarmNone(), timeNow()),
		newVString("z", alarmNone(), timeNow()) };
	VStringArray expected = newVStringArray(Arrays.asList("x", "y", "z"),
		alarmNone(), timeNow());
	testFunction(set, "arrayOf", expected, (Object[]) data);
    }

    @Test
    public void arrayOfNumber() {
	VNumber[] data = {
		newVDouble(Double.valueOf(1), alarmNone(), timeNow(),
			displayNone()),
		newVDouble(Double.valueOf(2), alarmNone(), timeNow(),
			displayNone()),
		newVDouble(Double.valueOf(3), alarmNone(), timeNow(),
			displayNone()) };
	ListDouble expectedData = new ArrayDouble(1, 2, 3);
	VNumberArray expected = newVDoubleArray(expectedData, alarmNone(),
		timeNow(), displayNone());
	testFunction(set, "arrayOf", expected, (Object[]) data);
    }

    @Test
    public void addArrayDoubleOfNumber() {
        VNumberArray array = newVNumberArray(new ArrayDouble(1, 2, 3), alarmNone(), timeNow(), displayNone());
        VNumberArray array2 = newVNumberArray(new ArrayDouble(4, 5, 6), alarmNone(), timeNow(), displayNone());
        VNumberArray array3 = newVNumberArray(new ArrayDouble(5, 7, 9), alarmNone(), timeNow(), displayNone());
        FunctionTester.findBySignature(set, "+", VNumberArray.class, VNumberArray.class)
                .compareReturnValue(array3, array, array2)
                .compareReturnValue(null, null, array2)
                .compareReturnValue(null, array, null)
                .highestAlarmReturned()
                .latestTimeReturned();
    }

    @Test
    public void addArrayIntOfNumber() {
        VNumberArray array = newVNumberArray(new ArrayInt(1, 2, 3), alarmNone(), timeNow(), displayNone());
        VNumberArray array2 = newVNumberArray(new ArrayInt(4, 5, 6), alarmNone(), timeNow(), displayNone());
        VNumberArray array3 = newVNumberArray(new ArrayDouble(5, 7, 9), alarmNone(), timeNow(), displayNone());
        FunctionTester.findBySignature(set, "+", VNumberArray.class, VNumberArray.class)
                .compareReturnValue(array3, array, array2)
                .compareReturnValue(null, null, array2)
                .compareReturnValue(null, array, null)
                .highestAlarmReturned()
                .latestTimeReturned();
    }

    @Test
    public void subtractArrayOfNumber() {
        VNumberArray array = newVDoubleArray(new ArrayDouble(4, 5, 6), alarmNone(), timeNow(), displayNone());
        VNumberArray array2 = newVDoubleArray(new ArrayDouble(4, 3, 2), alarmNone(), timeNow(), displayNone());
        VNumberArray array3 = newVDoubleArray(new ArrayDouble(0, 2, 4), alarmNone(), timeNow(), displayNone());
        FunctionTester.findBySignature(set, "-", VNumberArray.class, VNumberArray.class)
                .compareReturnValue(array3, array, array2)
                .compareReturnValue(null, null, array2)
                .compareReturnValue(null, array, null)
                .highestAlarmReturned()
                .latestTimeReturned();
    }

    @Test
    public void multiplyArrayWithNumber() {
	testTwoArgArrayFunction(set, "*", new ArrayDouble(1, 2, 3),
		Double.valueOf(2), new ArrayDouble(2, 4, 6));
    }

    @Test
    public void divideArrayWithNumber() {
        VNumberArray array = newVDoubleArray(new ArrayDouble(1, 2, 3), alarmNone(), timeNow(), displayNone());
        VNumberArray array2 = newVDoubleArray(new ArrayDouble(2, 4, 6), alarmNone(), timeNow(), displayNone());
	VNumber number = newVNumber(2, alarmNone(), timeNow(), displayNone());
        FunctionTester.findBySignature(set, "/", VNumberArray.class, VNumber.class)
                .compareReturnValue(array, array2, number);
    }

    @Test
    public void rescaleArray() {
	ListDouble data = new ArrayDouble(1, 2, 3);
	ListDouble expectedData = new ArrayDouble(2, 3, 4);
	VNumberArray expected = newVDoubleArray(expectedData, alarmNone(),
		timeNow(), displayNone());
	testFunction(
		set,
		"rescale",
		expected,
		newVDoubleArray(data , alarmNone(), timeNow(),
			displayNone()),
		newVNumber(1.0, alarmNone(), timeNow(),
			displayNone()),
		newVNumber(1.0, alarmNone(), timeNow(),
			displayNone()));
    }
    
    @Test
    public void subArray() {
	ListDouble data = new ArrayDouble(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
	ListDouble expectedData = new ArrayDouble(2, 3, 4);
	VNumberArray expected = newVDoubleArray(expectedData, alarmNone(),
		timeNow(), displayNone());
	testFunction(set, "subArray", expected,
		newVDoubleArray(data, alarmNone(), timeNow(), displayNone()),
		newVNumber(2, alarmNone(), timeNow(), displayNone()),
		newVNumber(5, alarmNone(), timeNow(), displayNone()));
    }
    
    @Test
    public void elementAtArray1() {
        VNumberArray array = newVDoubleArray(new ArrayDouble(0, 1, 2, 3, 4, 5, 6, 7, 8, 9), alarmNone(), timeNow(), displayNone());
        Alarm alarm = newAlarm(AlarmSeverity.MINOR, "HIGH");
        Time time = newTime(Timestamp.of(16548379, 0));
        VNumberArray array2 = newVDoubleArray(new ArrayDouble(0, 1, 2, 3, 4, 5, 6, 7, 8, 9), alarm, time, displayNone());
	VNumber index = newVNumber(5, alarmNone(), timeNow(), displayNone());
	VNumber expected = newVNumber(5.0, alarmNone(),timeNow(), displayNone());
        
        FunctionTester.findBySignature(set, "elementAt", VNumberArray.class, VNumber.class)
                .compareReturnValue(expected, array, index)
                .compareReturnValue(null, array, null)
                .compareReturnValue(null, null, index)
                .compareReturnAlarm(alarmNone(), array, index)
                .compareReturnAlarm(alarm, array2, index)
                .compareReturnTime(time, array2, index);
    }
    
    @Test
    public void elementAtArray2() {
        VStringArray array = newVStringArray(Arrays.asList("A", "B", "C", "D", "E"), alarmNone(), timeNow());
        Alarm alarm = newAlarm(AlarmSeverity.MINOR, "HIGH");
        Time time = newTime(Timestamp.of(16548379, 0));
        VStringArray array2 = newVStringArray(Arrays.asList("A", "B", "C", "D", "E"), alarm, time);
	VNumber index = newVNumber(2, alarmNone(), timeNow(), displayNone());
	VString expected = newVString("C", alarmNone(),timeNow());
        
        FunctionTester.findBySignature(set, "elementAt", VStringArray.class, VNumber.class)
                .compareReturnValue(expected, array, index)
                .compareReturnValue(null, array, null)
                .compareReturnValue(null, null, index)
                .compareReturnAlarm(alarmNone(), array, index)
                .compareReturnAlarm(alarm, array2, index)
                .compareReturnTime(time, array2, index);
    }
    
    @Test
    public void arrayWithBoundaries(){
        // TODO: should test alarm, time and display
        VNumberArray array = newVDoubleArray(new ArrayDouble(1,2,3,4), alarmNone(), timeNow(), displayNone());
        ListNumberProvider generator = VTableFactory.step(-1, 0.5);
        VNumberArray expected = ValueFactory.newVNumberArray(new ArrayDouble(1,2,3,4), new ArrayInt(4),
                Arrays.asList(ValueFactory.newDisplay(new ArrayDouble(-1, -0.5, 0, 0.5, 1), "")), alarmNone(), timeNow(), displayNone());
	
	testFunction(set, "arrayWithBoundaries", expected,
		array,
		generator);
    }
    
    @Test
    public void histogramOf() {
	ListDouble data = new ArrayDouble(0, 10, 3, 3, 3.5, 4, 4.5, 3, 7, 3.1);
        ListInt expectedData = new ArrayInt(1, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                                           3, 1, 0, 0, 0, 1, 0, 0, 0, 0,
                                           1, 0, 0, 0, 0, 1, 0, 0, 0, 0,
                                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                                           1, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                                           0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                                           0, 0, 0, 0, 0, 0, 0, 0, 0, 1);
	VNumberArray array = newVDoubleArray(data, alarmNone(),
		timeNow(), displayNone());
	VNumberArray expected = newVIntArray(expectedData, alarmNone(),
		timeNow(), displayNone());
        
        FunctionTester.findByName(set, "histogramOf")
                .compareReturnValue(expected, array);
    }
   
}
