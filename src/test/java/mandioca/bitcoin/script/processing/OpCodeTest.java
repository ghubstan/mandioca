package mandioca.bitcoin.script.processing;

import mandioca.bitcoin.MandiocaTest;
import org.junit.Ignore;
import org.junit.Test;

import static mandioca.bitcoin.function.ByteArrayFunctions.MASK_0xFF;
import static mandioca.bitcoin.script.processing.Op.*;
import static mandioca.bitcoin.script.processing.OpCode.*;
import static org.junit.Assert.*;


public class OpCodeTest extends MandiocaTest {

    @Ignore
    @Test
    public void testDumpLookupMap() {
        Op.dumpOpCodeLookupMap();
    }

    @Test
    public void testIsSupportedOpCode() {
        assertFalse(isSupportedOpCode.test((byte) 0xf1));
        assertTrue(isSupportedOpCode.test((byte) MASK_0xFF));
    }

    @Test
    public void testGetUnsupportedOpCode() {
        exception.expect(RuntimeException.class);
        exception.expectMessage("Script OpCode 0xf1 is not supported");
        getOpCode.apply((byte) 0xf1);
    }

    @Test
    public void testGetDisabledOpCode() {
        exception.expect(RuntimeException.class);
        exception.expectMessage("Script OpCode 0x8d is not supported");
        getOpCode.apply(OP_2MUL.code);

        exception.expect(RuntimeException.class);
        exception.expectMessage("Script OpCode 0x8e is not supported");
        getOpCode.apply(OP_2DIV.code);

    }

    @Test
    public void testGetOpCode() {
        assertEquals(0x82, getOpCodeAsInt(OP_SIZE));
        assertEquals(0x9c, getOpCodeAsInt(OP_NUMEQUAL));
        assertEquals(0xa2, getOpCodeAsInt(OP_GREATERTHANOREQUAL));
        assertEquals(0xa4, getOpCodeAsInt(OP_MAX));
        assertEquals(0xaf, getOpCodeAsInt(OP_CHECKMULTISIGVERIFY));
        assertEquals(0xb2, getOpCodeAsInt(OP_CHECKSEQUENCEVERIFY));
    }

    @Test
    public void testGetOpCodeName() {
        assertEquals(OP_16.name(), getOpCodeName((byte) 0x60));
        assertEquals(OP_ENDIF.name(), getOpCodeName((byte) 0x68));
        assertEquals(OP_FROMALTSTACK.name(), getOpCodeName((byte) 0x6c));
        assertEquals(OP_0NOTEQUAL.name(), getOpCodeName((byte) 0x92));
        assertEquals(OP_BOOLOR.name(), getOpCodeName((byte) 0x9b));
        assertEquals(OP_NOP10.name(), getOpCodeName((byte) 0xb9));
    }

    @Test
    public void testIsOpCode() {
        assertTrue(isOpCode.apply((byte) 0x74, OP_DEPTH));
        assertTrue(isOpCode.apply((byte) 0x75, OP_DROP));
        assertTrue(isOpCode.apply((byte) 0x7b, OP_ROT));
        assertTrue(isOpCode.apply((byte) 0xa5, OP_WITHIN));
        assertTrue(isOpCode.apply((byte) 0xa6, OP_RIPEMD160));
        assertTrue(isOpCode.apply((byte) 0xa7, OP_SHA1));
        assertTrue(isOpCode.apply((byte) 0xaa, OP_HASH256));
        assertTrue(isOpCode.apply((byte) 0xb0, OP_NOP1));
    }

    @Test
    public void testToOpCodeForName() {
        assertEquals(OP_INVALIDOPCODE, toOpCode("OP_INVALIDOPCODE"));
        assertEquals(OP_BOOLAND, toOpCode("OP_BOOLAND"));
        assertEquals(OP_NIP, toOpCode("OP_NIP"));
        assertEquals(OP_TUCK, toOpCode("OP_TUCK"));
    }
}
