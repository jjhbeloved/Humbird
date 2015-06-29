package org.humbird.soa.common.tools;

import org.humbird.soa.common.model.common.MapModel;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by david on 14/12/17.
 */
public class TString {

    /**
     * <b>简易字符串分割</b><br/>
     * 本方法用来根据指定字符，将某字符串以此为分割，拆分成多个子字符串。
     * 对于分割字符串功能，在 Java 6.0 以内，都只提供了支持正则表达式的
     * {@link String#split(String) split} 方法。此方法为追求通用，即便是简单的
     * 分割，也会基于正则表达式来进行。即便是编译过的正则表达式，其性能也无法与简单
     * 的字符相等判断相提并论。<br/>
     * 本方法不涉及正则表达式，通过遍历原字符串对应的字符数组来寻找符合分割字符的
     * 字符，然后通过 {@link String#substring(int, int)} 来获取每一个分割字符之间
     * 的子字符串，存入一个 {@link java.util.LinkedList} 中。这是一个功能简单但高效的方法。
     * 如果规模比较大，拟考虑先通过一次循环，取得原字符串中分割字符的数量，以此制作
     * 定长的 {@link java.util.ArrayList} 。
     * 本方法尤其适用于常见的由半角逗号结合在一起的字符串的分割。<br/>
     * 在编写之初，本方法曾采取将字符串的字符数组分段处理，通过系统字符串复制来形成
     * 一个个子字符串。后经考证，{@link String#substring(int, int)} 是一个很高效的
     * 方法，遂改。效率提高了一倍。
     * 本方法使用示例如下：
     * <pre>
     * String source = "a b c d e f g h i j k l m n o p q r s t u v w x y z";
     * List<String> secs = StringTool.splitSimpleString(source, ' ');</pre>
     * 此示例中，{@link String} source 为原字符串。{@link java.util.List} secs 为删除空格后
     * 的结果。
     * @see     String#split(String)
     * @see     String#substring(int, int)
     * @param   source  待被处理的字符串，即本方法的“原字符串”
     * @param   gap     分割字符
     * @return      从指定原字符按分割字符拆分成的子字符串列表
     * @exception   NullPointerException    当传入参数 source 为空时
     */
    public static List<String> splitSimpleString(String source, char gap) {
        return splitSimpleString(source, String.valueOf(gap));
    }

    /**
     * 根据 gap 快速分割字符串
     * @param source 元字符串
     * @param gap 分隔符
     * @return List<String>对象
     */
    public static List<String> splitSimpleString(String source, String gap) {
        synchronized (source) {
            List<String> result = new LinkedList<String>();
            char[] sourceChars = source.toCharArray();
            String section = null;
            int startIndex = 0;
            char split = gap.charAt(0);
            for (int index = -1; ++index != sourceChars.length; ) {
                if (sourceChars[index] != split) continue;
                section = source.substring(startIndex, index);
                result.add(section);
                startIndex = index + 1;
            }
            section = source.substring(startIndex, sourceChars.length);
            result.add(section);
            return result;
        }
    }

    /**
     * 根据 gap 快速分割字符串
     * @param source 元字符串
     * @param gap 分隔符
     * @return List<String>对象
     */
    public static List<String> splitSimpleString(String source, String gap, Map<Integer, String> replacesVal) {
        synchronized (source) {
            List<String> result = new LinkedList<String>();
            char[] sourceChars = source.toCharArray();
            String section = null;
            int startIndex = 0;
            int count = 0;
            char split = gap.charAt(0);
            StringBuilder newStr = new StringBuilder("");

            for (int index = -1; ++index != sourceChars.length; ) {
                if (sourceChars[index] != split) continue;
                section = source.substring(startIndex, index);
                result.add(section);
                startIndex = index + 1;
                if(count > 0) newStr.append(gap);
                if (replacesVal.containsKey(count)) {
                    newStr.append(replacesVal.get(count));
                } else {
                    newStr.append(section);
                }
                count++;
            }
            section = source.substring(startIndex, sourceChars.length);
            result.add(section);
            newStr.append(gap);
            if (replacesVal.containsKey(count)) {
                newStr.append(replacesVal.get(count));
            } else {
                newStr.append(section);
            }
            result.add(newStr.toString());
            return result;
        }
    }

    /**
     * 根据 gap 快速分割字符串
     * @param source 元字符串
     * @param gap 分隔符
     * @return List<String>对象
     */
    public static List<String> splitSimpleString(String source, String gap, List<MapModel> mapModels, MapModel.Callback callback) {
        synchronized (source) {
            List<String> result = new LinkedList<String>();
            char[] sourceChars = source.toCharArray();
            String section = null;
            int startIndex = 0;
            char split = gap.charAt(0);
            StringBuilder newStr = new StringBuilder("");

            for (int index = -1; ++index != sourceChars.length; ) {
                if (sourceChars[index] != split) continue;
                section = source.substring(startIndex, index);
                result.add(section);
                startIndex = index + 1;
            }
            section = source.substring(startIndex, sourceChars.length);
            result.add(section);
            for(MapModel mapModel : mapModels) {
                newStr.append(result.get(Integer.parseInt(mapModel.getTrue(callback))));
            }
            result.add(newStr.toString());
            return result;
        }
    }

    /**
     * <b>字符串删除其中某字符</b><br/>
     * 本方法用来移除指定字符串中的指定字符。
     * 在 Java 6.0 以内，删除字符串中的指定字符，需要通过
     * {@link String#replace(CharSequence, CharSequence) replace} 方法，将指定单
     * 字符的字符串，替换成空字符串来实现。而 <code>replace</code> 方法为了追求通用，
     * 使用了正则表达式参数。即便是编译过的正则表达式，其性能也无法与简单的字符相等
     * 判断相提并论。<br/>
     * 本方法不涉及正则表达式，通过遍历原字符串对应的字符数组来寻找符合待删除字符的
     * 字符，然后通过 {@link StringBuilder} 来追加其余字符。这是一个简单但高效的方法。
     * <br/>
     * 本方法编写之初，曾试图通过 <code>StringBuilder</code> 的功能来直接删除字符串
     * 中待删除字符。后经 www.iteye.com 网站用户 shenyuc629 提示，并经过考证，发现
     * {@link StringBuilder#deleteCharAt(int) deleteCharAt} 方法并不高效，应该是
     * 因为其内部每次删除都进行了数组迁移。遂改为追加方式，效率提高了 2 倍多。<br/>>>
     * 本方法使用示例如下：
     * <pre>
     * String source = "a b c d e f g h i j k l m n o p q r s t u v w x y z";
     * String removed = StringTool.removeChar(source, ' ');</pre>
     * 此示例中，{@link String} source 为原字符串。String removed 为删除空格后的
     * 结果。
     * @see     String#replace(CharSequence, CharSequence)
     * @see     StringBuilder#append(char)
     * @param   source  待被处理的字符串，即本方法的“原字符串”
     * @param   srcChar 需要从原字符串中取代的字符
     * @param   tarChar 取代的字符
     * @return      从指定原字符串中移除指定字符后所得的结果字符串
     * @exception   NullPointerException    当传入参数 source 为空时
     */
    public static String replaceChar(String source, char srcChar, char tarChar) {
        StringBuilder builder = new StringBuilder();
        for (char c: source.toCharArray())
            if (c != srcChar) {
                builder.append(c);
            } else {
                builder.append(tarChar);
            }
        return builder.toString();
    }

    /**
     * 快速取代字符串
     * @param source 元字符串
     * @param srcChar 源分隔符
     * @param tarChar 目标分隔符
     * @return 新字符串
     */
    public static String replaceChar(String source, String srcChar, String tarChar) {
        return replaceChar(source, srcChar.charAt(0), tarChar.charAt(0));
    }

    public static String builderToString(Object ...values) {
        StringBuilder _sb_ = new StringBuilder("");
        for(int i=0, size=values.length; i<size; i++) {
            _sb_.append(values[i]);
        }
        String value = _sb_.toString();
        _sb_.setLength(0);
        return value;
    }
}
