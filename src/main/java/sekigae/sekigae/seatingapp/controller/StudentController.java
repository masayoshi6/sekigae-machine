package sekigae.sekigae.seatingapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import sekigae.sekigae.seatingapp.entity.Student;
import sekigae.sekigae.seatingapp.service.StudentService;

@Valid
@Controller
@RequiredArgsConstructor
@RequestMapping("/students")
public class StudentController {

  private final StudentService studentService;

  /**
   * 生徒一覧を取得してHTMLページに表示（Thymeleaf用）
   */
  @Operation(summary = "一覧検索", description = "生徒の一覧を検索します。",
      responses = {
          @ApiResponse(responseCode = "200", description = "正常に生徒一覧情報を取得しました。"),
          @ApiResponse(
              responseCode = "404",
              description = "生徒情報が１件もありません。",
              content = @Content(
                  mediaType = "application/json",
                  examples = @ExampleObject(
                      name = "NotFoundExample",
                      summary = "生徒情報が存在しない",
                      description = "生徒のリストがnullの場合",
                      value = """
                          {
                            "error": "Not Found",
                            "message": "生徒情報が１件もありません。",
                            "code": 404
                          }""")))})
  @GetMapping
  public String getAllStudents(Model model) {
    List<Student> students = studentService.getAllStudents();
    model.addAttribute("students", students);
    return "students/list"; // → templates/students/list.html を返す
  }

  /**
   * 新規生徒登録フォームの表示
   */
  @Operation(summary = "生徒登録画面の表示", description = "生徒の登録画面を表示します。",
      responses = {
          @ApiResponse(responseCode = "200", description = "正常に生徒登録画面を取得しました。"),
          @ApiResponse(
              responseCode = "404",
              description = "登録画面の表示ができません。",
              content = @Content(
                  mediaType = "application/json",
                  examples = @ExampleObject(
                      name = "NotFoundExample",
                      summary = "登録画面の不表示",
                      description = "フォーム画面の読み込みができない場合",
                      value = """
                          {
                            "error": "Not Found",
                            "message": "フォーム画面の表示をすることができませんでした。",
                            "code": 404
                          }""")))})
  @GetMapping("/new")
  public String showCreateForm(Model model) {
    // 初期状態では空の Student インスタンスを渡す（性別は null のまま）
    model.addAttribute("student", new Student());
    return "students/create"; // → templates/students/create.html
  }

  /**
   * 新規生徒を登録する（POST）
   */
  @Operation(summary = "生徒登録", description = "新規で生徒の登録を行います。",
      responses = {
          @ApiResponse(responseCode = "200", description = "正常に生徒情報を新規登録しました。"),
          @ApiResponse(
              responseCode = "400",
              description = "座席に生徒が存在します。",
              content = @Content(
                  mediaType = "application/json",
                  examples = @ExampleObject(
                      name = "BadRequestExample",
                      summary = "座席登録重複エラー",
                      description = "生徒の座席登録時に重複する場合のエラー例",
                      value = """
                          {
                            "error": "Bad Request",
                            "message": "その座席にはすでに生徒が存在します。",
                            "code": 400
                          }""")))})
  @PostMapping
  public String registerStudent(@Valid @ModelAttribute Student student,
      BindingResult bindingResult,
      Model model) {

    // 入力エラーがある場合
    if (bindingResult.hasErrors()) {
      return "students/create";
    }

    // ✅ 学籍番号の重複チェックをここで追加！
    if (studentService.isStudentCodeDuplicate(student.getStudentCode())) {
      bindingResult.rejectValue("studentCode", "duplicate", "その学籍番号はすでに登録されています");
      return "students/create";
    }

    try {
      studentService.registerStudent(student);
    } catch (IllegalArgumentException e) {
      model.addAttribute("seatError", e.getMessage());
      return "students/create";
    }

    return "redirect:/students";
  }

  /**
   * REST API: 生徒一覧をJSONで取得
   */
  @Operation(summary = "一覧検索", description = "生徒の一覧を検索します。",
      responses = {
          @ApiResponse(responseCode = "200", description = "正常に生徒一覧情報を取得しました。"),
          @ApiResponse(
              responseCode = "404",
              description = "生徒情報が１件もありません。",
              content = @Content(
                  mediaType = "application/json",
                  examples = @ExampleObject(
                      name = "NotFoundExample",
                      summary = "生徒情報が存在しない",
                      description = "生徒のリストがnullの場合",
                      value = """
                          {
                            "error": "Not Found",
                            "message": "生徒情報が１件もありません。",
                            "code": 404
                          }""")))})
  @GetMapping("/api")
  @ResponseBody
  public List<Student> getAllStudentsApi() {
    return studentService.getAllStudents();
  }

  /**
   * REST API: ID指定で生徒情報を取得
   */
  @Operation(summary = "生徒単一検索", description = "指定ID生徒の単一検索をします。",
      responses = {
          @ApiResponse(responseCode = "200", description = "正常に生徒情報の取得に成功しました。"),
          @ApiResponse(
              responseCode = "400",
              description = "不正なID形式です。",
              content = @Content(
                  mediaType = "application/json",
                  examples = @ExampleObject(
                      name = "BadRequestExample",
                      summary = "ID形式エラー",
                      description = "IDが自然数でない場合のエラー例",
                      value = """
                          {
                            "error": "Bad Request",
                            "message": "IDの形式が不正です。自然数値を指定してください。",
                            "code": 400
                          }"""))),
          @ApiResponse(
              responseCode = "404",
              description = "指定されたIDの生徒情報は存在しません。",
              content = @Content(
                  mediaType = "application/json",
                  examples = @ExampleObject(
                      name = "NotFoundExample",
                      summary = "生徒情報未登録",
                      description = "指定したIDの生徒情報が存在しない場合",
                      value = """
                          {
                            "error": "Not Found",
                            "message": "指定された生徒情報は存在しませんでした。",
                            "code": 404
                          }""")))})
  @GetMapping("/api/{id}")
  @ResponseBody
  public ResponseEntity<Student> getStudentById(@PathVariable Long id) {
    Student student = studentService.getStudentById(id);
    if (student != null) {
      return ResponseEntity.ok(student);
    } else {
      return ResponseEntity.notFound().build();
    }
  }


  /**
   * 生徒を削除する（修正版）
   */
  @Operation(summary = "登録生徒の削除", description = "登録済み生徒を選択削除します。",
      responses = {
          @ApiResponse(responseCode = "200", description = "正常に生徒情報の削除に成功しました。"),
          @ApiResponse(
              responseCode = "400",
              description = "不正なID形式です。",
              content = @Content(
                  mediaType = "application/json",
                  examples = @ExampleObject(
                      name = "BadRequestExample",
                      summary = "ID形式エラー",
                      description = "IDが自然数でない場合のエラー例",
                      value = """
                          {
                            "error": "Bad Request",
                            "message": "IDの形式が不正です。自然数値を指定してください。",
                            "code": 400
                          }"""))),
          @ApiResponse(
              responseCode = "404",
              description = "指定されたIDの生徒情報は存在しません。",
              content = @Content(
                  mediaType = "application/json",
                  examples = @ExampleObject(
                      name = "NotFoundExample",
                      summary = "生徒情報未登録",
                      description = "指定したIDの生徒情報が存在しない場合",
                      value = """
                          {
                            "error": "Not Found",
                            "message": "指定された生徒情報は存在しませんでした。",
                            "code": 404
                          }""")))})
  @PostMapping("/delete/{id}")
  public String deleteStudent(@PathVariable Long id) {
    studentService.deleteStudent(id);
    return "redirect:/students";
  }

}
