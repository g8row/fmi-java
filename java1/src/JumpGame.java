public class JumpGame {
    private static boolean canWinRec(int[] array, int pos){
        if(pos == array.length-1){
            return true;
        }else{
            for(int i=1;i<=array[pos] && pos+i < array.length;i++){
                if(canWinRec(array, pos+i)){
                    return true;
                }
            }
            return false;
        }
    }
    public static boolean canWin(int[] array){
        return canWinRec(array,0);
    }
}
