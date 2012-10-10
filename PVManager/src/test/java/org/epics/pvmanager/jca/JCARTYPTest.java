/**
 * Copyright (C) 2010-12 Brookhaven National Laboratory
 * All rights reserved. Use is subject to license terms.
 */
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.epics.pvmanager.jca;

import gov.aps.jca.CAStatus;
import gov.aps.jca.Channel;
import gov.aps.jca.dbr.DBR;
import gov.aps.jca.dbr.DBRType;
import gov.aps.jca.dbr.DBR_String;
import gov.aps.jca.dbr.DBR_TIME_String;
import gov.aps.jca.dbr.Severity;
import gov.aps.jca.dbr.Status;
import gov.aps.jca.event.MonitorEvent;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.epics.pvmanager.ExpressionLanguage.*;
import org.epics.pvmanager.expression.DesiredRateExpression;
import org.epics.pvmanager.expression.DesiredRateReadWriteExpression;
import org.junit.Before;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

/**
 *
 * @author carcassi
 */
public class JCARTYPTest {
    
    public JCARTYPTest() {
    }

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }
    
    @Mock Channel channel;
    @Mock JCADataSource dataSource;

    @Test
    public void payloadConvert1() {
        DBR_TIME_String value = new DBR_TIME_String(new String[]{"theValue"});
        value.setSeverity(Severity.MINOR_ALARM);
        value.setStatus(Status.LOW_ALARM);
        MonitorEvent event = new MonitorEvent(channel, value, CAStatus.NORMAL);
        JCAMessagePayload payload = new JCAMessagePayload(null, event);
        assertThat(payload.getEvent().getDBR(), sameInstance((DBR) value));
    }

    @Test
    public void payloadConvert2() {
        DBR_String value = new DBR_String(new String[]{"theValue"});
        MonitorEvent event = new MonitorEvent(channel, value, CAStatus.NORMAL);
        JCAMessagePayload payload = new JCAMessagePayload(null, event);
        assertThat(payload.getEvent().getDBR(), not(sameInstance((DBR) value)));
        assertThat(payload.getEvent().getDBR(), instanceOf(DBR_TIME_String.class));
    }

    @Test
    public void dataTypeRequired1() {
        JCAChannelHandler handler = new JCAChannelHandler("test", dataSource);
        when(dataSource.isRtypValueOnly()).thenReturn(Boolean.TRUE);
        when(channel.getFieldType()).thenReturn(DBR_String.TYPE);
        when(channel.getName()).thenReturn("test");
        DBRType type = handler.valueTypeFor(channel);
        assertThat(type, equalTo(DBR_TIME_String.TYPE));
    }

    @Test
    public void dataTypeRequired2() {
        JCAChannelHandler handler = new JCAChannelHandler("test.RTYP", dataSource);
        when(dataSource.isRtypValueOnly()).thenReturn(Boolean.TRUE);
        when(channel.getFieldType()).thenReturn(DBR_String.TYPE);
        when(channel.getName()).thenReturn("test.RTYPE");
        DBRType type = handler.valueTypeFor(channel);
        assertThat(type, equalTo(DBR_String.TYPE));
    }

    @Test
    public void dataTypeRequired3() {
        JCAChannelHandler handler = new JCAChannelHandler("test.RTYP", dataSource);
        when(dataSource.isRtypValueOnly()).thenReturn(Boolean.FALSE);
        when(channel.getFieldType()).thenReturn(DBR_String.TYPE);
        when(channel.getName()).thenReturn("test.RTYPE");
        DBRType type = handler.valueTypeFor(channel);
        assertThat(type, equalTo(DBR_TIME_String.TYPE));
    }
}
