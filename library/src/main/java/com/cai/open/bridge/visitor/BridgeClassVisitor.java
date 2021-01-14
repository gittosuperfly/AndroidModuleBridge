package com.cai.open.bridge.visitor;

import com.cai.open.bridge.BridgeInfo;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

import java.util.List;
import java.util.Map;


public class BridgeClassVisitor extends ClassVisitor {

    String bridgeBaseClass;
    String bridgeAnnotationClass;
    private Map<String, BridgeInfo> bridgeInfoMap;
    private List<String> bridgeClasses;
    private String className;
    private String superClassName;
    private boolean hasAnnotation;
    private boolean isMock;

    public BridgeClassVisitor(
            ClassVisitor classVisitor,
            Map<String, BridgeInfo> bridgeInfoMap,
            List<String> bridgeClasses,
            String bridgeBaseClass,
            String bridgeAnnotationClass) {
        super(Opcodes.ASM5, classVisitor);
        this.bridgeInfoMap = bridgeInfoMap;
        this.bridgeClasses = bridgeClasses;
        this.bridgeBaseClass = bridgeBaseClass.replace(".", "/");
        this.bridgeAnnotationClass = bridgeAnnotationClass.replace(".", "/");
    }

    @Override
    public void visit(
            int version,
            int access,
            String name,
            String signature,
            String superName,
            String[] interfaces) {
        className = name;
        superClassName = superName;
        if ((bridgeBaseClass).equals(superName)) {
            bridgeClasses.add(name);
        }
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        if (("L" + bridgeAnnotationClass + ";").equals(desc)) {
            hasAnnotation = true;
            return new AnnotationVisitor(Opcodes.ASM5, super.visitAnnotation(desc, visible)) {
                @Override
                public void visit(String name, Object value) {
                    isMock = (boolean) value;
                    super.visit(name, value);
                }
            };
        }
        return super.visitAnnotation(desc, visible);
    }

    @Override
    public void visitEnd() {
        super.visitEnd();
        if (hasAnnotation) {
            if (!isMock) {
                BridgeInfo apiInfo = bridgeInfoMap.get(superClassName);
                if (apiInfo == null || apiInfo.isMock) {
                    bridgeInfoMap.put(superClassName, new BridgeInfo(className, false));
                } else {
                    throw new IllegalArgumentException("<"
                            + className
                            + "> and <"
                            + apiInfo.implClass
                            + "> impl same bridge of <"
                            + superClassName + ">");
                }
            } else {
                if (!bridgeInfoMap.containsKey(superClassName)) {
                    bridgeInfoMap.put(superClassName, new BridgeInfo(className, true));
                }
            }
        }
    }
}
