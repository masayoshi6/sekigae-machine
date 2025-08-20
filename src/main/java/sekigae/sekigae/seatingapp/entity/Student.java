package sekigae.sekigae.seatingapp.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "生徒")
@Entity
@Table(name = "students")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Schema(description = "生徒ID", example = "5")
  private Long id;

  @Schema(description = "生徒氏名", example = "田中　太郎")
  @NotBlank(message = "名前を入力してください")
  @Column(nullable = false)
  private String name;

  @Schema(description = "性別", example = "男子")
  @NotBlank(message = "性別を選択してください")
  @Column(nullable = false)
  private String gender;

  @Schema(description = "学籍番号", example = "5")
  @NotBlank(message = "学籍番号は必須です")
  @Pattern(regexp = "^[1-9][0-9]*$", message = "学籍番号は半角の自然数を入力してください")
  @Column(unique = true)
  private String studentCode;

  @Schema(description = "座席の行数", example = "5")
  @NotNull(message = "座席の行を入力してください")
  @Min(value = 1, message = "座席（行）は1以上にしてください")
  @Max(value = 6, message = "座席（行）は6以下にしてください")
  private Integer seatRow;

  @Schema(description = "座席の列数", example = "5")
  @NotNull(message = "座席の列を入力してください")
  @Min(value = 1, message = "座席（列）は1以上にしてください")
  @Max(value = 5, message = "座席（列）は5以下にしてください")
  private Integer seatColumn;
}
