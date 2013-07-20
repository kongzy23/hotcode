package org.hotcode.hotcode.adapter;

import org.hotcode.hotcode.CodeFragment;
import org.hotcode.hotcode.reloader.ClassReloader;
import org.hotcode.hotcode.structure.HotCodeMethod;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Add check and reload code before methods except "<clinit>"
 * 
 * @author khotyn 13-6-25 PM9:48
 */
public class BeforeMethodCheckAdapter extends ClassVisitor {

    private String        classInternalName;
    private ClassReloader classReloader;

    public BeforeMethodCheckAdapter(ClassVisitor cv, ClassReloader classReloader){
        super(Opcodes.ASM4, cv);
        this.classReloader = classReloader;
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        this.classInternalName = name;
        super.visit(version, access, name, signature, superName, interfaces);
    }

    @Override
    public MethodVisitor visitMethod(final int access, final String name, final String desc, String signature,
                                     String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);

        if (name.equals("<clinit>")
            || (name.equals("<init>") && !classReloader.getOriginClass().hasConstructor(new HotCodeMethod(access, name,
                                                                                                          desc,
                                                                                                          signature,
                                                                                                          exceptions)))) {
            return mv;
        }

        return new MethodVisitor(Opcodes.ASM4, mv) {

            @Override
            public void visitCode() {
                CodeFragment.beforeMethodCheck(mv, access, name, desc, classInternalName);
                super.visitCode();
            }
        };
    }
}
