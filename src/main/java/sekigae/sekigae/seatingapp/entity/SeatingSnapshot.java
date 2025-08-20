package sekigae.sekigae.seatingapp.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "座席表保存")
@Entity
@Table(name = "seating_snapshots")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatingSnapshot {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Schema(description = "座席表ID", example = "5")
  private Long id;

  @Schema(description = "座席表名", example = "期末テスト用")
  @Column(nullable = false)
  private String snapshotName;

  @Schema(description = "座席表の行数", example = "5")
  @Column(name = "`rows`", nullable = false)  // バッククォートで囲む(rowsはJavaの予約語のため)
  private Integer rows;

  @Schema(description = "座席表の列数", example = "5")
  @Column(name = "`columns`", nullable = false)  // バッククォートで囲む(columnsはJavaの予約語のため)
  private Integer columns;

  @Schema(description = "保存んする座席表の概要（簡単な説明）", example = "期末テスト用の座席")
  @Column(columnDefinition = "TEXT")
  private String description;

  @Schema(description = "座席表の保存日時", example = "2025-04-09-13:00:00")
  @Column(nullable = false)
  private LocalDateTime createdAt;

  @Schema(description = "座席表の更新日時", example = "2025-05-05-14:00:00")
  @Column(nullable = false)
  private LocalDateTime updatedAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
  }

  @PreUpdate
  protected void onUpdate() {
    updatedAt = LocalDateTime.now();
  }
}