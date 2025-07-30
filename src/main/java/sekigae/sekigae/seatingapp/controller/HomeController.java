package sekigae.sekigae.seatingapp.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import sekigae.sekigae.seatingapp.entity.Student;
import sekigae.sekigae.seatingapp.service.SeatingService;
import sekigae.sekigae.seatingapp.service.StudentService;

@Controller
@RequiredArgsConstructor
public class HomeController {

  private final StudentService studentService;
  private final SeatingService seatingService;

  @GetMapping("/")
  public String home(Model model) {
    // 学生データを取得
    List<Student> students = studentService.getAllStudents();
    int studentCount = students.size();

    // 現在の座席表設定を取得（デフォルトは6行5列）
    int currentRows = seatingService.getCurrentRows();
    int currentColumns = seatingService.getCurrentColumns();

    // 座席使用状況を計算
    String[][] currentSeatingChart = seatingService.getSeatingChart(currentRows, currentColumns);
    int occupiedSeats = calculateOccupiedSeats(currentSeatingChart);

    // モデルに追加
    model.addAttribute("studentCount", studentCount);
    model.addAttribute("currentRows", currentRows);
    model.addAttribute("currentColumns", currentColumns);
    model.addAttribute("occupiedSeats", occupiedSeats);

    return "home/dashboard";
  }

  /**
   * 使用中の座席数を計算
   */
  private int calculateOccupiedSeats(String[][] seatingChart) {
    int count = 0;
    for (String[] row : seatingChart) {
      for (String seat : row) {
        if (seat != null && !seat.trim().isEmpty()) {
          count++;
        }
      }
    }
    return count;
  }
}