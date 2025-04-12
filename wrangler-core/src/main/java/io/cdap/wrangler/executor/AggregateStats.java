package io.cdap.wrangler.executor;

import io.cdap.wrangler.api.Directive;
import io.cdap.wrangler.api.DirectiveContext;
import io.cdap.wrangler.api.DirectiveExecutionException;
import io.cdap.wrangler.api.DirectiveParseException;
import io.cdap.wrangler.api.ExecutorContext;
import io.cdap.wrangler.api.Row;
import io.cdap.wrangler.api.annotations.Categories;
import io.cdap.wrangler.api.parser.ByteSize;
import io.cdap.wrangler.api.parser.ColumnName;
import io.cdap.wrangler.api.parser.Identifier;
import io.cdap.wrangler.api.parser.Text;
import io.cdap.wrangler.api.parser.TimeDuration;
import io.cdap.wrangler.api.parser.TokenType;
import io.cdap.wrangler.api.parser.UsageDefinition;

import java.util.List;

/**
 * A directive for aggregating byte size and time duration statistics.
 */
@Categories(categories = {"aggregate"})
public class AggregateStats implements Directive {
  public static final String NAME = "aggregate-stats";
  private String sizeColumn;
  private String timeColumn;
  private String totalSizeColumn;
  private String totalTimeColumn;
  private String sizeUnit = "MB";
  private String timeUnit = "s";
  private String aggregationType = "total";

  private long totalBytes = 0;
  private long totalNanoseconds = 0;
  private int rowCount = 0;

  @Override
  public UsageDefinition define() {
    UsageDefinition.Builder builder = UsageDefinition.builder(NAME);
    builder.define("size-column", TokenType.COLUMN_NAME, "Source column containing byte sizes");
    builder.define("time-column", TokenType.COLUMN_NAME, "Source column containing time durations");
    builder.define("total-size-column", TokenType.COLUMN_NAME, "Target column for total size");
    builder.define("total-time-column", TokenType.COLUMN_NAME, "Target column for total time");
    builder.define("size-unit", TokenType.TEXT, "Output unit for size (B, KB, MB, GB, TB, PB, EB)", false);
    builder.define("time-unit", TokenType.TEXT, "Output unit for time (ns, µs, ms, s, m, h, d, y)", false);
    builder.define("aggregation-type", TokenType.TEXT, "Type of aggregation (total, average)", false);
    return builder.build();
  }

  @Override
  public void initialize(Arguments args) throws DirectiveParseException {
    this.sizeColumn = ((ColumnName) args.value("size-column")).value();
    this.timeColumn = ((ColumnName) args.value("time-column")).value();
    this.totalSizeColumn = ((ColumnName) args.value("total-size-column")).value();
    this.totalTimeColumn = ((ColumnName) args.value("total-time-column")).value();

    if (args.contains("size-unit")) {
      this.sizeUnit = ((Text) args.value("size-unit")).value();
    }
    if (args.contains("time-unit")) {
      this.timeUnit = ((Text) args.value("time-unit")).value();
    }
    if (args.contains("aggregation-type")) {
      this.aggregationType = ((Text) args.value("aggregation-type")).value();
    }
  }

  @Override
  public void destroy() {
    // No-op
  }

  @Override
  public List<Row> execute(List<Row> rows, ExecutorContext context) throws DirectiveExecutionException {
    for (Row row : rows) {
      Object sizeValue = row.getValue(sizeColumn);
      Object timeValue = row.getValue(timeColumn);

      if (sizeValue != null) {
        ByteSize byteSize = new ByteSize(sizeValue.toString());
        totalBytes += byteSize.getBytes();
      }

      if (timeValue != null) {
        TimeDuration timeDuration = new TimeDuration(timeValue.toString());
        totalNanoseconds += timeDuration.getNanoseconds();
      }

      rowCount++;
    }

    // Create result row with aggregated values
    Row result = new Row();
    result.add(totalSizeColumn, convertSizeToUnit(totalBytes, sizeUnit));
    result.add(totalTimeColumn, convertTimeToUnit(totalNanoseconds, timeUnit));

    return List.of(result);
  }

  private double convertSizeToUnit(long bytes, String unit) {
    ByteSize byteSize = new ByteSize(bytes + "B");
    switch (unit.toUpperCase()) {
      case "B":
        return byteSize.getBytes();
      case "KB":
        return byteSize.getKB();
      case "MB":
        return byteSize.getMB();
      case "GB":
        return byteSize.getGB();
      case "TB":
        return byteSize.getTB();
      case "PB":
        return byteSize.getPB();
      case "EB":
        return byteSize.getEB();
      default:
        throw new IllegalArgumentException("Unsupported size unit: " + unit);
    }
  }

  private double convertTimeToUnit(long nanoseconds, String unit) {
    TimeDuration timeDuration = new TimeDuration(nanoseconds + "ns");
    switch (unit.toLowerCase()) {
      case "ns":
        return timeDuration.getNanoseconds();
      case "µs":
        return timeDuration.getMicroseconds();
      case "ms":
        return timeDuration.getMilliseconds();
      case "s":
        return timeDuration.getSeconds();
      case "m":
        return timeDuration.getMinutes();
      case "h":
        return timeDuration.getHours();
      case "d":
        return timeDuration.getDays();
      case "y":
        return timeDuration.getYears();
      default:
        throw new IllegalArgumentException("Unsupported time unit: " + unit);
    }
  }
} 