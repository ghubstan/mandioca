package mandioca.bitcoin.ecc;

import org.junit.Test;

import java.math.BigInteger;

import static java.math.BigInteger.*;
import static mandioca.bitcoin.util.HashUtils.getSHA256HashAsInteger;
import static mandioca.bitcoin.util.HexUtils.HEX;
import static org.junit.Assert.assertEquals;

public class Secp256k1DERTest extends AbstractSecp256k1Test {

    // Signature DER Encoding / Decoding Tests

    @Test  // Test params from Jimmy Song book, Chapter 4, Exercise 3
    public void testSignatureDer0() {
        Signature signature = new Signature(
                new BigInteger("37206a0610995c58074999cb9767b87af4c4978db68c06e8e6e81d282047a7c6", 16),
                new BigInteger("8ca63759c1157ebeaec0d03cecca119fc9a75bf8e6d0fa65c841c8e2738cdaec", 16)
        );
        byte[] der = signature.getDer();
        String expectedDer = "3045022037206a0610995c58074999cb9767b87af4c4978db68c06e8e6e81d282047a7c60221008ca63759c1157ebeaec0d03cecca119fc9a75bf8e6d0fa65c841c8e2738cdaec";
        assertEquals(expectedDer, HEX.encode(der));
        Signature parsedSignature = Signature.parse(der);
        assertEquals(signature, parsedSignature);
    }

    @Test
    // Test params from https://crypto.stackexchange.com/questions/20838/request-for-data-to-test-deterministic-ecdsa-signature-algorithm-for-secp256k1
    public void testSignatureDer1() {
        Secp256k1PrivateKey privateKey = new Secp256k1PrivateKey(ONE);
        // BigInteger z = getDoubleSHA256Hash("Absence makes the heart grow fonder."); // signature dbl-hash (for bitcoin)
        BigInteger z = getSHA256HashAsInteger("Absence makes the heart grow fonder."); // signature single-hash (for test)
        Signature signature = privateKey.sign(z);
        byte[] der = signature.getDer();
        String expectedDer = "3045022100AFFF580595971B8C1700E77069D73602AEF4C2A760DBD697881423DFFF845DE80220579ADB6A1AC03ACDE461B5821A049EBD39A8A8EBF2506B841B15C27342D2E342";
        assertEquals(expectedDer, HEX.encode(der).toUpperCase());
        Signature parsedSignature = Signature.parse(der);
        assertEquals(signature, parsedSignature);
    }

    @Test
    // Test params from https://crypto.stackexchange.com/questions/20838/request-for-data-to-test-deterministic-ecdsa-signature-algorithm-for-secp256k1
    public void testSignatureDer2() {
        Secp256k1PrivateKey privateKey = new Secp256k1PrivateKey(TWO);
        // BigInteger z = getDoubleSHA256Hash("Actions speak louder than words."); // signature dbl-hash (for bitcoin)
        BigInteger z = getSHA256HashAsInteger("Actions speak louder than words."); // signature single-hash (for test)
        Signature signature = privateKey.sign(z);
        byte[] der = signature.getDer();
        String expectedDer = "304502210085F28BBC90975B1907A51CBFE7BF0DC1AC74ADE49318EE97498DBBDE3894A31C0220241D24DA8D263E7AF7FF49BCA6A7A850F0E087FAF6FEF44F85851B0283C3F026";
        assertEquals(expectedDer, HEX.encode(der).toUpperCase());
        Signature parsedSignature = Signature.parse(der);
        assertEquals(signature, parsedSignature);
    }


    @Test
    // Test params from https://crypto.stackexchange.com/questions/20838/request-for-data-to-test-deterministic-ecdsa-signature-algorithm-for-secp256k1
    public void testSignatureDer3() {
        Secp256k1PrivateKey privateKey = new Secp256k1PrivateKey(BigInteger.valueOf(3));
        // BigInteger z = getDoubleSHA256Hash("Actions speak louder than words."); // signature dbl-hash (for bitcoin)
        BigInteger z = getSHA256HashAsInteger("All for one and one for all."); // signature single-hash (for test)
        Signature signature = privateKey.sign(z);
        byte[] der = signature.getDer();
        String expectedDer = "30440220502C6AC38E1C68CE68F044F5AB680F2880A6C1CD34E70F2B4F945C6FD30ABD03022018EF5C6C3392B9D67AD5109C85476A0E159425D7F6ACE2CEBEAA65F02F210BBB";
        assertEquals(expectedDer, HEX.encode(der).toUpperCase());
        Signature parsedSignature = Signature.parse(der);
        assertEquals(signature, parsedSignature);
    }


    @Test
    // Test params from https://crypto.stackexchange.com/questions/20838/request-for-data-to-test-deterministic-ecdsa-signature-algorithm-for-secp256k1
    public void testSignatureDer4() {
        Secp256k1PrivateKey privateKey = new Secp256k1PrivateKey(BigInteger.valueOf(4));
        // BigInteger z = getDoubleSHA256Hash("All's fair in love and war."); // signature dbl-hash (for bitcoin)
        BigInteger z = getSHA256HashAsInteger("All's fair in love and war."); // signature single-hash (for test)
        Signature signature = privateKey.sign(z);
        byte[] der = signature.getDer();
        String expectedDer = "30440220452D4AB234891CF6E5432CD5472BDCA1CFC6FB28563333885F068DA02EE216D8022056C368D16A64D29CFF92F17203D926E113064527AF0480D3BCC1D3FADFDE9364";
        assertEquals(expectedDer, HEX.encode(der).toUpperCase());
        Signature parsedSignature = Signature.parse(der);
        assertEquals(signature, parsedSignature);
    }


    @Test
    // Test params from https://crypto.stackexchange.com/questions/20838/request-for-data-to-test-deterministic-ecdsa-signature-algorithm-for-secp256k1
    public void testSignatureDer5() {
        Secp256k1PrivateKey privateKey = new Secp256k1PrivateKey(BigInteger.valueOf(5));
        BigInteger z = getSHA256HashAsInteger("All work and no play makes Jack a dull boy."); // signature single-hash (for test)
        Signature signature = privateKey.sign(z);
        byte[] der = signature.getDer();
        String expectedDer = "3045022100995025B4880EEB1ECEDBA945FE8C9B2DDF2B07DBC293C2586C079D7B663EF38A022022FB54AB95014616D014277E05C97A7ED9E22596A0420BBD2D749CA9A2F876FE";
        assertEquals(expectedDer, HEX.encode(der).toUpperCase());
        Signature parsedSignature = Signature.parse(der);
        assertEquals(signature, parsedSignature);
    }


    @Test
    // Test params from https://crypto.stackexchange.com/questions/20838/request-for-data-to-test-deterministic-ecdsa-signature-algorithm-for-secp256k1
    public void testSignatureDer6() {
        Secp256k1PrivateKey privateKey = new Secp256k1PrivateKey(BigInteger.valueOf(6));
        BigInteger z = getSHA256HashAsInteger("All's well that ends well."); // signature single-hash (for test)
        Signature signature = privateKey.sign(z);
        byte[] der = signature.getDer();
        String expectedDer = "3045022100A9C1593FA6459777B2EBA6D7E2A206E3BB119E85B2163973CF28FFAF24EC381C02202F166F13230B3853B928EFB649D30375EC6A4B1A64A8D56FBCC0A9D86A0943E9";
        assertEquals(expectedDer, HEX.encode(der).toUpperCase());
        Signature parsedSignature = Signature.parse(der);
        assertEquals(signature, parsedSignature);
    }


    @Test
    // Test params from https://crypto.stackexchange.com/questions/20838/request-for-data-to-test-deterministic-ecdsa-signature-algorithm-for-secp256k1
    public void testSignatureDer7() {
        Secp256k1PrivateKey privateKey = new Secp256k1PrivateKey(BigInteger.valueOf(7));
        BigInteger z = getSHA256HashAsInteger("An apple a day keeps the doctor away."); // signature single-hash (for test)
        Signature signature = privateKey.sign(z);
        byte[] der = signature.getDer();
        String expectedDer = "304402202FC9C8B749621241C33FD51B57FC5140C1D7FC1594F91B073953E79DA2F5E8F60220345E4EA7693B5069C0251771EA476CBE236586ED24B90AEEEA7B7C2814EDF477";
        assertEquals(expectedDer, HEX.encode(der).toUpperCase());
        Signature parsedSignature = Signature.parse(der);
        assertEquals(signature, parsedSignature);
    }

    @Test
    // Test params from https://crypto.stackexchange.com/questions/20838/request-for-data-to-test-deterministic-ecdsa-signature-algorithm-for-secp256k1
    public void testSignatureDer8() {
        Secp256k1PrivateKey privateKey = new Secp256k1PrivateKey(BigInteger.valueOf(8));
        BigInteger z = getSHA256HashAsInteger("An apple never falls far from the tree."); // signature single-hash (for test)
        Signature signature = privateKey.sign(z);
        byte[] der = signature.getDer();
        String expectedDer = "3044022052B6E2C49A6F6ADBE52FB6BBE744CAA3F49364085DB118EAB8670BC766BE160302207D96A42866637CA3D4CAF36E597A460EB305ADAC0220B027410C821A7191A1C4";
        assertEquals(expectedDer, HEX.encode(der).toUpperCase());
        Signature parsedSignature = Signature.parse(der);
        assertEquals(signature, parsedSignature);
    }

    @Test
    // Test params from https://crypto.stackexchange.com/questions/20838/request-for-data-to-test-deterministic-ecdsa-signature-algorithm-for-secp256k1
    public void testSignatureDer9() {
        Secp256k1PrivateKey privateKey = new Secp256k1PrivateKey(BigInteger.valueOf(9));
        BigInteger z = getSHA256HashAsInteger("An ounce of prevention is worth a pound of cure."); // signature single-hash (for test)
        Signature signature = privateKey.sign(z);
        byte[] der = signature.getDer();
        String expectedDer = "3045022100BE53E7C00788E4417083D7511800F18C7C6F5F259DE39BC6F8B1BEBCD5056BD002201F389E13CFE7D1DBD8D2D1BFF18138219F57DE166673762009686A28FBC44DF6";
        assertEquals(expectedDer, HEX.encode(der).toUpperCase());
        Signature parsedSignature = Signature.parse(der);
        assertEquals(signature, parsedSignature);
    }

    @Test
    // Test params from https://crypto.stackexchange.com/questions/20838/request-for-data-to-test-deterministic-ecdsa-signature-algorithm-for-secp256k1
    public void testSignatureDer10() {
        Secp256k1PrivateKey privateKey = new Secp256k1PrivateKey(TEN);
        BigInteger z = getSHA256HashAsInteger("Appearances can be deceiving."); // signature single-hash (for test)
        Signature signature = privateKey.sign(z);
        byte[] der = signature.getDer();
        String expectedDer = "304402202F2413A1673F642C30EA2E23FCAE45776BC77A94F96920AEA3C14303B1469428022053AC3E8EA0A488E9159D56E429A51F207BF04E462F8D4BA2C69B1B1635F30217";
        assertEquals(expectedDer, HEX.encode(der).toUpperCase());
        Signature parsedSignature = Signature.parse(der);
        assertEquals(signature, parsedSignature);
    }

    @Test
    // Test params from https://crypto.stackexchange.com/questions/20838/request-for-data-to-test-deterministic-ecdsa-signature-algorithm-for-secp256k1
    public void testSignatureDer11() {
        Secp256k1PrivateKey privateKey = new Secp256k1PrivateKey(new BigInteger("34356466678672179216206944866734405838331831190171667647615530531663699592602"));
        BigInteger z = getSHA256HashAsInteger("Absence makes the heart grow fonder."); // signature single-hash (for test)
        Signature signature = privateKey.sign(z);
        byte[] der = signature.getDer();
        String expectedDer = "3045022100996D79FBA54B24E9394FC5FAB6BF94D173F3752645075DE6E32574FE08625F770220345E638B373DCB0CE0C09E5799695EF64FFC5E01DD8367B9A205CE25F28870F6";
        assertEquals(expectedDer, HEX.encode(der).toUpperCase());
        Signature parsedSignature = Signature.parse(der);
        assertEquals(signature, parsedSignature);
    }

    @Test
    // Test params from https://crypto.stackexchange.com/questions/20838/request-for-data-to-test-deterministic-ecdsa-signature-algorithm-for-secp256k1
    public void testSignatureDer12() {
        Secp256k1PrivateKey privateKey = new Secp256k1PrivateKey(new BigInteger("99398763056634537812744552006896172984671876672520535998211840060697129507206"));
        BigInteger z = getSHA256HashAsInteger("Actions speak louder than words."); // signature single-hash (for test)
        Signature signature = privateKey.sign(z);
        byte[] der = signature.getDer();
        String expectedDer = "304502210088164430985A4437471417C2386FAA536E1FE8EC91BD0F1F642BC22A776891530220090DC83D6E3B54A1A54DC2E79C693144179A512D9C9E686A6C25E7641A2101A8";
        assertEquals(expectedDer, HEX.encode(der).toUpperCase());
        Signature parsedSignature = Signature.parse(der);
        assertEquals(signature, parsedSignature);
    }

    @Test
    // Test params from https://crypto.stackexchange.com/questions/20838/request-for-data-to-test-deterministic-ecdsa-signature-algorithm-for-secp256k1
    public void testSignatureDer13() {
        Secp256k1PrivateKey privateKey = new Secp256k1PrivateKey(new BigInteger("3759719655879806965811134282268177329967523491661175987246621825209053686213"));
        BigInteger z = getSHA256HashAsInteger("All for one and one for all."); // signature single-hash (for test)
        Signature signature = privateKey.sign(z);
        byte[] der = signature.getDer();
        String expectedDer = "30450221009F1073C9C09B664498D4B216983330B01C29A0FB55DD61AA145B4EBD0579905502204592FB6626F672D4F3AD4BB2D0A1ED6C2A161CC35C6BB77E6F0FD3B63FEAB36F";
        assertEquals(expectedDer, HEX.encode(der).toUpperCase());
        Signature parsedSignature = Signature.parse(der);
        assertEquals(signature, parsedSignature);
    }

    @Test
    // Test params from https://crypto.stackexchange.com/questions/20838/request-for-data-to-test-deterministic-ecdsa-signature-algorithm-for-secp256k1
    public void testSignatureDer14() {
        Secp256k1PrivateKey privateKey = new Secp256k1PrivateKey(new BigInteger("103660229287485550546857170818258546832194359524010586713457827121778385264241"));
        BigInteger z = getSHA256HashAsInteger("All's fair in love and war."); // signature single-hash (for test)
        Signature signature = privateKey.sign(z);
        byte[] der = signature.getDer();
        String expectedDer = "304502210080EABF24117B492635043886E7229B9705B970CBB6828C4E03A39DAE7AC34BDA022070E8A32CA1DF82ADD53FACBD58B4F2D3984D0A17B6B13C44460238D9FF74E41F";
        assertEquals(expectedDer, HEX.encode(der).toUpperCase());
        Signature parsedSignature = Signature.parse(der);
        assertEquals(signature, parsedSignature);
    }

    @Test
    // Test params from https://crypto.stackexchange.com/questions/20838/request-for-data-to-test-deterministic-ecdsa-signature-algorithm-for-secp256k1
    public void testSignatureDer15() {
        Secp256k1PrivateKey privateKey = new Secp256k1PrivateKey(new BigInteger("104702657257102633579772822622124422673143939576486771274630765314225900831707"));
        BigInteger z = getSHA256HashAsInteger("All work and no play makes Jack a dull boy."); // signature single-hash (for test)
        Signature signature = privateKey.sign(z);
        byte[] der = signature.getDer();
        String expectedDer = "3045022100A43FF5EDEA7EA0B9716D4359574E990A6859CDAEB9D7D6B4964AFD40BE11BD35022067F9D82E22FC447A122997335525F117F37B141C3EFA9F8C6D77B586753F962F";
        assertEquals(expectedDer, HEX.encode(der).toUpperCase());
        Signature parsedSignature = Signature.parse(der);
        assertEquals(signature, parsedSignature);
    }

    @Test
    // Test params from https://crypto.stackexchange.com/questions/20838/request-for-data-to-test-deterministic-ecdsa-signature-algorithm-for-secp256k1
    public void testSignatureDer16() {
        Secp256k1PrivateKey privateKey = new Secp256k1PrivateKey(new BigInteger("46744469262201639974910661553202053327388301297897803474665777634455660653814"));
        BigInteger z = getSHA256HashAsInteger("All's well that ends well."); // signature single-hash (for test)
        Signature signature = privateKey.sign(z);
        byte[] der = signature.getDer();
        String expectedDer = "3044022053CE16251F4FAE7EB87E2AB040A6F334E08687FB445566256CD217ECE389E0440220576506A168CBC9EE0DD485D6C418961E7A0861B0F05D22A93401812978D0B215";
        assertEquals(expectedDer, HEX.encode(der).toUpperCase());
        Signature parsedSignature = Signature.parse(der);
        assertEquals(signature, parsedSignature);
    }

    @Test
    // Test params from https://crypto.stackexchange.com/questions/20838/request-for-data-to-test-deterministic-ecdsa-signature-algorithm-for-secp256k1
    public void testSignatureDer17() {
        Secp256k1PrivateKey privateKey = new Secp256k1PrivateKey(new BigInteger("91461772442478604154082755547318472082410323943823420797096392355159818037369"));
        BigInteger z = getSHA256HashAsInteger("An apple a day keeps the doctor away."); // signature single-hash (for test)
        Signature signature = privateKey.sign(z);
        byte[] der = signature.getDer();
        String expectedDer = "3045022100DF8744CC06A304B041E88149ACFD84A68D8F4A2A4047056644E1EC8357E11EBE02204BA2D5499A26D072C797A86C7851533F287CEB8B818CAE2C5D4483C37C62750C";
        assertEquals(expectedDer, HEX.encode(der).toUpperCase());
        Signature parsedSignature = Signature.parse(der);
        assertEquals(signature, parsedSignature);
    }

    @Test
    // Test params from https://crypto.stackexchange.com/questions/20838/request-for-data-to-test-deterministic-ecdsa-signature-algorithm-for-secp256k1
    public void testSignatureDer18() {
        Secp256k1PrivateKey privateKey = new Secp256k1PrivateKey(new BigInteger("86354370597268376573642079301756246922349732255591245149271869674095200273050"));
        BigInteger z = getSHA256HashAsInteger("An apple never falls far from the tree."); // signature single-hash (for test)
        Signature signature = privateKey.sign(z);
        byte[] der = signature.getDer();
        String expectedDer = "3045022100878372D211ED0DBDE1273AE3DD85AEC577C08A06A55960F2E274F97CC9F2F38F02203F992CAA66F472A64F6CCDD8076C0A12202C674155A6A61B8CD23C1DED08AAB7";
        assertEquals(expectedDer, HEX.encode(der).toUpperCase());
        Signature parsedSignature = Signature.parse(der);
        assertEquals(signature, parsedSignature);
    }

    @Test
    // Test params from https://crypto.stackexchange.com/questions/20838/request-for-data-to-test-deterministic-ecdsa-signature-algorithm-for-secp256k1
    public void testSignatureDer19() {
        Secp256k1PrivateKey privateKey = new Secp256k1PrivateKey(new BigInteger("19584093032798730129230525910686445865718710074652466673872143043325364812985"));
        BigInteger z = getSHA256HashAsInteger("An ounce of prevention is worth a pound of cure."); // signature single-hash (for test)
        Signature signature = privateKey.sign(z);
        byte[] der = signature.getDer();
        String expectedDer = "3045022100D5CB4E148C0A29CE37F1542BE416E8EF575DA522666B19B541960D726C99662B022045C951C1CA938C90DAD6C3EEDE7C5DF67FCF0D14F90FAF201E8D215F215C5C18";
        assertEquals(expectedDer, HEX.encode(der).toUpperCase());
        Signature parsedSignature = Signature.parse(der);
        assertEquals(signature, parsedSignature);
    }

    @Test
    // Test params from https://crypto.stackexchange.com/questions/20838/request-for-data-to-test-deterministic-ecdsa-signature-algorithm-for-secp256k1
    public void testSignatureDer20() {
        Secp256k1PrivateKey privateKey = new Secp256k1PrivateKey(new BigInteger("781437121688497986836158713061237152541328908182646473971063062031575438443"));
        BigInteger z = getSHA256HashAsInteger("Appearances can be deceiving."); // signature single-hash (for test)
        Signature signature = privateKey.sign(z);
        byte[] der = signature.getDer();
        String expectedDer = "304402203E2F0118062306E2239C873828A7275DD35545A143797E224148C5BBBD59DD08022073A8C9E17BE75C66362913B5E05D81FD619B434EDDA766FAE6C352E86987809D";
        assertEquals(expectedDer, HEX.encode(der).toUpperCase());
        Signature parsedSignature = Signature.parse(der);
        assertEquals(signature, parsedSignature);
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
