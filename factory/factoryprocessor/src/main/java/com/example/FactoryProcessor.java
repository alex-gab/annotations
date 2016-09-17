package com.example;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableSet;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.Set;

import static javax.lang.model.SourceVersion.latestSupported;
import static javax.tools.Diagnostic.Kind.ERROR;

@AutoService(Processor.class)
public final class FactoryProcessor extends AbstractProcessor {
    private static final Class<Factory> FACTORY_CLASS = Factory.class;

    private Types typeUtils;
    private Elements elementUtils;
    private Filer filer;
    private Messager messager;

    @Override
    public final synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        typeUtils = processingEnv.getTypeUtils();
        elementUtils = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
    }

    @Override
    public final Set<String> getSupportedAnnotationTypes() {
        return ImmutableSet.of(FACTORY_CLASS.getCanonicalName());
    }

    @Override
    public final SourceVersion getSupportedSourceVersion() {
        return latestSupported();
    }

    @Override
    public final boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {
            for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(FACTORY_CLASS)) {
                checkThatOnlyClassesAreAnnotatedWithFactory(annotatedElement);

                final TypeElement typeElement = (TypeElement) annotatedElement;
                final FactoryAnnotatedClass factoryClass = new FactoryAnnotatedClass(typeElement);
                checkValidClass(factoryClass);
            }
        } catch (ProcessingException e) {
            error(e.getElement(), e.getMessage());
        }
        return true;
    }

    private void checkThatOnlyClassesAreAnnotatedWithFactory(Element annotatedElement) throws ProcessingException {
        if (annotatedElement.getKind() != ElementKind.CLASS) {
            throw new ProcessingException(annotatedElement,
                    "Only classes can be annotated with @%s",
                    FACTORY_CLASS.getSimpleName());
        }
    }

    private void error(Element element, String message) {
        messager.printMessage(ERROR,
                message,
                element);
    }

    private void checkValidClass(FactoryAnnotatedClass item) throws ProcessingException {
        final TypeElement classElement = item.getAnnotatedClassElement();
        final String qualifiedFactoryGroupName = item.getQualifiedFactoryGroupName();
        checkIfClassElementIsPublic(classElement);
        checkIfClassElementIsNotAbstract(classElement);
        checkIfClassElementSubclassesFactoryType(classElement, qualifiedFactoryGroupName);
        checkIfAnEmptyPublicConstructorIsGiven(classElement);
    }

    private void checkIfClassElementIsPublic(final TypeElement classElement) throws ProcessingException {
        if (!classElement.getModifiers().contains(Modifier.PUBLIC)) {
            throw new ProcessingException(classElement,
                    "The class %s is not public.",
                    classElement.getQualifiedName().toString());
        }
    }

    private void checkIfClassElementIsNotAbstract(TypeElement classElement) throws ProcessingException {
        if (!classElement.getModifiers().contains(Modifier.ABSTRACT)) {
            throw new ProcessingException(classElement,
                    "The class %s is abstract. You can't annotate abstract classes with @%",
                    classElement.getQualifiedName().toString(),
                    Factory.class.getSimpleName());
        }
    }

    private void checkIfClassElementSubclassesFactoryType(TypeElement classElement,
                                                          String qualifiedFactoryGroupName) throws ProcessingException {
        if (isAnInterface(qualifiedFactoryGroupName)) {
            checkInterfaceImplemented(classElement, qualifiedFactoryGroupName);
        } else {
            checkSubclassing(classElement, qualifiedFactoryGroupName);
        }
    }

    private boolean isAnInterface(String qualifiedFactoryGroupName) {
        return elementUtils.getTypeElement(qualifiedFactoryGroupName).getKind() == ElementKind.INTERFACE;
    }

    private void checkInterfaceImplemented(TypeElement classElement, String qualifiedFactoryGroupName) throws ProcessingException {
        final TypeElement superClassElement = elementUtils.getTypeElement(qualifiedFactoryGroupName);
        if (!classElement.getInterfaces().contains(superClassElement.asType())) {
            throw new ProcessingException(classElement,
                    "The class %s annotated with @%s must implement the interface %s",
                    classElement.getQualifiedName().toString(),
                    Factory.class.getSimpleName(),
                    qualifiedFactoryGroupName);
        }
    }

    private void checkSubclassing(TypeElement classElement, String qualifiedFactoryGroupName) throws ProcessingException {
        TypeElement currentClass = classElement;
        while (true) {
            final TypeMirror superclassType = currentClass.getSuperclass();
            if (superclassType.getKind() == TypeKind.NONE) {
                throw new ProcessingException(classElement,
                        "The class %s annotated with @%s must inherit from %s",
                        classElement.getQualifiedName().toString(), Factory.class.getSimpleName(),
                        qualifiedFactoryGroupName);
            }
            if (superclassType.toString().equals(qualifiedFactoryGroupName)) {
                break;
            }
            currentClass = (TypeElement) typeUtils.asElement(superclassType);
        }
    }

    private static void checkIfAnEmptyPublicConstructorIsGiven(TypeElement classElement) throws ProcessingException {
        boolean found = false;
        for (Element enclosed : classElement.getEnclosedElements()) {
            if (enclosed.getKind() == ElementKind.CONSTRUCTOR) {
                ExecutableElement constructorElement = (ExecutableElement) enclosed;
                if (constructorElement.getParameters().size() == 0 &&
                        constructorElement.getModifiers().contains(Modifier.PUBLIC)) {
                    // Found an empty constructor
                    found = true;
                }
            }
        }
        if (!found) {
            throw new ProcessingException(classElement,
                    "The class %s must provide an public empty default constructor",
                    classElement.getQualifiedName().toString());
        }
    }
}
