package com.bluelinelabs.logansquare.processor.type.field;

import com.bluelinelabs.logansquare.processor.JsonMapperLoaderInjector;
import com.bluelinelabs.logansquare.processor.TypeUtils;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec.Builder;
import com.squareup.javapoet.TypeName;

import java.util.List;

import static com.bluelinelabs.logansquare.processor.ObjectMapperInjector.JSON_GENERATOR_VARIABLE_NAME;
import static com.bluelinelabs.logansquare.processor.ObjectMapperInjector.JSON_PARSER_VARIABLE_NAME;

public class JsonFieldType extends FieldType {

    private final ClassName mClassName;
    private final String mMapperVariableName;

    public JsonFieldType(ClassName className) {
        mClassName = className;

        final String mapperClassName = TypeUtils.getInjectedFQCN(className);
        mMapperVariableName = JsonMapperLoaderInjector.getMapperVariableName(mapperClassName);
        usedMappersFromLoader.add(ClassName.bestGuess(mapperClassName));
    }

    @Override
    public TypeName getTypeName() {
        return mClassName;
    }

    @Override
    public TypeName getNonPrimitiveTypeName() {
        return mClassName;
    }

    @Override
    public void parse(Builder builder, int depth, String setter, Object... setterFormatArgs) {
        setter = replaceLastLiteral(setter, "$L.parse($L)");
        builder.addStatement(setter, expandStringArgs(setterFormatArgs, mMapperVariableName, JSON_PARSER_VARIABLE_NAME));
    }

    @Override
    public void serialize(Builder builder, int depth, String fieldName, List<String> processedFieldNames, String getter, boolean isObjectProperty, boolean checkIfNull, boolean writeIfNull, boolean writeCollectionElementIfNull) {

        if (checkIfNull) {
            builder.beginControlFlow("if ($L != null)", getter);
        }

        if (isObjectProperty) {
            builder.addStatement("$L.writeFieldName($S)", JSON_GENERATOR_VARIABLE_NAME, fieldName);
        }

        builder.addStatement("$L.serialize($L, $L, true)", mMapperVariableName, getter, JSON_GENERATOR_VARIABLE_NAME);

        if (checkIfNull) {
            if (writeIfNull) {
                builder.nextControlFlow("else");

                if (isObjectProperty) {
                    builder.addStatement("$L.writeFieldName($S)", JSON_GENERATOR_VARIABLE_NAME, fieldName);
                }
                builder.addStatement("$L.writeNull()", JSON_GENERATOR_VARIABLE_NAME);
            }

            builder.endControlFlow();
        }
    }
}
