package sekigae.sekigae.seatingapp.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sekigae.sekigae.seatingapp.entity.Student;

@Service
@RequiredArgsConstructor
public class SeatingService {

  private final StudentService studentService;

  /**
   * 座席表を2次元配列で取得する
   *
   * @param rows    行数
   * @param columns 列数
   * @return 座席表（生徒名が入った2次元配列）
   */
  public String[][] getSeatingChart(int rows, int columns) {
    String[][] chart = new String[rows][columns];

    // 全生徒を取得
    List<Student> students = studentService.getAllStudents();

    // 各座席に生徒を配置
    for (Student student : students) {
      if (student.getSeatRow() != null && student.getSeatColumn() != null) {
        int row = student.getSeatRow() - 1; // 1始まりを0始まりに変換
        int col = student.getSeatColumn() - 1;

        // 範囲チェック
        if (row >= 0 && row < rows && col >= 0 && col < columns) {
          chart[row][col] = student.getName();
        }
      }
    }

    return chart;
  }

  /**
   * 座席をシャッフルする（席替え機能）
   */
  public void shuffleSeats(int rows, int columns) {
    List<Student> students = studentService.getAllStudents();

    // TODO: 席替えロジックを実装
    // 1. 制約条件を考慮した席替えアルゴリズム
    // 2. 男女の配置バランス
    // 3. 特定の生徒同士を隣にしない等の制約
  }

  //public void shuffleSeats(int rows, int columns, SeatingConstraints constraints)
}