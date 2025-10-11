1. Только одна запись "SEND" и ни одной "RESIVE"
Для выбора записей с одинаковым name, у которых есть ровно одна запись со status 'SEND' и ни одной со status 'RESIVE', вы можете использовать следующий запрос:

SQL

SELECT *
FROM your_table
WHERE name IN (
    SELECT name
    FROM your_table
    GROUP BY name
    HAVING COUNT(CASE WHEN status = 'SEND' THEN 1 END) = 1
       AND COUNT(CASE WHEN status = 'RESIVE' THEN 1 END) = 0
);
Как это работает:

GROUP BY name: Группирует все записи по одинаковому name.

HAVING COUNT(CASE WHEN status = 'SEND' THEN 1 END) = 1: Подсчитывает количество записей со статусом 'SEND' в каждой группе и отбирает те группы, где это количество равно 1.

AND COUNT(CASE WHEN status = 'RESIVE' THEN 1 END) = 0: Дополнительно проверяет, что в этих же группах нет ни одной записи со статусом 'RESIVE'.

SELECT * FROM your_table WHERE name IN (...): Выбирает все исходные записи, name которых соответствует найденным группам.

2. Две записи "FIELD"
Чтобы вывести все записи с одинаковым name, у которых существует ровно две записи со status 'FIELD', используйте этот запрос:

SQL

SELECT *
FROM your_table
WHERE name IN (
    SELECT name
    FROM your_table
    WHERE status = 'FIELD'
    GROUP BY name
    HAVING COUNT(*) = 2
);
Как это работает:

WHERE status = 'FIELD': Сначала отбираются только записи со статусом 'FIELD'.

GROUP BY name: Затем они группируются по name.

HAVING COUNT(*) = 2: Отбираются те группы, в которых ровно две записи.

SELECT * FROM your_table WHERE name IN (...): Как и в первом случае, этот внешний запрос выбирает все записи, name которых попало в отфильтрованный список.
