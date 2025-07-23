package sekigae.sekigae.seatingapp.controller;

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

@Controller
@RequiredArgsConstructor
@RequestMapping("/students")
public class StudentController {

  private final StudentService studentService;

  /**
   * 生徒一覧を取得してHTMLページに表示（Thymeleaf用）
   */
  @GetMapping
  public String getAllStudents(Model model) {
    List<Student> students = studentService.getAllStudents();
    model.addAttribute("students", students);
    return "students/list"; // → templates/students/list.html を返す
  }

  /**
   * 新規生徒登録フォームの表示
   */
  @GetMapping("/new")
  public String showCreateForm(Model model) {
    // 初期状態では空の Student インスタンスを渡す（性別は null のまま）
    model.addAttribute("student", new Student());
    return "students/create"; // → templates/students/create.html
  }

  /**
   * 新規生徒を登録する（POST）
   */
  @PostMapping
  public String registerStudent(@Valid @ModelAttribute Student student,
      BindingResult bindingResult,
      Model model) {
    if (bindingResult.hasErrors()) {
      return "students/create";
    }

    studentService.registerStudent(student);
    return "redirect:/students";
  }

  /**
   * REST API: 生徒一覧をJSONで取得
   */
  @GetMapping("/api")
  @ResponseBody
  public List<Student> getAllStudentsApi() {
    return studentService.getAllStudents();
  }

  /**
   * REST API: ID指定で生徒情報を取得
   */
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
}
