package sekigae.sekigae.seatingapp.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import sekigae.sekigae.seatingapp.entity.Student;
import sekigae.sekigae.seatingapp.service.StudentService;

@ExtendWith(MockitoExtension.class)
@DisplayName("StudentController の単体テスト")
class StudentControllerTest {

  @Mock
  private StudentService studentService;

  @Mock
  private Model model;

  @Mock
  private BindingResult bindingResult;

  @InjectMocks
  private StudentController studentController;

  private MockMvc mockMvc;
  private ObjectMapper objectMapper;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(studentController)
        .setControllerAdvice() // グローバル例外ハンドラーを有効にする
        .setValidator(new LocalValidatorFactoryBean()) // バリデーターを明示的に設定
        .build();
    objectMapper = new ObjectMapper();
  }

  @Nested
  @DisplayName("getAllStudents メソッドのテスト")
  class GetAllStudentsTest {

    @Test
    @DisplayName("正常系: 生徒一覧が正常に取得できる")
    void getAllStudents_Success() throws Exception {
      // Given
      List<Student> students = Arrays.asList(
          Student.builder()
              .id(1L)
              .name("山田太郎")
              .gender("男子")
              .studentCode("1")
              .seatRow(1)
              .seatColumn(1)
              .build(),
          Student.builder()
              .id(2L)
              .name("田中花子")
              .gender("女子")
              .studentCode("2")
              .seatRow(1)
              .seatColumn(2)
              .build()
      );

      when(studentService.getAllStudents()).thenReturn(students);

      // When & Then
      mockMvc.perform(get("/students"))
          .andExpect(status().isOk())
          .andExpect(view().name("students/list"))
          .andExpect(model().attribute("students", students));

      verify(studentService, times(1)).getAllStudents();
    }

    @Test
    @DisplayName("正常系: 生徒一覧が空の場合")
    void getAllStudents_EmptyList() throws Exception {
      // Given
      List<Student> emptyStudents = Collections.emptyList();
      when(studentService.getAllStudents()).thenReturn(emptyStudents);

      // When & Then
      mockMvc.perform(get("/students"))
          .andExpect(status().isOk())
          .andExpect(view().name("students/list"))
          .andExpect(model().attribute("students", emptyStudents));

      verify(studentService, times(1)).getAllStudents();
    }

    @Test
    @DisplayName("異常系: サービスで例外が発生した場合")
    void getAllStudents_ServiceException() throws Exception {
      // Given
      when(studentService.getAllStudents()).thenThrow(new RuntimeException("データベースエラー"));

      // When & Then
      // MockMvcではなく、直接コントローラーメソッドをテストする
      RuntimeException exception = assertThrows(RuntimeException.class, () -> {
        studentController.getAllStudents(model);
      });

      assertEquals("データベースエラー", exception.getMessage());
      verify(studentService, times(1)).getAllStudents();
    }
  }

  @Nested
  @DisplayName("showCreateForm メソッドのテスト")
  class ShowCreateFormTest {

    @Test
    @DisplayName("正常系: 新規登録フォームが正常に表示される")
    void showCreateForm_Success() throws Exception {
      // When & Then
      mockMvc.perform(get("/students/new"))
          .andExpect(status().isOk())
          .andExpect(view().name("students/create"))
          .andExpect(model().attributeExists("student"));
    }
  }

  @Nested
  @DisplayName("registerStudent メソッドのテスト")
  class RegisterStudentTest {

    @Test
    @DisplayName("正常系: 生徒が正常に登録される")
    void registerStudent_Success() throws Exception {
      // Given
      Student student = Student.builder()
          .name("山田太郎")
          .gender("男子")
          .studentCode("1")
          .seatRow(1)
          .seatColumn(1)
          .build();

      when(studentService.isStudentCodeDuplicate("1")).thenReturn(false);
      when(studentService.registerStudent(any(Student.class))).thenReturn(student);

      // When & Then
      mockMvc.perform(post("/students")
              .param("name", "山田太郎")
              .param("gender", "男子")
              .param("studentCode", "1")
              .param("seatRow", "1")
              .param("seatColumn", "1"))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/students"));

      verify(studentService, times(1)).isStudentCodeDuplicate("1");
      verify(studentService, times(1)).registerStudent(any(Student.class));
    }


    @Test
    @DisplayName("異常系: バリデーションエラーがある場合")
    void registerStudent_ValidationError() throws Exception {
      // When & Then
      mockMvc.perform(post("/students")
              .param("name", "")  // 名前が空
              .param("gender", "")  // 性別が空
              .param("studentCode", "")  // 学籍番号が空
              .param("seatRow", "0")  // 無効な行
              .param("seatColumn", "0"))  // 無効な列
          .andExpect(status().isOk())
          .andExpect(view().name("students/create"));

      verify(studentService, never()).registerStudent(any(Student.class));
    }

    @Test
    @DisplayName("異常系: 学籍番号が重複している場合")
    void registerStudent_DuplicateStudentCode() throws Exception {
      // Given
      when(studentService.isStudentCodeDuplicate("1")).thenReturn(true);

      // When & Then
      mockMvc.perform(post("/students")
              .param("name", "山田太郎")
              .param("gender", "男子")
              .param("studentCode", "1")
              .param("seatRow", "1")
              .param("seatColumn", "1"))
          .andExpect(status().isOk())
          .andExpect(view().name("students/create"));

      verify(studentService, times(1)).isStudentCodeDuplicate("1");
      verify(studentService, never()).registerStudent(any(Student.class));
    }

    @Test
    @DisplayName("異常系: 座席が既に占有されている場合")
    void registerStudent_SeatAlreadyOccupied() throws Exception {
      // Given
      when(studentService.isStudentCodeDuplicate("1")).thenReturn(false);
      doThrow(new IllegalArgumentException("指定された座席は既に使用されています"))
          .when(studentService).registerStudent(any(Student.class));

      // When & Then
      mockMvc.perform(post("/students")
              .param("name", "山田太郎")
              .param("gender", "男子")
              .param("studentCode", "1")
              .param("seatRow", "1")
              .param("seatColumn", "1"))
          .andExpect(status().isOk())
          .andExpect(view().name("students/create"))
          .andExpect(model().attributeExists("seatError"));

      verify(studentService, times(1)).registerStudent(any(Student.class));
    }

    @Test
    @DisplayName("異常系: 学籍番号が自然数でない場合")
    void registerStudent_InvalidStudentCodeFormat() throws Exception {
      // When & Then
      mockMvc.perform(post("/students")
              .param("name", "山田太郎")
              .param("gender", "男子")
              .param("studentCode", "abc")  // 無効な学籍番号
              .param("seatRow", "1")
              .param("seatColumn", "1"))
          .andExpect(status().isOk())
          .andExpect(view().name("students/create"));

      verify(studentService, never()).registerStudent(any(Student.class));
    }

    @Test
    @DisplayName("異常系: 座席の行が範囲外の場合")
    void registerStudent_InvalidSeatRow() throws Exception {
      // When & Then
      mockMvc.perform(post("/students")
              .param("name", "山田太郎")
              .param("gender", "男子")
              .param("studentCode", "1")
              .param("seatRow", "7")  // 範囲外（最大6）
              .param("seatColumn", "1"))
          .andExpect(status().isOk())
          .andExpect(view().name("students/create"));

      verify(studentService, never()).registerStudent(any(Student.class));
    }

    @Test
    @DisplayName("異常系: 座席の列が範囲外の場合")
    void registerStudent_InvalidSeatColumn() throws Exception {
      // When & Then
      mockMvc.perform(post("/students")
              .param("name", "山田太郎")
              .param("gender", "男子")
              .param("studentCode", "1")
              .param("seatRow", "1")
              .param("seatColumn", "6"))  // 範囲外（最大5）
          .andExpect(status().isOk())
          .andExpect(view().name("students/create"));

      verify(studentService, never()).registerStudent(any(Student.class));
    }
  }

  @Nested
  @DisplayName("getAllStudentsApi メソッドのテスト")
  class GetAllStudentsApiTest {

    @Test
    @DisplayName("正常系: API経由で生徒一覧が正常に取得できる")
    void getAllStudentsApi_Success() throws Exception {
      // Given
      List<Student> students = Arrays.asList(
          Student.builder()
              .id(1L)
              .name("山田太郎")
              .gender("男子")
              .studentCode("1")
              .seatRow(1)
              .seatColumn(1)
              .build(),
          Student.builder()
              .id(2L)
              .name("田中花子")
              .gender("女子")
              .studentCode("2")
              .seatRow(1)
              .seatColumn(2)
              .build()
      );

      when(studentService.getAllStudents()).thenReturn(students);

      // When & Then
      mockMvc.perform(get("/students/api"))
          .andExpect(status().isOk())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$").isArray())
          .andExpect(jsonPath("$.length()").value(2))
          .andExpect(jsonPath("$[0].name").value("山田太郎"))
          .andExpect(jsonPath("$[0].gender").value("男子"))
          .andExpect(jsonPath("$[1].name").value("田中花子"))
          .andExpect(jsonPath("$[1].gender").value("女子"));

      verify(studentService, times(1)).getAllStudents();
    }

    @Test
    @DisplayName("正常系: 空のリストが返される")
    void getAllStudentsApi_EmptyList() throws Exception {
      // Given
      when(studentService.getAllStudents()).thenReturn(Collections.emptyList());

      // When & Then
      mockMvc.perform(get("/students/api"))
          .andExpect(status().isOk())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$").isArray())
          .andExpect(jsonPath("$.length()").value(0));
    }
  }

  @Nested
  @DisplayName("getStudentById メソッドのテスト")
  class GetStudentByIdTest {

    @Test
    @DisplayName("正常系: IDで生徒が正常に取得できる")
    void getStudentById_Success() throws Exception {
      // Given
      Student student = Student.builder()
          .id(1L)
          .name("山田太郎")
          .gender("男子")
          .studentCode("1")
          .seatRow(1)
          .seatColumn(1)
          .build();

      when(studentService.getStudentById(1L)).thenReturn(student);

      // When & Then
      mockMvc.perform(get("/students/api/1"))
          .andExpect(status().isOk())
          .andExpect(content().contentType(MediaType.APPLICATION_JSON))
          .andExpect(jsonPath("$.id").value(1))
          .andExpect(jsonPath("$.name").value("山田太郎"))
          .andExpect(jsonPath("$.gender").value("男子"))
          .andExpect(jsonPath("$.studentCode").value("1"))
          .andExpect(jsonPath("$.seatRow").value(1))
          .andExpect(jsonPath("$.seatColumn").value(1));

      verify(studentService, times(1)).getStudentById(1L);
    }

    @Test
    @DisplayName("異常系: 存在しないIDが指定された場合")
    void getStudentById_NotFound() throws Exception {
      // Given
      when(studentService.getStudentById(999L)).thenReturn(null);

      // When & Then
      mockMvc.perform(get("/students/api/999"))
          .andExpect(status().isNotFound());

      verify(studentService, times(1)).getStudentById(999L);
    }

    @Test
    @DisplayName("異常系: 無効なIDが指定された場合")
    void getStudentById_InvalidId() throws Exception {
      // When & Then
      mockMvc.perform(get("/students/api/abc"))
          .andExpect(status().isBadRequest());

      verify(studentService, never()).getStudentById(anyLong());
    }
  }

  @Nested
  @DisplayName("deleteStudent メソッドのテスト")
  class DeleteStudentTest {

    @Test
    @DisplayName("正常系: 生徒が正常に削除される")
    void deleteStudent_Success() throws Exception {
      // Given
      doNothing().when(studentService).deleteStudent(1L);

      // When & Then
      mockMvc.perform(post("/students/delete/1"))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/students"));

      verify(studentService, times(1)).deleteStudent(1L);
    }

    @Test
    @DisplayName("異常系: 存在しないIDで削除を試みる場合")
    void deleteStudent_NotFound() throws Exception {
      // Given
      doThrow(new RuntimeException("生徒が見つかりません"))
          .when(studentService).deleteStudent(999L);

      // When & Then
      // 直接コントローラーメソッドをテストする
      RuntimeException exception = assertThrows(RuntimeException.class, () -> {
        studentController.deleteStudent(999L);
      });

      assertEquals("生徒が見つかりません", exception.getMessage());
      verify(studentService, times(1)).deleteStudent(999L);
    }

    @Test
    @DisplayName("異常系: 無効なIDで削除を試みる場合")
    void deleteStudent_InvalidId() throws Exception {
      // When & Then
      mockMvc.perform(post("/students/delete/abc"))
          .andExpect(status().isBadRequest());

      verify(studentService, never()).deleteStudent(anyLong());
    }

    @Test
    @DisplayName("異常系: データベースエラーが発生した場合")
    void deleteStudent_DatabaseError() throws Exception {
      // Given
      doThrow(new RuntimeException("データベースエラー"))
          .when(studentService).deleteStudent(1L);

      // When & Then
      // 直接コントローラーメソッドをテストする
      RuntimeException exception = assertThrows(RuntimeException.class, () -> {
        studentController.deleteStudent(1L);
      });

      assertEquals("データベースエラー", exception.getMessage());
      verify(studentService, times(1)).deleteStudent(1L);
    }
  }

  @Nested
  @DisplayName("統合テスト: 複数のメソッドを組み合わせたテスト")
  class IntegrationTest {

    @Test
    @DisplayName("統合テスト: 生徒登録から削除まで")
    void fullWorkflow() throws Exception {
      // 1. 生徒登録
      when(studentService.isStudentCodeDuplicate("1")).thenReturn(false);
      when(studentService.registerStudent(any(Student.class))).thenReturn(null);

      mockMvc.perform(post("/students")
              .param("name", "山田太郎")
              .param("gender", "男子")
              .param("studentCode", "1")
              .param("seatRow", "1")
              .param("seatColumn", "1"))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/students"));

      // 2. 生徒一覧取得
      List<Student> students = Arrays.asList(
          Student.builder()
              .id(1L)
              .name("山田太郎")
              .gender("男子")
              .studentCode("1")
              .seatRow(1)
              .seatColumn(1)
              .build()
      );
      when(studentService.getAllStudents()).thenReturn(students);

      mockMvc.perform(get("/students"))
          .andExpect(status().isOk())
          .andExpect(view().name("students/list"));

      // 3. 生徒削除
      doNothing().when(studentService).deleteStudent(1L); // deleteは通常void

      mockMvc.perform(post("/students/delete/1"))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/students"));

      // 検証
      verify(studentService, times(1)).registerStudent(any(Student.class));
      verify(studentService, times(1)).getAllStudents();
      verify(studentService, times(1)).deleteStudent(1L);
    }
  }

  @Nested
  @DisplayName("境界値テスト")
  class BoundaryValueTest {

    @Test
    @DisplayName("境界値テスト: 座席の最小値（1行1列）")
    void registerStudent_MinimumSeatPosition() throws Exception {
      // Given
      when(studentService.isStudentCodeDuplicate("1")).thenReturn(false);
      when(studentService.registerStudent(any(Student.class))).thenReturn(null); // 適切な戻り値を設定

      // When & Then
      mockMvc.perform(post("/students")
              .param("name", "山田太郎")
              .param("gender", "男子")
              .param("studentCode", "1")
              .param("seatRow", "1")
              .param("seatColumn", "1"))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/students"));

      verify(studentService, times(1)).registerStudent(any(Student.class));
    }


    @Test
    @DisplayName("境界値テスト: 座席の最大値（6行5列）")
    void registerStudent_MaximumSeatPosition() throws Exception {
      // Given
      when(studentService.isStudentCodeDuplicate("1")).thenReturn(false);
      when(studentService.registerStudent(any(Student.class))).thenReturn(null); // 適切な戻り値を設定

      // When & Then
      mockMvc.perform(post("/students")
              .param("name", "山田太郎")
              .param("gender", "男子")
              .param("studentCode", "1")
              .param("seatRow", "6")
              .param("seatColumn", "5"))
          .andExpect(status().is3xxRedirection())
          .andExpect(redirectedUrl("/students"));

      verify(studentService, times(1)).registerStudent(any(Student.class));
    }
  }
}