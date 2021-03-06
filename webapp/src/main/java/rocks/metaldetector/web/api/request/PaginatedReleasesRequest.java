package rocks.metaldetector.web.api.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.lang.Nullable;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;

import static javax.validation.constraints.Pattern.Flag.CASE_INSENSITIVE;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginatedReleasesRequest implements WithTimeRangeValidation {

  @Min(value = 1, message = "'page' must be greater than zero!")
  private int page = 1;

  @Min(value = 1, message = "'size' must be greater than zero!")
  @Max(value = 50, message = "'size' must be equal or less than 50!")
  private int size = 40;

  @NotNull
  @NotEmpty
  private String sort;

  @NotNull
  @Pattern(regexp = "asc|desc", flags = CASE_INSENSITIVE)
  private String direction = "asc";

  @Nullable
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate dateFrom;

  @Nullable
  @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
  private LocalDate dateTo;

  @Nullable
  private String query;

}
