package sekigae.sekigae.seatingapp.service;

import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sekigae.sekigae.seatingapp.entity.Student;
import sekigae.sekigae.seatingapp.repository.StudentRepository;

@Service
@RequiredArgsConstructor
public class SeatingService {

  private final StudentService studentService;
  private final StudentRepository studentRepository;


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
   * 現在の座席表の行数を取得
   *
   * @return 現在設定されている行数（デフォルト: 6）
   */
  public int getCurrentRows() {
    // データベースまたは設定から現在の行数を取得
    // 実装方法例：
    // 1. データベースに設定テーブルを作成して保存
    // 2. application.propertiesから読み込み
    // 3. セッションやキャッシュに保存

    // とりあえずデフォルト値を返す
    return 6;
  }

  /**
   * 現在の座席表の列数を取得
   *
   * @return 現在設定されている列数（デフォルト: 5）
   */
  public int getCurrentColumns() {
    // データベースまたは設定から現在の列数を取得
    // getCurrentRows()と同様の実装

    // とりあえずデフォルト値を返す
    return 5;
  }

  /**
   * 座席表設定を保存
   *
   * @param rows    行数
   * @param columns 列数
   */
  public void saveSeatingConfiguration(int rows, int columns) {
    // データベースやキャッシュに現在の設定を保存
    // 例：設定テーブルに保存、またはアプリケーション変数に保存
  }

  /**
   * 座席表を再生成（シャッフル）
   *
   * @param rows    行数
   * @param columns 列数
   */
  public void regenerateSeatingChart(int rows, int columns) {
    // 現在の学生リストを取得
    // 座席をランダムに再配置
    // 新しい配置を保存

    // 設定も更新
    saveSeatingConfiguration(rows, columns);
  }

  /**
   * 現在の座席をシャッフルするためのメソッドです
   *
   * @param rows    現在の座席の行数
   * @param columns 現在の座席の列数
   * @return 新しい座席配置（２次元配列として受け取ります。）
   */
  public Student[][] getSeatingChartWithGender(int rows, int columns) {
    // 全生徒を取得
    List<Student> allStudents = studentService.getAllStudents();

    Student[][] chart = new Student[rows][columns];
    Collections.shuffle(allStudents);

    int index = 0;
    for (int r = 0; r < rows; r++) {
      for (int c = 0; c < columns; c++) {
        if (index < allStudents.size()) {
          chart[r][c] = allStudents.get(index++);
        }
      }
    }

    return chart;
  }


  public Student[][] shuffleSeatingChart(int rows, int columns) {
    // すべての学生を取得
    List<Student> allStudents = studentRepository.findAll();

    // ランダムに並び替える
    Collections.shuffle(allStudents);

    // 座席表（2次元配列）を初期化
    Student[][] chart = new Student[rows][columns];

    // 学生を座席表に配置
    int index = 0;
    for (int r = 0; r < rows; r++) {
      for (int c = 0; c < columns; c++) {
        if (index < allStudents.size()) {
          chart[r][c] = allStudents.get(index++);
        } else {
          chart[r][c] = null; // 生徒が足りないときはnull
        }
      }
    }

    return chart;
  }

}
