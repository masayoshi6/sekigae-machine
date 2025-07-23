package sekigae.sekigae.seatingapp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
  private Long id;

  @NotBlank(message = "名前を入力してください")
  @Column(nullable = false)
  private String name;

  @NotBlank(message = "性別を選択してください")
  @Column(nullable = false)
  private String gender;

  @NotBlank(message = "学籍番号を入力してください")
  @Column(unique = true)
  private String studentCode;

  @Min(value = 1, message = "座席の行番号は1以上を入力してください")
  private Integer seatRow;

  @Min(value = 1, message = "座席の列番号は1以上を入力してください")
  private Integer seatColumn;
}
