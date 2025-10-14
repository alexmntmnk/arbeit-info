@Query("""
SELECT sit.objectId
FROM SendToEgaisDb sit
GROUP BY sit.objectId
HAVING 
    -- Проверяем, что во всей группе есть только одна запись 'sent'
    SUM(CASE WHEN sit.logStatus = :statusSent THEN 1 ELSE 0 END) = 1
    -- Проверяем, что во всей группе нет записей 'received'
AND SUM(CASE WHEN sit.logStatus = :statusReceived THEN 1 ELSE 0 END) = 0
    -- Убеждаемся, что в группе есть хотя бы одна запись, подходящая по дате
AND SUM(CASE WHEN sit.documentDateCreate >= :startDate THEN 1 ELSE 0 END) > 0
""")
List<String> findDocEntriesWithOneSentAndReceivedByObjectId(
    @Param("statusSent") SendingToAbsdStatus statusSent,
    @Param("statusReceived") SendingToAbsdStatus statusReceived,
    @Param("startDate") LocalDateTime startDate
);
