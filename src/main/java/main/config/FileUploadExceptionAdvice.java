package main.config;

import main.web.dto.RegisterRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class FileUploadExceptionAdvice {
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ModelAndView handleMaxSizeException(MaxUploadSizeExceededException ex) {
        ModelAndView mav = new ModelAndView("register");
        mav.addObject("user", new RegisterRequest());
        mav.addObject("errorMessage", "File too large! Please upload an image under 10MB.");
        return mav;
    }
}
