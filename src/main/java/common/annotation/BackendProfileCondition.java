package common.annotation;

import api.configs.Config;
import common.backendprofiles.BackendProfile;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

public class BackendProfileCondition implements ExecutionCondition {

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        var element = context.getElement();

        if (element.isEmpty()) {
            return ConditionEvaluationResult.enabled("No test element found");
        }

        EnabledForBackend annotation = element.get().getAnnotation(EnabledForBackend.class);

        if (annotation == null) {
            annotation = context.getTestClass()
                    .map(testClass -> testClass.getAnnotation(EnabledForBackend.class))
                    .orElse(null);
        }

        if (annotation == null) {
            return ConditionEvaluationResult.enabled("No @EnabledForBackend annotation found");
        }

        String activeProfileRaw = Config.getProperty("backend.profile");

        if (activeProfileRaw == null || activeProfileRaw.isBlank()) {
            return ConditionEvaluationResult.disabled(
                    "Property 'backend.profile' is missing or blank"
            );
        }

        BackendProfile activeProfile;
        try {
            activeProfile = BackendProfile.valueOf(activeProfileRaw.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return ConditionEvaluationResult.disabled(
                    "Unknown backend profile in config: " + activeProfileRaw
            );
        }

        boolean matches = java.util.Arrays.asList(annotation.value()).contains(activeProfile);

        if (matches) {
            return ConditionEvaluationResult.enabled(
                    "Test is enabled for backend profile: " + activeProfile
            );
        }

        return ConditionEvaluationResult.disabled(
                "Test is disabled. Active profile: " + activeProfile
                        + ", allowed profiles: " + java.util.Arrays.toString(annotation.value())
        );
    }


        /*
        BackendProfile activeProfile;
try {
    activeProfile = ...
} catch (...) {
    return ...
}
         */
}

    
