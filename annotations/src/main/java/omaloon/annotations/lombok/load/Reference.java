package omaloon.annotations.lombok.load;

import lombok.*;

public abstract class Reference{
    abstract String stringify(int[] data);

    @AllArgsConstructor
    public static class IndexReference extends Reference{
        public final int index;

        @Override
        String stringify(int[] data){
            return data[index] + "";
        }
    }

    @AllArgsConstructor
    public static class ExpressionReference extends Reference{
        public final String expression;

        @Override
        String stringify(int[] data){
            return "\" + " + expression + "+ \"";
        }
    }

    @AllArgsConstructor
    public static class StringReference extends Reference{
        public final String value;

        @Override
        String stringify(int[] data){
            return value;
        }
    }

    public boolean isString(){
        return this instanceof StringReference;
    }
}
