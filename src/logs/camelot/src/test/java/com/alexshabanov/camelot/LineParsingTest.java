package com.alexshabanov.camelot;

import com.alexshabanov.camelot.camel.LogMessageProcessor;
import com.alexshabanov.camelot.common.Constants;
import com.alexshabanov.camelot.model.LogMessage;
import com.alexshabanov.camelot.model.Severity;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class LineParsingTest {

  private static final String MSG1 = "2015-07-24 23:21:16,942 INFO learn.LogProducerMain " +
          "oid=aJ0JLwgnBlw7+tbZ, rid=8tYCTFqDZfXJEzgD " +
          "[learn.LogProducerMain.main()] Application started with args=[]";

  private static final String MSG2 = "2015-07-24 23:22:20,748 WARN learn.LogProducerMain  " +
          "[learn.LogProducerMain.main()] Operation timed out";

  private static final String MSG3 = "2015-07-25 00:03:08,356 ERROR learn.LogProducerMain " +
          "rid=KhnHxNK/BbLbaiH4 " +
          "[learn.LogProducerMain.main()] Disk full";

  private static final String MSG4 = "2015-07-25 00:03:08,356 ERROR learn.LogProducerMain " +
          "rid=1, oid=2, lc=3 " +
          "[learn.LogProducerMain.main()] Disk full";

  private static final String MSG5 = "2015-07-24 23:22:20,748 INFO learn.LogProducerMain " +
      "rid=KhnHxNK/BbLbaiH4 " +
      "[learn.LogProducerMain.main()] @metric tDelta=545, op=UserService.getUserById";

  private final LogMessageProcessor processor = new LogMessageProcessor();

  @Test
  public void shouldMatchMsg1() {
    final LogMessage logMessage = processor.parse(MSG1);
    assertFalse(logMessage.isNull());
    assertEquals(Severity.INFO, logMessage.getSeverity());
    assertEquals(MSG1, logMessage.getLogEntry());
    assertEquals(1437780076942L, logMessage.getUnixTime());
    assertTrue(logMessage.getStackTrace().isEmpty());
    assertEquals(2, logMessage.getAttributes().size());
    assertEquals("aJ0JLwgnBlw7+tbZ", logMessage.getAttributes().get(Constants.ORIGINATING_REQUEST_ID));
    assertEquals("8tYCTFqDZfXJEzgD", logMessage.getAttributes().get(Constants.REQUEST_ID));
  }

  @Test
  public void shouldMatchMsg2() {
    final LogMessage logMessage = processor.parse(MSG2);
    assertFalse(logMessage.isNull());
    assertEquals(Severity.WARN, logMessage.getSeverity());
    assertEquals(MSG2, logMessage.getLogEntry());
    assertTrue(logMessage.getAttributes().isEmpty());
  }

  @Test
  public void shouldMatchMsg3() {
    final LogMessage logMessage = processor.parse(MSG3);
    assertFalse(logMessage.isNull());
    assertEquals(Severity.ERROR, logMessage.getSeverity());
    assertEquals(MSG3, logMessage.getLogEntry());
    assertEquals(null, logMessage.getAttributes().get(Constants.ORIGINATING_REQUEST_ID));
    assertEquals("KhnHxNK/BbLbaiH4", logMessage.getAttributes().get(Constants.REQUEST_ID));
  }

  @Test
  public void shouldMatchMsg4() {
    final LogMessage logMessage = processor.parse(MSG4);
    assertFalse(logMessage.isNull());
    assertEquals(Severity.ERROR, logMessage.getSeverity());
    assertEquals(MSG4, logMessage.getLogEntry());
    assertEquals("1", logMessage.getAttributes().get(Constants.REQUEST_ID));
    assertEquals("2", logMessage.getAttributes().get(Constants.ORIGINATING_REQUEST_ID));
    assertEquals("3", logMessage.getAttributes().get("lc"));
  }

  @Test
  public void shouldMatchMsg5() {
    final LogMessage logMessage = processor.parse(MSG5);
    assertFalse(logMessage.isNull());
    assertEquals(Severity.INFO, logMessage.getSeverity());
    assertEquals(MSG5, logMessage.getLogEntry());

    assertEquals("UserService.getUserById", logMessage.getAttributes().get(Constants.OPERATION));
    assertEquals("545", logMessage.getAttributes().get(Constants.TIME_DELTA));
    assertEquals("KhnHxNK/BbLbaiH4", logMessage.getAttributes().get(Constants.REQUEST_ID));
  }
}
