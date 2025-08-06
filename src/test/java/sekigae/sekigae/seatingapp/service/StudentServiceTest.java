package sekigae.sekigae.seatingapp.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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
}