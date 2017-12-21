package priv.syb.component.nfc;

import android.nfc.tech.IsoDep;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by：Administrator on 2017/12/14 17:49
 * 619389279@qq.com
 */

    //7816规范接口
    public class Iso7816 {
        public static final byte[] EMPTY = { 0 };

        protected byte[] data;

        protected Iso7816() {
            data = Iso7816.EMPTY;
        }
        protected Iso7816(byte[] bytes) {
            data = (bytes == null) ? Iso7816.EMPTY : bytes;
        }

        public boolean match(byte[] bytes) {
            return match(bytes, 0);
        }

        public boolean match(byte[] bytes, int start) {
            final byte[] data = this.data;
            if (data.length <= bytes.length - start) {
                for (final byte v : data) {
                    if (v != bytes[start++])
                        return false;
                }
            }
            return true;
        }

        /*
         *  判断本TAG是否是指令的TAG
         */
        public boolean match(byte tag) {
            return (data.length == 1 && data[0] == tag);
        }

        public boolean match(short tag) {
            final byte[] data = this.data;
            if (data.length == 2) {
                final byte d0 = (byte) (0x000000FF & tag);
                final byte d1 = (byte) (0x000000FF & (tag >> 8));
                return (data[0] == d0 && data[1] == d1);
            }
            return false;
        }

        public int size() {
            return data.length;
        }

        public byte[] getBytes() {
            return data;
        }

        /*	将读取的数据转换为字符串输出	 */
        public String toString() {
            return Util.toHexString(data, 0, data.length);
        }

        public final static class ID extends Iso7816 {
            public ID(byte[] bytes) {
                super(bytes);
            }
        }


        //标签模板类，解析标签  TLV-T
        public final static class BerT extends Iso7816 {
            // tag template
            public static final byte TMPL_FCP = 0x62; // File Control Parameters
            public static final byte TMPL_FMD = 0x64; // File Management Data
            public static final byte TMPL_FCI = 0x6F; // FCP and FMD

            // proprietary information
            public final static BerT CLASS_PRI = new BerT((byte) 0xA5);
            // short EF identifier
            public final static BerT CLASS_SFI = new BerT((byte) 0x88);
            // dedicated file name
            public final static BerT CLASS_DFN = new BerT((byte) 0x84);
            // application data object
            public final static BerT CLASS_ADO = new BerT((byte) 0x61);
            // application id
            public final static BerT CLASS_AID = new BerT((byte) 0x4F);

            public BerT(byte tag) {
                this(new byte[] { tag });
            }

            public BerT(short tag) {
                this(new byte[] { (byte) (0x000000FF & (tag >> 8)),
                        (byte) (0x000000FF & tag) });
            }

            public BerT(byte[] bytes) {
                super(bytes);
            }

            //T数据长度
            public static int test(byte[] bytes, int start) {
                int len = 1;
                if ((bytes[start] & 0x1F) == 0x1F) {
                    while ((bytes[start + len] & 0x80) == 0x80) {
                        ++len;
                    }
                    ++len;
                }
                return len;
            }

            public static BerT read(byte[] bytes, int start) {
                return new BerT(Arrays.copyOfRange(bytes, start,
                        start + test(bytes, start)));
            }

            //是否含有子标签(复合标签)
            public boolean hasChild() {
                return ((data[0] & 0x20) == 0x20);
            }
        }

        //标签模板类，解析长度  TLV-L
        public final static class BerL extends Iso7816 {
            private final int val;

            public BerL(byte[] bytes) {
                super(bytes);
                val = calc(bytes, 0);
            }
            //L数据长度
            public static int test(byte[] bytes, int start) {
                int len = 1;
                if ((bytes[start] & 0x80) == 0x80) {
                    len += bytes[start] & 0x07;
                }
                return len;
            }

            //计算数据长度
            public static int calc(byte[] bytes, int start) {
                if ((bytes[start] & 0x80) == 0x80) {
                    int v = 0;
                    int e = start + bytes[start] & 0x07;
                    while (++start <= e) {
                        v <<= 8;
                        v |= bytes[start] & 0xFF;
                    }
                    return v;
                }
                return bytes[start];
            }

            public static BerL read(byte[] bytes, int start) {
                return new BerL(Arrays.copyOfRange(bytes, start,
                        start + test(bytes, start)));
            }

            public int toInt() {
                return val;
            }
        }

        //标签模板类，解析值  TLV-V
        public final static class BerV extends Iso7816 {
            public static BerV read(byte[] bytes, int start, int len) {
                return new BerV(Arrays.copyOfRange(bytes, start, start + len));
            }

            public BerV(byte[] bytes) {
                super(bytes);
            }
        }

        //标签模板类，解析TLV记录数据
        public final static class BerTLV extends Iso7816 {
            public final BerT t;
            public final BerL l;
            public final BerV v;

            public BerTLV(BerT t, BerL l, BerV v) {
                this.t = t;
                this.l = l;
                this.v = v;
            }
            //数据总数据长度
            public static int test(byte[] bytes, int start) {
                //TLV标签数据长度
                final int lt = BerT.test(bytes, start);
                //TLV长度数据长度
                final int ll = BerL.test(bytes, start + lt);
                //TLV数据长度
                final int lv = BerL.calc(bytes, start + lt);

                return lt + ll + lv;
            }

            public static BerTLV read(Iso7816 obj) {
                return read(obj.getBytes(), 0);
            }

            public static BerTLV read(byte[] bytes, int start) {
                int s = start;
                final BerT t = BerT.read(bytes, s);
                s += t.size();

                final BerL l = BerL.read(bytes, s);
                s += l.size();

                final BerV v = BerV.read(bytes, s, l.toInt());
                s += v.size();

                final BerTLV tlv = new BerTLV(t, l, v);
                tlv.data = Arrays.copyOfRange(bytes, start, s);

                return tlv;
            }

            public static ArrayList<BerTLV> readList(Iso7816 obj) {
                return readList(obj.getBytes());
            }

            public static ArrayList<BerTLV> readList(final byte[] data) {
                final ArrayList<BerTLV> ret = new ArrayList<BerTLV>();

                int start = 0;
                int end = data.length - 3;
                while (start < end) {
                    final BerTLV tlv = read(data, start);
                    ret.add(tlv);

                    start += tlv.size();
                }

                return ret;
            }

            public BerTLV getChildByTag(BerT tag) {
                if (t.hasChild()) {
                    final byte[] raw = v.getBytes();
                    int start = 0;
                    int end = raw.length;
                    while (start < end) {
                        if (tag.match(raw, start))
                            return read(raw, start);

                        start += test(raw, start);
                    }
                }

                return null;
            }

            public BerTLV getChild(int index) {
                if (t.hasChild()) {
                    final byte[] raw = v.getBytes();
                    int start = 0;
                    int end = raw.length;

                    int i = 0;
                    while (start < end) {
                        if (i++ == index)
                            return read(raw, start);
                        start += test(raw, start);
                    }
                }
                return null;
            }
        }

        /**************************************
         * 20160303－－－陶志高
         * Response类说明：
         * 本类为所有卡交互时产生的卡片返回数据处理
         * 使用方法：通过Tag类的接口调用后，返回的Response对象，调用相应的方法获得卡片返回的数据
         * 一般接口：getSw12，    获取卡片对APDU指令执行的结果状态
         *        getBytes，获取卡片返回的数据，如随机数、记录数据等
         *************************************/
        //返回数据与状态码
        public final static class Response extends Iso7816 {
            public static final byte[] EMPTY = {};
            public static final byte[] ERROR = { 0x6F, 0x00 }; // SW_UNKNOWN

            public Response(byte[] bytes) {
                super((bytes == null || bytes.length < 2) ? Response.ERROR : bytes);
            }
            //获取状态码高字节
            public byte getSw1() {
                return data[data.length - 2];
            }
            //获取状态码低字节
            public byte getSw2() {
                return data[data.length - 1];
            }

            //获取状态码
            public short getSw12() {
                final byte[] d = this.data;
                int n = d.length;
                return (short) ((d[n - 2] << 8) | (0xFF & d[n - 1]));
            }

            public boolean isOkey() {
                return equalsSw12(SW_NO_ERROR);
            }

            public boolean equalsSw12(short val) {
                return getSw12() == val;
            }

            //获取数据长度，不带状态码
            public int size() {
                return data.length - 2;
            }

            //获取返回数据
            public byte[] getBytes() {
                return isOkey() ? Arrays.copyOfRange(data, 0, size())
                        : Response.EMPTY;
            }
        }
        /*****************************************************************
         * 20160303－－－陶志高
         * Tag类说明：
         * 本类提供NFC与卡片交互功能，即发送APDU，接收返回数据及状态码
         * 使用方法：创建TAG后，即可调用相应接口功能
         * 交易步骤：connect(NFC连接卡片)-->getATR(卡片复位)-->业务接口-->close
         * 7816标准交易接口：
         * 				verify       校验PIN
         * 				readRecord   读取记录
         * 				readBinary   读取二进制文件的数据
         *  			readData	   读取文件数据
         *   			selectByID	   通过SID\FID选择应用或文件
         *    			selectByName 通过AID选择应用
         *    			transceive   发送APDU，接收	Response
         *
         *    			connect      打开NFC连接，与卡片进行通讯，类似于打开读卡器
         *    			close        关闭连接
         * 一卡通业务接口：
         *
         *****************************************************************/
        //操作NFC与卡片交互的类
        public final static class Tag {
            private final IsoDep nfcTag;
            private ID id;
            /************************
             * 应用版本号
             * 0－－老宝岛通
             * 1--交通部
             * 100--无效卡
             ***********************/
            private int apver;

            public Tag(IsoDep tag) {
                nfcTag = tag;
                id = new ID(tag.getTag().getId());
            }

            public ID getID() {
                return id;
            }

            //校验PIN
            public Response verify(byte[] pin) {
                ByteBuffer buff = ByteBuffer.allocate(pin.length + 6);
                buff.put((byte) 0x00) // CLA Class
                        .put((byte) 0x20) // INS Instruction
                        .put((byte) 0x00) // P1 Parameter 1
                        .put((byte) 0x00) // P2 Parameter 2
                        .put((byte) pin.length) // Lc
                        .put(pin).put((byte) 0x00); // Le
                return new Response(transceive(buff.array()));
            }

            //读取记录文件的记录数据
            public Response readRecord(int sfi, int index) {
                final byte[] cmd = { (byte) 0x00, // CLA Class
                        (byte) 0xB2, // INS Instruction
                        (byte) index, // P1 Parameter 1
                        (byte) ((sfi << 3) | 0x04), // P2 Parameter 2
                        (byte) 0x00, // Le
                };
                return new Response(transceive(cmd));
            }

            /**取指定文件的第一条记录*/
            public Response readRecord(int sfi) {
                final byte[] cmd = { (byte) 0x00, // CLA Class
                        (byte) 0xB2, // INS Instruction
                        (byte) 0x01, // P1 Parameter 1
                        (byte) ((sfi << 3) | 0x05), // P2 Parameter 2
                        (byte) 0x00, // Le
                };
                return new Response(transceive(cmd));
            }

            /**读取SFI指定的二进制文件数据***/
            public Response readBinary(int sfi) {
                final byte[] cmd = { (byte) 0x00, // CLA Class
                        (byte) 0xB0, // INS Instruction
                        (byte) (0x00000080 | (sfi & 0x1F)), // P1 Parameter 1
                        (byte) 0x00, // P2 Parameter 2
                        (byte) 0x00, // Le
                };
                return new Response(transceive(cmd));
            }
            /**从beginIndex开始读取SFI指定的二进制文件的len长度的数据***/
            public Response readBinary(int sfi,byte beginIndex,byte len) {
                final byte[] cmd = { (byte) 0x00, // CLA Class
                        (byte) 0xB0, // INS Instruction
                        (byte) (0x00000080 | (sfi & 0x1F)), // P1 Parameter 1
                        (byte) beginIndex, // P2 Parameter 2
                        (byte) len, // Le
                };
                return new Response(transceive(cmd));
            }

            public Response readData(int sfi) {
                final byte[] cmd = { (byte) 0x80, // CLA Class
                        (byte) 0xCA, // INS Instruction
                        (byte) 0x00, // P1 Parameter 1
                        (byte) (sfi & 0x1F), // P2 Parameter 2
                        (byte) 0x00, // Le
                };
                return new Response(transceive(cmd));
            }

            //通过短文件标识(FID)选择
            public Response selectByID(byte... name) {
                ByteBuffer buff = ByteBuffer.allocate(name.length + 6);
                buff.put((byte) 0x00) // CLA Class
                        .put((byte) 0xA4) // INS Instruction
                        .put((byte) 0x00) // P1 Parameter 1
                        .put((byte) 0x00) // P2 Parameter 2
                        .put((byte) name.length) // Lc
                        .put(name).put((byte) 0x00); // Le
                return new Response(transceive(buff.array()));
            }

            //通过应用名(AID)选择
            public Response selectByName(byte... name) {
                ByteBuffer buff = ByteBuffer.allocate(name.length + 6);
                buff.put((byte) 0x00) // CLA Class
                        .put((byte) 0xA4) // INS Instruction
                        .put((byte) 0x04) // P1 Parameter 1
                        .put((byte) 0x00) // P2 Parameter 2
                        .put((byte) name.length) // Lc
                        .put(name).put((byte) 0x00); // Le
                return new Response(transceive(buff.array()));
            }

            //打开NFC连接
            public void connect() {
                try {
                    nfcTag.connect();
                } catch (Exception e) {
                }
            }

            //关闭NFC连接
            public void close() {
                try {
                    nfcTag.close();
                } catch (Exception e) {
                }
            }

            //获取复位信息
            public Response getATR() {
                return new Response(nfcTag.getHistoricalBytes());
            }
            //发送卡片指令
            public byte[] transceive(final byte[] cmd) {
                try {
                    return nfcTag.transceive(cmd);
                } catch (Exception e) {
                    return Response.ERROR;
                }
            }

            /**********************************************************************************************
             * 一卡通定制的业务接口辅助函数
             **********************************************************************************************/
            //选择到主应用目录--3F00
            public Response SelectMainDir() {
                apver = 0;
                /**因为AID与银行AID冲突，天喻制的联名卡以62616F64616F746F6E673031303030这个为3F00的AID。
                 * 联名卡选择3F00时，默认进入宝岛通应用，联名卡选择315041592E5359532E4444463031时，
                 * 默认进入银行卡应用。所以机具进行联名卡交易时，应选择3F00或者不选择MF*/
                byte[] FID_DFI_MAIN = { (byte) 0x3F, (byte) 0x00};
                Response tempres = selectByID(FID_DFI_MAIN);
                if (!tempres.isOkey())
                {
                    apver = 100;
                    return new Response(Response.ERROR);
                }
                byte[] DFI_MAIN = { (byte) 0x31, (byte) 0x50
                        , (byte) 0x41, (byte) 0x59, (byte) 0x2E, (byte) 0x53
                        , (byte) 0x59, (byte) 0x53, (byte) 0x2E, (byte) 0x44
                        , (byte) 0x44, (byte) 0x46, (byte) 0x30, (byte) 0x31};
                tempres = selectByName(DFI_MAIN);
                if (!tempres.isOkey())
                {
                    //交通部主应用ID与原宝岛通应用ID不一样
                    byte[] NEW_DFI_MAIN = { (byte) 0x31, (byte) 0x50
                            , (byte) 0x41, (byte) 0x59, (byte) 0x2E, (byte) 0x53
                            , (byte) 0x59, (byte) 0x53, (byte) 0x2E, (byte) 0x44
                            , (byte) 0x44, (byte) 0x46, (byte) 0x30, (byte) 0x32};
                    tempres = selectByName(NEW_DFI_MAIN);
                    apver = 1;
                    if (!tempres.isOkey())
                    {
                        apver = 100;
                        return new Response(Response.ERROR);
                    }
                }
                return tempres;
            }

            //选择电子钱包
            public Response SelectEP() {
                byte[] DFI_EP_VER0 = {(byte)0x4D,(byte)0x4F,(byte)0x54,(byte)0x2E,(byte)0x49
                        ,(byte)0x4E,(byte)0x54,(byte)0x45,(byte)0x52,(byte)0x43,(byte)0x49
                        ,(byte)0x54,(byte)0x59,(byte)0x30,(byte)0x31 };
                byte[] DFI_EP_VER1 = {(byte)0xA0,(byte)0x00,(byte)0x00,(byte)0x06,(byte)0x32
                        ,(byte)0x4D,(byte)0x4F,(byte)0x54,(byte)0x2E,(byte)0x43,(byte)0x50
                        ,(byte)0x54,(byte)0x49,(byte)0x43,(byte)0x30,(byte)0x32 };

                Response tempres = null;
                if (0 == apver)
                {
                    tempres = selectByName(DFI_EP_VER0);
                }
                else if (1 == apver)
                {
                    tempres = selectByName(DFI_EP_VER1);
                }
                if (!tempres.isOkey())
                {
                    return new Response(Response.ERROR);
                }
                return tempres;
            }
            /***************************************************************************************************
             * 一卡通定制的业务接口
             * 使用方法：
             *     所有接口，不带有应用选择，在做业务前，先选择到应用目录
             *****************************************************************************************************/

            /********************************
             * 读取电子钱包余额，isEP＝true
             * 读取电子存折余额，isEP＝false
             ********************************/
            public Response getBalance(boolean isEP) {
                final byte[] cmd = { (byte) 0x80, // CLA Class
                        (byte) 0x5C, // INS Instruction
                        (byte) 0x00, // P1 Parameter 1
                        (byte) (isEP ? 2 : 1), // P2 Parameter 2
                        (byte) 0x04, // Le
                };
                return new Response(transceive(cmd));
            }

            //获取卡号，暂支持老宝岛通结构
            /***************************
             * 返回10字节，如：F5899780000000001122
             * 当卡片做交易时，分散因子为9780000000001122，即，取返回的卡号的后8字节
             *******************************/
            public Response getCardno() {
        	/*
        	//选择主应用
        	Response tempres = SelectMainDir();
        	if (!tempres.isOkey())
            {
                return new Response(Response.ERROR);
            }
        	//选择电子钱包应用
            tempres = SelectEP();
            if (!tempres.isOkey())
            {
                return new Response(Response.ERROR);
            }
            */
                //00B0970C08
                final byte[] cmd = { (byte) 0x00, // CLA Class
                        (byte) 0xB0, // INS Instruction
                        (byte) 0x97, // P1 Parameter 1
                        (byte) 0x0A, // P2 Parameter 2
                        (byte) 0x0A, // Le
                };
                return new Response(transceive(cmd));
            }
            /****************************
             * 获取卡信息，返回数据参考卡结构规
             * 老卡为1001应用下的0017文件数据
             * 新卡为DF01应用下的0017文件
             **************************/
            public Response getCardInfo() {
                final byte[] cmd = { (byte) 0x00, // CLA Class
                        (byte) 0xB0, // INS Instruction
                        (byte) 0x97, // P1 Parameter 1
                        (byte) (0x00), // P2 Parameter 2
                        (byte) 0x00, // Le
                };
                return new Response(transceive(cmd));
            }

            //消费初始化
            public Response initPurchase(byte[] money ,byte[] termno) {
                ByteBuffer buff = ByteBuffer.allocate(16);
                buff.put((byte) 0x80) // CLA Class
                        .put((byte) 0x50) // INS Instruction
                        .put((byte) 0x01) // P1 Parameter 1
                        .put((byte) 0x02) // P2 Parameter 2
                        .put((byte) 0x0B) // Lc
                        .put((byte) 0x01)// keyid
                        .put(money).put(termno).put((byte) 0x00);//交易金额+终端机编号
                return new Response(transceive(buff.array()));
            }

            //圈存初始化
            /********************************
             money  圈存金额
             termno 终端序号
             *********************************
             Response返回的数据结构：(按字节定义)
             1-4     电子钱包旧余额
             5-6     电子钱包交易序号
             7       密钥版本
             8       算法ID
             9-12    伪随机数
             13-16   圈存MAC1
             *********************************/
            public Response initLoad(byte[] money ,byte[] termno) {
                ByteBuffer buff = ByteBuffer.allocate(17);
                buff.put((byte) 0x80) // CLA Class
                        .put((byte) 0x50) // INS Instruction
                        .put((byte) 0x00) // P1 Parameter 1
                        .put((byte) 0x02) // P2 Parameter 2
                        .put((byte) 0x0B) // Lc
                        .put((byte) 0x01)// keyid
                        .put(money).put(termno).put((byte) 0x00);//交易金额+终端机编号
                return new Response(transceive(buff.array()));
            }
            /***********************圈存交易
             transtime  交易时间
             mac2       充值MAC2
             esponse返回的数据结构：(按字节定义)
             1-4 TAC
             **********************************/
            public Response Load(byte[] transtime ,byte[] mac2) {
                ByteBuffer buff = ByteBuffer.allocate(17);
                buff.put((byte) 0x80) // CLA Class
                        .put((byte) 0x52) // INS Instruction
                        .put((byte) 0x00) // P1 Parameter 1
                        .put((byte) 0x00) // P2 Parameter 2
                        .put((byte) 0x0B) // Lc
                        .put(transtime).put(mac2).put((byte) 0x00);//交易金额+终端机编号
                return new Response(transceive(buff.array()));
            }

            /***************************消费交易
             money     消费金额
             mac1      交易MAC1
             termjyxh  终端交易序号
             *********************************
             Response返回的数据结构：(按字节定义)
             1-4     电子钱包旧余额
             5-6     电子钱包交易序号
             7-9     透支限额
             10      密钥版本
             11      算法标识
             12-15    伪随机数
             ***********************************/
            public Response purchase(byte[] transtime ,byte[] mac1 ,byte[] termjyxh) {
                ByteBuffer buff = ByteBuffer.allocate(21);
                buff.put((byte) 0x80) // CLA Class
                        .put((byte) 0x54) // INS Instruction
                        .put((byte) 0x01) // P1 Parameter 1
                        .put((byte) 0x00) // P2 Parameter 2
                        .put((byte) 0x0F) // Lc
                        .put(termjyxh).put(transtime).put(mac1).put((byte) 0x00);//终端交易序号+交易时间+MAC1
                return new Response(transceive(buff.array()));
            }

            //读取脱机交易记录  001A文件
            public Response readTransRecord(int index) {
                final byte[] cmd = { (byte) 0x00, // CLA Class
                        (byte) 0xB2, // INS Instruction
                        (byte) index, // P1 Parameter 1
                        (byte) 0xD4, // P2 Parameter 2
                        (byte) 0x00, // Le
                };
                return new Response(transceive(cmd));
            }

            //读取本地脱机交易记录   0019文件
            /******************************
             index = 03 读取本地复合交易记录，可以查是否有补扣，限额余额等
             ******************************
             Response返回的数据结构：参考卡结构规范
             **********************************/
            public Response readLocalTransRecord(int index) {
                final byte[] cmd = { (byte) 0x00, // CLA Class
                        (byte) 0xB2, // INS Instruction
                        (byte) index, // P1 Parameter 1
                        (byte) 0xCC, // P2 Parameter 2
                        (byte) 0x00, // Le
                };
                return new Response(transceive(cmd));
            }
        }

        //状态码信息
        public static final short SW_NO_ERROR = (short) 0x9000;
        public static final short SW_BYTES_REMAINING_00 = 0x6100;
        public static final short SW_WRONG_LENGTH = 0x6700;
        public static final short SW_SECURITY_STATUS_NOT_SATISFIED = 0x6982;
        public static final short SW_FILE_INVALID = 0x6983;
        public static final short SW_DATA_INVALID = 0x6984;
        public static final short SW_CONDITIONS_NOT_SATISFIED = 0x6985;
        public static final short SW_COMMAND_NOT_ALLOWED = 0x6986;
        public static final short SW_APPLET_SELECT_FAILED = 0x6999;
        public static final short SW_WRONG_DATA = 0x6A80;
        public static final short SW_FUNC_NOT_SUPPORTED = 0x6A81;
        public static final short SW_FILE_NOT_FOUND = 0x6A82;
        public static final short SW_RECORD_NOT_FOUND = 0x6A83;
        public static final short SW_INCORRECT_P1P2 = 0x6A86;
        public static final short SW_WRONG_P1P2 = 0x6B00;
        public static final short SW_CORRECT_LENGTH_00 = 0x6C00;
        public static final short SW_INS_NOT_SUPPORTED = 0x6D00;
        public static final short SW_CLA_NOT_SUPPORTED = 0x6E00;
        public static final short SW_UNKNOWN = 0x6F00;
        public static final short SW_FILE_FULL = 0x6A84;
    }

