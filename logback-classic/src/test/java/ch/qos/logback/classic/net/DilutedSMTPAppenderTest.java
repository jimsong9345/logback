package ch.qos.logback.classic.net;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import javax.mail.Address;
import javax.mail.MessagingException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Layout;

public class DilutedSMTPAppenderTest {

  SMTPAppender appender;

  @Before
  public void setUp() throws Exception {
    LoggerContext lc = new LoggerContext();
    appender = new SMTPAppender();
    appender.setContext(lc);
    appender.setName("smtp");
    appender.setFrom("user@host.dom");
    appender.setLayout(buildLayout(lc));
    appender.setSMTPHost("mail2.qos.ch");
    appender.setSubject("logging report");
    appender.addTo("sebastien.nospam@qos.ch");
    appender.start();
  }

  private static Layout<LoggingEvent> buildLayout(LoggerContext lc) {
    PatternLayout layout = new PatternLayout();
    layout.setContext(lc);
    layout.setFileHeader("Some header\n");
    layout.setPattern("%-4relative [%thread] %-5level %class - %msg%n");
    layout.setFileFooter("Some footer");
    layout.start();
    return layout;
  }
  
  @After
  public void tearDown() throws Exception {
    appender = null;
  }

  @Test
  public void testStart() {
    try {
      Address[] addressArray = appender.getMessage().getFrom();
      Address address = addressArray[0];
      assertEquals("user@host.dom", address.toString());

      addressArray = null;
      address = null;

      addressArray = appender.getMessage().getAllRecipients();
      address = addressArray[0];
      assertEquals("sebastien.nospam@qos.ch", address.toString());

      assertEquals("logging report", appender.getSubject());

      assertTrue(appender.isStarted());

    } catch (MessagingException ex) {
      fail("Unexpected exception.");
    }
  }

  @Test
  public void testAppendNonTriggeringEvent() {
    LoggingEvent event = new LoggingEvent();
    event.setThreadName("thead name");
    event.setLevel(Level.DEBUG);
    appender.subAppend(event);
    assertEquals(1, appender.cb.length());
  }

  @Test
  public void testEntryConditionsCheck() {
    appender.checkEntryConditions();
    assertEquals(0, appender.getContext().getStatusManager().getCount());
  }

  @Test
  public void testEntryConditionsCheckNoMessage() {
    appender.setMessage(null);
    appender.checkEntryConditions();
    assertEquals(1, appender.getContext().getStatusManager().getCount());
  }

  @Test
  public void testTriggeringPolicy() {
    appender.setEvaluator(null);
    appender.checkEntryConditions();
    assertEquals(1, appender.getContext().getStatusManager().getCount());
  }
  
  @Test
  public void testEntryConditionsCheckNoLayout() {
    appender.setLayout(null);
    appender.checkEntryConditions();
    assertEquals(1, appender.getContext().getStatusManager().getCount());
  }
  
  

  
}