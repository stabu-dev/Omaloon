package ol.tools.shapes;

public class JustPoly extends Poly<JustPoly>{
    public JustPoly(int size) {
        super(size);
    }

    @Override
    protected JustPoly getThis() {
        return this;
    }
}
