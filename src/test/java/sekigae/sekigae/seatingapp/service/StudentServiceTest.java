package sekigae.sekigae.seatingapp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sekigae.sekigae.seatingapp.entity.Student;
import sekigae.sekigae.seatingapp.repository.StudentRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("StudentService - getAllStudents() メソッドのテスト")
class StudentServiceTest {

  @Mock
  private StudentRepository studentRepository;

  @InjectMocks
  private StudentService studentService;

  private Student student1;
  private Student student2;
  private Student student3;
  private Student newStudent;
  private Student existingStudent;
  private Student savedStudent;

  @BeforeEach
  void setUp() {
    // テスト用のStudentオブジェクトを作成
    student1 = Student.builder()
        .id(1L)
        .name("田中太郎")
        .gender("男性")
        .studentCode("1001")
        .seatRow(1)
        .seatColumn(1)
        .build();

    student2 = Student.builder()
        .id(2L)
        .name("佐藤花子")
        .gender("女性")
        .studentCode("1002")
        .seatRow(2)
        .seatColumn(3)
        .build();

    student3 = Student.builder()
        .id(3L)
        .name("鈴木次郎")
        .gender("男性")
        .studentCode("1003")
        .seatRow(3)
        .seatColumn(2)
        .build();

    // 新規登録する生徒
    newStudent = Student.builder()
        .name("田中太郎")
        .gender("男性")
        .studentCode("1001")
        .seatRow(1)
        .seatColumn(1)
        .build();

    // 既に存在する生徒（座席重複チェック用）
    existingStudent = Student.builder()
        .id(1L)
        .name("佐藤花子")
        .gender("女性")
        .studentCode("1002")
        .seatRow(1)
        .seatColumn(1)
        .build();

    // 保存後の生徒（IDが付与された状態）
    savedStudent = Student.builder()
        .id(2L)
        .name("田中太郎")
        .gender("男性")
        .studentCode("1001")
        .seatRow(1)
        .seatColumn(1)
        .build();


  }

  @Test
  @DisplayName("複数の生徒が存在する場合、全ての生徒を取得できること")
  void getAllStudents_複数の生徒が存在する場合() {
    // Given
    List<Student> expectedStudents = Arrays.asList(student1, student2, student3);
    when(studentRepository.findAll()).thenReturn(expectedStudents);

    // When
    List<Student> actualStudents = studentService.getAllStudents();

    // Then
    assertThat(actualStudents).isNotNull();
    assertThat(actualStudents).hasSize(3);
    assertThat(actualStudents).containsExactly(student1, student2, student3);

    // repositoryのfindAll()が1回呼ばれたことを検証
    verify(studentRepository).findAll();
  }

  @Test
  @DisplayName("生徒が1人だけ存在する場合、その生徒を取得できること")
  void getAllStudents_生徒が1人だけ存在する場合() {
    // Given
    List<Student> expectedStudents = Arrays.asList(student1);
    when(studentRepository.findAll()).thenReturn(expectedStudents);

    // When
    List<Student> actualStudents = studentService.getAllStudents();

    // Then
    assertThat(actualStudents).isNotNull();
    assertThat(actualStudents).hasSize(1);
    assertThat(actualStudents).containsExactly(student1);

    verify(studentRepository).findAll();
  }

  @Test
  @DisplayName("生徒が存在しない場合、空のリストを取得できること")
  void getAllStudents_生徒が存在しない場合() {
    // Given
    List<Student> expectedStudents = Collections.emptyList();
    when(studentRepository.findAll()).thenReturn(expectedStudents);

    // When
    List<Student> actualStudents = studentService.getAllStudents();

    // Then
    assertThat(actualStudents).isNotNull();
    assertThat(actualStudents).isEmpty();

    verify(studentRepository).findAll();
  }

  @Test
  @DisplayName("取得した生徒リストの各要素が正しい値を持っていること")
  void getAllStudents_取得した生徒の値が正しいこと() {
    // Given
    List<Student> expectedStudents = Arrays.asList(student1, student2);
    when(studentRepository.findAll()).thenReturn(expectedStudents);

    // When
    List<Student> actualStudents = studentService.getAllStudents();

    // Then
    assertThat(actualStudents).hasSize(2);

    // 1番目の生徒の詳細検証
    Student firstStudent = actualStudents.get(0);
    assertThat(firstStudent.getId()).isEqualTo(1L);
    assertThat(firstStudent.getName()).isEqualTo("田中太郎");
    assertThat(firstStudent.getGender()).isEqualTo("男性");
    assertThat(firstStudent.getStudentCode()).isEqualTo("1001");
    assertThat(firstStudent.getSeatRow()).isEqualTo(1);
    assertThat(firstStudent.getSeatColumn()).isEqualTo(1);

    // 2番目の生徒の詳細検証
    Student secondStudent = actualStudents.get(1);
    assertThat(secondStudent.getId()).isEqualTo(2L);
    assertThat(secondStudent.getName()).isEqualTo("佐藤花子");
    assertThat(secondStudent.getGender()).isEqualTo("女性");
    assertThat(secondStudent.getStudentCode()).isEqualTo("1002");
    assertThat(secondStudent.getSeatRow()).isEqualTo(2);
    assertThat(secondStudent.getSeatColumn()).isEqualTo(3);

    verify(studentRepository).findAll();
  }

  @Nested
  @DisplayName("正常系テスト")
  class SuccessTests {

    @Test
    @DisplayName("空いている座席に生徒を正常に登録できること")
    void registerStudent_空いている座席の場合() {
      // Given
      when(studentRepository.findBySeatRowAndSeatColumn(1, 1)).thenReturn(null);
      when(studentRepository.save(newStudent)).thenReturn(savedStudent);

      // When
      Student result = studentService.registerStudent(newStudent);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.getId()).isEqualTo(2L);
      assertThat(result.getName()).isEqualTo("田中太郎");
      assertThat(result.getGender()).isEqualTo("男性");
      assertThat(result.getStudentCode()).isEqualTo("1001");
      assertThat(result.getSeatRow()).isEqualTo(1);
      assertThat(result.getSeatColumn()).isEqualTo(1);

      // メソッドの呼び出し順序と回数を検証
      verify(studentRepository).findBySeatRowAndSeatColumn(1, 1);
      verify(studentRepository).save(newStudent);
    }

    @Test
    @DisplayName("異なる座席番号での生徒登録が正常に完了すること")
    void registerStudent_異なる座席番号の場合() {
      // Given
      Student studentForSeat23 = Student.builder()
          .name("山田次郎")
          .gender("男性")
          .studentCode("1003")
          .seatRow(2)
          .seatColumn(3)
          .build();

      Student savedStudentSeat23 = Student.builder()
          .id(3L)
          .name("山田次郎")
          .gender("男性")
          .studentCode("1003")
          .seatRow(2)
          .seatColumn(3)
          .build();

      when(studentRepository.findBySeatRowAndSeatColumn(2, 3)).thenReturn(null);
      when(studentRepository.save(studentForSeat23)).thenReturn(savedStudentSeat23);

      // When
      Student result = studentService.registerStudent(studentForSeat23);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.getId()).isEqualTo(3L);
      assertThat(result.getSeatRow()).isEqualTo(2);
      assertThat(result.getSeatColumn()).isEqualTo(3);

      verify(studentRepository).findBySeatRowAndSeatColumn(2, 3);
      verify(studentRepository).save(studentForSeat23);
    }

    @Test
    @DisplayName("最小値と最大値の座席での登録が正常に完了すること")
    void registerStudent_境界値での座席登録() {
      // Given - 最小値（1,1）
      Student minSeatStudent = Student.builder()
          .name("最小座席")
          .gender("男性")
          .studentCode("0001")
          .seatRow(1)
          .seatColumn(1)
          .build();

      Student savedMinSeatStudent = Student.builder()
          .id(10L)
          .name("最小座席")
          .gender("男性")
          .studentCode("0001")
          .seatRow(1)
          .seatColumn(1)
          .build();

      when(studentRepository.findBySeatRowAndSeatColumn(1, 1)).thenReturn(null);
      when(studentRepository.save(minSeatStudent)).thenReturn(savedMinSeatStudent);

      // When
      Student result = studentService.registerStudent(minSeatStudent);

      // Then
      assertThat(result.getSeatRow()).isEqualTo(1);
      assertThat(result.getSeatColumn()).isEqualTo(1);

      // Given - 最大値（6,5）
      Student maxSeatStudent = Student.builder()
          .name("最大座席")
          .gender("女性")
          .studentCode("0002")
          .seatRow(6)
          .seatColumn(5)
          .build();

      Student savedMaxSeatStudent = Student.builder()
          .id(11L)
          .name("最大座席")
          .gender("女性")
          .studentCode("0002")
          .seatRow(6)
          .seatColumn(5)
          .build();

      when(studentRepository.findBySeatRowAndSeatColumn(6, 5)).thenReturn(null);
      when(studentRepository.save(maxSeatStudent)).thenReturn(savedMaxSeatStudent);

      // When
      Student maxResult = studentService.registerStudent(maxSeatStudent);

      // Then
      assertThat(maxResult.getSeatRow()).isEqualTo(6);
      assertThat(maxResult.getSeatColumn()).isEqualTo(5);
    }
  }

  @Nested
  @DisplayName("正常系テスト")
  class SuccessTests2 {

    @Test
    @DisplayName("存在するIDを指定した場合、対応する生徒が取得できること")
    void getStudentById_存在するIDの場合() {
      // Given
      Long studentId = 1L;
      when(studentRepository.findById(studentId)).thenReturn(Optional.of(existingStudent));

      // When
      Student result = studentService.getStudentById(studentId);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.getId()).isEqualTo(1L);
      assertThat(result.getName()).isEqualTo("佐藤花子");
      assertThat(result.getGender()).isEqualTo("女性");
      assertThat(result.getStudentCode()).isEqualTo("1002");
      assertThat(result.getSeatRow()).isEqualTo(1);
      assertThat(result.getSeatColumn()).isEqualTo(1);

      verify(studentRepository).findById(studentId);
    }

    @Test
    @DisplayName("異なる存在するIDでも正常に取得できること")
    void getStudentById_異なるIDの場合() {
      // Given
      Long studentId = 100L;
      Student differentStudent = Student.builder()
          .id(100L)
          .name("佐藤花子")
          .gender("女性")
          .studentCode("2001")
          .seatRow(3)
          .seatColumn(2)
          .build();

      when(studentRepository.findById(studentId)).thenReturn(Optional.of(differentStudent));

      // When
      Student result = studentService.getStudentById(studentId);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.getId()).isEqualTo(100L);
      assertThat(result.getName()).isEqualTo("佐藤花子");
      assertThat(result.getGender()).isEqualTo("女性");
      assertThat(result.getStudentCode()).isEqualTo("2001");

      verify(studentRepository).findById(studentId);
    }

    @Test
    @DisplayName("最小値のID（1）で正常に取得できること")
    void getStudentById_最小値のID() {
      // Given
      Long minId = 1L;
      when(studentRepository.findById(minId)).thenReturn(Optional.of(existingStudent));

      // When
      Student result = studentService.getStudentById(minId);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.getId()).isEqualTo(1L);

      verify(studentRepository).findById(minId);
    }

    @Test
    @DisplayName("大きなID値でも正常に取得できること")
    void getStudentById_大きなID値() {
      // Given
      Long largeId = 999999L;
      Student studentWithLargeId = Student.builder()
          .id(largeId)
          .name("大きなID生徒")
          .gender("男性")
          .studentCode("999999")
          .seatRow(6)
          .seatColumn(5)
          .build();

      when(studentRepository.findById(largeId)).thenReturn(Optional.of(studentWithLargeId));

      // When
      Student result = studentService.getStudentById(largeId);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.getId()).isEqualTo(999999L);

      verify(studentRepository).findById(largeId);
    }
  }

  @Nested
  @DisplayName("異常系テスト - データが見つからない場合")
  class NotFoundTests {

    @Test
    @DisplayName("存在しないIDを指定した場合、nullが返されること")
    void getStudentById_存在しないIDの場合() {
      // Given
      Long nonExistentId = 999L;
      when(studentRepository.findById(nonExistentId)).thenReturn(Optional.empty());

      // When
      Student result = studentService.getStudentById(nonExistentId);

      // Then
      assertThat(result).isNull();

      verify(studentRepository).findById(nonExistentId);
    }

    @Test
    @DisplayName("負のIDを指定した場合、nullが返されること")
    void getStudentById_負のIDの場合() {
      // Given
      Long negativeId = -1L;
      when(studentRepository.findById(negativeId)).thenReturn(Optional.empty());

      // When
      Student result = studentService.getStudentById(negativeId);

      // Then
      assertThat(result).isNull();

      verify(studentRepository).findById(negativeId);
    }

    @Test
    @DisplayName("ゼロのIDを指定した場合、nullが返されること")
    void getStudentById_ゼロのIDの場合() {
      // Given
      Long zeroId = 0L;
      when(studentRepository.findById(zeroId)).thenReturn(Optional.empty());

      // When
      Student result = studentService.getStudentById(zeroId);

      // Then
      assertThat(result).isNull();

      verify(studentRepository).findById(zeroId);
    }

    @Test
    @DisplayName("非常に大きなIDを指定した場合、nullが返されること")
    void getStudentById_非常に大きなIDの場合() {
      // Given
      Long veryLargeId = Long.MAX_VALUE;
      when(studentRepository.findById(veryLargeId)).thenReturn(Optional.empty());

      // When
      Student result = studentService.getStudentById(veryLargeId);

      // Then
      assertThat(result).isNull();

      verify(studentRepository).findById(veryLargeId);
    }
  }

  @Nested
  @DisplayName("エッジケーステスト")
  class EdgeCaseTests {

    @Test
    @DisplayName("repositoryから取得したOptionalがそのまま処理されること")
    void getStudentById_Optionalの処理確認() {
      // Given
      Long testId = 50L;
      when(studentRepository.findById(testId)).thenReturn(Optional.of(existingStudent));

      // When
      Student result = studentService.getStudentById(testId);

      // Then
      // Optional.of()で包まれた値が正しくorElse(null)で取り出されること
      assertThat(result).isSameAs(existingStudent);

      verify(studentRepository).findById(testId);
    }

    @Test
    @DisplayName("空のOptionalがnullに正しく変換されること")
    void getStudentById_空のOptionalの処理確認() {
      // Given
      Long testId = 51L;
      when(studentRepository.findById(testId)).thenReturn(Optional.empty());

      // When
      Student result = studentService.getStudentById(testId);

      // Then
      // Optional.empty()が正しくorElse(null)でnullに変換されること
      assertThat(result).isNull();

      verify(studentRepository).findById(testId);
    }

    @Test
    @DisplayName("repositoryのfindByIdが一度だけ呼ばれること")
    void getStudentById_repositoryメソッド呼び出し回数確認() {
      // Given
      Long testId = 123L;
      when(studentRepository.findById(testId)).thenReturn(Optional.of(existingStudent));

      // When
      studentService.getStudentById(testId);

      // Then
      // findByIdが正確に1回だけ呼ばれることを確認
      verify(studentRepository).findById(testId);
    }

    @Test
    @DisplayName("同じIDで複数回呼び出しても正しく動作すること")
    void getStudentById_複数回呼び出し() {
      // Given
      Long testId = 456L;
      when(studentRepository.findById(testId)).thenReturn(Optional.of(existingStudent));

      // When
      Student result1 = studentService.getStudentById(testId);
      Student result2 = studentService.getStudentById(testId);

      // Then
      assertThat(result1).isEqualTo(result2);
      assertThat(result1).isSameAs(existingStudent);
      assertThat(result2).isSameAs(existingStudent);

      // 2回呼び出されたことを確認
      verify(studentRepository, times(2)).findById(testId);
    }
  }

  // -------------------
  // deleteStudent のテスト
  // -------------------

  @Test
  @DisplayName("deleteStudent 正常系: ID指定で削除が呼ばれる")
  void testDeleteStudent_Normal() {
    Long id = 1L;

    // 実行
    studentService.deleteStudent(id);

    // deleteById が1回呼ばれたことを検証
    verify(studentRepository, times(1)).deleteById(id);
  }

  @Test
  @DisplayName("deleteStudent 異常系: 存在しないIDを削除しようとしても例外をスロー")
  void testDeleteStudent_Exception() {
    Long id = 999L;
    doThrow(new IllegalArgumentException("存在しないID"))
        .when(studentRepository).deleteById(id);

    Exception ex = assertThrows(IllegalArgumentException.class, () -> {
      studentService.deleteStudent(id);
    });

    assertEquals("存在しないID", ex.getMessage());
    verify(studentRepository, times(1)).deleteById(id);
  }

  // -------------------
  // isStudentCodeDuplicate のテスト
  // -------------------

  @Test
  @DisplayName("isStudentCodeDuplicate 正常系: 学籍番号が存在する場合 true を返す")
  void testIsStudentCodeDuplicate_True() {
    String studentCode = "1001";
    when(studentRepository.findByStudentCode(studentCode))
        .thenReturn(Optional.of(new Student()));

    boolean result = studentService.isStudentCodeDuplicate(studentCode);

    assertTrue(result);
    verify(studentRepository, times(1)).findByStudentCode(studentCode);
  }

  @Test
  @DisplayName("isStudentCodeDuplicate 正常系: 学籍番号が存在しない場合 false を返す")
  void testIsStudentCodeDuplicate_False() {
    String studentCode = "2002";
    when(studentRepository.findByStudentCode(studentCode))
        .thenReturn(Optional.empty());

    boolean result = studentService.isStudentCodeDuplicate(studentCode);

    Assertions.assertFalse(result);
    verify(studentRepository, times(1)).findByStudentCode(studentCode);
  }

  @Test
  @DisplayName("isStudentCodeDuplicate 異常系: Repositoryで例外発生時にそのまま例外をスロー")
  void testIsStudentCodeDuplicate_Exception() {
    String studentCode = "3003";
    when(studentRepository.findByStudentCode(studentCode))
        .thenThrow(new RuntimeException("DBエラー"));

    RuntimeException ex = assertThrows(RuntimeException.class, () -> {
      studentService.isStudentCodeDuplicate(studentCode);
    });

    assertEquals("DBエラー", ex.getMessage());
    verify(studentRepository, times(1)).findByStudentCode(studentCode);
  }
}
