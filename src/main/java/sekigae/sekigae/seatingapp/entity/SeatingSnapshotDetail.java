package sekigae.sekigae.seatingapp.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "保存した座席表詳細")
@Entity
@Table(name = "seating_snapshot_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatingSnapshotDetail {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Schema(description = "座席表ID", example = "5")
  private Long id;

  @Schema(description = "座席表保存用オブジェクト", example = "4ja@001hkl5")
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "snapshot_id", nullable = false)
  private SeatingSnapshot snapshot;

  @Schema(description = "保存した座席表の行数", example = "5")
  @Column(nullable = false)
  private Integer seatRow;

  @Schema(description = "保存した座席表の列数", example = "5")
  @Column(nullable = false)
  private Integer seatColumn;

  @Schema(description = "生徒ID", example = "5")
  @Column(nullable = false)
  private Long studentId;

  @Schema(description = "生徒氏名", example = "田中　太郎")
  @Column(nullable = false)
  private String studentName;

  @Schema(description = "性別", example = "男子")
  @Column(nullable = false)
  private String studentGender;

  @Schema(description = "学籍番号", example = "5")
  @Column(nullable = false)
  private String studentCode;
}