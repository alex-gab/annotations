package com.example;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;

import static org.apache.commons.lang3.StringUtils.isEmpty;

public final class FactoryAnnotatedClass {
    private final TypeElement annotatedClassElement;
    private final String qualifiedFactoryGroupName;
    private final String simpleFactoryGroupName;
    private final String id;

    public FactoryAnnotatedClass(TypeElement classElement) throws ProcessingException {
        annotatedClassElement = classElement;
        final Factory annotation = classElement.getAnnotation(Factory.class);
        id = annotation.id();

        if (isEmpty(id)) {
            throw new ProcessingException(classElement,
                    "id() in @%s for class %s is null or empty! that's not allowed",
                    Factory.class.getSimpleName(),
                    classElement.getQualifiedName().toString());
        }

        String qualifiedFactoryGroupName;
        String simpleFactoryGroupName;
        try {
            final Class clazz = annotation.type();
            qualifiedFactoryGroupName = clazz.getCanonicalName();
            simpleFactoryGroupName = clazz.getSimpleName();
        } catch (MirroredTypeException mte) {
            final DeclaredType classTypeMirror = (DeclaredType) mte.getTypeMirror();
            final TypeElement classTypeElement = (TypeElement) classTypeMirror.asElement();
            qualifiedFactoryGroupName = classTypeElement.getQualifiedName().toString();
            simpleFactoryGroupName = classTypeElement.getSimpleName().toString();
        }
        this.qualifiedFactoryGroupName = qualifiedFactoryGroupName;
        this.simpleFactoryGroupName = simpleFactoryGroupName;
    }

    public final TypeElement getTypeElement() {
        return annotatedClassElement;
    }

    public final String getQualifiedFactoryGroupName() {
        return qualifiedFactoryGroupName;
    }

    public final String getSimpleFactoryGroupName() {
        return simpleFactoryGroupName;
    }

    public final String getId() {
        return id;
    }
}
