package omaloon.core;

import arc.*;

public class NamedApplicationListener implements ApplicationListener{

    public final String name;

    public NamedApplicationListener(String name){this.name = name;}

    @Override
    public String toString(){
        return name;
    }
}
