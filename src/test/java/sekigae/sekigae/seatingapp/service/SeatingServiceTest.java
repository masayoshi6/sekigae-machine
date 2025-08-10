package sekigae.sekigae.seatingapp.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
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
@DisplayName("SeatingService単体テスト")
class SeatingServiceTest {

  @Mock
  private StudentService studentService;

  @Mock
  private StudentRepository studentRepository;

  @InjectMocks
  private SeatingService seatingService;

  private List<Student> testStudents;
  private Student student1;
  private Student student2;
  private Student student3;

  @BeforeEach
  void setUp() {
    // テスト用の学生データを準備
    student1 = Student.builder()
        .id(1L)
        .name("田中太郎")
        .gender("男子")
        .studentCode("1")
        .seatRow(1)
        .seatColumn(1)
        .build();

    student2 = Student.builder()
        .id(2L)
        .name("佐藤花子")
        .gender("女子")
        .studentCode("2")
        .seatRow(1)
        .seatColumn(2)
        .build();

    student3 = Student.builder()
        .id(3L)
        .name("鈴木次郎")
        .gender("男子")
        .studentCode("3")
        .seatRow(2)
        .seatColumn(1)
        .build();

    testStudents = Arrays.asList(student1, student2, student3);
  }

  @Test
  @DisplayName("getSeatingChart - 正常系：生徒が正しく配置される")
  void getSeatingChart_Success() {
    // given
    when(studentService.getAllStudents()).thenReturn(testStudents);

    // when
    String[][] result = seatingService.getSeatingChart(3, 3);

    // then
    assertEquals("田中太郎", result[0][0]);
    assertEquals("佐藤花子", result[0][1]);
    assertEquals("鈴木次郎", result[1][0]);
    assertNull(result[0][2]);
    assertNull(result[1][1]);
    assertNull(result[2][0]);
  }

  @Test
  @DisplayName("getSeatingChart - 正常系：座席位置がnullの学生は配置されない")
  void getSeatingChart_WithNullSeatPosition() {
    // given
    Student studentWithNullSeat = Student.builder()
        .id(4L)
        .name("山田三郎")
        .gender("男子")
        .studentCode("4")
        .seatRow(null)
        .seatColumn(null)
        .build();
    List<Student> studentsWithNull = Arrays.asList(student1, studentWithNullSeat);
    when(studentService.getAllStudents()).thenReturn(studentsWithNull);

    // when
    String[][] result = seatingService.getSeatingChart(3, 3);

    // then
    assertEquals("田中太郎", result[0][0]);
    for (int i = 0; i < 3; i++) {
      for (int j = 0; j < 3; j++) {
        if (i == 0 && j == 0) {
          continue;
        }
        assertNull(result[i][j]);
      }
    }
  }

  @Test
  @DisplayName("getSeatingChart - 異常系：範囲外の座席位置の学生は配置されない")
  void getSeatingChart_OutOfRange() {
    // given
    Student outOfRangeStudent = Student.builder()
        .id(4L)
        .name("範囲外太郎")
        .gender("男子")
        .studentCode("4")
        .seatRow(5) // 範囲外
        .seatColumn(5) // 範囲外
        .build();
    List<Student> studentsWithOutOfRange = Arrays.asList(student1, outOfRangeStudent);
    when(studentService.getAllStudents()).thenReturn(studentsWithOutOfRange);

    // when
    String[][] result = seatingService.getSeatingChart(2, 2);

    // then
    assertEquals("田中太郎", result[0][0]);
    assertNull(result[0][1]);
    assertNull(result[1][0]);
    assertNull(result[1][1]);
  }

  @Test
  @DisplayName("getSeatingChart - 異常系：学生リストが空の場合")
  void getSeatingChart_EmptyStudentList() {
    // given
    when(studentService.getAllStudents()).thenReturn(Collections.emptyList());

    // when
    String[][] result = seatingService.getSeatingChart(3, 3);

    // then
    for (int i = 0; i < 3; i++) {
      for (int j = 0; j < 3; j++) {
        assertNull(result[i][j]);
      }
    }
  }

  @Test
  @DisplayName("getCurrentRows - 正常系：デフォルト値6を返す")
  void getCurrentRows_ReturnsDefaultValue() {
    // when
    int result = seatingService.getCurrentRows();

    // then
    assertEquals(6, result);
  }

  @Test
  @DisplayName("getCurrentColumns - 正常系：デフォルト値5を返す")
  void getCurrentColumns_ReturnsDefaultValue() {
    // when
    int result = seatingService.getCurrentColumns();

    // then
    assertEquals(5, result);
  }

  @Test
  @DisplayName("saveSeatingConfiguration - 正常系：処理が完了する")
  void saveSeatingConfiguration_Success() {
    // when & then（例外が発生しないことを確認）
    assertDoesNotThrow(() -> seatingService.saveSeatingConfiguration(6, 5));
  }

  @Test
  @DisplayName("regenerateSeatingChart - 正常系：処理が完了する")
  void regenerateSeatingChart_Success() {
    // when & then（例外が発生しないことを確認）
    assertDoesNotThrow(() -> seatingService.regenerateSeatingChart(6, 5));
  }

  @Test
  @DisplayName("getSeatingChartWithGender - 正常系：学生オブジェクトが正しく配置される")
  void getSeatingChartWithGender_Success() {
    // given
    when(studentService.getAllStudents()).thenReturn(testStudents);

    // when
    Student[][] result = seatingService.getSeatingChartWithGender(3, 3);

    // then
    assertEquals(student1, result[0][0]);
    assertEquals(student2, result[0][1]);
    assertEquals(student3, result[1][0]);
    assertNull(result[0][2]);
    assertNull(result[1][1]);
    assertNull(result[2][0]);
  }

  @Test
  @DisplayName("getSeatingChartWithGender - 異常系：範囲外の座席位置")
  void getSeatingChartWithGender_OutOfRange() {
    // given
    Student outOfRangeStudent = Student.builder()
        .id(4L)
        .name("範囲外太郎")
        .gender("男子")
        .studentCode("4")
        .seatRow(-1) // 負の値
        .seatColumn(10) // 範囲外
        .build();
    List<Student> studentsWithOutOfRange = Arrays.asList(student1, outOfRangeStudent);
    when(studentService.getAllStudents()).thenReturn(studentsWithOutOfRange);

    // when
    Student[][] result = seatingService.getSeatingChartWithGender(2, 2);

    // then
    assertEquals(student1, result[0][0]);
    assertNull(result[0][1]);
    assertNull(result[1][0]);
    assertNull(result[1][1]);
  }

  @Test
  @DisplayName("shuffleSeatingChart - 正常系：引数1つ版")
  void shuffleSeatingChart_OneParam_Success() {
    // given
    when(studentRepository.findAll()).thenReturn(testStudents);

    // when
    Student[][] result = seatingService.shuffleSeatingChart(3, 3);

    // then
    assertNotNull(result);
    assertEquals(3, result.length);
    assertEquals(3, result[0].length);
    verify(studentRepository,
        times(2)).findAll(); // shuffleSeatingChartで1回、updateSeatPositionsInDatabaseで1回
    verify(studentRepository).saveAll(anyList());
  }

  @Test
  @DisplayName("shuffleSeatingChart - 正常系：引数2つ版（通常配置）")
  void shuffleSeatingChart_TwoParam_NormalArrangement() {
    // given
    when(studentRepository.findAll()).thenReturn(testStudents);

    // when
    Student[][] result = seatingService.shuffleSeatingChart(3, 3, false);

    // then
    assertNotNull(result);
    assertEquals(3, result.length);
    assertEquals(3, result[0].length);
    verify(studentRepository,
        times(2)).findAll(); // shuffleSeatingChartで1回、updateSeatPositionsInDatabaseで1回
    verify(studentRepository).saveAll(anyList());
  }

  @Test
  @DisplayName("shuffleSeatingChart - 正常系：男女交互配置")
  void shuffleSeatingChart_AlternateGenders() {
    // given
    List<Student> mixedStudents = Arrays.asList(
        createMaleStudent(1L, "男子1", "1", null, null),
        createFemaleStudent(2L, "女子1", "2", null, null),
        createMaleStudent(3L, "男子2", "3", null, null),
        createFemaleStudent(4L, "女子2", "4", null, null)
    );
    when(studentRepository.findAll()).thenReturn(mixedStudents);

    // when
    Student[][] result = seatingService.shuffleSeatingChart(2, 2, true);

    // then
    assertNotNull(result);
    assertEquals(2, result.length);
    assertEquals(2, result[0].length);
    verify(studentRepository, times(2)).findAll();
    verify(studentRepository).saveAll(anyList());
  }

  @Test
  @DisplayName("shuffleSeatingChart - 正常系：列単位で男女分離")
  void shuffleSeatingChart_AlternateColumns() {
    // given
    List<Student> mixedStudents = Arrays.asList(
        createMaleStudent(1L, "男子1", "1", null, null),
        createFemaleStudent(2L, "女子1", "2", null, null),
        createMaleStudent(3L, "男子2", "3", null, null),
        createFemaleStudent(4L, "女子2", "4", null, null)
    );
    when(studentRepository.findAll()).thenReturn(mixedStudents);

    // when
    Student[][] result = seatingService.shuffleSeatingChart(2, 2, false, true);

    // then
    assertNotNull(result);
    assertEquals(2, result.length);
    assertEquals(2, result[0].length);
    verify(studentRepository, times(2)).findAll();
    verify(studentRepository).saveAll(anyList());
  }

  @Test
  @DisplayName("shuffleSeatingChart - 異常系：学生がいない場合")
  void shuffleSeatingChart_EmptyStudentList() {
    // given
    when(studentRepository.findAll()).thenReturn(Collections.emptyList());

    // when
    Student[][] result = seatingService.shuffleSeatingChart(3, 3);

    // then
    assertNotNull(result);
    assertEquals(3, result.length);
    assertEquals(3, result[0].length);
    for (int i = 0; i < 3; i++) {
      for (int j = 0; j < 3; j++) {
        assertNull(result[i][j]);
      }
    }
    verify(studentRepository, times(2)).findAll();
    verify(studentRepository).saveAll(anyList());
  }

  @Test
  @DisplayName("shuffleSeatingChart - 異常系：座席数より学生数が多い場合")
  void shuffleSeatingChart_MoreStudentsThanSeats() {
    // given
    List<Student> manyStudents = new ArrayList<>();
    for (int i = 1; i <= 10; i++) {
      manyStudents.add(createMaleStudent((long) i, "学生" + i, String.valueOf(i), null, null));
    }
    when(studentRepository.findAll()).thenReturn(manyStudents);

    // when
    Student[][] result = seatingService.shuffleSeatingChart(2, 2); // 4席しかない

    // then
    assertNotNull(result);
    int assignedStudents = 0;
    for (int i = 0; i < 2; i++) {
      for (int j = 0; j < 2; j++) {
        if (result[i][j] != null) {
          assignedStudents++;
        }
      }
    }
    assertEquals(4, assignedStudents); // 座席数分だけ配置される
    verify(studentRepository, times(2)).findAll();
  }

  @Test
  @DisplayName("shuffleSeatingChart - 正常系：男子のみの場合")
  void shuffleSeatingChart_MaleStudentsOnly() {
    // given
    List<Student> maleOnlyStudents = Arrays.asList(
        createMaleStudent(1L, "男子1", "1", null, null),
        createMaleStudent(2L, "男子2", "2", null, null),
        createMaleStudent(3L, "男子3", "3", null, null)
    );
    when(studentRepository.findAll()).thenReturn(maleOnlyStudents);

    // when
    Student[][] result = seatingService.shuffleSeatingChart(2, 2, true);

    // then
    assertNotNull(result);
    int assignedStudents = 0;
    for (int i = 0; i < 2; i++) {
      for (int j = 0; j < 2; j++) {
        if (result[i][j] != null) {
          assignedStudents++;
          assertEquals("男子", result[i][j].getGender());
        }
      }
    }
    assertTrue(assignedStudents > 0);
    verify(studentRepository, times(2)).findAll();
  }

  @Test
  @DisplayName("shuffleSeatingChart - 正常系：女子のみの場合")
  void shuffleSeatingChart_FemaleStudentsOnly() {
    // given
    List<Student> femaleOnlyStudents = Arrays.asList(
        createFemaleStudent(1L, "女子1", "1", null, null),
        createFemaleStudent(2L, "女子2", "2", null, null),
        createFemaleStudent(3L, "女子3", "3", null, null)
    );
    when(studentRepository.findAll()).thenReturn(femaleOnlyStudents);

    // when
    Student[][] result = seatingService.shuffleSeatingChart(2, 2, false, true);

    // then
    assertNotNull(result);
    int assignedStudents = 0;
    for (int i = 0; i < 2; i++) {
      for (int j = 0; j < 2; j++) {
        if (result[i][j] != null) {
          assignedStudents++;
          assertEquals("女子", result[i][j].getGender());
        }
      }
    }
    assertTrue(assignedStudents > 0);
    verify(studentRepository, times(2)).findAll();
  }

  @Test
  @DisplayName("shuffleSeatingChart - 境界値：1x1の座席表")
  void shuffleSeatingChart_SingleSeat() {
    // given
    when(studentRepository.findAll()).thenReturn(Arrays.asList(student1));

    // when
    Student[][] result = seatingService.shuffleSeatingChart(1, 1);

    // then
    assertNotNull(result);
    assertEquals(1, result.length);
    assertEquals(1, result[0].length);
    assertEquals(student1, result[0][0]);
    verify(studentRepository, times(2)).findAll();
  }

  @Test
  @DisplayName("shuffleSeatingChart - 境界値：0x0の座席表")
  void shuffleSeatingChart_ZeroSeat() {
    // given
    when(studentRepository.findAll()).thenReturn(testStudents);

    // when
    Student[][] result = seatingService.shuffleSeatingChart(0, 0);

    // then
    assertNotNull(result);
    assertEquals(0, result.length);
    verify(studentRepository, times(2)).findAll();
  }

  @Test
  @DisplayName("shuffleSeatingChart - 異常系：repositoryでエラーが発生")
  void shuffleSeatingChart_RepositoryError() {
    // given
    when(studentRepository.findAll()).thenThrow(new RuntimeException("データベースエラー"));

    // when & then
    assertThrows(RuntimeException.class, () -> {
      seatingService.shuffleSeatingChart(3, 3);
    });
  }

  // ヘルパーメソッド
  private Student createMaleStudent(Long id, String name, String code, Integer row, Integer col) {
    return Student.builder()
        .id(id)
        .name(name)
        .gender("男子")
        .studentCode(code)
        .seatRow(row)
        .seatColumn(col)
        .build();
  }

  private Student createFemaleStudent(Long id, String name, String code, Integer row, Integer col) {
    return Student.builder()
        .id(id)
        .name(name)
        .gender("女子")
        .studentCode(code)
        .seatRow(row)
        .seatColumn(col)
        .build();
  }
}