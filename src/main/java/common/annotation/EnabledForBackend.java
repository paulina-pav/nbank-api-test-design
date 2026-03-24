package common.annotation;

import common.backendprofiles.BackendProfile;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;

@Retention(RetentionPolicy.RUNTIME)
@Target({METHOD, TYPE})
@ExtendWith(BackendProfileCondition.class)
public @interface EnabledForBackend {
    BackendProfile[] value();

}
