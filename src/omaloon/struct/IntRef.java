package omaloon.struct;

public class IntRef{
    public static final IntRef
        tmp1 = new IntRef(),
        tmp2 = new IntRef(),
        tmp3 = new IntRef(),
        tmp4 = new IntRef();
    public int value;

    public IntRef zero(){
        value = 0;
        return this;
    }

    public IntRef(int value){
        this.value = value;
    }


    public IntRef(){
    }
}
