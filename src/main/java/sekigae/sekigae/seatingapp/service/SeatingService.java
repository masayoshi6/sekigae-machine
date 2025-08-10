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
   * 現在の座席表の行数を取得します
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
   * 現在の座席表の列数を取得します
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
   * 座席表設定を保存します
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
   * 現在の座席配置を取得する（シャッフルしない） データベースに保存されている座席位置情報を元に座席表を作成します
   *
   * @param rows    現在の座席の行数
   * @param columns 現在の座席の列数
   * @return 現在の座席配置（２次元配列として受け取ります。）
   */
  public Student[][] getSeatingChartWithGender(int rows, int columns) {
    // 全生徒を取得
    List<Student> allStudents = studentService.getAllStudents();

    Student[][] chart = new Student[rows][columns];

    // 各生徒をデータベースに保存されている座席位置に配置
    for (Student student : allStudents) {
      if (student.getSeatRow() != null && student.getSeatColumn() != null) {
        int row = student.getSeatRow() - 1; // 1始まりを0始まりに変換
        int col = student.getSeatColumn() - 1;

        // 範囲チェック
        if (row >= 0 && row < rows && col >= 0 && col < columns) {
          chart[row][col] = student;
        }
      }
    }

    return chart;
  }

  /**
   * 座席をシャッフルして新しい配置を作成し、データベースに保存します
   *
   * @param rows    座席の行数
   * @param columns 座席の列数
   * @return シャッフルされた座席配置
   */
  public Student[][] shuffleSeatingChart(int rows, int columns) {
    return shuffleSeatingChart(rows, columns, false);
  }

  /**
   * 座席をシャッフルして新しい配置を作成し、データベースに保存します
   *
   * @param rows             座席の行数
   * @param columns          座席の列数
   * @param alternateGenders 男女を交互に配置するかどうか
   * @return シャッフルされた座席配置
   */
  public Student[][] shuffleSeatingChart(int rows, int columns, boolean alternateGenders) {
    return shuffleSeatingChart(rows, columns, alternateGenders, false);
  }

  /**
   * 座席をシャッフルして新しい配置を作成し、データベースに保存します
   *
   * @param rows             座席の行数
   * @param columns          座席の列数
   * @param alternateGenders 男女を交互に配置するかどうか
   * @param alternateColumns 列単位で男女を分けるかどうか
   * @return シャッフルされた座席配置
   */
  public Student[][] shuffleSeatingChart(int rows, int columns, boolean alternateGenders,
      boolean alternateColumns) {
    // すべての学生を取得
    List<Student> allStudents = studentRepository.findAll();

    // 座席表（2次元配列）を初期化
    Student[][] chart = new Student[rows][columns];

    if (alternateColumns) {
      // 列単位で男女交互配置の場合
      chart = arrangeStudentsByColumns(allStudents, rows, columns);
    } else if (alternateGenders) {
      // チェスボード式男女交互配置の場合
      chart = arrangeStudentsAlternating(allStudents, rows, columns);
    } else {
      // 通常のランダム配置
      Collections.shuffle(allStudents);
      chart = arrangeStudentsNormally(allStudents, rows, columns);
    }

    // データベースの座席位置を更新
    updateSeatPositionsInDatabase(chart, rows, columns);

    return chart;
  }

  /**
   * 列単位で男女を交互に配置する
   */
  private Student[][] arrangeStudentsByColumns(List<Student> allStudents, int rows, int columns) {
    Student[][] chart = new Student[rows][columns];

    // 男子と女子を分ける
    List<Student> maleStudents = allStudents.stream()
        .filter(s -> "男子".equals(s.getGender()))
        .collect(java.util.stream.Collectors.toList());
    List<Student> femaleStudents = allStudents.stream()
        .filter(s -> "女子".equals(s.getGender()))
        .collect(java.util.stream.Collectors.toList());

    // それぞれをシャッフル
    Collections.shuffle(maleStudents);
    Collections.shuffle(femaleStudents);

    int maleIndex = 0;
    int femaleIndex = 0;

    // 列ごとに処理
    for (int c = 0; c < columns; c++) {
      boolean isMaleColumn = (c % 2 == 0); // 偶数列は男子、奇数列は女子

      for (int r = 0; r < rows; r++) {
        if (isMaleColumn && maleIndex < maleStudents.size()) {
          // 男子の列
          chart[r][c] = maleStudents.get(maleIndex++);
        } else if (!isMaleColumn && femaleIndex < femaleStudents.size()) {
          // 女子の列
          chart[r][c] = femaleStudents.get(femaleIndex++);
        } else if (maleIndex < maleStudents.size()) {
          // 女子が足りない場合は男子を配置
          chart[r][c] = maleStudents.get(maleIndex++);
        } else if (femaleIndex < femaleStudents.size()) {
          // 男子が足りない場合は女子を配置
          chart[r][c] = femaleStudents.get(femaleIndex++);
        }
      }
    }

    return chart;
  }

  /**
   * 男女を交互に配置する（チェスボードパターン）
   */
  private Student[][] arrangeStudentsAlternating(List<Student> allStudents, int rows, int columns) {
    Student[][] chart = new Student[rows][columns];

    // 男子と女子を分ける
    List<Student> maleStudents = allStudents.stream()
        .filter(s -> "男子".equals(s.getGender()))
        .collect(java.util.stream.Collectors.toList());
    List<Student> femaleStudents = allStudents.stream()
        .filter(s -> "女子".equals(s.getGender()))
        .collect(java.util.stream.Collectors.toList());

    // それぞれをシャッフル
    Collections.shuffle(maleStudents);
    Collections.shuffle(femaleStudents);

    int maleIndex = 0;
    int femaleIndex = 0;
    boolean shouldBeMale = true; // 最初は男子から開始

    // チェスボード式に配置
    for (int r = 0; r < rows; r++) {
      for (int c = 0; c < columns; c++) {
        // チェスボードパターンで男女を決定
        boolean isMalePosition = ((r + c) % 2 == 0) ? shouldBeMale : !shouldBeMale;

        if (isMalePosition && maleIndex < maleStudents.size()) {
          chart[r][c] = maleStudents.get(maleIndex++);
        } else if (!isMalePosition && femaleIndex < femaleStudents.size()) {
          chart[r][c] = femaleStudents.get(femaleIndex++);
        } else if (maleIndex < maleStudents.size()) {
          // 女子が足りない場合は男子を配置
          chart[r][c] = maleStudents.get(maleIndex++);
        } else if (femaleIndex < femaleStudents.size()) {
          // 男子が足りない場合は女子を配置
          chart[r][c] = femaleStudents.get(femaleIndex++);
        }
      }
    }

    return chart;
  }

  /**
   * 通常のランダム配置
   */
  private Student[][] arrangeStudentsNormally(List<Student> allStudents, int rows, int columns) {
    Student[][] chart = new Student[rows][columns];

    int index = 0;
    for (int r = 0; r < rows; r++) {
      for (int c = 0; c < columns; c++) {
        if (index < allStudents.size()) {
          chart[r][c] = allStudents.get(index++);
        } else {
          chart[r][c] = null;
        }
      }
    }

    return chart;
  }

  /**
   * データベースの座席位置を更新
   */
  private void updateSeatPositionsInDatabase(Student[][] chart, int rows, int columns) {
    // まず全学生の座席位置をクリア
    List<Student> allStudents = studentRepository.findAll();
    for (Student student : allStudents) {
      student.setSeatRow(null);
      student.setSeatColumn(null);
    }

    // 座席表に配置された学生の位置を更新
    for (int r = 0; r < rows; r++) {
      for (int c = 0; c < columns; c++) {
        if (chart[r][c] != null) {
          Student student = chart[r][c];
          student.setSeatRow(r + 1); // 1始まりで保存
          student.setSeatColumn(c + 1);
        }
      }
    }

    // データベースに保存
    studentRepository.saveAll(allStudents);
  }
}