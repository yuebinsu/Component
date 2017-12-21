package priv.syb.component.nfc;

/**
 * Created by：Administrator on 2017/12/14 17:53
 * 619389279@qq.com
 */
public final class Util {
    private final static char[] HEX = { '0', '1', '2', '3', '4', '5', '6', '7',
            '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };
    private Util() {
    }
    /**
     * 测试使用的主函数
     * @param args   命令行参数
     */
    public static void main(String[] args) {
        int ntest = 200;
        byte val;
        byte [] res = IntToHexStr(ntest);
        val = res[0];
        //
        byte[] val2 = new byte[] {0x11,(byte) 0xaa,(byte) 0xbb};
        byte [] res2 =new byte[6];
        HexToAscii(val2,res2,3);

        byte[] val3 = new byte[]{'1','a','2','b'};
        int res3 = HexStrToInt(val3,4);
    }
    /*将整型数据转换为HEX字节码
    如 :200转换为000000C8
    * */
    public static byte[] toBytes(int a) {
        return new byte[] { (byte) (0x000000ff & (a >>> 24)),
                (byte) (0x000000ff & (a >>> 16)),
                (byte) (0x000000ff & (a >>> 8)), (byte) (0x000000ff & (a)) };
    }

    //************************************
    // 函数功能:  16进制数组值转为整型
    // 函数全名:  HexToInt
    // 作    者:  陶志高
    // 访问权限:  public
    // 返回类型:  TXTCONVERSIONE_API BOOL WINAPI
    // 参数说明:  char * pHex   如：0x1A2B,最多4字节
    // 参数说明:  int * intVal  转换后的int值6699
    // 参数说明:  int hexlen
	/*
    将b的startpos开始的连续nbytes字节转换为INT数据
    如：000000C8转换为200*/
    //************************************
    public static int toInt(byte[] b, int startpos, int nbytes) {
        int ret = 0;
        final int endpos = startpos + nbytes;
        for (int i = startpos; i < endpos; ++i) {
            ret <<= 8;
            ret |= b[i] & 0xFF;
        }
        return ret;
    }

    /*
    将b的startpos结束的连续nbytes字节转换为INT数据(b中的整型数据是高低字节倒序的)
    如：000000C8转换为200
     */
    public static int toIntR(byte[] b, int s, int n) {
        int ret = 0;
        for (int i = s; (i >= 0 && n > 0); --i, --n) {
            ret <<= 8;
            ret |= b[i] & 0xFF;
        }
        return ret;
    }

    /*
    * */
    public static int toInt(byte... b) {
        int ret = 0;
        for (final byte a : b) {
            ret <<= 8;
            ret |= a & 0xFF;
        }
        return ret;
    }

    /*
	    nbytes      d的长度
	    startpos    d中要转换为字符串的字节起始位置  start
     */
    public static String toHexString(byte[] d, int startpos, int nbytes) {
        final char[] ret = new char[nbytes * 2];
        final int endpos = startpos + nbytes;
        int x = 0;
        for (int i = startpos; i < endpos; ++i) {
            final byte v = d[i];
            ret[x++] = HEX[0x0F & (v >> 4)];
            ret[x++] = HEX[0x0F & v];
        }
        return new String(ret);
    }

    public static String toHexStringR(byte[] d, int s, int n) {
        final char[] ret = new char[n * 2];

        int x = 0;
        for (int i = s + n - 1; i >= s; --i) {
            final byte v = d[i];
            ret[x++] = HEX[0x0F & (v >> 4)];
            ret[x++] = HEX[0x0F & v];
        }
        return new String(ret);
    }

    public static int parseInt(String txt, int radix, int def) {
        int ret;
        try {
            ret = Integer.valueOf(txt, radix);
        } catch (Exception e) {
            ret = def;
        }
        return ret;
    }

    public static String toAmountString(float value) {
        return String.format("%.2f", value);
    }

    public static String toBcdCardno(String cardno){
        String cardnoafter = cardno.substring(8);
        while(cardnoafter.substring(0,1).equals("0")){
            cardnoafter = cardnoafter.substring(1);
        }
        String bcdcard = String.valueOf(Integer.parseInt(cardnoafter+"",16));
        while(bcdcard.length()<8){
            bcdcard="0"+bcdcard;
        }
        return cardno.substring(0,8)+bcdcard;
    }

    public static String toHexCardno(String cardno){
        String cardnoafter = cardno.substring(8);
        while(cardnoafter.substring(0,1).equals("0")){
            cardnoafter = cardnoafter.substring(1);
        }
        String bcdcard = String.valueOf(Integer.parseInt(cardnoafter+"",16));
        while(bcdcard.length()<8){
            bcdcard="0"+bcdcard;
        }
        return cardno.substring(0,8)+bcdcard;
    }
    /////////////////////////////////////////////////////////
    //************************************
    // 功    能:    将一个十六进制字节串转换成 ASCII 码表示的字符串
    // 函 数 名:    HexToAscii
    // 返 回 值:    void
    // 作    者：   陶志高
    // 参    数:    unsigned char * pHex     要转换的十六进制数字节串首地址
    // 参    数:    unsigned char * pAscii   转换后的 ASCII 码表示的字符串的首地址
    // 参    数:    int nHexLen                 要转换的十六进制数的长度（字节数）
    //说明   不对数组越界进行检查 例: "\x12\x34\x56\x78"->"12345678" nHexLen=4 转换后8字节
    //************************************
    public static void HexToAscii(byte[] pHex, byte[] pAscii, int nHexLen)
    {
        byte curChar;
        for (int nChar = 0 ;nChar < nHexLen ; ++nChar)
        {
            //高四字节
            curChar = (byte) ((pHex[nChar] & 0xF0)>>4);
            if (curChar >= 0x00 && curChar <= 0x09)
            {
                //0-9
                pAscii[nChar*2] = (byte) (curChar + 0x30);
            }
            if (curChar >= 0x0A)
            {
                //A-F
                pAscii[nChar*2] = (byte) (curChar + 0x37);
            }
            //低四字节
            curChar = (byte) (pHex[nChar] & 0x0F);
            if (curChar >= 0x00 && curChar <= 0x09)
            {
                //0-9
                pAscii[nChar*2+1] = (byte) (curChar + 0x30);
            }
            if (curChar >= 0x0A)
            {
                //A-F
                pAscii[nChar*2+1] = (byte) (curChar + 0x37);
            }
        }
    }

    //************************************
    // 功    能:    将一个 ASCII 码表示的十六进制字符串转换成十六进制的字节串(值)
    // 函 数 名:    AsciiToHex
    // 返 回 值:    BOOL  不是0-9 A-F a-f返回FALSE
    // 作    者：   陶志高
    // 参    数:    unsigned char * pAscii  要转换的 ASCII 码表示的十六进制字符串的首地址
    // 参    数:    unsigned char * pHex    转换后的十六进制数字节串首地址
    // 参    数:    int nASCLen             要转换的 ASCII 码表示的十六进制字符串的长度（字节数）
    //说明：自动去掉空格，不对数组越界进行检查, 例  "12 34 5678"->"\x12\x34\x56\x78" nlen=8 转换后4字节
    //************************************
    public static boolean AsciiToHex(byte[] pAscii, byte[] pHex, int nASCLen)
    {
        byte curChar;
        int nHex = 0 , nSpace = 0;
        boolean bHight = false;
        for (int nChar = 0 ;nChar < nASCLen ; ++nChar)
        {
            //第N个字符
            curChar = pAscii[nChar];
            if (curChar == ' ')
            {
                //去掉空格
                ++nSpace;
                continue;
            }
            //对应的16进制数
            nHex = (nChar - nSpace)/2;
            bHight = (((nChar - nSpace)%2) == 1)?false:true;
            if (curChar >= '0' && curChar <= '9')
            {
                //0-9
                if (bHight)
                {
                    pHex[nHex] = (byte) ((curChar - 0x30)<<4);
                }
                else
                {
                    pHex[nHex] = (byte) (pHex[nHex] + curChar - 0x30);
                }
                continue;
            }
            if (curChar >= 'a' && curChar <= 'f')
            {
                //a-f
                if (bHight)
                {
                    pHex[nHex] = (byte) ((curChar - 0x57)<<4);
                }
                else
                {
                    pHex[nHex] = (byte) (pHex[nHex] + curChar - 0x57);
                }
                continue;
            }
            if (curChar >= 'A' && curChar <= 'F')
            {
                //A-F
                if (bHight)
                {
                    pHex[nHex] = (byte) ((curChar - 0x37)<<4);
                }
                else
                {
                    pHex[nHex] = (byte) (pHex[nHex] + curChar - 0x37);
                }
                continue;
            }
            return false;
        }
        return true;
    }

    //************************************
    // 函数功能:  16进制字符串转为整型
    // 函数全名:  HexStrToInt
    // 作    者:  陶志高
    // 访问权限:  public
    // 返回类型:  TXTCONVERSIONE_API BOOL WINAPI
    // 参数说明:  char * pHexStr  如：“1A2B”最多8个字符，4字节
    // 参数说明:  int * intVal    转换后的int值6699
    // 参数说明:  int hexlen      字符串长度
    //************************************
    public static int HexStrToInt(byte[] pHexStr , int hexlen)
    {
        int  intVal = 0;
        byte[] pHex = new byte[4];
        if (!AsciiToHex(pHexStr,pHex,hexlen))
        {
            return 0;
        }
        intVal = toInt(pHex,0,hexlen/2);
        return intVal;
    }

    //************************************
    // 函数功能:  整型转为16进制数组
    // 函数全名:  IntToHex
    // 作    者:  陶志高
    // 访问权限:  public
    // 返回类型:  TXTCONVERSIONE_API BOOL WINAPI
    // 参数说明:  char * pHexSource  最多4字节,如：36转换后为0x00000024
    // 参数说明:  int  intVal  要转换的整数
    // 参数说明:  int* hexlen   转换后的HEX长度,总长度为4字节，hexlen为实际数据字节长度，如36转换后为0x00000024，hexlen＝1
    //************************************
    public static byte[] IntToHex(  int intVal)
    {
        return toBytes(intVal);
    }

    //************************************
    // 函数功能:  整型转为16进制字符串
    // 函数全名:  IntToHexStr
    // 作    者:  陶志高
    // 访问权限:  public
    // 返回类型:  TXTCONVERSIONE_API BOOL WINAPI
    // 参数说明:  char * pHexStr   最多8字节,如：36转换后为0x00000024
    // 参数说明:  int  intVal   要转换的整数
    // 参数说明:  int * hexlen     转换后的HEX长度
    //************************************
    public static byte[] IntToHexStr( int intVal )
    {
        byte[] pHexStr = new byte[8];
        byte[] pHex = IntToHex(intVal);
        HexToAscii(pHex,pHexStr,4);
        return pHexStr;
    }
}