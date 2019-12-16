package com.wudongdong.butterknife;

import android.app.Activity;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * 创建人：吴冬冬<br/>
 * 创建时间：2019/12/16 20:04 <br/>
 */
public class ButterKnife {
    public static Unbinder bind(Activity activity) {
        Class<? extends Activity> aClass = activity.getClass();
        try {
            ClassLoader classLoader = aClass.getClassLoader();
            if (classLoader == null) {
                throw new RuntimeException(activity.getLocalClassName() + "获取不到当前类的ClassLoader");
            }
            Class<?> bindingClass = classLoader.loadClass(aClass.getName() + "_ViewBinding");
            Constructor<? extends Unbinder> constructor = (Constructor<? extends Unbinder>) bindingClass.getConstructor(aClass);
            return constructor.newInstance(activity);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException e) {
            throw new RuntimeException("Unable to find binding constructor for " + aClass.getName(), e);
        }
    }
}
