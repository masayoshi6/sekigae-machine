package sekigae.sekigae.seatingapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import sekigae.sekigae.seatingapp.entity.SeatingConfigurationForm;
import sekigae.sekigae.seatingapp.service.SeatingService;
import sekigae.sekigae.seatingapp.service.StudentService;

/**
 * 座席表設定画面のコントローラー 座席の行数・列数を変更して新しい座席表を作成する機能を提供します。
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/seating/setup")
public class SeatingSetupController {

  private final SeatingService seatingService;
  private final StudentService studentService;

  /**
   * 座席表設定画面を表示
   */
  @Operation(summary = "座席表設定画面表示", description = "画面に表示させる座席表情報を設定します。")
  @GetMapping
  public String showSeatingSetup(Model model) {
    // 現在の設定を取得
    int currentRows = seatingService.getCurrentRows();
    int currentColumns = seatingService.getCurrentColumns();

    // フォームオブジェクトを作成（現在の設定を初期値として設定）
    SeatingConfigurationForm form = new SeatingConfigurationForm();
    form.setRows(currentRows);
    form.setColumns(currentColumns);

    // 統計情報を取得
    int totalStudents = studentService.getAllStudents().size();
    int currentTotalSeats = currentRows * currentColumns;

    model.addAttribute("seatingConfigurationForm", form);
    model.addAttribute("currentRows", currentRows);
    model.addAttribute("currentColumns", currentColumns);
    model.addAttribute("totalStudents", totalStudents);
    model.addAttribute("currentTotalSeats", currentTotalSeats);

    return "seating/setup";
  }

  /**
   * 座席表設定を保存して座席表画面にリダイレクト
   */
  @Operation(summary = "座席表設定の保存", description = "現在の座席表設定を保存します。")
  @PostMapping
  public String saveSeatingConfiguration(
      @Valid @ModelAttribute SeatingConfigurationForm form,
      BindingResult bindingResult,
      Model model,
      RedirectAttributes redirectAttributes) {

    // バリデーションエラーがある場合は設定画面に戻る
    if (bindingResult.hasErrors()) {
      // 統計情報を再取得
      int currentRows = seatingService.getCurrentRows();
      int currentColumns = seatingService.getCurrentColumns();
      int totalStudents = studentService.getAllStudents().size();
      int currentTotalSeats = currentRows * currentColumns;

      model.addAttribute("currentRows", currentRows);
      model.addAttribute("currentColumns", currentColumns);
      model.addAttribute("totalStudents", totalStudents);
      model.addAttribute("currentTotalSeats", currentTotalSeats);

      return "seating/setup";
    }

    try {
      // 座席表設定を保存
      seatingService.saveSeatingConfiguration(form.getRows(), form.getColumns());

      // 成功メッセージを設定
      redirectAttributes.addFlashAttribute("successMessage",
          String.format("座席表設定を %d行 %d列 に変更しました。", form.getRows(),
              form.getColumns()));

      // 座席表画面にリダイレクト（新しい設定で表示）
      return "redirect:/seating/chart?rows=" + form.getRows() + "&columns=" + form.getColumns();

    } catch (Exception e) {
      // エラーメッセージを設定
      redirectAttributes.addFlashAttribute("errorMessage",
          "座席表設定の保存に失敗しました。");

      return "redirect:/seating/setup";
    }
  }

  /**
   * プレビュー機能：設定変更時の座席数を計算してAJAXで返す
   */
  @Operation(summary = "座席数計算", description = "座席表設定変更後の座席数を求めます。")
  @PostMapping("/preview")
  public String previewConfiguration(
      @Valid @ModelAttribute SeatingConfigurationForm form,
      BindingResult bindingResult,
      Model model) {

    // 現在の設定を取得
    int currentRows = seatingService.getCurrentRows();
    int currentColumns = seatingService.getCurrentColumns();
    int totalStudents = studentService.getAllStudents().size();

    // プレビュー情報
    int previewTotalSeats = form.getRows() * form.getColumns();
    boolean hasEnoughSeats = previewTotalSeats >= totalStudents;

    model.addAttribute("seatingConfigurationForm", form);
    model.addAttribute("currentRows", currentRows);
    model.addAttribute("currentColumns", currentColumns);
    model.addAttribute("totalStudents", totalStudents);
    model.addAttribute("currentTotalSeats", currentRows * currentColumns);
    model.addAttribute("previewTotalSeats", previewTotalSeats);
    model.addAttribute("hasEnoughSeats", hasEnoughSeats);
    model.addAttribute("showPreview", true);

    return "seating/setup";
  }
}