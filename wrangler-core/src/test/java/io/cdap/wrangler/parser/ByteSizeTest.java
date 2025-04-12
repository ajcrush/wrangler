package io.cdap.wrangler.parser;

import io.cdap.wrangler.api.parser.ByteSize;
import org.junit.Assert;
import org.junit.Test;

public class ByteSizeTest {
  @Test
  public void testByteSizeParsing() {
    // Test basic byte sizes
    ByteSize b1 = new ByteSize("1B");
    Assert.assertEquals(1, b1.getBytes());
    Assert.assertEquals(1.0 / 1024, b1.getKB(), 0.0001);

    ByteSize b2 = new ByteSize("1KB");
    Assert.assertEquals(1024, b2.getBytes());
    Assert.assertEquals(1.0, b2.getKB(), 0.0001);

    ByteSize b3 = new ByteSize("1MB");
    Assert.assertEquals(1024 * 1024, b3.getBytes());
    Assert.assertEquals(1.0, b3.getMB(), 0.0001);

    // Test decimal values
    ByteSize b4 = new ByteSize("1.5MB");
    Assert.assertEquals((long)(1.5 * 1024 * 1024), b4.getBytes());
    Assert.assertEquals(1.5, b4.getMB(), 0.0001);

    // Test case insensitivity
    ByteSize b5 = new ByteSize("1kb");
    Assert.assertEquals(1024, b5.getBytes());
    Assert.assertEquals(1.0, b5.getKB(), 0.0001);

    // Test large values
    ByteSize b6 = new ByteSize("1GB");
    Assert.assertEquals(1024L * 1024 * 1024, b6.getBytes());
    Assert.assertEquals(1.0, b6.getGB(), 0.0001);

    ByteSize b7 = new ByteSize("1TB");
    Assert.assertEquals(1024L * 1024 * 1024 * 1024, b7.getBytes());
    Assert.assertEquals(1.0, b7.getTB(), 0.0001);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidByteSize() {
    new ByteSize("invalid");
  }

  @Test(expected = IllegalArgumentException.class)
  public void testInvalidUnit() {
    new ByteSize("1XB");
  }
} 