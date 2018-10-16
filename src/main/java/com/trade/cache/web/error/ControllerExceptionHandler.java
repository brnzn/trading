package com.trade.cache.web.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ControllerExceptionHandler {
    private static final String MISSING_PARAM_MSG = "Missing request parameter. Supported parameters are vendor or symbol";
    private static final String MULTI_PARAM_MSG = "Requesting prices by Vendor and Symbol is not supported";

    private static Logger LOG = LoggerFactory.getLogger(ControllerExceptionHandler.class);

    @ExceptionHandler({MissingRequestParameterException.class})
    public ResponseEntity<ApiError> missingParameter(MissingRequestParameterException ex) {
        LOG.error("Handling missing parameter exception", ex);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiError(MISSING_PARAM_MSG));
    }

    @ExceptionHandler({MultipleRequestParametersException.class})
    public ResponseEntity<ApiError> multiParameters(MultipleRequestParametersException ex) {
        LOG.error("Handling multi parameters exception", ex);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiError(MULTI_PARAM_MSG));
    }

    @ExceptionHandler({Throwable.class})
    public ResponseEntity<ApiError> handleUnexpectedException(Throwable ex) {
        LOG.error("Handling Unexpected Exception", ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiError("Unexpected Error Occurred"));
    }
}
