/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package it.micronixnetwork.gaf.util.xml.bean;

import it.micronixnetwork.gaf.util.xml.XMLObject;
import it.micronixnetwork.gaf.util.xml.bean.annotation.Flat;
import it.micronixnetwork.gaf.util.xml.bean.annotation.NoMarshal;
import it.micronixnetwork.gaf.util.xml.bean.annotation.XMLBean;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 * @author kobo
 */
public class BeanMallow {

    private static final Set<Class> SCALAR = new HashSet<Class>(Arrays.asList(
            String.class, Boolean.class, Byte.class, Character.class, Short.class, Integer.class, Float.class, Long.class, Double.class,
            BigInteger.class, BigDecimal.class, AtomicBoolean.class, AtomicInteger.class, AtomicLong.class, Date.class, Calendar.class, GregorianCalendar.class,
            Class.class, UUID.class, Number.class, Object.class, Timestamp.class));

    /**
     * Esporta il bean in un XMLObject
     *
     * @param toMarsh oggetto di cui fare il marshal
     * @return un XMLObject che rappresenta il bean passato come parametro
     */
    public static XMLObject marshal(Object toMarsh) {
        if (toMarsh == null) {
            return null;
        }
        Class c = toMarsh.getClass();
        XMLBean xb = (XMLBean) c.getAnnotation(XMLBean.class);
        XMLObject tag = new XMLObject(c.getSimpleName());
        if (xb != null) {
            try {
                addFields(tag, toMarsh);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return tag;
    }

    public static void unmarshal(Object toUnmarsh, XMLObject obj) {
        if (obj != null && toUnmarsh != null) {
            for (XMLObject child : obj.getChildren()) {
                setField(toUnmarsh, child);
            }
        }
    }

    private static void addFields(XMLObject tag, Object toMarsh) {
        if (toMarsh != null) {
            Class clazz = toMarsh.getClass();
            if (isScalar(clazz)) {
                tag.setText(formatValue(toMarsh));
            } else {
                Field[] fields = clazz.getDeclaredFields();
                for (Field field : fields) {
                    NoMarshal nom = (NoMarshal) field.getAnnotation(NoMarshal.class);
                    if (nom == null) {
                        Flat flat = (Flat) field.getAnnotation(Flat.class);
                        if (flat == null) {
                            addFieldTag(tag, toMarsh, field.getName(), null);
                        } else {
                            Field[] embFields = field.getType().getDeclaredFields();
                            Object embToMarsh = getValue(toMarsh, field.getName());
                            for (Field embField : embFields) {
                                nom = (NoMarshal) embField.getAnnotation(NoMarshal.class);
                                if (nom == null) {
                                    addFieldTag(tag, embToMarsh, embField.getName(), null);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static void addFieldTag(XMLObject tag, Object toMarsh, String attribute, String new_name) {
        String name;
        Object value;

        if (new_name != null) {
            name = new_name;
        } else {
            name = attribute;
        }

        value = getValue(toMarsh, attribute);

        if (value == null) {
            return;
        }

        Class vclass = value.getClass();

        XMLBean xb = (XMLBean) vclass.getAnnotation(XMLBean.class);
        if (xb != null) {
            XMLObject flTag = new XMLObject(name);
            addFields(flTag, value);
            tag.addContent(flTag);
        } else {
            if (value instanceof Collection) {
                XMLObject flTag = new XMLObject(name);
                for (Object ele : ((Collection) value)) {
                    XMLObject vtag = new XMLObject("value");
                    addFields(vtag, ele);
                    flTag.addContent(vtag);
                }
                tag.addContent(flTag);
            } else {
                XMLObject flTag = new XMLObject(name);
                flTag.setText(formatValue(value));
                tag.addContent(flTag);
            }
        }
    }

    private static void setField(Object toUnmarsh, XMLObject child) {
        Class c = toUnmarsh.getClass();
        Object value;
        try {
            Field field;
            try {
                field = c.getDeclaredField(child.getTagName());
            } catch (NoSuchFieldException e1) {
                return;
            }

            //Recupero tipo del field
            Class fclass = field.getType();
            if (Collection.class.isAssignableFrom(fclass)) {
                value = createCollection(fclass);
                for (XMLObject subchild : child.getChildren()) {
                    Class type = getGenericType(field);
                    if (type != null) {
                        Object newEle = null;
                        if (isScalar(type)) {
                            newEle = formatType(type, subchild.getText());
                        } else {
                            newEle = type.newInstance();
                            unmarshal(newEle, subchild);
                        }
                        if (newEle != null) {
                            ((Collection) value).add(newEle);
                        }
                    }
                }

            } else {
                if (isScalar(fclass)) {
                    value = formatType(field.getType(), child.getText());
                } else {
                    value = fclass.newInstance();
                    unmarshal(value, child);
                }
            }
            setValue(toUnmarsh, field, value);
        } catch (Exception e) {
        }
    }

    private static void setValue(Object toUnmarsh, Field field, Object value) {
        if (value != null) {
            try {
                field.setAccessible(true);
                field.set(toUnmarsh, value);
                field.setAccessible(false);
            } catch (Exception e) {
            }
        }
    }

    private static Object formatType(Class type, String text) {
        if (type == Integer.class || type == Integer.TYPE) {
            return new Integer(text);
        }
        if (type == String.class) {
            return text;
        }
        if (type == Double.class || type == Double.TYPE) {
            return new Double(text);
        }
        if (type == Short.class || type == Short.TYPE) {
            return new Short(text);
        }
        if (type == java.util.Date.class) {
            DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
            try {
                return formatter.parse(text);
            } catch (ParseException e) {
                return null;
            }
        }
        if (type == Float.class || type == Float.TYPE) {
            return new Float(text);
        }
        if (type == Boolean.class || type == Boolean.TYPE) {
            return new Boolean(text);
        }
        return null;
    }

    private static Class getGenericType(Field field) {
        Type type = field.getGenericType();

        if (type instanceof ParameterizedType) {

            ParameterizedType pType = (ParameterizedType) type;
            Type[] arr = pType.getActualTypeArguments();

            for (Type tp : arr) {
                Class<?> clzz = (Class<?>) tp;
                return clzz;
            }
        }
        return null;
    }

    private static Object getValue(Object toMarsh, String attribute) {
        try {
            Field field = toMarsh.getClass().getDeclaredField(attribute);
            field.setAccessible(true);
            Object toReturn = field.get(toMarsh);
            field.setAccessible(false);
            return toReturn;
        } catch (Exception e) {
            return null;
        }
    }

    private static boolean isScalar(Class toCheck) {
        if (toCheck.isPrimitive()) {
            return true;
        }
        return SCALAR.contains(toCheck);
    }

    private static String formatValue(Object value) {
        String outValue = null;
        if (value == null) {
            return "NULL";
        }

        Class clazz = value.getClass();

        if (value instanceof java.util.Date) {
            DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
            outValue = formatter.format(value);
            return outValue;
        }

        if (isScalar(clazz)) {
            return value.toString();
        }

        return "UNDEFINED";
    }

    private static Object createCollection(Class<?> c) throws InstantiationException, IllegalAccessException, IllegalArgumentException, ClassNotFoundException {

        if (Set.class.isAssignableFrom(c)) {
            return new HashSet();
        }
        if (List.class.isAssignableFrom(c)) {
            return new ArrayList();
        }

        return new ArrayList();
    }

    public static void main(String[] args) {

        EmbId id = new EmbId("Uno", "Due");

        Costruzione costruzione = new Costruzione();

        costruzione.primo = 1;
        costruzione.secondo = "ciao";
        costruzione.terzo = "Hello world";
        costruzione.id = id;

        costruzione.list = new HashSet();
        costruzione.list.add("Prova1&");
        costruzione.list.add("Prova2'");
        costruzione.list.add("Prova'3");
        costruzione.subbean = new BeanContent();

        costruzione.subbean.primo = 34;
        costruzione.subbean.secondo = "subbean";

        costruzione.subbean.list = new HashSet();
        costruzione.subbean.list.add("Prova1");
        costruzione.subbean.list.add("Prova2");

        BeanContent tol2 = new BeanContent();

        tol2.primo = 34;
        tol2.secondo = "subbean";

        tol2.list = new HashSet();
        tol2.list.add("Prova1");

        costruzione.list2 = new HashSet<BeanContent>();

        costruzione.list2.add(tol2);

        XMLObject xml_costruzione = BeanMallow.marshal(costruzione);

        System.out.println("COSRUZIONE");
        System.out.println("");
        System.out.println(xml_costruzione.describe());
        System.out.println("");
        System.out.println("");
        System.out.println("");
        Ricostruzione ricostruzione = new Ricostruzione();
        BeanMallow.unmarshal(ricostruzione, xml_costruzione);

        XMLObject xml_ricostruzione = BeanMallow.marshal(ricostruzione);
        System.out.println("RICOSRUZIONE");
        System.out.println("");
        System.out.println(xml_ricostruzione.describe());
    }

    @XMLBean
    static class EmbId {

        private String val1;
        private String val2;

        public EmbId() {
        }

        public EmbId(String val1, String val2) {
            this.val1 = val1;
            this.val2 = val2;
        }

        public String getVal1() {
            return val1;
        }

        public void setVal1(String val1) {
            this.val1 = val1;
        }

        public String getVal2() {
            return val2;
        }

        public void setVal2(String val2) {
            this.val2 = val2;
        }

    }

    @XMLBean
    static class Costruzione {

        Costruzione() {
        }
        ;
         
        @Flat
        private EmbId id;

        private Integer primo;

        private String secondo;

        private String terzo;

        private BeanContent subbean;

        private Set<String> list;

        private Set<BeanContent> list2;

        public Integer getPrimo() {
            return primo;
        }

        public void setPrimo(Integer primo) {
            this.primo = primo;
        }

        public String getSecondo() {
            return secondo;
        }

        public void setSecondo(String secondo) {
            this.secondo = secondo;
        }

        public String getTerzo() {
            return terzo;
        }

        public void setTerzo(String terzo) {
            this.terzo = terzo;
        }

        public Set getList() {
            return list;
        }

        public void setList(Set list) {
            this.list = list;
        }

        public Set<BeanContent> getList2() {
            return list2;
        }

        public void setList2(Set<BeanContent> list2) {
            this.list2 = list2;
        }

        public BeanContent getSubbean() {
            return subbean;
        }

        public void setSubbean(BeanContent subbean) {
            this.subbean = subbean;
        }

        public EmbId getId() {
            return id;
        }

        public void setId(EmbId id) {
            this.id = id;
        }

    };

    @XMLBean
    static class Ricostruzione {

        Ricostruzione() {
        }
        ;
         
       
        private String val1;

        private String val2;

        private Integer primo;

        private String secondo;

        private String terzo;

        private BeanContent subbean;

        private Set<String> list;

        private Set<BeanContent> list2;

        public Integer getPrimo() {
            return primo;
        }

        public void setPrimo(Integer primo) {
            this.primo = primo;
        }

        public String getSecondo() {
            return secondo;
        }

        public void setSecondo(String secondo) {
            this.secondo = secondo;
        }

        public String getTerzo() {
            return terzo;
        }

        public void setTerzo(String terzo) {
            this.terzo = terzo;
        }

        public Set getList() {
            return list;
        }

        public void setList(Set list) {
            this.list = list;
        }

        public Set<BeanContent> getList2() {
            return list2;
        }

        public void setList2(Set<BeanContent> list2) {
            this.list2 = list2;
        }

        public BeanContent getSubbean() {
            return subbean;
        }

        public void setSubbean(BeanContent subbean) {
            this.subbean = subbean;
        }

        public String getVal1() {
            return val1;
        }

        public void setVal1(String val1) {
            this.val1 = val1;
        }

        public String getVal2() {
            return val2;
        }

        public void setVal2(String val2) {
            this.val2 = val2;
        }

    };

    @XMLBean
    static class BeanContent {

        BeanContent() {
        }
        ;
         
        private Integer primo;
        private String secondo;

        private Set<String> list;

        public Integer getPrimo() {
            return primo;
        }

        public void setPrimo(Integer primo) {
            this.primo = primo;
        }

        public String getSecondo() {
            return secondo;
        }

        public void setSecondo(String secondo) {
            this.secondo = secondo;
        }

        public Set getList() {
            return list;
        }

        public void setList(Set list) {
            this.list = list;
        }

    };
}
