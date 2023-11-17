package com.danielvm.destiny2bot.annotation;

import com.danielvm.destiny2bot.validator.SignatureValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target(ElementType.PARAMETER)
@Constraint(validatedBy = SignatureValidator.class)
public @interface ValidSignature {

    String message() default "Signature is invalid";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
