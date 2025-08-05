package sekigae.sekigae.seatingapp.entity;

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
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "snapshot_id", nullable = false)
  private SeatingSnapshot snapshot;

  @Column(nullable = false)
  private Integer seatRow;

  @Column(nullable = false)
  private Integer seatColumn;

  @Column(nullable = false)
  private Long studentId;

  @Column(nullable = false)
  private String studentName;

  @Column(nullable = false)
  private String studentGender;

  @Column(nullable = false)
  private String studentCode;
}