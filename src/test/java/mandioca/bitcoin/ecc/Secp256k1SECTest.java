package mandioca.bitcoin.ecc;

import org.junit.Test;

import java.math.BigInteger;

import static mandioca.bitcoin.util.HexUtils.HEX;
import static org.junit.Assert.assertEquals;

public class Secp256k1SECTest extends AbstractSecp256k1Test {

    // Uncompressed SEC Tests

    @Test  // Test params from Jimmy Song book, Chapter 4, Exercise 1
    public void testSecUncompressed1() {
        Secp256k1PrivateKey privateKey = new Secp256k1PrivateKey(BigInteger.valueOf(5000));
        Secp256k1Point publicKey = privateKey.getPublicKey();
        byte[] sec = publicKey.getSec(false);
        String expectedSec = "04" // marker
                + "ffe558e388852f0120e46af2d1b370f85854a8eb0841811ece0e3e03d282d57c"    // x-coordinate
                + "315dc72890a4f10a1481c031b03b351b0dc79901ca18a00cf009dbdb157a1d10";   // y-coordinate
        assertEquals(expectedSec, HEX.encode(sec));
        Secp256k1Point deserializedPublicKey = Secp256k1Point.parse(sec);
        assertEquals(publicKey, deserializedPublicKey);
    }

    @Test  // Test params from Jimmy Song book, Chapter 4, Exercise 1
    public void testSecUncompressed2() {
        BigInteger secret = BigInteger.valueOf(2018).pow(5);
        Secp256k1PrivateKey privateKey = new Secp256k1PrivateKey(secret);
        Secp256k1Point publicKey = privateKey.getPublicKey();
        byte[] sec = publicKey.getSec(false);
        String expectedSec = "04" // marker
                + "027f3da1918455e03c46f659266a1bb5204e959db7364d2f473bdf8f0a13cc9d"    // x-coordinate
                + "ff87647fd023c13b4a4994f17691895806e1b40b57f4fd22581a4f46851f3b06";   // y-coordinate
        assertEquals(expectedSec, HEX.encode(sec));
        Secp256k1Point deserializedPublicKey = Secp256k1Point.parse(sec);
        assertEquals(publicKey, deserializedPublicKey);
    }

    @Test  // Test params from Jimmy Song book, Chapter 4, Exercise 1
    public void testSecUncompressed3() {
        BigInteger secret = HEX.stringToBigInt.apply("0xdeadbeef12345");
        Secp256k1PrivateKey privateKey = new Secp256k1PrivateKey(secret);
        Secp256k1Point publicKey = privateKey.getPublicKey();
        byte[] sec = publicKey.getSec(false);
        String expectedSec = "04" // marker
                + "d90cd625ee87dd38656dd95cf79f65f60f7273b67d3096e68bd81e4f5342691f"    // x-coordinate
                + "842efa762fd59961d0e99803c61edba8b3e3f7dc3a341836f97733aebf987121";   // y-coordinate
        assertEquals(expectedSec, HEX.encode(sec));
        Secp256k1Point deserializedPublicKey = Secp256k1Point.parse(sec);
        assertEquals(publicKey, deserializedPublicKey);
    }

    @Test  // Test params from openssl ecparam command
    public void testSecUncompressed4() {
        BigInteger secret = HEX.stringToBigInt.apply("3374DB0ECD7B1D5FEC5CFD17D216D3DF609EC44F34B7DB74418DD34146AB59D0");
        Secp256k1PrivateKey privateKey = new Secp256k1PrivateKey(secret);
        Secp256k1Point publicKey = privateKey.getPublicKey();
        byte[] sec = publicKey.getSec(false);
        String expectedSec = "04" // marker
                + "3FACD93724530FAB9CE41D1058A8C4BE53AE51012F32B3E39B22C18BFB20571B"    // x-coordinate
                + "FD1227435BF24EB5F4BC1A2181CC4C1906F91DBFC188C8CAE6EE9BEC22AE0C68";   // y-coordinate
        assertEquals(expectedSec, HEX.encode(sec).toUpperCase());
        Secp256k1Point deserializedPublicKey = Secp256k1Point.parse(sec);
        assertEquals(publicKey, deserializedPublicKey);
    }


    @Test  // Test params from openssl ecparam command
    public void testSecUncompressed5() {
        BigInteger secret = HEX.stringToBigInt.apply("DE87A76B2E74528C6B233F407ED92C04AB3147A77DDCDA86EFC01AE481457E87");
        Secp256k1PrivateKey privateKey = new Secp256k1PrivateKey(secret);
        Secp256k1Point publicKey = privateKey.getPublicKey();
        byte[] sec = publicKey.getSec(false);
        String expectedSec = "04" // marker
                + "1960ECABE2B996D9A8C1E688CF60915A712632EEFB41678EFB23B0FB570BE60D"    // x-coordinate
                + "DE78E92F4FBEAF487D9AB8FD0A35D0E717C85B56C2879B545CD39D68B0836AD2";   // y-coordinate
        assertEquals(expectedSec, HEX.encode(sec).toUpperCase());
        Secp256k1Point deserializedPublicKey = Secp256k1Point.parse(sec);
        assertEquals(publicKey, deserializedPublicKey);
    }

    @Test  // Test params from openssl ecparam command
    public void testSecUncompressed6() {
        BigInteger secret = HEX.stringToBigInt.apply("9279659357DE3C1B856429A2F384FA7850003D2D5E617D093CC9CA665565452D");
        Secp256k1PrivateKey privateKey = new Secp256k1PrivateKey(secret);
        Secp256k1Point publicKey = privateKey.getPublicKey();
        byte[] sec = publicKey.getSec(false);
        String expectedSec = "04" // marker
                + "3125AB027D891B0B08856D1B6C1F59BCC0962FA275E591BA205EB58D7F7FEEE5"    // x-coordinate
                + "3DE0AC37FD8EE5C43592AA12A72BC312BF6617BD1743D0D7582A0D92E2625C4F";   // y-coordinate
        assertEquals(expectedSec, HEX.encode(sec).toUpperCase());
        Secp256k1Point deserializedPublicKey = Secp256k1Point.parse(sec);
        assertEquals(publicKey, deserializedPublicKey);
    }


    @Test  // Test params from openssl ecparam command
    public void testSecUncompressed7() {
        BigInteger secret = HEX.stringToBigInt.apply("333E2A5BEE5BE33EEDFC4FFDD981802EC133C58685ADB24C23F66B75B2D923C8");
        Secp256k1PrivateKey privateKey = new Secp256k1PrivateKey(secret);
        Secp256k1Point publicKey = privateKey.getPublicKey();
        byte[] sec = publicKey.getSec(false);
        String expectedSec = "04" // marker
                + "4770C2BA49EEB10ABC98413F7454EA171D4F06FEDB7759752EE6B134BB9581B5"    // x-coordinate
                + "67615D1F55E00075341E0BD148C35BD5CCB992F4D67AABBEFDD28AF44595ED03";   // y-coordinate
        assertEquals(expectedSec, HEX.encode(sec).toUpperCase());
        Secp256k1Point deserializedPublicKey = Secp256k1Point.parse(sec);
        assertEquals(publicKey, deserializedPublicKey);
    }


    @Test  // Test params from openssl ecparam command
    public void testSecUncompressed8() {
        BigInteger secret = HEX.stringToBigInt.apply("4181C4D9F30C71C700DEFD231B4733794286A26BD00B87DFC9E5ED4D5EBF1A81");
        Secp256k1PrivateKey privateKey = new Secp256k1PrivateKey(secret);
        Secp256k1Point publicKey = privateKey.getPublicKey();
        byte[] sec = publicKey.getSec(false);
        String expectedSec = "04" // marker
                + "E5EFB0FF1FD0D1EA2764305153225B8BBEC71169AD6F1D6A277E44AD9D04E2E9"    // x-coordinate
                + "CCB393DD28677FEBA3B5499AFB04D1D1FC6060F004DAE682A4AF267067D5EF28";   // y-coordinate
        assertEquals(expectedSec, HEX.encode(sec).toUpperCase());
        Secp256k1Point deserializedPublicKey = Secp256k1Point.parse(sec);
        assertEquals(publicKey, deserializedPublicKey);
    }


    @Test  // Test params from openssl ecparam command
    public void testSecUncompressed9() {
        BigInteger secret = HEX.stringToBigInt.apply("3B820C5779BF0792ED4E21301C70D069865F4378F970C5279C1CDA5703F6FA16");
        Secp256k1PrivateKey privateKey = new Secp256k1PrivateKey(secret);
        Secp256k1Point publicKey = privateKey.getPublicKey();
        byte[] sec = publicKey.getSec(false);
        String expectedSec = "04" // marker
                + "FE75758BAD3B9A29DFF5202204D3D55CFD61CA1CBD8C804BBC4A6A8B435539D2"    // x-coordinate
                + "E27A9BA8798ED058F6E6D95A0B858B005EB3E14652321BD3C86690F81AD61EB4";   // y-coordinate
        assertEquals(expectedSec, HEX.encode(sec).toUpperCase());
        Secp256k1Point deserializedPublicKey = Secp256k1Point.parse(sec);
        assertEquals(publicKey, deserializedPublicKey);
    }

    @Test  // Test params from openssl ecparam command
    public void testSecUncompressed10() {
        BigInteger secret = HEX.stringToBigInt.apply("6D7206A99D4375A0DCC8B2F20D73AE2DC9DC46E5013A56DF2942D5AA5C3EE979");
        Secp256k1PrivateKey privateKey = new Secp256k1PrivateKey(secret);
        Secp256k1Point publicKey = privateKey.getPublicKey();
        byte[] sec = publicKey.getSec(false);
        String expectedSec = "04" // marker
                + "0BB102FC5F3A5601B9CAE8B984BC8579C13CC1DEDB0FF62BAAB2DD46EB4D7838"    // x-coordinate
                + "FE8649E9061B7E65C4E393ADD6F5C6B1A33E0862C7F57FB02F693AEB746081F7";   // y-coordinate
        assertEquals(expectedSec, HEX.encode(sec).toUpperCase());
        Secp256k1Point deserializedPublicKey = Secp256k1Point.parse(sec);
        assertEquals(publicKey, deserializedPublicKey);
    }

    // Compressed SEC Tests

    @Test  // Test params from Jimmy Song book, Chapter 4, Exercise 2
    public void testSecCompressed1() {
        Secp256k1PrivateKey privateKey = new Secp256k1PrivateKey(BigInteger.valueOf(5001));
        Secp256k1Point publicKey = privateKey.getPublicKey();
        byte[] sec = publicKey.getSec(true);
        String expectedSec = "03" // marker for y is odd
                + "57a4f368868a8a6d572991e484e664810ff14c05c0fa023275251151fe0e53d1";    // x-coordinate
        assertEquals(expectedSec, HEX.encode(sec));
        Secp256k1Point deserializedPublicKey = Secp256k1Point.parse(sec);
        assertEquals(publicKey, deserializedPublicKey);
    }

    @Test  // Test params from Jimmy Song book, Chapter 4, Exercise 2
    public void testSecCompressed2() {
        BigInteger secret = BigInteger.valueOf(2019).pow(5);
        Secp256k1PrivateKey privateKey = new Secp256k1PrivateKey(secret);
        Secp256k1Point publicKey = privateKey.getPublicKey();
        byte[] sec = publicKey.getSec(true);
        String expectedSec = "02" // marker for y is even
                + "933ec2d2b111b92737ec12f1c5d20f3233a0ad21cd8b36d0bca7a0cfa5cb8701";    // x-coordinate
        assertEquals(expectedSec, HEX.encode(sec));
        Secp256k1Point deserializedPublicKey = Secp256k1Point.parse(sec);
        assertEquals(publicKey, deserializedPublicKey);
    }

    @Test  // Test params from Jimmy Song book, Chapter 4, Exercise 2
    public void testSecCompressed3() {
        BigInteger secret = HEX.stringToBigInt.apply("0xdeadbeef54321");
        Secp256k1PrivateKey privateKey = new Secp256k1PrivateKey(secret);
        Secp256k1Point publicKey = privateKey.getPublicKey();
        byte[] sec = publicKey.getSec(true);
        String expectedSec = "02" // marker for y is even
                + "96be5b1292f6c856b3c5654e886fc13511462059089cdf9c479623bfcbe77690";    // x-coordinate
        assertEquals(expectedSec, HEX.encode(sec));
        Secp256k1Point deserializedPublicKey = Secp256k1Point.parse(sec);
        assertEquals(publicKey, deserializedPublicKey);
    }

    @Test  // Test params from openssl ecparam command
    public void testSecCompressed4() {
        BigInteger secret = HEX.stringToBigInt.apply("3374DB0ECD7B1D5FEC5CFD17D216D3DF609EC44F34B7DB74418DD34146AB59D0");
        Secp256k1PrivateKey privateKey = new Secp256k1PrivateKey(secret);
        Secp256k1Point publicKey = privateKey.getPublicKey();
        byte[] sec = publicKey.getSec(true);
        String expectedSec = "02" // marker for y is even
                + "3FACD93724530FAB9CE41D1058A8C4BE53AE51012F32B3E39B22C18BFB20571B";   // x-coordinate
        assertEquals(expectedSec, HEX.encode(sec).toUpperCase());
        Secp256k1Point deserializedPublicKey = Secp256k1Point.parse(sec);
        assertEquals(publicKey, deserializedPublicKey);
    }


    @Test  // Test params from openssl ecparam command
    public void testSecCompressed5() {
        BigInteger secret = HEX.stringToBigInt.apply("DE87A76B2E74528C6B233F407ED92C04AB3147A77DDCDA86EFC01AE481457E87");
        Secp256k1PrivateKey privateKey = new Secp256k1PrivateKey(secret);
        Secp256k1Point publicKey = privateKey.getPublicKey();
        byte[] sec = publicKey.getSec(true);
        String expectedSec = "02" // marker for y is even
                + "1960ECABE2B996D9A8C1E688CF60915A712632EEFB41678EFB23B0FB570BE60D";    // x-coordinate
        assertEquals(expectedSec, HEX.encode(sec).toUpperCase());
        Secp256k1Point deserializedPublicKey = Secp256k1Point.parse(sec);
        assertEquals(publicKey, deserializedPublicKey);
    }

    @Test  // Test params from openssl ecparam command
    public void testSecCompressed6() {
        BigInteger secret = HEX.stringToBigInt.apply("9279659357DE3C1B856429A2F384FA7850003D2D5E617D093CC9CA665565452D");
        Secp256k1PrivateKey privateKey = new Secp256k1PrivateKey(secret);
        Secp256k1Point publicKey = privateKey.getPublicKey();
        byte[] sec = publicKey.getSec(true);
        String expectedSec = "03" // marker for y is odd
                + "3125AB027D891B0B08856D1B6C1F59BCC0962FA275E591BA205EB58D7F7FEEE5";   // x-coordinate
        assertEquals(expectedSec, HEX.encode(sec).toUpperCase());
        Secp256k1Point deserializedPublicKey = Secp256k1Point.parse(sec);
        assertEquals(publicKey, deserializedPublicKey);
    }


    @Test  // Test params from openssl ecparam command
    public void testSecCompressed7() {
        BigInteger secret = HEX.stringToBigInt.apply("333E2A5BEE5BE33EEDFC4FFDD981802EC133C58685ADB24C23F66B75B2D923C8");
        Secp256k1PrivateKey privateKey = new Secp256k1PrivateKey(secret);
        Secp256k1Point publicKey = privateKey.getPublicKey();
        byte[] sec = publicKey.getSec(true);
        String expectedSec = "03" // marker for y is odd
                + "4770C2BA49EEB10ABC98413F7454EA171D4F06FEDB7759752EE6B134BB9581B5";   // x-coordinate
        assertEquals(expectedSec, HEX.encode(sec).toUpperCase());
        Secp256k1Point deserializedPublicKey = Secp256k1Point.parse(sec);
        assertEquals(publicKey, deserializedPublicKey);
    }


    @Test  // Test params from openssl ecparam command
    public void testSecCompressed8() {
        BigInteger secret = HEX.stringToBigInt.apply("4181C4D9F30C71C700DEFD231B4733794286A26BD00B87DFC9E5ED4D5EBF1A81");
        Secp256k1PrivateKey privateKey = new Secp256k1PrivateKey(secret);
        Secp256k1Point publicKey = privateKey.getPublicKey();
        byte[] sec = publicKey.getSec(true);
        String expectedSec = "02" // marker for y is even
                + "E5EFB0FF1FD0D1EA2764305153225B8BBEC71169AD6F1D6A277E44AD9D04E2E9";    // x-coordinate
        assertEquals(expectedSec, HEX.encode(sec).toUpperCase());
        Secp256k1Point deserializedPublicKey = Secp256k1Point.parse(sec);
        assertEquals(publicKey, deserializedPublicKey);
    }


    @Test  // Test params from openssl ecparam command
    public void testSecCompressed9() {
        BigInteger secret = HEX.stringToBigInt.apply("3B820C5779BF0792ED4E21301C70D069865F4378F970C5279C1CDA5703F6FA16");
        Secp256k1PrivateKey privateKey = new Secp256k1PrivateKey(secret);
        Secp256k1Point publicKey = privateKey.getPublicKey();
        byte[] sec = publicKey.getSec(true);
        String expectedSec = "02" // marker for y is even
                + "FE75758BAD3B9A29DFF5202204D3D55CFD61CA1CBD8C804BBC4A6A8B435539D2";    // x-coordinate
        assertEquals(expectedSec, HEX.encode(sec).toUpperCase());
        Secp256k1Point deserializedPublicKey = Secp256k1Point.parse(sec);
        assertEquals(publicKey, deserializedPublicKey);
    }

    @Test  // Test params from openssl ecparam command
    public void testSecCompressed10() {
        BigInteger secret = HEX.stringToBigInt.apply("6D7206A99D4375A0DCC8B2F20D73AE2DC9DC46E5013A56DF2942D5AA5C3EE979");
        Secp256k1PrivateKey privateKey = new Secp256k1PrivateKey(secret);
        Secp256k1Point publicKey = privateKey.getPublicKey();
        byte[] sec = publicKey.getSec(true);
        String expectedSec = "03" // marker for y is odd
                + "0BB102FC5F3A5601B9CAE8B984BC8579C13CC1DEDB0FF62BAAB2DD46EB4D7838";   // x-coordinate
        assertEquals(expectedSec, HEX.encode(sec).toUpperCase());
        Secp256k1Point deserializedPublicKey = Secp256k1Point.parse(sec);
        assertEquals(publicKey, deserializedPublicKey);
    }


/*
https://crypto.stackexchange.com/questions/20838/request-for-data-to-test-deterministic-ecdsa-signature-algorithm-for-secp256k1

Here are 20 test vectors for SECP256k1 (RFC6979): (Source).

The format is (private key, message, DER signature).

Its not in the format you needed but the final signature should validate all the other values.

(1,Absence makes the heart grow fonder.,3045022100AFFF580595971B8C1700E77069D73602AEF4C2A760DBD697881423DFFF845DE80220579ADB6A1AC03ACDE461B5821A049EBD39A8A8EBF2506B841B15C27342D2E342)
(2,Actions speak louder than words.,304502210085F28BBC90975B1907A51CBFE7BF0DC1AC74ADE49318EE97498DBBDE3894A31C0220241D24DA8D263E7AF7FF49BCA6A7A850F0E087FAF6FEF44F85851B0283C3F026)
(3,All for one and one for all.,30440220502C6AC38E1C68CE68F044F5AB680F2880A6C1CD34E70F2B4F945C6FD30ABD03022018EF5C6C3392B9D67AD5109C85476A0E159425D7F6ACE2CEBEAA65F02F210BBB)
(4,All's fair in love and war.,30440220452D4AB234891CF6E5432CD5472BDCA1CFC6FB28563333885F068DA02EE216D8022056C368D16A64D29CFF92F17203D926E113064527AF0480D3BCC1D3FADFDE9364)
(5,All work and no play makes Jack a dull boy.,3045022100995025B4880EEB1ECEDBA945FE8C9B2DDF2B07DBC293C2586C079D7B663EF38A022022FB54AB95014616D014277E05C97A7ED9E22596A0420BBD2D749CA9A2F876FE)
(6,All's well that ends well.,3045022100A9C1593FA6459777B2EBA6D7E2A206E3BB119E85B2163973CF28FFAF24EC381C02202F166F13230B3853B928EFB649D30375EC6A4B1A64A8D56FBCC0A9D86A0943E9)
(7,An apple a day keeps the doctor away.,304402202FC9C8B749621241C33FD51B57FC5140C1D7FC1594F91B073953E79DA2F5E8F60220345E4EA7693B5069C0251771EA476CBE236586ED24B90AEEEA7B7C2814EDF477)
(8,An apple never falls far from the tree.,3044022052B6E2C49A6F6ADBE52FB6BBE744CAA3F49364085DB118EAB8670BC766BE160302207D96A42866637CA3D4CAF36E597A460EB305ADAC0220B027410C821A7191A1C4)
(9,An ounce of prevention is worth a pound of cure.,3045022100BE53E7C00788E4417083D7511800F18C7C6F5F259DE39BC6F8B1BEBCD5056BD002201F389E13CFE7D1DBD8D2D1BFF18138219F57DE166673762009686A28FBC44DF6)
(10,Appearances can be deceiving.,304402202F2413A1673F642C30EA2E23FCAE45776BC77A94F96920AEA3C14303B1469428022053AC3E8EA0A488E9159D56E429A51F207BF04E462F8D4BA2C69B1B1635F30217)
(34356466678672179216206944866734405838331831190171667647615530531663699592602,Absence makes the heart grow fonder.,3045022100996D79FBA54B24E9394FC5FAB6BF94D173F3752645075DE6E32574FE08625F770220345E638B373DCB0CE0C09E5799695EF64FFC5E01DD8367B9A205CE25F28870F6)
(99398763056634537812744552006896172984671876672520535998211840060697129507206,Actions speak louder than words.,304502210088164430985A4437471417C2386FAA536E1FE8EC91BD0F1F642BC22A776891530220090DC83D6E3B54A1A54DC2E79C693144179A512D9C9E686A6C25E7641A2101A8)
(3759719655879806965811134282268177329967523491661175987246621825209053686213,All for one and one for all.,30450221009F1073C9C09B664498D4B216983330B01C29A0FB55DD61AA145B4EBD0579905502204592FB6626F672D4F3AD4BB2D0A1ED6C2A161CC35C6BB77E6F0FD3B63FEAB36F)
(103660229287485550546857170818258546832194359524010586713457827121778385264241,All's fair in love and war.,304502210080EABF24117B492635043886E7229B9705B970CBB6828C4E03A39DAE7AC34BDA022070E8A32CA1DF82ADD53FACBD58B4F2D3984D0A17B6B13C44460238D9FF74E41F)
(104702657257102633579772822622124422673143939576486771274630765314225900831707,All work and no play makes Jack a dull boy.,3045022100A43FF5EDEA7EA0B9716D4359574E990A6859CDAEB9D7D6B4964AFD40BE11BD35022067F9D82E22FC447A122997335525F117F37B141C3EFA9F8C6D77B586753F962F)
(46744469262201639974910661553202053327388301297897803474665777634455660653814,All's well that ends well.,3044022053CE16251F4FAE7EB87E2AB040A6F334E08687FB445566256CD217ECE389E0440220576506A168CBC9EE0DD485D6C418961E7A0861B0F05D22A93401812978D0B215)
(91461772442478604154082755547318472082410323943823420797096392355159818037369,An apple a day keeps the doctor away.,3045022100DF8744CC06A304B041E88149ACFD84A68D8F4A2A4047056644E1EC8357E11EBE02204BA2D5499A26D072C797A86C7851533F287CEB8B818CAE2C5D4483C37C62750C)
(86354370597268376573642079301756246922349732255591245149271869674095200273050,An apple never falls far from the tree.,3045022100878372D211ED0DBDE1273AE3DD85AEC577C08A06A55960F2E274F97CC9F2F38F02203F992CAA66F472A64F6CCDD8076C0A12202C674155A6A61B8CD23C1DED08AAB7)
(19584093032798730129230525910686445865718710074652466673872143043325364812985,An ounce of prevention is worth a pound of cure.,3045022100D5CB4E148C0A29CE37F1542BE416E8EF575DA522666B19B541960D726C99662B022045C951C1CA938C90DAD6C3EEDE7C5DF67FCF0D14F90FAF201E8D215F215C5C18)
(781437121688497986836158713061237152541328908182646473971063062031575438443,Appearances can be deceiving.,304402203E2F0118062306E2239C873828A7275DD35545A143797E224148C5BBBD59DD08022073A8C9E17BE75C66362913B5E05D81FD619B434EDDA766FAE6C352E86987809D)
*/
}
