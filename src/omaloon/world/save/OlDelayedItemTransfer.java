package omaloon.world.save;

import arc.Core;
import arc.struct.Seq;
import arc.util.io.Reads;
import arc.util.io.Writes;
import arc.util.pooling.Pool;
import arc.util.pooling.Pools;
import mindustry.Vars;
import mindustry.gen.Building;
import mindustry.gen.Call;
import mindustry.gen.Unit;
import mindustry.input.InputHandler;
import mindustry.io.TypeIO;
import mindustry.type.Item;

import java.io.DataInput;
import java.io.DataOutput;

public class OlDelayedItemTransfer extends OlSaveChunk {


    private static final Seq<Request> requests = new Seq<>();
    private static final Pool<Request> requestPool = Pools.get(Request.class, Request::new);

    public OlDelayedItemTransfer() {
        super("delayed-item-transfer");
    }

    public static void makeRequest(Item item, float x, float y, Unit owner, Building core) {
        firstState(item, x, y, owner, core);
    }

    private static void firstState(Item item, float x, float y, Unit owner, Building core) {
        Request request = requestPool.obtain().set(item, x, y, owner, core);
        requests.add(request);
        InputHandler.createItemTransfer(item, 1, x, y, owner, () -> {
            request.state++;
            if(owner.hasItem() && owner.item()!=item){
                int amount = core.acceptStack(item, owner.stack.amount, owner);
                Call.transferItemTo(owner,owner.item(),amount,owner.x,owner.y,core);
                owner.clearItem();
            }
            owner.addItem(item);
            secondState(request);
        });
    }

    private static void secondState(Request request) {
        Item item = request.item;
        Unit owner = request.owner;
        Building core = request.core;
        InputHandler.createItemTransfer(item, 1, owner.x, owner.y, core, () -> {
            if (owner.stack().item != item || owner.stack().amount <= 0) return;
            int amount = core.acceptStack(item, 1, owner);
            owner.stack().amount -=amount;
            core.handleStack(item,amount,owner);
            requests.remove(request);
            requestPool.free(request);
        });
    }

    @Override
    int version() {
        return 0;
    }

    @Override
    protected void write(Writes write, DataOutput dataOutput) {
        write.i(requests.size);
        for (Request request : requests) {
            write.str(request.item.name);
            write.f(request.x);
            write.f(request.y);
            TypeIO.writeUnit(write, request.owner);
            TypeIO.writeBuilding(write, request.core);
            write.i(request.state);
        }
    }

    @Override
    protected void read(Reads read, DataInput dataInput, int version) {
        int amount = read.i();
        requests.clear();
        requests.ensureCapacity(amount);
        for (int i = 0; i < amount; i++) {
            Item item = Vars.content.item(read.str());
            float x = read.f();
            float y = read.f();
            Unit owner = TypeIO.readUnit(read);
            Building build = TypeIO.readBuilding(read);
            int state = read.i();
            Core.app.post(() -> {
                if (state == 0) {
                    makeRequest(item, x, y, owner, build);
                } else {
                    Request set = requestPool.obtain()
                                             .set(item, x, y, owner, build);
                    set.state=state;
                    secondState(set
                    );
                }
            });
        }
    }

    public static class Request implements Pool.Poolable {
        Item item;
        float x;
        float y;
        Unit owner;
        Building core;
        int state = 0;

        public Request set(Item item, float x, float y, Unit owner, Building core) {
            this.item = item;
            this.x = x;
            this.y = y;
            this.owner = owner;
            this.core = core;
            return this;
        }

        public void reset() {
            item = null;
            owner = null;
            core = null;
            x = y = 0;
            state = 0;
        }
    }

}
