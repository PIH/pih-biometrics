package org.pih.biometric.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_GATEWAY)
public class BadScanException extends BiometricServiceException {

    public BadScanException(String message) {
        super(message);
    }

}
