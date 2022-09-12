package mandioca.bitcoin.util;

import static java.lang.System.out;
import static mandioca.bitcoin.util.HexUtils.HEX;

/**
 * Transforms openssl ecparam generated octets and prints them to STDOUT.
 */
public class OctetFormatter {

    public static void main(String[] args) {
        out.println("OctetFormatter\n");
        if (args == null) {
            out.println("No octet args");
            return;
        }
        for (String delimitedOctets : args) {
            String octets = HEX.prettyOctets(delimitedOctets);
            out.println("Delimited Octets:  " + delimitedOctets + "\n\t\t\t -> " + octets);
        }
    }

}
