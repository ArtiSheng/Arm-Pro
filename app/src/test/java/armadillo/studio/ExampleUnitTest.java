package armadillo.studio;

import android.app.Activity;

import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    String rule = "armadillo.studio.ExampleUnitTest.test([activity],s[test],i[1111],I[2222],b[true],B[false],f[1.32],F[12.21],d[22.22],D[33.33],l[11111111111111],L[2222222222222])";
    String rule1 = "armadillo.studio.ExampleUnitTest.test([activity],s[test],I[2222],B[false],F[12.21],D[33.33],L[2222222222222])";
    String rule2 = "armadillo.studio.ExampleUnitTest.testEmpty()";
    String rule3 = "armadillo.studio.ExampleUnitTest.test([activity],s[sss\nsss])";

    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void testModLoad() throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException, InstantiationException {
        final String[] strings = rule1.split("\\(");
        final String MainClass = strings[0].substring(0, strings[0].lastIndexOf("."));
        final String MainMethod = strings[0].substring(strings[0].lastIndexOf(".") + 1);
        final String Parameters = strings[1].substring(0, strings[1].length() - 1);
        final List<Class<?>> classList = new ArrayList<>();
        final List<Object> parameterList = new ArrayList<>();
        if (!Parameters.isEmpty()) {
            final LinkedHashMap<Class<?>, Object> maps = new LinkedHashMap<>();
            for (String s : Parameters.split(",")) {
                /**
                 * 当前Activity对象
                 */
                if (s.equals("[activity]"))
                    maps.put(Activity.class, null);
                /**
                 * String类型
                 */
                else if (s.startsWith("s["))
                    maps.put(String.class, s.replace("s[", "").replace("]", ""));
                /**
                 * int类型
                 */
                else if (s.startsWith("i[")) {
                    String buffer = s.replace("i[", "").replace("]", "");
                    try {
                        int i = Integer.parseInt(buffer);
                        maps.put(int.class, i);
                    } catch (Exception e) {
                        throw new IllegalArgumentException(String.format("Parameter type %s -> %s conversion error", buffer, "int"));
                    }
                }
                /**
                 * Integer 类型
                 */
                else if (s.startsWith("I[")) {
                    String buffer = s.replace("I[", "").replace("]", "");
                    try {
                        Integer i = Integer.parseInt(buffer);
                        maps.put(Integer.class, i);
                    } catch (Exception e) {
                        throw new IllegalArgumentException(String.format("Parameter type %s -> %s conversion error", buffer, "Integer"));
                    }
                }
                /**
                 * boolean 类型
                 */
                else if (s.startsWith("b[")) {
                    String buffer = s.replace("b[", "").replace("]", "");
                    try {
                        boolean b = Boolean.parseBoolean(buffer);
                        maps.put(boolean.class, b);
                    } catch (Exception e) {
                        throw new IllegalArgumentException(String.format("Parameter type %s -> %s conversion error", buffer, "boolean"));
                    }
                }
                /**
                 * Boolean 类型
                 */
                else if (s.startsWith("B[")) {
                    String buffer = s.replace("B[", "").replace("]", "");
                    try {
                        Boolean b = Boolean.parseBoolean(buffer);
                        maps.put(Boolean.class, b);
                    } catch (Exception e) {
                        throw new IllegalArgumentException(String.format("Parameter type %s -> %s conversion error", buffer, "Boolean"));
                    }
                }
                /**
                 * float 类型
                 */
                else if (s.startsWith("f[")) {
                    String buffer = s.replace("f[", "").replace("]", "");
                    try {
                        float v = Float.parseFloat(buffer);
                        maps.put(float.class, v);
                    } catch (Exception e) {
                        throw new IllegalArgumentException(String.format("Parameter type %s -> %s conversion error", buffer, "float"));
                    }
                }
                /**
                 * Float 类型
                 */
                else if (s.startsWith("F[")) {
                    String buffer = s.replace("F[", "").replace("]", "");
                    try {
                        Float v = Float.parseFloat(buffer);
                        maps.put(Float.class, v);
                    } catch (Exception e) {
                        throw new IllegalArgumentException(String.format("Parameter type %s -> %s conversion error", buffer, "Float"));
                    }
                }
                /**
                 * double 类型
                 */
                else if (s.startsWith("d[")) {
                    String buffer = s.replace("d[", "").replace("]", "");
                    try {
                        double d = Double.parseDouble(buffer);
                        maps.put(double.class, d);
                    } catch (Exception e) {
                        throw new IllegalArgumentException(String.format("Parameter type %s -> %s conversion error", buffer, "double"));
                    }
                }
                /**
                 * Double 类型
                 */
                else if (s.startsWith("D[")) {
                    String buffer = s.replace("D[", "").replace("]", "");
                    try {
                        Double d = Double.parseDouble(buffer);
                        maps.put(Double.class, d);
                    } catch (Exception e) {
                        throw new IllegalArgumentException(String.format("Parameter type %s -> %s conversion error", buffer, "Double"));
                    }
                }
                /**
                 * long 类型
                 */
                else if (s.startsWith("l[")) {
                    String buffer = s.replace("l[", "").replace("]", "");
                    try {
                        long l = Long.parseLong(buffer);
                        maps.put(long.class, l);
                    } catch (Exception e) {
                        throw new IllegalArgumentException(String.format("Parameter type %s -> %s conversion error", buffer, "long"));
                    }
                }
                /**
                 * Long 类型
                 */
                else if (s.startsWith("L[")) {
                    String buffer = s.replace("L[", "").replace("]", "");
                    try {
                        Long l = Long.parseLong(buffer);
                        maps.put(Long.class, l);
                    } catch (Exception e) {
                        throw new IllegalArgumentException(String.format("Parameter type %s -> %s conversion error", buffer, "Long"));
                    }
                }
            }
            for (Map.Entry<Class<?>, Object> entry : maps.entrySet()) {
                classList.add(entry.getKey());
                parameterList.add(entry.getValue());
            }
        }
        final Class<?> aClass = Class.forName(MainClass);
        Method method = null;
        try {
            method = aClass.getDeclaredMethod(MainMethod, classList.toArray(new Class[0]));
        } catch (NoSuchMethodException e) {
            if (classList.size() > 0)
                method = aClass.getDeclaredMethod(MainMethod, Object[].class);
        }
        if (method != null) {
            method.setAccessible(true);
            if (Modifier.isStatic(method.getModifiers())) {
                if (method.getParameterCount() > 0 && method.getParameterTypes()[0] == Object[].class)
                    method.invoke(null, new Object[]{parameterList.toArray(new Object[0])});
                else
                    method.invoke(null, parameterList.toArray(new Object[0]));
            } else {
                if (method.getParameterCount() > 0 && method.getParameterTypes()[0] == Object[].class)
                    method.invoke(aClass.newInstance(), new Object[]{parameterList.toArray(new Object[0])});
                else
                    method.invoke(aClass.newInstance(), parameterList.toArray(new Object[0]));
            }
        } else
            throw new NoSuchMethodException(String.format("Unable to query the specified method:%s", MainMethod));
    }

    public static void testEmpty() {
        System.out.println("Empty");
    }

    public static void test(Object... objs) {
        System.out.println(Arrays.toString(objs));
    }

    public static void test1(
            Activity activity,
            String s,
            int i,
            Integer i1,
            boolean b,
            Boolean b1,
            float f,
            Float f1,
            double d,
            Double d1,
            long l,
            Long l1) {
        System.out.println(activity);
        System.out.println(String.format("%s,%d,%d,%s,%s,%f,%f,%f,%f,%s,%s",
                s,
                i,
                i1,
                b,
                b1,
                f,
                f1,
                d,
                d1,
                l,
                l1));
    }
}