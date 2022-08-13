package io.shum.utils;

sealed public interface Maybe<T> permits Maybe.None, Maybe.Some {

    static <T> Maybe<T> of(T t) {
        if (t == null) {
            return new None<>();
        }
        return new Some<>(t);
    }

    final class Some<T> implements Maybe<T> {
        private final T t;

        public Some(T t) {
            this.t = t;
        }

        public T getValue() {
            return t;
        }
    }

    final class None<T> implements Maybe<T> {
    }

}

