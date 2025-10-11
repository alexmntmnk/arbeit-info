Конечно, вот SQL, JPQL и HQL запросы для каждого из ваших пунктов.

Примечание:

В примерах SQL используется имя таблицы YourTable. Замените его на фактическое имя вашей таблицы.

В примерах JPQL (Java Persistence Query Language) и HQL (Hibernate Query Language) используется имя сущности YourEntity. Замените его на имя вашей сущности (Entity).

Для данных задач синтаксис JPQL и HQL идентичен.

1. Записи с одним SEND и без RESIVE
Надо вывести все записи с одинаковым name, у которых существует только одна запись со status равным SEND и нет ни одной записи со status равным RESIVE.

SQL
SQL

SELECT * FROM YourTable
WHERE name IN (
    SELECT name
    FROM YourTable
    GROUP BY name
    HAVING
        COUNT(CASE WHEN status = 'SEND' THEN 1 END) = 1
        AND COUNT(CASE WHEN status = 'RESIVE' THEN 1 END) = 0
);
JPQL / HQL
Фрагмент кода

SELECT t FROM YourEntity t
WHERE t.name IN (
    SELECT t2.name
    FROM YourEntity t2
    GROUP BY t2.name
    HAVING
        SUM(CASE WHEN t2.status = 'SEND' THEN 1 ELSE 0 END) = 1
        AND SUM(CASE WHEN t2.status = 'RESIVE' THEN 1 ELSE 0 END) = 0
)
2. Записи с двумя или более FIELD
Надо вывести все записи с одинаковым name, у которых существует две или более записи со status равным FIELD.

SQL
SQL

SELECT * FROM YourTable
WHERE name IN (
    SELECT name
    FROM YourTable
    WHERE status = 'FIELD'
    GROUP BY name
    HAVING COUNT(*) >= 2
);
JPQL / HQL
Фрагмент кода

SELECT t FROM YourEntity t
WHERE t.name IN (
    SELECT t2.name
    FROM YourEntity t2
    WHERE t2.status = 'FIELD'
    GROUP BY t2.name
    HAVING COUNT(t2) >= 2
)
3. Записи с более чем двумя SEND
Надо вывести все записи с одинаковым name, у которых существует более двух записей со status равным SEND.

SQL
SQL

SELECT * FROM YourTable
WHERE name IN (
    SELECT name
    FROM YourTable
    WHERE status = 'SEND'
    GROUP BY name
    HAVING COUNT(*) > 2
);
JPQL / HQL
Фрагмент кода

SELECT t FROM YourEntity t
WHERE t.name IN (
    SELECT t2.name
    FROM YourEntity t2
    WHERE t2.status = 'SEND'
    GROUP BY t2.name
    HAVING COUNT(t2) > 2
)
4. Записи с одним RESIVE
Надо вывести все записи с одинаковым name, у которых существует только одна запись со status равным RESIVE.

SQL
SQL

SELECT * FROM YourTable
WHERE name IN (
    SELECT name
    FROM YourTable
    GROUP BY name
    HAVING COUNT(CASE WHEN status = 'RESIVE' THEN 1 END) = 1
);
JPQL / HQL
Фрагмент кода

SELECT t FROM YourEntity t
WHERE t.name IN (
    SELECT t2.name
    FROM YourEntity t2
    GROUP BY t2.name
    HAVING SUM(CASE WHEN t2.status = 'RESIVE' THEN 1 ELSE 0 END) = 1
)
5. Записи с двумя или более RESIVE
Надо вывести все записи с одинаковым name, у которых существует две или более записи со status равным RESIVE.

SQL
SQL

SELECT * FROM YourTable
WHERE name IN (
    SELECT name
    FROM YourTable
    WHERE status = 'RESIVE'
    GROUP BY name
    HAVING COUNT(*) >= 2
);
JPQL / HQL
Фрагмент кода

SELECT t FROM YourEntity t
WHERE t.name IN (
    SELECT t2.name
    FROM YourEntity t2
    WHERE t2.status = 'RESIVE'
    GROUP BY t2.name
    HAVING COUNT(t2) >= 2
)
6. Количество групп с одним SEND и без RESIVE
Надо вывести количество записей с одинаковым name, у которых существует только одна запись со status равным SEND и нет ни одной записи со status равным RESIVE. (Имеется в виду количество уникальных name, удовлетворяющих условию).

SQL
SQL

SELECT COUNT(*) FROM (
    SELECT name
    FROM YourTable
    GROUP BY name
    HAVING
        COUNT(CASE WHEN status = 'SEND' THEN 1 END) = 1
        AND COUNT(CASE WHEN status = 'RESIVE' THEN 1 END) = 0
) AS subquery;
JPQL / HQL
Фрагмент кода

SELECT COUNT(DISTINCT t.name)
FROM YourEntity t
WHERE t.name IN (
    SELECT t2.name
    FROM YourEntity t2
    GROUP BY t2.name
    HAVING
        SUM(CASE WHEN t2.status = 'SEND' THEN 1 ELSE 0 END) = 1
        AND SUM(CASE WHEN t2.status = 'RESIVE' THEN 1 ELSE 0 END) = 0
)
7. Количество групп с двумя или более FIELD
Надо вывести количество записей с одинаковым name, у которых существует две или более записи со status равным FIELD.

SQL
SQL

SELECT COUNT(*) FROM (
    SELECT name
    FROM YourTable
    WHERE status = 'FIELD'
    GROUP BY name
    HAVING COUNT(*) >= 2
) AS subquery;
JPQL / HQL
Фрагмент кода

SELECT COUNT(DISTINCT t.name)
FROM YourEntity t
WHERE t.name IN (
    SELECT t2.name
    FROM YourEntity t2
    WHERE t2.status = 'FIELD'
    GROUP BY t2.name
    HAVING COUNT(t2) >= 2
)
8. Количество групп с более чем двумя SEND
Надо вывести количество записей с одинаковым name, у которых существует более двух записей со status равным SEND.

SQL
SQL

SELECT COUNT(*) FROM (
    SELECT name
    FROM YourTable
    WHERE status = 'SEND'
    GROUP BY name
    HAVING COUNT(*) > 2
) AS subquery;
JPQL / HQL
Фрагмент кода

SELECT COUNT(DISTINCT t.name)
FROM YourEntity t
WHERE t.name IN (
    SELECT t2.name
    FROM YourEntity t2
    WHERE t2.status = 'SEND'
    GROUP BY t2.name
    HAVING COUNT(t2) > 2
)
9. Количество групп с одним RESIVE
Надо вывести количество записей с одинаковым name, у которых существует только одна запись со status равным RESIVE.

SQL
SQL

SELECT COUNT(*) FROM (
    SELECT name
    FROM YourTable
    GROUP BY name
    HAVING COUNT(CASE WHEN status = 'RESIVE' THEN 1 END) = 1
) AS subquery;
JPQL / HQL
Фрагмент кода

SELECT COUNT(DISTINCT t.name)
FROM YourEntity t
WHERE t.name IN (
    SELECT t2.name
    FROM YourEntity t2
    GROUP BY t2.name
    HAVING SUM(CASE WHEN t2.status = 'RESIVE' THEN 1 ELSE 0 END) = 1
)
10. Количество групп с двумя или более RESIVE
Надо вывести количество записей с одинаковым name, у которых существует две или более записи со status равным RESIVE.

SQL
SQL

SELECT COUNT(*) FROM (
    SELECT name
    FROM YourTable
    WHERE status = 'RESIVE'
    GROUP BY name
    HAVING COUNT(*) >= 2
) AS subquery;
JPQL / HQL
Фрагмент кода

SELECT COUNT(DISTINCT t.name)
FROM YourEntity t
WHERE t.name IN (
    SELECT t2.name
    FROM YourEntity t2
    WHERE t2.status = 'RESIVE'
    GROUP BY t2.name
    HAVING COUNT(t2) >= 2
)
