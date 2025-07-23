package sekigae.sekigae.seatingapp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import sekigae.sekigae.seatingapp.service.SeatingService;

@Controller
@RequiredArgsConstructor
@RequestMapping("/seating")
public class SeatingController {

  private final SeatingService seatingService;

  /**
   * 座席表を表示する
   */
  @GetMapping
  public String showSeatingChart(Model model) {
    // 座席表のデータを取得（6行5列で固定）
    String[][] seatingChart = seatingService.getSeatingChart(6, 5);

    model.addAttribute("seatingChart", seatingChart);
    model.addAttribute("rows", 6);
    model.addAttribute("columns", 5);

    return "seating/chart";
  }
}