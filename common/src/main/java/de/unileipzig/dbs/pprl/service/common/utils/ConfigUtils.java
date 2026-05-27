package de.unileipzig.dbs.pprl.service.common.utils;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;

public class ConfigUtils {

  public static void mergeNonNullProperties(Object src, Object target) {
    if (src == null || target == null) return;
    if (!src.getClass().equals(target.getClass())) {
      throw new IllegalArgumentException("Source and target must be same type");
    }

    try {
      for (PropertyDescriptor pd : Introspector.getBeanInfo(src.getClass(), Object.class).getPropertyDescriptors()) {
        if (pd.getReadMethod() == null || pd.getWriteMethod() == null) continue;

        Object value = pd.getReadMethod().invoke(src);
        if (value != null) {
          // set on target
          pd.getWriteMethod().invoke(target, value);
        }
      }
    } catch (IntrospectionException | IllegalAccessException | InvocationTargetException e) {
      throw new RuntimeException("Failed to merge properties", e);
    }
  }
}
