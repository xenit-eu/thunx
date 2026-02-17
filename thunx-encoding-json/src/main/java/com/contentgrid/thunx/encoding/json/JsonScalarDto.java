package com.contentgrid.thunx.encoding.json;

import com.contentgrid.thunx.encoding.json.InvalidExpressionDataException.InvalidExpressionTypeException;
import com.contentgrid.thunx.encoding.json.InvalidExpressionDataException.InvalidExpressionValueException;
import com.contentgrid.thunx.predicates.model.Scalar;
import com.contentgrid.thunx.predicates.model.ThunkExpression;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
class JsonScalarDto<T> implements JsonExpressionDto {

    static final Map<Class<?>, String> SCALAR_TYPES = Map.of(
            String.class, "string",
            Number.class, "number",
            Boolean.class, "bool",
            LocalDate.class, "date",
            Instant.class, "instant",
            UUID.class, "uuid",
            Void.class, "null");

    private String type;

    @JsonInclude(Include.NON_NULL)
    private T value;

    public static JsonScalarDto of(@NonNull String string) {
        return new JsonScalarDto(SCALAR_TYPES.get(String.class), string);
    }

    public static JsonScalarDto of(@NonNull Number number) {
        return new JsonScalarDto(SCALAR_TYPES.get(Number.class), number);
    }

    public static JsonScalarDto of(@NonNull Boolean bool) {
        return new JsonScalarDto(SCALAR_TYPES.get(Boolean.class), bool);
    }

    public static JsonScalarDto of(@NonNull LocalDate localDate) {
        return new JsonScalarDto(SCALAR_TYPES.get(LocalDate.class), localDate.toString());
    }

    public static JsonScalarDto of(@NonNull Instant instant) {
        return new JsonScalarDto(SCALAR_TYPES.get(Instant.class), instant.toString());
    }

    public static JsonScalarDto of(@NonNull UUID uuid) {
        return new JsonScalarDto(SCALAR_TYPES.get(UUID.class), uuid.toString());
    }

    public static JsonScalarDto nullValue() {
        return new JsonScalarDto(SCALAR_TYPES.get(Void.class), null);
    }

    @JsonCreator
    public static JsonScalarDto of(@NonNull @JsonProperty("type") String type, @JsonProperty("value") Object value) {
        return new JsonScalarDto(type, value);
    }

    @Override
    public ThunkExpression<?> toExpression() throws InvalidExpressionDataException {
        switch (this.getType()) {
            case "string":
                if (this.value instanceof String stringValue) {
                    return Scalar.of(stringValue);
                }
                throw new InvalidExpressionValueException(this.value, String.class);
            case "number":
                if (this.value instanceof BigDecimal bigDecimalValue) {
                    return Scalar.of(bigDecimalValue);
                }
                if (this.value instanceof Integer intValue) {
                    return Scalar.of(intValue);
                }
                if (this.value instanceof Long longValue) {
                    return Scalar.of(longValue);
                }
                if (this.value instanceof Float floatValue) {
                    return Scalar.of(floatValue);
                }
                if (this.value instanceof Double doubleValue) {
                    return Scalar.of(doubleValue);
                }
                throw new InvalidExpressionValueException(this.value, Number.class);
            case "bool":
                if (this.value instanceof Boolean) {
                    return Scalar.of(Boolean.TRUE.equals(this.value));
                }
                throw new InvalidExpressionValueException(this.value, Boolean.class);
            case "date":
                if (this.value instanceof String stringValue) {
                    try {
                        return Scalar.of(LocalDate.parse(stringValue));
                    } catch (DateTimeParseException e) {
                        throw new InvalidExpressionValueException(this.value, LocalDate.class);
                    }
                }
                throw new InvalidExpressionValueException(this.value, LocalDate.class);
            case "instant":
                if (this.value instanceof String stringValue) {
                    try {
                        return Scalar.of(Instant.parse(stringValue));
                    } catch (DateTimeParseException e) {
                        throw new InvalidExpressionValueException(this.value, Instant.class);
                    }
                }
                throw new InvalidExpressionValueException(this.value, Instant.class);
            case "uuid":
                if (this.value instanceof String stringValue) {
                    try {
                        return Scalar.of(UUID.fromString(stringValue));
                    } catch (IllegalArgumentException e) {
                        throw new InvalidExpressionValueException(this.value, UUID.class);
                    }
                }
                throw new InvalidExpressionValueException(this.value, UUID.class);
            case "null":
                if (this.value != null) {
                    throw new InvalidExpressionValueException(this.value, Void.class);
                }
                return Scalar.nullValue();
            default:
                String message = String.format("Scalar type '%s' is not supported", this.getType());
                throw new UnsupportedOperationException(message);
        }
    }

    private static ScalarTypes getValidScalarType(String type) throws InvalidExpressionDataException {
        try {
            return ScalarTypes.valueOf(type);
        } catch (IllegalArgumentException iae) {
            throw new InvalidExpressionTypeException(type, String.format("Scalar type '%s' is not valid", type));
        }
    }

    enum ScalarTypes {
        STRING, NUMBER, BOOLEAN, NULL
    }

    static class Either<A, B, C, D> {

        private final A a;
        private final B b;
        private final C c;
        private final D d;

        private Either(A a, B b, C c, D d) {
            this.a = a;
            this.b = b;
            this.c = c;
            this.d = d;
        }

        static <A, B, C, D> Either<A,B,C,D> first(A a) {
            return new Either<>(a, null, null, null);
        }

        static <A, B, C, D> Either<A,B,C,D> second(B b) {
            return new Either<>(null, b, null, null);
        }

        static <A, B, C, D> Either<A,B,C,D> third(C c) {
            return new Either<>(null, null, c, null);
        }

        static <A, B, C, D> Either<A,B,C,D> fourth(D d) {
            return new Either<>(null, null, null, d);
        }

        public <T> T map(
                Function<A, T> aFunc,
                Function<B, T> bFunc,
                Function<C, T> cFunc,
                Function<D, T> dFunc) {

            return Optional.ofNullable(a).map(value -> aFunc.apply(value))
                    .or(() -> Optional.ofNullable(b).map(value -> bFunc.apply(value)))
                    .or(() -> Optional.ofNullable(c).map(value -> cFunc.apply(value)))
                    .or(() -> Optional.ofNullable(d).map(value -> dFunc.apply(value))).orElseThrow();
        }

        public void accept(
                Consumer<A> aFunc,
                Consumer<B> bFunc,
                Consumer<C> cFunc,
                Consumer<D> dFunc) {

            Optional.ofNullable(a).ifPresent(value -> aFunc.accept(value));
            Optional.ofNullable(b).ifPresent(value -> bFunc.accept(value));
            Optional.ofNullable(c).ifPresent(value -> cFunc.accept(value));
            Optional.ofNullable(d).ifPresent(value -> dFunc.accept(value));
        }


    }

    private static class EitherSerializer extends StdSerializer<Either<String, Number, Boolean, Void>> {

        /**
         * Empty constructor required by Jackons' {@link JsonSerialize} annotation.
         */
        public EitherSerializer(){
            this(null);
        }

        public EitherSerializer(Class<Either<String, Number, Boolean, Void>> type) {
            super(type);
        }

        @Override
        public void serialize(Either<String, Number, Boolean, Void> value, JsonGenerator gen,
                SerializerProvider provider) throws IOException {
            value.accept(
                    string -> unchecked(() -> gen.writeString(string)),
                    number -> unchecked(() -> gen.writeNumber(String.valueOf(number))),
                    bool -> unchecked(() -> gen.writeBoolean(bool)),
                    nothing -> unchecked(() -> gen.writeNull())
            );
        }

        private static void unchecked(IOExceptionThrowingRunnable runnable) {
            try {
                runnable.run();
            } catch (IOException ioe) {
                throw new UncheckedIOException(ioe);
            }
        }

        @FunctionalInterface
        interface IOExceptionThrowingRunnable {
            void run() throws IOException;
        }

    }

}
