package org.ethereum.miner;

import org.apache.commons.lang3.tuple.Pair;
import org.ethereum.core.Block;
import org.ethereum.mine.Ethash;
import org.ethereum.mine.EthashAlgo;
import org.ethereum.util.ByteUtil;
import org.ethereum.util.FastByteComparisons;
import org.junit.Assert;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.ethereum.crypto.HashUtil.sha3;
import static org.ethereum.util.ByteUtil.*;
import static org.junit.Assert.assertArrayEquals;

/**
 * Created by Anton Nashatyrev on 02.12.2015.
 */
public class EthashTest {
    @Test // check exact values
    public void test_0() {
        byte[] rlp = Hex.decode("f9021af90215a0809870664d9a43cf1827aa515de6374e2fad1bf64290a9f261dd49c525d6a0efa01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d4934794f927a40c8b7f6e07c5af7fa2155b4864a4112b13a010c8ec4f62ecea600c616443bcf527d97e5b1c5bb4a9769c496d1bf32636c95da056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421a056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421b901000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000086015a1c28ae5e82bf958302472c808455c4e47b99476574682f76312e302e312f6c696e75782f676f312e342e32a0788ac534cb2f6a226a01535e29b11a96602d447aed972463b5cbcc7dd5d633f288e2ff1b6435006517c0c0");
        Block b = new Block(rlp);

        EthashAlgo ethash = new EthashAlgo();
        long cacheSize = ethash.getParams().getCacheSize(b.getNumber());
        long fullSize = ethash.getParams().getFullSize(b.getNumber());
        byte[] seedHash = ethash.getSeedHash(b.getNumber());
        byte[][] cache = ethash.makeCache(cacheSize, seedHash);
        byte[] blockTrunkHash = sha3(b.getHeader().getEncodedWithoutNonce());

        long nonce = ByteUtil.byteArrayToLong(b.getNonce());
        long timeSum = 0;
        for (int i = 0; i < 100; i++) {
            long s = System.currentTimeMillis();
            Pair<byte[], byte[]> pair = ethash.hashimotoLight(fullSize, cache, blockTrunkHash, longToBytes(nonce));
            timeSum += System.currentTimeMillis() - s;
            System.out.println("Time: " + (System.currentTimeMillis() - s));
            nonce++;
        }

        Assert.assertTrue("hashimotoLigt took > 500ms in avrg", timeSum / 100 < 500);

        Pair<byte[], byte[]> pair = ethash.hashimotoLight(fullSize, cache, blockTrunkHash, b.getNonce());

        System.out.println(Hex.toHexString(pair.getLeft()));
        System.out.println(Hex.toHexString(pair.getRight()));

        byte[] boundary = b.getHeader().getPowBoundary();
        byte[] pow = b.getHeader().calcPowValue();

        assertArrayEquals(Hex.decode("0000000000bd59a74a8619f14c3d793747f1989a29ed6c83a5a488bac185679b"), boundary);
        assertArrayEquals(Hex.decode("000000000017f78925469f2f18fe7866ef6d3ed28d36fb013bc93d081e05809c"), pow);
        assertArrayEquals(pow, pair.getRight());
    }

    @Test
    public void cacheTest() {
        EthashAlgo ethash = new EthashAlgo();
        byte[] seed = "~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~".getBytes();
        long cacheSize = 1024;
        long fullSize = 1024 * 32;
        byte[][] cache = ethash.makeCache(cacheSize, seed);

        Assert.assertArrayEquals(merge(cache), Hex.decode("2da2b506f21070e1143d908e867962486d6b0a02e31d468fd5e3a7143aafa76a14201f63374314e2a6aaf84ad2eb57105dea3378378965a1b3873453bb2b78f9a8620b2ebeca41fbc773bb837b5e724d6eb2de570d99858df0d7d97067fb8103b21757873b735097b35d3bea8fd1c359a9e8a63c1540c76c9784cf8d975e995ca8620b2ebeca41fbc773bb837b5e724d6eb2de570d99858df0d7d97067fb8103b21757873b735097b35d3bea8fd1c359a9e8a63c1540c76c9784cf8d975e995ca8620b2ebeca41fbc773bb837b5e724d6eb2de570d99858df0d7d97067fb8103b21757873b735097b35d3bea8fd1c359a9e8a63c1540c76c9784cf8d975e995c259440b89fa3481c2c33171477c305c8e1e421f8d8f6d59585449d0034f3e421808d8da6bbd0b6378f567647cc6c4ba6c434592b198ad444e7284905b7c6adaf70bf43ec2daa7bd5e8951aa609ab472c124cf9eba3d38cff5091dc3f58409edcc386c743c3bd66f92408796ee1e82dd149eaefbf52b00ce33014a6eb3e50625413b072a58bc01da28262f42cbe4f87d4abc2bf287d15618405a1fe4e386fcdafbb171064bd99901d8f81dd6789396ce5e364ac944bbbd75a7827291c70b42d26385910cd53ca535ab29433dd5c5714d26e0dce95514c5ef866329c12e958097e84462197c2b32087849dab33e88b11da61d52f9dbc0b92cc61f742c07dbbf751c49d7678624ee60dfbe62e5e8c47a03d8247643f3d16ad8c8e663953bcda1f59d7e2d4a9bf0768e789432212621967a8f41121ad1df6ae1fa78782530695414c6213942865b2730375019105cae91a4c17a558d4b63059661d9f108362143107babe0b848de412e4da59168cce82bfbff3c99e022dd6ac1e559db991f2e3f7bb910cefd173e65ed00a8d5d416534e2c8416ff23977dbf3eb7180b75c71580d08ce95efeb9b0afe904ea12285a392aff0c8561ff79fca67f694a62b9e52377485c57cc3598d84cac0a9d27960de0cc31ff9bbfe455acaa62c8aa5d2cce96f345da9afe843d258a99c4eaf3650fc62efd81c7b81cd0d534d2d71eeda7a6e315d540b4473c80f8730037dc2ae3e47b986240cfc65ccc565f0d8cde0bc68a57e39a271dda57440b3598bee19f799611d25731a96b5dbbbefdff6f4f656161462633030d62560ea4e9c161cf78fc96a2ca5aaa32453a6c5dea206f766244e8c9d9a8dc61185ce37f1fc804459c5f07434f8ecb34141b8dcae7eae704c950b55556c5f40140c3714b45eddb02637513268778cbf937a33e4e33183685f9deb31ef54e90161e76d969587dd782eaa94e289420e7c2ee908517f5893a26fdb5873d68f92d118d4bcf98d7a4916794d6ab290045e30f9ea00ca547c584b8482b0331ba1539a0f2714fddc3a0b06b0cfbb6a607b8339c39bcfd6640b1f653e9d70ef6c985b"));
        byte[] bytes = ethash.calcDatasetItem(cache, 0);
        Assert.assertArrayEquals(bytes, Hex.decode("b1698f829f90b35455804e5185d78f549fcb1bdce2bee006d4d7e68eb154b596be1427769eb1c3c3e93180c760af75f81d1023da6a0ffbe321c153a7c0103597"));

        byte[] blockHash = "~~~X~~~~~~~~~~~~~~~~~~~~~~~~~~~~".getBytes();
        long nonce = 0x7c7c597cL;
        Pair<byte[], byte[]> pair = ethash.hashimotoLight(fullSize, cache, blockHash, longToBytes(nonce));

        // comparing mix hash
        Assert.assertArrayEquals(pair.getLeft(), Hex.decode("d7b668b90c2f26961d98d7dd244f5966368165edbce8cb8162dd282b6e5a8eae"));
        // comparing the final hash
        Assert.assertArrayEquals(pair.getRight(), Hex.decode("b8cb1cb3ac1a7a6e12c4bc90f2779ef97e661f7957619e677636509d2f26055c"));

        System.out.println(Hex.toHexString(pair.getLeft()));
        System.out.println(Hex.toHexString(pair.getRight()));
    }

    @Test
    public void realBlockValidateTest() {
        byte[] rlp = Hex.decode("f9021af90215a0809870664d9a43cf1827aa515de6374e2fad1bf64290a9f261dd49c525d6a0efa01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d4934794f927a40c8b7f6e07c5af7fa2155b4864a4112b13a010c8ec4f62ecea600c616443bcf527d97e5b1c5bb4a9769c496d1bf32636c95da056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421a056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421b901000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000086015a1c28ae5e82bf958302472c808455c4e47b99476574682f76312e302e312f6c696e75782f676f312e342e32a0788ac534cb2f6a226a01535e29b11a96602d447aed972463b5cbcc7dd5d633f288e2ff1b6435006517c0c0");

        Block b = new Block(rlp);

        EthashAlgo ethash = new EthashAlgo();
        long cacheSize = ethash.getParams().getCacheSize(b.getNumber());
        long fullSize = ethash.getParams().getFullSize(b.getNumber());
        byte[] seedHash = ethash.getSeedHash(b.getNumber());
        long s = System.currentTimeMillis();
        byte[][] cache = ethash.makeCache(cacheSize, seedHash);
        System.out.println("Cache generation took: " + (System.currentTimeMillis() - s) + " ms");
        byte[] blockTruncHash = sha3(b.getHeader().getEncodedWithoutNonce());

        Pair<byte[], byte[]> pair = ethash.hashimotoLight(fullSize, cache, blockTruncHash, b.getNonce());

        System.out.println(Hex.toHexString(pair.getLeft()));
        System.out.println(Hex.toHexString(pair.getRight()));

        byte[] boundary = b.getHeader().getPowBoundary();

        Assert.assertTrue(FastByteComparisons.compareTo(pair.getRight(), 0, 32, boundary, 0, 32) < 0);
    }


    @Test
    public void blockMineTest()throws Exception {
       byte[] rlp = Hex.decode("f9021af90215a0809870664d9a43cf1827aa515de6374e2fad1bf64290a9f261dd49c525d6a0efa01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d4934794f927a40c8b7f6e07c5af7fa2155b4864a4112b13a010c8ec4f62ecea600c616443bcf527d97e5b1c5bb4a9769c496d1bf32636c95da056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421a056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421b90100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000008600000000010082bf958302472c808455c4e47b99476574682f76312e302e312f6c696e75782f676f312e342e32a0788ac534cb2f6a226a01535e29b11a96602d447aed972463b5cbcc7dd5d633f288e2ff1b6435006517c0c0");
        Block b = new Block(rlp);

        long nonce = Ethash.getForBlock(b.getNumber()).mineLight(b.getHeader()).get();
        b.setNonce(intToBytes((int) nonce));

        Assert.assertTrue(Ethash.getForBlock(b.getNumber()).validate(b.getHeader()));

    }

    @Test
    public void mineCancelTest() throws Exception {
        byte[] rlp = Hex.decode("f9021af90215a0809870664d9a43cf1827aa515de6374e2fad1bf64290a9f261dd49c525d6a0efa01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d4934794f927a40c8b7f6e07c5af7fa2155b4864a4112b13a010c8ec4f62ecea600c616443bcf527d97e5b1c5bb4a9769c496d1bf32636c95da056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421a056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421b90100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000008600000000010082bf958302472c808455c4e47b99476574682f76312e302e312f6c696e75782f676f312e342e32a0788ac534cb2f6a226a01535e29b11a96602d447aed972463b5cbcc7dd5d633f288e2ff1b6435006517c0c0");
        // small difficulty
        Block b = new Block(rlp);

        // large difficulty
        Block difficultBlock = new Block(Hex.decode("f9021af90215a0809870664d9a43cf1827aa515de6374e2fad1bf64290a9f261dd49c525d6a0efa01dcc4de8dec75d7aab85b567b6ccd41ad312451b948a7413f0a142fd40d4934794f927a40c8b7f6e07c5af7fa2155b4864a4112b13a010c8ec4f62ecea600c616443bcf527d97e5b1c5bb4a9769c496d1bf32636c95da056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421a056e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421b90100000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000008600000001000082bf958302472c808455c4e47b99476574682f76312e302e312f6c696e75782f676f312e342e32a0788ac534cb2f6a226a01535e29b11a96602d447aed972463b5cbcc7dd5d633f288e2ff1b6435006517c0c0"));

        // first warming up for the cache to be created
        System.out.println("Warming...");
        long res = Ethash.getForBlock(b.getNumber()).mineLight(b.getHeader()).get();

        System.out.println("Submitting...");
        Future<Long> light = Ethash.getForBlock(b.getNumber()).mineLight(difficultBlock.getHeader());
        long s = System.nanoTime();

        boolean cancel = light.cancel(true);
        try {
            System.out.println("Waiting");
            light.get();
            Assert.assertTrue(false);
        } catch (InterruptedException|ExecutionException|CancellationException e) {
            System.out.println("Exception: ok");
        }

        long t = System.nanoTime() - s;
        System.out.println("Time: " + (t / 1000) + " usec");
        Assert.assertTrue(cancel);
        Assert.assertTrue(t < 500_000_000);
        Assert.assertTrue(light.isCancelled());
    }
}
