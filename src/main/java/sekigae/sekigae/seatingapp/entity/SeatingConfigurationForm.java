package sekigae.sekigae.seatingapp.entity;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 座席表設定フォームのDTOクラス
 */
@Data
public class SeatingConfigurationForm {

  @NotNull(message = "行数を入力してください")
  @Min(value = 1, message = "行数は1以上で入力してください")
  @Max(value = 20, message = "行数は20以下で入力してください")
  private Integer rows;

  @NotNull(message = "列数を入力してください")
  @Min(value = 1, message = "列数は1以上で入力してください")
  @Max(value = 20, message = "列数は20以下で入力してください")
  private Integer columns;

  /**
   * 総座席数を計算して返す
   */
  public int getTotalSeats() {
    if (rows != null && columns != null) {
      return rows * columns;
    }
    return 0;
  }
}
