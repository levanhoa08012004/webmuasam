package com.example.webmuasam.util.constant;

import com.example.webmuasam.util.validation.EnumPatternValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import lombok.experimental.FieldDefaults;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({METHOD, FIELD, CONSTRUCTOR, PARAMETER, TYPE_USE})
@Constraint(validatedBy = EnumPatternValidator.class)
public @interface EnumPattern {
    String name();
    String regexp();
    String message() default "{name} must match {regexp}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}