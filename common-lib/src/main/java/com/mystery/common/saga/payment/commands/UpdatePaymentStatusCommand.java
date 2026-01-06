package com.mystery.common.saga.payment.commands;

import com.mystery.common.entity.Payment;
import com.mystery.common.saga.command.BaseCommand;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

/**
 * Command to update payment object.
 */
@Getter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class UpdatePaymentStatusCommand extends BaseCommand {

    private Payment payment;

    public UpdatePaymentStatusCommand(String commandId, Long sagaId, Instant timestamp, 
                                     Payment payment) {
        super(commandId, sagaId, timestamp);
        this.payment = payment;
    }

    public static UpdatePaymentStatusCommand create(long sagaId, Payment payment) {
        return new UpdatePaymentStatusCommand(
            java.util.UUID.randomUUID().toString(),
            sagaId,
            Instant.now(),
            payment
        );
    }
}
