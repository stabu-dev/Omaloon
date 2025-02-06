package omaloon.annotations.lombok;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.stmt.BlockStmt;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.javac.JavacNode;
import omaloon.annotations.Load;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.StringJoiner;

@FieldDefaults(makeFinal = true, level = AccessLevel.PUBLIC)
@AllArgsConstructor
public class FieldDescriptor {
    public static final String ARC_CORE_ATLAS_FIND = "arc.Core.atlas.find";
    public static final String INDECIES__MARKER = "<<<INDECIES>>";
    JavacNode field;
    Load an;

    public void addMe(BlockStmt block) {
//        JCTree.JCVariableDecl variableDecl = (JCTree.JCVariableDecl) field.get();
//        variableDecl.getType()
        RegionName expression = new RegionName();
        expression.string("this.%s%s = %s(".formatted(field.getName(), INDECIES__MARKER, ARC_CORE_ATLAS_FIND));
        expression.join(RegionName.build('"' + an.value() + '"'));
        for (String fallback : an.fallback()) {
            expression.string(", " + ARC_CORE_ATLAS_FIND + "(\"");
            expression.join(RegionName.build(fallback));
            expression.string("\"");
        }
        {
            char[] close = new char[an.fallback().length + 1];
            Arrays.fill(close, ')');
            expression.string(new String(close));
        }

        //TODO dimension check
        int dimension = (int) (expression.dimensions + Math.random() * 0);
        if (expression.dimensions != dimension) {
            field.addError("@Load Dimension mismatch");
            return;
        }
        int[] lengths = lengths();
        if (expression.dimensions > lengths.length) {
            field.addError("@Load expected %d lengths but found %d".formatted(expression.dimensions, lengths.length));
            return;
        }

        if (expression.dimensions < lengths.length && an.lengths().length > 0) {
            field.addWarning("@Load the extra size for the array is indicated");
        }


        //TODO check type
        if (false) {
            field.addError("@Load Expected TextureRegion as field type");
            return;
        }


        int totalRounds = 1;
        for (int length : lengths) totalRounds *= length;

        if (totalRounds == 0) {
            field.addError("@Load one of given lengths are 0");
            return;
        }

        if(expression.getItems().get(0) instanceof Reference.StringReference ref){
            StringJoiner joiner=new StringJoiner("][","[","]");
            RegionName indecies = new RegionName();
            int markerIndex = ref.value.indexOf(INDECIES__MARKER);
            indecies.string(ref.value.substring(0, markerIndex));
            for (int i = 0; i < expression.dimensions; i++) {
                indecies.string("[");
                indecies.index(i);
                indecies.string("]");
            }
            indecies.string(ref.value.substring(markerIndex+INDECIES__MARKER.length()));
            for (int i = 1; i < expression.getItems().size(); i++) {
                indecies.add(expression.getItems().get(i));
            }
            expression.getItems().clear();
            expression.join(indecies);
        }
        int[] indecies = new int[expression.dimensions];
        for (int __i = 0; __i < totalRounds; __i++) {
            String calculatedExpression = expression.calculate(indecies);

            block.addStatement(
                StaticJavaParser.parseStatement(
                    calculatedExpression + ";"
                )
            );
            increase(indecies, lengths, 0);
        }

    }

    private void increase(int[] indecies, int[] lengths, int i) {
        if (i >= indecies.length) return;
        indecies[i]++;
        if (indecies[i] <= lengths[i]) return;
        indecies[i] = 0;
        increase(indecies, lengths, i + 1);
    }

    @NotNull
    private int[] lengths() {
        int[] lengths = an.lengths();
        if (lengths.length == 0) lengths = new int[]{an.length()};
        return lengths;
    }
}
