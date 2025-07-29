package sekigae.sekigae.seatingapp.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import sekigae.sekigae.seatingapp.entity.Student;
import sekigae.sekigae.seatingapp.service.SeatingService;
import sekigae.sekigae.seatingapp.service.StudentService;

@Controller
@RequiredArgsConstructor
@RequestMapping("/seating")
public class SeatingController {

  private final SeatingService seatingService;
  private final StudentService studentService;

  /**
   * 座席表を表示する
   */
  @GetMapping
  public String showSeatingChart(Model model) {
    // 6行5列で固定
    int rows = 6;
    int columns = 5;

    // Studentオブジェクトを含む座席表を取得
    Student[][] seatingChart = seatingService.getSeatingChartWithGender(rows, columns);

    // 全学生データ（必要なら他の用途用）
    List<Student> students = studentService.getAllStudents();

    model.addAttribute("seatingChart", seatingChart);
    model.addAttribute("students", students);
    model.addAttribute("rows", rows);
    model.addAttribute("columns", columns);

    return "seating/chart";
  }


  /**
   * 指定した行数・列数で座席表を表示する
   */
  @GetMapping("/chart")
  public String showSeatingChartWithParams(
      @RequestParam(value = "rows", defaultValue = "6") int rows,
      @RequestParam(value = "columns", defaultValue = "5") int columns,
      Model model) {
    return showSeatingChartWithSize(rows, columns, model);
  }

  /**
   * 座席表設定フォームから送信された場合
   */
  @PostMapping("/generate")
  public String generateSeatingChart(
      @RequestParam("rows") int rows,
      @RequestParam("columns") int columns,
      Model model) {
    return showSeatingChartWithSize(rows, columns, model);
  }

  /**
   * 座席表生成の共通処理
   */
  private String showSeatingChartWithSize(int rows, int columns, Model model) {
    // 入力値のバリデーション
    if (rows < 1 || rows > 20) {
      rows = 6; // デフォルト値に戻す
    }
    if (columns < 1 || columns > 20) {
      columns = 5; // デフォルト値に戻す
    }

    // 座席表のデータを取得
    String[][] seatingChart = seatingService.getSeatingChart(rows, columns);

    // 学生データも追加
    List<Student> students = studentService.getAllStudents();

    model.addAttribute("seatingChart", seatingChart);
    model.addAttribute("students", students);
    model.addAttribute("rows", rows);
    model.addAttribute("columns", columns);

    return "seating/chart";
  }

  /**
   * 座席表を再生成する（シャッフル等）
   */
  @PostMapping("/regenerate")
  public String regenerateSeatingChart(
      @RequestParam("rows") int rows,
      @RequestParam("columns") int columns,
      Model model) {
    // 座席表を再生成（シャッフルなど）
    //seatingService.regenerateSeatingChart(rows, columns);
    return showSeatingChartWithSize(rows, columns, model);
  }


  @PostMapping("/shuffle")
  public String shuffleSeatingChart(
      @RequestParam("rows") int rows,
      @RequestParam("columns") int columns,
      Model model) {

    // ランダムにシャッフルした座席表を取得
    Student[][] seatingChart = seatingService.shuffleSeatingChart(rows, columns);

    // 学生データも追加（必要であれば）
    List<Student> students = studentService.getAllStudents();

    model.addAttribute("seatingChart", seatingChart);
    model.addAttribute("students", students);
    model.addAttribute("rows", rows);
    model.addAttribute("columns", columns);

    return "seating/chart";
  }

}
