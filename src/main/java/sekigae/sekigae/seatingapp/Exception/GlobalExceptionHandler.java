package sekigae.sekigae.seatingapp.Exception;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(IllegalArgumentException.class)
  public String handleIllegalArgumentException(IllegalArgumentException ex, Model model) {
    model.addAttribute("errorMessage", ex.getMessage());
    return "students/create"; // もう一度フォームを表示
  }
}
