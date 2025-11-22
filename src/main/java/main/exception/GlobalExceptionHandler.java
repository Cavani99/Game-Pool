package main.exception;

import feign.FeignException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(value = NoResourceFoundException.class)
    public ModelAndView handleNotFoundExceptions() {
        return new ModelAndView("/exceptions-views/not-found");
    }

    @ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
    @ExceptionHandler(value = FeignException.class)
    public ModelAndView handleAPIConnectionError() {
        return new ModelAndView("/exceptions-views/feign-error");
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = Exception.class)
    public ModelAndView handleOtherExceptions() {
        return new ModelAndView("/exceptions-views/default-error");
    }
}
