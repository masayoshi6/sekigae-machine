package sekigae.sekigae.seatingapp.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import sekigae.sekigae.seatingapp.entity.Student;
import sekigae.sekigae.seatingapp.service.StudentService;

@Controller
@RequiredArgsConstructor
public class HomeController {

  private final StudentService studentService;


  @GetMapping("/")
  public String home(Model model) {
    // 統計情報を追加
    List<Student> students = studentService.getAllStudents();
    model.addAttribute("studentCount", students.size());
    model.addAttribute("occupiedSeats", students.size());

    return "home/dashboard"; // templates/home/dashboard.html を返す
  }
  //return "redirect:/seating"; // 座席表を最初に表示
  //return "redirect:/students";

}