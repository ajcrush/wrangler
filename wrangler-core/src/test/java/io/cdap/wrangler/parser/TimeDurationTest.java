package io.cdap.wrangler.parser;

import io.cdap.wrangler.api.parser.TimeDuration;
import org.junit.Assert;
import org.junit.Test;

public class TimeDurationTest {
  @Test
  public void testTimeDurationParsing() {
    // Test basic time durations
    TimeDuration t1 = new TimeDuration("1ns");
    Assert.assertEquals(1, t1.getNanoseconds());
    Assert.assertEquals(1.0 / 1000, t1.getMicroseconds(), 0.0001);

    TimeDuration t2 = new TimeDuration("1µs");
    Assert.assertEquals(1000, t2.getNanoseconds());
    Assert.assertEquals(1.0, t2.getMicroseconds(), 0.0001);

    TimeDuration t3 = new TimeDuration("1ms");
    Assert.assertEquals(1000 * 1000, t3.getNanoseconds());
    Assert.assertEquals(1.0, t3.getMilliseconds(), 0.0001);

    TimeDuration t4 = new TimeDuration("1s");
    Assert.assertEquals(1000L * 1000 * 1000, t4.getNanoseconds());
    Assert.assertEquals(1.0, t4.getSeconds(), 0.0001);

    // Test decimal values
    TimeDuration t5 = new TimeDuration("1.5s");
    Assert.assertEquals((long)(1.5 * 1000 * 1000 * 1000), t5.getNanoseconds());
    Assert.assertEquals(1.5, t5.getSeconds(), 0.0001);

    // Test larger units
    TimeDuration t6 = new TimeDuration("1m");
    Assert.assertEquals(60L * 1000 * 1000 * 1000, t6.getNanoseconds());
    Assert.assertEquals(1.0, t6.getMinutes(), 0.0001);

    TimeDuration t7 = new TimeDuration("1h");
    Assert.assertEquals(60L * 60 * 1000 * 1000 * 1000, t7.getNanoseconds());
    Assert.assertEquals(1.0, t7.getHours(), 0.0001);

    TimeDuration t8 = new TimeDuration("1d");
    Assert.assertEquals(24L * 60 * 60 * 1000 * 1000 * 1000, t8.getNanoseconds());
    Assert.assertEquals(1.0, t8.getDays(), 0.0001);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidTimeDuration() {
    new TimeDuration("invalid");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidUnit() {
    new TimeDuration("1xs");
  }
} 