package com.flywhl.saa.jsonschema;

import com.flywhl.saa.common.exception.GlobalExceptionHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

/**
 * JSON Schema / 嵌套泛型 / 校验容错演示。
 *
 * @author flywhl
 */
@SpringBootApplication
@Import(GlobalExceptionHandler.class)
public class JsonSchemaDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(JsonSchemaDemoApplication.class, args);
    }
}
