package mandioca.bitcoin.script;

import mandioca.bitcoin.MandiocaTest;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertArrayEquals;

public class CombineScriptsTest extends MandiocaTest {

    private static final Logger log = LoggerFactory.getLogger(CombineScriptsTest.class);

    @Test
    public void testCombineScripts() {
        byte[][] script1Commands = createDummyCommandSet(10, 4, B0x00[0], B0x01[0], B0x02[0], B0x03[0]);
        byte[][] script2Commands = createDummyCommandSet(12, 4, B0x04[0], B0x05[0], B0x06[0], B0x07[0]);
        Script script1 = new Script(script1Commands);
        Script script2 = new Script(script2Commands);
        Script combinedScript = script1.add(script2);
        assertEquals(22, combinedScript.getCmds().length);
        assertArrayEquals(new byte[]{B0x00[0], B0x01[0], B0x02[0], B0x03[0]}, combinedScript.getCmds()[0]);
        assertArrayEquals(new byte[]{B0x00[0], B0x01[0], B0x02[0], B0x03[0]}, combinedScript.getCmds()[9]);
        assertArrayEquals(new byte[]{B0x04[0], B0x05[0], B0x06[0], B0x07[0]}, combinedScript.getCmds()[10]);
        assertArrayEquals(new byte[]{B0x04[0], B0x05[0], B0x06[0], B0x07[0]}, combinedScript.getCmds()[21]);
    }

    private byte[][] createDummyCommandSet(int numCommands, @SuppressWarnings("SameParameterValue") int commandLength, byte... bytes) {
        byte[][] cmds = new byte[numCommands][commandLength];
        for (int i = 0; i < cmds.length; i++) {
            System.arraycopy(bytes, 0, cmds[i], 0, bytes.length);
        }
        return cmds;
    }

}
