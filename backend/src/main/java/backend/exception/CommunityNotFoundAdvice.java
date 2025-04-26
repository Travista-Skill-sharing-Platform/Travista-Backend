package backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

public class CommunityNotFoundAdvice {

    @ResponseBody
    @ExceptionHandler(CommunityNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    String communityNotFoundHandler(CommunityNotFoundException ex) {
        return ex.getMessage();
    }
}
