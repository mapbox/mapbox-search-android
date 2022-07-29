package com.mapbox.search.tests_support;

import java.lang.reflect.Field;

public class TestFieldsComparator {
    public static void compare(Object o1, Object o2) throws Exception {
        if (o1 == o2) return;
        if (o1.getClass() != o2.getClass()) {
            throw new IllegalArgumentException("Types are not the same");
        }

        final Field[] fields = o1.getClass().getDeclaredFields();

        for (Field f : fields) {
            f.setAccessible(true);

            final Object v1 = f.get(o1);
            final Object v2 = f.get(o2);

            if (v1 == null || v2 == null) {
                if (v1 != v2) {
                    throw new Exception(f.getName() + " fields are different");
                }
            } else if (!v1.equals(v2)) {
                throw new Exception(f.getName() + " fields are different");
            }
        }
    }
}
