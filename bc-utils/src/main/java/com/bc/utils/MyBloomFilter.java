package com.bc.utils;

import com.google.common.hash.Funnels;
import com.google.common.hash.Hashing;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.nio.charset.Charset;
import java.util.List;

public class MyBloomFilter {
    //计算hash函数个数 方法来自guava
    private static int optimalNumOfHashFunctions(long expectedInsertions, long numBits) throws Exception{
        return Math.max(1, (int) Math.round((double) numBits / expectedInsertions * Math.log(2)));
    }

    //计算bit数组长度 方法来自guava
    private static long optimalNumOfBits(long expectedInsertions, double fpp) throws Exception{
        if (fpp == 0) {
            fpp = Double.MIN_VALUE;
        }
        return (long) (-expectedInsertions * Math.log(fpp) / (Math.log(2) * Math.log(2)));
    }

    /**
     * 判断keys是否存在于集合where中
     */
    public static boolean isExist(String where, String key,long expectedInsertions,double fpp, StringRedisTemplate redisTemplate) throws Exception{
        long[] indexs = getIndexs(key,expectedInsertions,fpp);
        boolean result;
        //这里使用了Redis管道来降低过滤器运行当中访问Redis次数 降低Redis并发量
        List<Object> objects = redisTemplate.executePipelined(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection conn) throws DataAccessException {
                for (long index : indexs) {
                    conn.getBit(where.getBytes(), index);
                }
                return null;
            }
        });
        for (Object obj : objects) {
            Boolean flag = (Boolean) obj;
            if (!flag) return false;
        }
        return true;
    }

    /**
     * 将key存入redis bitmap
     */
    public static void put(String where, String key, long expectedInsertions,double fpp,StringRedisTemplate redisTemplate) throws Exception{
        long[] indexs = getIndexs(key,expectedInsertions,fpp);
        //这里使用了Redis管道来降低过滤器运行当中访问Redis次数 降低Redis并发量
        redisTemplate.executePipelined(new RedisCallback<Object>() {
            @Override
            public Object doInRedis(RedisConnection conn) throws DataAccessException {
                for (long index : indexs) {
                    conn.setBit(where.getBytes(),index,true);
                }
                return null;
            }
        });
    }

    /**
     * 根据key获取bitmap下标 方法来自guava
     */
    private static long[] getIndexs(String key,long expectedInsertions,double fpp) throws Exception{
        //bit数组长度
        long numBits = optimalNumOfBits(expectedInsertions, fpp);
        //hash函数数量
        int numHashFunctions = optimalNumOfHashFunctions(expectedInsertions, numBits);

        long hash1 = hash(key);
        long hash2 = hash1 >>> 16;
        long[] result = new long[numHashFunctions];
        for (int i = 0; i < numHashFunctions; i++) {
            long combinedHash = hash1 + i * hash2;
            if (combinedHash < 0) {
                combinedHash = ~combinedHash;
            }
            result[i] = combinedHash % numBits;
        }
        return result;
    }

    /**
     * 获取一个hash值 方法来自guava
     */
    private static long hash(String key) throws Exception{
        Charset charset = Charset.forName("UTF-8");
        return Hashing.murmur3_128().hashObject(key, Funnels.stringFunnel(charset)).asLong();
    }
}
