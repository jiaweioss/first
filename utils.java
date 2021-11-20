public class utils {

    //检查Token的value是否等于特定值
    public static boolean checkTokenValue(ASTNode Node, String value) {
        return Node.getToken().getValue().equals(value);
    }

    //检查block块中是否有该变量

    public static Identifier searchKey(String name, Integer blockID) {
        Block temp = IRBlockMap.getBlockMap().get(blockID);

        while (!temp.Identifiers.containsKey(name)) {
            temp = temp.Father;
        }
        return temp.Identifiers.get(name);
    }
}
