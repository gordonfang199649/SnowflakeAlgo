package org.example;

/**
 * 位元分配器
 * @author gordonfang
 * @date 2023/02/25
 */
public class BitsAllocator {
    /** 符號位元長度 */
    private final int signBit = 1;
    /** 時間戳記位元長度 */
    private final int timestampBits;
    /** 站台位元長度 */
    private final int workerIdBits;
    /** 流水號位元長度 */
    private final int sequenceBits;
    /** 最大時間差 */
    private final long maxDeltaMilliSeconds;
    /** 站台最大配置數 */
    private final long maxWorkerId;
    /** 最大序號數 */
    private final long maxSequence;
    /** 時間截位元位移個數 */
    private final long timestampBitShift;
    /** 站台 ID 位元位移個數 */
    private final long workerIdBitShift;

    /**
     * 建構子
     * @param timestampBits 時間戳記位元長度
     * @param workerIdBits 站台位元長度
     * @param sequenceBits 流水號位元長度
     */
    public BitsAllocator(int timestampBits, int workerIdBits, int sequenceBits) {
        // 時間戳記位元長度
        this.timestampBits = timestampBits;
        // 站台位元長度
        this.workerIdBits = workerIdBits;
        // 流水號位元長度
        this.sequenceBits = sequenceBits;

        // 製作位元遮罩
        this.maxDeltaMilliSeconds = ~(-1L << timestampBits);
        this.maxWorkerId = ~(-1L << workerIdBits);
        this.maxSequence = ~(-1L << sequenceBits);

        // 位元位移
        this.timestampBitShift = workerIdBits + sequenceBits;
        this.workerIdBitShift = sequenceBits;
    }

    /**
     * 配置位元，產生序號
     * @param deltaMillSeconds 時間戳記
     * @param workerId 工作站 ID
     * @param sequence 流水號
     * @return 序號
     */
    public long allocate(long deltaMillSeconds, long workerId, long sequence) {
        return (deltaMillSeconds << timestampBitShift) | (workerId << workerIdBitShift) | sequence;
    }

    public int getTimestampBits() {
        return timestampBits;
    }

    public int getWorkerIdBits() {
        return workerIdBits;
    }

    public int getSequenceBits() {
        return sequenceBits;
    }

    public long getMaxDeltaMilliSeconds() {
        return maxDeltaMilliSeconds;
    }

    public long getMaxWorkerId() {
        return maxWorkerId;
    }

    public long getMaxSequence() {
        return maxSequence;
    }

    public long getTimestampBitShift() {
        return timestampBitShift;
    }

    public long getWorkerIdBitShift() {
        return workerIdBitShift;
    }
}
