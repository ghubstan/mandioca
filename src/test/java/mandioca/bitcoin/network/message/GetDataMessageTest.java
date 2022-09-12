package mandioca.bitcoin.network.message;

import mandioca.bitcoin.MandiocaTest;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static mandioca.bitcoin.network.message.InventoryObjectType.MSG_FILTERED_BLOCK;
import static mandioca.bitcoin.util.HexUtils.HEX;
import static org.junit.Assert.assertEquals;

public class GetDataMessageTest extends MandiocaTest {

    @SuppressWarnings("unused")
    private static final Logger log = LoggerFactory.getLogger(GetDataMessageTest.class);

    @Test
    public void testSerialize() {
        //         def test_serialize(self):
        //            hex_msg = '020300000030eb2540c41025690160a1014c577061596e32e426b712c7ca00000000000000030000001049847939585b0652fba793661c361223446b6fc41089b8be00000000000000'
        //            get_data = GetDataMessage()
        //            block1 = bytes.fromhex('00000000000000cac712b726e4326e596170574c01a16001692510c44025eb30')
        //            get_data.add_data(FILTERED_BLOCK_DATA_TYPE, block1)
        //            block2 = bytes.fromhex('00000000000000beb88910c46f6b442312361c6693a7fb52065b583979844910')
        //            get_data.add_data(FILTERED_BLOCK_DATA_TYPE, block2)
        //            self.assertEqual(get_data.serialize().hex(), hex_msg)
        String getDataHex = "020300000030eb2540c41025690160a1014c577061596e32e426b712c7ca00000000000000030000001049847939585b0652fba793661c361223446b6fc41089b8be00000000000000";
        GetDataMessage getDataMessage = new GetDataMessage();

        byte[] block1 = HEX.decode("00000000000000cac712b726e4326e596170574c01a16001692510c44025eb30");
        getDataMessage.add(MSG_FILTERED_BLOCK, block1);
        byte[] block2 = HEX.decode("00000000000000beb88910c46f6b442312361c6693a7fb52065b583979844910");
        getDataMessage.add(MSG_FILTERED_BLOCK, block2);

        assertEquals(getDataHex, HEX.encode(getDataMessage.serialize()));
    }
}
