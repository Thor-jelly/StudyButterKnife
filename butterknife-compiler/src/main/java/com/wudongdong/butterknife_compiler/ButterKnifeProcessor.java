package com.wudongdong.butterknife_compiler;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.wudongdong.butterknife_annotations.BindView;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

/**
 * 创建人：吴冬冬<br/>
 * 创建时间：2019/12/13 19:24 <br/>
 */
@AutoService(Processor.class)
public class ButterKnifeProcessor extends AbstractProcessor {
    private static final String VIEW_TYPE = "android.view.View";
    private Filer mFiler;
    private Elements mElementUtils;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
        mFiler = processingEnvironment.getFiler();
        mElementUtils = processingEnvironment.getElementUtils();
    }

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
        //System.out.println("---------------process--------------------");
        //System.out.println("---------------process--------------------");
        //System.out.println("---------------process--------------------");
        //System.out.println("---------------process--------------------");
        //System.out.println("---------------process--------------------");
        //System.out.println("---------------process--------------------");
        //System.out.println("---------------process--------------------");

        //代码来自ButterKnife，主要代码，不是所有的都要。只是理解主要过程
        //找到并解析
        Map<TypeElement, List<Element>> map = findAndParseTargets(roundEnvironment);
        if (map.size() == 0) {
            return false;
        }

        //生成代码
        for (Map.Entry<TypeElement, List<Element>> entry : map.entrySet()) {
            TypeElement key = entry.getKey();
            List<Element> value = entry.getValue();
            /*
            System.out.println(key.getQualifiedName());
            for (Element element : value) {
                System.out.println(element);
            }
            //打印
            com.wudongdong.studybutterknife.MainActivity
            mButterKnifeTv
            com.wudongdong.studybutterknife.test.MainActivity
            mButterKnifeTv22
            mButterKnifeTv1
            mButterKnifeTv2
            */

            //生成类名
            //public final class xxxActivity_ViewBinding implements Unbunder
            ClassName activityClassName = ClassName.bestGuess(key.getSimpleName().toString());
            ClassName unbinderClassNme = ClassName.get("com.wudongdong.butterknife", "Unbinder");
            String activityClassNameStr = key.getSimpleName().toString();
            //System.out.println("当前生成类型=" + activityClassNameStr + "_ViewBinding");
            TypeSpec.Builder classTypeSpecBuilder = TypeSpec.classBuilder(activityClassNameStr + "_ViewBinding")
                    .addModifiers(Modifier.FINAL, Modifier.PUBLIC)//添加修饰符
                    .addField(activityClassName, "target", Modifier.PRIVATE)//添加属性
                    .addSuperinterface(unbinderClassNme)//添加实现
                    ;

            //实现接口Unbinder中的unbind方法
            ClassName callSuper = ClassName.get("androidx.annotation", "CallSuper");
            MethodSpec.Builder unbindMethodSpecBuilder = MethodSpec.methodBuilder("unbind")
                    .addAnnotation(Override.class)//添加注解
                    .addAnnotation(callSuper)
                    .addModifiers(Modifier.PUBLIC, Modifier.FINAL);


            //构造函数
            MethodSpec.Builder constructorMethodSpecBuilder = MethodSpec.constructorBuilder()
                    .addParameter(activityClassName, "target")//添加参数
                    .addStatement("this.target = target")//添加代码
                    ;


            //在unbind中添加 Activity target = this.target
            unbindMethodSpecBuilder.addStatement("$T target = this.target", activityClassName);
            //在unbind中添加
            unbindMethodSpecBuilder.addStatement("if (target == null) throw new $T($S)", IllegalStateException.class, "Bindings already cleared.");
            unbindMethodSpecBuilder.addCode("\n");

            //添加findViewById 和 unbind时候设置为null
            for (Element element : value) {
                String filedNameStr = element.getSimpleName().toString();
                ClassName utilsClassName = ClassName.get("com.wudongdong.butterknife", "Utils");
                int id = element.getAnnotation(BindView.class).value();
                //构造方法中添加 Utils.findViewById
                constructorMethodSpecBuilder.addStatement(
                        "target.$L = $L.findViewById(target, $L)",
                        filedNameStr,
                        utilsClassName,
                        id
                );

                //unbind中添加null
                unbindMethodSpecBuilder.addStatement("target.$L = null", filedNameStr);
            }
            unbindMethodSpecBuilder.addStatement("target = null");


            //添加方法到类中
            classTypeSpecBuilder.addMethod(unbindMethodSpecBuilder.build());
            classTypeSpecBuilder.addMethod(constructorMethodSpecBuilder.build());

            //生成类
            String packageNameStr = mElementUtils.getPackageOf(key).getQualifiedName().toString();
            try {
                JavaFile.builder(packageNameStr, classTypeSpecBuilder.build())
                        .addFileComment("butterKnife 自动生成")
                        .build()
                        .writeTo(mFiler);

            } catch (IOException e) {
                System.out.println("出错了=" + e.getMessage());
            }
        }


        return false;
    }

    private LinkedHashMap<TypeElement, List<Element>> findAndParseTargets(RoundEnvironment roundEnvironment) {
        LinkedHashMap<TypeElement, List<Element>> map = new LinkedHashMap<>();

        //获取被BindView注解的元素
        Set<? extends Element> bindViewElements = roundEnvironment.getElementsAnnotatedWith(BindView.class);
        for (Element bindViewElement : bindViewElements) {
            /*
            //当前注解的类型
            System.out.println(bindViewElement.asType());
            //当前注解的位置
            System.out.println(bindViewElement.getKind());
            //当前注解变量名称
            System.out.println(bindViewElement.getSimpleName().toString());

            Element bindViewElementEnclosingElement = bindViewElement.getEnclosingElement();
            //获取当前bindView的类
            System.out.println(bindViewElementEnclosingElement.getSimpleName().toString());

            //TypeElement 一个类或接口的元素
            TypeElement enclosingElement = (TypeElement) bindViewElement.getEnclosingElement();
            //获取当前bindView类的全路径
            System.out.println(enclosingElement.getQualifiedName());
            */

            if (!bindViewElement.getKind().isField()) {
                continue;
            }

            //解析当前view
            parseBindView(bindViewElement, map);
        }

        return map;
    }

    private void parseBindView(Element element, LinkedHashMap<TypeElement, List<Element>> map) {
        TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
        //首先验证一些限制生成代码限制
        boolean hasError = isInaccessibleViaGeneratedCode(BindView.class, "fields", element)
                || isBindingInWrongPackage(BindView.class, element);

        //验证注解的属性是否是继承自View
        TypeMirror elementType = element.asType();
        //System.out.println(elementType);//android.widget.TextView
        //System.out.println(elementType.getKind());//DECLARED

        //省略一些代码
        Name qualifiedName = enclosingElement.getQualifiedName();
        Name simpleName = element.getSimpleName();
        if (!isSubtypeOfType(elementType, VIEW_TYPE)) {
            hasError = true;
            error(element,
                    "@%s fields must extend from View or be an interface. (%s.%s)",
                    BindView.class.getSimpleName(),
                    qualifiedName, simpleName
            );
        }
        if (hasError) {
            return;
        }

        //这边同ButterKnife不同
        //保存正常的Element
        List<Element> saveElementsList = map.get(enclosingElement);
        if (saveElementsList == null) {
            saveElementsList = new ArrayList<>();
            map.put(enclosingElement, saveElementsList);
        }
        saveElementsList.add(element);
    }

    private boolean isSubtypeOfType(TypeMirror typeMirror, String otherType) {
        if (isTypeEqual(typeMirror, otherType)) {
            return true;
        }
        if (typeMirror.getKind() != TypeKind.DECLARED) {
            return false;
        }
        DeclaredType declaredType = (DeclaredType) typeMirror;
        List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();
        if (typeArguments.size() > 0) {
            StringBuilder typeString = new StringBuilder(declaredType.asElement().toString());
            typeString.append('<');
            for (int i = 0; i < typeArguments.size(); i++) {
                if (i > 0) {
                    typeString.append(',');
                }
                typeString.append('?');
            }
            typeString.append('>');
            if (typeString.toString().equals(otherType)) {
                return true;
            }
        }
        Element element = declaredType.asElement();
        if (!(element instanceof TypeElement)) {
            return false;
        }
        TypeElement typeElement = (TypeElement) element;
        //System.out.println("123===" + otherType + "   " + typeElement);
        TypeMirror superType = typeElement.getSuperclass();
        //System.out.println("123===" + otherType + "   " + superType);
        if (isSubtypeOfType(superType, otherType)) {
            return true;
        }
        for (TypeMirror interfaceType : typeElement.getInterfaces()) {
            if (isSubtypeOfType(interfaceType, otherType)) {
                return true;
            }
        }
        return false;
    }

    private boolean isTypeEqual(TypeMirror typeMirror, String viewType) {
        return viewType.equals(typeMirror.toString());
    }

    /**
     * 验证是否可以生成代码
     */
    private boolean isInaccessibleViaGeneratedCode(
            Class<? extends Annotation> annotationClass,
            String targetThing,
            Element element) {
        boolean hasError = false;

        TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();

        //验证属性的修饰符
        Set<Modifier> modifiers = enclosingElement.getModifiers();
        if (modifiers.contains(Modifier.PRIVATE) || modifiers.contains(Modifier.STATIC)) {
            hasError = true;
            error(element,
                    "@%s %s 必须不是 private or static. (%s.%s)",
                    annotationClass.getSimpleName(),
                    targetThing,
                    enclosingElement.getQualifiedName(),
                    element.getSimpleName()
            );
        }

        //验证父类是否是class
        if (ElementKind.CLASS != enclosingElement.getKind()) {
            hasError = true;
            error(enclosingElement,
                    "@%s %s 只能是在classes中。(%s.%s)",
                    annotationClass.getSimpleName(),
                    targetThing,
                    enclosingElement.getQualifiedName(),
                    element.getSimpleName()
            );
        }

        //验证父类是否是私有的
        if (enclosingElement.getModifiers().contains(Modifier.PRIVATE)) {
            hasError = true;
            error(enclosingElement,
                    "@%s %s 不是是私有的classes. (%s.%s)",
                    annotationClass.getSimpleName(),
                    targetThing,
                    enclosingElement.getQualifiedName(),
                    element.getSimpleName()
            );
        }
        return hasError;
    }

    /**
     * 是否绑定错误的包下
     */
    private boolean isBindingInWrongPackage(Class<? extends Annotation> annotationClass,
                                            Element element) {
        TypeElement enclosingElement = (TypeElement) element.getEnclosingElement();
        String qualifiedName = enclosingElement.getQualifiedName().toString();

        if (qualifiedName.startsWith("android.")) {
            error(element, "@%s-annotated class incorrectly in Android framework package. (%s)",
                    annotationClass.getSimpleName(), qualifiedName);
            return true;
        }
        if (qualifiedName.startsWith("java.")) {
            error(element, "@%s-annotated class incorrectly in Java framework package. (%s)",
                    annotationClass.getSimpleName(), qualifiedName);
            return true;
        }

        return false;
    }

    private void error(Element element, String message, Object... args) {
        printMessage(Diagnostic.Kind.ERROR, element, message, args);
    }

    private void note(Element element, String message, Object... args) {
        printMessage(Diagnostic.Kind.NOTE, element, message, args);
    }

    private void printMessage(Diagnostic.Kind kind, Element element, String message, Object[] args) {
        if (args.length > 0) {
            message = String.format(message, args);
        }

        processingEnv.getMessager().printMessage(kind, message, element);
    }
}
