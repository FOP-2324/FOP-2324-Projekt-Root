package projekt.util;

import java.util.function.BiFunction;
import java.util.function.Predicate;

public interface MockClass<T> {

    T getDelegate();

    Predicate<String> getUseDelegate();

    void setUseDelegate(Predicate<String> useDelegate) ;

    BiFunction<String, Object[], ?> getMethodAction();

    void setMethodAction(BiFunction<String, Object[], ?> methodAction);
}
