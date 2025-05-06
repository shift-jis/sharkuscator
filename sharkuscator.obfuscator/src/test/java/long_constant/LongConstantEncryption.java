package long_constant;

import java.lang.invoke.MethodHandles;

public class LongConstantEncryption {
    public static void main(String[] args) {
        OperationTarget instance = BitwiseStateObject.createAndRegisterInstance(-6668866287544365101L, -1571766388775559791L, MethodHandles.lookup().lookupClass());
        System.out.println(instance.processLongValue(19959084951498L));
    }
}
