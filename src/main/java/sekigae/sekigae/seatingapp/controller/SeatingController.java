package sekigae.sekigae.seatingapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
  @Operation(summary = "座席表表示", description = "現在の座席表を表示します。",
      responses = {
          @ApiResponse(responseCode = "200", description = "正常に座席表情報を取得しました。"),
          @ApiResponse(
              responseCode = "404",
              description = "座席情報が見つかりません。",
              content = @Content(
                  mediaType = "application/json",
                  examples = @ExampleObject(
                      name = "NotFoundExample",
                      summary = "座席表未登録",
                      description = "座席表情報が存在しない場合",
                      value = """
                          {
                            "error": "Not Found",
                            "message": "座席表情報が見つかりませんでした。",
                            "code": 404
                          }""")))})
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
   * 指定した行数・列数で座席表を表示します。
   *
   * @param rows    指定行数
   * @param columns 指定列数
   * @return 引数で指定した行列数を持つ座席表
   */
  @Operation(summary = "座席表表示", description = "指定行列数の座席表を表示します。",
      responses = {
          @ApiResponse(responseCode = "200", description = "正常に座席表情報を取得しました。"),
          @ApiResponse(
              responseCode = "400",
              description = "不正な行列数形式です。",
              content = @Content(
                  mediaType = "application/json",
                  examples = @ExampleObject(
                      name = "BadRequestExample",
                      summary = "行列数形式エラー",
                      description = "行列数が数値でない場合のエラー例",
                      value = """
                          {
                            "error": "Bad Request",
                            "message": "行列の形式が不正です。１以上の整数値を指定してください。",
                            "code": 400
                          }"""))),
          @ApiResponse(
              responseCode = "404",
              description = "指定された行列数の座席表は作成できません。",
              content = @Content(
                  mediaType = "application/json",
                  examples = @ExampleObject(
                      name = "NotFoundExample",
                      summary = "座席表不成立",
                      description = "指定した行列数の座席表を作成することができない場合",
                      value = """
                          {
                            "error": "Not Found",
                            "message": "指定座席表を作成することができませんでした。",
                            "code": 404
                          }""")))})
  @Parameter(
      name = "rows",
      description = "新規作成する座席の列数（自然数値のみ）",
      required = true,
      example = "7")
  @Parameter(
      name = "columns",
      description = "新規作成する座席の行数（自然数値のみ）",
      required = true,
      example = "6")
  @GetMapping("/chart")
  public String showSeatingChartWithParams(
      @RequestParam(value = "rows", defaultValue = "6") int rows,
      @RequestParam(value = "columns", defaultValue = "5") int columns,
      Model model) {
    return showSeatingChartWithSize(rows, columns, model);
  }

  /**
   * 指定した行数・列数で座席表を表示します。
   *
   * @param rows    指定行数
   * @param columns 指定列数
   * @return 引数で指定した行列数を持つ座席表
   */
  @Operation(summary = "座席表表示", description = "指定行列数の座席表を表示します。",
      responses = {
          @ApiResponse(responseCode = "200", description = "正常に座席表情報を取得しました。"),
          @ApiResponse(
              responseCode = "400",
              description = "不正な行列数形式です。",
              content = @Content(
                  mediaType = "application/json",
                  examples = @ExampleObject(
                      name = "BadRequestExample",
                      summary = "行列数形式エラー",
                      description = "行列数が数値でない場合のエラー例",
                      value = """
                          {
                            "error": "Bad Request",
                            "message": "行列の形式が不正です。１以上の整数値を指定してください。",
                            "code": 400
                          }"""))),
          @ApiResponse(
              responseCode = "404",
              description = "指定された行列数の座席表は作成できません。",
              content = @Content(
                  mediaType = "application/json",
                  examples = @ExampleObject(
                      name = "NotFoundExample",
                      summary = "座席表不成立",
                      description = "指定した行列数の座席表を作成することができない場合",
                      value = """
                          {
                            "error": "Not Found",
                            "message": "指定座席表を作成することができませんでした。",
                            "code": 404
                          }""")))})
  @Parameter(
      name = "rows",
      description = "新規作成する座席の列数（自然数値のみ）",
      required = true,
      example = "7")
  @Parameter(
      name = "columns",
      description = "新規作成する座席の行数（自然数値のみ）",
      required = true,
      example = "6")
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
   * 座席表を再生成します（シャッフル等）
   *
   * @param rows    行数
   * @param columns 列数
   * @return シャッフル後の新たな座席表
   */
  @Operation(summary = "座席のシャッフル", description = "現在の座席表をシャッフルします。",
      responses = {
          @ApiResponse(responseCode = "200", description = "正常に座席表情報を入れ替えました。"),
          @ApiResponse(
              responseCode = "400",
              description = "不正な行列数形式です。",
              content = @Content(
                  mediaType = "application/json",
                  examples = @ExampleObject(
                      name = "BadRequestExample",
                      summary = "行列数形式エラー",
                      description = "行列数が数値でない場合のエラー例",
                      value = """
                          {
                            "error": "Bad Request",
                            "message": "行列の形式が不正です。１以上の整数値を指定してください。",
                            "code": 400
                          }"""))),
          @ApiResponse(
              responseCode = "404",
              description = "指定された行列数の座席表は作成できません。",
              content = @Content(
                  mediaType = "application/json",
                  examples = @ExampleObject(
                      name = "NotFoundExample",
                      summary = "座席表不成立",
                      description = "指定した行列数の座席表を作成することができない場合",
                      value = """
                          {
                            "error": "Not Found",
                            "message": "指定座席表を作成することができませんでした。",
                            "code": 404
                          }""")))})
  @Parameter(
      name = "rows",
      description = "座席の列数（自然数値のみ）",
      required = false,
      example = "7")
  @Parameter(
      name = "columns",
      description = "座席の行数（自然数値のみ）",
      required = false,
      example = "6")
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
  @Operation(summary = "座席のシャッフル", description = "現在の座席表をシャッフルします。",
      responses = {
          @ApiResponse(responseCode = "200", description = "正常に座席表情報を入れ替えました。"),
          @ApiResponse(
              responseCode = "400",
              description = "不正な行列数形式です。",
              content = @Content(
                  mediaType = "application/json",
                  examples = @ExampleObject(
                      name = "BadRequestExample",
                      summary = "行列数形式エラー",
                      description = "行列数が数値でない場合のエラー例",
                      value = """
                          {
                            "error": "Bad Request",
                            "message": "行列の形式が不正です。１以上の整数値を指定してください。",
                            "code": 400
                          }"""))),
          @ApiResponse(
              responseCode = "404",
              description = "指定された行列数の座席表は作成できません。",
              content = @Content(
                  mediaType = "application/json",
                  examples = @ExampleObject(
                      name = "NotFoundExample",
                      summary = "座席表不成立",
                      description = "指定した行列数の座席表を作成することができない場合",
                      value = """
                          {
                            "error": "Not Found",
                            "message": "指定座席表を作成することができませんでした。",
                            "code": 404
                          }""")))})
  @Parameter(
      name = "rows",
      description = "座席の列数（自然数値のみ）",
      required = false,
      example = "7")
  @Parameter(
      name = "columns",
      description = "座席の行数（自然数値のみ）",
      required = false,
      example = "6")
  @Parameter(
      name = "preventSameGender",
      description = "同性同士を隣接させない制約",
      required = false,
      example = "null")
  @Parameter(
      name = "alternateColumns",
      description = "列ごとに性別を交互配置する制約",
      required = false,
      example = "true")
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
   * 現在の座席配置を保存します。
   */
  @Operation(summary = "座席表保存", description = "現在の座席表を保存します。",
      responses = {
          @ApiResponse(responseCode = "200", description = "正常に座席表情報を保存しました。"),
          @ApiResponse(
              responseCode = "404",
              description = "現在の座席表は保存することができません。",
              content = @Content(
                  mediaType = "application/json",
                  examples = @ExampleObject(
                      name = "NotFoundExample",
                      summary = "保存不成立",
                      description = "現在の座席表を保存することができない場合",
                      value = """
                          {
                            "error": "Not Found",
                            "message": "現在の座席表を保存することができませんでした。",
                            "code": 404
                          }""")))})
  @Parameter(
      name = "rows",
      description = "座席の列数（自然数値のみ）",
      required = true,
      example = "7")
  @Parameter(
      name = "columns",
      description = "座席の行数（自然数値のみ）",
      required = true,
      example = "6")
  @Parameter(
      name = "snapshotName",
      description = "保存する座席表名",
      required = false,
      example = "試験用座席")
  @Parameter(
      name = "snapshotName",
      description = "定期テスト時に使用",
      required = false,
      example = "true")
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
  @Operation(summary = "座席表復元", description = "保存した座席表を復元します。",
      responses = {
          @ApiResponse(responseCode = "200", description = "正常に座席表情報を復元しました。"),
          @ApiResponse(
              responseCode = "400",
              description = "不正な座席表形式です。",
              content = @Content(
                  mediaType = "application/json",
                  examples = @ExampleObject(
                      name = "BadRequestExample",
                      summary = "座席表形式エラー",
                      description = "登録されていない生徒が含まれている座席表を復元しようとした場合のエラー例",
                      value = """
                          {
                            "error": "Bad Request",
                            "message": "座席表復元時にエラーが発生しました。",
                            "code": 400
                          }""")))})
  @Parameter(
      name = "snapshotId",
      description = "保存済み座席表のID(自動採番されています（自然数値のみ））",
      required = true,
      example = "1")
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
  @Operation(summary = "座席表削除", description = "保存中の座席表を選択削除します。",
      responses = {
          @ApiResponse(responseCode = "200", description = "正常に座席表情報を削除しました。"),})
  @Parameter(
      name = "snapshotId",
      description = "保存済み座席表のID(自動採番されています（自然数値のみ））",
      required = true,
      example = "1")
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