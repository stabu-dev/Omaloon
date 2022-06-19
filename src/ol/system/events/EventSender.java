package ol.system.events;

import arc.Events;
import arc.struct.ObjectMap;
import arc.struct.Seq;

public class EventSender {
    public final String commandName;
    private final ObjectMap<String, Object> parametersMap = new ObjectMap<>();

    public EventSender(EventReceiver receiver){
        this(receiver.commandName);
    }

    public EventSender(String commandName){
        this.commandName = commandName;
    }

    public <T> void setParameter(String name, T parameter){
        parametersMap.put(name, parameter);
    }

    public void clear(){
        parametersMap.clear();
    }

    public void fire(){
        fire(false);
    }

    public void fire(boolean clearParams){
        Seq<Object> objects = new Seq<>();
        objects.add(commandName);
        for(ObjectMap.Entry<String, Object> entry : parametersMap){
            objects.add(entry.key, entry.value);
        }
        Events.fire(objects.toArray(Object.class));
        if(clearParams){
            clear();
        }
    }
}
