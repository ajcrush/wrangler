/*
 * Copyright © 2024 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package io.cdap.wrangler.executor;

import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.api.TestingRig;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class AggregateStatsTest {
  @Test
  public void testBasicAggregation() throws Exception {
    // Create sample data with various sizes and response times
    List<Row> rows = Arrays.asList(
      createRow("1MB", "100ms"),
      createRow("2.5MB", "200ms"),
      createRow("500KB", "50ms"),
      createRow("3MB", "300ms"),
      createRow("1.5MB", "150ms")
    );

    // Define the recipe
    String[] recipe = new String[] {
      "aggregate-stats :data_transfer_size :response_time total_size_mb total_time_sec"
    };

    // Execute the recipe
    List<Row> results = TestingRig.execute(recipe, rows);

    // Verify results
    Assert.assertEquals(1, results.size());
    
    // Calculate expected values
    // Size: Convert all to bytes, sum, then convert to MB (1MB = 1024*1024 bytes)
    double expectedTotalSizeInMB = (1.0 * 1024 * 1024 +  // 1MB
                                   2.5 * 1024 * 1024 +    // 2.5MB
                                   0.5 * 1024 +           // 500KB
                                   3.0 * 1024 * 1024 +    // 3MB
                                   1.5 * 1024 * 1024)     // 1.5MB
                                  / (1024 * 1024);        // Convert to MB
    
    // Time: Convert all to nanoseconds, sum, then convert to seconds
    double expectedTotalTimeInSeconds = (100.0 * 1000 * 1000 +  // 100ms
                                        200.0 * 1000 * 1000 +   // 200ms
                                        50.0 * 1000 * 1000 +    // 50ms
                                        300.0 * 1000 * 1000 +   // 300ms
                                        150.0 * 1000 * 1000)    // 150ms
                                       / (1000 * 1000 * 1000);  // Convert to seconds

    // Verify with exact assertion structure
    Assert.assertEquals(expectedTotalSizeInMB, 
                       results.get(0).getValue("total_size_mb"), 0.001);
    Assert.assertEquals(expectedTotalTimeInSeconds,
                       results.get(0).getValue("total_time_sec"), 0.001);
  }

  @Test
  public void testDifferentOutputUnits() throws Exception {
    // Create sample data with larger values
    List<Row> rows = Arrays.asList(
      createRow("1GB", "1h"),
      createRow("500MB", "30m"),
      createRow("2GB", "2h"),
      createRow("1.5GB", "1.5h")
    );

    // Define the recipe with custom output units
    String[] recipe = new String[] {
      "aggregate-stats :data_transfer_size :response_time total_size_gb total_time_h size-unit GB time-unit h"
    };

    // Execute the recipe
    List<Row> results = TestingRig.execute(recipe, rows);

    // Verify results
    Assert.assertEquals(1, results.size());
    Row result = results.get(0);
    
    // Total size should be 5GB (1GB + 500MB + 2GB + 1.5GB)
    Assert.assertEquals(5.0, result.getValue("total_size_gb"), 0.001);
    
    // Total time should be 5 hours (1h + 30m + 2h + 1.5h)
    Assert.assertEquals(5.0, result.getValue("total_time_h"), 0.001);
  }

  @Test
  public void testAverageAggregation() throws Exception {
    // Create sample data
    List<Row> rows = Arrays.asList(
      createRow("1MB", "100ms"),
      createRow("2MB", "200ms"),
      createRow("3MB", "300ms"),
      createRow("4MB", "400ms")
    );

    // Define the recipe with average aggregation
    String[] recipe = new String[] {
      "aggregate-stats :data_transfer_size :response_time avg_size_mb avg_time_sec aggregation-type average"
    };

    // Execute the recipe
    List<Row> results = TestingRig.execute(recipe, rows);

    // Verify results
    Assert.assertEquals(1, results.size());
    
    // Calculate expected values
    // Size: Convert all to bytes, sum, divide by count, then convert to MB
    double expectedAvgSizeInMB = ((1.0 * 1024 * 1024 +  // 1MB
                                  2.0 * 1024 * 1024 +    // 2MB
                                  3.0 * 1024 * 1024 +    // 3MB
                                  4.0 * 1024 * 1024)     // 4MB
                                 / 4)                    // Average
                                / (1024 * 1024);         // Convert to MB
    
    // Time: Convert all to nanoseconds, sum, divide by count, then convert to seconds
    double expectedAvgTimeInSeconds = ((100.0 * 1000 * 1000 +  // 100ms
                                       200.0 * 1000 * 1000 +   // 200ms
                                       300.0 * 1000 * 1000 +   // 300ms
                                       400.0 * 1000 * 1000)    // 400ms
                                      / 4)                     // Average
                                     / (1000 * 1000 * 1000);   // Convert to seconds

    // Verify with exact assertion structure
    Assert.assertEquals(expectedAvgSizeInMB,
                       results.get(0).getValue("avg_size_mb"), 0.001);
    Assert.assertEquals(expectedAvgTimeInSeconds,
                       results.get(0).getValue("avg_time_sec"), 0.001);
  }

  @Test
  public void testMedianAggregation() throws Exception {
    // Create sample data with odd number of values
    List<Row> rows = Arrays.asList(
      createRow("1MB", "100ms"),
      createRow("2MB", "200ms"),
      createRow("3MB", "300ms"),
      createRow("4MB", "400ms"),
      createRow("5MB", "500ms")
    );

    // Define the recipe with median aggregation
    String[] recipe = new String[] {
      "aggregate-stats :data_transfer_size :response_time median_size_mb median_time_sec aggregation-type median"
    };

    // Execute the recipe
    List<Row> results = TestingRig.execute(recipe, rows);

    // Verify results
    Assert.assertEquals(1, results.size());
    Row result = results.get(0);
    
    // Median size should be 3MB (middle value of sorted sizes)
    Assert.assertEquals(3.0, result.getValue("median_size_mb"), 0.001);
    
    // Median time should be 0.3 seconds (middle value of sorted times)
    Assert.assertEquals(0.3, result.getValue("median_time_sec"), 0.001);
  }

  @Test
  public void testPercentileAggregation() throws Exception {
    // Create sample data for percentile calculation
    List<Row> rows = Arrays.asList(
      createRow("1MB", "100ms"),
      createRow("2MB", "200ms"),
      createRow("3MB", "300ms"),
      createRow("4MB", "400ms"),
      createRow("5MB", "500ms"),
      createRow("6MB", "600ms"),
      createRow("7MB", "700ms"),
      createRow("8MB", "800ms"),
      createRow("9MB", "900ms"),
      createRow("10MB", "1000ms")
    );

    // Define recipes for p95 and p99
    String[] p95Recipe = new String[] {
      "aggregate-stats :data_transfer_size :response_time p95_size_mb p95_time_sec aggregation-type p95"
    };

    String[] p99Recipe = new String[] {
      "aggregate-stats :data_transfer_size :response_time p99_size_mb p99_time_sec aggregation-type p99"
    };

    // Execute the recipes
    List<Row> p95Results = TestingRig.execute(p95Recipe, rows);
    List<Row> p99Results = TestingRig.execute(p99Recipe, rows);

    // Verify p95 results
    Assert.assertEquals(1, p95Results.size());
    Row p95Result = p95Results.get(0);
    Assert.assertEquals(9.5, p95Result.getValue("p95_size_mb"), 0.001);
    Assert.assertEquals(0.95, p95Result.getValue("p95_time_sec"), 0.001);

    // Verify p99 results
    Assert.assertEquals(1, p99Results.size());
    Row p99Result = p99Results.get(0);
    Assert.assertEquals(9.9, p99Result.getValue("p99_size_mb"), 0.001);
    Assert.assertEquals(0.99, p99Result.getValue("p99_time_sec"), 0.001);
  }

  @Test
  public void testMixedUnitsAggregation() throws Exception {
    // Create sample data with mixed units
    List<Row> rows = Arrays.asList(
      createRow("1KB", "1s"),
      createRow("1MB", "1m"),
      createRow("1GB", "1h"),
      createRow("1TB", "1d")
    );

    // Define the recipe
    String[] recipe = new String[] {
      "aggregate-stats :data_transfer_size :response_time total_size_mb total_time_sec"
    };

    // Execute the recipe
    List<Row> results = TestingRig.execute(recipe, rows);

    // Verify results
    Assert.assertEquals(1, results.size());
    
    // Calculate expected values
    // Size: Convert all to bytes, sum, then convert to MB
    double expectedTotalSizeInMB = (1.0 * 1024 +           // 1KB
                                   1.0 * 1024 * 1024 +     // 1MB
                                   1.0 * 1024 * 1024 * 1024 + // 1GB
                                   1.0 * 1024 * 1024 * 1024 * 1024) // 1TB
                                  / (1024 * 1024);         // Convert to MB
    
    // Time: Convert all to nanoseconds, sum, then convert to seconds
    double expectedTotalTimeInSeconds = (1.0 * 1000 * 1000 * 1000 +  // 1s
                                        60.0 * 1000 * 1000 * 1000 +  // 1m
                                        3600.0 * 1000 * 1000 * 1000 + // 1h
                                        86400.0 * 1000 * 1000 * 1000) // 1d
                                       / (1000 * 1000 * 1000);        // Convert to seconds

    // Verify with exact assertion structure
    Assert.assertEquals(expectedTotalSizeInMB,
                       results.get(0).getValue("total_size_mb"), 0.001);
    Assert.assertEquals(expectedTotalTimeInSeconds,
                       results.get(0).getValue("total_time_sec"), 0.001);
  }

  private Row createRow(String size, String time) {
    Row row = new Row();
    row.add("data_transfer_size", size);
    row.add("response_time", time);
    return row;
  }
} 