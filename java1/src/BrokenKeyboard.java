public class BrokenKeyboard {
    public static int calculateFullyTypedWords(String message, String brokenKeys){
        int res=0;
        int words =0;
        String[] array = message.strip().split("\\s+");
        char[] chars = brokenKeys.toCharArray();
        for (String st: array) {
            st = st.strip();
            if(st.equals("")){
                continue;
            }
            words++;
            for(char ch: chars) {
                if (st.contains(String.valueOf(ch))){
                    res++;
                    break;
                }
            }
        }
        return words - res;
    }
}
