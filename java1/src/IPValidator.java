public class IPValidator {
    public static boolean validateIPv4Address(String str) {
        String[] arr = str.split("\\.");
        if (arr.length != 4) {
            return false;
        }
        for (String curr : arr) {
            int temp;
            try {
                temp = Integer.parseInt(curr);
            } catch (NumberFormatException e) {
                return false;
            }
            if(curr.length() != String.valueOf(temp).length()){
                return false;
            }
            if (temp < 0 || temp > 255) {
                return false;
            }
        }
        return true;
    }
}
