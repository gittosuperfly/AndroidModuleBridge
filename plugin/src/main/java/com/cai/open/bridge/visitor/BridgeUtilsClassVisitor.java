package com.cai.open.bridge.visitor;


import com.cai.open.bridge.BridgeInfo;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;

import java.util.Map;


public class BridgeUtilsClassVisitor extends ClassVisitor {

    private Map<String, BridgeInfo> bridgeInfoMap;
    private String bridgeUtilsClass;

    public BridgeUtilsClassVisitor(ClassVisitor classVisitor, Map<String, BridgeInfo> apiImplMap, String apiUtilsClass) {
        super(Opcodes.ASM5, classVisitor);
        bridgeInfoMap = apiImplMap;
        bridgeUtilsClass = apiUtilsClass.replace(".", "/");
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        if (!"init".equals(name)) {
            return super.visitMethod(access, name, descriptor, signature, exceptions);
        }
        // 往 init() 函数中写入
        if (cv == null) return null;
        MethodVisitor mv = cv.visitMethod(access, name, descriptor, signature, exceptions);
        mv = new AdviceAdapter(Opcodes.ASM5, mv, access, name, descriptor) {

            @Override
            public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
                return super.visitAnnotation(desc, visible);
            }

            @Override
            protected void onMethodEnter() {
                super.onMethodEnter();
            }

            @Override
            protected void onMethodExit(int opcode) {
                super.onMethodExit(opcode);
                for (Map.Entry<String, BridgeInfo> apiImplEntry : bridgeInfoMap.entrySet()) {
                    mv.visitVarInsn(Opcodes.ALOAD, 0);
                    mv.visitLdcInsn(Type.getType("L" + apiImplEntry.getValue().implClass + ";"));
                    mv.visitMethodInsn(Opcodes.INVOKESPECIAL, bridgeUtilsClass, "registerImpl", "(Ljava/lang/Class;)V", false);
                }
            }
        };
        return mv;
    }
}

