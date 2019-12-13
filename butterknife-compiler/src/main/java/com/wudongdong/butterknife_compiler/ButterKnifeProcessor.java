package com.wudongdong.butterknife_compiler;

import com.google.auto.service.AutoService;
import com.wudongdong.butterknife_annotations.BindView;

import java.lang.annotation.Annotation;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;

/**
 * 创建人：吴冬冬<br/>
 * 创建时间：2019/12/13 19:24 <br/>
 */
@AutoService(Processor.class)
public class ButterKnifeProcessor extends AbstractProcessor {
    //1. 指定处理版本
    @Override
    public SourceVersion getSupportedSourceVersion() {
        //直接返回最新版本就好了
        return SourceVersion.latestSupported();
    }

    //2. 给到需要处理的注解
    @Override
    public Set<String> getSupportedAnnotationTypes() {
        //添加需要处理的注解，比如我们自己写的BindView
        //复制butterKnife源码
        Set<String> types = new LinkedHashSet<>();
        for (Class<? extends Annotation> annotation : getSupportedAnnotations()) {
            types.add(annotation.getCanonicalName());
        }
        return types;
    }

    private Set<Class<? extends Annotation>> getSupportedAnnotations() {
        Set<Class<? extends Annotation>> annotations = new LinkedHashSet<>();
        annotations.add(BindView.class);
        return annotations;
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
        //测试是否进来
        System.out.println("---------------process--------------------");
        System.out.println("---------------process--------------------");
        System.out.println("---------------process--------------------");
        System.out.println("---------------process--------------------");
        System.out.println("---------------process--------------------");
        System.out.println("---------------process--------------------");
        System.out.println("---------------process--------------------");
        return false;
    }
}
