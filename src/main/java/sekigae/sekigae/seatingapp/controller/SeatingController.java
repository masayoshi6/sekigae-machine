package sekigae.sekigae.seatingapp.controller;
//あいうえお

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import sekigae.sekigae.seatingapp.entity.SeatingSnapshot;
import sekigae.sekigae.seatingapp.entity.Student;
import sekigae.sekigae.seatingapp.service.SeatingService;
import sekigae.sekigae.seatingapp.service.SeatingSnapshotService;
import sekigae.sekigae.seatingapp.service.StudentService;

@Valid
@Controller
@RequiredArgsConstructor
@RequestMapping("/seating")
public class SeatingController {

  private final SeatingService seatingService;
  private final StudentService studentService;
  private final SeatingSnapshotService snapshotService;

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

    // 保存されたスナップショット一覧を取得
    List<SeatingSnapshot> snapshots = snapshotService.getAllSnapshots();

    model.addAttribute("seatingChart", seatingChart);
    model.addAttribute("students", students);
    model.addAttribute("rows", rows);
    model.addAttribute("columns", columns);
    model.addAttribute("snapshots", snapshots);

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

    // 保存されたスナップショット一覧を取得
    List<SeatingSnapshot> snapshots = snapshotService.getAllSnapshots();

    model.addAttribute("seatingChart", seatingChart);
    model.addAttribute("students", students);
    model.addAttribute("rows", rows);
    model.addAttribute("columns", columns);
    model.addAttribute("snapshots", snapshots);

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
    return showSeatingChartWithSize(rows, columns, model);
  }


  /**
   * 座席をシャッフルする（制約条件を考慮）
   */
  @PostMapping("/shuffle")
  public String shuffleSeatingChart(
      @RequestParam("rows") int rows,
      @RequestParam("columns") int columns,
      @RequestParam(value = "preventSameGender", required = false) Boolean preventSameGender,
      @RequestParam(value = "alternateColumns", required = false) Boolean alternateColumns,
      Model model) {

    // 制約条件の確認
    boolean alternateGenders = Boolean.TRUE.equals(preventSameGender);
    boolean alternateByColumns = Boolean.TRUE.equals(alternateColumns);

    // ランダムにシャッフルした座席表を取得（制約条件を適用）
    Student[][] seatingChart = seatingService.shuffleSeatingChart(rows, columns, alternateGenders,
        alternateByColumns);

    // 学生データも追加（必要であれば）
    List<Student> students = studentService.getAllStudents();

    // 保存されたスナップショット一覧を取得
    List<SeatingSnapshot> snapshots = snapshotService.getAllSnapshots();

    model.addAttribute("seatingChart", seatingChart);
    model.addAttribute("students", students);
    model.addAttribute("rows", rows);
    model.addAttribute("columns", columns);
    model.addAttribute("snapshots", snapshots);

    return "seating/chart";
  }

  /**
   * 現在の座席配置を保存する
   */
  @PostMapping("/save")
  public String saveSeatingConfiguration(
      @RequestParam("rows") int rows,
      @RequestParam("columns") int columns,
      @RequestParam(value = "snapshotName", required = false) String snapshotName,
      @RequestParam(value = "description", required = false) String description,
      RedirectAttributes redirectAttributes) {

    try {
      SeatingSnapshot snapshot = snapshotService.saveCurrentSeatingSnapshot(rows, columns,
          snapshotName, description);
      redirectAttributes.addFlashAttribute("successMessage",
          "座席配置「" + snapshot.getSnapshotName() + "」を保存しました。");
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("errorMessage",
          "座席配置の保存に失敗しました。");
    }

    return "redirect:/seating";
  }

  /**
   * 保存された座席配置を復元する
   */
  @PostMapping("/restore")
  public String restoreSeatingConfiguration(
      @RequestParam("snapshotId") Long snapshotId,
      RedirectAttributes redirectAttributes) {

    try {
      boolean success = snapshotService.restoreSeatingSnapshot(snapshotId);
      if (success) {
        redirectAttributes.addFlashAttribute("successMessage",
            "座席配置を復元しました。");
      } else {
        redirectAttributes.addFlashAttribute("errorMessage",
            "座席配置の復元に失敗しました。");
      }
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("errorMessage",
          "座席配置の復元中にエラーが発生しました。");
    }

    return "redirect:/seating";
  }

  /**
   * 保存された座席配置を削除する
   */
  @PostMapping("/delete-snapshot")
  public String deleteSeatingSnapshot(
      @RequestParam("snapshotId") Long snapshotId,
      RedirectAttributes redirectAttributes) {

    try {
      boolean success = snapshotService.deleteSnapshot(snapshotId);
      if (success) {
        redirectAttributes.addFlashAttribute("successMessage",
            "座席配置を削除しました。");
      } else {
        redirectAttributes.addFlashAttribute("errorMessage",
            "座席配置の削除に失敗しました。");
      }
    } catch (Exception e) {
      redirectAttributes.addFlashAttribute("errorMessage",
          "座席配置の削除中にエラーが発生しました。");
    }

    return "redirect:/seating";
  }
}