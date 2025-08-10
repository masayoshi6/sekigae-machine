package sekigae.sekigae.seatingapp.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import sekigae.sekigae.seatingapp.entity.Student;
import sekigae.sekigae.seatingapp.service.SeatingService;
import sekigae.sekigae.seatingapp.service.StudentService;

@ExtendWith(MockitoExtension.class)
@DisplayName("HomeController テスト")
class HomeControllerTest {

  @Mock
  private StudentService studentService;

  @Mock
  private SeatingService seatingService;

  @InjectMocks
  private HomeController homeController;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(homeController).build();
  }

  @Nested
  @DisplayName("home メソッドのテスト")
  class HomeMethodTest {

    @Test
    @DisplayName("正常系: 生徒データが存在する場合")
    void testHome_WithStudents_Success() throws Exception {
      // テストデータの準備
      List<Student> students = Arrays.asList(
          createStudent(1L, "田中太郎", "男子", "001", 1, 1),
          createStudent(2L, "佐藤花子", "女子", "002", 1, 2),
          createStudent(3L, "山田次郎", "男子", "003", 2, 1)
      );

      String[][] seatingChart = {
          {"田中太郎", "佐藤花子", null, null, null},
          {"山田次郎", null, null, null, null},
          {null, null, null, null, null},
          {null, null, null, null, null},
          {null, null, null, null, null},
          {null, null, null, null, null}
      };

      // モックの設定
      when(studentService.getAllStudents()).thenReturn(students);
      when(seatingService.getCurrentRows()).thenReturn(6);
      when(seatingService.getCurrentColumns()).thenReturn(5);
      when(seatingService.getSeatingChart(6, 5)).thenReturn(seatingChart);

      // テスト実行
      mockMvc.perform(get("/"))
          .andExpect(status().isOk())
          .andExpect(view().name("home/dashboard"))
          .andExpect(model().attribute("studentCount", 3))
          .andExpect(model().attribute("currentRows", 6))
          .andExpect(model().attribute("currentColumns", 5))
          .andExpect(model().attribute("occupiedSeats", 3));

      // メソッド呼び出し回数の検証
      verify(studentService, times(1)).getAllStudents();
      verify(seatingService, times(1)).getCurrentRows();
      verify(seatingService, times(1)).getCurrentColumns();
      verify(seatingService, times(1)).getSeatingChart(6, 5);
    }

    @Test
    @DisplayName("正常系: 生徒データが空の場合")
    void testHome_WithNoStudents_Success() throws Exception {
      // テストデータの準備
      List<Student> emptyStudents = Collections.emptyList();
      String[][] emptySeatingChart = new String[6][5]; // 全てnull

      // モックの設定
      when(studentService.getAllStudents()).thenReturn(emptyStudents);
      when(seatingService.getCurrentRows()).thenReturn(6);
      when(seatingService.getCurrentColumns()).thenReturn(5);
      when(seatingService.getSeatingChart(6, 5)).thenReturn(emptySeatingChart);

      // テスト実行
      mockMvc.perform(get("/"))
          .andExpect(status().isOk())
          .andExpect(view().name("home/dashboard"))
          .andExpect(model().attribute("studentCount", 0))
          .andExpect(model().attribute("currentRows", 6))
          .andExpect(model().attribute("currentColumns", 5))
          .andExpect(model().attribute("occupiedSeats", 0));

      // メソッド呼び出し回数の検証
      verify(studentService, times(1)).getAllStudents();
      verify(seatingService, times(1)).getCurrentRows();
      verify(seatingService, times(1)).getCurrentColumns();
      verify(seatingService, times(1)).getSeatingChart(6, 5);
    }

    @Test
    @DisplayName("正常系: 部分的に座席が埋まっている場合")
    void testHome_WithPartiallyOccupiedSeats_Success() throws Exception {
      // テストデータの準備
      List<Student> students = Arrays.asList(
          createStudent(1L, "田中太郎", "男子", "001", 1, 1),
          createStudent(2L, "佐藤花子", "女子", "002", 3, 3)
      );

      String[][] seatingChart = {
          {"田中太郎", null, null, null, null},
          {null, null, null, null, null},
          {null, null, "佐藤花子", null, null},
          {null, null, null, null, null},
          {null, null, null, null, null},
          {null, null, null, null, null}
      };

      // モックの設定
      when(studentService.getAllStudents()).thenReturn(students);
      when(seatingService.getCurrentRows()).thenReturn(6);
      when(seatingService.getCurrentColumns()).thenReturn(5);
      when(seatingService.getSeatingChart(6, 5)).thenReturn(seatingChart);

      // テスト実行
      mockMvc.perform(get("/"))
          .andExpect(status().isOk())
          .andExpect(view().name("home/dashboard"))
          .andExpect(model().attribute("studentCount", 2))
          .andExpect(model().attribute("currentRows", 6))
          .andExpect(model().attribute("currentColumns", 5))
          .andExpect(model().attribute("occupiedSeats", 2));
    }

    @Test
    @DisplayName("正常系: 空文字列が含まれる座席表の場合")
    void testHome_WithEmptyStringSeats_Success() throws Exception {
      // テストデータの準備
      List<Student> students = Arrays.asList(
          createStudent(1L, "田中太郎", "男子", "001", 1, 1)
      );

      String[][] seatingChart = {
          {"田中太郎", "", "   ", null, null},
          {null, null, null, null, null},
          {null, null, null, null, null},
          {null, null, null, null, null},
          {null, null, null, null, null},
          {null, null, null, null, null}
      };

      // モックの設定
      when(studentService.getAllStudents()).thenReturn(students);
      when(seatingService.getCurrentRows()).thenReturn(6);
      when(seatingService.getCurrentColumns()).thenReturn(5);
      when(seatingService.getSeatingChart(6, 5)).thenReturn(seatingChart);

      // テスト実行
      mockMvc.perform(get("/"))
          .andExpect(status().isOk())
          .andExpect(view().name("home/dashboard"))
          .andExpect(model().attribute("studentCount", 1))
          .andExpect(model().attribute("currentRows", 6))
          .andExpect(model().attribute("currentColumns", 5))
          .andExpect(model().attribute("occupiedSeats", 1)); // 空文字・空白文字はカウントしない
    }

    @Test
    @DisplayName("正常系: カスタム行列数の場合")
    void testHome_WithCustomRowsAndColumns_Success() throws Exception {
      // テストデータの準備
      List<Student> students = Arrays.asList(
          createStudent(1L, "田中太郎", "男子", "001", 1, 1)
      );

      String[][] customSeatingChart = {
          {"田中太郎", null, null},
          {null, null, null},
          {null, null, null},
          {null, null, null}
      };

      // モックの設定
      when(studentService.getAllStudents()).thenReturn(students);
      when(seatingService.getCurrentRows()).thenReturn(4);
      when(seatingService.getCurrentColumns()).thenReturn(3);
      when(seatingService.getSeatingChart(4, 3)).thenReturn(customSeatingChart);

      // テスト実行
      mockMvc.perform(get("/"))
          .andExpect(status().isOk())
          .andExpect(view().name("home/dashboard"))
          .andExpect(model().attribute("studentCount", 1))
          .andExpect(model().attribute("currentRows", 4))
          .andExpect(model().attribute("currentColumns", 3))
          .andExpect(model().attribute("occupiedSeats", 1));

      // メソッド呼び出しの検証
      verify(seatingService, times(1)).getSeatingChart(4, 3);
    }


    @Test
    @DisplayName("異常系: nullの座席表が返された場合（シンプルバージョン）")
    void testHome_NullSeatingChart_ThrowsException_Simple() throws Exception {
      // テストデータの準備
      List<Student> students = Arrays.asList(
          createStudent(1L, "田中太郎", "男子", "001", 1, 1)
      );

      // モックの設定 - nullの座席表
      when(studentService.getAllStudents()).thenReturn(students);
      when(seatingService.getCurrentRows()).thenReturn(6);
      when(seatingService.getCurrentColumns()).thenReturn(5);
      when(seatingService.getSeatingChart(6, 5)).thenReturn(null);

      // テスト実行 - 例外が発生することを確認
      Exception exception = assertThrows(Exception.class, () -> {
        mockMvc.perform(get("/"));
      });

      // 例外チェーン内にNullPointerExceptionがあることを確認
      Throwable cause = exception;
      boolean foundNPE = false;
      while (cause != null) {
        if (cause instanceof NullPointerException) {
          foundNPE = true;
          assertThat(cause.getMessage()).contains("Cannot read the array length");
          break;
        }
        cause = cause.getCause();
      }
      assertThat(foundNPE).as("NullPointerExceptionが発生するはずです").isTrue();

      // メソッド呼び出し回数の検証
      verify(studentService, times(1)).getAllStudents();
      verify(seatingService, times(1)).getCurrentRows();
      verify(seatingService, times(1)).getCurrentColumns();
      verify(seatingService, times(1)).getSeatingChart(6, 5);
    }
  }

  /**
   * テスト用のStudentオブジェクトを作成するヘルパーメソッド
   */
  private Student createStudent(Long id, String name, String gender, String studentCode,
      Integer seatRow, Integer seatColumn) {
    return Student.builder()
        .id(id)
        .name(name)
        .gender(gender)
        .studentCode(studentCode)
        .seatRow(seatRow)
        .seatColumn(seatColumn)
        .build();
  }
}