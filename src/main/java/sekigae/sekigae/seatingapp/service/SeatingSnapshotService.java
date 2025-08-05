package sekigae.sekigae.seatingapp.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sekigae.sekigae.seatingapp.entity.SeatingSnapshot;
import sekigae.sekigae.seatingapp.entity.SeatingSnapshotDetail;
import sekigae.sekigae.seatingapp.entity.Student;
import sekigae.sekigae.seatingapp.repository.SeatingSnapshotDetailRepository;
import sekigae.sekigae.seatingapp.repository.SeatingSnapshotRepository;
import sekigae.sekigae.seatingapp.repository.StudentRepository;

@Service
@RequiredArgsConstructor
public class SeatingSnapshotService {

  private final SeatingSnapshotRepository snapshotRepository;
  private final SeatingSnapshotDetailRepository snapshotDetailRepository;
  private final StudentRepository studentRepository;
  private final SeatingService seatingService;

  /**
   * 現在の座席配置を保存する
   *
   * @param rows         座席の行数
   * @param columns      座席の列数
   * @param snapshotName スナップショット名（nullの場合は自動生成）
   * @param description  説明文
   * @return 保存されたスナップショット
   */
  @Transactional
  public SeatingSnapshot saveCurrentSeatingSnapshot(int rows, int columns, String snapshotName,
      String description) {
    // スナップショット名が指定されていない場合は自動生成
    if (snapshotName == null || snapshotName.trim().isEmpty()) {
      LocalDateTime now = LocalDateTime.now();
      snapshotName = "座席配置_" + now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
    }

    // スナップショットメインデータを作成
    SeatingSnapshot snapshot = SeatingSnapshot.builder()
        .snapshotName(snapshotName)
        .rows(rows)
        .columns(columns)
        .description(description)
        .build();

    // スナップショットを保存
    snapshot = snapshotRepository.save(snapshot);

    // 現在の座席配置を取得
    Student[][] currentSeating = seatingService.getSeatingChartWithGender(rows, columns);

    // 座席詳細データを作成・保存
    List<SeatingSnapshotDetail> details = new ArrayList<>();
    for (int row = 0; row < rows; row++) {
      for (int col = 0; col < columns; col++) {
        Student student = currentSeating[row][col];
        if (student != null) {
          SeatingSnapshotDetail detail = SeatingSnapshotDetail.builder()
              .snapshot(snapshot)
              .seatRow(row + 1) // 1始まりで保存
              .seatColumn(col + 1)
              .studentId(student.getId())
              .studentName(student.getName())
              .studentGender(student.getGender())
              .studentCode(student.getStudentCode())
              .build();
          details.add(detail);
        }
      }
    }

    if (!details.isEmpty()) {
      snapshotDetailRepository.saveAll(details);
    }

    return snapshot;
  }

  /**
   * 保存されたスナップショットを復元する
   *
   * @param snapshotId 復元するスナップショットのID
   * @return 復元に成功した場合true
   */
  @Transactional
  public boolean restoreSeatingSnapshot(Long snapshotId) {
    // スナップショットを取得
    SeatingSnapshot snapshot = snapshotRepository.findById(snapshotId).orElse(null);
    if (snapshot == null) {
      return false;
    }

    // スナップショットの詳細データを取得
    List<SeatingSnapshotDetail> details = snapshotDetailRepository.findBySnapshotId(snapshotId);

    // まず全生徒の座席位置をクリア
    List<Student> allStudents = studentRepository.findAll();
    for (Student student : allStudents) {
      student.setSeatRow(null);
      student.setSeatColumn(null);
    }

    // スナップショットデータを基に座席位置を復元
    for (SeatingSnapshotDetail detail : details) {
      // 学生IDで学生を検索して座席位置を設定
      Student student = studentRepository.findById(detail.getStudentId()).orElse(null);
      if (student != null) {
        student.setSeatRow(detail.getSeatRow());
        student.setSeatColumn(detail.getSeatColumn());
      }
    }

    // データベースに保存
    studentRepository.saveAll(allStudents);

    return true;
  }

  /**
   * 全てのスナップショットを取得（作成日時の降順）
   *
   * @return スナップショットのリスト
   */
  public List<SeatingSnapshot> getAllSnapshots() {
    return snapshotRepository.findAllByOrderByCreatedAtDesc();
  }

  /**
   * スナップショットを削除
   *
   * @param snapshotId 削除するスナップショットのID
   * @return 削除に成功した場合true
   */
  @Transactional
  public boolean deleteSnapshot(Long snapshotId) {
    try {
      // まず詳細データを削除
      snapshotDetailRepository.deleteBySnapshotId(snapshotId);
      // スナップショット本体を削除
      snapshotRepository.deleteById(snapshotId);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * 特定のスナップショットの詳細情報を取得
   *
   * @param snapshotId スナップショットID
   * @return スナップショットの詳細データ
   */
  public List<SeatingSnapshotDetail> getSnapshotDetails(Long snapshotId) {
    return snapshotDetailRepository.findBySnapshotId(snapshotId);
  }
}