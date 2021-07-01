package io.quarkiverse.operatorsdk.deployment;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Type;
import org.jboss.jandex.Type.Kind;

import io.javaoperatorsdk.operator.ControllerUtils;
import io.quarkiverse.operatorsdk.runtime.BuildTimeControllerConfiguration;

class BuildTimeHybridControllerConfiguration {

    private final BuildTimeControllerConfiguration externalConfiguration;
    private final AnnotationInstance controllerAnnotation;
    private final AnnotationInstance delayRegistrationAnnotation;

    public BuildTimeHybridControllerConfiguration(
            BuildTimeControllerConfiguration externalConfiguration,
            AnnotationInstance controllerAnnotation,
            AnnotationInstance delayRegistrationAnnotation) {
        this.externalConfiguration = externalConfiguration;
        this.controllerAnnotation = controllerAnnotation;
        this.delayRegistrationAnnotation = delayRegistrationAnnotation;
    }

    boolean generationAware() {
        return ValueExtractor.extract(
                externalConfiguration,
                controllerAnnotation, c -> c.generationAware,
                "generationAwareEventProcessing",
                AnnotationValue::asBoolean,
                () -> true);
    }

    Type eventType() {
        return ValueExtractor.extract(
                externalConfiguration,
                delayRegistrationAnnotation, c -> c.delayRegistrationUntilEvent
                        .filter(s -> void.class.getName().equals(s))
                        .map(DotName::createSimple)
                        .map(dn -> Type.create(dn, Kind.CLASS)),
                "event",
                AnnotationValue::asClass,
                () -> null);
    }

    boolean delayedRegistration() {
        return ValueExtractor.extract(
                externalConfiguration,
                delayRegistrationAnnotation,
                c -> c.delayRegistrationUntilEvent.map(s -> void.class.getName().equals(s)),
                "event",
                v -> v.asClass().kind() != Kind.VOID,
                () -> false);
    }

    String name(String resourceControllerClassName) {
        // retrieve the controller's name
        final var defaultControllerName = ControllerUtils
                .getDefaultResourceControllerName(resourceControllerClassName);
        return ValueExtractor.annotationValueOrDefault(
                controllerAnnotation, "name", AnnotationValue::asString, () -> defaultControllerName);
    }
}
