@Query("""
    SELECT slt.objectId
    FROM SendLogToAbed slt
    WHERE slt.logStatus = :status
      AND slt.documentDateCreate >= :startDate
    GROUP BY slt.objectId
    HAVING COUNT(slt) >= 2
""")
List<String> findObjectIdsWithTwoOrMoreStatuses(
    @Param("status") SendLogToAbedStatus status,
    @Param("startDate") LocalDateTime startDate
);

@Query("""
    SELECT COUNT(a.objectId) FROM (
        SELECT slt.objectId
        FROM SendLogToAbed slt
        WHERE slt.logStatus = :status
          AND slt.documentDateCreate >= :startDate
        GROUP BY slt.objectId
        HAVING COUNT(slt) >= 2
    ) a
""")
Long countObjectsWithTwoOrMoreStatuses(
    @Param("status") SendLogToAbedStatus status,
    @Param("startDate") LocalDateTime startDate
);
