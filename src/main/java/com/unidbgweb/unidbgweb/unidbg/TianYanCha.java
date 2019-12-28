package com.unidbgweb.unidbgweb.unidbg;

import cn.banny.unidbg.Module;
import cn.banny.unidbg.arm.ARMEmulator;
import cn.banny.unidbg.linux.android.AndroidARMEmulator;
import cn.banny.unidbg.linux.android.AndroidResolver;
import cn.banny.unidbg.linux.android.dvm.DalvikModule;
import cn.banny.unidbg.linux.android.dvm.DvmClass;
import cn.banny.unidbg.linux.android.dvm.StringObject;
import cn.banny.unidbg.linux.android.dvm.VM;
import cn.banny.unidbg.linux.android.dvm.array.ByteArray;
import cn.banny.unidbg.memory.Memory;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Base64;

public class TianYanCha {
    //ARM模拟器
    private final ARMEmulator emulator;
    //vm
    private final VM vm;
    //载入的模块
    private final Module module;

    private final DvmClass TTEncryptUtils;

    //初始化
    public TianYanCha() throws IOException {
        //创建毒进程，这里我用了天眼查的也可以，不知道咋回事
        emulator = new AndroidARMEmulator("com.du.du");
        Memory memory = emulator.getMemory();
        //作者支持19和23两个sdk
        memory.setLibraryResolver(new AndroidResolver(23));
        memory.setCallInitFunction();
        //创建DalvikVM，利用apk本身，可以为null
        //如果用apk文件加载so的话，会自动处理签名方面的jni，具体可看AbstractJni,这就是利用apk加载的好处
        vm = emulator.createDalvikVM(new File("src/main/resources/app/tianyancha/tianyancha10.8.0.apk"));
//        vm = emulator.createDalvikVM(null);
        //加载so，使用armv8-64速度会快很多
        DalvikModule dm = vm.loadLibrary("JMEncryptBox", false);
        //调用jni
        dm.callJNI_OnLoad(emulator);
        module = dm.getModule();
        //Jni调用的类，加载so
        TTEncryptUtils = vm.resolveClass("com/ijiami/JMEncryptBoxByRandom");
    }

    public static void main(String[] args) throws IOException {
        TianYanCha TianYanCha = new TianYanCha();
        TianYanCha.encryptToBase64("", 2);
    }

    private String encryptToBase64(String str, int i) throws UnsupportedEncodingException {
        Number ret = null;
        if (i == 2) {
            ret = TTEncryptUtils.callStaticJniMethod(emulator, "encryptByRandomType2([B)[B", vm.addLocalObject(new ByteArray(str.getBytes("UTF-8"))));
        } else {
            ret = TTEncryptUtils.callStaticJniMethod(emulator, "encryptByRandomType1([B)[B", vm.addLocalObject(new ByteArray(str.getBytes("UTF-8"))));
        }
        long hash = ret.intValue() & 0xffffffffL;
        //或得其值
        ByteArray res = vm.getObject(hash);
        System.out.println(res);
        return Base64.getEncoder().encodeToString("".getBytes());
    }
}
