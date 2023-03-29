package ai.ecma.apphistoryservice.utils;

import ai.ecma.apphistoryservice.aop.AuditField;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.hibernate.proxy.HibernateProxy;

import javax.persistence.*;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

public class CommonUtils {
    public static final ObjectMapper objectMapper = new ObjectMapper().configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

    public static String camelToSnake(String camelCase) {
        StringBuilder snakeCase = new StringBuilder();
        for (int i = 0; i < camelCase.length(); i++) {
            char c = camelCase.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i != 0)
                    snakeCase.append('_');
                snakeCase.append(Character.toLowerCase(c));
            } else
                snakeCase.append(c);
        }
        return snakeCase.toString();
    }

    public static String getEntityName(Class<?> aClass) {
        Table table = aClass.getAnnotation(Table.class);
        Entity entity = aClass.getAnnotation(Entity.class);

        if (Objects.isNull(table) && Objects.isNull(entity))
            return CommonUtils.camelToSnake(aClass.getSimpleName());

        String name = "";

        //TABLE DAN NAME NI OLADI YOKI SNEAK CASE
        if (Objects.nonNull(table)) {
            String tableName = table.name();
            if (tableName.isBlank() || tableName.isEmpty())
                name = CommonUtils.camelToSnake(aClass.getSimpleName());
            else
                return name;
        }

        //ENTITY DAN NAME NI OLADI YOKI SNEAK CASE
        if (Objects.nonNull(entity)) {
            String entityName = entity.name();
            if (entityName.isEmpty() || entityName.isBlank())
                name = CommonUtils.camelToSnake(aClass.getSimpleName());
            else
                return name;
        }

        return name;
    }

    public static String getRowId(Object object) {
        try {
            Field field = object.getClass().getDeclaredField(AppConstant.ID);
            field.setAccessible(true);
            return field.get(object).toString();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<Field> getFields(Class<?> clazz) {
        List<Field> fieldNames = new ArrayList<>();
        for (Field field : clazz.getDeclaredFields()) {

            AuditField auditField = field.getAnnotation(AuditField.class);
            if (Objects.nonNull(auditField) && auditField.ignore())
                continue;

            boolean ignore = checkObjectRelation(field);
            if (ignore)
                continue;

            fieldNames.add(field);
        }
        return fieldNames;
    }

    public static List<Field> getRelationFields(Class<?> aClass) {
        return Arrays.stream(aClass.getDeclaredFields())
                .filter(CommonUtils::isRelationalField)
                .collect(Collectors.toList());
    }

    private static boolean isRelationalField(Field field) {
        boolean oneToOne = false;
        boolean manyToOne = false;
        if (field.getAnnotation(OneToOne.class) != null) {
            String mappedBy = field.getAnnotation(OneToOne.class).mappedBy();
            if (mappedBy.isEmpty())
                oneToOne = true;
        }

        if (field.getAnnotation(ManyToOne.class) != null)
            manyToOne = true;
        return manyToOne || oneToOne;
    }


    //USHBU ANNATATION LAR QO'YILGAN FIELD LARNI IGNORE QILINADI
    private static boolean checkObjectRelation(Field field) {
        OneToMany oneToMany = field.getAnnotation(OneToMany.class);
        ManyToMany manyToMany = field.getAnnotation(ManyToMany.class);
        OneToOne oneToOne = field.getAnnotation(OneToOne.class);
        ManyToOne manyToOne = field.getAnnotation(ManyToOne.class);
        ElementCollection elementCollection = field.getAnnotation(ElementCollection.class);
        Transient aTransient = field.getAnnotation(Transient.class);
        return Objects.nonNull(oneToMany) || Objects.nonNull(manyToMany)
                || Objects.nonNull(elementCollection) || Objects.nonNull(oneToOne)
                || Objects.nonNull(manyToOne) || Objects.nonNull(aTransient);
    }


    public static <T> T readObject(String json, String className) {
        try {
            String regex = "[<>]";

            String[] parts = className.split(regex);

            Class<?> rawType = Class.forName(parts[0]);

            //DEMAK ODDIY TYPE
            if (parts.length == 1)
                return (T) objectMapper.readValue(json, rawType);

            ParameterizedType parameterizeForSub = null;
            Class<?> innerClass = null;
            for (int i = parts.length - 1; i > 0; i--) {
                Class<?> aClass = Class.forName(parts[i]);
                if (innerClass == null) {
                    innerClass = Class.forName(parts[i]);
                    continue;
                } else if (parameterizeForSub == null) {
                    parameterizeForSub = TypeUtils.parameterize(aClass, innerClass);
                } else {
                    parameterizeForSub = TypeUtils.parameterize(innerClass, parameterizeForSub);
                }
                innerClass = aClass;
            }

            ParameterizedType parameterize = TypeUtils.parameterize(rawType, parameterizeForSub);

            JavaType javaType = TypeFactory.defaultInstance().constructType(parameterize);

            return objectMapper.readValue(json, javaType);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public static String toJsonWithoutIgnoreFields(Object obj, Set<String> ignoredFields) {
        try {

            //AGAR NULL BO'LSA OBJECT O'CHIRILGAN YOKI YANGI QO'SHILGAN
            if (Objects.isNull(obj))
                return null;

            JsonNode node = objectMapper.valueToTree(obj);

            ObjectNode objectNode = (ObjectNode) node;

            //KERAKSIZ FIELD LARNI JSON DAN REMOVE QILAMIZ
            ignoredFields.forEach(objectNode::remove);

            //BOSHQA ENTITY GA RELATION BO'LGAN ID LARNI QAYTARADI
            Map<String, JsonNode> jsonNodeMap = getRelationIds(obj);
            for (String key : jsonNodeMap.keySet())
                objectNode.put(key, jsonNodeMap.get(key));

            return objectMapper.writeValueAsString(node);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    //BOSHQA ENTITY GA RELATION BO'LGAN ID LARNI QAYTARADI
    private static Map<String, JsonNode> getRelationIds(Object obj) {

        Map<String, JsonNode> jsonNodeMap = new HashMap<>();

        //OBJECT NI BARCHA FIELD LARINI AYLANIB ONE TO ONE VA MANY TO ONE LARNI ID LARINI YI'GIB OLADI
        for (Field field : obj.getClass().getDeclaredFields()) {

            OneToOne oneToOne = field.getAnnotation(OneToOne.class);
            ManyToOne manyToOne = field.getAnnotation(ManyToOne.class);

            //AGAR ONE TO ONE YOKI MANY TO ONE ULANMAGAN BO'LSA BULARNI SKIP QILSIN
            if (Objects.isNull(oneToOne) && Objects.isNull(manyToOne))
                continue;

            //MAPPED BY BO'LSA SKIP QILSIN
            boolean isMapped = checkMappedBy(oneToOne);
            if (isMapped)
                continue;

            field.setAccessible(true);

            try {
                Object relationObj = field.get(obj);

                Object relationalId = getRelationalId(relationObj);
                JsonNode jsonNode = objectMapper.valueToTree(relationalId);
                jsonNodeMap.put(field.getName() + AppConstant.PREFIX_ID, jsonNode);

            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        return jsonNodeMap;
    }

    public static Object getRelationalId(Object relationObj) throws NoSuchFieldException, IllegalAccessException {
        //AGAR LAZY LOAD BO'LSA HibernateProxy CLASS DA ID SI BOR
        if (relationObj instanceof HibernateProxy) {
            HibernateProxy proxy = (HibernateProxy) relationObj;
            return proxy.getHibernateLazyInitializer().getIdentifier();
        }

        //AGAR HALI ULANMAGAN BO'LSA NULL
        if (Objects.isNull(relationObj))
            return null;

        //AGAR OBJECT KELSA ICHIDAN ID FIELDINI OLAMIZ
        Field beforeRelationIdField = relationObj.getClass().getDeclaredField(AppConstant.ID);
        beforeRelationIdField.setAccessible(true);
        return beforeRelationIdField.get(relationObj);
    }

    private static boolean checkMappedBy(OneToOne oneToOne) {
        return Objects.nonNull(oneToOne) && (!oneToOne.mappedBy().isBlank() || !oneToOne.mappedBy().isEmpty());
    }


    private static Object useGetMethod(Field field, Object obj) {

        try {
            String methodName = "get" + StringUtils.capitalize(field.getName());

            Method method = obj.getClass().getMethod(methodName);

            return method.invoke(obj);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String getClassName(Field field) {
        StringBuilder className = new StringBuilder();
        Class<?> type = field.getType();
        className.append(type.getName());
        Type genericType = field.getGenericType();

        if (genericType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) genericType;
            Type[] typeArguments = parameterizedType.getActualTypeArguments();
            for (Type typeArgument : typeArguments) {
                className.append("<").append(typeArgument.getTypeName()).append(">");
            }
        }

        if (type.isPrimitive())
            className = new StringBuilder().append(getWrapper(className.toString()).getName());

        return className.toString();
    }


    public static Class<?> getWrapper(String className) {

        switch (className) {
            case "byte":
                return Byte.class;
            case "short":
                return Short.class;
            case "int":
                return Integer.class;
            case "long":
                return Long.class;
            case "float":
                return Float.class;
            case "double":
                return Double.class;
            case "char":
                return Character.class;
            case "boolean":
                return Boolean.class;
            default:
                throw new IllegalArgumentException("primitive turi topilmadi type -> " + className);
        }
    }


    public static <T> T getOrDefault(T val, T def) {
        return val == null ? def : val;
    }

    public static Object getOrDefaultObj(Object val, Object def) {
        return val == null ? def : val;
    }

    public static Set<String> getIgnoredFields(List<Field> fields, Class<?> aClass) {
        Set<Field> allFields = Arrays.stream(aClass.getDeclaredFields()).collect(Collectors.toSet());
        fields.forEach(allFields::remove);
        return allFields.stream().map(Field::getName).collect(Collectors.toSet());
    }


}
