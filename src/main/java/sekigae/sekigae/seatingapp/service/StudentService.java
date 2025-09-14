package sekigae.sekigae.seatingapp.service;

import jakarta.transaction.Transactional;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sekigae.sekigae.seatingapp.entity.Student;
import sekigae.sekigae.seatingapp.repository.StudentRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class StudentService {

  private final StudentRepository studentRepository;

  /**
   * 全ての生徒情報を取得するメソッドです
   *
   * @return 生徒情報（全件）
   */
  public List<Student> getAllStudents() {
    return studentRepository.findAll();
  }

  /**
   * 生徒を名前で検索するためのメソッドです
   *
   * @param keyword 生徒氏名
   * @return 検索をかけた生徒情報のリスト
   */
  public List<Student> searchByName(String keyword) {
    return studentRepository.findByNameContaining(keyword);
  }

  /**
   * 生徒を性別で検索します
   *
   * @param gender 男子　または　女子
   * @return 該当する性別の生徒情報のリスト
   */
  public List<Student> getByGender(String gender) {

    return studentRepository.findByGender(gender);
  }

  /**
   * 生徒を新規登録するメソッドです
   *
   * @param student 新規登録のフォーム画面で入力した情報を持つ生徒オブジェクト
   * @return 新規登録のフォーム画面で入力した生徒情報をDBに保存します
   */
  public Student registerStudent(Student student) {

    // 同じ座席の生徒がいるかチェック
    Student existing = studentRepository.findBySeatRowAndSeatColumn(
        student.getSeatRow(), student.getSeatColumn());

    if (existing != null) {
      // ここで例外をスロー → メソッド終了
      throw new IllegalArgumentException("指定された座席にはすでに他の生徒が登録されています。");
      // この下のコードは実行されない（到達不可能コード）
    }

    // existing == null の場合のみ、ここまで↓到達する　
    return studentRepository.save(student);
  }

  /**
   * 指定IDの生徒情報を取得するメソッドです（該当する生徒が見つからない場合はnullを返します）
   *
   * @param id 生徒ID
   * @return
   */
  public Student getStudentById(Long id) {
    Optional<Student> student = studentRepository.findById(id);
    return student.orElse(null);
  }

  /**
   * 指定座席にいる生徒を取得
   */
  public Student getStudentBySeat(int row, int col) {
    return studentRepository.findBySeatRowAndSeatColumn(row, col);
  }

  /**
   * 生徒の座席を更新する
   */
  public Student updateSeat(Long studentId, int newRow, int newCol) {
    Student student = getStudentById(studentId);
    if (student != null) {
      student.setSeatRow(newRow);
      student.setSeatColumn(newCol);
      return studentRepository.save(student);
    }
    return null;
  }

  /**
   * 生徒情報の削除
   *
   * @param id 学籍番号
   */
  public void deleteStudent(Long id) {

    studentRepository.deleteById(id);
  }

  /**
   * 生徒の座席をシャッフルします
   *
   * @param rows    座席の行数
   * @param columns 座席の列数
   * @return シャッフル後の座席配置
   */
  public Student[][] shuffleSeatingChart(int rows, int columns) {
    List<Student> allStudents = studentRepository.findAll();
    Collections.shuffle(allStudents); // ← ランダムに並び替え

    Student[][] chart = new Student[rows][columns];
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


  public boolean isStudentCodeDuplicate(String studentCode) {
    return studentRepository.findByStudentCode(studentCode).isPresent();
  }

}
