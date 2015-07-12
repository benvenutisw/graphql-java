package graphql.introspection;


import graphql.Scalars;
import graphql.schema.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static graphql.Scalars.*;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLObjectType.*;

public class Schema {

    public enum TypeKind {
        SCALAR,
        OBJECT,
        INTERFACE,
        UNION,
        ENUM,
        INPUT_OBJECT,
        LIST,
        NON_NULL
    }

    GraphQLEnumType __TypeKind = GraphQLEnumType.newEnum()
            .name("__TypeKind")
            .description("An enum describing what kind of type a given __Type is")
            .value("SCALAR", TypeKind.SCALAR, "Indicates this type is a scalar.")
            .value("OBJECT", TypeKind.OBJECT, "Indicates this type is an object. `fields` and `interfaces` are valid fields.")
            .value("INTERFACE", TypeKind.INTERFACE, "Indicates this type is an interface. `fields` and `possibleTypes` are valid fields.")
            .value("UNION", TypeKind.UNION, "Indicates this type is a union. `possibleTypes` is a valid field.")
            .value("ENUM", TypeKind.ENUM, "Indicates this type is an enum. `enumValues` is a valid field.")
            .value("INPUT_OBJECT", TypeKind.INPUT_OBJECT, "Indicates this type is an input object. `inputFields` is a valid field.")
            .value("LIST", TypeKind.LIST, "Indicates this type is a list. `ofType` is a valid field.")
            .value("NON_NULL", TypeKind.NON_NULL, "Indicates this type is a non-null. `ofType` is a valid field.")
            .build();

    DataFetcher kindDataFetcher = new DataFetcher() {
        @Override
        public Object get(DataFetchingEnvironment environment) {
            Object type = environment.getSource();
            if (type instanceof GraphQLScalarType) {
                return TypeKind.SCALAR;
            } else if (type instanceof GraphQLObjectType) {
                return TypeKind.OBJECT;
            } else if (type instanceof GraphQLInterfaceType) {
                return TypeKind.INTERFACE;
            } else if (type instanceof GraphQLUnionType) {
                return TypeKind.UNION;
            } else if (type instanceof GraphQLEnumType) {
                return TypeKind.ENUM;
            } else if (type instanceof GraphQLInputObjectType) {
                return TypeKind.INPUT_OBJECT;
            } else if (type instanceof GraphQLList) {
                return TypeKind.LIST;
            } else if (type instanceof GraphQLNonNull) {
                return TypeKind.NON_NULL;
            } else {
                throw new RuntimeException("Unknown kind of type: " + type);
            }
        }
    };

    GraphQLObjectType __Field = newObject()
            .name("__Field")
            .build();


    GraphQLObjectType __InputValue = newObject()
            .name("__Field")
            .build();

    GraphQLObjectType __EnumValue = newObject()
            .name("__Field")
            .build();

    DataFetcher fieldsFetcher = new DataFetcher() {
        @Override
        public Object get(DataFetchingEnvironment environment) {
            Object type = environment.getSource();
            boolean includeDeprecated = environment.getArgument("includeDeprecated");
            if (type instanceof GraphQLFieldsContainer) {
                GraphQLFieldsContainer fieldsContainer = (GraphQLFieldsContainer) type;
                List<GraphQLFieldDefinition> fieldDefinitions = fieldsContainer.getFieldDefinitions();
                if (includeDeprecated) return fieldDefinitions;
                List<GraphQLFieldDefinition> filtered = new ArrayList<>(fieldDefinitions);
                for (GraphQLFieldDefinition fieldDefinition : fieldDefinitions) {
                    if (fieldDefinition.isDeprecated()) filtered.remove(fieldDefinition);
                }
                return filtered;
            }
            return null;
        }
    };

    DataFetcher interfacesFetcher = new DataFetcher() {
        @Override
        public Object get(DataFetchingEnvironment environment) {
            Object type = environment.getSource();
            if (type instanceof GraphQLObjectType) {
                return ((GraphQLObjectType) type).getInterfaces();
            }
            return null;
        }
    };

    DataFetcher possibleTypesFetcher = new DataFetcher() {
        @Override
        public Object get(DataFetchingEnvironment environment) {
            Object type = environment.getSource();
            if (type instanceof GraphQLInterfaceType) {
                return SchemaUtil.findImplementations(environment.getGraphQLSchema(), (GraphQLInterfaceType) type);
            }
            if (type instanceof GraphQLUnionType) {
                return ((GraphQLUnionType) type).getTypes();
            }
            return null;
        }
    };

    DataFetcher enumValuesTypesFetcher = new DataFetcher() {
        @Override
        public Object get(DataFetchingEnvironment environment) {
            Object type = environment.getSource();
            boolean includeDeprecated = environment.getArgument("includeDeprecated");
            if (type instanceof GraphQLEnumType) {
                List<GraphQLEnumValueDefinition> values = ((GraphQLEnumType) type).getValues();
                if (includeDeprecated) return values;
                List<GraphQLEnumValueDefinition> filtered = new ArrayList<>(values);
                for (GraphQLEnumValueDefinition valueDefinition : values) {
                    if (valueDefinition.isDeprecated()) filtered.remove(valueDefinition);
                }
                return filtered;
            }
            return null;
        }
    };

    DataFetcher inputFieldsFetcher = new DataFetcher() {
        @Override
        public Object get(DataFetchingEnvironment environment) {
            Object type = environment.getSource();
            if (type instanceof GraphQLInputObjectType) {
                return ((GraphQLInputObjectType) type).getFields();
            }
            return null;
        }
    };


    GraphQLObjectType __Type = newObject()
            .name("__Type")
            .field(newFieldDefinition()
                    .name("kind")
                    .type(new GraphQLNonNull(__TypeKind))
                    .dataFetcher(kindDataFetcher)
                    .build())
            .field(newFieldDefinition()
                    .name("name")
                    .type(GraphQLString)
                    .build())
            .field(newFieldDefinition()
                    .name("description")
                    .type(GraphQLString)
                    .build())
            .field(newFieldDefinition()
                    .name("fields")
                    .type(new GraphQLList(new GraphQLNonNull(__Field)))
                    .argument(GraphQLFieldArgument.newFieldArgument()
                            .name("includeDeprecated")
                            .type(GraphQLBoolean)
                            .defaultValue(false)
                            .build())
                    .dataFetcher(fieldsFetcher)
                    .build())
            .field(newFieldDefinition()
                    .name("interfaces")
                    .type(new GraphQLList(new GraphQLNonNull(new GraphQLTypeReference("__Type"))))
                    .dataFetcher(interfacesFetcher)
                    .build())
            .field(newFieldDefinition()
                    .name("possibleTypes")
                    .type(new GraphQLList(new GraphQLNonNull(new GraphQLTypeReference("__Type"))))
                    .dataFetcher(possibleTypesFetcher)
                    .build())
            .field(newFieldDefinition()
                    .name("enumValues")
                    .type(new GraphQLList(new GraphQLNonNull(__EnumValue)))
                    .argument(GraphQLFieldArgument.newFieldArgument()
                            .name("includeDeprecated")
                            .type(GraphQLBoolean)
                            .defaultValue(false)
                            .build())
                    .dataFetcher(enumValuesTypesFetcher)
                    .build())
            .field(newFieldDefinition()
                    .name("inputFields")
                    .type(new GraphQLList(new GraphQLNonNull(__InputValue)))
                    .dataFetcher(inputFieldsFetcher)
                    .build())
            .build();


    GraphQLObjectType __Schema =
            newObject()
                    .name("__Schema")
                    .description("A GraphQL Schema defines the capabilities" +
                            " of a GraphQL server. It exposes all available types and directives on " +
                            "'the server, as well as the entry points for query and  'mutation operations.")
                    .field(newFieldDefinition()
                            .name("types")
                            .description("A list of all types supported by this server.")
//                    .type(new GraphQLNonNull(new GraphQLList(new GraphQLNonNull(__Type)))
//                            .dataFetcher()
                            .build())
                    .build();
}
