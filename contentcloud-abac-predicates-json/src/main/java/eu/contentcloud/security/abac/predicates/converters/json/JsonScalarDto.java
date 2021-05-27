package eu.contentcloud.security.abac.predicates.converters.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import eu.contentcloud.abac.predicates.model.Expression;
import eu.contentcloud.abac.predicates.model.Scalar;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Value;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = false)
class JsonScalarDto<T> implements JsonExpressionDto {

    enum ScalarType {
        STRING,
        NUMBER,
        BOOL,
        NULL
    }

    static final Map<Class<?>, String> SCALAR_TYPES = Map.of(
            String.class, "string",
            Number.class, "number",
            Boolean.class, "bool",
            Void.class, "null");

    private String type;
//    private T value;

//    @JsonSerialize(using = EitherSerializer.class)
    private Either<String, Number, Boolean, Void> value;

    public Object getValue() {
        return this.value.map(
                string -> string,
                number -> number,
                bool -> bool,
                nothing -> null);
    }

    public static JsonScalarDto of(@NonNull String string) {
        return new JsonScalarDto(SCALAR_TYPES.get(String.class), Either.first(string));
    }

    public static JsonScalarDto of(@NonNull Number number) {
        return new JsonScalarDto(SCALAR_TYPES.get(Number.class), Either.second(number));
    }

    public static JsonScalarDto of(@NonNull Boolean bool) {
        return new JsonScalarDto(SCALAR_TYPES.get(Boolean.class), Either.third(bool));
    }

    private static JsonScalarDto nullValue() {
        return new JsonScalarDto(SCALAR_TYPES.get(Void.class), Either.fourth(null));
    }

    @JsonCreator
    public static JsonScalarDto of(@JsonProperty("type") String type, @JsonProperty("value") Object value) {
        return null;
    }

    @Override
    public Expression<?> toExpression() {
//        switch (this.getType()) {
//            case "string":
//                return Scalar.of((String) this.getValue());
//            case "number":
//                return Scalar.of((Number) this.getValue());
//            case "bool":
//                return Scalar.of(this.getValue().equals(true));
//            case "null":
//                return Scalar.nullValue();
//            default:
//                throw new UnsupportedOperationException("type '" + this.getType() + "' not supported");
//        }
        return this.value.map(
                string -> Scalar.of(string),
                number -> Scalar.of(number),
                bool -> Scalar.of(bool),
                nothing -> Scalar.nullValue()
        );
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
