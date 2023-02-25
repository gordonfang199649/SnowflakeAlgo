package org.example;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 雪花演算法
 * @author gordonfang
 * @date 2023/02/25
 */
public class SnowFlakeAlg {
    /** 位元分配器 */
    private final org.example.BitsAllocator bitsAllocator;
    /** 現在取到的號碼 */
    private long sequence = 0L;
    /** 上次產製 ID 的時間截 */
    private long lastTimestamp = -1L;
    /** 工作站台 */
    private int workerId;
    /** 起始時間戳(2022/04/09) */
    private final long epochTime = 1649462400;

    /**
     * 建構子
     * <ul>
     *     <li>MST(Most significant bit) 為 signBit，佔 1 bit</li>
     *     <li>時間戳記(單位：毫秒)佔 41 bits</li>
     *     <li>站台 ID 佔 8 bits</li>
     *     <li>流水號佔 14 bits</li>
     * </ul>
     * @param timestampBits 時間戳記位元長度
     * @param workerIdBits  站台位元長度
     * @param sequenceBits  流水號位元長度
     */
    public SnowFlakeAlg(int timestampBits, int workerIdBits, int sequenceBits) throws Exception {
        // 建立位元分配器的實體
        bitsAllocator = new org.example.BitsAllocator(timestampBits, workerIdBits, sequenceBits);
        // 設定工作站台 ID
        setWorkerId();
        // 檢查工作站台 ID 是否合法
        verifyWorkerId();
    }

    /**
     * 配發 ID
     * @return id
     */
    public synchronized long nextId() {
        long timestamp = getCurrentTimestamp();

        // 如果目前的時戳小於上一次產 UID 的時戳，說明系統時脈有回退現象，會拋 RuntimeException 。
        if (timestamp < lastTimestamp) {
            throw new RuntimeException(
                    String.format("因逆向時脈，無法在 %d 毫秒差產製 UID。", lastTimestamp - timestamp));
        }

        // 在同一個毫秒下，流水號遞增取號
        if (timestamp == lastTimestamp) {
            // 流水號 + 1 後與最大流水號(14 個位元全為 1 的二進位的值)進行 AND 運算
            // 若流水號遞增後，做完遮罩後結果值會為 0
            sequence = (sequence + 1) & bitsAllocator.getMaxSequence();
            // 流水號全數發放完畢，重新取得下一個時戳
            if (sequence == 0) {
                timestamp = nextMilliSecond(lastTimestamp);
            }
        } else {
            // 新的一毫秒，重新分配流水號
            sequence = 0L;
        }

        lastTimestamp = timestamp;
        return bitsAllocator.allocate(timestamp - epochTime, workerId, sequence);
    }

    /**
     * 會呼叫此方法原因在上一毫秒的流水號已經分配完，所以要循環等待到下一毫秒
     * @param lastTimestamp 上次的時間戳記
     * @return 下一個時間戳記
     */
    private long nextMilliSecond(long lastTimestamp) {
        long timestamp = getCurrentTimestamp();
        while (timestamp <= lastTimestamp) {
            timestamp = getCurrentTimestamp();
        }
        return timestamp;
    }

    /**
     * 回傳以毫秒為單位系統目前的時間
     * @return 系統目前的時間(單位 ： 毫秒)
     */
    private long getCurrentTimestamp() {
        long currentTimestamp = System.currentTimeMillis();
        if (currentTimestamp - epochTime > bitsAllocator.getMaxDeltaMilliSeconds()) {
            throw new RuntimeException("因時戳佔用位元已耗盡，無法產製 UID。目前時戳：" + currentTimestamp);
        }
        return currentTimestamp;
    }

    /**
     * 設定工作站台 (IP 最後位)
     */
    private void setWorkerId() throws Exception {
        // 抓取部屬機台的 IP(網路位址 + 主機位址)
        String ip = null;
        try {
            ip = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException ignored) {
        }

        // 以 IPv4 主機位址最後一欄設為站台 ID
        if (!"".equals(ip) && null != ip) {
            workerId = Integer.parseInt(ip.split("\\.")[3]);
        } else {
            throw new Exception("取得不到主機位址，無法產生工作站站台 ID");
        }
    }

    /**
     * 檢查最大工作站ID
     * @throws IllegalArgumentException 工作站 ID 必須為 1 或是 IP 第四位
     */
    private void verifyWorkerId() {
        long maxWorkerId = bitsAllocator.getMaxWorkerId();
        if (workerId > maxWorkerId || workerId < 1) {
            throw new IllegalArgumentException(String.format("Worker Id 不能比 %d 大，或比 1 小。", maxWorkerId));
        }
    }
}