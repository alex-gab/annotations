package com.example.processor;

import com.example.annotation.Immutable;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.tools.Diagnostic;
import java.util.Set;

@SupportedAnnotationTypes("com.com.example.annotation.Immutable")
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class SimpleAnnotationProcessor extends AbstractProcessor {
    @Override
    @SuppressWarnings("Duplicates")
    public boolean process(final Set<? extends TypeElement> annotations,
                           final RoundEnvironment roundEnv) {

        for (final Element element : roundEnv.getElementsAnnotatedWith(Immutable.class)) {
            if (element instanceof TypeElement) {
                final TypeElement typeElement = (TypeElement) element;

                for (final Element eclosedElement : typeElement.getEnclosedElements()) {
                    if (eclosedElement instanceof VariableElement) {
                        final VariableElement variableElement = (VariableElement) eclosedElement;

                        if (!variableElement.getModifiers().contains(Modifier.FINAL)) {
                            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR,
                                    String.format("Class '%s' is annotated as @Immutable, " +
                                                    "but field '%s' is not declared as final ",
                                            typeElement.getSimpleName(), variableElement.getSimpleName()
                                    )
                            );
                        }
                    }
                }
            }
        }

        // Claiming that annotations have been processed by this processor
        return true;
    }
}
