package org.pih.biometric.service.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.GATEWAY_TIMEOUT)
public class DeviceTimeoutException extends BiometricServiceException {

    public DeviceTimeoutException() {
        super("Device timeout");
    }


}
