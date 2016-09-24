package com.example;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public final class FactoryGroupedClasses {
    private static final String SUFFIX = "Factory";

    private final String qualifiedFactoryGroupName;
    private final Map<String, FactoryAnnotatedClass> itemsMap = new LinkedHashMap<>();


    public FactoryGroupedClasses(String qualifiedFactoryGroupName) {
        this.qualifiedFactoryGroupName = qualifiedFactoryGroupName;
    }

    public final void add(FactoryAnnotatedClass toInsert) throws ProcessingException {
        FactoryAnnotatedClass existing = itemsMap.get(toInsert.getId());

        if (existing != null) {
            throw new ProcessingException(toInsert.getTypeElement(),
                    "Conflict: The class %s is annotated with @%s with id ='%s' but %s already uses the same id",
                    toInsert.getTypeElement().getQualifiedName().toString(),
                    Factory.class.getSimpleName(),
                    toInsert.getId(),
                    existing.getTypeElement().getQualifiedName().toString());
        }

        itemsMap.put(toInsert.getId(), toInsert);
    }

    public void generateCode(Elements elementUtils, Filer filer) throws IOException {
        final TypeElement superClassName = elementUtils.getTypeElement(qualifiedFactoryGroupName);
        final String factoryClassName = superClassName.getSimpleName() + SUFFIX;

        final PackageElement pkg = elementUtils.getPackageOf(superClassName);
        final String packageName = pkg.isUnnamed() ? null : pkg.getQualifiedName().toString();

        final MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder("create").
                addModifiers(Modifier.PUBLIC).
                addParameter(String.class, "id").
                returns(TypeName.get(superClassName.asType()));

        methodBuilder.beginControlFlow("if (id == null)").
                addStatement("throw new IllegalArgumentException($S)", "id is null!").
                endControlFlow();

        for (FactoryAnnotatedClass item : itemsMap.values()) {
            methodBuilder.beginControlFlow("if ($S.equals(id))", item.getId()).
                    addStatement("return new $L()",
                            item.getTypeElement().
                                    getQualifiedName().
                                    toString()).
                    endControlFlow();
        }

        methodBuilder.addStatement("throw new IllegalArgumentException($S + id)", "Unknown id = ");

        final TypeSpec typeSpec = TypeSpec.classBuilder(factoryClassName).
                addModifiers(Modifier.PUBLIC).
                addMethod(methodBuilder.build()).
                build();
        JavaFile.builder(packageName, typeSpec).build().writeTo(filer);
    }
}
