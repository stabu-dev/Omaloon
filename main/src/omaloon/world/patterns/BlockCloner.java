package omaloon.world.patterns;

import arc.struct.*;
import mindustry.gen.*;
import mindustry.world.*;
import sun.misc.*;
import java.lang.reflect.*;

/** Clones a Block instance at startup without triggering content registration. */
public class BlockCloner{
    private static final Unsafe unsafe;
    private static final ObjectMap<Class<?>, long[]> outerOffsetsCache = new ObjectMap<>();

    static{
        try{
            Field f = Unsafe.class.getDeclaredField("theUnsafe");
            f.setAccessible(true);
            unsafe = (Unsafe)f.get(null);
        }catch(Exception e){
            throw new RuntimeException("Unsafe unavailable", e);
        }
    }

    public static Unsafe unsafe(){
        return unsafe;
    }

    public static long[] getOuterOffsets(Class<?> buildClass){
        long[] offsets = outerOffsetsCache.get(buildClass);
        if(offsets != null) return offsets;

        LongSeq list = new LongSeq();
        Class<?> c = buildClass;
        while(c != null && c != Object.class){
            for(Field f : c.getDeclaredFields()){
                if(f.getName().startsWith("this$")){
                    f.setAccessible(true);
                    list.add(unsafe.objectFieldOffset(f));
                }
            }
            c = c.getSuperclass();
        }
        offsets = list.toArray();
        outerOffsetsCache.put(buildClass, offsets);
        return offsets;
    }

    public static void swapOuter(Building b, Block target){
        if(b == null || target == null) return;
        long[] offsets = getOuterOffsets(b.getClass());
        for(long offset : offsets){
            unsafe.putObject(b, offset, target);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T cloneObject(T original){
        if(original == null) return null;
        try{
            T copy = (T)unsafe.allocateInstance(original.getClass());
            Class<?> c = original.getClass();
            while(c != null && c != Object.class){
                for(Field f : c.getDeclaredFields()){
                    if(Modifier.isStatic(f.getModifiers())) continue;
                    f.setAccessible(true);
                    f.set(copy, f.get(original));
                }
                c = c.getSuperclass();
            }
            return copy;
        }catch(Exception e){
            throw new RuntimeException("Failed to clone: " + original, e);
        }
    }

    /** Shallow-clones a block: same class, all instance fields copied, no constructor called. */
    @SuppressWarnings("unchecked")
    public static <T extends Block> T clone(T original){
        try{
            T copy = (T)unsafe.allocateInstance(original.getClass());
            Class<?> c = original.getClass();
            while(c != null && c != Object.class){
                for(Field f : c.getDeclaredFields()){
                    if(Modifier.isStatic(f.getModifiers())) continue;
                    f.setAccessible(true);
                    Object val = f.get(original);
                    if(val != null && f.getType().getName().startsWith("omaloon.")){
                        val = cloneObject(val);
                    }
                    f.set(copy, val);
                }
                c = c.getSuperclass();
            }
            return copy;
        }catch(Exception e){
            throw new RuntimeException("Failed to clone block: " + original, e);
        }
    }
}
