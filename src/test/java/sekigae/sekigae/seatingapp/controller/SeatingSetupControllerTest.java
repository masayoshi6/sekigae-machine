package sekigae.sekigae.seatingapp.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import jakarta.servlet.ServletException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import sekigae.sekigae.seatingapp.entity.SeatingConfigurationForm;
import sekigae.sekigae.seatingapp.entity.Student;
import sekigae.sekigae.seatingapp.service.SeatingService;
import sekigae.sekigae.seatingapp.service.StudentService;

@ExtendWith(MockitoExtension.class)
@DisplayName("SeatingSetupController の単体テスト")
class SeatingSetupControllerTest {

  private MockMvc mockMvc;

  @Mock
  private SeatingService seatingService;

  @Mock
  private StudentService studentService;

  @InjectMocks
  private SeatingSetupController seatingSetupController;

  private List<Student> mockStudents;

  @BeforeEach
  void setUp() {
    // MockMvcの手動セットアップ
    InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
    viewResolver.setPrefix("/WEB-INF/views/");
    viewResolver.setSuffix(".html");

    mockMvc = MockMvcBuilders.standaloneSetup(seatingSetupController)
        .setViewResolvers(viewResolver)
        .build();
    // テストデータの準備
    Student student1 = Student.builder()
        .id(1L)
        .name("田中太郎")
        .gender("男子")
        .studentCode("1")
        .seatRow(1)
        .seatColumn(1)
        .build();

    Student student2 = Student.builder()
        .id(2L)
        .name("佐藤花子")
        .gender("女子")
        .studentCode("2")
        .seatRow(1)
        .seatColumn(2)
        .build();

    Student student3 = Student.builder()
        .id(3L)
        .name("鈴木次郎")
        .gender("男子")
        .studentCode("3")
        .seatRow(2)
        .seatColumn(1)
        .build();

    mockStudents = Arrays.asList(student1, student2, student3);
  }

  @Nested
  @DisplayName("GET /seating/setup - 座席表設定画面表示")
  class ShowSeatingSetupTest {

    @Test
    @DisplayName("正常系：座席表設定画面が正常に表示される")
    void showSeatingSetup_Success() throws Exception {
      // Given
      when(seatingService.getCurrentRows()).thenReturn(6);
      when(seatingService.getCurrentColumns()).thenReturn(5);
      when(studentService.getAllStudents()).thenReturn(mockStudents);

      // When & Then
      mockMvc.perform(get("/seating/setup"))
          .andExpect(status().isOk())
          .andExpect(view().name("seating/setup"))
          .andExpect(model().attributeExists("seatingConfigurationForm"))
          .andExpect(model().attribute("currentRows", 6))
          .andExpect(model().attribute("currentColumns", 5))
          .andExpect(model().attribute("totalStudents", 3))
          .andExpect(model().attribute("currentTotalSeats", 30));

      verify(seatingService).getCurrentRows();
      verify(seatingService).getCurrentColumns();
      verify(studentService).getAllStudents();
    }

    @Test
    @DisplayName("正常系：生徒が0人の場合でも正常に表示される")
    void showSeatingSetup_Success_WithNoStudents() throws Exception {
      // Given
      when(seatingService.getCurrentRows()).thenReturn(4);
      when(seatingService.getCurrentColumns()).thenReturn(4);
      when(studentService.getAllStudents()).thenReturn(Collections.emptyList());

      // When & Then
      mockMvc.perform(get("/seating/setup"))
          .andExpect(status().isOk())
          .andExpect(view().name("seating/setup"))
          .andExpect(model().attribute("totalStudents", 0))
          .andExpect(model().attribute("currentTotalSeats", 16));
    }

    @Test
    @DisplayName("正常系：多数の生徒がいる場合")
    void showSeatingSetup_Success_WithManyStudents() throws Exception {
      // Given - 大クラスの生徒データ（30人）
      List<Student> manyStudents = createLargeStudentList(30);
      when(seatingService.getCurrentRows()).thenReturn(6);
      when(seatingService.getCurrentColumns()).thenReturn(5);
      when(studentService.getAllStudents()).thenReturn(manyStudents);

      // When & Then
      mockMvc.perform(get("/seating/setup"))
          .andExpect(status().isOk())
          .andExpect(view().name("seating/setup"))
          .andExpect(model().attribute("totalStudents", 30))
          .andExpect(model().attribute("currentTotalSeats", 30));
    }

    @Test
    @DisplayName("異常系：SeatingServiceでRuntime例外が発生した場合")
    void showSeatingSetup_SeatingServiceException() throws Exception {
      // Given
      when(seatingService.getCurrentRows()).thenThrow(
          new RuntimeException("データベース接続エラー"));

      // When & Then
      ServletException exception = assertThrows(ServletException.class, () -> {
        mockMvc.perform(get("/seating/setup"));
      });

      assertThat(exception.getCause()).isInstanceOf(RuntimeException.class);
      assertThat(exception.getCause().getMessage()).isEqualTo("データベース接続エラー");
    }

    @Test
    @DisplayName("異常系：SeatingServiceが無効な値（0）を返した場合の完全テスト")
    void showSeatingSetup_ServiceReturnsZeroRows_CompleteTest() throws Exception {
      // Given
      when(seatingService.getCurrentRows()).thenReturn(0);
      when(seatingService.getCurrentColumns()).thenReturn(5);
      when(studentService.getAllStudents()).thenReturn(mockStudents);

      // When & Then
      mockMvc.perform(get("/seating/setup"))
          .andExpect(status().isOk())
          .andExpect(view().name("seating/setup"))
          .andExpect(model().attribute("currentRows", 0))
          .andExpect(model().attribute("currentColumns", 5))
          .andExpect(model().attribute("totalStudents", mockStudents.size()))
          .andExpect(model().attribute("currentTotalSeats", 0))
          .andExpect(model().attributeExists("seatingConfigurationForm"))
          .andDo(result -> {
            // SeatingConfigurationFormの初期値も確認
            SeatingConfigurationForm form =
                (SeatingConfigurationForm) result.getModelAndView().getModel()
                    .get("seatingConfigurationForm");
            assertThat(form.getRows()).isEqualTo(0);
            assertThat(form.getColumns()).isEqualTo(5);
          });
    }

    @Test
    @DisplayName("異常系：SeatingServiceが負の値を返した場合")
    void showSeatingSetup_ServiceReturnsNegativeValues() throws Exception {
      // Given
      when(seatingService.getCurrentRows()).thenReturn(-1);
      when(seatingService.getCurrentColumns()).thenReturn(-2);
      when(studentService.getAllStudents()).thenReturn(mockStudents);

      // When & Then
      mockMvc.perform(get("/seating/setup"))
          .andExpect(status().isOk())
          .andExpect(model().attribute("currentRows", -1))
          .andExpect(model().attribute("currentColumns", -2))
          .andExpect(model().attribute("currentTotalSeats", 2)); // -1 * -2 = 2
    }

    @Test
    @DisplayName("診断用：実際にモデルに設定されている属性を確認")
    void showSeatingSetup_DiagnoseModelAttributes() throws Exception {
      // Given
      when(seatingService.getCurrentRows()).thenReturn(0);
      when(seatingService.getCurrentColumns()).thenReturn(5);
      when(studentService.getAllStudents()).thenReturn(mockStudents);

      // When & Then
      mockMvc.perform(get("/seating/setup"))
          .andExpect(status().isOk())
          .andDo(result -> {
            Map<String, Object> model = result.getModelAndView().getModel();
            System.out.println("Model attributes:");
            model.forEach((key, value) ->
                System.out.println("  " + key + " = " + value + " (" +
                    (value != null ? value.getClass().getSimpleName() : "null") + ")"));
          });
    }
  }

  @Nested
  @DisplayName("POST /seating/setup - 座席表設定保存")
  class SaveSeatingConfigurationTest {

    @Test
    @DisplayName("正常系：有効な設定で保存が成功し座席表画面にリダイレクトされる")
    void saveSeatingConfiguration_Success() throws Exception {
      // Given
      doNothing().when(seatingService).saveSeatingConfiguration(anyInt(), anyInt());

      // When & Then
      mockMvc.perform(post("/seating/setup")
              .param("rows", "8")
              .param("columns", "6"))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/seating/chart?rows=8&columns=6"))
          .andExpect(flash().attribute("successMessage", "座席表設定を 8行 6列 に変更しました。"));

      verify(seatingService).saveSeatingConfiguration(8, 6);
    }

    @Test
    @DisplayName("正常系：最小値での設定保存")
    void saveSeatingConfiguration_Success_MinimumValues() throws Exception {
      // Given
      doNothing().when(seatingService).saveSeatingConfiguration(anyInt(), anyInt());

      // When & Then
      mockMvc.perform(post("/seating/setup")
              .param("rows", "1")
              .param("columns", "1"))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/seating/chart?rows=1&columns=1"))
          .andExpect(flash().attribute("successMessage", "座席表設定を 1行 1列 に変更しました。"));

      verify(seatingService).saveSeatingConfiguration(1, 1);
    }

    @Test
    @DisplayName("正常系：最大値での設定保存")
    void saveSeatingConfiguration_Success_MaximumValues() throws Exception {
      // Given
      doNothing().when(seatingService).saveSeatingConfiguration(anyInt(), anyInt());

      // When & Then
      mockMvc.perform(post("/seating/setup")
              .param("rows", "10")
              .param("columns", "10"))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/seating/chart?rows=10&columns=10"))
          .andExpect(flash().attribute("successMessage", "座席表設定を 10行 10列 に変更しました。"));

      verify(seatingService).saveSeatingConfiguration(10, 10);
    }

    @Test
    @DisplayName("異常系：バリデーションエラー - 行数が0の場合")
    void saveSeatingConfiguration_ValidationError_ZeroRows() throws Exception {
      // Given
      when(seatingService.getCurrentRows()).thenReturn(6);
      when(seatingService.getCurrentColumns()).thenReturn(5);
      when(studentService.getAllStudents()).thenReturn(mockStudents);

      // When & Then
      mockMvc.perform(post("/seating/setup")
              .param("rows", "0")
              .param("columns", "5"))
          .andExpect(status().isOk())
          .andExpect(view().name("seating/setup"))
          .andExpect(model().attributeHasFieldErrors("seatingConfigurationForm", "rows"))
          .andExpect(model().attribute("currentRows", 6))
          .andExpect(model().attribute("currentColumns", 5))
          .andExpect(model().attribute("totalStudents", 3));

      verify(seatingService, never()).saveSeatingConfiguration(anyInt(), anyInt());
    }

    @Test
    @DisplayName("異常系：バリデーションエラー - 列数が負の値の場合")
    void saveSeatingConfiguration_ValidationError_NegativeColumns() throws Exception {
      // Given
      when(seatingService.getCurrentRows()).thenReturn(6);
      when(seatingService.getCurrentColumns()).thenReturn(5);
      when(studentService.getAllStudents()).thenReturn(mockStudents);

      // When & Then
      mockMvc.perform(post("/seating/setup")
              .param("rows", "5")
              .param("columns", "-1"))
          .andExpect(status().isOk())
          .andExpect(view().name("seating/setup"))
          .andExpect(model().attributeHasFieldErrors("seatingConfigurationForm", "columns"));

      verify(seatingService, never()).saveSeatingConfiguration(anyInt(), anyInt());
    }

    @Test
    @DisplayName("異常系：バリデーションエラー - 行数と列数の両方が無効")
    void saveSeatingConfiguration_ValidationError_BothInvalid() throws Exception {
      // Given
      when(seatingService.getCurrentRows()).thenReturn(6);
      when(seatingService.getCurrentColumns()).thenReturn(5);
      when(studentService.getAllStudents()).thenReturn(mockStudents);

      // When & Then
      mockMvc.perform(post("/seating/setup")
              .param("rows", "0")
              .param("columns", "0"))
          .andExpect(status().isOk())
          .andExpect(view().name("seating/setup"))
          .andExpect(model().attributeHasFieldErrors("seatingConfigurationForm", "rows"))
          .andExpect(model().attributeHasFieldErrors("seatingConfigurationForm", "columns"));

      verify(seatingService, never()).saveSeatingConfiguration(anyInt(), anyInt());
    }

    @Test
    @DisplayName("異常系：バリデーションエラー - パラメータが未指定")
    void saveSeatingConfiguration_ValidationError_MissingParameters() throws Exception {
      // Given
      when(seatingService.getCurrentRows()).thenReturn(6);
      when(seatingService.getCurrentColumns()).thenReturn(5);
      when(studentService.getAllStudents()).thenReturn(mockStudents);

      // When & Then
      mockMvc.perform(post("/seating/setup"))
          .andExpect(status().isOk())
          .andExpect(view().name("seating/setup"))
          .andExpect(model().attributeHasFieldErrors("seatingConfigurationForm", "rows"))
          .andExpect(model().attributeHasFieldErrors("seatingConfigurationForm", "columns"));

      verify(seatingService, never()).saveSeatingConfiguration(anyInt(), anyInt());
    }

    @Test
    @DisplayName("異常系：数値以外の文字列が入力された場合")
    void saveSeatingConfiguration_ValidationError_NonNumericInput() throws Exception {
      // Given
      when(seatingService.getCurrentRows()).thenReturn(6);
      when(seatingService.getCurrentColumns()).thenReturn(5);
      when(studentService.getAllStudents()).thenReturn(mockStudents);

      // When & Then
      mockMvc.perform(post("/seating/setup")
              .param("rows", "abc")
              .param("columns", "xyz"))
          .andExpect(status().isOk())
          .andExpect(view().name("seating/setup"))
          .andExpect(model().attributeHasErrors("seatingConfigurationForm"));

      verify(seatingService, never()).saveSeatingConfiguration(anyInt(), anyInt());
    }

    @Test
    @DisplayName("異常系：極端に大きな数値が入力された場合")
    void saveSeatingConfiguration_ValidationError_TooLargeValues() throws Exception {
      // Given
      when(seatingService.getCurrentRows()).thenReturn(6);
      when(seatingService.getCurrentColumns()).thenReturn(5);
      when(studentService.getAllStudents()).thenReturn(mockStudents);

      // When & Then
      mockMvc.perform(post("/seating/setup")
              .param("rows", "999999")
              .param("columns", "999999"))
          .andExpect(status().isOk())
          .andExpect(view().name("seating/setup"))
          .andExpect(model().attributeHasErrors("seatingConfigurationForm"));

      verify(seatingService, never()).saveSeatingConfiguration(anyInt(), anyInt());
    }

    @Test
    @DisplayName("異常系：保存処理でRuntime例外が発生")
    void saveSeatingConfiguration_SaveException() throws Exception {
      // Given
      doThrow(new RuntimeException("保存エラー"))
          .when(seatingService).saveSeatingConfiguration(anyInt(), anyInt());

      // When & Then
      mockMvc.perform(post("/seating/setup")
              .param("rows", "6")
              .param("columns", "5"))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/seating/setup"))
          .andExpect(flash().attribute("errorMessage", "座席表設定の保存に失敗しました。"));

      verify(seatingService).saveSeatingConfiguration(6, 5);
    }

    @Test
    @DisplayName("異常系：保存処理でIllegalArgumentException例外が発生")
    void saveSeatingConfiguration_IllegalArgumentException() throws Exception {
      // Given
      doThrow(new IllegalArgumentException("無効な引数"))
          .when(seatingService).saveSeatingConfiguration(anyInt(), anyInt());

      // When & Then
      mockMvc.perform(post("/seating/setup")
              .param("rows", "6")
              .param("columns", "5"))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/seating/setup"))
          .andExpect(flash().attribute("errorMessage", "座席表設定の保存に失敗しました。"));

      verify(seatingService).saveSeatingConfiguration(6, 5);
    }
  }

  @Nested
  @DisplayName("POST /seating/setup/preview - プレビュー機能")
  class PreviewConfigurationTest {

    @Test
    @DisplayName("正常系：プレビューが正常に表示される（十分な座席数）")
    void previewConfiguration_Success_EnoughSeats() throws Exception {
      // Given
      when(seatingService.getCurrentRows()).thenReturn(6);
      when(seatingService.getCurrentColumns()).thenReturn(5);
      when(studentService.getAllStudents()).thenReturn(mockStudents);

      // When & Then
      mockMvc.perform(post("/seating/setup/preview")
              .param("rows", "8")
              .param("columns", "6"))
          .andExpect(status().isOk())
          .andExpect(view().name("seating/setup"))
          .andExpect(model().attribute("currentRows", 6))
          .andExpect(model().attribute("currentColumns", 5))
          .andExpect(model().attribute("totalStudents", 3))
          .andExpect(model().attribute("currentTotalSeats", 30))
          .andExpect(model().attribute("previewTotalSeats", 48))
          .andExpect(model().attribute("hasEnoughSeats", true))
          .andExpect(model().attribute("showPreview", true));

      verify(seatingService).getCurrentRows();
      verify(seatingService).getCurrentColumns();
      verify(studentService).getAllStudents();
    }

    @Test
    @DisplayName("正常系：プレビューが正常に表示される（座席数不足）")
    void previewConfiguration_Success_NotEnoughSeats() throws Exception {
      // Given
      when(seatingService.getCurrentRows()).thenReturn(6);
      when(seatingService.getCurrentColumns()).thenReturn(5);
      when(studentService.getAllStudents()).thenReturn(mockStudents);

      // When & Then
      mockMvc.perform(post("/seating/setup/preview")
              .param("rows", "2")
              .param("columns", "1"))
          .andExpect(status().isOk())
          .andExpect(view().name("seating/setup"))
          .andExpect(model().attribute("previewTotalSeats", 2))
          .andExpect(model().attribute("hasEnoughSeats", false))
          .andExpect(model().attribute("showPreview", true));
    }

    @Test
    @DisplayName("正常系：プレビューで座席数がちょうど同じ場合")
    void previewConfiguration_Success_ExactSeats() throws Exception {
      // Given
      when(seatingService.getCurrentRows()).thenReturn(6);
      when(seatingService.getCurrentColumns()).thenReturn(5);
      when(studentService.getAllStudents()).thenReturn(mockStudents);

      // When & Then
      mockMvc.perform(post("/seating/setup/preview")
              .param("rows", "3")
              .param("columns", "1"))
          .andExpect(status().isOk())
          .andExpect(view().name("seating/setup"))
          .andExpect(model().attribute("previewTotalSeats", 3))
          .andExpect(model().attribute("hasEnoughSeats", true));
    }

    @Test
    @DisplayName("正常系：生徒が0人の場合のプレビュー")
    void previewConfiguration_Success_NoStudents() throws Exception {
      // Given
      when(seatingService.getCurrentRows()).thenReturn(4);
      when(seatingService.getCurrentColumns()).thenReturn(4);
      when(studentService.getAllStudents()).thenReturn(Collections.emptyList());

      // When & Then
      mockMvc.perform(post("/seating/setup/preview")
              .param("rows", "2")
              .param("columns", "2"))
          .andExpect(status().isOk())
          .andExpect(view().name("seating/setup"))
          .andExpect(model().attribute("totalStudents", 0))
          .andExpect(model().attribute("previewTotalSeats", 4))
          .andExpect(model().attribute("hasEnoughSeats", true));
    }

    @Test
    @DisplayName("正常系：大規模クラスでのプレビュー")
    void previewConfiguration_Success_LargeClass() throws Exception {
      // Given - 40人の大規模クラス
      List<Student> largeClass = createLargeStudentList(40);
      when(seatingService.getCurrentRows()).thenReturn(6);
      when(seatingService.getCurrentColumns()).thenReturn(5);
      when(studentService.getAllStudents()).thenReturn(largeClass);

      // When & Then
      mockMvc.perform(post("/seating/setup/preview")
              .param("rows", "8")
              .param("columns", "5"))
          .andExpect(status().isOk())
          .andExpect(view().name("seating/setup"))
          .andExpect(model().attribute("totalStudents", 40))
          .andExpect(model().attribute("previewTotalSeats", 40))
          .andExpect(model().attribute("hasEnoughSeats", true));
    }

    @Test
    @DisplayName("異常系：バリデーションエラーがあってもプレビュー表示される")
    void previewConfiguration_WithValidationError() throws Exception {
      // Given
      when(seatingService.getCurrentRows()).thenReturn(6);
      when(seatingService.getCurrentColumns()).thenReturn(5);
      when(studentService.getAllStudents()).thenReturn(mockStudents);

      // When & Then
      mockMvc.perform(post("/seating/setup/preview")
              .param("rows", "0")
              .param("columns", "5"))
          .andExpect(status().isOk())
          .andExpect(view().name("seating/setup"))
          .andExpect(model().attributeHasFieldErrors("seatingConfigurationForm", "rows"))
          .andExpect(model().attribute("previewTotalSeats", 0))
          .andExpect(model().attribute("hasEnoughSeats", false))
          .andExpect(model().attribute("showPreview", true));
    }

    @Test
    @DisplayName("正常系：極端に大きな値での計算")
    void previewConfiguration_Success_LargeValues() throws Exception {
      // Given
      when(seatingService.getCurrentRows()).thenReturn(6);
      when(seatingService.getCurrentColumns()).thenReturn(5);
      when(studentService.getAllStudents()).thenReturn(mockStudents);

      // When & Then
      mockMvc.perform(post("/seating/setup/preview")
              .param("rows", "50")
              .param("columns", "50"))
          .andExpect(status().isOk())
          .andExpect(view().name("seating/setup"))
          .andExpect(model().attribute("previewTotalSeats", 2500))
          .andExpect(model().attribute("hasEnoughSeats", true));
    }
  }

  @Nested
  @DisplayName("エッジケースとパフォーマンステスト")
  class EdgeCaseAndPerformanceTest {

    @Test
    @DisplayName("エッジケース：座席の上限値テスト")
    void edgeCase_MaximumSeatingCapacity() throws Exception {
      // Given - Student エンティティの制約に基づく最大値（6行5列）
      when(seatingService.getCurrentRows()).thenReturn(6);
      when(seatingService.getCurrentColumns()).thenReturn(5);
      when(studentService.getAllStudents()).thenReturn(mockStudents);

      // When & Then
      mockMvc.perform(post("/seating/setup/preview")
              .param("rows", "6")
              .param("columns", "5"))
          .andExpect(status().isOk())
          .andExpect(model().attribute("previewTotalSeats", 30));
    }

    @Test
    @DisplayName("エッジケース：座席の下限値テスト")
    void edgeCase_MinimumSeatingCapacity() throws Exception {
      // Given
      when(seatingService.getCurrentRows()).thenReturn(1);
      when(seatingService.getCurrentColumns()).thenReturn(1);
      when(studentService.getAllStudents()).thenReturn(
          Collections.singletonList(mockStudents.get(0)));

      // When & Then
      mockMvc.perform(post("/seating/setup/preview")
              .param("rows", "1")
              .param("columns", "1"))
          .andExpect(status().isOk())
          .andExpect(model().attribute("previewTotalSeats", 1))
          .andExpect(model().attribute("hasEnoughSeats", true));
    }

    @Test
    @DisplayName("パフォーマンス：大量の学生データ処理")
    void performance_LargeDataSet() throws Exception {
      // Given - 1000人の学生データ
      List<Student> massiveStudentList = createLargeStudentList(1000);
      when(seatingService.getCurrentRows()).thenReturn(6);
      when(seatingService.getCurrentColumns()).thenReturn(5);
      when(studentService.getAllStudents()).thenReturn(massiveStudentList);

      // When & Then
      mockMvc.perform(get("/seating/setup"))
          .andExpect(status().isOk())
          .andExpect(model().attribute("totalStudents", 1000));
    }
  }

  @Nested
  @DisplayName("統合テスト - 複数メソッド連携")
  class IntegrationTest {

    @Test
    @DisplayName("設定画面表示 → プレビュー → 保存の一連の流れ")
    void completeWorkflow() throws Exception {
      // Given
      when(seatingService.getCurrentRows()).thenReturn(6);
      when(seatingService.getCurrentColumns()).thenReturn(5);
      when(studentService.getAllStudents()).thenReturn(mockStudents);
      doNothing().when(seatingService).saveSeatingConfiguration(anyInt(), anyInt());

      // 1. 設定画面表示
      mockMvc.perform(get("/seating/setup"))
          .andExpect(status().isOk())
          .andExpect(view().name("seating/setup"));

      // 2. プレビュー
      mockMvc.perform(post("/seating/setup/preview")
              .param("rows", "8")
              .param("columns", "6"))
          .andExpect(status().isOk())
          .andExpect(model().attribute("showPreview", true));

      // 3. 保存
      mockMvc.perform(post("/seating/setup")
              .param("rows", "8")
              .param("columns", "6"))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/seating/chart?rows=8&columns=6"));

      verify(seatingService).saveSeatingConfiguration(8, 6);
    }

    @Test
    @DisplayName("座席数不足の警告から適切な座席数への修正")
    void workflowWithWarning() throws Exception {
      // Given
      when(seatingService.getCurrentRows()).thenReturn(6);
      when(seatingService.getCurrentColumns()).thenReturn(5);
      when(studentService.getAllStudents()).thenReturn(mockStudents);
      doNothing().when(seatingService).saveSeatingConfiguration(anyInt(), anyInt());

      // 1. 座席数不足のプレビュー
      mockMvc.perform(post("/seating/setup/preview")
              .param("rows", "2")
              .param("columns", "1"))
          .andExpect(status().isOk())
          .andExpect(model().attribute("hasEnoughSeats", false));

      // 2. 適切な座席数での保存
      mockMvc.perform(post("/seating/setup")
              .param("rows", "2")
              .param("columns", "2"))
          .andExpect(status().is3xxRedirection());

      verify(seatingService).saveSeatingConfiguration(2, 2);
    }

    @Test
    @DisplayName("バリデーションエラー → 修正 → 保存の流れ")
    void validationErrorCorrectionWorkflow() throws Exception {
      // Given
      when(seatingService.getCurrentRows()).thenReturn(6);
      when(seatingService.getCurrentColumns()).thenReturn(5);
      when(studentService.getAllStudents()).thenReturn(mockStudents);
      doNothing().when(seatingService).saveSeatingConfiguration(anyInt(), anyInt());

      // 1. バリデーションエラー
      mockMvc.perform(post("/seating/setup")
              .param("rows", "0")
              .param("columns", "5"))
          .andExpect(status().isOk())
          .andExpect(view().name("seating/setup"))
          .andExpect(model().attributeHasFieldErrors("seatingConfigurationForm", "rows"));

      // 2. 修正して正常保存
      mockMvc.perform(post("/seating/setup")
              .param("rows", "4")
              .param("columns", "5"))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/seating/chart?rows=4&columns=5"));

      verify(seatingService).saveSeatingConfiguration(4, 5);
    }
  }

  // ヘルパーメソッド

  /**
   * 大量の学生データを作成するヘルパーメソッド
   */
  private List<Student> createLargeStudentList(int count) {
    List<Student> students = new java.util.ArrayList<>();
    for (int i = 1; i <= count; i++) {
      Student student = Student.builder()
          .id((long) i)
          .name("学生" + i)
          .gender(i % 2 == 0 ? "女子" : "男子")
          .studentCode(String.valueOf(i))
          .seatRow(((i - 1) % 6) + 1)  // 1-6の範囲でループ
          .seatColumn(((i - 1) % 5) + 1)  // 1-5の範囲でループ
          .build();
      students.add(student);
    }
    return students;
  }

  /**
   * 座席位置が重複する学生データを作成するヘルパーメソッド
   */
  private List<Student> createStudentsWithDuplicateSeats() {
    Student student1 = Student.builder()
        .id(1L)
        .name("重複太郎")
        .gender("男子")
        .studentCode("100")
        .seatRow(1)
        .seatColumn(1)
        .build();

    Student student2 = Student.builder()
        .id(2L)
        .name("重複花子")
        .gender("女子")
        .studentCode("101")
        .seatRow(1)  // 同じ座席
        .seatColumn(1)  // 同じ座席
        .build();

    return Arrays.asList(student1, student2);
  }

  /**
   * 無効な座席位置を持つ学生データを作成するヘルパーメソッド
   */
  private List<Student> createStudentsWithInvalidSeats() {
    Student student1 = Student.builder()
        .id(1L)
        .name("無効太郎")
        .gender("男子")
        .studentCode("200")
        .seatRow(7)  // 範囲外（最大6）
        .seatColumn(1)
        .build();

    Student student2 = Student.builder()
        .id(2L)
        .name("無効花子")
        .gender("女子")
        .studentCode("201")
        .seatRow(1)
        .seatColumn(6)  // 範囲外（最大5）
        .build();

    return Arrays.asList(student1, student2);
  }
}