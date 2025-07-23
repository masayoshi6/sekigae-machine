package sekigae.sekigae.seatingapp.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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

  // 生徒の名前（必須）
  @Column(nullable = false)
  private String name;

  // 性別（例：MALE, FEMALEなど）
  @Column(nullable = false)
  private String gender;

  // 学籍番号や一意なコードが必要な場合は追加可能
  @Column(unique = true)
  private String studentCode;

  // 座席番号（行と列を管理するなど座席の位置管理のため）
  private Integer seatRow;

  private Integer seatColumn;
}
