package com.shawn.fraud.application.detect;

import com.shawn.fraud.application.CommandResult;
import com.shawn.fraud.domain.FraudError;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

@Data
@NoArgsConstructor
public class FraudDetectCommandResult implements CommandResult {
    private boolean success;
    private FraudError error;

    public static FraudDetectCommandResult success() {
        return new FraudDetectCommandResult(true, null);
    }

    public static FraudDetectCommandResult fail(FraudError error) {
        Assert.notNull(error, () -> "error required");
        return new FraudDetectCommandResult(false, error);
    }

    public FraudDetectCommandResult(boolean success, FraudError error) {
        this.success = success;
        this.error = error;
    }

}