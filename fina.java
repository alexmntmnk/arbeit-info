@Query("""
    SELECT sit.objectId
    FROM SendToEgaisDb sit
    WHERE sit.documentDateCreate >= :startDate
    GROUP BY sit.objectId
    HAVING COUNT(CASE WHEN sit.logStatus = :statusSent THEN 1 END) = 1
       AND COUNT(CASE WHEN sit.logStatus = :statusReceived THEN 1 END) = 0
""")
List<String> findObjectIdsWithOneSentAndNoReceived(
    @Param("statusSent") SendingToAbsdStatus statusSent,
    @Param("statusReceived") SendingToAbsdStatus statusReceived,
    @Param("startDate") LocalDateTime startDate
);
