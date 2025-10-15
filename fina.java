@Query("""
    SELECT count(a.objectId) FROM (
        SELECT slt.objectId
        FROM SendLogToAbed slt
        WHERE slt.documentDateCreate >= :startDate
        GROUP BY slt.objectId
        HAVING COUNT(CASE WHEN slt.logStatus = :statusSent THEN 1 END) = 1
           AND COUNT(CASE WHEN slt.logStatus = :statusReceived THEN 1 END) = 0
    ) a
""")
Long countDocumentWithExactlyOneSendAndNoReceive(
    @Param("statusSent") SendLogToAbedStatus statusSent,
    @Param("statusReceived") SendLogToAbedStatus statusReceived,
    @Param("startDate") LocalDateTime startDate
);

https://github.com/PacktPublishing/Learn-Java-17-Programming
