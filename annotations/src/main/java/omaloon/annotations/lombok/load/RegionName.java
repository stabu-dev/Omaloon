package omaloon.annotations.lombok.load;

import lombok.Getter;
import omaloon.annotations.lombok.load.Reference.ExpressionReference;
import omaloon.annotations.lombok.load.Reference.IndexReference;
import omaloon.annotations.lombok.load.Reference.StringReference;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegionName {
    public static final int[] EMPTY_INTS = new int[0];
    static Pattern accessExpressionPattern = Pattern.compile("@(\\w+(\\(\\))?(\\.\\w+(\\(\\))?)*)*");
    static Pattern indexAccessPattern = Pattern.compile("#\\d*");

    public int dimensions;
    @Getter
    private List<Reference> items = new ArrayList<>();

    private static void flatReplacePattern(RegionName regionName, Pattern pattern, BiConsumer<String, RegionName> replacement) {

        ArrayList<Reference> copy = new ArrayList<>(regionName.items);
        regionName.items.clear();
        for (Reference reference : copy) {
            if (!reference.isString()) {
                regionName.add(reference);
                continue;
            }
            String value = reference.stringify(EMPTY_INTS);
            Matcher matcher = pattern.matcher(value);
            if (matcher.find()) {

                int prevIndex = 0;
                while (matcher.find(prevIndex)) {
                    regionName.string(value.substring(prevIndex, matcher.start()));
                    replacement.accept(matcher.group(), regionName);
                    prevIndex = matcher.end();
                }
                regionName.string(value.substring(prevIndex));
            }else{
                regionName.string(value);
            }
        }
    }

    public static RegionName build(String string) {
        RegionName name = new RegionName();
        name.string(string);
//        StringBuilder builder = new StringBuilder("\"");
        flatReplacePattern(name, accessExpressionPattern, (group, l) -> {
            String expression = group.substring(1);//removing "@"
            if (expression.isEmpty()) {
                expression = "name";
            }
            l.expression("this."+expression);
        });
        flatReplacePattern(name, indexAccessPattern, (group, l) -> {
            String number = group.substring(1);//removing "#"
            if (number.isEmpty()) {
                number = "0";
            }
            l.index(Integer.parseInt(number));
        });
        name.updateDimentions();
        return name;
    }

    public void add(Reference reference) {
        if (reference instanceof StringReference stringReference) {
            string(stringReference.value);
        } else items.add(reference);

    }

    public RegionName copy() {
        RegionName name = new RegionName();
        name.items.addAll(items);
        name.dimensions = dimensions;
        return name;
    }

    public void join(RegionName other) {
        for (Reference reference : other.items) {
            add(reference);
        }
        updateDimentions();
    }

    ;

    private void updateDimentions() {
        dimensions = (int) items.stream().filter(it -> it instanceof IndexReference).count();
    }

    public boolean isString() {
        for (Reference item : items) {
            if (!(item instanceof StringReference)) {
                return false;
            }
        }
        return true;
    }

    public String calculate(int... indecies) {
        String[] strings = new String[items.size()];
        for (int i = 0; i < strings.length; i++) {
            strings[i] = items.get(i).stringify(indecies);
        }
        return String.join("", strings);
    }

    public void index(int num) {
        items.add(new IndexReference(num));
    }

    public void string(String string) {
        int lastIdx = items.size() - 1;
        if (lastIdx >= 0 && items.get(lastIdx) instanceof StringReference last_) {
            items.set(lastIdx, new StringReference(
                last_.value + string
            ));
        } else {
            items.add(new StringReference(string));
        }
    }

    public void expression(String expression) {
        items.add(new ExpressionReference(expression));
    }


}
