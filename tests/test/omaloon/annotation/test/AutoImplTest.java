package omaloon.annotation.test;

import omaloon.annotations.*;
import org.junit.jupiter.api.*;

import java.lang.reflect.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AutoImplTest{
    @Test
    void testField(){
        Field[] fields = TestClassWithImpl.class.getDeclaredFields();
        assertEquals(1,fields.length);
        assertEquals("val",fields[0].getName());
        assertEquals(float.class,fields[0].getType());
    }

    @Test
    void testGetAndSet(){
        TestClassWithImpl obj = new TestClassWithImpl();
        assertEquals(0,obj.value());
        obj.value(10);
        assertEquals(10,obj.value());
    }
    static class TestClassWithImpl implements ITest{

    }
    @AutoImplement

    interface ITest{
        public float val=0;
         default float value(){
            return val;
        }
         default void value(float it){
            AutoImplement.Util.Param("__val__set","val = it");
        }
    }
}
